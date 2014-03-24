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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;

public class LeafNodeRenderer extends AbstractPersistenceRenderer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( LeafNodeRenderer.class );
	
	/**
	 * Constructs a {@link LeafNodeRenderer} that is used to render {@link InfoNode} representing
	 * leaf {@link InfoNode}s into key value pairs.
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model. The builder calls
	 * this class' {@link #buildKeyValuePair(InfoNode, String, List, boolean)} as part of the recursive
	 * algorithm to flatten the semantic model
	 * @param decorators The mapping between the classes and their {@link Decorator}s. The {@link Decorator}s format the
	 * strings, ints, doubles, etc. For example, by default, strings are surrounded by quotes.
	 */
	public LeafNodeRenderer( final KeyValueBuilder builder, final Map< Class< ? >, Decorator > decorators )
	{
		super( builder, decorators );
	}
	
	/**
	 * Constructs a {@link CollectionRenderer} that is used to render {@link InfoNode} representing
	 * leaf {@link InfoNode}s into key value pairs. Uses the default decorators
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model. The builder calls
	 * this class' {@link #buildKeyValuePair(InfoNode, String, List, boolean)} as part of the recursive
	 * algorithm to flatten the semantic model
	 */
	public LeafNodeRenderer( final KeyValueBuilder builder )
	{
		super( builder );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The key-value renderer to copy
	 */
	public LeafNodeRenderer( final LeafNodeRenderer renderer )
	{
		super( renderer );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersistName )
	{
		// ensure that the info node is a leaf
		if( !infoNode.isLeafNode() )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "To render a key-value pair as a String, the info node must be a leaf node." ).append( Constants.NEW_LINE )
					.append( "  Current Key:" ).append( key ).append( Constants.NEW_LINE )
					.append( "  InfoNode:" ).append( Constants.NEW_LINE )
					.append( "    Persist Name: " ).append( infoNode.getPersistName() ).append( Constants.NEW_LINE )
					.append( "    Node Type: " ).append( infoNode.getNodeType().name() ).append( Constants.NEW_LINE )
					.append( "    Child Nodes: " ).append( infoNode.getChildCount() ).append( Constants.NEW_LINE )
					.append( "    Node Class Type: " ).append( infoNode.getClazz().getName() ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// create the front part of the key
		String newKey = key;
		if( !isWithholdPersistName && infoNode.getPersistName() != null && !infoNode.getPersistName().isEmpty() )
		{
			newKey += getPersistenceBuilder().getSeparator() + infoNode.getPersistName();
		}
		
		// find the decorator, if one exists, that is associated with the class
		final Object object = infoNode.getValue();
		final Class< ? > clazz = object.getClass();
		String value;
		if( containsDecorator( clazz ) )
		{
			value = getDecorator( clazz ).decorate( object );
		}
		else
		{
			value = object.toString();
		}
		keyValues.add( new Pair< String, Object >( newKey, value ) );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#buildInfoNode(org.freezedry.persistence.tree.InfoNode, java.util.List)
	 */
	@Override
	public void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues )
	{
		// make sure there is one and ONLY one key-value pair
		if( keyValues.size() != 1 )
		{
			final StringBuilder message = new StringBuilder()
					.append( "A leaf node can only have one key (name) and on value." ).append( Constants.NEW_LINE )
					.append( "  Parent Node's Persistence Name: " ).append( parentNode.getPersistName() ).append( Constants.NEW_LINE )
					.append( "  Number of Key-Value Pairs: " );
			if( keyValues == null )
			{
				message.append( "[null]" ).append( Constants.NEW_LINE );
			}
			else
			{
				message.append( keyValues.size() ).append( Constants.NEW_LINE );
				for( Pair<String, String> keyValue : keyValues )
				{
					message.append( "    " ).append( keyValue ).append( Constants.NEW_LINE );
				}
			}
			LOGGER.error( message.toString() );
			throw new IllegalStateException( message.toString() );
		}
		
		final String key = keyValues.get( 0 ).getFirst();
		final String value = keyValues.get( 0 ).getSecond();
		final String rawValue = getDecorator( value ).undecorate( value );
		final InfoNode node = InfoNode.createLeafNode( null, rawValue, key, null );
		parentNode.addChild( node );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#isRenderer(java.lang.String)
	 */
	@Override
	public boolean isRenderer( String keyElement )
	{
		return Pattern.matches( "^[\\w\\_]+$", keyElement );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#getGroupName(java.lang.String)
	 */
	@Override
	public String getGroupName( final String key )
	{
		final Pattern decorationPattern = Pattern.compile( "^[\\w\\_]+$" );
		final Matcher matcher = decorationPattern.matcher( key );
		String group = null;
		if( matcher.find() )
		{
			group = key;
		}
		return group;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public LeafNodeRenderer getCopy()
	{
		return new LeafNodeRenderer( this );
	}

}
