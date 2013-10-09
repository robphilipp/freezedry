/*
 * Copyright 2013 Robert Philipp, InvestLab Technology LLC
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

import org.apache.log4j.Logger;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [Description]
 *
 * @author rphilipp
 *         10/7/13, 11:14 AM
 */
public class KeyValueMapWriter {

	private static final Logger LOGGER = Logger.getLogger( KeyValueMapWriter.class );

	private final KeyValueFlattener keyValueFlattener;

	/**
	 * Constructs a basic key-value writer that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link org.freezedry.persistence.tree.InfoNode} and
	 * the {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create key-value pairs for
	 * {@link org.freezedry.persistence.tree.InfoNode}s that represent an array.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @see org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public KeyValueMapWriter( final Map< Class< ? >, PersistenceRenderer > renderers,
							  final PersistenceRenderer arrayRenderer,
							  final String keySeparator )
	{
		keyValueFlattener = new KeyValueFlattener( renderers, arrayRenderer, keySeparator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and specified separator.
	 * @param keySeparator The separator between the flattened elements of the key
	 */
	public KeyValueMapWriter( final String keySeparator )
	{
		keyValueFlattener = new KeyValueFlattener( keySeparator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and separator.
	 */
	public KeyValueMapWriter()
	{
		keyValueFlattener = new KeyValueFlattener();
	}

	/**
	 * Constructs a key-value writer using the specified key-value list builder
	 * @param builder The {@link org.freezedry.persistence.keyvalue.KeyValueBuilder} used to flatten the semantic model
	 */
	public KeyValueMapWriter( final KeyValueBuilder builder )
	{
		keyValueFlattener = new KeyValueFlattener( builder );
	}

	/**
	 * @return The flattener used to flatten the object into a list of key-value pairs
	 */
	public KeyValueFlattener getKeyValueFlattener()
	{
		return keyValueFlattener;
	}

	/**
	 * The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 * @param separator The separator
	 */
	public void setKeyElementSeparator( final String separator )
	{
		keyValueFlattener.setKeyElementSeparator( separator );
	}

	/**
	 * @return The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 */
	public String getKeyElementSeparator()
	{
		return keyValueFlattener.getKeyElementSeparator();
	}

	/**
	 * @return the {@link KeyValueBuilder} responsible for creating the key-value pairs
	 * from the semantic model, and that is responsible for parsing the key-value pairs into
	 * a semantic model.
	 */
	public KeyValueBuilder getBuilder()
	{
		return keyValueFlattener.getBuilder();
	}

	/**
	 * Creates a {@link Map} that contains the flattened object. The key into the map is the flattened field name, and
	 * the value is the string representation of that value.
	 * @param rootNode The root {@link InfoNode} of the semantic model
	 * @return a {@link Map} that contains the flattened object. The key into the map is the flattened field name, and
	 * the value is the string representation of that value.
	 */
	public Map< String, Object > createMap( final InfoNode rootNode )
	{
		final List< Pair< String, Object > > keyValuePairs = keyValueFlattener.buildKeyValuePairs( rootNode );

		final Map< String, Object > flattenedObject = new LinkedHashMap<>();
		for( final Pair< String, Object > pair : keyValuePairs )
		{
			flattenedObject.put( pair.getFirst(), pair.getSecond() );
		}

		if( LOGGER.isTraceEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				message.append( pair.getFirst() + " = " + pair.getSecond().toString() + Constants.NEW_LINE );
			}
			LOGGER.trace( message.toString() );
		}
		return flattenedObject;
	}

	/**
	 * For testing
	 * @param args
	 * @throws javax.xml.parsers.ParserConfigurationException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws java.text.ParseException
	 */
	public static void main( String[] args ) throws ParserConfigurationException, ReflectiveOperationException, IOException, ParseException
	{
//		DOMConfigurator.configure( "log4j.xml" );
//
//		final Division division = new Division();
//		final Person johnny = new Person( "Hernandez", "Johnny", 13 );
//		johnny.addFriend( "Polly", "bird" );
//		johnny.addFriend( "Sparky", "dog" );
//		for( int i = 0; i < 10; ++i )
//		{
//			johnny.addMood( Math.sin( Math.PI / 4 * i ) );
//		}
//		Map< String, String > group = new LinkedHashMap<>();
//		group.put( "one", "ONE" );
//		group.put( "two", "TWO" );
//		group.put( "three", "THREE" );
//		johnny.addGroup( "numbers", group );
//
//		group = new LinkedHashMap<>();
//		group.put( "a", "AY" );
//		group.put( "b", "BEE" );
//		johnny.addGroup( "letters", group );
//
//		johnny.setBirthdate( DateUtils.createDateFromString( "1963-04-22", "yyyy-MM-dd" ) );
//
//		division.addPerson( johnny );
//
//		division.addPerson( new Person( "Prosky", "Julie", 15 ) );
//		division.addPerson( new Person( "Jones", "Janet", 13 ) );
//		division.addPerson( new Person( "Ghad", "Booda", 17 ) );
//
//		division.addMonth( "January", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//		division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 2, 3, 28 ) ) );
//		division.addMonth( "March", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//		division.addMonth( "April", new HashSet<>( Arrays.asList( 1, 2, 3, 30 ) ) );
//
//		division.setCarNames( new String[] { "civic", "tsx", "accord" } );
//
//		int[][] arrayMatrix = { { 11, 12, 13 }, { 21, 22, 23 }, { 31, 32, 33 } };
//		division.setArrayMatrix( arrayMatrix );
//
//		List< List< Integer > > collectionMatrix = Arrays.asList(
//				Arrays.asList( 11, 12, 13 ),
//				Arrays.asList( 21, 22, 23 ),
//				Arrays.asList( 31, 32, 33 ) );
//		division.setCollectionMatrix( collectionMatrix );
//
//		List< Map< String, String > > listOfMaps = new ArrayList<>();
//		Map< String, String > map = new LinkedHashMap<>();
//		map.put( "color", "green" );
//		map.put( "size", "large" );
//		map.put( "condition", "used" );
//		listOfMaps.add( map );
//
//		map = new LinkedHashMap<>();
//		map.put( "color", "red" );
//		map.put( "size", "small" );
//		map.put( "condition", "new" );
//		listOfMaps.add( map );
//
//		map = new LinkedHashMap<>();
//		map.put( "color", "blue" );
//		map.put( "size", "medium" );
//		map.put( "condition", "good" );
//		listOfMaps.add( map );
//		division.setListOfMaps( listOfMaps );
//
//		Map< String, Person > personMap = new LinkedHashMap<>();
//		personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
//		personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
//		personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
//		division.setPersonMap( personMap );
//
//		final PersistenceEngine engine = new PersistenceEngine();
//		final InfoNode rootNode = engine.createSemanticModel( division );
//		System.out.println( rootNode.simpleTreeToString() );
//
//		final KeyValueMapWriter writer = new KeyValueMapWriter();
////			writer.setShowFullKey( true );
//		final KeyValueBuilder builder = writer.getBuilder();
//		builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
//		writer.setKeyElementSeparator( "." );
//		Map< String, Object > flattenedObject = writer.createMap( rootNode );
//
//		for( Map.Entry< String, Object > entry : flattenedObject.entrySet() )
//		{
//			System.out.println( entry.getKey() + " = " + entry.getValue().toString() );
//		}
	}
}
