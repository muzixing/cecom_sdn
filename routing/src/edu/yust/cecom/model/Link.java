package edu.yust.cecom.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class Link
{
	private String src_switch;
	private Integer src_port;
	private String dst_switch;
	private Integer dst_port;
	private String type;
	private String direction;
	
	public String getSrc_switch()
	{
		return src_switch;
	}
	public void setSrc_switch( String src_switch )
	{
		this.src_switch = src_switch;
	}
	public Integer getSrc_port()
	{
		return src_port;
	}
	public void setSrc_port( Integer src_port )
	{
		this.src_port = src_port;
	}
	public String getDst_switch()
	{
		return dst_switch;
	}
	public void setDst_switch( String dst_switch )
	{
		this.dst_switch = dst_switch;
	}
	public Integer getDst_port()
	{
		return dst_port;
	}
	public void setDst_port( Integer dst_port )
	{
		this.dst_port = dst_port;
	}
	public String getType()
	{
		return type;
	}
	public void setType( String type )
	{
		this.type = type;
	}
	public String getDirection()
	{
		return direction;
	}
	public void setDirection( String direction )
	{
		this.direction = direction;
	}
}
