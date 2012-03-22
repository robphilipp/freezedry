package org.freezedry.persistence.keyvalue.renderers.decorators;

import java.text.DecimalFormat;

public class DoubleDecorator implements Decorator {

	private static DecimalFormat FORMATTER = new DecimalFormat( "#0.000" );
	
	private DecimalFormat formatter;
	
	public DoubleDecorator( final DecimalFormat formatter )
	{
		this.formatter = formatter;
	}
	
	public DoubleDecorator()
	{
		this( FORMATTER );
	}
	
	public DoubleDecorator( final DoubleDecorator decorator )
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
	public DoubleDecorator getCopy()
	{
		return new DoubleDecorator( this );
	}

}
