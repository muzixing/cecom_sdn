package edu.yust.cecom.model;

import java.util.ArrayList;
import java.util.List;

public class EntryList
{
	private List< Entry > entryList = new ArrayList< Entry >();
	
	public void addEntry ( Entry entry )
	{
		entryList.add( entry );
	}
	
	public List< Entry > getEntryList()
	{
		return entryList;
	}
}
