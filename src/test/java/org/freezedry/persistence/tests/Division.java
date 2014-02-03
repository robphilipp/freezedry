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

import java.util.*;

import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.utils.Constants;


public class Division {

	@Persist(instantiateAs = ArrayList.class)
	private List<Person> people;

	private Map<String, Set<Integer>> months;

	private String[] carNames;

	private int[][] arrayMatrix;

	private List<List<Integer>> collectionMatrix;

	private Map<String, Person> personMap;

	private List<Map<String, String>> listOfMaps;

	private Set< String > crazySet;

	public void addToCrazySet( final String craziness )
	{
		if( crazySet == null )
		{
			crazySet = new LinkedHashSet<>();
		}
		crazySet.add( craziness );
	}

	public void addPerson( final Person person )
	{
		if( people == null )
		{
			people = new ArrayList<>();
		}
		people.add( person );
	}

	public boolean removePerson( final Person person )
	{
		return people.remove( person );
	}

	public boolean removePerson( final String familyName, final String givenName )
	{
		return removePerson( getPerson( familyName, givenName ) );
	}

	public Person getPerson( final String familyName, final String givenName )
	{
		for( Person person : people )
		{
			if( person.getFamilyName().equals( familyName ) && person.getGivenName().equals( givenName ) )
			{
				return person;
			}
		}
		return null;
	}

	public void addMonth( final String name, final Set<Integer> days )
	{
		if( months == null )
		{
			months = new HashMap<>();
		}
		months.put( name, days );
	}

	public void setCarNames( final String[] names )
	{
		carNames = names;
	}

	public void setArrayMatrix( final int[][] matrix )
	{
		this.arrayMatrix = matrix;
	}

	public void setCollectionMatrix( final List<List<Integer>> matrix )
	{
		this.collectionMatrix = matrix;
	}

	public void setPersonMap( final Map<String, Person> map )
	{
		this.personMap = map;
	}

	public void setListOfMaps( final List<Map<String, String>> listOfMaps )
	{
		this.listOfMaps = listOfMaps;
	}

	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		if( people != null )
		{
			buffer.append( "people: " + Constants.NEW_LINE );
			for( Person person : people )
			{
				buffer.append( person.toString() + Constants.NEW_LINE );
			}
		}
		if( months != null )
		{
			buffer.append( "months: " + Constants.NEW_LINE );
			for( Map.Entry<String, Set<Integer>> entry : months.entrySet() )
			{
				buffer.append( "  " + entry.toString() + Constants.NEW_LINE );
			}
		}
		if( carNames != null )
		{
			buffer.append( "carNames: " + Constants.NEW_LINE );
			for( String name : carNames )
			{
				buffer.append( "  " + name + Constants.NEW_LINE );
			}
		}

		if( collectionMatrix != null )
		{
			buffer.append( "collectionMatrix: " + Constants.NEW_LINE );
			for( List<Integer> row : collectionMatrix )
			{
				for( int column : row )
				{
					buffer.append( column + "\t" );
				}
				buffer.append( Constants.NEW_LINE );
			}
		}

		if( arrayMatrix != null )
		{
			buffer.append( "arrayMatrix: " + Constants.NEW_LINE );
			for( int[] row : arrayMatrix )
			{
				for( int column : row )
				{
					buffer.append( column + "\t" );
				}
				buffer.append( Constants.NEW_LINE );
			}
		}

		if( personMap != null )
		{
			buffer.append( "personMap: " + Constants.NEW_LINE );
			for( Map.Entry<String, Person> entry : personMap.entrySet() )
			{
				buffer.append( "  " + entry.getKey() + ": " + entry.getValue() + Constants.NEW_LINE );
			}
		}

		if( listOfMaps != null )
		{
			buffer.append( "listOfMaps: " + Constants.NEW_LINE );
			for( Map<String, String> map : listOfMaps )
			{
				buffer.append( "  Map " + listOfMaps.indexOf( map ) + ": " + map.toString() + Constants.NEW_LINE );
			}
		}
		return buffer.toString();
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) return true;
		if( o == null || getClass() != o.getClass() ) return false;

		Division division = (Division) o;

		if( !Arrays.equals( carNames, division.carNames ) ) return false;
		if( collectionMatrix != null ? !collectionMatrix.equals( division.collectionMatrix ) : division.collectionMatrix != null )
			return false;
		if( listOfMaps != null ? !listOfMaps.equals( division.listOfMaps ) : division.listOfMaps != null ) return false;
		if( months != null ? !months.equals( division.months ) : division.months != null ) return false;
		if( people != null ? !people.equals( division.people ) : division.people != null ) return false;
		if( personMap != null ? !personMap.equals( division.personMap ) : division.personMap != null ) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = people != null ? people.hashCode() : 0;
		result = 31 * result + (months != null ? months.hashCode() : 0);
		result = 31 * result + (carNames != null ? Arrays.hashCode( carNames ) : 0);
		result = 31 * result + (collectionMatrix != null ? collectionMatrix.hashCode() : 0);
		result = 31 * result + (personMap != null ? personMap.hashCode() : 0);
		result = 31 * result + (listOfMaps != null ? listOfMaps.hashCode() : 0);
		return result;
	}
}
