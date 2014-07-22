'''
@package l3_routing

This is the sample code for realizing the multicast and 
fast failover using group table features

'''

from ryu.base import app_manager
from ryu.controller import dpset
from ryu.controller import mac_to_network
from ryu.controller import mac_to_port
from ryu.controller import network
from ryu.controller import ofp_event

from ryu.controller.handler import MAIN_DISPATCHER
from ryu.controller.handler import CONFIG_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.lib import stplib

from ryu.ofproto import ofproto_v1_3
from ryu.ofproto import ofproto_v1_3_parser
from ryu.ofproto import ether
from ryu.lib.packet import packet
from ryu.lib.packet import ethernet
from ryu.lib import dpid
from ryu.lib import ofctl_v1_3

from ryu import cfg
from ryu.topology   import switches, event

from subprocess import call
import sys, signal

def exit_handler(signum, frame):
    print '\nExit controller'
    sys.exit()

signal.signal(signal.SIGINT, exit_handler)

class l3_routing(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]
    table_dict = {}
    dp_dict = {}
    sw_name_list = []
    dp_port_dict = {}
    dpid_xid_dict = {}

    ''' application initialization '''
    def __init__(self, *args, **kwargs):
	super(l3_routing, self).__init__(*args, **kwargs)		
	self.change_of_mod( self.get_sw_names() )

    ''' Set a switch's OF version by force '''
    def ofcompatible(self, swname, ofver):
	ofver_str = 'OpenFlow' + ofver
	call(["sudo", "ovs-vsctl","set","bridge", swname, "protocols="+ofver_str])

    ''' Change all switches' OF version to 1.3'''
    def change_of_mod(self, sw_name_list):
	for sw_name in sw_name_list:
		self.ofcompatible(sw_name, '13')

    ''' Generate CIDR like submask by giving mask bit'''
    def gen_mask(self, maskbit, type = 'p'):
	if ( type == 'p' and maskbit == 24 ):
		submask = '255.255.255.0'
	elif ( type == 'p' and maskbit == 16 ):
		submask = '255.255.0.0'
	elif ( type == 'p' and maskbit == 8 ):
		submask = '255.0.0.0'
	elif ( maskbit == 0 ):
		submask = '0.0.0.0'
	elif ( maskbit == 32 ):
		submask = '255.255.255.255'
	elif ( type == 's' and maskbit == 32 ):
		submask = '255.255.255.255'
	elif ( type == 's' and maskbit == 24 ):
		submask = '0.255.255.255'
	elif ( type == 's' and maskbit == 16 ):
		submask = '0.0.255.255'
	elif ( type == 's' and maskbit == 8 ):
		submask = '0.0.0.255'
	else:
		submask = '255.255.255.255'
	return submask

    ''' Generate IP address giving region and subnet idx '''
    def gen_ip( self, podidx, subnetidx ):
	return PREFIX + '.' + str(podidx) + '.' + str(subnetidx) + '.' + '0'

    ''' Generate DPID giving region and subnet idx '''
    def gen_dpid( self, podidx, subnetidx, swidx ):
	dpid_str = '%02d%02d%02d' % (podidx, subnetidx, swidx)
	zeros = '0' * (LEN_DPID - len(dpid_str))
	return zeros + dpid_str

    ''' Generate switch name giving pod and subnet idx '''
    def gen_swname( self, podidx, subnetidx, swidx):
	sw_name = 's%02d_%02d_%02d' % (podidx, subnetidx, swidx)
	return sw_name

    ''' Obtain all switches name as a list '''
    def get_sw_names(self):
	k = 4
	sw_name_list = []
	# edge & aggregation switches
	for i in range(0, k):
	  for j in range(0, k):
		sc = 's%i_%i_%i' % (i,j,1)
		sw_name_list.append(sc)

	# core switches
	for i in range(1,k/2+1):
	  for j in range(1,k/2+1):
		sc = 's%i_%i_%i' % (k,i,j)
		sw_name_list.append(sc)

	return sw_name_list

    @set_ev_cls(dpset.EventDP, dpset.DPSET_EV_DISPATCHER)
    def init_flow_tables(self, ev):
	dp = ev.dp
	dpid_str = dpid.dpid_to_str(dp.id);
	ports = ev.ports
	ofproto = dp.ofproto
	parser = dp.ofproto_parser
	priority = 0
	group_id = 1
	if (ev.enter == True ):		
		self.logger.info('Switch ' + dpid_str + ' detected!')
		if ( dpid_str == '0000000000000001' ): # only enable multicast for switch 0000000000000001
			ports = [1,2]
			grp_buckets = []
			for port in ports:
				grp_act = [parser.OFPActionOutput(port, 2000)]
				tmp_bucket = parser.OFPBucket(actions = grp_act)
				grp_buckets.append(tmp_bucket)
			
			self.add_all_group_entry( dp, group_id, grp_buckets )

			meta_info = ['240.0.0.1', group_id]
			self.gen_flow_entry_group(dp, parser, priority, meta_info)

    ''' Generate flow entry with CIDR mask '''
    def gen_flow_entry_masked(self, datapath, parser, priority, meta):
	ip_match = parser.OFPMatch()
	dst_int = self.ipv4_to_int(meta[0])
	mask_int = self.ipv4_to_int(meta[1])
	ip_match.set_dl_type(ether.ETH_TYPE_IP)
	ip_match.set_ipv4_dst_masked(dst_int,mask_int)

	arp_match = parser.OFPMatch()
	arp_match.set_dl_type(ether.ETH_TYPE_ARP)
	arp_match.set_arp_tpa_masked(dst_int,mask_int)
	action = [parser.OFPActionOutput(meta[2], 0)]

	self.add_flow_entry(datapath, priority, ip_match, action)
	self.add_flow_entry(datapath, priority, arp_match, action)

    ''' Generate flow entry '''
    def gen_flow_entry(self, datapath, parser, priority, meta):
	ip_match = parser.OFPMatch(eth_type=ether.ETH_TYPE_IP,ipv4_dst=meta[0])
	arp_match = parser.OFPMatch(eth_type=ether.ETH_TYPE_ARP,arp_tpa=meta[0])
	action = [parser.OFPActionOutput(meta[1], 0)]

	self.add_flow_entry(datapath, priority, ip_match, action)
	self.add_flow_entry(datapath, priority, arp_match, action)

    ''' Generate flow entry with one group table '''
    def gen_flow_entry_group(self, datapath, parser, priority, meta):
	ip_match = parser.OFPMatch(eth_type=ether.ETH_TYPE_IP, ipv4_dst=meta[0])
	arp_match = parser.OFPMatch(eth_type=ether.ETH_TYPE_ARP, arp_tpa=meta[0])
	action = [parser.OFPActionGroup(meta[1])]

	self.add_flow_entry(datapath, priority, ip_match, action)
	self.add_flow_entry(datapath, priority, arp_match, action)

    ''' Generate flow entry with one meter table '''
    def gen_flow_entry_meter(self, datapath, parser, priority, meta):
	ip_match = parser.OFPMatch(eth_type=ether.ETH_TYPE_IP, ipv4_dst=meta[0])
	arp_match = parser.OFPMatch(eth_type=ether.ETH_TYPE_ARP, arp_tpa=meta[0])
	action = [parser.OFPActionOutput(meta[1], 0)]

	self.add_flow_entry_with_meter(datapath, priority, ip_match, meta[2], action)
	self.add_flow_entry_with_meter(datapath, priority, arp_match, meta[2], action)

    ''' Add flow entry which points to a meter id '''
    def add_flow_entry_with_meter(self, datapath, priority, match, meter_id, actions):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS, actions), 
		parser.OFPInstructionMeter(meter_id)]
	idle_timeout = hard_timeout = 3600 # preserve the flow entries for long time

	mod = parser.OFPFlowMod( datapath=datapath, priority=priority, idle_timeout=idle_timeout, 
								hard_timeout=hard_timeout, match=match, instructions=inst)
	datapath.send_msg(mod)

    ''' Insert a new flow entry '''
    def add_flow_entry(self, datapath, priority, match, actions):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS,
										 actions)]
	idle_timeout = hard_timeout = 3600 # preserve the flow entries for long time

	mod = parser.OFPFlowMod( datapath=datapath, priority=priority, idle_timeout=idle_timeout, 
								hard_timeout=hard_timeout, match=match, instructions=inst)
	datapath.send_msg(mod)

    ''' Insert a group entry whose type is all '''
    def add_all_group_entry(self, datapath, group_id, buckets):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	mod = parser.OFPGroupMod(datapath, ofproto.OFPGC_ADD, 
						ofproto.OFPGT_ALL, group_id, buckets)
	datapath.send_msg(mod)

    ''' Delete a group entry by referring its id '''
    def del_all_group_entry_by_id(self, datapath, group_id):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	mod = parser.OFPGroupMod(datapath, ofproto.OFPGC_DELETE, 
						ofproto.OFPGT_ALL, group_id)
	datapath.send_msg(mod)

    ''' Modify a group entry by referring its id '''
    def mod_all_group_entry_by_id(self, datapath, group_id, buckets):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	mod = parser.OFPGroupMod(datapath, ofproto.OFPGC_MODIFY, 
						ofproto.OFPGT_ALL, group_id, buckets)
	datapath.send_msg(mod)

    ''' Insert a group entry whose type is select '''
    def add_sel_group_entry(self, datapath, group_id, buckets):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	mod = parser.OFPGroupMod(datapath, ofproto.OFPGC_ADD, 
						ofproto.OFPGT_SELECT, group_id, buckets)
	datapath.send_msg(mod)

    ''' Insert a group entry whose type is indirection '''
    def add_idr_group_entry(self, datapath, group_id, buckets):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	mod = parser.OFPGroupMod(datapath, ofproto.OFPGC_ADD, 
						ofproto.OFPGT_INDIRECT, group_id, buckets)
	datapath.send_msg(mod)

    ''' Insert a group entry whose type is fast failover '''
    def add_ff_group_entry(self, datapath, group_id, buckets):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	mod = parser.OFPGroupMod(datapath, ofproto.OFPGC_ADD, 
						ofproto.OFPGT_FF, group_id, buckets)

	datapath.send_msg(mod)

    ''' Insert a group entry, this is a base method '''
    def add_group_entry(self, datapath, group_type, group_id, weight, watch_port, watch_group, actions):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	buckets = [ofp_parser.OFPBucket(weight, watch_port, watch_group,
								actions)]

	mod = parser.OFPGroupMod(datapath, ofproto.OFPGC_ADD,
							 group_type, group_id, buckets)

	datapath.send_msg(mod)

    ''' Insert a meter entry '''
    def add_meter_entry(self, datapath, meter_id, bands):
	ofproto = datapath.ofproto
	parser = datapath.ofproto_parser

	mod = parser.OFPMeterMod(datapath, ofproto.OFPMC_ADD, ofproto.OFPMF_KBPS, meter_id, bands)

	datapath.send_msg(mod)

    ''' Convert ipv4 address to interger type '''
    def ipv4_to_int(self, string):
	ip = string.split('.')
	assert len(ip) == 4
	i = 0
	for b in ip:
		b = int(b)
		i = (i << 8) | b
	return i

    def _addToGroupTable(self, dpid_str, ports, host, groupId):

	dp = self.dp_dict.get(dpid_str)
	parser = dp.ofproto_parser
	grp_buckets = []
	for port in ports:
		grp_act = [parser.OFPActionOutput(port, 2000)]
		tmp_bucket = parser.OFPBucket(actions = grp_act)
		grp_buckets.append(tmp_bucket)

	# need to find a way to generate a unique group id
	group_id = 1

	# need to find a way to increment the priority number
	priority = HIGH_PRI + 1
		
	self.add_all_group_entry( dp, groupId, grp_buckets )

	# host is the destination host ip address
	meta_info = [host, group_id]
	self.gen_flow_entry_group(dp, parser, priority, meta_info)

