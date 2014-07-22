

from mininet.topo import Topo

class MyTopo( Topo ):
    def def_nopts( self, pod, node, name):
        
	key = "%i_%i_%i" % (pod, node, name)
        d = {'key': key}

        # For hosts only, set the IP
        mac = "00:00:00:%02x:%02x:%02x" % (pod, node, name)
        ip = "10.%i.%i.%i" % (pod, node, name)

	dpid = (pod << 16) + (node << 8) + name
        
	d.update({'ip': ip})
        d.update({'mac': mac})
        d.update({'dpid': "%016x" % dpid})
	return d

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        '''
	nodes is dictonary of hosts and switches
	k is number of level
        '''
        
        nodes = {}
        k=4
        # Add hosts
        for i in range(0,k):
	  for j in range(0,k/2):
	    for l in range(2,k/2+2):
	      hosts_opts = self.def_nopts( i,j,l )
	      hs='h%i_%i_%i' % (i,j,l)
	      nodes['%i_%i_%i' % (i,j,l)] = self.addHost( hs, **hosts_opts )

        #add switches
	for i in range(0,k):
	  for j in range(0,k):
	    switches_opts = self.def_nopts( i, j, 1 )
	    sc = 's%i_%i_%i' % (i,j,1)
	    nodes['%i_%i_%i' % (i,j,1)] = self.addSwitch( sc , **switches_opts )
	
	#add switches of top
        for i in range(1,k/2+1):
	  for j in range(1,k/2+1):
	    switches_opts = self.def_nopts( k, i, j)
	    sc = 's%i_%i_%i' % (k,i,j)
	    nodes['%i_%i_%i' % (k,i,j)] = self.addSwitch( sc , **switches_opts )
        
        
        # Add Hosts links
        for i in range(0,k):
	  for j in range(0,k/2):
	    for l in range(2,k/2+2):
	      self.addLink(nodes['%i_%i_%i' % (i,j,l)], nodes['%i_%i_%i' % (i,j,1)] )
	      
	# Add Switchs links
	for i in range(0,k):
	  for j in range(0,k/2):
	    for l in range(k/2,k):
	      self.addLink(nodes['%i_%i_%i' % (i,j,1)], nodes['%i_%i_%i' % (i,l,1)] )
	    
	# Add Switchs links of top
	for i in range(1,k/2+1):
	  for j in range(1,k/2+1):
	    for l in range(0,k):
	      self.addLink(nodes['%i_%i_%i' % (l,k/2+(i-1),1)], nodes['%i_%i_%i' % (k,i,j)] )
	

topos = { 'mytopo': ( lambda: MyTopo() ) }
