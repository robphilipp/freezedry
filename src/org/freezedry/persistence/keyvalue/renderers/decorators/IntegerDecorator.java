package org.freezedry.persistence.keyvalue.renderers.decorators;

import java.text.DecimalFormat;

/**
 * 
 * @author rob
 */
public class IntegerDecorator implements Decorator {

	private static DecimalFormat FORMATTER = new DecimalFormat( "#0" );
	
	private DecimalFormat formatter;
	
	public IntegerDecorator( final DecimalFormat formatter )
	{
		this.formatter = formatter;
	}
	
	public IntegerDecorator()
	{
		this( FORMATTER );
	}
	
	public IntegerDecorator( final IntegerDecorator decorator )
	{
		this.formatter = decorator.formatter;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#decorate(java.lang.Object)
	 */
	@Override
	public String decorate( Object object )
	{
		return formatter.format( (Number)object );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public IntegerDecorator getCopy()
	{
		return new IntegerDecorator( this );
	}

}
