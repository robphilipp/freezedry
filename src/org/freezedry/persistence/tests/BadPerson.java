package org.freezedry.persistence.tests;

import java.util.ArrayList;
import java.util.List;

public class BadPerson extends Person {
	
	private List< String > evilDoings = new ArrayList<>();

	public BadPerson( String familyName, String givenName, int age )
	{
		super( familyName, givenName, age );
	}

	public void addEvilDoing( final String evilDoing )
	{
		evilDoings.add( evilDoing );
	}
	
	public String getEvilDoing( final int index )
	{ 
		return evilDoings.get( index );
	}
	
	public int getEvilMagnitude()
	{
		return evilDoings.size();
	}
	
	public List< String > getEvilDoings()
	{
		return new ArrayList<>( evilDoings );
	}
}
