package org.freezedry.persistence.keyvalue.renderers.decorators;

public class StringDecorator implements Decorator {

	private final static String QUOTE = "\"";
	
	private String open;
	private String close;
	
	public StringDecorator( final String open, final String close )
	{
		this.open = open;
		this.close = close;
	}
	
	public StringDecorator()
	{
		this( QUOTE, QUOTE );
	}
	
	public StringDecorator( final StringDecorator decorator )
	{
		this.open = decorator.open;
		this.close = decorator.close;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#decorate(java.lang.Object)
	 */
	@Override
	public String decorate( Object object )
	{
		return open + object + close;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public StringDecorator getCopy()
	{
		return new StringDecorator( this );
	}

}
