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

import java.util.LinkedHashMap;
import java.util.Map;

import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.annotations.PersistMap;

public class MapMagic {

	@Persist(ignore=true)
	private volatile int hashCode;

	@PersistMap(entryPersistName="client",keyPersistName="endPoint",valuePersistName="weight")
	private Map< String, Double > mapOne;
	
	@Persist(persistenceName="otherMap")
	private Map< String, Integer > mapTwo;
	
	public MapMagic()
	{
		mapOne = createMapOne();
		mapTwo = createMapTwo();
	}
	
	private static Map< String, Double > createMapOne()
	{
		final Map< String, Double > map = new LinkedHashMap<>();
		map.put( "http://192.168.1.1:8182/diffusers", 3.14 );
		map.put( "http://192.168.1.2:8182/diffusers", 2 * 3.14 );
		map.put( "http://192.168.1.3:8182/diffusers", 3 * 3.14 );
		map.put( "http://192.168.1.4:8182/diffusers", 4 * 3.14 );
		map.put( "http://192.168.1.5:8182/diffusers", 5 * 3.14 );
		return map;
	}
	
	private static Map< String, Integer > createMapTwo()
	{
		final Map< String, Integer > map = new LinkedHashMap<>();
		map.put( "http://192.168.1.1:8182/diffusers", 1 );
		map.put( "http://192.168.1.2:8182/diffusers", 2 );
		map.put( "http://192.168.1.3:8182/diffusers", 3 );
		map.put( "http://192.168.1.4:8182/diffusers", 4 );
		map.put( "http://192.168.1.5:8182/diffusers", 5 );
		return map;
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
		if( !( object instanceof MapMagic ) )
		{
			return false;
		}
		
		// cast
		final MapMagic mapMagic = (MapMagic)object;

		// check map one
		if( mapOne.size() != mapMagic.mapOne.size() )
		{
			return false;
		}
		
		for( Map.Entry< String, Double > entry : mapOne.entrySet() )
		{
			final Double weight = mapMagic.mapOne.get( entry.getKey() );
			if( weight == null || !entry.getValue().equals( weight ) )
			{
				return false;
			}
		}
		
		// check map two
		if( mapTwo.size() != mapMagic.mapTwo.size() )
		{
			return false;
		}
		
		for( Map.Entry< String, Integer > entry : mapTwo.entrySet() )
		{
			final Integer weight = mapMagic.mapTwo.get( entry.getKey() );
			if( weight == null || !entry.getValue().equals( weight ) )
			{
				return false;
			}
		}
		return true;
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
			result = 31 * result + mapOne.hashCode();
			result = 31 * result + mapTwo.hashCode();
			hashCode = result;
		}
		return hashCode;
	}

}
