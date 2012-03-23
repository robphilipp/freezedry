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
package org.freezedry.persistence.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder;
import org.freezedry.persistence.keyvalue.BasicKeyValueBuilder;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.DateUtils;

/**
 * Writes the semantic model as a list of key-value pairs to the specified output stream.
 * The writer uses a {@link KeyValueBuilder} that flattens the semantic model and returns 
 * a list of key-value pairs. The {@link KeyValueBuilder} can be specified, or a default one is created
 * based on the supplied specifications for the {@link KeyValueBuilder}. 
 * 
 * @author Robert Philipp
 */
public class KeyValueWriter implements PersistenceWriter {

	private static final Logger LOGGER = Logger.getLogger( KeyValueWriter.class );
	
	private KeyValueBuilder builder;
	
	/**
	 * Constructs a basic key-value writer that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link InfoNode} and
	 * the {@link PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link PersistenceRenderer} used to create key-value pairs for
	 * {@link InfoNode}s that represent an array.
	 * @param separator The separator between the flattened elements of the key
	 * @see AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public KeyValueWriter( final Map< Class< ? >, PersistenceRenderer > renderers, 
						   final PersistenceRenderer arrayRenderer,
						   final String separator )
	{
		builder = new BasicKeyValueBuilder( renderers, arrayRenderer, separator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and specified separator.
	 * @param separator The separator between the flattened elements of the key
	 */
	public KeyValueWriter( final String separator )
	{
		builder = new BasicKeyValueBuilder( separator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and separator.
	 */
	public KeyValueWriter()
	{
		builder = new BasicKeyValueBuilder();
	}
	
	/**
	 * Constructs a key-value writer using the specified key-value list builder
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model
	 */
	public KeyValueWriter( final KeyValueBuilder builder )
	{
		this.builder = builder;
	}
	
	/**
	 * The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 * @param separator The separator
	 */
	public void setSeparator( final String separator )
	{
		builder.setSeparator( separator );
	}
	
	/**
	 * @return The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 */
	public String getSeparator()
	{
		return builder.getSeparator();
	}
	
	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link List} would have a key of the form {@code names[i].String}. I recommend
	 * against setting this to true.
	 * @param isShowFullKey true means that the full key will be persisted; false is default
	 */
	public void setShowFullKey( final boolean isShowFullKey )
	{
		builder.setShowFullKey( isShowFullKey );
	}
	
	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link List} would have a key of the form {@code names[i].String}
	 * @return true means that the full key will be persisted; false is default
	 */
	public boolean isShowFullKey()
	{
		return builder.isShowFullKey();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.PersistenceWriter#write(org.freezedry.persistence.tree.InfoNode, java.io.Writer)
	 */
	@Override
	public void write( final InfoNode rootNode, final Writer output )
	{
		final List< Pair< String, Object > > keyValuePairs = builder.buildKeyValuePairs( rootNode );
		try
		{
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				final String str = pair.getFirst() + " = " + pair.getSecond().toString() + Constants.NEW_LINE;
				output.write( str );
			}
		}
		catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
		
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				message.append( pair.getFirst() + " = " + pair.getSecond().toString() + Constants.NEW_LINE );
			}
			LOGGER.info( message.toString() );
		}
	}
	
	/**
	 * For testing
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static void main( String[] args ) throws ParserConfigurationException, ReflectiveOperationException, IOException, ParseException
	{
		DOMConfigurator.configure( "log4j.xml" );

		final Division division = new Division();
		final Person johnny = new Person( "Hernandez", "Johnny", 13 );
		johnny.addFriend( "Polly", "bird" );
		johnny.addFriend( "Sparky", "dog" );
		for( int i = 0; i < 10; ++i )
		{
			johnny.addMood( Math.sin( Math.PI / 4 * i ) );
		}
		Map< String, String > group = new LinkedHashMap<>();
		group.put( "one", "ONE" );
		group.put( "two", "TWO" );
		group.put( "three", "THREE" );
		johnny.addGroup( "numbers", group );

		group = new LinkedHashMap<>();
		group.put( "a", "AY" );
		group.put( "b", "BEE" );
		johnny.addGroup( "letters", group );
		
		johnny.setBirthdate( DateUtils.createDateFromString( "1963-04-22", "yyyy-MM-dd" ) );
		
		division.addPerson( johnny );

		division.addPerson( new Person( "Prosky", "Julie", 15 ) );
		division.addPerson( new Person( "Jones", "Janet", 13 ) );
		division.addPerson( new Person( "Ghad", "Booda", 17 ) );
		
		division.addMonth( "January", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
		division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 2, 3, 28 ) ) );
		division.addMonth( "March", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
		division.addMonth( "April", new HashSet<>( Arrays.asList( 1, 2, 3, 30 ) ) );
		
		division.setCarNames( new String[] { "civic", "tsx", "accord" } );
		
		
		final PersistenceEngine engine = new PersistenceEngine();
		final InfoNode rootNode = engine.createSemanticModel( division );
		System.out.println( rootNode.treeToString() );

		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.txt" ) ) )
		{
			final KeyValueWriter writer = new KeyValueWriter();
//			writer.setShowFullKey( true );
			writer.setSeparator( "." );
			writer.write( rootNode, printWriter );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
