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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder;
import org.freezedry.persistence.keyvalue.BasicKeyValueBuilder;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.FlatteningCollectionRenderer;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.writers.KeyValueWriter;

public class KeyValueReader implements PersistenceReader {

	private static final Logger LOGGER = Logger.getLogger( KeyValueReader.class );
	private static final String KEY_VALUE_SEPARATOR = KeyValueWriter.KEY_VALUE_SEPARATOR;
	
	private KeyValueBuilder builder;
	private String keyValueSeparator = KEY_VALUE_SEPARATOR;

	/**
	 * Constructs a key-value reader with that uses the specified key-value separator and key-element
	 * separator when parsing the key values.
	 * @param keyValueSeparator
	 * @param keyElementSeparator
	 */
	public KeyValueReader( final Map< Class< ? >, PersistenceRenderer > renderers, 
			   			   final PersistenceRenderer arrayRenderer,
			   			   final String keyValueSeparator, 
			   			   final String keyElementSeparator )
	{
		this.keyValueSeparator = keyValueSeparator;
		builder = new BasicKeyValueBuilder( renderers, arrayRenderer, keyElementSeparator );
	}
	
	/**
	 * Constructs a basic key-value writer that uses the default renderers and specified separator.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @param keyValueSeparator The separator between the key and the value
	 */
	public KeyValueReader( final String keySeparator, final String keyValueSeparator )
	{
		builder = new BasicKeyValueBuilder( keySeparator );
		this.keyValueSeparator = keyValueSeparator;
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and separator.
	 */
	public KeyValueReader()
	{
		builder = new BasicKeyValueBuilder();
	}
	
	/**
	 * Constructs a key-value writer using the specified key-value list builder
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model
	 */
	public KeyValueReader( final KeyValueBuilder builder )
	{
		this.builder = builder;
	}
	
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
		builder.setSeparator( separator );
	}
	
	/**
	 * @return The separator between the elements of the key.
	 */
	public String getKeyElementSeparator()
	{
		return builder.getSeparator();
	}
	
	/**
	 * @return the {@link KeyValueBuilder} responsible for creating the key-value pairs 
	 * from the semantic model, and that is responsible for parsing the key-value pairs into 
	 * a semantic model.
	 */
	public KeyValueBuilder getBuilder()
	{
		return builder;
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
		
		return builder.buildInfoNode( clazz, keyValues );
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
		final KeyValueBuilder builder = reader.getBuilder();
		builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
		final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.txt" ) );
		final Reader input = new InputStreamReader( inputStream );
		final InfoNode infoNode = reader.read( Division.class, input );
		System.out.println( infoNode.simpleTreeToString() );
		
		final PersistenceEngine engine = new PersistenceEngine();
		final Object reperson = engine.parseSemanticModel( Division.class, infoNode );
		System.out.println( reperson );
	}
}
