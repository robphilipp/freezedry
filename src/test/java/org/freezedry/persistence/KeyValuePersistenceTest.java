package org.freezedry.persistence;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyValuePersistenceTest {
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

			List<List< Integer >> collectionMatrix = new ArrayList<>();
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 11, 12, 13 ) ) );
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 21, 22, 23 ) ) );
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 31, 32, 33 ) ) );
			division.setCollectionMatrix( collectionMatrix );

			Map< String, Person > personMap = new LinkedHashMap<>();
			personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
			personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
			personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
			division.setPersonMap( personMap );

			final KeyValuePersistence persistence = new KeyValuePersistence();
			persistence.setKeySeparator( "." );
//			KeyValueBuilder builder = persistence.getKeyValueWriterBuilder();
//			builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
//			builder = persistence.getKeyValueReaderBuilder();
//			builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );

			persistence.write( division, "person.txt" );
			// two ways to override the output behavior programmatically
			// override the default date setting and replace the date node builder with the new one
//			final PersistenceEngine engine = persistence.getPersistenceEngine();
//			final DateNodeBuilder dateNodeBuilder = new DateNodeBuilder( engine, "yyyy-MM-dd" );
//			engine.addNodeBuilder( Calendar.class, dateNodeBuilder );
			// -- OR --
//			((DateNodeBuilder)engine.getNodeBuilder( Calendar.class )).setOutputDateFormat( "yyyy-MM-dd" );

			final Division redivision = persistence.read( Division.class, "person.txt" );
			System.out.println( redivision.toString() );

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	@Test
	public void testSetKeyValueBuilder() throws Exception
	{

	}

	@Test
	public void testGetKeyValueWriterBuilder() throws Exception
	{

	}

	@Test
	public void testGetKeyValueReaderBuilder() throws Exception
	{

	}

	@Test
	public void testSetKeySeparator() throws Exception
	{

	}

	@Test
	public void testGetKeySeparator() throws Exception
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
	public void testGetPersistenceWriter() throws Exception
	{

	}

	@Test
	public void testGetPersistenceReader() throws Exception
	{

	}
}
