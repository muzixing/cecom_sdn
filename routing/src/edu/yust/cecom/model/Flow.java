package edu.yust.cecom.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class Flow
{
	private String Switch;
	private String name;
	private String actions;
	private Integer priority;
	private Boolean active;
	private Integer wildcards;
	private Integer ingress_port;
	private String src_mac;
	private String dst_mac;
	private String vlan_id;
	private String vlan_priority;
	private String ether_type;
	private String tos_bits;
	private String protocol;
	private String src_ip;
	private String dst_ip;
	private String src_port;
	private String dst_port;
	
	public String getSwitch()
	{
		return Switch;
	}
	public void setSwitch( String mySwitch )
	{
		Switch = mySwitch;
	}
	public String getName()
	{
		return name;
	}
	public void setName( String name )
	{
		this.name = name;
	}	
	public String getActions()
	{
		return actions;
	}
	public void setActions( String actions )
	{
		this.actions = actions;
	}
	public Integer getWildcards()
	{
		return wildcards;
	}
	public void setWildcards( Integer wildcards )
	{
		this.wildcards = wildcards;
	}
	public Integer getPriority()
	{
		return priority;
	}
	public void setPriority( Integer priority )
	{
		this.priority = priority;
	}
	public Boolean getActive()
	{
		return active;
	}
	public void setActive( Boolean active )
	{
		this.active = active;
	}
	public Integer getIngress_port()
	{
		return ingress_port;
	}
	public void setIngress_port( Integer ingress_port )
	{
		this.ingress_port = ingress_port;
	}
	public String getSrc_mac()
	{
		return src_mac;
	}
	public void setSrc_mac( String src_mac )
	{
		this.src_mac = src_mac;
	}
	public String getDst_mac()
	{
		return dst_mac;
	}
	public void setDst_mac( String dst_mac )
	{
		this.dst_mac = dst_mac;
	}
	public String getVlan_id()
	{
		return vlan_id;
	}
	public void setVlan_id( String vlan_id )
	{
		this.vlan_id = vlan_id;
	}
	public String getVlan_priority()
	{
		return vlan_priority;
	}
	public void setVlan_priority( String vlan_priority )
	{
		this.vlan_priority = vlan_priority;
	}
	public String getEther_type()
	{
		return ether_type;
	}
	public void setEther_type( String ether_type )
	{
		this.ether_type = ether_type;
	}
	public String getTos_bits()
	{
		return tos_bits;
	}
	public void setTos_bits( String tos_bits )
	{
		this.tos_bits = tos_bits;
	}
	public String getProtocol()
	{
		return protocol;
	}
	public void setProtocol( String protocol )
	{
		this.protocol = protocol;
	}
	public String getSrc_ip()
	{
		return src_ip;
	}
	public void setSrc_ip( String src_ip )
	{
		this.src_ip = src_ip;
	}
	public String getDst_ip()
	{
		return dst_ip;
	}
	public void setDst_ip( String dst_ip )
	{
		this.dst_ip = dst_ip;
	}
	public String getSrc_port()
	{
		return src_port;
	}
	public void setSrc_port( String src_port )
	{
		this.src_port = src_port;
	}
	public String getDst_port()
	{
		return dst_port;
	}
	public void setDst_port( String dst_port )
	{
		this.dst_port = dst_port;
	}
}
