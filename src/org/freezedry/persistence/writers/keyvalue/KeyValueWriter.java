package org.freezedry.persistence.writers.keyvalue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.containers.orderedseries.IntegerOrderedSeries;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.freezedry.persistence.writers.PersistenceWriter;
import org.freezedry.persistence.writers.keyvalue.renderers.CollectionRenderer;
import org.freezedry.persistence.writers.keyvalue.renderers.DoubleRenderer;
import org.freezedry.persistence.writers.keyvalue.renderers.IntegerRenderer;
import org.freezedry.persistence.writers.keyvalue.renderers.MapRenderer;
import org.freezedry.persistence.writers.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.writers.keyvalue.renderers.StringRenderer;
import org.w3c.dom.Document;

public class KeyValueWriter implements PersistenceWriter {

//	private static final Logger LOGGER = Logger.getLogger( KeyValueWriter.class );
	
	private Map< Class< ? >, PersistenceRenderer > renderers;
	private PersistenceRenderer arrayRenderer;
	private boolean isShowFullKey = false;
	
	/**
	 * 
	 */
	public KeyValueWriter()
	{
		renderers = createDefaultRenderers();
		arrayRenderer = new CollectionRenderer( this );
	}
	
	/*
	 * @return The mapping between class and their associated renderer
	 */
	private Map< Class< ? >, PersistenceRenderer > createDefaultRenderers()
	{
		final Map< Class< ? >, PersistenceRenderer > renderers = new HashMap<>();
		renderers.put( Collection.class, new CollectionRenderer( this ) );
		renderers.put( Map.class, new MapRenderer( this ) );
		renderers.put( String.class, new StringRenderer( this ) );
		renderers.put( Integer.class, new IntegerRenderer( this ) );
		renderers.put( Double.class, new DoubleRenderer( this ) );
		
		return renderers;
	}
	
	public void setShowFullKey( final boolean isShowFullKey )
	{
		this.isShowFullKey = isShowFullKey;
	}
	
	public boolean isShowFullKey()
	{
		return isShowFullKey;
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
		for( Pair< String, Object > pair : keyValuePairs )
		{
			System.out.println( pair );
		}
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
//		final Pair< String, Object > rootPair = createKeyValuePairs( rootInfoNode, "", keyValuePairs );
		
		// recursively build the DOM tree from the info-node tree
//		buildKeyValuePairs( rootInfoNode, rootPair.getFirst(), keyValuePairs );
		buildKeyValuePairs( rootInfoNode, rootInfoNode.getPersistName(), keyValuePairs );
		
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
		// run through the node's children, and for each one create and add the key-value pairs
		// to the list of key-value pairs
		for( InfoNode child : infoNode.getChildren() )
		{
			// if a child has been processed already, and marked processed, then we don't process
			// it again. this can occur if the node is, for example, a collection or map, in which
			// case the subnodes are processed outside of this loop, and this method may be called
			// recursively, and we want to ensure that the node is only processed once.
			if( !child.isProcessed() )
			{
				// create the new key value pairs
				createKeyValuePairs( child, key, keyValues, false );
				
				// mark the node as being processed
				child.setIsProcessed( true );
			}
		}
	}

//	/**
//	 * 
//	 * @param infoNode
//	 * @param key
//	 * @param keyValues
//	 */
//	public void createKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues )
//	{
//		createKeyValuePairs( infoNode, key, keyValues, false );
//	}
	
	/**
	 * 
	 * @param infoNode
	 * @param key
	 * @param keyValues
	 * @param isWithholdPersitName
	 */
	public void createKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersitName )
	{
		final boolean isHidePersistName = ( isShowFullKey ? false : isWithholdPersitName );
		final Class< ? > clazz = infoNode.getClazz();
		if( containsRenderer( clazz ) )
		{
			getRenderer( clazz ).buildKeyValuePair( infoNode, key, keyValues, isHidePersistName );
		}
		else if( clazz.isArray() )
		{
			arrayRenderer.buildKeyValuePair( infoNode, key, keyValues, isHidePersistName );
		}
		else
		{
			// create the new key based on the specified key and the persistence name
			final String newKey = createKey( infoNode, key, isHidePersistName );
			final Pair< String, Object > keyValuePair = new Pair< String, Object >( newKey, null );
			
			// if the node is a leaf node, then it has a value, and we need to create a key-value pair
			if( infoNode.isLeafNode() )
			{
				// create the key-value pair and return it
				keyValuePair.setSecond( infoNode.getValue() );
				keyValues.add( keyValuePair );
			}
			else
			{
				buildKeyValuePairs( infoNode, newKey, keyValues );
			}
		}
	}
	
	private String createKey( final InfoNode infoNode, final String key, final boolean isWithholdPersitName )
	{
		final StringBuffer newKey = new StringBuffer();
		if( key != null && !key.isEmpty() )
		{
			newKey.append( key );
			if( !isWithholdPersitName )
			{
				newKey.append( ":" );
			}
		}
		if( !isWithholdPersitName )
		{
			newKey.append( infoNode.getPersistName() );
		}
		return newKey.toString();
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
		
		
		final PersistenceEngine engine = new PersistenceEngine();
		final InfoNode rootNode = engine.createSemanticModel( division );
		System.out.println( rootNode.treeToString() );

		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.txt" ) ) )
		{
			final KeyValueWriter writer = new KeyValueWriter();
//			writer.setShowFullKey( true );
			writer.write( rootNode, printWriter );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
