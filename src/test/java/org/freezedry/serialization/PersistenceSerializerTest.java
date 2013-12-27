package org.freezedry.serialization;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import junit.framework.Assert;
import org.freezedry.PaxExamTestUtils;
import org.freezedry.difference.ObjectDifferenceCalculator;
import org.freezedry.persistence.tests.BadPerson;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import static org.freezedry.PaxExamTestUtils.freezedryBundles;
import static org.freezedry.PaxExamTestUtils.logging;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * [Description]
 *
 * @author rob
 *         12/1/13 3:04 PM
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class PersistenceSerializerTest {

	private Person person;
	private BadPerson badPerson;
	private Division division;

	private final Serializer xmlSerializer = new XmlPersistenceSerializer();
	private final Serializer jsonSerializer = new JsonPersistenceSerializer();
	private final Serializer keyValueSerializer = new KeyValuePersistenceSerializer();

	/**
	 * @return The configuration for the OSGi framework
	 */
	@Configuration
	public Option[] configuration()
	{
//		((Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME )).setLevel( Level.WARN );
		return combine( combine( freezedryBundles(), logging() ),
				junitBundles(),
				cleanCaches() );
	}

	@Before
	public void setup() throws ParseException
	{
		person = new Person( "shocker", "derek", 29 );
		person.addFriend( "james henley", "buddy buddy" );
		person.addFriend( "castor heliopolis", "buddy" );
		person.addFriend( "janis joplin", "music" );
		Map< String, String > group = new LinkedHashMap<>();
		group.put( "one", "ONE" );
		group.put( "two", "TWO" );
		group.put( "three", "THREE" );
		person.addGroup( "numbers", group );
		group = new LinkedHashMap<>();
		group.put( "a", "AY" );
		group.put( "b", "BEE" );
		person.addGroup( "letters", group );

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
	public void testDeserializeSerializeXml() throws Exception
	{
		serializeDeserialize( xmlSerializer, person, Person.class );
		serializeDeserialize( xmlSerializer, badPerson, BadPerson.class );
		serializeDeserialize( xmlSerializer, division, Division.class );
	}

	@Test
	public void testDeserializeSerializeJson() throws Exception
	{
		serializeDeserialize( jsonSerializer, person, Person.class );
		serializeDeserialize( jsonSerializer, badPerson, BadPerson.class );
		serializeDeserialize( jsonSerializer, division, Division.class );
	}

	@Test
	public void testSerializeDeserializeKeyValue() throws Exception
	{
		serializeDeserialize( keyValueSerializer, person, Person.class );
		serializeDeserialize( keyValueSerializer, badPerson, BadPerson.class );
		serializeDeserialize( keyValueSerializer, division, Division.class );
	}

	/**
	 * Serializes the object into a byte[] and then reconstructes that object and compares it
	 * @param serializer The serializer to use for serializing and deserializing
	 * @param object The object to serialize and deserialized
	 * @param clazz The type of the oject being serialized/deserialized
	 * @param <T> The object type
	 * @throws FileNotFoundException
	 */
	private < T > void serializeDeserialize( final Serializer serializer,
											 final T object,
											 final Class< T > clazz ) throws FileNotFoundException
	{
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		serializer.serialize( object, outputStream );

		final byte[] serializedBytes = outputStream.toByteArray();
		final T reObject = serializer.deserialize( new ByteArrayInputStream( serializedBytes ), clazz );

		final ObjectDifferenceCalculator calculator = new ObjectDifferenceCalculator();
		final Map< String, ObjectDifferenceCalculator.Difference > differences = calculator.calculateDifference( reObject, object );
		Assert.assertTrue( differences == null || differences.isEmpty() );
	}
}
