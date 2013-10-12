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
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.annotations.PersistCollection;
import org.freezedry.persistence.annotations.PersistDateAs;
import org.freezedry.persistence.builders.DateNodeBuilder;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.DateUtils;


public class Person {

	@Persist( ignore = true )
	private volatile int hashCode;

//	@Persist( persistenceName = "FirstName" )
	private String givenName;
	
//	@Persist( persistenceName = "LastName" )
//	@Persist( useNodeBuilder = CapStringNodeBuilder.class )
//	@Persist( persistenceName = "LastName", useNodeBuilder = CapStringNodeBuilder.class )
	private String familyName;
	private int age;
	
	@PersistDateAs( "yyyy-MM-dd" )
	private Calendar birthDate;
	
	@PersistCollection( elementPersistName = "Volatility", elementType = Double.class )
	@Persist( persistenceName = "Mood", instantiateAs = ArrayList.class )
	private List< Double > mood;
	
	@Persist( instantiateAs = LinkedHashMap.class )
	private Map< String, String > friends;
	
	@Persist( instantiateAs = LinkedHashMap.class )
	private Map< String, Map< String, String > > groups;
	
	public Person( final String familyName, final String givenName, final int age )
	{
		this.givenName = givenName;
		this.familyName = familyName;
		this.age = age;
	}
	
	/**
	 * @return the givenName
	 */
	public final String getGivenName()
	{
		return givenName;
	}

	/**
	 * @param givenName the givenName to set
	 */
	public final void setGivenName( String givenName )
	{
		this.givenName = givenName;
		hashCode = 0;
	}

	/**
	 * @return the familyName
	 */
	public final String getFamilyName()
	{
		return familyName;
	}

	/**
	 * @param familyName the familyName to set
	 */
	public final void setFamilyName( String familyName )
	{
		this.familyName = familyName;
		hashCode = 0;
	}

	/**
	 * @return the age
	 */
	public final int getAge()
	{
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public final void setAge( int age )
	{
		this.age = age;
		hashCode = 0;
	}
	
	public final void setBirthdate( final Calendar date )
	{
		birthDate = date;
		hashCode = 0;
	}
	
	public final Calendar getBirthdate()
	{
		return birthDate;
	}
	
	public void addMood( final double mood )
	{
		if( this.mood == null )
		{
			this.mood = new ArrayList<>();
		}
		this.mood.add( mood );
		hashCode = 0;
	}
	
	public final List< Double > getMood()
	{
		return mood;
	}
	
	public final void addFriend( final String name, final String type )
	{
		if( friends == null )
		{
			friends = new LinkedHashMap<>();
		}
		friends.put( name, type );
		hashCode = 0;
	}
	
	public final void addGroup( final String name, final Map< String, String > group )
	{
		if( groups == null )
		{
			groups = new LinkedHashMap<>();
		}
		groups.put( name, group );
		hashCode = 0;
	}
	
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "Person: " + Constants.NEW_LINE );
		buffer.append( "  Family Name: " + familyName + Constants.NEW_LINE );
		buffer.append( "  Given Name: " + givenName + Constants.NEW_LINE );
		buffer.append( "  Age: " + age + Constants.NEW_LINE );
		if( birthDate != null )
		{
			buffer.append( "  Birthdate: " + DateUtils.createStringFromDate( birthDate, DateNodeBuilder.ISO_8601_DATE_FORMAT ) );
			buffer.append( Constants.NEW_LINE );
		}
		if( mood != null )
		{
			buffer.append( "  Mood: " + mood + Constants.NEW_LINE );
		}
		if( friends != null )
		{
			buffer.append( "  Friends: " + friends.toString()  + Constants.NEW_LINE );
		}
		if( groups != null )
		{
			buffer.append( "  Groups: " + groups.toString() );
		}
		return buffer.toString();
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
		if( !( object instanceof Person ) )
		{
			return false;
		}
		
		// cast
		final Person person = (Person)object;
		
		boolean isEqual = ( givenName == null ? person.givenName == null : givenName.equals( person.givenName ) );
		isEqual = isEqual && ( familyName == null ? true : familyName.equals( person.familyName ) );
		isEqual = isEqual && age == person.age;
		isEqual = isEqual && ( birthDate == null ? person.birthDate == null : birthDate.equals( person.birthDate ) );
		if( !isEqual )
		{
			return false;
		}
		if( mood != null && person.mood != null && mood.size() == person.mood.size() )
		{
			for( int i = 0; i < mood.size(); ++i )
			{
				if( !mood.get( i ).equals( person.mood.get( i ) ) )
				{
					return false;
				}
			}
		}
		if( friends != null && person.friends != null && friends.size() == person.friends.size() )
		{
			for( String name : friends.keySet() )
			{
				if( !friends.get( name ).equals( person.friends.get( name ) ) )
				{
					return false;
				}
			}
		}
		if( groups != null && person.groups != null && groups.size() == person.groups.size() )
		{
			for( String groupName : groups.keySet() )
			{
				final Map< String, String > group = groups.get( groupName );
				final Map< String, String > otherGroup = person.groups.get( groupName );
				if( group.size() == otherGroup.size() )
				{
					for( String name : group.keySet() )
					{
						if( !group.get( name ).equals( otherGroup.get( name ) ) )
						{
							return false;
						}
					}
				}
				else
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
			result = 17;
			result = 31 * result + givenName.hashCode();
			result = 31 * result + familyName.hashCode();
			result = 31 * result + age;
			result = 31 * result + birthDate.hashCode();
			result = 31 * result + mood.hashCode();
			result = 31 * result + friends.hashCode();
			result = 31 * result + groups.hashCode();
			hashCode = result;
		}
		return hashCode;
	}

}
