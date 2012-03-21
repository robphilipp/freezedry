package org.freezedry.persistence.writers.keyvalue.renderers;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.writers.keyvalue.KeyValueWriter;

public class MapRenderer extends AbstractPersistenceRenderer {

	/**
	 * 
	 * @param writer
	 */
	public MapRenderer( final KeyValueWriter writer )
	{
		super( writer );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link MapRenderer} to copy
	 */
	public MapRenderer( final MapRenderer renderer )
	{
		super( renderer );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.keyvalue.renderers.PersistenceRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues )
	{
		// the return pair
		Pair< String, Object > keyValuePair = null;

		// [Division:months{January,value[0]}, 1]
		// [Division:months{January,value[1]}, 2]
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
	public MapRenderer getCopy()
	{
		return new MapRenderer( this );
	}

}
