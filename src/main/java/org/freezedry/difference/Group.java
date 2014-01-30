/*
 * Copyright 2014 Robert Philipp
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
package org.freezedry.difference;

import java.util.*;

/**
 * todo
 *
 * @author Robert Philipp
 *         1/29/14, 11:32 AM
 */
public class Group
{
	private final String name;
	private String value;
	private Group parent;
	private List< Group > children;
//	private List< String > values;
//	private Map< String, Integer > values;

	public Group( final String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public Group addChild( final Group group )
	{
		if( children == null )
		{
			children = new ArrayList<>();
		}
		group.parent = this;
		children.add( group );
		return this;
	}

	public List< Group > getChildren()
	{
		return Collections.unmodifiableList( children );
	}

	public boolean removeChild( final Group child )
	{
		if( children != null )
		{
			return children.remove( child );
		}
		return false;
	}

	public Group withValue( final String value )
	{
		this.value = value;
		return this;
	}

	public String getValue()
	{
		return value;
	}

//	public void addValue( final int index, final String value )
//	{
//		if( values == null )
//		{
//			values = new ArrayList<>();
//		}
//		values.add( index, value );
//	}

//	public void addValue( final String value )
//	{
//		if( values == null )
//		{
//			values = new HashMap<>();
//		}
//
//		// increment the value count if it exists, or add it to the map of values
//		if( values.containsKey( value ) )
//		{
//			values.put( value, values.get( value )+1 );
//		}
//		else
//		{
//			values.put( value, 1 );
//		}
//	}

//	public String removeValue( final int index )
//	{
//		if( values != null )
//		{
//			return values.remove( index );
//		}
//		return null;
//	}

//	public void removeValue( final String value )
//	{
//		if( values.containsKey( value ) )
//		{
//			final int count = values.get( value ) - 1;
//			if( count > 0 )
//			{
//				values.put( value, count );
//			}
//			else
//			{
//				values.remove( value );
//			}
//		}
//	}

//	public boolean hasList()
//	{
//		return values != null && !values.isEmpty();
//	}

	public boolean hasChildren()
	{
		return children != null && !children.isEmpty();
	}

//	public boolean areValuesEqual( final Group group )
//	{
//		return values != null && values.equals( group.values );
//	}
}
