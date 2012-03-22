package org.freezedry.persistence.writers.keyvalue.renderers;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.writers.keyvalue.KeyValueWriter;

public class CollectionRenderer extends AbstractPersistenceRenderer {
	
	/**
	 * 
	 * @param writer
	 */
	public CollectionRenderer( final KeyValueWriter writer )
	{
		super( writer );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link PersistenceRenderer} to copy
	 */
	public CollectionRenderer( final CollectionRenderer renderer )
	{
		super( renderer );
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
//			final String newKey = key + ":" + infoNode.getPersistName() + "[" + index + "]";
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
			newKey += ":" + node.getPersistName();
		}
		newKey += "[" + index + "]";
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
