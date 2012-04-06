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
package org.freezedry.persistence;

import java.io.Reader;
import java.io.Writer;

/**
 * Interface that defines the methods for a {@link Persistence}. Implementing subclasses provide the
 * specifics of the persistence. For example, an XML persistence would provide a mechanism that persists
 * object to XML streams or files.<p>
 * 
 * The {@link #read(Class, Reader)} method is intended to read a persisted object from the {@link Reader} 
 * stream into an object of the specified {@link Class}. And the {@link #write(Object, Writer)} is intended
 * to persist the specified object to the {@link Writer} stream. 
 * 
 * @author Robert Philipp
 */
public interface Persistence {

	/**
	 * @return The {@link PersistenceEngine} that is used to convert an object to a semantic mode
	 *  and convert a semantic model into an object.
	 */
	PersistenceEngine getPersistenceEngine();
	
	/**
	 * Writes the specified object's persisted form to the specified {@link Writer}. The subclass implementations
	 * determine the objects persisted form. For example, they could be XML, JSON, lists of key-value pairs,
	 * or some other implementation.
	 * @param object The object to convert to its persisted form and written to the specified {@link Writer}.
	 * @param writer The {@link Writer} used to which to write the persisted form.
	 */
	void write( final Object object, final Writer writer );
	
	/**
	 * Reads the persisted form of an object of the specified {@link Class} from the {@link Reader} into an
	 * object of the specified {@link Class}. The subclass implementations determine the objects persisted form. 
	 * For example, they could be XML, JSON, lists of key-value pairs, or some other implementation. 
	 * @param clazz The {@link Class} of the object represented by the persisted form.
	 * @param reader The {@link Reader} from which to read the persisted form of the object
	 * @return the reconstituted object
	 */
	< T > T read( final Class< T > clazz, final Reader reader );
}
