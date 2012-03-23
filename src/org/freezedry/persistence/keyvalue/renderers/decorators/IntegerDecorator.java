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
