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

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tree.InfoNode;

/**
 * Abstract class that implements the {@link #createInfoNode(Object, String)} for classes
 * that are leaves in the semantic model, and also primitive types or wrappers.
 * 
 * @author Robert Philipp
 */
public abstract class AbstractPrimitiveLeafNodeBuilder extends AbstractLeafNodeBuilder {

	/**
	 * Constructs the {@link NodeBuilder} for going between primitives, their wrappers, {@link String}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public AbstractPrimitiveLeafNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public AbstractPrimitiveLeafNodeBuilder()
	{
		super();
	}
	
	/**
	 * Copy constructor
	 * @param builder
	 */
	public AbstractPrimitiveLeafNodeBuilder( final AbstractPrimitiveLeafNodeBuilder builder )
	{
		super( builder );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.AbstractLeafNodeBuilder#createInfoNode(java.lang.Object, java.lang.String)
	 */
	@Override
	public InfoNode createInfoNode( final Object object, final String persistName )
	{
		// grab the class for the object to persist
		final Class< ? > clazz = object.getClass();
		
		// we must convert the object to the appropriate format
		final InfoNode stringNode = InfoNode.createLeafNode( "value", object, "value", clazz );

		// create the root node and add the string rep of the date
		final InfoNode node = InfoNode.createRootNode( persistName, clazz );
		node.addChild( stringNode );
		
		// return the node
		return node;
	}

}
