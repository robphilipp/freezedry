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

public class DoubleNodeBuilder extends AbstractPrimitiveLeafNodeBuilder {

	/**
	 * Constructs the {@link NodeBuilder} for going between primitives, their wrappers, {@link String}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public DoubleNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public DoubleNodeBuilder()
	{
		super();
	}
	
	/**
	 * Copy constructor
	 * @param builder The {@link DoubleNodeBuilder} to copy
	 */
	public DoubleNodeBuilder( final DoubleNodeBuilder builder )
	{
		super( builder );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.nodes.InfoNode)
	 */
	@Override
	public Double createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node )
	{
		final Object valueString = node.getValue();
		return Double.parseDouble( valueString.toString() );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.AbstractLeafNodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.InfoNode)
	 */
	@Override
	public Double createObject( final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// grab the child node's value
		final Object nodeValue = node.getChild( 0 ).getValue();
		
		// here it is a bit complicated. recall that this method is called for root nodes, and so
		// value seems to jump between Double and String, and because "1.0" could be "1", also Integer
		Double value;
		if( nodeValue instanceof Double )
		{
			value = (Double)nodeValue;
		}
		else if( nodeValue instanceof Integer )
		{
			value = (double) (int) nodeValue;
		}
		else
		{
			value = Double.parseDouble( (String)nodeValue );
		}
		return value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public DoubleNodeBuilder getCopy()
	{
		return new DoubleNodeBuilder( this );
	}
}
