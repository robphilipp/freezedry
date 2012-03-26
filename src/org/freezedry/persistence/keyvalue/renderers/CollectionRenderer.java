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
import java.util.regex.Pattern;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
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
	
	private static String OPEN = "[";
	private static String CLOSE = "]";
	
	private final StringDecorator indexDecorator;
	private final String regex;
	private final Pattern pattern;

	/**
	 * Constructs a {@link CollectionRenderer} that is used to render {@link InfoNode} representing
	 * {@link Collection}s into key value pairs.
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model. The builder calls
	 * this class' {@link #buildKeyValuePair(InfoNode, String, List, boolean)} as part of the recursive
	 * algorithm to flatten the semantic model
	 * @param indexDecorator The {@link Decorator} for the index.
	 */
	public CollectionRenderer( final KeyValueBuilder builder, final String openIndex, final String closeIndex )
	{
		super( builder );
		
		this.indexDecorator = new StringDecorator( openIndex, closeIndex );
		
		// create the regular expression that determines if a string is renderered by this class
		// people[0] is a collection, but people{"test"}[0] is a map< string, list< integer > >
		// so we want to check that only word characters precede the "["
		final String open = Pattern.quote( openIndex );
		final String close = Pattern.quote( closeIndex );
		this.regex = "\\w+" + open + "[0-9]" + close + "|^" + open + "[0-9]+" + close;
		this.pattern = Pattern.compile( regex );
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
		this( builder, OPEN, CLOSE );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link PersistenceRenderer} to copy
	 */
	public CollectionRenderer( final CollectionRenderer renderer )
	{
		super( renderer );
		
		this.indexDecorator = renderer.indexDecorator.getCopy();
		this.regex = renderer.regex;
		this.pattern = renderer.pattern;
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

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#isRenderer(java.lang.String)
	 */
	@Override
	public boolean isRenderer( final String keyElement )
	{
		return pattern.matcher( keyElement ).find();
//		boolean isFound = false;
//		// does the pattern exist in any part of the key.
//		final Matcher matcher = pattern.matcher( keyElement );
//		if( matcher.find() )
//		{
//			isFound = true;
//			// now check to make sure that it was the primary type. for example,
//			// people[0] is a collection, but people{"test"}[0] is a map< string, list< integer > >
//			// so we want to check that only word characters preceed the "["
//			final Pattern strictPattern = Pattern.compile( "\\w" + regex );
//			final Matcher strictMatcher = strictPattern.matcher( keyElement );
//			if( strictMatcher.find() )
//			{
//				isFound = true;
//			}
//		}
//		return isFound;
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

	/*
	 * Creates a key for a compound node collection. For example, if the persist name for a {@link List} is
	 * people, which is a <code>{@link List}< {@link Person} ></code>, then the key will be {@code people[i].Person}
	 * where the {@code i} is the index of the list.
	 * @param key The current key to which to append the persisted name and decorated index
	 * @param parentNode The parent node, which holds the name of the field (in this example, "{@code people}")
	 * @param node The current node (in this example, "{@code Person}")
	 * @param index The index of the element in the {@link List}
	 * @return The key
	 */
	private String createNodeKey( final String key, final InfoNode parentNode, final InfoNode node, final int index )
	{
		// grab the key-element separator
		final String separator = getPersistenceWriter().getSeparator();

		// if the parent node has a persistence name then add it
		String newKey = key;
		if( parentNode.getPersistName() != null && !parentNode.getPersistName().isEmpty() )
		{
			newKey += separator + parentNode.getPersistName();
		}
		
		// decorate the index. for example prepend a "[" and append a "]"
		newKey += indexDecorator.decorate( index );
		
		// if the current node isn't null, and the parent has a persistence name, then add the 
		// current nodes persistence name. For example, if you have a list of compound objects,
		// such as 
		//    List< Person > people;
		// then this part of the key would look like people[0].Person. etc...
		if( node != null && parentNode.getPersistName() != null && !parentNode.getPersistName().isEmpty() )
		{
			newKey += separator + node.getPersistName();
		}
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
