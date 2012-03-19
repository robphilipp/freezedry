package org.freezedry.persistence.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;
import org.w3c.dom.Document;

public class KeyValueWriter implements PersistenceWriter {

//	private static final Logger LOGGER = Logger.getLogger( KeyValueWriter.class );
	
	@Override
	public void write( final InfoNode rootNode, final Writer output )
	{
		final List< Pair< String, Object > > keyValuePairs = buildKeyValuePairs( rootNode );
		System.out.println( keyValuePairs );
	}

	/**
	 * Builds the DOM tree from the info node tree through recursive algorithm.
	 * @param rootInfoNode The root {@link InfoNode}
	 * @return The DOM tree as a {@link Document}
	 */
	private List< Pair< String, Object > > buildKeyValuePairs( final InfoNode rootInfoNode )
	{
		// create the map for holding the key-value pairs.
		final List< Pair< String, Object > > keyValuePairs = new ArrayList<>();
		
		// create the first DOM node from the info-node and add it to the document
		final Pair< String, Object > rootPair = createKeyValuePair( rootInfoNode, ""/*, keyValuePairs*/ );
		
		// recursively build the DOM tree from the info-node tree
		buildKeyValuePairs( rootInfoNode, rootPair.getFirst(), keyValuePairs );
		
		// once complete, then return the document (root node of the DOM tree)
		return keyValuePairs;
	}

	/*
	 * Recurses through the {@link InfoNode} tree and builds the DOM {@link Node} tree.
	 * @param infoNode The {@link InfoNode} from which to build the domNode
	 * @param domNode The DOM {@link Node} to which to add the new DOM {@link Node}s
	 * @param document The root node of the DOM tree (which is a {@link Document})
	 */
	private void buildKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues )
	{
		final List< InfoNode > children = infoNode.getChildren();
		for( InfoNode child : children )
		{
			// if the node represents a list, then let's use a list renderer? use similar design as used with the
			// the node builders...
			// create the new key value pair
			final Pair< String, Object > newKeyValuePair = createKeyValuePair( child, key/*, keyValues*/ );
			final String newKey = newKeyValuePair.getFirst();
			
			// when the value is null, then the key-value pair is associated with a compound node,
			// meaning it doesn't have a plain value. in this case, we don't add the key to the map,
			// but we do need to continue to aggregated the key
			if( newKeyValuePair.getSecond() != null )
			{
				keyValues.add( new Pair< String, Object >( newKey, newKeyValuePair.getSecond() ) );
			}
			
			// recursive call back to this method to create any child nodes
			buildKeyValuePairs( child, newKey, keyValues );
		}
	}

	/**
	 * 
	 * @param infoNode
	 * @param key
	 * @param keyValues
	 * @return
	 */
	private Pair< String, Object > createKeyValuePair( final InfoNode infoNode, final String key/*, final List< Pair< String, Object > > keyValues*/ )
	{
		// create the new key based on the specified key and the persistence name
		final String newKey = key + ":" + infoNode.getPersistName();

		final Pair< String, Object > keyValuePair = new Pair< String, Object >( newKey, null );
		
		// if the node is a leaf node, then it has a value, and we need to create a key-value pair
		if( infoNode.isLeafNode() )
		{
			// create the key-value pair and return it
			keyValuePair.setSecond( infoNode.getValue() );
		}
		
		return keyValuePair;
	}

	/**
	 * For testing
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static void main( String[] args ) throws ParserConfigurationException, ReflectiveOperationException, IOException, ParseException
	{
		DOMConfigurator.configure( "log4j.xml" );

		final Division division = new Division();
		final Person johnny = new Person( "Hernandez", "Johnny", 13 );
//		johnny.addFriend( "Polly", "bird" );
//		johnny.addFriend( "Sparky", "dog" );
//		for( int i = 0; i < 10; ++i )
//		{
//			johnny.addMood( Math.sin( Math.PI / 4 * i ) );
//		}
//		Map< String, String > group = new LinkedHashMap<>();
//		group.put( "one", "ONE" );
//		group.put( "two", "TWO" );
//		group.put( "three", "THREE" );
//		johnny.addGroup( "numbers", group );
//
//		group = new LinkedHashMap<>();
//		group.put( "a", "AY" );
//		group.put( "b", "BEE" );
//		johnny.addGroup( "letters", group );
		
		johnny.setBirthdate( DateUtils.createDateFromString( "1963-04-22", "yyyy-MM-dd" ) );
		
		division.addPerson( johnny );

		division.addPerson( new Person( "Prosky", "Julie", 15 ) );
		division.addPerson( new Person( "Jones", "Janet", 13 ) );
		division.addPerson( new Person( "Ghad", "Booda", 17 ) );
		
//		division.addMonth( "January", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//		division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 2, 3, 28 ) ) );
//		division.addMonth( "March", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//		division.addMonth( "April", new HashSet<>( Arrays.asList( 1, 2, 3, 30 ) ) );
//		
//		division.setCarNames( new String[] { "civic", "tsx", "accord" } );
		
		
		final PersistenceEngine engine = new PersistenceEngine();
		final InfoNode rootNode = engine.createSemanticModel( division );
		System.out.println( rootNode.treeToString() );

		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.txt" ) ) )
		{
			final KeyValueWriter writer = new KeyValueWriter();
			writer.write( rootNode, printWriter );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
