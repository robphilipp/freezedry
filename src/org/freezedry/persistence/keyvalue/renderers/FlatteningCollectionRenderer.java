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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.tree.InfoNode;

/**
 * {@link PersistenceRenderer} that is used to renderer simple collections that can be flattened,
 * and forwards those that can't be flattened to the {@link CollectionRenderer}, it's parent class.
 * The flattening renderer takes a {@link Collection}, such as a {@link Set}, {@link List}, etc, whose
 * elements are either {@link String}s or {@link Number}s, and expresses them as:<p>
 * {@code collection = [element1, element2, element3, ..., elementN]}<p>
 * as opposed to:
 * <code><pre>
 * collection[0] = element1
 * collection[1] = element2
 * collection[2] = element3
 * .
 * .
 * .
 * collection[N-1] = elementN
 * </pre></code><p>
 * Using this renderer for collections of collections will result in the following format:
 * <code><pre>
 * collection[0] = [element11, element12, element13, ..., element1M_1]
 * collection[1] = [element21, element22, element23, ..., element2M_2]
 * collection[2] = [element31, element32, element33, ..., element3M_3]
 * .
 * .
 * .
 * collection[N-1] = [elementN1, elementN2, elementN3, ..., elementNM_N]
 * </pre></code><p>
 * as opposed to:
 * <code><pre>
 * collection[0][0] = element11
 * collection[0][1] = element12
 * collection[0][2] = element13
 * .
 * .
 * .
 * collection[0][M_1-1] = element1M_1
 * collection[1][0] = element21
 * collection[1][1] = element22
 * collection[1][2] = element23
 * .
 * .
 * .
 * collection[1][M_2-1] = element2M_2
 * collection[2][0] = element31
 * collection[2][1] = element32
 * collection[2][2] = element33
 * .
 * .
 * .
 * collection[2][M_3-1] = element3M_3
 * .
 * .
 * .
 * collection[N-1][0] = elementN1
 * collection[N-1][1] = elementN2
 * collection[N-1][2] = elementN3
 * .
 * .
 * .
 * collection[N-1][M_3-1] = elementNM_N
 * </pre></code><p>
 *  
 * @see CollectionRenderer
 * 
 * @author Robert Philipp
 */
public class FlatteningCollectionRenderer extends CollectionRenderer {

//	private static final Logger LOGGER = Logger.getLogger( FlatteningCollectionRenderer.class );

	private final String decorationRegex;
	private final Pattern decorationPattern;
	private final String validationRegex;
	private final Pattern validationPattern;
	
	private final String listBegin = "[";
	private final String listEnd= "]";
	private final String listSeparator = ",";
	private final String listRegex;
	private final Pattern listPattern;

	/**
	 * Constructs a {@link FlatteningCollectionRenderer} that is used to render {@link InfoNode} representing
	 * {@link Collection}s into key value pairs.
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model. The builder calls
	 * this class' {@link #buildKeyValuePair(InfoNode, String, List, boolean)} as part of the recursive
	 * algorithm to flatten the semantic model
	 * @param indexDecorator The {@link Decorator} for the index.
	 */
	public FlatteningCollectionRenderer( final KeyValueBuilder builder, final String openIndex, final String closeIndex )
	{
		super( builder, openIndex, closeIndex );
		
		// create and compile the regex pattern for the decoration
		decorationRegex = Pattern.quote( getOpenIndex() ) + Pattern.quote( getCloseIndex() ) + "$";
		decorationPattern = Pattern.compile( decorationRegex );
		
		// create and compile the regex pattern for validating the complete key
		// we allow \w+ and [0-9] and {"key"} before the ending "[]". For example, the following
		// keys would be allowed: collection[]; collection[0][]; collection{3}[]; collection{"test"}[]
		// The regular expression is ^\w*(\[[0-9]\])?((\{"\w+"\})|(\{\w+\}))?\[\]$
		validationRegex = "^\\w*(\\[[0-9]\\])?(\\{(\\w+)|(\"\\w+\")\\})?" + decorationRegex;
		validationPattern = Pattern.compile( validationRegex );

		// create and compile the regex pattern for validating the list value 
		// i.e [ element1, element2, ..., elementN ]
		// the regex expression below describes the allowable lists
		// (^\[(\s*"\w+"\s*,\s*)*(\s*"\w+"\s*)\]$)|(^\[(\s*\w+\s*,\s*)*(\s*\w+\s*)\]$)
		// (^\[(\s*"\w+"\s*,\s*)*(\s*"\w+"\s*)\]$)|(^\[(\s*\w+(\.\w+)?\s*,\s*)*(\s*-?\w+(\.\w+)?\s*)\]$)
		final StringBuffer listRegexBuffer = new StringBuffer();
		// number lists
		listRegexBuffer.append( "(^" + Pattern.quote( listBegin ) );
		listRegexBuffer.append( "(\\s*-?\\w+(\\.\\w+)?\\s*" + Pattern.quote( listSeparator ) + "\\s*)*(\\s*-?\\w+(\\.\\w+)?\\s*)" );
		listRegexBuffer.append( Pattern.quote( listEnd ) + ")$" );
		// or
		listRegexBuffer.append( "|" );
		// string list
		listRegexBuffer.append( "(^" + Pattern.quote( listBegin ) );
		listRegexBuffer.append( "(\\s*\"\\w+\"\\s*" + Pattern.quote( listSeparator ) + "\\s*)*(\\s*\"\\w+\"\\s*)" );
		listRegexBuffer.append( Pattern.quote( listEnd ) + ")$" );
		listRegex = listRegexBuffer.toString();
		listPattern = Pattern.compile( listRegex );
	}

	/**
	 * Constructs a {@link FlatteningCollectionRenderer} that is used to render {@link InfoNode} representing
	 * {@link Collection}s into key value pairs. Uses the default the index decorator which prepends
	 * a "{@code [}" onto the index and appends a "{@code ]}" to the end of the index. For example,
	 * if the {@code index=1} then the index would be decorated to look like {@code [1]}.
	 * @param builder
	 */
	public FlatteningCollectionRenderer( final KeyValueBuilder builder )
	{
		this( builder, OPEN, CLOSE );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link FlatteningCollectionRenderer} to copy
	 */
	public FlatteningCollectionRenderer( final FlatteningCollectionRenderer renderer )
	{
		super( renderer );

		this.decorationRegex = renderer.decorationRegex;
		this.decorationPattern = renderer.decorationPattern;
		this.validationRegex = renderer.validationRegex;
		this.validationPattern = renderer.validationPattern;
		this.listRegex = renderer.listRegex;
		this.listPattern = renderer.listPattern;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.CollectionRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List, boolean)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, 
								   final String key, 
								   final List< Pair< String, Object > > keyValues,
								   final boolean isWithholdPersistName )
	{
		if( !areAllNodesLeafs( infoNode ) )
		{
			super.buildKeyValuePair( infoNode, key, keyValues, isWithholdPersistName );
		}
		else
		{
			// at this point we know that all the nodes are children, since we just checked
			// so we create the from the previous key and the persistence name of the field
			final StringBuffer keyBuffer = new StringBuffer( key );
			if( !isWithholdPersistName )
			{
				keyBuffer.append( getPersistenceBuilder().getSeparator() );
			}
			keyBuffer.append( infoNode.getPersistName() ).append( getOpenIndex() ).append( getCloseIndex() );
			
			// now we construct the value in the form of "[ element1, element2, ..., elementN ]"
			final StringBuffer value = new StringBuffer( listBegin );
			int index = 0;
			final int numChildren = infoNode.getChildCount();
			for( InfoNode node : infoNode.getChildren() )
			{
				// add the decorated value
				final Decorator decorator = getDecorator( node.getClazz() );
				value.append( decorator.decorate( node.getValue() ) );
				
				// add a comma between the values if it isn't the last value
				if( index < numChildren-1 )
				{
					value.append( listSeparator + " " );
				}
				
				// increment the counter
				index++;
			}
			value.append( listEnd );
			keyValues.add( new Pair< String, Object >( keyBuffer.toString(), value.toString() ) );
		}
	}
	
	/*
	 * Returns true if all the specified parentNode's (direct) children are leaf nodes; false otherwise.
	 * @param parentNode The parent node whose children to check
	 * @return true if all the specified parentNode's (direct) children are leaf nodes; false otherwise
	 */
	private static boolean areAllNodesLeafs( final InfoNode parentNode )
	{
		boolean isAllLeafs = true;
		
		// do fast fail check.
		for( InfoNode node : parentNode.getChildren() )
		{
			if( !node.isLeafNode() )
			{
				isAllLeafs = false;
				break;
			}
		}
		return isAllLeafs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.CollectionRenderer#buildInfoNode(org.freezedry.persistence.tree.InfoNode, java.util.List)
	 */
	@Override
	public void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues )
	{
		// nothing to do
		if( keyValues == null || keyValues.isEmpty() )
		{
			return;
		}
		
		// if all the key-values aren't flattened, then we need to forward this to the 
		// parent collection renderer 
		if( !areAllFlattenedCollections( keyValues ) )
		{
			super.buildInfoNode( parentNode, keyValues );
		}
		else
		{
			// grab the group name for the collection, and create the compound node
			// that holds the elements of the collection as child nodes, and add it
			// to the parent node
			final String group = getGroupName( keyValues.get( 0 ).getFirst() );
			final InfoNode collectionNode = InfoNode.createCompoundNode( null, group, null );
			parentNode.addChild( collectionNode );
			
			// run through the list of key-values creating the child nodes for the collection node
			for( Pair< String, String > keyValue : keyValues )
			{
				// grab the key
				final String key = keyValue.getFirst();
				
				// this must match the validation pattern, i.e. that it is a simple collection. if
				// it doesn't match the simple collection pattern, the forward it to the compound 
				// collection renderer (CollectionRenderer, this class' parent class)
				final Matcher leafMatcher = validationPattern.matcher( key );
				if( leafMatcher.find() )
				{
					// we should have list represented by "[ element1, element2, ..., elementN ]". We need to
					// pull apart the elements.
					final String valueList = keyValue.getSecond();
					final Matcher listMatcher = listPattern.matcher( valueList );
					if( listMatcher.find() )
					{
						// create the list of values
						final List< String > values = parseValueList( valueList );
						
						for( String value : values )
						{
							// its a leaf, so now we need to figure out what the value is. we know that
							// it must be a number (integer, double) or a string.
							final Decorator decorator = getDecorator( value); 
							final String rawValue = decorator.undecorate( value );
							final String persistName = decorator.representedClass().getSimpleName();
							
							// create the leaf info node and add it to the collection node
							final InfoNode elementNode = InfoNode.createLeafNode( null, rawValue, persistName, null );
							collectionNode.addChild( elementNode );
						}
					}
				}
			}
		}
	}
	
	/*
	 * Returns true if all the keys have the form of a flattened list; false otherwise
	 * @param keyValues The list of key-value pairs of which to check the keys
	 * @return true if all the keys have the form of a flattened list; false otherwise
	 */
	private boolean areAllFlattenedCollections( final List< Pair< String, String > > keyValues )
	{
		boolean allFlattened = true;
		for( Pair< String, String > keyValue : keyValues )
		{
			// grab the key
			final String key = keyValue.getFirst();
			
			// this must match the validation pattern, i.e. that it is a simple collection. if
			// it doesn't match the simple collection pattern, the forward it to the compound 
			// collection renderer (CollectionRenderer, this class' parent class)
			final Matcher leafMatcher = validationPattern.matcher( key );
			if( !leafMatcher.find() )
			{
				allFlattened = false;
			}
		}
		return allFlattened;
	}
	
	/*
	 * Takes a string of the form "[element1, element2, ...., elementN]" and parses it into
	 * a list of trimmed strings. A list can contain elements that either all strings, or all
	 * numbers. Strings are represented by surrounding quotes. For example a list of numbers
	 * could be "[ 1, 3, 4, 7, 9, 11]", and a list of strings "[ "house", "car", "dog", "cat" ]"
	 * @param valueList The string representation of a list of numbers or a list of strings
	 * @return A {@link List} of {@link String} that has been tokenized based on the list separator.
	 */
	private List< String > parseValueList( final String valueList )
	{
		// make a copy of the string
		String list = new String( valueList );
		
		// pull the list begin (default value is "[") and end (default value is "]") string out
		list = list.replaceAll( "^" + Pattern.quote( listBegin ), "" );
		list = list.replaceAll( Pattern.quote( listEnd ) + "$", "" );

		// tokenize by the list separator
		final String[] valueArray = list.split( Pattern.quote( listSeparator ) );
		
		// trim each string and add it to the array list
		final List< String > values = new ArrayList<>();
		for( String value : valueArray )
		{
			values.add( value.trim() );
		}
		
		return values;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.CollectionRenderer#isRenderer(java.lang.String)
	 */
	@Override
	public boolean isRenderer( final String keyElement )
	{
		// because this renderer calls back to its parent, the CollectionRenderer, we claim this to be
		// the renderer if the key element matches this regular expression or the parent's regular expression
		final boolean isThisRenderer = validationPattern.matcher( keyElement ).find();
		final boolean isParentRenderer = super.isRenderer( keyElement ); 
		return isThisRenderer || isParentRenderer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#getGroupName(java.lang.String)
	 */
	@Override
	public String getGroupName( final String key )
	{
		String group = null;
		
		// we check to see if the key matches the decorations from this renderer,
		// and if not, then we call the parent renderer's getGroupName(...) method.
		// if neither renderers match, then the group will be returned as null
		final Matcher matcher = decorationPattern.matcher( key );
		if( matcher.find() )
		{
			group = getGroup( key.substring( 0, matcher.start() ) );
		}
		else
		{
			group = super.getGroupName( key );
		}
		return group;
	}

	/*
	 * Recursive method that further deconstructs to get the raw group name. For example,
	 * suppose that the collection is matrix[0][], matrix[1][], etc. Then we want to the
	 * group name to be "matrix", and not have a separate group name for "matrix[0]", 
	 * "matrix[1]", etc.
	 * @param key The key from which to pull the groupName
	 * @return The raw group name
	 */
	private String getGroup( final String key )
	{
		String groupName = key;
		String tempName = null;
		do
		{
			// grab the group name from the CollectionRenderer (the parent class)
			tempName = super.getGroupName( groupName );
			
			// if the returned group name is null, or equals the previous call, then we're done
			// otherwise, set the group name to the new value, and recurse through the collection
			if( tempName != null && !tempName.equals( groupName ) )
			{
				groupName = tempName;
				tempName = getGroup( groupName );
			}
		}
		while( tempName != null && !tempName.equals( groupName ) );
		
		return groupName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.CollectionRenderer#getCopy()
	 */
	@Override
	public FlatteningCollectionRenderer getCopy()
	{
		return new FlatteningCollectionRenderer( this );
	}

}
