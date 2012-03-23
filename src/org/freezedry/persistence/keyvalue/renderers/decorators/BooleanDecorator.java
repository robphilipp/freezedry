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
	 * @see org.freezedry.persistence.keyvalue.renderers.decorators.Decorator#decorate(java.lang.Object)
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
