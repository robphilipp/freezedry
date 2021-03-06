package org.freezedry.persistence.writers;

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.FlatteningCollectionRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 8:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyValueMapWriterTest {

	private static final Logger LOGGER = LoggerFactory.getLogger( KeyValueMapWriterTest.class );

	@Before
	public void setUp() throws Exception
	{
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

		int[][] arrayMatrix = { { 11, 12, 13 }, { 21, 22, 23 }, { 31, 32, 33 } };
		division.setArrayMatrix( arrayMatrix );

		List<List< Integer >> collectionMatrix = Arrays.asList(
				Arrays.asList( 11, 12, 13 ),
				Arrays.asList( 21, 22, 23 ),
				Arrays.asList( 31, 32, 33 ) );
		division.setCollectionMatrix( collectionMatrix );

		List< Map< String, String > > listOfMaps = new ArrayList<>();
		Map< String, String > map = new LinkedHashMap<>();
		map.put( "color", "green" );
		map.put( "size", "large" );
		map.put( "condition", "used" );
		listOfMaps.add( map );

		map = new LinkedHashMap<>();
		map.put( "color", "red" );
		map.put( "size", "small" );
		map.put( "condition", "new" );
		listOfMaps.add( map );

		map = new LinkedHashMap<>();
		map.put( "color", "blue" );
		map.put( "size", "medium" );
		map.put( "condition", "good" );
		listOfMaps.add( map );
		division.setListOfMaps( listOfMaps );

		Map< String, Person > personMap = new LinkedHashMap<>();
		personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
		personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
		personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
		division.setPersonMap( personMap );

		final PersistenceEngine engine = new PersistenceEngine();
		final InfoNode rootNode = engine.createSemanticModel( division );
//		LOGGER.debug( rootNode.simpleTreeToString() );

		final KeyValueMapWriter writer = new KeyValueMapWriter();
		final KeyValueBuilder builder = writer.getBuilder();
		builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
		writer.setKeyElementSeparator( "." );
		Map< String, Object > flattenedObject = writer.createMap( rootNode );

//		for( Map.Entry< String, Object > entry : flattenedObject.entrySet() )
//		{
//			LOGGER.debug( entry.getKey() + " = " + entry.getValue().toString() );
//		}
	}

	@Test
	public void testNullValues() throws Exception
	{
		final Division division = new Division();
		final PersistenceEngine engine = new PersistenceEngine().withPersistNullValues();
		final InfoNode rootNode = engine.createSemanticModel( division );

		final KeyValueMapWriter writer = new KeyValueMapWriter();
		final KeyValueBuilder builder = writer.getBuilder();
		builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
		writer.setKeyElementSeparator( "." );
		Map< String, Object > flattenedObject = writer.createMap( rootNode );
	}

	@Test
	public void testGetKeyValueFlattener() throws Exception
	{
		assertNotNull( new KeyValueMapWriter().getKeyValueFlattener() );
	}

	@Test
	public void testSetKeyElementSeparator() throws Exception
	{

	}

	@Test
	public void testGetKeyElementSeparator() throws Exception
	{

	}

	@Test
	public void testGetBuilder() throws Exception
	{

	}

	@Test
	public void testCreateMap() throws Exception
	{

	}
}
