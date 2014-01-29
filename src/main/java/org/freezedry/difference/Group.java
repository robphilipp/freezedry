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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * todo
 *
 * @author Robert Philipp
 *         1/29/14, 11:32 AM
 */
public class Group
{
	private final String groupName;
	private Group parent;
	private List< Group > children;
	private List< String > values;

	public Group( final String groupName )
	{
		this.groupName = groupName;
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void addChild( final int index, final Group group )
	{
		if( children == null )
		{
			children = new ArrayList<>();
		}
		group.parent = this;
		children.add( index, group );
	}

	public List< Group > getChildren()
	{
		return Collections.unmodifiableList( children );
	}

	public Group removeChild( final int index )
	{
		if( children != null )
		{
			return children.remove( index );
		}
		return null;
	}

	public void addValue( final int index, final String value )
	{
		if( values == null )
		{
			values = new ArrayList<>();
		}
		values.add( index, value );
	}

	public String removeValue( final int index )
	{
		if( values != null )
		{
			return values.remove( index );
		}
		return null;
	}

	public boolean hasList()
	{
		return values != null && !values.isEmpty();
	}

	public boolean hasChildren()
	{
		return children != null && !children.isEmpty();
	}
}
