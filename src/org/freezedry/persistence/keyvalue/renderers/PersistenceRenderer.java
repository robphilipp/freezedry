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
package org.freezedry.persistence.keyvalue.renderers;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.copyable.Copyable;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.tree.InfoNode;

/**
 * Interface defining what a {@link PersistenceRenderer} should look like. The {@link PersistenceRenderer}s
 * are intended to be used to create key-value pairs during the flattening of the semantic model. The 
 * {@link PersistenceRenderer}s are intended to be part of the recursive algorithm in which the {@link KeyValueBuilder}s
 * call the {@link #buildKeyValuePair(InfoNode, String, List, boolean)} method, which in turn, for compound
 * {@link InfoNode}s may call the {@link KeyValueBuilder#buildKeyValuePairs(InfoNode, String, List)} or 
 * the {@link KeyValueBuilder#createKeyValuePairs(InfoNode, String, List, boolean)} methods. 
 * 
 * It ends up being a beautiful dance of between the builders and renderers...though the code isn't so pretty.
 * 
 * @author Robert Philipp
 */
public interface PersistenceRenderer extends Copyable< PersistenceRenderer >{

	/**
	 * Builds a key-value pair and adds it to the list of key-value pairs. If the
	 * {@link InfoNode} is compound, then the {@link PersistenceRenderer} may refer back to 
	 * the {@link KeyValueBuilder} to build out the compound node.
	 * @param infoNode The current {@link InfoNode} in the semantic model.
	 * @param key The current key. The key is constructed by appending persistence names, which
	 * may or may not be decorated, to the current key. In this way it represents a flattening
	 * of the semantic model.
	 * @param keyValues The current list of key-value pairs
	 * @param isWithholdPersistName true if the renderer implementation should not append the
	 * {@link InfoNode}'s persistence name to the key.
	 */
	void buildKeyValuePair( final InfoNode infoNode, 
							final String key, 
							final List< Pair< String, Object > > keyValues,
							final boolean isWithholdPersistName );

	/**
	 * Returns true if the specified key matches the pattern created by the renderer; false otherwise
	 * @param keyElement The element of the key to test
	 * @return true if the specified key matches the pattern created by the renderer; false otherwise
	 */
	boolean isRenderer( final String keyElement );
}
