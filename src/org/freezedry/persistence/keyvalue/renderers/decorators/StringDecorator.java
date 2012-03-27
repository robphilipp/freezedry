/*
 * Copyright 2012 Robert Philipp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.freezedry.persistence.keyvalue.renderers.decorators;


/**
 * A {@link Decorator} that surrounds a {@link String} with quotes. For example,
 * if the value of the {@link String} was {@code house} this method would return {@code "house"}.
 * The default decoration is to prepend the {@link String} with a quote ({@code "}) and append
 * a quote to the {@link String}. However, the prepended and appended {@link String} can be set
 * to other values. For example, one could surround the {@link String} with parantheses or curly-braces.
 * 
 * @author Robert Philipp
 */
public class StringDecorator implements Decorator {

	private final static String QUOTE = "\"";
	
	private String open;
	private String close;
	
	/**
	 * Constructs a {@link StringDecorator} that sets the prepending and appending {@link String}
	 * for the decoration operation. The default decoration is to prepend the {@link String} with a 
	 * quote ({@code "}) and append a quote to the {@link String}. However, the prepended and appended 
	 * {@link String} can be set to other values. For example, one could surround the {@link String} 
	 * with parantheses or curly-braces.
	 * @param open The {@link String} that is prepended to the {@link String} being decorated.
	 * @param close The {@link String} that is appended to the {@link String} being decorated.
	 */
	public StringDecorator( final String open, final String close )
	{
		this.open = open;
		this.close = close;
	}
	
	/**
	 * Constructs a {@link StringDecorator} that sets the prepending and appending {@link String}
	 * for the decoration operation. Uses the default decoration: prepend the {@link String} with a 
	 * quote ({@code "}) and append a quote to the {@link String}. 
	 */
	public StringDecorator()
	{
		this( QUOTE, QUOTE );
	}
	
	/**
	 * Copy constructor
	 * @param decorator The {@link StringDecorator} to copy
	 */
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
	public String decorate( final Object object )
	{
		return open + object + close;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#isDecorated(java.lang.String)
	 */
	@Override
	public boolean isDecorated( final String value )
	{
		return value.startsWith( open ) && value.endsWith( close ) && value.length() > 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#undecorate(java.lang.String)
	 */
	@Override
	public String undecorate( final String value )
	{
		String undecorated = null;
		if( isDecorated( value ) )
		{
			undecorated = value.replaceAll( open + "*" + close + "*", "" );
		}
		return undecorated;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#representedClass()
	 */
	@Override
	public Class< ? > representedClass()
	{
		return String.class;
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
