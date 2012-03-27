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
 * Decorator that converts a boolean into a String. The default behavior is to convert
 * a boolean value of {@code true} into a {@link String} "{@code true}"; and a boolean 
 * value of {@code false} into a {@link String} "{@code false}". 
 * 
 * @author Robert Philipp
 */
public class BooleanDecorator implements Decorator {

	private final static String TRUE = "true";
	private final static String FALSE = "false";
	
	private String trueString;
	private String falseString;
	
	/**
	 * Constructs a {@link BooleanDecorator} that sets converts a boolean into a {@link String}.
	 * @param trueString The {@link String} is used for {@code true} values.
	 * @param falseString The {@link String} is used for {@code true} values.
	 */
	public BooleanDecorator( final String trueString, final String falseString )
	{
		this.trueString = trueString;
		this.falseString = falseString;
	}

	/**
	 * Constructs a {@link BooleanDecorator} that sets converts a boolean into a {@link String}.
	 * Uses the default behavior is to convert a boolean value of {@code true} into a {@link String}
	 * "{@code true}"; and a boolean value of {@code false} into a {@link String} "{@code false}".
	 */
	public BooleanDecorator()
	{
		this( TRUE, FALSE );
	}
	
	/**
	 * Copy constructor
	 * @param decorator The {@link BooleanDecorator} to copy
	 */
	public BooleanDecorator( final BooleanDecorator decorator )
	{
		this.trueString = decorator.trueString;
		this.falseString = decorator.falseString;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#decorate(java.lang.Object)
	 */
	@Override
	public String decorate( Object object )
	{
		return ((Boolean)object ? trueString : falseString );
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
			undecorated = value;
		}
		return undecorated;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#isDecorated(java.lang.String)
	 */
	@Override
	public boolean isDecorated( final String value )
	{
		return value.equals( trueString ) || value.equals( falseString );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#representedClass()
	 */
	@Override
	public Class< ? > representedClass()
	{
		return Boolean.class;
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
