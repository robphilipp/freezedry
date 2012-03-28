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
package org.freezedry.persistence.keyvalue;

import java.util.List;

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;

/**
 * Interface that defines what a {@link KeyValueBuilder} builder should perform. The {@link KeyValueBuilder}
 * takes a semantic model and flattens the structure into a {@link List} of key-value pairs. The specifics
 * of how the keys and values are represented is determined by the implementing classes.
 *   
 * @author Robert Philipp
 */
public interface KeyValueBuilder {

	/**
	 * The entry point for building the {@link List} of key-value pairs from the semantic model,
	 * through a recursive algorithm ({@link #buildKeyValuePairs(InfoNode, String, List)}. Effectively,
	 * this will flatten the tree into a list of key-value pairs.
	 * @param rootInfoNode The root {@link InfoNode} of the semantic model
	 * @return the {@link List} of key-value pairs 
	 * @see #buildKeyValuePairs(InfoNode, String, List)
	 */
	List< Pair< String, Object > > buildKeyValuePairs( final InfoNode rootInfoNode );
	
	/**
	 * The recursive algorithm for flattening the semantic model into a {@link List} of key-value pairs.
	 * @param infoNode The current node in the semantic model ({@link InfoNode}) for processing. 
	 * @param key The current key, which has accumulated the parents persistence names as part of the flattening
	 * @param keyValues The current list of key-values to which to add the ones created in this algorithm.
	 */
	void buildKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues );
	
	/**
	 * Creates the actual key-value pair, though for compound nodes, it calls back its calling method, 
	 * {@link #buildKeyValuePairs(InfoNode, String, List)}.
	 * @param infoNode The current node in the semantic model ({@link InfoNode}) for processing. 
	 * @param key The current key, which has accumulated the parents persistence names as part of the flattening
	 * @param keyValues The current list of key-values to which to add the ones created in this algorithm.
	 * @param isWithholdPersitName true if the current persistence name should not be added to the key; false otherwise.
	 * This parameter allows {@link PersistenceRenderer}s to suppress the persistence name when it is appropriate. For
	 * example, in a {@link List} of {@link String}, you may not want to at "{@code String}" to the key.
	 */
	void createKeyValuePairs( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersitName );
	
	/**
	 * @return The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 */
	String getSeparator();
	
	/**
	 * Returns the renderer for the specified {@link Class} or null if the specified {@link Class}
	 * doesn't have an associated renderer. The returned renderer will either be the renderer associated
	 * with:
	 * <ul>
	 * 	<li>the specified {@link Class}</li>
	 * 	<li>the renderer associated with the closest ancestor</li>
	 * 	<li>null if neither of the previous two are found</li>
	 * </ul>
	 * @param clazz The {@link Class} for which to return the renderer.
	 * @return the renderer for the specified {@link Class} or null if the specified {@link Class}.
	 */
	PersistenceRenderer getRenderer( final Class< ? > clazz );
	
	/**
	 * The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 * @param separator The separator
	 */
	void setSeparator( final String separator );

	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link List} would have a key of the form {@code names[i].String}
	 * @param isShowFullKey true means that the full key will be persisted; false is default
	 */
	void setShowFullKey( final boolean isShowFullKey );
	
	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link List} would have a key of the form {@code names[i].String}
	 * @return true means that the full key will be persisted; false is default
	 */
	boolean isShowFullKey();
	
	/**
	 * Main entry point for building the semantic model from a list of key-value pairs and the target class.
	 * @param clazz The target class for the building of the semantic model
	 * @param keyValues The key-value pairs
	 * @return The root {@link InfoNode} of the semantic model
	 */
	InfoNode buildInfoNode( final Class< ? > clazz, final List< Pair< String, String > > keyValues );
	
	/**
	 * Recursively builds the semantic model. The keys in the key-value list should all have as their
	 * first element, the name found in the parentNode's persistence name.
	 * @param parentNode The node to which to add the child nodes
	 * @param keyValues The list of key-value pairs. The first key element of every key should match
	 * the persistence name of the parent node.
	 * @return the new info node
	 */
	void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues );

	/**
	 * Creates an {@link InfoNode} based on the group name and the specified key-value pairs. Part of the 
	 * recursive algorithm to build the semantic model.
	 * @param parentNode The node to which to add the child nodes
	 * @param groupName The name of the group that will appear as the persistence name
	 * @param keyValues The list of key-value pairs. The first key element of every key should match
	 * the persistence name of the parent node.
	 * @see #buildInfoNode(InfoNode, List)
	 */
	void createInfoNode( final InfoNode parentNode, final String groupName, final List< Pair< String, String > > keyValues );
	
	/**
	 * Returns the root key. If the {@code useClassAsRootKey} default was set via the constructor or the {@link #setRootKey(String)}
	 * method, then it returns that root key. Otherwise, it pulls the first key element from each key, ensures that
	 * they are all the same, and then returns that root key.
	 * @param keyValues The list of key-value pairs
	 * @return the root key.
	 */
	String getRootKey( final List< Pair< String, String > > keyValues, final Class< ? > clazz );
}
