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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
import org.freezedry.persistence.keyvalue.utils.KeyValueUtils;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;

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
	
	private static final Logger LOGGER = Logger.getLogger( CollectionRenderer.class );

	private static String OPEN = "[";
	private static String CLOSE = "]";
	
	private final StringDecorator indexDecorator;
	private final String decorationRegex;
	private final Pattern decorationPattern;
	private final String validationRegex;
	private final Pattern validationPattern;

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
		
		// create and compile the regex pattern for the decoration
		decorationRegex = open + "[0-9]" + close ;
		decorationPattern = Pattern.compile( decorationRegex );
		
		// create and compile the regex pattern for validating the complete key
		validationRegex = "\\w+" + decorationRegex + "|^" + decorationRegex;
		validationPattern = Pattern.compile( validationRegex );
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
		this.decorationRegex = renderer.decorationRegex;
		this.decorationPattern = renderer.decorationPattern;
		this.validationRegex = renderer.validationRegex;
		this.validationPattern = renderer.validationPattern;
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
				getPersistenceBuilder().createKeyValuePairs( node, newKey, keyValues, true );
			}
			else
			{
				final String newKey = createNodeKey( key, infoNode, node, index );
				getPersistenceBuilder().buildKeyValuePairs( node, newKey, keyValues );
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
		final String separator = getPersistenceBuilder().getSeparator();

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
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#buildInfoNode(org.freezedry.persistence.tree.InfoNode, java.util.List)
	 */
	@Override
	public void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues )
	{
		// nothing to do
		if( keyValues == null || keyValues.isEmpty() )
		{
			return;
		}
		
		// grab the group name for the collection, and create the compound node
		// that holds the elements of the collection as child nodes, and add it
		// to the parent node
		final String group = getGroupName( keyValues.get( 0 ).getFirst() );
		final InfoNode collectionNode = InfoNode.createCompoundNode( null, group, null );
		parentNode.addChild( collectionNode );
		
		// construct the patterns to determine if the node should be a compound node,
		// in which case we recurse back to the builder, or a leaf node, in which case
		// we simply create it here and add it to the collection node
		final String compoundRegex = "^" + group + decorationRegex;
		final Pattern compoundPattern = Pattern.compile( compoundRegex );
		
		final String leafRegex = compoundRegex + "$";
		final Pattern leafPattern = Pattern.compile( leafRegex );
		
		// run through the list of key-values creating the child nodes for the collection node
		final List< Pair< String, String > > copiedKeyValues = new ArrayList<>( keyValues );
		for( Pair< String, String > keyValue : keyValues )
		{
			// check to see if any items have been removed from the list. this could happen
			// when there is a compound node that we have combined, and removed all the entries
			// from this list
			if( !copiedKeyValues.contains( keyValue ) )
			{
				continue;
			}

			// grab the key
			final String key = keyValue.getFirst();
			
			// then we must figure out whether this is a compound node or a leaf node
			final Matcher compoundMatcher = compoundPattern.matcher( key );
			final Matcher leafMatcher = leafPattern.matcher( key );
			if( leafMatcher.find() )
			{
				// its a leaf, so now we need to figure out what the value is. we know that
				// it must be a number (integer, double) or a string.
				final String value = keyValue.getSecond();
				final Decorator decorator = getDecorator( value ); 
				final String rawValue = decorator.undecorate( value );
				final String persistName = decorator.representedClass().getSimpleName();
				
				// create the leaf info node and add it to the collection node
				final InfoNode elementNode = InfoNode.createLeafNode( null, rawValue, persistName, null );
				collectionNode.addChild( elementNode );
			}
			else if( compoundMatcher.find() )
			{
				// in this case, we'll have several entries that have the same index, so
				// we'll need to pull those out and put them into a new key-value list
				final String separator = getPersistenceBuilder().getSeparator();
				final List< Pair< String, String > > elementKeyValues = new ArrayList<>();
				for( Pair< String, String > copiedKeyValue : keyValues )
				{
					final String copiedKey = copiedKeyValue.getFirst();
					final String keyFirstElement = key.split( Pattern.quote( separator ) )[ 0 ]; 
					if( copiedKey.startsWith( keyFirstElement ) )
					{
						// strip the first element off the key
						final String strippedKey = KeyValueUtils.stripFirstKeyElement( copiedKey, separator );
						
						// add the key to the list of keys that belong to the compound node
						elementKeyValues.add( new Pair< String, String >( strippedKey, copiedKeyValue.getSecond() ) );
						
						// and remove the element from the list of key values
						copiedKeyValues.remove( copiedKeyValue );
					}
				}
				
				// create the node that holds the compound object and add it to the collection node
				final String persistName = KeyValueUtils.getFirstKeyElement( elementKeyValues.get( 0 ).getFirst(), separator );
				final InfoNode elementNode = InfoNode.createCompoundNode( null, persistName, null );
				collectionNode.addChild( elementNode );
				
				// call the builder (which called this method) to build the compound node
				getPersistenceBuilder().buildInfoNode( elementNode, elementKeyValues );
			}
			else
			{
				// error
				final StringBuffer message = new StringBuffer();
				message.append( "The key neither represents a leaf node nor a compound node. This is a real problem!" + Constants.NEW_LINE );
				message.append( "  Parent Node Persistence Name: " + parentNode.getPersistName() + Constants.NEW_LINE );
				message.append( "  Key: " + key + Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#isRenderer(java.lang.String)
	 */
	@Override
	public boolean isRenderer( final String keyElement )
	{
		return validationPattern.matcher( keyElement ).find();
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#getGroupName(java.lang.String)
	 */
	@Override
	public String getGroupName( final String key )
	{
		final Matcher matcher = decorationPattern.matcher( key );
		String group = null;
		if( matcher.find() )
		{
			group = key.substring( 0, matcher.start() );
		}
		return group;
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
