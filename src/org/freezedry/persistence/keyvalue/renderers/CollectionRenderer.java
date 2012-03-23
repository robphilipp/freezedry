package org.freezedry.persistence.keyvalue.renderers;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.keyvalue.BasicKeyValueBuilder;

public class CollectionRenderer extends AbstractPersistenceRenderer {
	
	private static StringDecorator INDEX_DECORATOR = new StringDecorator( "[", "]" );
	
	private StringDecorator indexDecorator;
	
	/**
	 * 
	 * @param writer
	 */
	public CollectionRenderer( final BasicKeyValueBuilder writer, final StringDecorator indexDecorator )
	{
		super( writer );
		
		this.indexDecorator = indexDecorator.getCopy();
	}
	
	/**
	 * 
	 * @param writer
	 */
	public CollectionRenderer( final BasicKeyValueBuilder writer )
	{
		this( writer, INDEX_DECORATOR );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link PersistenceRenderer} to copy
	 */
	public CollectionRenderer( final CollectionRenderer renderer )
	{
		super( renderer );
		
		this.indexDecorator = renderer.indexDecorator.getCopy();
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.renderers.PersistenceRenderer#createKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersistName )
	{
		int index = 0;
		for( InfoNode node : infoNode.getChildren() )
		{
			if( node.isLeafNode() )
			{
				// create the key-value pair and return it
				final String newKey = createLeafNodeKey( key, infoNode, index );
				getPersistenceWriter().createKeyValuePairs( node, newKey, keyValues, true );
			}
			else
			{
				final String newKey = createNodeKey( key, infoNode, node, index );
				getPersistenceWriter().buildKeyValuePairs( node, newKey, keyValues );
			}

			// increment the index count
			++index;
			
			// mark the node as processed so that it doesn't get processed again
			node.setIsProcessed( true );
		}
	}

	/**
	 * Creates a key for a leaf node collection. For example, if the persist name for a {@link List} is
	 * people, which is a <code>{@link List}< {@link String} ></code>, then the key will be {@code people[i]}
	 * where the {@code i} is the index of the list.
	 * @param key The current key to which to append the persisted name and decorated index
	 * @param parentNode The parent node, which holds the name of the field
	 * @param index The index of the element in the {@link List}
	 * @return The key
	 */
	private String createLeafNodeKey( final String key, final InfoNode parentNode, final int index )
	{
		return createNodeKey( key, parentNode, null, index );
	}

	/**
	 * Creates a key for a compound node collection. For example, if the persist name for a {@link List} is
	 * people, which is a <code>{@link List}< {@link Person} ></code>, then the key will be {@code people.Person[i]}
	 * where the {@code i} is the index of the list.
	 * @param key The current key to which to append the persisted name and decorated index
	 * @param parentNode The parent node, which holds the name of the field (in this example, "{@code people}")
	 * @param node The current node (in this example, "{@code Person}")
	 * @param index The index of the element in the {@link List}
	 * @return The key
	 */
	private String createNodeKey( final String key, final InfoNode parentNode, final InfoNode node, final int index )
	{
		String newKey = key;
		if( parentNode.getPersistName() != null && !parentNode.getPersistName().isEmpty() )
		{
			final String separator = getPersistenceWriter().getSeparator(); 
			newKey += separator + parentNode.getPersistName();
			if( node != null )
			{
				newKey += separator + node.getPersistName();
			}
		}
		newKey += indexDecorator.decorate( index );
		return newKey;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public CollectionRenderer getCopy()
	{
		return new CollectionRenderer( this );
	}

}
