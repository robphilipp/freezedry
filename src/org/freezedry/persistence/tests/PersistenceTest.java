package org.freezedry.persistence.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.readers.JsonReader;
import org.freezedry.persistence.readers.XmlReader;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.writers.JsonWriter;
import org.freezedry.persistence.writers.XmlWriter;
import org.junit.Test;

public class PersistenceTest {

	private static final String PATH = "src/org/freezedry/persistence/tests/";
	
	private final PersistenceEngine engine;
	
	/**
	 * 
	 */
	public PersistenceTest()
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.ERROR );
		
		// create the persistence engine that is used to create the semantic model from an object
		// and used to create an object from the semantic model.
		this.engine = new PersistenceEngine();
	}
	
	/**
	 * 
	 * @return
	 * @throws ParseException
	 */
	private static Division createDivision() throws ParseException
	{
		// create the object to persist
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
		
		List< List< Integer > > collectionMatrix = Arrays.asList( 
				Arrays.asList( 11, 12, 13 ), 
				Arrays.asList( 21, 22, 23 ), 
				Arrays.asList( 31, 32, 33 ) );
		division.setCollectionMatrix( collectionMatrix );
		
		Map< String, Person > personMap = new LinkedHashMap<>();
		personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
		personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
		personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
		division.setPersonMap( personMap );
		
		return division;
	}
	
	private InfoNode createInfoNode( final Object object )
	{
		// create the semantic model
		return engine.createSemanticModel( object );
	}
	
	private Object createObject( final InfoNode infoNode, final Class< ? > clazz )
	{
		return engine.parseSemanticModel( clazz, infoNode );
	}
	
	/**
	 * 
	 * @param node
	 * @param fileName
	 * @throws IOException
	 */
	private void writeXml( final InfoNode node, final String fileName ) throws IOException
	{
		// write out XML
		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( PATH + fileName ) ) )
		{
			final XmlWriter writer = new XmlWriter();
			writer.setDisplayTypeInfo( false );
			writer.write( node, printWriter );
		}
		catch( IOException e )
		{
			throw new IOException( e );
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	private InfoNode readXml( final String fileName, final Class< ? > clazz ) throws FileNotFoundException
	{
		final XmlReader reader = new XmlReader();
		final InputStream inputStream = new BufferedInputStream( new FileInputStream( PATH + fileName ) );
		final Reader input = new InputStreamReader( inputStream );
		final InfoNode infoNode = reader.read( clazz, input );
		return infoNode;
	}
	
	/**
	 * 
	 * @param node
	 * @param fileName
	 * @throws IOException
	 */
	private void writeJson( final InfoNode node, final String fileName ) throws IOException
	{
		// write out XML
		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( PATH + fileName ) ) )
		{
			final JsonWriter writer = new JsonWriter();
			writer.write( node, printWriter );
		}
		catch( IOException e )
		{
			throw new IOException( e );
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	private InfoNode readJson( final String fileName, final Class< ? > clazz ) throws FileNotFoundException
	{
		final InputStream inputStream = new BufferedInputStream( new FileInputStream( PATH + fileName ) );
		final Reader input = new InputStreamReader( inputStream );
		final JsonReader reader = new JsonReader();
		final InfoNode infoNode = reader.read( clazz, input );
		return infoNode;
	}
	
	/**
	 * 
	 * @param object
	 * @param reObject
	 */
	private void testTrue( final Object object, final Object reObject )
	{
		if( object.getClass().isArray() )
		{
			final int length = Array.getLength( object );
			assertTrue( Array.getLength( reObject ) == length );
	
			for( int i = 0; i < length; ++i )
			{
				assertTrue( Array.get( object, i ).equals( Array.get( reObject, i ) ) );
			}
		}
		else if( object.getClass().isPrimitive() )
		{
			assertTrue( object == reObject );
		}
		else
		{
			assertTrue( object.toString().equals( reObject.toString() ) );
		}
	}

	/**
	 * 
	 * @param object
	 * @param fileName
	 */
	private void testXml( final Object object, final String fileName )
	{
		try
		{
			// create the semantic model for the division
			final InfoNode rootNode = createInfoNode( object );
	
			// write out XML
			writeXml( rootNode, fileName );
			
			// read in XML
			final InfoNode infoNode = readXml( fileName, object.getClass() );
			
			// reconstruct the object from the semantic model
			final Object reObject = createObject( infoNode, object.getClass() );
			
			// these should be the same
			testTrue( object, reObject );
		}
		catch( Exception e )
		{
			fail( e.getMessage() );
		}
	}
	
	/**
	 * 
	 * @param object
	 * @param fileName
	 */
	private void testJson( final Object object, final String fileName )
	{
		try
		{
			// create the semantic model for the division
			final InfoNode rootNode = createInfoNode( object );
	
			// write out JSON
			writeJson( rootNode, fileName );
			
			// read in JSON
			final InfoNode infoNode = readJson( fileName, object.getClass() );
			
			// reconstruct the object from the semantic model
			final Object reObject = createObject( infoNode, object.getClass() );
			
			// these should be the same
			testTrue( object, reObject );
		}
		catch( Exception e )
		{
			fail( e.getMessage() );
		}
	}
	
	@Test
	public void testNodeCopy()
	{
		try
		{
			// create the semantic model for the division
			final InfoNode rootNode = createInfoNode( createDivision() );
			assertTrue( rootNode.treeToString().equals( rootNode.getCopy().treeToString() ) );
		}
		catch( ParseException e )
		{
			fail( e.getMessage() );
		}
	}

	/**
	 * 
	 */
	@Test
	public void testDivisionXml()
	{
		try
		{
			testXml( createDivision(), "division_test.xml" );
		}
		catch( Exception e )
		{
			fail( e.getMessage() );
		}
	}

	/**
	 * 
	 */
	@Test
	public void testDivisionJson()
	{
		try
		{
			testJson( createDivision(), "division_test.json" );
		}
		catch( Exception e )
		{
			fail( e.getMessage() );
		}
	}

	@Test
	public void testIntArraysXml()
	{
		testXml( new int[] { 3, 1, 4, 1, 5, 9, 2, 6 }, "int_array.xml" );
	}

	@Test
	public void testIntArraysJson()
	{
		testJson( new int[] { 3, 1, 4, 1, 5, 9, 2, 6 }, "int_array.json" );
	}

	@Test
	public void testDoubleArraysXml()
	{
		testXml( new double[] { 3, 1, 4, 1, 5, 9, 2, 6 }, "double_array.xml" );
		testXml( new double[] { 3.14, 1.41, 4.15, 1.59, 5.92, 9.26, 2.6, 6. }, "double_array.xml" );
	}

	@Test
	public void testDoubleArraysJson()
	{
		testJson( new double[] { 3, 1, 4, 1, 5, 9, 2, 6 }, "double_array.json" );
		testJson( new double[] { 3.14, 1.41, 4.15, 1.59, 5.92, 9.26, 2.6, 6. }, "double_array.json" );
	}
	@Test
	public void testStringArraysXml()
	{
		testXml( new String[] { "three", "point", "one", "four", "one", "five", "nine" }, "string_array.xml" );
	}

	@Test
	public void testStringArraysJson()
	{
		testJson( new String[] { "three", "point", "one", "four", "one", "five", "nine" }, "string_array.json" );
	}
}
