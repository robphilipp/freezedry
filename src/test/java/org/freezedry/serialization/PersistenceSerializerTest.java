package org.freezedry.serialization;

import junit.framework.Assert;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.tests.BadPerson;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.*;

/**
 * [Description]
 *
 * @author rob
 *         12/1/13 3:04 PM
 */
public class PersistenceSerializerTest {

	private static final Logger LOGGER = Logger.getLogger( PersistenceSerializerTest.class );

	protected final static String OUTPUT_DIR = "src/test/output/";

	private Person person;
	private BadPerson badPerson;
	private Division division;

	private final Serializer xmlSerializer = new XmlPersistenceSerializer();
	private final Serializer jsonSerializer = new JsonPersistenceSerializer();
	private final Serializer keyValueSerializer = new KeyValuePersistenceSerializer();

	@BeforeClass
	public static void init()
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.WARN );
	}

	@Before
	public void setup() throws ParseException
	{
		person = new Person( "shocker", "derek", 29 );
		person.addFriend( "james henley", "buddy buddy" );
		person.addFriend( "castor heliopolis", "buddy" );
		person.addFriend( "janis joplin", "music" );

		badPerson = new BadPerson( "henley", "james", 42 );
		badPerson.addEvilDoing( "Added semicolon after the closing parenthesis of colleague's code." );
		badPerson.addEvilDoing( "Made the hashCode() method of colleague's class always return a constant." );

		createDivistion();
	}

	private void createDivistion() throws ParseException
	{
		// create the object to persist
		division = new Division();
		final Person johnny = new Person( "Hernandez", "Johnny", 13 );
		johnny.addFriend( "Polly (A)", "bird" );
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
		collectionMatrix.add( new ArrayList<>( Arrays.asList( 11, 12, 13 ) ) );
		collectionMatrix.add( new ArrayList<>( Arrays.asList( 21, 22, 23 ) ) );
		collectionMatrix.add( new ArrayList<>( Arrays.asList( 31, 32, 33 ) ) );
		division.setCollectionMatrix( collectionMatrix );

		Map< String, Person > personMap = new LinkedHashMap<>();
		personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
		personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
		personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
		division.setPersonMap( personMap );
	}

	@Test
	public void testSerializeXml() throws Exception
	{
		serializeDeserialize( OUTPUT_DIR + "person_test.xml", xmlSerializer, person, Person.class );
		serializeDeserialize( OUTPUT_DIR + "bad_person_test.xml", xmlSerializer, badPerson, BadPerson.class );
		serializeDeserialize( OUTPUT_DIR + "division_test.xml", xmlSerializer, division, Division.class );
	}

	@Test
	public void testSerializeJson() throws Exception
	{
		serializeDeserialize( OUTPUT_DIR + "person_test.json", jsonSerializer, person, Person.class );
		serializeDeserialize( OUTPUT_DIR + "bad_person_test.json", jsonSerializer, badPerson, BadPerson.class );
		serializeDeserialize( OUTPUT_DIR + "division_test.json", jsonSerializer, division, Division.class );
	}

	@Test
	public void testSerializeKeyValue() throws Exception
	{
		serializeDeserialize( OUTPUT_DIR + "person_test.txt", keyValueSerializer, person, Person.class );
		serializeDeserialize( OUTPUT_DIR + "bad_person_test.txt", keyValueSerializer, badPerson, BadPerson.class );
		serializeDeserialize( OUTPUT_DIR + "division_test.txt", keyValueSerializer, division, Division.class );
	}

	private < T > void serializeDeserialize( final String filename,
										final Serializer serializer,
										final T object,
										final Class< T > clazz ) throws FileNotFoundException
	{
		try
		{
			serializer.serialize( object, new FileOutputStream( filename ) );
			final T reObject = serializer.deserialize( new FileInputStream( filename ), clazz );
			Assert.assertEquals( "Deserialized object not equal to original: " + object.getClass().getName(), object, reObject );
		}
		catch( FileNotFoundException e )
		{
			Assert.fail( "Could not find serialized Person in file: " + e );
		}
	}

	@Test
	public void testDeserialize() throws Exception
	{

	}
}
