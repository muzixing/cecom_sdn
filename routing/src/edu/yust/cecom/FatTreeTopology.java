package edu.yust.cecom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.yust.cecom.model.Entry;
import edu.yust.cecom.model.EntryList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FatTreeTopology
{
	private int k = 0;
	private Log log = LogFactory.getLog( FatTreeTopology.class );
	private int[] pods;
	private int[] core_sws;
	private int[] agg_sws;
	private int[] edge_sws;
	private int[] hosts;
	private List< String > host_list = new ArrayList< String >();
	private Map< String, EntryList > edge_sw_list = new HashMap< String, EntryList >();
	private Map< String, EntryList > core_sw_list = new HashMap< String, EntryList >();
	private Map< String, EntryList > agg_sw_list = new HashMap< String, EntryList >();
	private Map< String, String > ip_mac_map = new HashMap< String, String >();
	private Map< String, String > ip_name_map = new HashMap< String, String >();
	
	final public static int PREFIX_PRIORITY = 10;
	final public static int SUFFIX_PRIORITY = 1;

	public Map< String, EntryList > getEdgeSWList()
	{
		return edge_sw_list;
	}

	public Map< String, EntryList > getCoreSWList()
	{
		return core_sw_list;
	}

	public Map< String, EntryList > getAggSWList()
	{
		return agg_sw_list;
	}

	public String getMacByIp( String ip )
	{
		return ip_mac_map.get( ip );
	}

	public String getNameByIp( String ip )
	{
		return ip_name_map.get( ip );
	}

	/**
	 * @param k
	 *            degree of FatTree Topology
	 */
	public FatTreeTopology( int k )
	{
		this.k = k;

		pods = new IntRange( 0, k ).toArray();
		core_sws = new IntRange( 1, k / 2 + 1 ).toArray();
		agg_sws = new IntRange( k / 2, k ).toArray();
		edge_sws = new IntRange( 0, k / 2 ).toArray();
		hosts = new IntRange( 2, k / 2 + 2 ).toArray();
	}

	/**
	 * Generate fatTree Topology with respect to the degree of k
	 */
	public void genHost()
	{
		
		// generate host ip address using pod, edge_sw and host idx

		// add host ip address into host_list...

	}

	/**
	 * Generate routing tables for core, aggregation and edge switches
	 */
	public void genTable()
	{
		// generate host...
		genHost();

		// generate and insert flow entries to edge switches
		genEdgeTable();

		// generate and insert flow entries to aggregation switches
		genAggTable();

		// generate and insert flow entries to core switches
		genCoreTable();

		printStatInfo();

		printRouteTable( "Edge", edge_sw_list );
		printRouteTable( "Aggr", agg_sw_list );
		printRouteTable( "Core", core_sw_list );
	}

	/**
	 * Generate routing tables for aggregation switches
	 */
	private void genAggTable()
	{

		// generate a EntryList (similar to flow table), and add flow entries to that object

		// Associate EntryList object with aggregation switch (ip) (hint, add element into agg_sw_list...)

	}

	/**
	 * Generate routing tables for edge switches
	 */
	private void genEdgeTable()
	{
		// generate a EntryList (similar to flow table), and add flow entries to that object

		// Associate EntryList object with edge switch (ip) (hint, add element into agg_sw_list...)
	}

	/**
	 * Generate routing tables for core switches
	 */
	private void genCoreTable()
	{
		// generate a EntryList (similar to flow table), and add flow entries to that object

		// Associate EntryList object with core switch (ip) (hint, add element into agg_sw_list...)
	}

	private void printRouteTable( String type, Map< String, EntryList > list )
	{
		for ( String key : list.keySet() )
		{
			String switchStr = StringUtils.center( type + " Switch:" + key, 37 );
			log.debug( "=======================================" );
			log.debug( "|" + switchStr + "|" );
			log.debug( "---------------------------------------" );

			List< Entry > el = list.get( key ).getEntryList();

			for ( Entry entry : el )
			{
				String maskStr = String.valueOf( entry.getMask() );
				String entryStr = StringUtils.leftPad( entry.getIp() + "/"
						+ maskStr, 24 );
				String portStr = StringUtils.leftPad(
						String.valueOf( entry.getPort() ), 6 );
				String priorityStr = StringUtils.leftPad(
						String.valueOf( entry.getPriority() ), 3 );
				log.debug( "|" + entryStr + "| " + portStr + "| " + priorityStr
						+ "|" );
			}

			log.debug( "=======================================" );
		}
	}

	private void printStatInfo()
	{
		int switch_num = core_sw_list.size() + agg_sw_list.size()
				+ edge_sw_list.size();

		String statInfo = StringUtils.center( "Stat. Infomation", 37 );

		log.debug( "========================================" );
		log.debug( "| " + statInfo + "|" );
		log.debug( "----------------------------------------" );

		String coreSwNumStr = StringUtils.rightPad( "# of core switches: "
				+ core_sw_list.size(), 37 );
		String aggrSwNumStr = StringUtils.rightPad(
				"# of aggregation switches: " + agg_sw_list.size(), 37 );
		String edgeSwNumStr = StringUtils.rightPad( "# of edge switches: "
				+ edge_sw_list.size(), 37 );
		String ttlSwNumStr = StringUtils.rightPad( "# of total switches: "
				+ switch_num, 37 );
		String hostNumStr = StringUtils.rightPad(
				"# of hosts: " + host_list.size(), 37 );

		log.debug( "| " + coreSwNumStr + "|" );
		log.debug( "| " + aggrSwNumStr + "|" );
		log.debug( "| " + edgeSwNumStr + "|" );
		log.debug( "| " + ttlSwNumStr + "|" );
		log.debug( "| " + hostNumStr + "|" );

		log.debug( "----------------------------------------" );
	}

	private String genMaskIpStr( int lastbit )
	{
		return String.format( "0.0.0.%d", lastbit );
	}

	private String genIpStr( int pod, int sw, int host )
	{
		return String.format( "10.%d.%d.%d", pod, sw, host );
	}

	private String genMacStr( int pod, int sw, int host )
	{
		return String.format( "00:00:00:%02d:%02d:%02d", pod, sw, host );
	}

	private String genNameStr( int pod, int sw, int host )
	{
		return String.format( "%d_%d_%d", pod, sw, host );
	}
}
