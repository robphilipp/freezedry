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
//	private String value;
//	private Map< String, Integer > values;
	private Map< String, Set< String > > values;
	private Group parent;
	private List< Group > children;

	public Group( final String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public Group getParent()
	{
		return parent;
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
		return children != null && children.remove( child );
	}

//	public Group withValue( final String value )
//	{
//		this.value = value;
//		return this;
//	}
//
//	public String getValue()
//	{
//		return value;
//	}

	public int addValue( final String key, final String value )
	{
		if( values == null )
		{
			values = new HashMap<>();
		}

//		if( values.containsKey( value ) )
//		{
//			return values.put( value, values.get( value )+1 );
//		}
//		else
//		{
//			values.put( value, 1 );
//			return 0;
//		}
		if( values.containsKey( value ) )
		{
			final Set< String > keys = values.get( value );
			keys.add( key );
			return keys.size();
		}
		else
		{
			final Set< String > keys = new HashSet<>();
			keys.add( key );
			values.put( value, keys );
			return keys.size();
		}
	}

	public Map< String, Set< String > > getValues()
	{
		if( values == null )
		{
			values = new HashMap<>();
		}
		return Collections.unmodifiableMap( values );
	}

	public int removeValue( final String key, final String value )
	{
		int remaining = -1;
//		if( values != null && values.containsKey( value ) )
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
//			remaining = count;
//		}
//		return remaining;
		if( values != null && values.containsKey( value ) && values.get( value ).contains( key ) )
		{
			final Set< String > keys = values.get( value );
			keys.remove( key );
			if( keys.isEmpty() )
			{
				values.remove( value );
			}
			remaining = keys.size();
		}
		return remaining;
	}

	public boolean equalValues( final Group group )
	{
		if( values == null || group.values == null )
		{
			return false;
		}
		else
		{
			for( Map.Entry< String, Set< String > > entry : values.entrySet() )
			{
				if( !group.values.containsKey( entry.getKey() ) ||
					entry.getValue().size() != group.values.get( entry.getKey() ).size() )
				{
					return false;
				}
			}
			return true;
//			return values.equals( group.values );
		}
	}

	public boolean hasChildren()
	{
		return children != null && !children.isEmpty();
	}

	public boolean isLeaf()
	{
		return !hasChildren();
	}

	public Group findParentOf( final String name )
	{
		final Group group = findGroup( name );
		return group == null ? null : group.parent;
	}

	public Group findGroup( final String name )
	{
		if( this.name.equals( name ) )
		{
			return this;
		}
		else if( !hasChildren() )
		{
			return null;
		}
		else
		{
			for( Group child : children )
			{
				final Group group = child.findGroup( name );
				if( group != null )
				{
					return group;
				}
			}
		}
		return null;
	}

//	public Map< String, Integer > getChildrenValueMap()
//	{
//		final Map< String, Integer > values = new HashMap<>();
//		for( Group child : children )
//		{
//			final String key = child.getValue();
//			if( values.containsKey( key ) )
//			{
//				values.put( key, values.get( key ) + 1 );
//			}
//			else
//			{
//				values.put( key, 1 );
//			}
//		}
//		return values;
//	}
}
