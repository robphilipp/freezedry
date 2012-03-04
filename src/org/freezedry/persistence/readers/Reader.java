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
package org.freezedry.persistence.readers;

import java.io.InputStream;

import org.freezedry.persistence.tree.InfoNode;

/**
 * Reads the format specified by the implementing class into the semantic model it creates and returns.
 * The semantic model is represented by the root {@link InfoNode} returned by the {@link #read(Class, InputStream)}
 * method.
 * 
 * @author Robert Philipp
 */
public interface Reader {

	/**
	 * Reads the input stream into a semantic model represented by the return root {@link InfoNode}.
	 * @param clazz The {@link Class} to use as a template for creating the semantic model.
	 * @param input The input stream from which to read the persisted object.
	 * @return The root {@link InfoNode} representing the semantic model.
	 */
	InfoNode read( final Class< ? > clazz, final InputStream input );
}
