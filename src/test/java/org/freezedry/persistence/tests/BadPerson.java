/*
 * Copyright 2012 Robert Philipp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.freezedry.persistence.tests;

import java.util.ArrayList;
import java.util.List;

import org.freezedry.persistence.annotations.Persist;

public class BadPerson extends Person {
	
	private volatile int hashCode;

	private List< String > evilDoings = new ArrayList<>();
	
	@Persist( ignore=true )
	private String fieldToIgnore = "this is a field to ignore and shouldn't be written";

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
