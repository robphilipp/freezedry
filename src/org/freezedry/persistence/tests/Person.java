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
	}
	
	public final void setBirthdate( final Calendar date )
	{
		birthDate = date;
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
	}
	
	public final void addGroup( final String name, final Map< String, String > group )
	{
		if( groups == null )
		{
			groups = new LinkedHashMap<>();
		}
		groups.put( name, group );
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
}
