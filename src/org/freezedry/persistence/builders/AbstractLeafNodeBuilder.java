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

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.generators.Generator#generateNode(java.lang.Class, java.lang.Object, java.lang.String)
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
		final InfoNode node = InfoNode.createLeafNode( fieldName, object, persistName, clazz );
		
		// return the node
		return node;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.NodeBuilder#createInfoNode(java.lang.Object)
	 */
	@Override
	public InfoNode createInfoNode( final Object object, final String persistName )
	{
		// grab the class for the object to persist
		final Class< ? > clazz = object.getClass();
		
		// create a new leaf node
		final String name = persistName;//clazz.getName();
		final InfoNode node = InfoNode.createLeafNode( name, object, name, clazz );
		
		// return the node
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.InfoNode)
	 */
	@Override
	public Object createObject( Class< ? > clazz, InfoNode node ) throws ReflectiveOperationException
	{
		return createObject( null, clazz, node );
	}
	
}
