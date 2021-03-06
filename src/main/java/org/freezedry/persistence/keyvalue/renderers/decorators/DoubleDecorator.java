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

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * {@link Decorator} that formats an {@link Double}. The default format is to format
 * the {@link Integer} with a decimal point and must have at least one digit to the right
 * of the decimal point, and has exactly three digits to the left of the decimal point. 
 * However, a different format may be specified.
 * 
 * @author Robert Philipp
 */
public class DoubleDecorator implements Decorator {

	private static DecimalFormat FORMATTER = new DecimalFormat( "#0.000" );
	
	private DecimalFormat formatter;
	
	/**
	 * Constructs a {@link Decorator} that formats an {@link Double}.
	 * @param formatter The format with which to format the {@link Double}.
	 */
	public DoubleDecorator( final DecimalFormat formatter )
	{
		this.formatter = formatter;
	}
	
	/**
	 * Constructs a {@link Decorator} that formats an {@link Integer} with the default
	 * format, which is a decimal point and must have at least one digit to the right
	 * of the decimal point, and has exactly three digits to the left of the decimal point.
	 */
	public DoubleDecorator()
	{
		this( FORMATTER );
	}
	
	/**
	 * Copy constructor
	 * @param decorator The {@link DoubleDecorator} to copy
	 */
	public DoubleDecorator( final DoubleDecorator decorator )
	{
		this.formatter = decorator.formatter;
	}

	/**
	 * Sets the formatter for the {@link Double}. Overwrites the default formatter
	 * @param formatter The {@link DecimalFormat} for formatting the decimal
	 */
	public void setFormatter( final DecimalFormat formatter )
	{
		this.formatter = formatter;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#decorate(java.lang.Object)
	 */
	@Override
	public String decorate( final Object object )
	{
		return object.toString();
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
		boolean isDecorated = false;
		try
		{
			formatter.parse( value );
			isDecorated = true;
		}
		catch( ParseException e ) {}
		
		return isDecorated;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#representedClass()
	 */
	@Override
	public Class< ? > representedClass()
	{
		return Double.class;
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
