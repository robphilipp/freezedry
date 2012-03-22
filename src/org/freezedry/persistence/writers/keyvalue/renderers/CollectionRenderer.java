package org.freezedry.persistence.writers.keyvalue.renderers;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.writers.keyvalue.KeyValueWriter;
import org.freezedry.persistence.writers.keyvalue.renderers.decorators.StringDecorator;

public class CollectionRenderer extends AbstractPersistenceRenderer {
	
	private static StringDecorator INDEX_DECORATOR = new StringDecorator( "[", "]" );
	
	private StringDecorator indexDecorator;
	
	/**
	 * 
	 * @param writer
	 */
	public CollectionRenderer( final KeyValueWriter writer, final StringDecorator indexDecorator )
	{
		super( writer );
		
		this.indexDecorator = indexDecorator.getCopy();
	}
	
	/**
	 * 
	 * @param writer
	 */
	public CollectionRenderer( final KeyValueWriter writer )
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
			final String newKey = createKey( key, infoNode, index );
			if( node.isLeafNode() )
			{
				// create the key-value pair and return it
				getPersistenceWriter().createKeyValuePairs( node, newKey, keyValues, true );
			}
			else
			{
				getPersistenceWriter().buildKeyValuePairs( node, newKey, keyValues );
			}

			// increment the index count
			++index;
			
			// mark the node as processed so that it doesn't get processed again
			node.setIsProcessed( true );
		}
	}
	
	private String createKey( final String key, final InfoNode node, final int index )
	{
		String newKey = key;
		if( node.getPersistName() != null && !node.getPersistName().isEmpty() )
		{
			newKey += getPersistenceWriter().getSeparator() + node.getPersistName();
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
