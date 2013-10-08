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

import org.apache.log4j.Logger;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;

public class LeafNodeRenderer extends AbstractPersistenceRenderer {
	
	private static final Logger LOGGER = Logger.getLogger( LeafNodeRenderer.class );
	
	/**
	 * Constructs a {@link CollectionRenderer} that is used to render {@link InfoNode} representing
	 * leaf {@link InfoNode}s into key value pairs.
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model. The builder calls
	 * this class' {@link #buildKeyValuePair(InfoNode, String, List, boolean)} as part of the recursive
	 * algorithm to flatten the semantic model
	 * @param decorators The mapping between the classes and their {@link Decorator}s. The {@link Decorator}s format the
	 * strings, ints, doubles, etc. For example, by default, strings are surrounded by quotes.
	 */
	public LeafNodeRenderer( final KeyValueBuilder writer, final Map< Class< ? >, Decorator > decorators )
	{
		super( writer, decorators );
	}
	
	/**
	 * Constructs a {@link CollectionRenderer} that is used to render {@link InfoNode} representing
	 * leaf {@link InfoNode}s into key value pairs. Uses the default decorators
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model. The builder calls
	 * this class' {@link #buildKeyValuePair(InfoNode, String, List, boolean)} as part of the recursive
	 * algorithm to flatten the semantic model
	 */
	public LeafNodeRenderer( final KeyValueBuilder writer )
	{
		super( writer );
	}
	
	/**
	 * Copy constructor
	 * @param renderer
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
			final StringBuffer message = new StringBuffer();
			message.append( "To render a key-value pair as a String, the info node must be a leaf node." + Constants.NEW_LINE );
			message.append( "  Current Key:" + key + Constants.NEW_LINE );
			message.append( "  InfoNode:" + Constants.NEW_LINE );
			message.append( "    Persist Name: " + infoNode.getPersistName() + Constants.NEW_LINE );
			message.append( "    Node Type: " + infoNode.getNodeType().name() + Constants.NEW_LINE );
			message.append( "    Child Nodes: " + infoNode.getChildCount() + Constants.NEW_LINE );
			message.append( "    Node Class Type: " + infoNode.getClazz().getName() + Constants.NEW_LINE );
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
			final StringBuffer message = new StringBuffer();
			message.append( "A leaf node can only have one key (name) and on value." + Constants.NEW_LINE );
			message.append( "  Parent Node's Persistence Name: " + parentNode.getPersistName() + Constants.NEW_LINE );
			message.append( "  Number of Key-Value Pairs: " + ( keyValues == null ? "[null]" : keyValues.size() ) + Constants.NEW_LINE );
			for( Pair< String, String > keyValue : keyValues )
			{
				message.append( "    " + keyValue + Constants.NEW_LINE );
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
		return Pattern.matches( "^\\w+$", keyElement );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#getGroupName(java.lang.String)
	 */
	@Override
	public String getGroupName( final String key )
	{
		final Pattern decorationPattern = Pattern.compile( "^\\w+$" );
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
