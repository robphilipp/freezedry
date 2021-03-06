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

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.keyvalue.utils.KeyValueUtils;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Basic key-value list builder that flattens the semantic model and returns a list of key-value pairs.
 * The entry point into the builder is the {@link #buildKeyValuePairs(InfoNode)} method.
 *  
 * @author rob
 */
public class BasicKeyValueBuilder extends AbstractKeyValueBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger( BasicKeyValueBuilder.class );

	private boolean useClassAsRootKey = false;

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

	/**
	 * The entry point for building the {@link List} of key-value pairs from the semantic model,
	 * through a recursive algorithm ({@link #buildKeyValuePairs(InfoNode, String, List)}. Effectively,
	 * this will flatten the tree into a list of key-value pairs.
	 * @param rootInfoNode The root {@link InfoNode} of the semantic model
	 * @return the {@link List} of key-value pairs
	 * @see #buildKeyValuePairs(InfoNode, String, List)
	 */
	@Override
	public List< Pair< String, Object > > buildKeyValuePairs( final InfoNode rootInfoNode )
	{
		// create the map for holding the key-value pairs.
		final List< Pair< String, Object > > keyValuePairs = new ArrayList<>();

		// make a deep copy of the semantic model (since there are parts of the code that change the model)
		final InfoNode rootNode = rootInfoNode.getCopy();

		// recursively build the key-value pairs from the info-node tree
		buildKeyValuePairs( rootNode, rootNode.getPersistName(), keyValuePairs );
		
		// once complete, then return the list of key-value pairs
		return keyValuePairs;
	}

	/**
	 * The recursive algorithm for flattening the semantic model into a {@link List} of key-value pairs.
	 * @param infoNode The current node in the semantic model ({@link InfoNode}) for processing.
	 * @param key The current key, which has accumulated the parents persistence names as part of the flattening
	 * @param keyValues The current list of key-values to which to add the ones created in this algorithm.
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

	/**
	 * Creates the actual key-value pair, though for compound nodes, it calls back its calling method,
	 * {@link #buildKeyValuePairs(InfoNode, String, List)}.
	 * @param infoNode The current node in the semantic model ({@link InfoNode}) for processing.
	 * @param key The current key, which has accumulated the parents persistence names as part of the flattening
	 * @param keyValues The current list of key-values to which to add the ones created in this algorithm.
	 * @param isWithholdPersitName true if the current persistence name should not be added to the key; false otherwise.
	 * This parameter allows {@link PersistenceRenderer}s to suppress the persistence name when it is appropriate. For
	 * example, in a {@link List} of {@link String}, you may not want to at "{@code String}" to the key.
	 */
	@Override
	public void createKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersitName )
	{
		// determine whether to show the persistence name. the isShowFullKey is top dog.
		final boolean isHidePersistName = (!isShowFullKey() && isWithholdPersitName);
		
		// grab the persistence renderer for the class or for its closest ancestor, or for the 
		// array renderer if the class is an array
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
			final Pair< String, Object > keyValuePair = new Pair<>( newKey, null );
			
			// if the node is a leaf node, then it has a value, and we need to create a key-value pair
			// otherwise we need to recurse back to the calling method to build out the key-value pairs
			// for a compound node
			if( infoNode.isLeafNode() )
			{
				// create the key-value pair and add it to the list of key-values
				keyValuePair.setSecond( infoNode.getValue() );
				keyValues.add( keyValuePair );
			}
			else
			{
				buildKeyValuePairs( infoNode, newKey, keyValues );
			}
		}
	}
	
	/**
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
		final StringBuilder newKey = new StringBuilder();
		
		// if the key is empty then don't add anything
		if( key != null && !key.isEmpty() )
		{
			newKey.append( key );
			if( !isWithholdPersitName )
			{
				newKey.append( getSeparator() );
			}
		}
		
		// if we're not withholding the persistence name, then add it to the key
		if( !isWithholdPersitName )
		{
			newKey.append( infoNode.getPersistName() );
		}
		
		return newKey.toString();
	}

	/**
	 * Main entry point for building the semantic model from a list of key-value pairs and the target class.
	 * @param clazz The target class for the building of the semantic model
	 * @param keyValues The key-value pairs
	 * @return The root {@link InfoNode} of the semantic model
	 */
	@Override
	public InfoNode buildInfoNode( final Class< ? > clazz, final List< Pair< String, String > > keyValues )
	{
		// grab the root key from all the values and use it to create the root info node.
		// recall that the root key should have the same name as the clazz we're using as
		// a template.
		final String rootKey = getRootKey( keyValues, clazz );
		final InfoNode rootNode = InfoNode.createRootNode( rootKey, clazz );
		
		// build the semantic model
		buildInfoNode( rootNode, keyValues );
		
		return rootNode;
	}

	/**
	 * Recursively builds the semantic model. The keys in the key-value list should all have as their
	 * first element, the name found in the parentNode's persistence name.
	 * @param parentNode The node to which to add the child nodes
	 * @param keyValues The list of key-value pairs. The first key element of every key should match
	 * the persistence name of the parent node.
	 */
	@Override
	public void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues )
	{
		// grab the persistence name from the parent node. Validate that the first elements of each 
		// key equal this the persistence name 
		final String rootKey = parentNode.getPersistName();
		validiateRootKey( keyValues, getSeparator(), rootKey );
		
		// strip the root key element from all the keys. For example, suppose the keys all start with
		// "Division.". And suppose further that the rootKey = "Division". The "Division." will be
		// stripped from each key in the list. So, "Division.people.Person[1]" would become "people.Person[1]".
		final List< Pair< String, String > > strippedKeyValues = KeyValueUtils.stripFirstKeyElement( keyValues, getSeparator() );

		// find the groups in the newly string list, and then create a new info node for each group
		final Map< String, List< Pair< String, String > > > groups = getGroups( strippedKeyValues, getSeparator() );
		for( Map.Entry< String, List< Pair< String, String > > > entry : groups.entrySet() )
		{
			// create the info node
			createInfoNode( parentNode, entry.getKey(), entry.getValue() );
		}
	}

	/**
	 * Creates an {@link InfoNode} based on the group name and the specified key-value pairs. Part of the
	 * recursive algorithm to build the semantic model.
	 * @param parentNode The node to which to add the child nodes
	 * @param groupName The name of the group that will appear as the persistence name
	 * @param keyValues The list of key-value pairs. The first key element of every key should match
	 * the persistence name of the parent node.
	 * @see #buildInfoNode(InfoNode, List)
	 */
	@Override
	public void createInfoNode( final InfoNode parentNode, final String groupName, final List< Pair< String, String > > keyValues )
	{
		if( LOGGER.isDebugEnabled() )
		{
			final StringBuilder message = new StringBuilder( "Creating InfoNode; " )
					.append( "parent node: " ).append( parentNode.getPersistName() )
					.append( "group name: " ).append( groupName );
			for( Pair< String, String > pair : keyValues )
			{
				message.append( Constants.NEW_LINE ).append( pair );
			}
			LOGGER.debug( message.toString() );
		}

		// key-value list cannot be empty.
		if( keyValues.isEmpty() )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Cannot create an InfoNode when the specified list of key-values for the group is empty." ).append( Constants.NEW_LINE );
			message.append( "  Parent Node's Persistence Name: " ).append( parentNode.getPersistName() ).append( Constants.NEW_LINE );
			message.append( "  Group Name: " ).append( groupName ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		// leaf node
		else if( keyValues.size() == 1 && !keyValues.get( 0 ).getFirst().contains( getSeparator() ) )
		{
			// grab the key and make sure that it matches the group name. at this point it shouldn't
			// have any decorations (i.e. for collection or map or anything else)
			final String name = keyValues.get( 0 ).getFirst();
			if( !groupName.equals( getGroupName( name ) ) )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "The group name must match the persist name for a leaf node." ).append( Constants.NEW_LINE );
				message.append( "  Parent Node's Persistence Name: " ).append( parentNode.getPersistName() ).append( Constants.NEW_LINE );
				message.append( "  Group Name: " ).append( groupName ).append( Constants.NEW_LINE );
				message.append( "  Key: " ).append( name ).append( Constants.NEW_LINE );
				message.append( "  Value: " ).append( keyValues.get( 0 ).getSecond() );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString() );
			}

			// in this case, the renderer for the key name should be a leaf-node renderer, so
			// lets pull it, and then ask it to build the info node for us, and add that new
			// info node to the parent, and then we're done
			PersistenceRenderer renderer = getRenderer( name );
			if( renderer == null )
			{
				renderer = getRenderer( String.class );
			}
			renderer.buildInfoNode( parentNode, keyValues );
		}
		// for compound nodes, we need to call the appropriate renderer's buildInfoNode(...) method. and
		// let the recursion begin.
		else
		{
			// grab the key from the first key value, and since all keys have the same group
			// all we need is to grab the first key and use it as a pattern for the remaining
			// key-value pairs in this group
			final String rootKey = keyValues.get( 0 ).getFirst();
			
			// get the appropriate renderer and ask it to build the info node from that type
			final PersistenceRenderer renderer = getRenderer( rootKey );
			if( renderer != null )
			{
				renderer.buildInfoNode( parentNode, keyValues );
			}
			else
			{
				final InfoNode node = InfoNode.createCompoundNode( null, groupName, null );
				parentNode.addChild( node );
				buildInfoNode( node, keyValues );
			}
		}
	}

	/**
	 * In cases where the key-value pairs don't ALL begin with the SAME root key, this method
	 * allows you to tell the reader to use the target class name as the root key. This will cause 
	 * the reader to act like all the keys begin with this key.
	 * @param isUseClass true if the reader should use the target class name as the root key; false otherwise
	 */
	public void setUseClassAsRootKey( final boolean isUseClass )
	{
		this.useClassAsRootKey = isUseClass;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#getRootKey(java.util.List, java.lang.Class)
	 */
	@Override
	public String getRootKey( final List< Pair< String, String > > keyValues, final Class< ? > clazz )
	{
		String key;
		if( useClassAsRootKey )
		{
			key = clazz.getSimpleName();
		}
		else
		{
			key = validiateRootKey( keyValues, getSeparator(), clazz.getSimpleName() );
		}
		return key;
	}

	/**
	 * Pulls the first key element from each key, ensures that they are all the same, and then returns that root key.
	 * @param keyValues The list of key-value pairs.
	 * @param keyElementSeparator The separator between the key elements. The default value is found in the
	 * {@link AbstractKeyValueBuilder#KEY_ELEMENT_SEPARATOR} and is usually "{@code :}".
	 * @return the root key.
	 */
	public String validiateRootKey( final List< Pair< String, String > > keyValues, final String keyElementSeparator )
	{
		return validiateRootKey( keyValues, keyElementSeparator, null );
	}
	
	/**
	 * Pulls the first key element from each key, ensures that they are all the same, and then returns that root key.
	 * @param keyValues The list of key-value pairs.
	 * @param keyElementSeparator The separator between the key elements. The default value is found in the
	 * {@link AbstractKeyValueBuilder#KEY_ELEMENT_SEPARATOR} and is usually "{@code :}".
	 * @param desiredName The name the root key should end up being. If it isn't, this method throws an exception.
	 * Set the desiredName to null if you don't want to validate against the name
	 * @return the root key.
	 * @see #validiateRootKey(List, String)
	 */
	public String validiateRootKey( final List< Pair< String, String > > keyValues, final String keyElementSeparator, final String desiredName )
	{
		final Set< String > keySet = new LinkedHashSet<>();
		for( Pair< String, String > pair : keyValues )
		{
			keySet.add( getGroupName( KeyValueUtils.getFirstKeyElement( pair.getFirst(), keyElementSeparator ) ) );
		}
		
		// the first key must all be in the same group. that is the case if the set only
		// has one element, or if all the group names are the same
		if( keySet.size() != 1 )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "The first element of all the keys must be the same. This is the root key." ).append( Constants.NEW_LINE );
			message.append( "  Set elements:" ).append( Constants.NEW_LINE );
			for( String keyName : keySet )
			{
				message.append( "    " ).append( keyName ).append( Constants.NEW_LINE );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// grab the key from the only element in the set
		final String rootKey = keySet.iterator().next();
		if( desiredName != null && !desiredName.equals( rootKey ) )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "The root key is not the same as the persistence name" ).append( Constants.NEW_LINE );
			message.append( "  Root Key: " ).append( rootKey ).append( Constants.NEW_LINE );
			message.append( "  Persistence Name: " ).append( desiredName );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		return rootKey;
	}
	
	/*
	 * Organizes the key values into groups based on the first key element of each key. Returns
	 * a {@code Map< String, List< Pair< String, String > > >} where the map's key is the group
	 * name. The group name is first key element from each key. The {@code List< Pair< String, String > >}
	 * holds all the keys that start with the group name.
	 * @param keyValues The list of key values 
	 * @param separator The key element separator
	 * @return a {@code Map< String, List< Pair< String, String > > >} where the map's key is the group
	 * name. The group name is first key element from each key. The {@code List< Pair< String, String > >}
	 * holds all the keys that start with the group name.
	 */
	private Map< String, List< Pair< String, String > > > getGroups( final List< Pair< String, String > > keyValues, final String separator )
	{
		final Map< String, List< Pair< String, String > > > groups = new LinkedHashMap<>();
		for( Pair< String, String > pair : keyValues )
		{
			// get the first key element.
			final String group = KeyValueUtils.getFirstKeyElement( pair.getFirst(), separator );
			
			// the thing is, though, that this could be decorated or formatted for a map or list 
			// or something else. so we need to pull the group name off, and then we'll have to 
			// figure out what the key represents, and how to deal with it. the good thing is that
			// the key name can only be part of [a-zA-Z_0-9], and so we can find the first character
			// that isn't that, and pull the key name out as the group
			final String groupName = getGroupName( group );
			
			// either add the pair to the list of key-value pairs of an existing group, or create 
			// a new group that contains the key-value pair
			if( groups.containsKey( groupName ) )
			{
				groups.get( groupName ).add( new Pair<>( pair ) );
			}
			else
			{
				final List< Pair< String, String > > pairs = new ArrayList<>();
				pairs.add( new Pair<>( pair ) );
				groups.put( groupName, pairs );
			}
		}
		
		return groups;
	}

	/**
	 * Returns the group name by finding the renderer for which the specified key
	 * matches its regular expression, and then uses that renderer to parse the
	 * group name. Returns null if no renderer was found.
	 * @param key The key for which to parse the group name
	 * @return the group name from the specified key, or null if no renderer claims it
	 */
	private String getGroupName( final String key )
	{
		String group = null;
		final PersistenceRenderer renderer = getRenderer( key );
		if( renderer != null )
		{
			group = renderer.getGroupName( key );
		}
		return group;
	}
}
