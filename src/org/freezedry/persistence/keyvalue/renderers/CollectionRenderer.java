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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.freezedry.persistence.utils.ReflectionUtils;

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

	protected static final String OPEN = "[";
	protected static final String CLOSE = "]";
	
	private final String openIndex;
	private final String closeIndex;
	private final StringDecorator indexDecorator;
	private final String decorationRegex;
	private final Pattern decorationPattern;
	private final String validationRegex;
	private final Pattern validationPattern;
	
	private final Set< Class< ? > > withholdPeristName;
	private boolean withholdArrayPersistName = true;

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
		
		this.openIndex = openIndex;
		this.closeIndex = closeIndex;
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
		
		// create the set of classes for with to withhold the persistence name for compound collection elements
		// for example, List< List< Double > > called matrix gets rendered as matrix[i][j] instead of
		// matrix[i].ArrayList[j], or List< Map< String, String > > listOfMap gets renderered as listOfMap[i]{key}
		withholdPeristName = createDefaultPersistenceWithholding();
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
	 * @param renderer The {@link CollectionRenderer} to copy
	 */
	public CollectionRenderer( final CollectionRenderer renderer )
	{
		super( renderer );
		
		this.openIndex = renderer.openIndex;
		this.closeIndex = renderer.closeIndex;
		this.indexDecorator = renderer.indexDecorator.getCopy();
		this.decorationRegex = renderer.decorationRegex;
		this.decorationPattern = renderer.decorationPattern;
		this.validationRegex = renderer.validationRegex;
		this.validationPattern = renderer.validationPattern;
		this.withholdPeristName = renderer.withholdPeristName;
		this.withholdArrayPersistName = renderer.withholdArrayPersistName;
	}

	/*
	 * Creates the set of classes for with to withhold the persistence name for compound collection elements
	 * for example, List< List< Double > > called matrix gets rendered as matrix[i][j] instead of
	 * matrix[i].ArrayList[j], or List< Map< String, String > > listOfMap gets renderered as listOfMap[i]{key}
	 * @return
	 */
	private static Set< Class< ? > > createDefaultPersistenceWithholding()
	{
		final Set< Class< ? > > withhold = new HashSet<>();
		withhold.add( Collection.class );
		withhold.add( Map.class );
		return withhold;
	}
	
	/**
	 * Tells the renderer to withhold the persistence name for the specified {@link Class} in
	 * instances where the {@link Collection} element is compound, and that compound element
	 * is the specified {@link Class} or a subclass of the specified {@link Class}. For example,
	 * when we have a {@code List< Map< String, String > >} called {@code listOfMaps}, then for each
	 * list element, we have a {@link Map}, and so we would want to render it as <code>listOfMap[i]{key}</code>.
	 * Similarly, if we have a {@code List< List< Double > >} called {@code matrix}, we would want to 
	 * render it as {@code matrix[i][j]}.
	 * <p>For arrays, see the {@link #withholdArrayPersistName} method.
	 * @param clazz The specified {@link Class} for which to withhold the persistence name
	 * @return true if the {@link Class} was added; false otherwise
	 * @see #withholdArrayPersistName
	 */
	public boolean withholdCompoundPersistNameFor( final Class< ? > clazz )
	{
		return withholdPeristName.add( clazz );
	}
	
	/**
	 * Tells the renderer that it should no longer withhold the persistence name for the specified {@link Class} or
	 * its subclasses.
	 * @param clazz The specified {@link Class} for which to no longer withhold the persistence name
	 * @return true if the {@link Class} was removed; false otherwise
	 * @see #withholdPeristName
	 * @see #withholdArrayPersistName
	 */
	public boolean removePersistNameWithholding( final Class< ? > clazz )
	{
		return withholdPeristName.remove( clazz );
	}
	
	/**
	 * Tells the renderer to withhold the persistence name for arrays in instances where the {@link Collection} 
	 * element is compound, and that compound element is an array. For example, if we have a {@code List< int[] > >} 
	 * called {@code matrix}, we would want to render it as {@code matrix[i][j]}.
	 * @param isWithhold true if peristence names are to be withheld; false otherwise
	 * @see #withholdPeristName
	 */
	public void setWithholdCompoundPersistNameForArrays( final boolean isWithhold )
	{
		withholdArrayPersistName = isWithhold;
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
				final String newKey = createNodeKey( key, infoNode, index );
				getPersistenceBuilder().createKeyValuePairs( node, newKey, keyValues, true );
			}
			else
			{
				// create the key-value pair and return it
				final String newKey = createNodeKey( key, infoNode, index );
				boolean hidePersistName = false;
				
				// next we need to check whether the group name for the key is empty. for example,
				// when we have a List< Map< String, String > > called listOfMaps, then for each
				// list element, we have a map, and so we would want to render it as listOfMap[i]{key}.
				// similarly, if we have a List< List< Double > > called matrix, we would want to 
				// render it as matrix[i][j].
				if( isZeroOutPersistName( node ) )
				{
					node.setPersistName( "" );
					hidePersistName = true;
				}
				
				// have the key-value builder that called this method create a new node. back
				// into the recursive algorithm.
				getPersistenceBuilder().createKeyValuePairs( node, newKey, keyValues, hidePersistName );
			}

			// increment the index count
			++index;
			
			// mark the node as processed so that it doesn't get processed again
			node.setIsProcessed( true );
		}
	}

	/**
	 * Returns true if we have specified that when the collection element is a compound
	 * element of the certain types, then we zero out the node's persistence name. For example,
	 * when we have a {@code List< Map< String, String > >} called {@code listOfMaps}, then for each
	 * list element, we have a {@link Map}, and so we would want to render it as <code>listOfMap[i]{key}</code>.
	 * Similarly, if we have a {@code List< List< Double > >} called {@code matrix}, we would want to 
	 * render it as {@code matrix[i][j]}.
	 * @param node The node containing the compound collection element
	 * @return true if the persistence name of the node should be zeroed out. 
	 */
	protected boolean isZeroOutPersistName( final InfoNode node )
	{
		boolean isZeroOut = false;
		
		// grab the class represented by the node
		final Class< ? > nodeClazz = node.getClazz();
		
		// first we check to see if we are to withhold the perist name for
		// compound collection elements that are arrays
		if( withholdArrayPersistName && nodeClazz.isArray() )
		{
			isZeroOut = true;
		}
		else
		{
			// run through the list of the classes for which to withhold the
			// persist name if the compound elements are those classes or
			// subclasses of those classes
			for( Class< ? > clazz : withholdPeristName )
			{
				if( ReflectionUtils.isClassOrSuperclass( clazz, nodeClazz ) )
				{
					isZeroOut = true;
					break;
				}
			}
		}
		
		return isZeroOut;
	}

	/**
	 * Creates a key for a node collection. For example, if the persist name for a {@link List} is
	 * {@code people}, which is a <code>{@link List}< {@link Person} ></code>, then the key will be {@code people[i].Person}
	 * where the {@code i} is the index of the list.
	 * @param key The current key to which to append the persisted name and decorated index
	 * @param parentNode The parent node, which holds the name of the field (in this example, "{@code people}")
	 * @param node The current node (in this example, "{@code Person}")
	 * @param index The index of the element in the {@link List}
	 * @return The key
	 */
	protected final String createNodeKey( final String key, final InfoNode parentNode, final int index )
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
					final String keyFirstElement = extractElementKeyPart( key.split( Pattern.quote( separator ) )[ 0 ] ); 
					if( copiedKey.startsWith( keyFirstElement ) )
					{
						// strip the first element off the key. this could mean one of three things:
						// 1. for something like matrix[0][1] we remove all but the [1]
						// 2. for something like matrix[0].Date we remove all but the Date
						// 3. for something like matrix[0]{"April"}.Mood we remove all but [1].Mood
						final String strippedKey = stripFirstElement( copiedKey, separator );
						
						// add the key to the list of keys that belong to the compound node
						elementKeyValues.add( new Pair< String, String >( strippedKey, copiedKeyValue.getSecond() ) );
						
						// and remove the element from the list of key values
						copiedKeyValues.remove( copiedKeyValue );
					}
				}
				
				// create the node that holds the compound object and add it to the collection node
				final String persistName = KeyValueUtils.getFirstKeyElement( elementKeyValues.get( 0 ).getFirst(), separator );
				
				// call the builder (which called this method) to build the compound node
				getPersistenceBuilder().createInfoNode( collectionNode, persistName, elementKeyValues );
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
	 * Extracts the map key part from the key. For example, if the key is <code>months{"January"}[0]</code>
	 * this method will return <code>months{"January"}</code>.
	 * @param key The key from which to extract the map-key part
	 * @return the map key part from the key
	 */
	private String extractElementKeyPart( final String key )
	{
		String mapKey = null;
		final Matcher matcher = decorationPattern.matcher( key );
		if( matcher.find() )
		{
			mapKey = key.substring( 0, matcher.end() );
		}
		return mapKey;
	}
	
	/*
	 * Removes the map key part from the key. For example, if the key is <code>months{"January"}[0]</code>
	 * this method will return <code>[0]</code>.
	 * @param key The key from which to strip the map-key part
	 * @return the key, stripped of the map key part
	 */
	private String removeElementKeyPart( final String key )
	{
		String keyRemainder = null;
		final Matcher matcher = decorationPattern.matcher( key );
		if( matcher.find() )
		{
			keyRemainder = key.substring( matcher.end() );
		}
		return keyRemainder;
	}
	
	private String stripFirstElement( final String key, final String separator )
	{
		String remainder = removeElementKeyPart( key );
		if( remainder.startsWith( separator ) )
		{
//			remainder.replaceFirst( Pattern.quote( separator ) + "?", "" );
			remainder = remainder.substring( 1 );
		}
		return remainder;
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
	
	protected String getOpenIndex()
	{
		return openIndex;
	}
	
	protected String getCloseIndex()
	{
		return closeIndex;
	}
	
	/**
	 * @return The regular expression string that is used to determine whether an index
	 * is decorated by this renderer
	 */
	protected final String getDecorationRegex()
	{
		return decorationRegex;
	}

	/**
	 * @return The regular expression string that is used to validate that the a string
	 * conforms to the this renderer.
	 */
	protected final String getValidationRegex()
	{
		return validationRegex;
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
