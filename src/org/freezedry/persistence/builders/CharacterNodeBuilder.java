package org.freezedry.persistence.builders;

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;

public class CharacterNodeBuilder extends AbstractPrimitiveLeafNodeBuilder {

	/**
	 * Constructs the {@link NodeBuilder} for going between primitives, their wrappers, {@link Character}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public CharacterNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public CharacterNodeBuilder()
	{
		super();
	}
	
	/**
	 * Copy constructor
	 * @param builder
	 */
	public CharacterNodeBuilder( final CharacterNodeBuilder builder )
	{
		super( builder );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.nodes.InfoNode)
	 */
	@Override
	public Character createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node )
	{
		// grab the node's value, which should be a string
		final String value = (String)node.getValue();
		
		// the string should only have 1 character, but may at times have zero chars, which 
		// represents a space. So we'll start it out as a space, and update/check accordingly
		Character character = ' ';
		if( value.length() == 1 )
		{
			character = value.charAt( 0 );
		}
		else if( value.length() > 1 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The value in the info node must be a character." + Constants.NEW_LINE );
			message.append( "  InfoNode value: " + node.getValue() + Constants.NEW_LINE );
			throw new IllegalArgumentException( message.toString() );
		}
		return character;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.AbstractLeafNodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.InfoNode)
	 */
	@Override
	public Character createObject( final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// grab the child node's value
		final Object nodeValue = node.getChild( 0 ).getValue();
		
		// here it is a bit complicated. recall that this method is called for root nodes, and so
		// value seems to jump between Character and String
		Character value = null;
		if( nodeValue instanceof Character )
		{
			value = (Character)nodeValue;
		}
		else
		{
			value = ((String)nodeValue).charAt( 0 );
		}
		return value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public CharacterNodeBuilder getCopy()
	{
		return new CharacterNodeBuilder( this );
	}
}
