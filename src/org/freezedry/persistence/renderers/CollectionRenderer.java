package org.freezedry.persistence.renderers;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.writers.KeyValueWriter;

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
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues )
	{
		// the return pair
		Pair< String, Object > keyValuePair = null;

		int index = 0;
		for( InfoNode node : infoNode.getChildren() )
		{
			final String newKey = key + "[" + index + "]";
//			final String newKey = key + ":" + node.getPersistName() + "[" + index + "]";
			keyValuePair = new Pair< String, Object >( newKey, null );
			if( node.isLeafNode() )
			{
				// create the key-value pair and return it
				keyValuePair.setSecond( node.getValue() );
				keyValues.add( keyValuePair );
			}
			else
			{
				getPeristPersistenceWriter().buildKeyValuePairs( node, newKey, keyValues );
			}
			
			++index;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public PersistenceRenderer getCopy()
	{
		return new CollectionRenderer( this );
	}

}
