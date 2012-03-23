package org.freezedry.persistence.keyvalue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;

public class BasicKeyValueBuilder extends AbstractKeyValueBuilder {

//	private static final Logger LOGGER = Logger.getLogger( BasicKeyValueBuilder.class );

	private boolean isShowFullKey = false;
	
	/**
	 * 
	 */
	public BasicKeyValueBuilder( final Map< Class< ? >, PersistenceRenderer > renderers, 
							final PersistenceRenderer arrayRenderer,
							final String separator )
	{
		super( renderers, arrayRenderer, separator );
	}

	/**
	 * 
	 */
	public BasicKeyValueBuilder( final String separator )
	{
		super( separator );
	}

	/**
	 * 
	 */
	public BasicKeyValueBuilder()
	{
		super();
	}
	

	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link List} would have a key of the form {@code names[i].String}
	 * @param isShowFullKey true means that the full key will be persisted; false is default
	 */
	public void setShowFullKey( final boolean isShowFullKey )
	{
		this.isShowFullKey = isShowFullKey;
	}
	
	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link List} would have a key of the form {@code names[i].String}
	 * @return true means that the full key will be persisted; false is default
	 */
	public boolean isShowFullKey()
	{
		return isShowFullKey;
	}
	
	/**
	 * Builds the {@link List} of key-value pairs from the info node tree through recursive algorithm.
	 * @param rootInfoNode The root {@link InfoNode}
	 * @return the {@link List} of key-value pairs 
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
			getArrayRenderer().buildKeyValuePair( infoNode, key, keyValues, isHidePersistName );
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
				newKey.append( getSeparator() );
			}
		}
		if( !isWithholdPersitName )
		{
			newKey.append( infoNode.getPersistName() );
		}
		return newKey.toString();
	}
}
