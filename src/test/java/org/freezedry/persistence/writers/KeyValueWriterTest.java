package org.freezedry.persistence.writers;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.FlatteningCollectionRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 8:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyValueWriterTest {
	@Before
	public void setup() throws ParseException
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
		System.out.println( rootNode.simpleTreeToString() );

		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.txt" ) ) )
		{
			final KeyValueWriter writer = new KeyValueWriter();
//			writer.setShowFullKey( true );
			final KeyValueBuilder builder = writer.getBuilder();
			builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
			writer.setKeyElementSeparator( "." );
			writer.write( rootNode, printWriter );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
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
	public void testSetKeyValueSeparator() throws Exception
	{

	}

	@Test
	public void testGetKeyValueSeparator() throws Exception
	{

	}

	@Test
	public void testSetBuilder() throws Exception
	{

	}

	@Test
	public void testGetBuilder() throws Exception
	{

	}

	@Test
	public void testWrite() throws Exception
	{

	}
}
