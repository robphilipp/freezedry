/*
 * Copyright 2013 Robert Philipp
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

import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [Description]
 *
 * @author rphilipp
 *         10/7/13, 11:14 AM
 */
public class KeyValueMapWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger( KeyValueMapWriter.class );

	private final KeyValueFlattener keyValueFlattener;

	/**
	 * Constructs a basic key-value writer that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link org.freezedry.persistence.tree.InfoNode} and
	 * the {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create key-value pairs for
	 * {@link org.freezedry.persistence.tree.InfoNode}s that represent an array.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @see org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public KeyValueMapWriter( final Map< Class< ? >, PersistenceRenderer > renderers,
							  final PersistenceRenderer arrayRenderer,
							  final String keySeparator )
	{
		keyValueFlattener = new KeyValueFlattener( renderers, arrayRenderer, keySeparator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and specified separator.
	 * @param keySeparator The separator between the flattened elements of the key
	 */
	public KeyValueMapWriter( final String keySeparator )
	{
		keyValueFlattener = new KeyValueFlattener( keySeparator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and separator.
	 */
	public KeyValueMapWriter()
	{
		keyValueFlattener = new KeyValueFlattener();
	}

	/**
	 * Constructs a key-value writer using the specified key-value list builder
	 * @param builder The {@link org.freezedry.persistence.keyvalue.KeyValueBuilder} used to flatten the semantic model
	 */
	public KeyValueMapWriter( final KeyValueBuilder builder )
	{
		keyValueFlattener = new KeyValueFlattener( builder );
	}

	/**
	 * @return The flattener used to flatten the object into a list of key-value pairs
	 */
	public KeyValueFlattener getKeyValueFlattener()
	{
		return keyValueFlattener;
	}

	/**
	 * The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 * @param separator The separator
	 */
	public void setKeyElementSeparator( final String separator )
	{
		keyValueFlattener.setKeyElementSeparator( separator );
	}

	/**
	 * @return The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 */
	public String getKeyElementSeparator()
	{
		return keyValueFlattener.getKeyElementSeparator();
	}

	/**
	 * @return the {@link KeyValueBuilder} responsible for creating the key-value pairs
	 * from the semantic model, and that is responsible for parsing the key-value pairs into
	 * a semantic model.
	 */
	public KeyValueBuilder getBuilder()
	{
		return keyValueFlattener.getBuilder();
	}

	/**
	 * Creates a {@link Map} that contains the flattened object. The key into the map is the flattened field name, and
	 * the value is the string representation of that value.
	 * @param rootNode The root {@link InfoNode} of the semantic model
	 * @return a {@link Map} that contains the flattened object. The key into the map is the flattened field name, and
	 * the value is the string representation of that value.
	 */
	public Map< String, Object > createMap( final InfoNode rootNode )
	{
		final List< Pair< String, Object > > keyValuePairs = keyValueFlattener.buildKeyValuePairs( rootNode );

		final Map< String, Object > flattenedObject = new LinkedHashMap<>();
		for( final Pair< String, Object > pair : keyValuePairs )
		{
			flattenedObject.put( pair.getFirst(), pair.getSecond() );
		}

		if( LOGGER.isTraceEnabled() )
		{
			final StringBuilder message = new StringBuilder();
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				message.append( pair.getFirst() ).append( " = " ).append( pair.getSecond().toString() ).append( Constants.NEW_LINE );
			}
			LOGGER.trace( message.toString() );
		}
		return flattenedObject;
	}
}
