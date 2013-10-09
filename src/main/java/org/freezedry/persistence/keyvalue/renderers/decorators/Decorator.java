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

import org.freezedry.persistence.copyable.Copyable;

/**
 * Interface defining what a {@link Decorator} should look like. The purpose of a
 * {@link Decorator} is to take an object, convert it to a String of some format, decorate it with
 * something, and return it. For example, a {@link StringDecorator} takes a {@link String} and
 * surrounds it with quotes (default behavior).
 * 
 * @author Robert Philipp
 */
public interface Decorator extends Copyable< Decorator > {

	/**
	 * Converts the specified {@link Object} to a {@link String} of some format, decorates it with
	 * something, and returns it. For example, a {@link StringDecorator} takes a {@link String} and
	 * surrounds it with quotes (default behavior). So if the value of the {@link String} was {@code house}
	 * this method would return {@code "house"}.
	 * @param object The object to decorate
	 * @return The decorated object
	 */
	String decorate( final Object object );
	
	/**
	 * Removes any decoration from the object. For example, if the value has been decorated as a {@link String},
	 * then it removes the quotation marks (or whatever is used for the decoration of the {@link String}). If 
	 * the specified value was has not been decorated in a manner consistent with the {@link Decorator} implementation,
	 * then the undecorated value return is {@code null}.
	 * @param value The value from which to remove decorations
	 * @return The undecorated value
	 */
	String undecorate( final String value );
	
	/**
	 * Returns true if the specified value matches the formatting used to decorate the object; false otherwise
	 * @param value The value to test to see if it is decorated according to this class.
	 * @return true if the specified value matches the formatting used to decorate the object; false otherwise
	 */
	boolean isDecorated( final String value );
	
	/**
	 * @return The {@link Class} represented by this decorator. For example, the {@link StringDecorator} represents
	 * the {@link String} class.
	 */
	Class< ? > representedClass();
}
