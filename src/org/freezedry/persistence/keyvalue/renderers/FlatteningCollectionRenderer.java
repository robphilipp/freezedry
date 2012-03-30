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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
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

	private static final Logger LOGGER = Logger.getLogger( FlatteningCollectionRenderer.class );

	private final String decorationRegex;
	private final Pattern decorationPattern;
	private final String validationRegex;
	private final Pattern validationPattern;

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
		validationRegex = "\\w+" + decorationRegex + "|^" + decorationRegex;
		validationPattern = Pattern.compile( validationRegex );

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
			final StringBuffer value = new StringBuffer( "[" );
			int index = 0;
			final int numChildren = infoNode.getChildCount();
			for( InfoNode node : infoNode.getChildren() )
			{
				// add the value
				value.append( node.getValue() );
				
				// add a comma between the values if it isn't the last value
				if( index < numChildren-1 )
				{
					value.append( ", " );
				}
				
				// increment the counter
				index++;
			}
			value.append( "]" );
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
	public void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String >> keyValues )
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
			else
			{
				// forward to parent
				super.buildInfoNode( parentNode, keyValues );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.CollectionRenderer#isRenderer(java.lang.String)
	 */
	@Override
	public boolean isRenderer( final String keyElement )
	{
		return validationPattern.matcher( keyElement ).find() || super.isRenderer( keyElement );
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
	 * @see org.freezedry.persistence.keyvalue.renderers.CollectionRenderer#getCopy()
	 */
	@Override
	public FlatteningCollectionRenderer getCopy()
	{
		return new FlatteningCollectionRenderer( this );
	}

}
