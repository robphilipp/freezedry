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
package org.freezedry.persistence.keyvalue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;

public class BasicKeyValueBuilder extends AbstractKeyValueBuilder {

//	private static final Logger LOGGER = Logger.getLogger( BasicKeyValueBuilder.class );

	/**
	 * Constructs a basic key-value builder that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link InfoNode} and
	 * the {@link PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link PersistenceRenderer} used to create key-value pairs for
	 * {@link InfoNode}s that represent an array.
	 * @param separator The separator between the flattened elements of the key
	 * @see AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public BasicKeyValueBuilder( final Map< Class< ? >, PersistenceRenderer > renderers, 
								 final PersistenceRenderer arrayRenderer,
								 final String separator )
	{
		super( renderers, arrayRenderer, separator );
	}

	/**
	 * Constructs a basic key-value builder that uses the default renderers and specified separator.
	 * @param separator The separator between the flattened elements of the key
	 */
	public BasicKeyValueBuilder( final String separator )
	{
		super( separator );
	}

	/**
	 * Constructs a basic key-value builder that uses the default renderers and separator.
	 */
	public BasicKeyValueBuilder()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#buildKeyValuePairs(org.freezedry.persistence.tree.InfoNode)
	 */
	@Override
	public List< Pair< String, Object > > buildKeyValuePairs( final InfoNode rootInfoNode )
	{
		// create the map for holding the key-value pairs.
		final List< Pair< String, Object > > keyValuePairs = new ArrayList<>();
		
		// create the first DOM node from the info-node and add it to the document
//		final Pair< String, Object > rootPair = createKeyValuePairs( rootInfoNode, "", keyValuePairs );
		
		// recursively build the DOM tree from the info-node tree
//		buildKeyValuePairs( rootInfoNode, rootPair.getFirst(), keyValuePairs );
		buildKeyValuePairs( rootInfoNode, rootInfoNode.getPersistName(), keyValuePairs );
		
		// once complete, then return the document (root node of the DOM tree)
		return keyValuePairs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#buildKeyValuePairs(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
	 */
	@Override
	public void buildKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues )
	{
		// run through the node's children, and for each one create and add the key-value pairs
		// to the list of key-value pairs
		for( InfoNode child : infoNode.getChildren() )
		{
			// if a child has been processed already, and marked processed, then we don't process
			// it again. this can occur if the node is, for example, a collection or map, in which
			// case the subnodes are processed outside of this loop, and this method may be called
			// recursively, and we want to ensure that the node is only processed once.
			if( !child.isProcessed() )
			{
				// create the new key value pairs
				createKeyValuePairs( child, key, keyValues, false );
				
				// mark the node as being processed
				child.setIsProcessed( true );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#createKeyValuePairs(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List, boolean)
	 */
	@Override
	public void createKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersitName )
	{
		final boolean isHidePersistName = ( isShowFullKey() ? false : isWithholdPersitName );
		final Class< ? > clazz = infoNode.getClazz();
		if( containsRenderer( clazz ) )
		{
			getRenderer( clazz ).buildKeyValuePair( infoNode, key, keyValues, isHidePersistName );
		}
		else if( clazz.isArray() )
		{
			getArrayRenderer().buildKeyValuePair( infoNode, key, keyValues, isHidePersistName );
		}
		else
		{
			// create the new key based on the specified key and the persistence name
			final String newKey = createKey( infoNode, key, isHidePersistName );
			final Pair< String, Object > keyValuePair = new Pair< String, Object >( newKey, null );
			
			// if the node is a leaf node, then it has a value, and we need to create a key-value pair
			if( infoNode.isLeafNode() )
			{
				// create the key-value pair and return it
				keyValuePair.setSecond( infoNode.getValue() );
				keyValues.add( keyValuePair );
			}
			else
			{
				buildKeyValuePairs( infoNode, newKey, keyValues );
			}
		}
	}
	
	/*
	 * Creates a key based on the specified information. In particular, it deals with the suppression
	 * of the leading separators when the specified key is null or empty. Also withholds the persistence name
	 * from the key if it is intended to be withheld.
	 * @param infoNode The current info node.
	 * @param key The current key
	 * @param isWithholdPersitName true if the persistence name is to be withheld from the key; false otherwise.
	 * @return The newly minted key
	 */
	private String createKey( final InfoNode infoNode, final String key, final boolean isWithholdPersitName )
	{
		final StringBuffer newKey = new StringBuffer();
		if( key != null && !key.isEmpty() )
		{
			newKey.append( key );
			if( !isWithholdPersitName )
			{
				newKey.append( getSeparator() );
			}
		}
		if( !isWithholdPersitName )
		{
			newKey.append( infoNode.getPersistName() );
		}
		return newKey.toString();
	}
}
