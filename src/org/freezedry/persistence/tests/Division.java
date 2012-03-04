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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.utils.Constants;


public class Division {

	@Persist( instantiateAs = ArrayList.class )
	private List< Person > people;
	
	private Map< String, Set< Integer > > months;
	
	private String[] carNames;
	
	public Division()
	{
	}
	
	public void addPerson( final Person person )
	{
		if( people == null )
		{
			people = new ArrayList<>();
		}
		people.add( person );
	}
	
	public void addMonth( final String name, final Set< Integer > days )
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
	
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		for( Person person : people )
		{
			buffer.append( person.toString() + Constants.NEW_LINE );
		}
		if( months != null )
		{
			for( Map.Entry< String, Set< Integer > > entry : months.entrySet() )
			{
				buffer.append( entry.toString() + Constants.NEW_LINE );
			}
		}
		if( carNames != null )
		{
			for( String name : carNames )
			{
				buffer.append( name + Constants.NEW_LINE );
			}
		}
		return buffer.toString();
	}
}
