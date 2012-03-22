package org.freezedry.persistence.writers.keyvalue.renderers;

import java.util.List;

import org.apache.log4j.Logger;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.writers.keyvalue.KeyValueWriter;

public class IntegerRenderer extends AbstractPersistenceRenderer {
	
	private static final Logger LOGGER = Logger.getLogger( IntegerRenderer.class );

	public IntegerRenderer( final KeyValueWriter writer )
	{
		super( writer );
	}
	
	public IntegerRenderer( final IntegerRenderer renderer )
	{
		super( renderer );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.keyvalue.renderers.PersistenceRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersistName )
	{
		// ensure that the info node is a leaf
		if( !infoNode.isLeafNode() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "To render a key-value pair as an Integer, the info node must be a leaf node." + Constants.NEW_LINE );
			message.append( "  Current Key:" + key + Constants.NEW_LINE );
			message.append( "  InfoNode:" + Constants.NEW_LINE );
			message.append( "    Persist Name: " + infoNode.getPersistName() + Constants.NEW_LINE );
			message.append( "    Node Type: " + infoNode.getNodeType().name() + Constants.NEW_LINE );
			message.append( "    Child Nodes: " + infoNode.getChildCount() + Constants.NEW_LINE );
			message.append( "    Node Class Type: " + infoNode.getClazz().getName() + Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		String newKey = key;
		if( !isWithholdPersistName && infoNode.getPersistName() != null && !infoNode.getPersistName().isEmpty() )
		{
			newKey += ":" + infoNode.getPersistName();
		}
		final Integer value = (Integer)infoNode.getValue();
		keyValues.add( new Pair< String, Object >( newKey, value ) );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public IntegerRenderer getCopy()
	{
		return new IntegerRenderer( this );
	}

}
