package org.freezedry.persistence.writers.keyvalue.renderers.decorators;

public class BooleanDecorator implements Decorator {

	private final static String TRUE = "true";
	private final static String FALSE = "false";
	
	private String trueString;
	private String falseString;
	
	public BooleanDecorator( final String trueString, final String falseString )
	{
		this.trueString = trueString;
		this.falseString = falseString;
	}
	
	public BooleanDecorator()
	{
		this( TRUE, FALSE );
	}
	
	public BooleanDecorator( final BooleanDecorator decorator )
	{
		this.trueString = decorator.trueString;
		this.falseString = decorator.falseString;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.keyvalue.renderers.decorators.Decorator#decorate(java.lang.Object)
	 */
	@Override
	public String decorate( Object object )
	{
		return ((Boolean)object ? trueString : falseString );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public BooleanDecorator getCopy()
	{
		return new BooleanDecorator( this );
	}

}
