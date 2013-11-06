package org.freezedry.persistence.builders;

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tree.InfoNode;

public class ByteNodeBuilder  extends AbstractPrimitiveLeafNodeBuilder {

	/**
	 * Constructs the {@link NodeBuilder} for going between primitives, their wrappers, {@link String}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public ByteNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public ByteNodeBuilder()
	{
		super();
	}
	
	/**
	 * Copy constructor
	 * @param builder
	 */
	public ByteNodeBuilder( final ByteNodeBuilder builder )
	{
		super( builder );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.nodes.InfoNode)
	 */
	@Override
	public Byte createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node )
	{
		final Object valueString = node.getValue();
		return Byte.parseByte( valueString.toString() );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.AbstractLeafNodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.InfoNode)
	 */
	@Override
	public Byte createObject( final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// grab the child node's value
		final Object nodeValue = node.getChild( 0 ).getValue();
		
		// here it is a bit complicated. recall that this method is called for root nodes, and so
		// value seems to jump between Byte, Integer, and String
		Byte value = null;
		if( nodeValue instanceof Byte )
		{
			value = (Byte)nodeValue;
		}
		if( nodeValue instanceof Integer )
		{
			value = (byte)(int)nodeValue;
		}
		else
		{
			value = Byte.parseByte( (String)nodeValue );
		}
		return value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public ByteNodeBuilder getCopy()
	{
		return new ByteNodeBuilder( this );
	}
}
