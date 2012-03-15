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
package org.freezedry.persistence.writers;

import java.io.PrintWriter;
import java.io.Writer;

import org.freezedry.persistence.readers.PersistenceReader;
import org.freezedry.persistence.tree.InfoNode;

/**
 * The interface for writing the semantic model to a format specified by the implementing class.
 *  
 * @author Robert Philipp
 */
public interface PersistenceWriter {
	
	/**
	 * Writes the semantic model, as represented by the root {@link InfoNode}, to the {@link PrintWriter}
	 * output stream. The implementing class determines the format with which the semantic model is written.
	 * There should also be an equivalent {@link PersistenceReader} that can read the output into the semantic model.
	 * @param rootNode The root {@link InfoNode} representing the semantic model of the object to be written.
	 * @param output The {@link Writer} output stream to which to write the semantic model.
	 */
	void write( final InfoNode rootNode, final Writer output );
}
