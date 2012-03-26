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
package org.freezedry.persistence.readers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.writers.KeyValueWriter;

public class KeyValueReader implements PersistenceReader {

	private static final Logger LOGGER = Logger.getLogger( KeyValueReader.class );
	
	private String keyValueSeparator = KeyValueWriter.KEY_VALUE_SEPARATOR;
	private String keyElementSeparator = AbstractKeyValueBuilder.KEY_ELEMENT_SEPARATOR;
	private boolean useClassAsRootKey = false;
	
	/**
	 * @param separator The separator between the key and the value. The default value is given by the
	 * {@link KeyValueWriter#KEY_VALUE_SEPARATOR}.
	 */
	public void setKeyValueSeparator( final String separator )
	{
		this.keyValueSeparator = separator;
	}
	
	/**
	 * @return The separator between the key and the value.
	 */
	public String getKeyValueSeparator()
	{
		return keyValueSeparator;
	}
	
	/**
	 * @param separator The separator between the key and the value. The default value is given by the
	 * {@link AbstractKeyValueBuilder#KEY_ELEMENT_SEPARATOR}.
	 */
	public void setKeyElementSeparator( final String separator )
	{
		this.keyElementSeparator = separator;
	}
	
	/**
	 * @return The separator between the elements of the key.
	 */
	public String getKeyElementSeparator()
	{
		return keyElementSeparator;
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
	 * @see org.freezedry.persistence.readers.PersistenceReader#read(java.lang.Class, java.io.Reader)
	 */
	@Override
	public InfoNode read( final Class< ? > clazz, final Reader input )
	{
		// read the file into a list of key-value pairs
		final List< Pair< String, String > > keyValues = readKeyValuePairs( input, keyValueSeparator );
		
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
	 * @return the new info node
	 */
	private void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues )
	{
		// grab the persistence name from the parent node. Validate that the first elements of each 
		// key equal this the persistence name 
		final String rootKey = parentNode.getPersistName();
		validiateRootKey( keyValues, keyElementSeparator, rootKey );
		
		// strip the root key element from all the keys. For example, suppose the keys all start with
		// "Division:". And suppose further that the rootKey = "Division". The "Division:" will be
		// stripped from each key in the list. So, "Division.people.Persion[1]" would become "people.Persion[1]".
		final List< Pair< String, String > > strippedKeyValues = stripFirstKeyElement( keyValues, keyElementSeparator );

		// find the groups in the newly string list, and then create a new info node for each group
		final Map< String, List< Pair< String, String > > > groups = getGroups( strippedKeyValues, keyElementSeparator );
		for( Map.Entry< String, List< Pair< String, String > > > entry : groups.entrySet() )
		{
			System.out.println( entry.getKey() );
			for( Pair< String, String > pair : entry.getValue() )
			{
				System.out.println( pair );
			}
			System.out.println();
			
			final String groupName = entry.getKey();
			final List< Pair< String, String > > pairs = entry.getValue();
			// leaf node
			if( pairs.size() == 1 )
			{
				final String name = pairs.get( 0 ).getFirst();
				if( !groupName.equals( name ) )
				{
					// houston, we have a problem
					throw new IllegalStateException( "The group name must match the persist name for a leaf node." );
				}
				final String value = pairs.get( 0 ).getSecond();
				parentNode.addChild( InfoNode.createLeafNode( null, value, name, null ) );
			}
			else
			{
				final InfoNode node = InfoNode.createCompoundNode( null, groupName, null );
				parentNode.addChild( node );
				buildInfoNode( node, entry.getValue() );
			}
		}
	}
	
	/*
	 * Reads the input stream into a {@link List} of key-value {@link Pair}s, where the first element in the
	 * {@link Pair} is the key, and the second element is the value.
	 * @param input The input stream
	 * @param keyValueSeparator The separator between the key and the value.
	 * @return a {@link List} of key-value {@link Pair}s, where the first element in the {@link Pair} is the 
	 * key, and the second element is the value.
	 */
	private static List< Pair< String, String > > readKeyValuePairs( final Reader input, final String keyValueSeparator )
	{
		// read the stream into a string buffer, which we'll process into key-value pairs
		final StringBuffer buffer = new StringBuffer();
		char[] charBuffer = null;
		try
		{
			int charsRead;
			do
			{
				charBuffer = new char[ 1024 ];
				charsRead = input.read( charBuffer );
				buffer.append( charBuffer );
			}
			while( charsRead != -1 );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to read from input stream." + Constants.NEW_LINE );
			message.append( "  Characters read before failure:" + Constants.NEW_LINE );
			message.append( buffer.toString() + Constants.NEW_LINE );
			message.append( "  Characters in char buffer before failure:" + Constants.NEW_LINE );
			message.append( charBuffer );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		
		// create a list of lines
		final List< String > lines = Arrays.asList( buffer.toString().split( "\\n" ) );
		
		// separate the lines into keys and values
		final List< Pair< String, String > > pairs = new ArrayList<>();
		for( String line : lines )
		{
			// if the line is empty, or full of only spaces, then we disregard it.
			if( !line.trim().isEmpty() )
			{
				final String[] keyValue = line.split( Pattern.quote( keyValueSeparator ) );
				pairs.add( new Pair< String, String >( keyValue[ 0 ].trim(), keyValue[ 1 ].trim() ) );
			}
		}
		
		return pairs;
	}
	
	/*
	 * Returns the root key. If the {@code useClassAsRootKey} default was set via the constructor or the {@link #setRootKey(String)}
	 * method, then it returns that root key. Otherwise, it pulls the first key element from each key, ensures that
	 * they are all the same, and then returns that root key.
	 * @param keyValues The list of key-value pairs
	 * @return the root key.
	 */
	private String getRootKey( final List< Pair< String, String > > keyValues, final Class< ? > clazz )
	{
		String key;
		if( useClassAsRootKey )
		{
			key = clazz.getSimpleName();
		}
		else
		{
			key = validiateRootKey( keyValues, keyElementSeparator, clazz.getSimpleName() );
		}
		return key;
	}

	/*
	 * Pulls the first key element from each key, ensures that they are all the same, and then returns that root key.
	 * @param keyValues The list of key-value pairs.
	 * @param keyElementSeparator The separator between the key elements. The default value is found in the
	 * {@link AbstractKeyValueBuilder#KEY_ELEMENT_SEPARATOR} and is usually "{@code :}".
	 * @return the root key.
	 */
	private String validiateRootKey( final List< Pair< String, String > > keyValues, final String keyElementSeparator )
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
	private String validiateRootKey( final List< Pair< String, String > > keyValues, final String keyElementSeparator, final String desiredName )
	{
		final Set< String > keySet = new LinkedHashSet<>();
		for( Pair< String, String > pair : keyValues )
		{
			keySet.add( getFirstKeyElement( pair.getFirst(), keyElementSeparator ) );
		}
		
		// the first key must be unique
		if( keySet.size() != 1 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The first element of all the keys must be the same. This is the root key." + Constants.NEW_LINE );
			message.append( "  Set elements:" + Constants.NEW_LINE );
			for( String key : keySet )
			{
				message.append( "    " + key + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// grab the key from the only element in the set
		final String rootKey = keySet.iterator().next();
		if( desiredName != null && !desiredName.equals( rootKey ) )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The root key is not the same as the persistence name" + Constants.NEW_LINE );
			message.append( "  Root Key: " + rootKey + Constants.NEW_LINE );
			message.append( "  Persistence Name: " + desiredName );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		return rootKey;
	}
	
	private static List< Pair< String, String > > stripFirstKeyElement( final List< Pair< String, String > > keyValues, final String keyElementSeparator )
	{
		final List< Pair< String, String > > strippedKeyValues = new ArrayList<>();
		for( Pair< String, String > pair : keyValues )
		{
			// grab the elements of the key
			final String[] elements = pair.getFirst().split( Pattern.quote( keyElementSeparator ) );
			
			// create a key that has the first key element stripped off
			final StringBuffer strippedKey = new StringBuffer();
			for( int i = 1; i < elements.length; ++i )
			{
				strippedKey.append( elements[ i ] );
				if( i < elements.length-1 )
				{
					strippedKey.append( keyElementSeparator );
				}
			}
			
			// add the new key and the old value to the list of stripped keys
			strippedKeyValues.add( new Pair< String, String >( strippedKey.toString(), pair.getSecond() ) );
		}
		
		return strippedKeyValues;
	}
	
	/**
	 * Returns the first key element for the specified key and separator
	 * @param key The key from which to pull the first element
	 * @param separator The key element separator
	 * @return the first key element for the specified key and separator
	 */
	private static String getFirstKeyElement( final String key, final String separator )
	{
		return key.split( Pattern.quote( separator ) )[ 0 ];
	}
	
	/**
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
	private static Map< String, List< Pair< String, String > > > getGroups( final List< Pair< String, String > > keyValues, final String separator )
	{
		final Map< String, List< Pair< String, String > > > groups = new LinkedHashMap<>();
		for( Pair< String, String > pair : keyValues )
		{
			// get the first key element.
			final String group = getFirstKeyElement( pair.getFirst(), separator );
			
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
	
	// TODO in this method, find the correct renderer, and then get the group name from that renderer.
	private static String getGroupName( final String key )
	{
		Pattern pattern = Pattern.compile( "^[\\w]*" );
		Matcher matcher = pattern.matcher( key );
		String group = key;
		if( matcher.find() )
		{
			group = key.substring( 0, matcher.end() );
		}
		return group;
	}
	
	/**
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main( String[] args ) throws FileNotFoundException
	{
		DOMConfigurator.configure( "log4j.xml" );
		
		final KeyValueReader reader = new KeyValueReader();
		reader.setKeyElementSeparator( "." );
//		reader.setRemoveEmptyTextNodes( false );
		final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.txt" ) );
		final Reader input = new InputStreamReader( inputStream );
		final InfoNode infoNode = reader.read( Division.class, input );
		System.out.println( infoNode.simpleTreeToString() );
		
		final PersistenceEngine engine = new PersistenceEngine();
		final Object reperson = engine.parseSemanticModel( Division.class, infoNode );
		System.out.println( reperson );
	}
}
