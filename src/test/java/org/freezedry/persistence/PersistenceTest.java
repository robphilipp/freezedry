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
package org.freezedry.persistence;

import org.freezedry.persistence.readers.JsonReader;
import org.freezedry.persistence.readers.XmlReader;
import org.freezedry.persistence.tests.BadPerson;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.MapMagic;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.writers.JsonWriter;
import org.freezedry.persistence.writers.PersistenceWriter;
import org.freezedry.persistence.writers.XmlWriter;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for FreezeDry
 * 
 * @author Robert Philipp
 */
public class PersistenceTest {

	private static final String PATH = "src/test/output/";
	
	private final PersistenceEngine engine;
	
	/**
	 * 
	 */
	public PersistenceTest()
	{
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
	
	private static BadPerson[] createBadPeople()
	{
		final BadPerson[] people = new BadPerson[ 3 ];
		BadPerson person = new BadPerson( "Evil", "Bob", 33 );
		person.addEvilDoing( "Frightened old lady." );
		person.addEvilDoing( "Stepped on innocent ant." );
		people[ 0 ] = person;
		
		person = new BadPerson( "Krugger", "Fred", 55 );
		person.addEvilDoing( "Invaded peoples' dreams" );
		person.addFriend( "Bob Evil", "colleague" );
		people[ 1 ] = person;
		
		person = new BadPerson( "Dropper", "Eve", 23 );
		person.addEvilDoing( "Listens to peoples conversations" );
		people[ 2 ] = person;
		return people;
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
	private void writeXml( final InfoNode node, final String fileName, final boolean isDisplayTypeInfo ) throws IOException
	{
		final XmlWriter writer = new XmlWriter();
		writer.setDisplayTypeInfo( isDisplayTypeInfo );
		writeFile( node, fileName, writer );
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
		writeFile( node, fileName, new JsonWriter() );
	}

	private void writeFile( final InfoNode node, final String fileName, final PersistenceWriter writer ) throws IOException
	{
		// write out JSON
		final File file = new File( PATH );
		if( !file.exists() )
		{
			final boolean isCreated = file.mkdirs();
			if( !isCreated )
			{
				final StringBuilder error = new StringBuilder();
				error.append( "Test output directory doesn't exist, and unable to create the directory: " ).append( Constants.NEW_LINE )
						.append( "  Directory: " ).append( file.toString() ).append( Constants.NEW_LINE )
						.append( "  File Name: " ).append( fileName ).append( Constants.NEW_LINE );
				throw new IOException( error.toString() );
			}
		}
		try( final PrintWriter printWriter = new PrintWriter( PATH + fileName ) )
		{
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
	private void testEquals( final Object object, final Object reObject )
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
		else if( object instanceof Collection && reObject instanceof Collection )
		{
			final Collection< ? > colObject = Collection.class.cast( object );
			final Collection< ? > colReObject = Collection.class.cast( reObject );
			
			final int size = colObject.size();
			assertEquals( size, colReObject.size() );

			final Iterator< ? > iter = colObject.iterator();
			final Iterator< ? > reIter = colReObject.iterator();
			while( iter.hasNext() && reIter.hasNext() )
			{
				final Object elem = iter.next();
				final Object reElem = reIter.next();
				assertTrue( elem.equals( reElem ) );
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
		testXml( object, fileName, false );
	}
	
	/**
	 * 
	 * @param object
	 * @param fileName
	 */
	private void testXml( final Object object, final String fileName, final boolean isDisplayTypeInfo )
	{
		try
		{
			// create the semantic model for the division
			final InfoNode rootNode = createInfoNode( object );
	
			// write out XML
			writeXml( rootNode, fileName, isDisplayTypeInfo );
			
			// read in XML
			final InfoNode infoNode = readXml( fileName, object.getClass() );
			
			// reconstruct the object from the semantic model
			final Object reObject = createObject( infoNode, object.getClass() );
			
			// these should be the same
			testEquals( object, reObject );
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
			testEquals( object, reObject );
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
//		testXml( new double[] { 3, 1, 4, 1, 5, 9, 2, 6 }, "double_array.xml" );
		testXml( new double[] { 3.14, 1.41, 4.15, 1.59, 5.92, 9.26, 2.6, 6. }, "double_array.xml" );
	}

	@Test
	public void testDoubleArraysJson()
	{
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
	
	@Test
	public void testBadPersonArraysXml()
	{
		testXml( createBadPeople(), "bad_person_array.xml" );
	}
	
	@Test
	public void testBadPersonArraysJson()
	{
		testJson( createBadPeople(), "bad_person_array.json" );
	}
	
	@Test
	public void testPrimitivesXml()
	{
		testXml( "this is a test", "string.xml" );
		testXml( 3.14, "double_primitive.xml" );
		testXml( Double.valueOf( 3.14 ), "double.xml" );
		testXml( Float.valueOf( (float)3.14 ), "float.xml" );
		testXml( (float)3.14, "float_primitive.xml" );
		testXml( Integer.valueOf( 3 ), "int.xml" );
		testXml( 3, "int_primitive.xml" );
		testXml( new Long( 31415926 ), "long.xml" );
		testXml( Short.valueOf( (short)314 ), "short.xml" );
		testXml( (short)314, "short_primitive.xml" );
		testXml( 'p', "char_primitive.xml" );
		testXml( new Character( 'p' ), "char.xml" );
		testXml( true, "boolean_primitive.xml" );
		testXml( Boolean.valueOf( true ), "boolean.xml" );
		testXml( (byte)3, "byte_primitive.xml" );
		testXml( Byte.valueOf( (byte)3 ), "byte.xml" );
	}

	@Test
	public void testPrimitivesJson()
	{
		testJson( "this is a test", "string.json" );
		testJson( 3.14, "double_primitive.json" );
		testJson( Double.valueOf( 3.14 ), "double.json" );
		testJson( Float.valueOf( (float)3.14 ), "float.json" );
		testJson( (float)3.14, "float_primitive.json" );
		testJson( Integer.valueOf( 3 ), "int.json" );
		testJson( 3, "int_primitive.json" );
		testJson( new Long( 31415926 ), "long.json" );
		testJson( Short.valueOf( (short)314 ), "short.json" );
		testJson( (short)314, "short_primitive.json" );
		testJson( 'p', "char_primitive.json" );
		testJson( new Character( 'p' ), "char.json" );
		testJson( true, "boolean_primitive.json" );
		testJson( Boolean.valueOf( true ), "boolean.json" );
		testJson( (byte)3, "byte_primitive.json" );
		testJson( Byte.valueOf( (byte)3 ), "byte.json" );
	}

	@Test
	public void testListXml()
	{
		testXml( new ArrayList< Integer >( Arrays.asList( 3, 1, 4, 1, 5, 9, 2, 6 ) ), "integer_array_list.xml" );
	}
	
	@Test
	public void testListListXml()
	{
		final List< List< Integer > > list = new ArrayList<>();
		list.add( new ArrayList< Integer >( Arrays.asList( 3, 1 ) ) );
		list.add( new ArrayList< Integer >( Arrays.asList( 4, 1 ) ) );
		list.add( new ArrayList< Integer >( Arrays.asList( 5, 9 ) ) );
		list.add( new ArrayList< Integer >( Arrays.asList( 2, 6 ) ) );
		testXml( list, "integer_array_list_list.xml" );
	}
	
	@Test
	public void testListMapXml()
	{
		final Map< String, Integer > pi = new LinkedHashMap<>();
		pi.put( "three", 3 );
		pi.put( "one", 1 );
		pi.put( "four", 4 );
		pi.put( "one again", 1 );
		pi.put( "five", 5 );
		pi.put( "nine", 9 );

		final List< Map< String, Integer > > list = new ArrayList<>();
		list.add( pi );
		list.add( pi );
		testXml( list, "map_array_list.xml" );
	}

	@Test
	public void testListJson()
	{
		testJson( new ArrayList< Integer >( Arrays.asList( 3, 1, 4, 5, 9, 2, 6 ) ), "integer_array_list.json" );

	}
	
	@Test
	public void testListListJson()
	{
		final List< List< Integer > > list = new ArrayList<>();
		list.add( new ArrayList< Integer >( Arrays.asList( 3, 1 ) ) );
		list.add( new ArrayList< Integer >( Arrays.asList( 4, 1 ) ) );
		list.add( new ArrayList< Integer >( Arrays.asList( 5, 9 ) ) );
		list.add( new ArrayList< Integer >( Arrays.asList( 2, 6 ) ) );
		testJson( list, "integer_array_list_list.json" );
	}

	@Test
	public void testListMapJson()
	{
		final Map< String, Integer > pi = new LinkedHashMap<>();
		pi.put( "three", 3 );
		pi.put( "one", 1 );
		pi.put( "four", 4 );
		pi.put( "one again", 1 );
		pi.put( "five", 5 );
		pi.put( "nine", 9 );

		final List< Map< String, Integer > > list = new ArrayList<>();
		list.add( pi );
		list.add( pi );
		testJson( list, "map_array_list.json" );
	}

	@Test
	public void testMapXml()
	{
		final Map< String, Integer > pi = new LinkedHashMap<>();
		pi.put( "three", 3 );
		pi.put( "one", 1 );
		pi.put( "four", 4 );
		pi.put( "one again", 1 );
		pi.put( "five", 5 );
		pi.put( "nine", 9 );
		testXml( pi, "map.xml" );
	}
	
	@Test
	public void testMapListXml()
	{
		final Map< String, List< Integer > > piList = new LinkedHashMap<>();
		piList.put( "three-one", new ArrayList< Integer >( Arrays.asList( 3, 1 ) ) );
		piList.put( "four-one", new ArrayList< Integer >( Arrays.asList( 4, 1 ) ) );
		piList.put( "five-nine", new ArrayList< Integer >( Arrays.asList( 5, 9 ) ) );
		piList.put( "two-six", new ArrayList< Integer >( Arrays.asList( 2, 6 ) ) );
		testXml( piList, "map_list.xml" );
	}
	
	@Test
	public void testMapJson()
	{
		final Map< String, Integer > pi = new LinkedHashMap<>();
		pi.put( "three", 3 );
		pi.put( "one", 1 );
		pi.put( "four", 4 );
		pi.put( "one again", 1 );
		pi.put( "five", 5 );
		pi.put( "nine", 9 );
		testJson( pi, "map.json" );
	}
	
	@Test
	public void testMapListJson()
	{
		final Map< String, List< Integer > > piList = new LinkedHashMap<>();
		piList.put( "three-one", new ArrayList< Integer >( Arrays.asList( 3, 1 ) ) );
		piList.put( "four-one", new ArrayList< Integer >( Arrays.asList( 4, 1 ) ) );
		piList.put( "five-nine", new ArrayList< Integer >( Arrays.asList( 5, 9 ) ) );
		piList.put( "two-six", new ArrayList< Integer >( Arrays.asList( 2, 6 ) ) );
		testJson( piList, "map_list.json" );
	}
	
	@Test
	public void testDateXml()
	{
		final Calendar date = Calendar.getInstance();
		testXml( date, "date.xml" );
	}
	
	@Test
	public void testDateJson()
	{
		final Calendar date = Calendar.getInstance();
		testJson( date, "date.json" );
	}
	
	@Test
	public void testInheritence()
	{
		final BadPerson evilBob = new BadPerson( "Evil", "Bob", 66 );
		evilBob.addEvilDoing( "Scaring an old lady." );
		evilBob.addEvilDoing( "Putting milk in friend's beer can." );
		
		final InfoNode evilBobRoot = createInfoNode( evilBob );
		
		final BadPerson evilBobTwin = (BadPerson)createObject( evilBobRoot, BadPerson.class );
		
		testEquals( evilBob, evilBobTwin );
	}
	
	@Test
	public void testMaps()
	{
		testXml( new MapMagic(), "map_magic.xml" );

		final MapMagic mapMagic = new MapMagic();
		final InfoNode magicRoot = createInfoNode( mapMagic );
		final MapMagic magicTwin = (MapMagic)createObject( magicRoot, MapMagic.class );
		testEquals( mapMagic, magicTwin );
	}
}
