package org.freezedry.serialization;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.freezedry.persistence.tests.Person;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * [Description]
 *
 * @author rob
 *         12/1/13 3:04 PM
 */
public class PersistenceSerializerTest {

	private static final Logger LOGGER = Logger.getLogger( PersistenceSerializerTest.class );

	protected final static String OUTPUT_DIR = "src/test/output/";

	private final Person person = new Person( "shocker", "derek", 29 );

	@Test
	public void testSerializeXml() throws Exception
	{
		serializeDeserialize( OUTPUT_DIR + "person_test.xml", new XmlPersistenceSerializer(), person, Person.class );
	}

	@Test
	public void testSerializeJson() throws Exception
	{
		serializeDeserialize( OUTPUT_DIR + "person_test.json", new JsonPersistenceSerializer(), person, Person.class );
	}

	@Test
	public void testSerializeKeyValue() throws Exception
	{
		serializeDeserialize( OUTPUT_DIR + "person_test.txt", new KeyValuePersistenceSerializer(), person, Person.class );
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
