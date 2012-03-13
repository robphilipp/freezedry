package org.freezedry.persistence.tests;

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.builders.NodeBuilder;
import org.freezedry.persistence.builders.StringNodeBuilder;
import org.freezedry.persistence.tree.InfoNode;

public class CapStringNodeBuilder extends StringNodeBuilder {

	/**
	 * Constructs the {@link NodeBuilder} for going between primitives, their wrappers, {@link String}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public CapStringNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public CapStringNodeBuilder()
	{
		super();
	}
	
	/**
	 * Copy constructor
	 * @param builder
	 */
	public CapStringNodeBuilder( final CapStringNodeBuilder builder )
	{
		super( builder );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.nodes.InfoNode)
	 */
	@Override
	public String createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node )
	{
		final Object valueString = node.getValue();
		return valueString.toString().toUpperCase();
	}

	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public CapStringNodeBuilder getCopy()
	{
		return new CapStringNodeBuilder( this );
	}
}
