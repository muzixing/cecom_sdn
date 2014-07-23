package edu.yust.cecom.model;

public class Entry
{
	private String ip;
	private Integer port;
	private Integer mask;
	private String subnetmask;
	private Integer priority;
	private String type;
	
	public Entry( String ip, Integer port, Integer mask, String subnetmask, String type, Integer priority )
	{
		this.ip = ip;
		this.port = port;
		this.mask = mask;
		this.subnetmask = subnetmask;
		this.type = type;
		this.priority = priority;
	}

	public String getIp()
	{
		return ip;
	}
	
	public void setIp( String ip )
	{
		this.ip = ip;
	}

	public Integer getPort()
	{
		return port;
	}
	
	public void setPort( Integer port )
	{
		this.port = port;
	}

	public Integer getMask()
	{
		return mask;
	}

	public void setMask( Integer mask )
	{
		this.mask = mask;
	}

	public String getSubnetmask()
	{
		return subnetmask;
	}

	public void setSubnetmask( String subnetmask )
	{
		this.subnetmask = subnetmask;
	}

	public Integer getPriority()
	{
		return priority;
	}

	public void setPriority( Integer priority )
	{
		this.priority = priority;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}
}
