package org.freezedry.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.readers.JsonReader;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.writers.JsonWriter;

public class JsonPersistence extends AbstractFileBasedPersistence {

	private JsonWriter jsonWriter;
	private JsonReader jsonReader;
	
	/*
	 * Creates the {@link JsonWriter} if it hasn't yet been instantiated, and returns it
	 * @return the {@link JsonWriter}
	 */
	protected JsonWriter getPersistenceWriter()
	{
		if( jsonWriter == null )
		{
			jsonWriter = new JsonWriter();
		}
		return jsonWriter;
	}

	/*
	 * Creates the {@link JsonReader} if it hasn't yet been instantiated, and returns it
	 * @return the {@link JsonReader}
	 */
	protected JsonReader getPersistenceReader()
	{
		if( jsonReader == null )
		{
			jsonReader = new JsonReader();
		}
		return jsonReader;
	}
	
	public static void main( String[] args )
	{
		try
		{
			DOMConfigurator.configure( "log4j.xml" );
	
			JsonPersistence persist = new JsonPersistence();
			persist.write( "this is a test", "test.json" );
//			persist.write( new Double( 3.14 ), "test.json" );
//			final List< Double > list = new ArrayList<>( Arrays.asList( 3.14, 2.7, 111.11 ) );
//			persist.write( list, "test.json" );
			
			System.out.println( "Read: " + persist.read( String.class, "test.json" ) );
//			System.out.println( "Read: " + persist.read( Double.class, "test.json" ) );
//			final List< ? > relist = persist.read( ArrayList.class, "test.json" );
//			for( Object item : relist )
//			{
//				System.out.println( "Read: " + item );
//			}
			System.exit( 0 );

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
			
			List< List< Integer > > collectionMatrix = new ArrayList<>();
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 11, 12, 13 ) ) );
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 21, 22, 23 ) ) );
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 31, 32, 33 ) ) );
			division.setCollectionMatrix( collectionMatrix );
			
			Map< String, Person > personMap = new LinkedHashMap<>();
			personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
			personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
			personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
			division.setPersonMap( personMap );
			
			final JsonPersistence persistence = new JsonPersistence();
//			persistence.setDisplayTypeInfo( true );
			persistence.write( division, "person.json" );
			// two ways to override the output behavior programmatically
			// override the default date setting and replace the date node builder with the new one
//			final PersistenceEngine engine = persistence.getPersistenceEngine();
//			final DateNodeBuilder dateNodeBuilder = new DateNodeBuilder( engine, "yyyy-MM-dd" );
//			engine.addNodeBuilder( Calendar.class, dateNodeBuilder );
			// -- OR --
//			((DateNodeBuilder)engine.getNodeBuilder( Calendar.class )).setOutputDateFormat( "yyyy-MM-dd" );
			
			final Division redivision = persistence.read( Division.class, "person.json" );
			System.out.println( redivision.toString() );
			
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
