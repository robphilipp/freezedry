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
 * Tree structure representing the multidimensional list.
 *
 * @author Robert Philipp
 *         1/29/14, 11:32 AM
 */
public class Group
{
	private final String name;
	private Map< String, Set< String > > values;
	private Group parent;
	private List< Group > children;

	/**
	 * Constructs a group with the specified group name (i.e. name[*][*])
	 * @param name The group name
	 */
	public Group( final String name )
	{
		this.name = name;
	}

	/**
	 * @return the group name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the parent of this group, null if this is the root group
	 */
	public Group getParent()
	{
		return parent;
	}

	/**
	 * Adds a child group to this group
	 * @param group The child group
	 * @return this group to allow chaining
	 */
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

	/**
	 * @return a list of this group's children
	 */
	public List< Group > getChildren()
	{
		return Collections.unmodifiableList( children );
	}

	/**
	 * Removes the child from the group
	 * @param child the child group to remove from this node
	 * @return {@code true} if the child was successfully removed; {@code false} otherwise
	 */
	public boolean removeChild( final Group child )
	{
		return children != null && children.remove( child );
	}

	/**
	 * Adds a value to the list of values held by this group. This is meant for leaf groups that hold the values
	 * of the lowest dimension of the list.
	 * @param key The field name of the list (i.e. name[0][1][3])
	 * @param value The value of the element (i.e. the value of name[0][1][3])
	 * @return the number of elements with the same value as the specified one
	 */
	public int addValue( final String key, final String value )
	{
		if( values == null )
		{
			values = new HashMap<>();
		}

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

	/**
	 * @return A map containing the values and their set of associated field names
	 */
	public Map< String, Set< String > > getValues()
	{
		if( values == null )
		{
			values = new HashMap<>();
		}
		return Collections.unmodifiableMap( values );
	}

	/**
	 * Removes a value with the specified key
	 * @param key The field name of the list (i.e. name[0][1][3])
	 * @param value The value of the element (i.e. the value of name[0][1][3])
	 * @return the number of elements remaining with the same value as the specified one
	 */
	public int removeValue( final String key, final String value )
	{
		int remaining = -1;
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

	/**
	 * Determines if the values of this group are equivalent to the values of the specified group.
	 * @param group The group whose values to compare to this one
	 * @return {@code true} if the values of the specified group are equivalent to this one; {@code false} otherwise
	 */
	public boolean equivalentValues( final Group group )
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
		}
	}

	/**
	 * @return {@code true} if this group has children; {@code false} otherwise
	 */
	public boolean hasChildren()
	{
		return children != null && !children.isEmpty();
	}

	/**
	 * @return {@code true} if this group is a leaf (i.e. has no children); {@code false} otherwise
	 */
	public boolean isLeaf()
	{
		return !hasChildren();
	}

	/**
	 * Finds the group that has the specfied group name and returns it. If no group can be found with the specified
	 * name, then returns {@code null}.
	 * @param name The name of the group to find
	 * @return the group that has the specfied group name, or, if no group can be found with the specified
	 * name, then returns {@code null}.
	 */
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
}
