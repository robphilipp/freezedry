package org.freezedry.persistence.writers;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.BasicKeyValueBuilder;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.FlatteningCollectionRenderer;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

/**
 * [Description]
 *
 * @author rphilipp
 *         10/7/13, 11:00 AM
 */
public class KeyValueFlattener {

	private static final Logger LOGGER = Logger.getLogger( KeyValueFlattener.class );

	private KeyValueBuilder builder;

	/**
	 * Constructs a basic key-value flattener that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link org.freezedry.persistence.tree.InfoNode} and
	 * the {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create key-value pairs for
	 * {@link org.freezedry.persistence.tree.InfoNode}s that represent an array.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @see org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public KeyValueFlattener( final Map< Class< ? >, PersistenceRenderer > renderers,
							  final PersistenceRenderer arrayRenderer,
						   	  final String keySeparator )
	{
		builder = new BasicKeyValueBuilder( renderers, arrayRenderer, keySeparator );
	}

	/**
	 * Constructs a basic key-value flattener that uses the default renderers and specified separator.
	 * @param keySeparator The separator between the flattened elements of the key
	 */
	public KeyValueFlattener( final String keySeparator )
	{
		builder = new BasicKeyValueBuilder( keySeparator );
	}

	/**
	 * Constructs a basic key-value flattener that uses the default renderers and separator.
	 */
	public KeyValueFlattener()
	{
		builder = new BasicKeyValueBuilder();
	}

	/**
	 * Constructs a key-value flattener using the specified key-value list builder
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model
	 */
	public KeyValueFlattener( final KeyValueBuilder builder )
	{
		this.builder = builder;
	}

	/**
	 * The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 * @param separator The separator
	 */
	public void setKeyElementSeparator( final String separator )
	{
		builder.setSeparator( separator );
	}

	/**
	 * @return The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 */
	public String getKeyElementSeparator()
	{
		return builder.getSeparator();
	}

	/**
	 * Sets the builder responsible for creating the key-value pairs from the semantic model,
	 * and that is responsible for parsing the key-value pairs into a semantic model.
	 * @param builder the {@link KeyValueBuilder} responsible for creating the key-value pairs
	 * from the semantic model, and that is responsible for parsing the key-value pairs into
	 * a semantic model.
	 */
	public void setBuilder( final KeyValueBuilder builder )
	{
		this.builder = builder;
	}

	/**
	 * @return the {@link KeyValueBuilder} responsible for creating the key-value pairs
	 * from the semantic model, and that is responsible for parsing the key-value pairs into
	 * a semantic model.
	 */
	public KeyValueBuilder getBuilder()
	{
		return builder;
	}

	public List<Pair< String, Object >> buildKeyValuePairs( final InfoNode root )
	{
		return builder.buildKeyValuePairs( root );
	}

	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link java.util.List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link java.util.List} would have a key of the form {@code names[i].String}. I recommend
	 * against setting this to true.
	 * @param isShowFullKey true means that the full key will be persisted; false is default
	 */
	public void setShowFullKey( final boolean isShowFullKey )
	{
		builder.setShowFullKey( isShowFullKey );
	}

	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link java.util.List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link java.util.List} would have a key of the form {@code names[i].String}
	 * @return true means that the full key will be persisted; false is default
	 */
	public boolean isShowFullKey()
	{
		return builder.isShowFullKey();
	}

	/**
	 * For testing
	 * @param args
	 * @throws javax.xml.parsers.ParserConfigurationException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws java.text.ParseException
	 */
	public static void main( String[] args ) throws ParserConfigurationException, ReflectiveOperationException, IOException, ParseException
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

		List< List< Integer > > collectionMatrix = Arrays.asList(
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
//			final KeyValueBuilder builder = writer.getBuilder();
//			builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
//			writer.setKeyElementSeparator( "." );
			writer.write( rootNode, printWriter );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
