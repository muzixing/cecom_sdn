package edu.yust.cecom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.yust.cecom.model.Entry;
import edu.yust.cecom.model.EntryList;
import edu.yust.cecom.model.Flow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OvsRouteTableMod
{
	public final static int FULL_BIT_MASK = 32;
	public final static int PREF_BIT_MASK = 24;
	public final static int SUFF_BIT_MASK = 8;
	
	private Log log = LogFactory.getLog( OvsRouteTableMod.class );

	public static void main( String[] args ) throws Exception
	{
		if( args.length == 0 ) showErrorMsg();
		
		OvsRouteTableMod orti = new OvsRouteTableMod();
		// in the case of add flows
		if( args[0].equals( "add" ) )
		{
			if( !( args.length >= 2 && args.length <= 3 ) ) showErrorMsg();

			String k = args[1]; // degree of k

			List< Flow > init_flows = orti.genFatTreeFlows( Integer.valueOf( k ) );

			List< Flow > all_flows = new ArrayList< Flow >();
			all_flows.addAll( init_flows );

			if ( args.length == 3 )
			{
				String ftFileName = args[2]; // FlowTable file path
				List< Flow > custom_flows = orti.genCustomFlows(
						Integer.valueOf( k ), ftFileName );
				all_flows.addAll( custom_flows );
			}

			// add all prefix flows into flow tables
			for ( Flow flow : all_flows )
			{
				orti.execute( genOvsAddCmd( flow ) );
			}
		}
		// in the case of remove all flows
		else if( args[0].equals( "remove" ) )
		{
			if( !( args.length >= 1 && args.length <= 2 ) ) showErrorMsg();
			String k = args[1]; // degree of k
			
			for( String switchName : orti.getFatTreeSwitches( Integer.valueOf( k ) ) )
			{
				orti.execute( genOvsDelCmd( switchName ) );
			}
		}
		else showErrorMsg();
	}
	
	/**
	 * Show program usages...
	 */
	public static void showErrorMsg()
	{
		System.err.println( "Error: Wrong number of arguments is specified!" );
		System.err.println( "Usage: modFlowTablesByOvs.sh command [k] [flow_table_file]" );
		System.err.println( "Command can be either add or remove..." );
		System.exit( 1 );
	}

	public List< Flow > genFlowsFromFtFile(
											FatTreeTopology ftt,
											String etherType,
											String filePath ) throws Exception
	{
		List< Flow > custom_flows = new ArrayList< Flow >();
		BufferedReader in = new BufferedReader( new FileReader( filePath ) );
		String flowStr = new String();

		Map< String, String > switch_map = new HashMap< String, String >();

		int idx = 0;
		int pri_idx = 0;
		
		// hard fix the priority as 9 which is lower than 
		// regular FatTree flow priority
		int priority = 9;		
		while ( ( flowStr = in.readLine() ) != null )
		{
			if ( idx != 0 ) // try to skip the first line
			{
				// the init prefix entry may have highest priority MAX - 1
				// the custom prefix entry may have
				// second highest priority MAX - 2
				// ... MAX - n (3 ~ 9)
				// the suffix entry may have the lowest priority MAX - 10
				// int priority = 32768 - 2;
				
				// flowString format
				// Switch IP, Src IP, Dst IP, output port
				String[] flowArr = StringUtils.split( flowStr, "," );

				Flow flow = new Flow();
				// if the etherType equal ARP, then only add one time
				if( etherType.equals( "0x0806" ) )
				{
					if( !switch_map.containsKey( flowArr[0] ) )
					{
						flow.setSwitch( ftt.getNameByIp( flowArr[0] ) );
						flow.setName( "flow-mod-" + ftt.getNameByIp( flowArr[0] ) );
						flow.setPriority( priority );
						flow.setEther_type( etherType );
						flow.setActive( true );
						flow.setActions( "flood" );
						
						custom_flows.add( flow );
					}
				}
				else
				{
					if ( ! switch_map.containsKey( flowArr[0] ) ) pri_idx = priority;
					else pri_idx ++ ;
					
					flow.setSwitch( ftt.getNameByIp( flowArr[0] ) );
					flow.setName( "flow-mod-" + ftt.getNameByIp( flowArr[0] )
															+ "-C-" + pri_idx );
					flow.setPriority( priority );
					
					int srcBitmask = FULL_BIT_MASK;
					int dstBitmask = FULL_BIT_MASK;
					
					// if the last bit of IP address is 1, then bitmask as 24
					if( getLastIpBit( flowArr[1] ) == 1 ) srcBitmask = PREF_BIT_MASK;
					if( getLastIpBit( flowArr[2] ) == 1 ) dstBitmask = PREF_BIT_MASK;
					
					// if the source IP equal to switch IP, then skip adding it 
					if( !StringUtils.equals( flowArr[0], flowArr[1] ) )
						flow.setSrc_ip( flowArr[1] + "/" + srcBitmask );
					
					flow.setDst_ip( flowArr[2] + "/" + dstBitmask );
					flow.setEther_type( etherType );
					flow.setActive( true );
					flow.setActions( "output=" + flowArr[3] );
					
					custom_flows.add( flow );
				}

				switch_map.put( flowArr[0], ftt.getMacByIp( flowArr[0] ) );
			}
			idx ++ ;
		}
		in.close();

		return custom_flows;
	}

	public static String genOvsAddCmd( Flow flow )
	{
		// int priority = 32768 - flow.getPriority();
		int priority = flow.getPriority();
		
		if ( flow != null )
		{
            StringBuilder sb = new StringBuilder();

            String portStr = StringUtils.split( flow.getActions(), "=" )[1];

            sb.append( "ovs-ofctl " );
            sb.append( "add-flow " );
            sb.append( "s"+flow.getSwitch() + " " );

            sb.append( "cookie=" + "0x" + StringUtils.leftPad( portStr, 16, '0' ) + "," );
            sb.append( "priority=" + priority + "," );
            sb.append( "dl_type=" + flow.getEther_type() + "," );

            if( flow.getSrc_ip() != null )
                                                    sb.append( "nw_src=" + flow.getSrc_ip() + "," );
            if( flow.getDst_ip() != null )
                                                    sb.append( "nw_dst=" + flow.getDst_ip() + "," );


            sb.append( "actions=" + flow.getActions() );
            return sb.toString();
		}
		else return null;
	}
	
	public static String genOvsDelCmd( String switchId )
	{
		if( switchId != null )
		{
			StringBuilder sb = new StringBuilder();
			sb.append( "ovs-ofctl " );
			sb.append( "del-flows " );
			sb.append( switchId );
			
			return sb.toString();
		}
		else return null;
	}

	public void execute( String cmd ) throws Exception
	{
		log.debug( cmd );
		Runtime.getRuntime().exec( new String[] { "bash", "-c", cmd } );
	}
	
	public List< Flow > genCustomFlows( int k, String filePath ) throws Exception
	{
		FatTreeTopology ftt = new FatTreeTopology( k );
		ftt.genTable();
		
		// List< Flow > arp_flows  = genFlowsFromFtFile( ftt, "0x0806", filePath );
		List< Flow > ipv4_flows = genFlowsFromFtFile( ftt, "0x0800", filePath );
		
		List< Flow > all_flows = new ArrayList< Flow >();
		// all_flows.addAll( arp_flows );
		all_flows.addAll( ipv4_flows );

		return all_flows;
	}

	/**
	 * Obtain the collection of switch names from FatTree Topology with k
	 * 
	 * @param k degree of FatTree Topology
	 * @return a collection of switch name (e.g., 1_1_1)
	 */
	public List< String > getFatTreeSwitches( int k )
	{
		FatTreeTopology ftt = new FatTreeTopology( k );
		ftt.genTable();
		
		Map< String, EntryList > aggr_sw_list = ftt.getAggSWList();
		Map< String, EntryList > edge_sw_list = ftt.getEdgeSWList();
		Map< String, EntryList > core_sw_list = ftt.getCoreSWList();

		Map< String, EntryList > ttl_sw_list = new HashMap< String, EntryList >();
		ttl_sw_list.putAll( edge_sw_list );
		ttl_sw_list.putAll( aggr_sw_list );
		ttl_sw_list.putAll( core_sw_list );
		
		List< String > switchNameList = new ArrayList< String >();
		
		for( String switchIp : ttl_sw_list.keySet() )
		{
			switchNameList.add( ftt.getNameByIp( switchIp ) );
		}
		
		return switchNameList;		
	}
	
	/**
	 * Generate the initial prefix flow entries for FatTree Topology with k
	 * 
	 * @param k degree of FatTree Topology
	 * @return a collection flow entries for flow table initialization
	 */
	public List< Flow > genFatTreeFlows( int k )
	{
		FatTreeTopology ftt = new FatTreeTopology( k );
		// ftt.treeGen();
		ftt.genTable();

		Map< String, EntryList > aggr_sw_list = ftt.getAggSWList();
		Map< String, EntryList > edge_sw_list = ftt.getEdgeSWList();
		Map< String, EntryList > core_sw_list = ftt.getCoreSWList();

		Map< String, EntryList > ttl_sw_list = new HashMap< String, EntryList >();
		ttl_sw_list.putAll( edge_sw_list );
		ttl_sw_list.putAll( aggr_sw_list );
		ttl_sw_list.putAll( core_sw_list );

		// List< Flow > arp_flows = genFlowByEtherType( ftt, "0x0806", ttl_sw_list );
		List< Flow > ipv4_flows = genFlowByEtherType(
				ftt, "0x0800", ttl_sw_list );

		List< Flow > all_flows = new ArrayList< Flow >();
		// all_flows.addAll( arp_flows );
		all_flows.addAll( ipv4_flows );

		return all_flows;
	}

	/**
	 * Generate the a specified collection of flow entries
	 * 
	 * @param ftt FatTreeTopology instance
	 * @param etherType ethernet type (e.g., ip: 0x0800, arp: 0x0806...)
	 * @param sw_list switch entry list which contains raw flow information
	 * @return
	 */
	public List< Flow > genFlowByEtherType(
											FatTreeTopology ftt,
											String etherType,
											Map< String, EntryList > sw_list )
	{
		List< Flow > all_flows = new ArrayList< Flow >();

		Map< String, String > switch_map = new HashMap< String, String >();
		int priority = 0;
		int pri_idx = 0;

		for ( String key : sw_list.keySet() )
		{
			for ( Entry entry : sw_list.get( key ).getEntryList() )
			{
				// priority = 32768 - entry.getPriority();
				priority = entry.getPriority();
				
				int port = entry.getPort() + 1;
				
				Flow flow = new Flow();
				
				if( etherType.equals( "0x0806" ) )
				{
					if( !switch_map.containsKey( key ) )
					{
						flow.setSwitch( ftt.getNameByIp( key ) );
						flow.setName( "flow-mod-" + ftt.getNameByIp( key ) );
						flow.setPriority( priority );
						flow.setEther_type( etherType );
						flow.setActive( true );
						flow.setActions( "flood" );
						
						all_flows.add( flow );
					}
				}
				else
				{
					if ( ! switch_map.containsKey( key ) ) pri_idx = priority;
					else pri_idx ++ ;
					
					flow.setSwitch( ftt.getNameByIp( key ) );
					flow.setName( "flow-mod-" + ftt.getNameByIp( key )
															+ "-C-" + pri_idx );
					flow.setPriority( priority );
					flow.setDst_ip( entry.getIp() + "/" + entry.getSubnetmask() );
					flow.setEther_type( etherType );
					flow.setActive( true );
					flow.setActions( "output=" + port );
					
					all_flows.add( flow );
				}

				switch_map.put( key, ftt.getMacByIp( key ) );
			}
		}

		return all_flows;
	}
	
	private int getLastIpBit( String ip )
	{
		String[] ipSplit = StringUtils.split( ip, "." );
		
		String last = StringUtils.trim( ipSplit[ ipSplit.length-1 ] );
		
		if( StringUtils.isNumeric( last ) ) return Integer.valueOf( last );
		else 
		{
			System.err.println( "Illigal format of IP address..." );
			System.exit( 1 );
			return 0;
		}
	}
}
