package edu.yust.cecom.model;

/**
 * @author GUNi
 *
 */
public class TestCase
{
	public static void main( String[] args ) throws Exception
	{
		int OFPFW_ALL = ((1 << 22) - 1);
		int OFPFW_NW_SRC_SHIFT = 8;
	    int OFPFW_NW_SRC_BITS = 6;
	    int OFPFW_NW_SRC_MASK = ((1 << OFPFW_NW_SRC_BITS) - 1) << OFPFW_NW_SRC_SHIFT;
	    int OFPFW_NW_SRC_ALL = 32 << OFPFW_NW_SRC_SHIFT;
		
		System.out.println( OFPFW_ALL );
		System.out.println( OFPFW_NW_SRC_MASK );
		System.out.println( OFPFW_NW_SRC_ALL );
	}
}
