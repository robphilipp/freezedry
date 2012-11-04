package org.freezedry.persistence.tests;

import java.util.ArrayList;
import java.util.List;

public class BadPerson extends Person {
	
	private volatile int hashCode;

	private List< String > evilDoings = new ArrayList<>();

	public BadPerson( String familyName, String givenName, int age )
	{
		super( familyName, givenName, age );
	}

	public void addEvilDoing( final String evilDoing )
	{
		evilDoings.add( evilDoing );
		hashCode = 0;
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
	
	@Override
	public boolean equals( final Object object )
	{
		// same object
		if( object == this )
		{
			return true;
		}
		
		// is it the same type, this also catches if obj is null
		if( !( object instanceof BadPerson ) )
		{
			return false;
		}
		
		// cast
		final BadPerson person = (BadPerson)object;
		
		// are the parent parts equal
		boolean isEqual = super.equals( person );
		
		// have they both done the same evil
		if( evilDoings != null && person.evilDoings != null && evilDoings.size() == person.evilDoings.size() )
		{
			for( int i = 0; i < evilDoings.size(); ++i )
			{
				if( !evilDoings.get( i ).equals( person.evilDoings.get( ( i ) ) ) )
				{
					return false;
				}
			}
		}
		
		return isEqual;
	}

	/* (non-Javadoc)
	 * @see org.sun.java.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = hashCode;
		if( result == 0 )
		{
			result = super.hashCode();
			result = 31 * result + evilDoings.hashCode();
			hashCode = result;
		}
		return hashCode;
	}
}
