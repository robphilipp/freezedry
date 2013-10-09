package org.freezedry.persistence;

import junit.framework.Assert;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.builders.NodeBuilder;
import org.freezedry.persistence.builders.StringNodeBuilder;
import org.freezedry.persistence.readers.JsonReader;
import org.freezedry.persistence.readers.XmlReader;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.writers.JsonWriter;
import org.freezedry.persistence.writers.XmlWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 7:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistenceEngineTest {

	@Before
	public void setUp() throws Exception
	{
		try
		{
			DOMConfigurator.configure( "log4j.xml" );

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

			// create the persistence engine that is used to create the semantic model from an object
			// and used to create an object from the semantic model.
			final PersistenceEngine engine = new PersistenceEngine();
			// two ways to override the output behavior programmatically
//			// override the default date setting and replace the date node builder with the new one
//			final DateNodeBuilder dateNodeBuilder = new DateNodeBuilder( engine, "yyyy-MM-dd" );
//			engine.addNodeBuilder( Calendar.class, dateNodeBuilder );
			// -- OR --
//			((DateNodeBuilder)engine.getNodeBuilder( Calendar.class )).setOutputDateFormat( "yyyy-MM-dd" );

			// create the semantic model
			final InfoNode rootNode = engine.createSemanticModel( division );
			System.out.println( rootNode.simpleTreeToString() );
//			System.out.println( "\n\nCopied Tree\n" );
//			System.out.println( rootNode.getCopy().simpleTreeToString() );

			// write out XML
			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.xml" ) ) )
			{
				final XmlWriter writer = new XmlWriter();
				writer.setDisplayTypeInfo( false );
				writer.write( rootNode, printWriter );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person2.xml" ) ) )
			{
				final XmlWriter writer = new XmlWriter();
				writer.setDisplayTypeInfo( false );
				writer.write( rootNode.getCopy(), printWriter );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}


			// write out JSON
			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.json" ) ) )
			{
				final JsonWriter writer = new JsonWriter();
				writer.write( rootNode, printWriter );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}

			// read in XML
			final XmlReader reader = new XmlReader();
	//		reader.setRemoveEmptyTextNodes( false );
			final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.xml" ) );
			final Reader input = new InputStreamReader( inputStream );
			final InfoNode infoNode = reader.read( Division.class, input );
			System.out.println( infoNode.simpleTreeToString() );

			final Object redivision = engine.parseSemanticModel( Division.class, infoNode );
			System.out.println( redivision );

			// read in JSON
			final InputStream jsonInputStream = new BufferedInputStream( new FileInputStream( "person.json" ) );
			final Reader jsonInput = new InputStreamReader( jsonInputStream );
			final JsonReader jsonReader = new JsonReader();
			final InfoNode jsonInfoNode = jsonReader.read( Division.class, jsonInput );
			System.out.println( jsonInfoNode.simpleTreeToString() );

			final Object reperson = engine.parseSemanticModel( Division.class, jsonInfoNode );
			System.out.println( reperson );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	@Test
	public void testSetPersistClassConstants() throws Exception
	{

	}

	@Test
	public void testAddNodeBuilder() throws Exception
	{
		final PersistenceEngine engine = new PersistenceEngine();
		engine.removeNodeBuilder( String.class );
		Assert.assertFalse( engine.containsNodeBuilder( String.class ) );

		engine.addNodeBuilder( String.class, new StringNodeBuilder() );
		Assert.assertTrue( engine.containsNodeBuilder( String.class ) );
	}

	@Test
	public void testContainsNodeBuilder() throws Exception
	{

	}

	@Test
	public void testContainsAnnotatedNodeBuilder() throws Exception
	{

	}

	@Test
	public void testGetNodeBuilder() throws Exception
	{

	}

	@Test
	public void testIsForbiddenRootObject() throws Exception
	{

	}

	@Test
	public void testIsAllowedRootObject() throws Exception
	{

	}

	@Test
	public void testRemoveNodeBuilder() throws Exception
	{

	}

	@Test
	public void testSetGeneralArrayNodeBuilder() throws Exception
	{

	}

	@Test
	public void testCreateSemanticModel() throws Exception
	{

	}

	@Test
	public void testCreateNode() throws Exception
	{

	}

	@Test
	public void testParseSemanticModel() throws Exception
	{

	}

	@Test
	public void testCreateObject() throws Exception
	{

	}
}
