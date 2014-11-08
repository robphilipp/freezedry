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
package org.freezedry.persistence.builders;

import java.util.Map;

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.ReflectionUtils;


/**
 * Generates and {@link InfoNode} for primitives (int, double, float, long, etc) and their
 * wrappers, and for {@link String}s.
 *  
 * @author Robert Philipp
 * 
 * @see AbstractNodeBuilder
 * @see NodeBuilder
 */
public abstract class AbstractLeafNodeBuilder extends AbstractNodeBuilder {
	
	/**
	 * Constructs the {@link NodeBuilder} for going between primitives, their wrappers, {@link String}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public AbstractLeafNodeBuilder( final PersistenceEngine engine )
	{
		super( engine, null );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public AbstractLeafNodeBuilder()
	{
		super( (Map< Class< ? >, Class< ? > >)null );
	}
	
	/**
	 * Copy constructor
	 * @param generator The {@link NodeBuilder} to copy
	 */
	public AbstractLeafNodeBuilder( final AbstractLeafNodeBuilder generator )
	{
		super( generator );
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. The specified containing {@link Class}
	 * is the {@link Class} in which the specified field name lives. And the object is the value of
	 * the field name.
	 * @param containingClass The {@link Class} that contains the specified field name
	 * @param object The value of the field with the specified field name
	 * @param fieldName The name of the field for which the object is the value
	 * @return The constructed {@link InfoNode} based on the specified information
	 */
	@Override
	public InfoNode createInfoNode( final Class< ? > containingClass, final Object object, final String fieldName )
	{
		// grab the class for the object to persist
		final Class< ? > clazz = object.getClass();
		
		// when the containing class is null, then class is the root node of the semantic model, and therefore
		// there won't be a field name to with a annotation containing the persist name.
		String persistName = null;
		if( containingClass != null )
		{
			// grab the persistence name if the annotation @Persist( persistName = "xxxx" ) is specified,
			// and if the leaf is part of another class (such as a collection) it will return the field name
			persistName = ReflectionUtils.getPersistenceName( containingClass, fieldName );
		}
		if( persistName == null || persistName.isEmpty() )
		{
			persistName = fieldName;
		}

		// create a new leaf node
		return InfoNode.createLeafNode( fieldName, object, persistName, clazz );
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. This method is used for objects that have
	 * an overriding node builder and are not contained within a class. For example, suppose you would like
	 * to persist an {@link java.util.ArrayList} for serialization and would like to maintain the type information.
	 * @param object The value of the field with the specified field name
	 * @return The constructed {@link InfoNode} based on the specified information
	 */
	@Override
	public InfoNode createInfoNode( final Object object, final String persistName )
	{
		// create a new leaf node
		return  InfoNode.createLeafNode( persistName, object, persistName, object.getClass() );
	}

	/**
	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}.
	 * This method is used for objects that have an overriding node builder and are not contained within a
	 * class. For example, suppose you would like to persist an {@link java.util.ArrayList} for serialization and would
	 * like to maintain the type information.
	 * @param clazz The {@link Class} of the object to create
	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
	 * @throws ReflectiveOperationException
	 */
	@Override
	public Object createObject( Class< ? > clazz, InfoNode node ) throws ReflectiveOperationException
	{
		return createObject( null, clazz, node );
	}
	
}
