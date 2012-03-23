package org.freezedry.persistence.keyvalue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.renderers.CollectionRenderer;
import org.freezedry.persistence.keyvalue.renderers.LeafNodeRenderer;
import org.freezedry.persistence.keyvalue.renderers.MapRenderer;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.w3c.dom.Document;

public class BasicKeyValueBuilder {

//	private static final Logger LOGGER = Logger.getLogger( BasicKeyValueBuilder.class );
	private static final String SEPARATOR = ":";
	
	private Map< Class< ? >, PersistenceRenderer > renderers;
	private PersistenceRenderer arrayRenderer;
	private boolean isShowFullKey = false;
	
	private String separator;
	
	/**
	 * 
	 */
	public BasicKeyValueBuilder( final Map< Class< ? >, PersistenceRenderer > renderers, 
							final PersistenceRenderer arrayRenderer,
							final String separator )
	{
		this.renderers = renderers;
		this.arrayRenderer = arrayRenderer;
		this.separator = separator;
	}

	/**
	 * 
	 */
	public BasicKeyValueBuilder( final String separator )
	{
		renderers = createDefaultRenderers();
		arrayRenderer = new CollectionRenderer( this );
		this.separator = separator;
	}

	/**
	 * 
	 */
	public BasicKeyValueBuilder()
	{
		renderers = createDefaultRenderers();
		arrayRenderer = new CollectionRenderer( this );
		separator = SEPARATOR;
	}
	
	/*
	 * @return The mapping between class and their associated renderer
	 */
	private Map< Class< ? >, PersistenceRenderer > createDefaultRenderers()
	{
		final Map< Class< ? >, PersistenceRenderer > renderers = new HashMap<>();
		renderers.put( Collection.class, new CollectionRenderer( this ) );
		renderers.put( Map.class, new MapRenderer( this ) );

		renderers.put( String.class, new LeafNodeRenderer( this ) );
		
		renderers.put( Integer.class, new LeafNodeRenderer( this ) );
		renderers.put( Long.class, new LeafNodeRenderer( this ) );
		renderers.put( Short.class, new LeafNodeRenderer( this ) );
		renderers.put( Double.class, new LeafNodeRenderer( this ) );
		renderers.put( Boolean.class, new LeafNodeRenderer( this ) );
		
		renderers.put( Integer.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Long.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Short.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Double.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Boolean.TYPE, new LeafNodeRenderer( this ) );
		
		return renderers;
	}
	
	public void setSeparator( final String separator )
	{
		this.separator = separator;
	}
	
	public String getSeparator()
	{
		return separator;
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
		return ReflectionUtils.getItemOrAncestor( clazz, renderers );
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
	
	/**
	 * Builds the DOM tree from the info node tree through recursive algorithm.
	 * @param rootInfoNode The root {@link InfoNode}
	 * @return The DOM tree as a {@link Document}
	 */
	public List< Pair< String, Object > > buildKeyValuePairs( final InfoNode rootInfoNode )
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
				newKey.append( separator );
			}
		}
		if( !isWithholdPersitName )
		{
			newKey.append( infoNode.getPersistName() );
		}
		return newKey.toString();
	}
}
