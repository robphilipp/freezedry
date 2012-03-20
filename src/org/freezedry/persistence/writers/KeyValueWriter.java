package org.freezedry.persistence.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.builders.NodeBuilder;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.containers.orderedseries.IntegerOrderedSeries;
import org.freezedry.persistence.renderers.ListRenderer;
import org.freezedry.persistence.renderers.PersistenceRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.w3c.dom.Document;

public class KeyValueWriter implements PersistenceWriter {

//	private static final Logger LOGGER = Logger.getLogger( KeyValueWriter.class );
	
	private Map< Class< ? >, PersistenceRenderer > renderers;
	
	/**
	 * 
	 */
	public KeyValueWriter()
	{
		renderers = createDefaultRenderers();
	}
	
	/*
	 * @return
	 */
	private static Map< Class< ? >, PersistenceRenderer > createDefaultRenderers()
	{
		final Map< Class< ? >, PersistenceRenderer > renderers = new HashMap<>();
		renderers.put( List.class, new ListRenderer() );
		
		return renderers;
	}
	
	/**
	 * Finds the {@link PersistenceRenderer} associated with the class. If the specified class
	 * doesn't have a renderer, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the persistence renderer map for the
	 * specified class associating it with the returned persistence renderer (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a persistence renderer
	 * @return the {@link PersistenceRenderer} associated with the class
	 */
	public PersistenceRenderer getRenderer( final Class< ? > clazz )
	{
		// simplest case is that the info node builders map has an entry for the class
		PersistenceRenderer renderer = renderers.get( clazz );
		
		// if the info node builder didn't have a direct entry, work our way up the inheritance
		// hierarchy, and find the closed parent class, assigning it its associated info node builder
		if( renderer == null )
		{
			// run through the available info node builders holding the distance (number of levels in the
			// inheritance hierarchy) they are from the specified class
			final IntegerOrderedSeries< Class< ? > > hierarchy = new IntegerOrderedSeries<>();
			for( Map.Entry< Class< ? >, PersistenceRenderer > entry : renderers.entrySet() )
			{
				final Class< ? > targetClass = entry.getKey();
				final int level = ReflectionUtils.calculateClassDistance( clazz, targetClass );
				if( level > -1 )
				{
					hierarchy.add( level, targetClass );
				}
			}
			
			// if one or more parent classes were found, then take the first one,
			// which is the closest one, grab its info node builder, and add an entry for the
			// specified class to the associated info node builder for faster subsequent look-ups
			if( !hierarchy.isEmpty() )
			{
				final Class< ? > closestParent = hierarchy.getFirstValue();
				renderer = renderers.get( closestParent );
				renderers.put( clazz, renderer.getCopy() );
			}
		}
		return renderer;
	}

	/**
	 * Finds the {@link PersistenceRenderer} associated with the class. If the specified class
	 * doesn't have a renderer, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the persistence renderer map for the
	 * specified class associating it with the returned persistence renderer (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a persistence renderer
	 * @return the true if a persistence renderer was found; false otherwise
	 */
	public boolean containsRenderer( final Class< ? > clazz )
	{
		return ( getRenderer( clazz ) != null );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.PersistenceWriter#write(org.freezedry.persistence.tree.InfoNode, java.io.Writer)
	 */
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

	/**
	 * 
	 * @param infoNode
	 * @param key
	 * @param keyValues
	 */
	public void buildKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues )
	{
		final List< InfoNode > children = infoNode.getChildren();
		for( InfoNode child : children )
		{
			// if the node represents a list, then let's use a list renderer? use similar design as used with the
			// the node builders...
			// create the new key value pair
			final Pair< String, Object > newKeyValuePair = createKeyValuePair( child, key );
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
	 * @return
	 */
	private Pair< String, Object > createKeyValuePair( final InfoNode infoNode, final String key )
	{
		// grab the type information from the info node
		final Class< ? > clazz = infoNode.getClazz();
		
		
		
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
