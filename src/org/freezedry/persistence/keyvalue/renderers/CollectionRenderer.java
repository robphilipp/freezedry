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
package org.freezedry.persistence.keyvalue.renderers;

import java.util.Collection;
import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;

/**
 * Renders the subtree of the semantic model that represents a {@link Collection}. {@link Collection}s are rendered
 * as the collection's persistence name followed by the decorated index. For example, a {@code List< String >}
 * would be persisted in the following format when using the default decorator and settings:
 * <code><pre>
 * Division.carNames[0] = "civic"
 * Division.carNames[1] = "tsx"
 * Division.carNames[2] = "accord"
 * </pre></code>
 * 
 * @author rob
 */
public class CollectionRenderer extends AbstractPersistenceRenderer {
	
	private static StringDecorator INDEX_DECORATOR = new StringDecorator( "[", "]" );
	
	private StringDecorator indexDecorator;

	/**
	 * Constructs a {@link CollectionRenderer} that is used to render {@link InfoNode} representing
	 * {@link Collection}s into key value pairs.
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model. The builder calls
	 * this class' {@link #buildKeyValuePair(InfoNode, String, List, boolean)} as part of the recursive
	 * algorithm to flatten the semantic model
	 * @param indexDecorator The {@link Decorator} for the index.
	 */
	public CollectionRenderer( final KeyValueBuilder builder, final StringDecorator indexDecorator )
	{
		super( builder );
		
		this.indexDecorator = indexDecorator.getCopy();
	}
	
	/**
	 * Constructs a {@link CollectionRenderer} that is used to render {@link InfoNode} representing
	 * {@link Collection}s into key value pairs. Uses the default the index decorator which prepends
	 * a "{@code [}" onto the index and appends a "{@code ]}" to the end of the index. For example,
	 * if the {@code index=1} then the index would be decorated to look like {@code [1]}.
	 * @param builder
	 */
	public CollectionRenderer( final KeyValueBuilder builder )
	{
		this( builder, INDEX_DECORATOR );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link PersistenceRenderer} to copy
	 */
	public CollectionRenderer( final CollectionRenderer renderer )
	{
		super( renderer );
		
		this.indexDecorator = renderer.indexDecorator.getCopy();
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.renderers.PersistenceRenderer#createKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersistName )
	{
		int index = 0;
		for( InfoNode node : infoNode.getChildren() )
		{
			if( node.isLeafNode() )
			{
				// create the key-value pair and return it
				final String newKey = createLeafNodeKey( key, infoNode, index );
				getPersistenceWriter().createKeyValuePairs( node, newKey, keyValues, true );
			}
			else
			{
				final String newKey = createNodeKey( key, infoNode, node, index );
				getPersistenceWriter().buildKeyValuePairs( node, newKey, keyValues );
			}

			// increment the index count
			++index;
			
			// mark the node as processed so that it doesn't get processed again
			node.setIsProcessed( true );
		}
	}

	/**
	 * Creates a key for a leaf node collection. For example, if the persist name for a {@link List} is
	 * people, which is a <code>{@link List}< {@link String} ></code>, then the key will be {@code people[i]}
	 * where the {@code i} is the index of the list.
	 * @param key The current key to which to append the persisted name and decorated index
	 * @param parentNode The parent node, which holds the name of the field
	 * @param index The index of the element in the {@link List}
	 * @return The key
	 */
	private String createLeafNodeKey( final String key, final InfoNode parentNode, final int index )
	{
		return createNodeKey( key, parentNode, null, index );
	}

	/**
	 * Creates a key for a compound node collection. For example, if the persist name for a {@link List} is
	 * people, which is a <code>{@link List}< {@link Person} ></code>, then the key will be {@code people.Person[i]}
	 * where the {@code i} is the index of the list.
	 * @param key The current key to which to append the persisted name and decorated index
	 * @param parentNode The parent node, which holds the name of the field (in this example, "{@code people}")
	 * @param node The current node (in this example, "{@code Person}")
	 * @param index The index of the element in the {@link List}
	 * @return The key
	 */
	private String createNodeKey( final String key, final InfoNode parentNode, final InfoNode node, final int index )
	{
		String newKey = key;
		if( parentNode.getPersistName() != null && !parentNode.getPersistName().isEmpty() )
		{
			final String separator = getPersistenceWriter().getSeparator(); 
			newKey += separator + parentNode.getPersistName();
			if( node != null )
			{
				newKey += separator + node.getPersistName();
			}
		}
		newKey += indexDecorator.decorate( index );
		return newKey;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public CollectionRenderer getCopy()
	{
		return new CollectionRenderer( this );
	}

}
