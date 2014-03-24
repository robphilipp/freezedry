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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Writes the semantic model as a list of key-value pairs to the specified output stream.
 * The writer uses a {@link KeyValueBuilder} that flattens the semantic model and returns 
 * a list of key-value pairs. The {@link KeyValueBuilder} can be specified, or a default one is created
 * based on the supplied specifications for the {@link KeyValueBuilder}. 
 * 
 * @author Robert Philipp
 */
public class KeyValueWriter implements PersistenceWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger( KeyValueWriter.class );

	public static final String KEY_VALUE_SEPARATOR = "=";

	private final KeyValueFlattener keyValueFlattener;

	private String keyValueSeparator;

	/**
	 * Constructs a basic key-value writer that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link InfoNode} and
	 * the {@link PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link PersistenceRenderer} used to create key-value pairs for
	 * {@link InfoNode}s that represent an array.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @param keyValueSeparator The separator between the key and the value
	 * @see AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public KeyValueWriter( final Map< Class< ? >, PersistenceRenderer > renderers, 
						   final PersistenceRenderer arrayRenderer,
						   final String keySeparator,
						   final String keyValueSeparator )
	{
		keyValueFlattener = new KeyValueFlattener( renderers, arrayRenderer, keySeparator );
		this.keyValueSeparator = keyValueSeparator;
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and specified separator.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @param keyValueSeparator The separator between the key and the value
	 */
	public KeyValueWriter( final String keySeparator, final String keyValueSeparator )
	{
		keyValueFlattener = new KeyValueFlattener( keySeparator );
		this.keyValueSeparator = keyValueSeparator;
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and separator.
	 */
	public KeyValueWriter()
	{
		keyValueFlattener = new KeyValueFlattener();
	}
	
	/**
	 * Constructs a key-value writer using the specified key-value list builder
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model
	 */
	public KeyValueWriter( final KeyValueBuilder builder )
	{
		keyValueFlattener = new KeyValueFlattener( builder );
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
		keyValueFlattener.getBuilder().setSeparator( separator );
	}

	/**
	 * @return The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 */
	public String getKeyElementSeparator()
	{
		return keyValueFlattener.getBuilder().getSeparator();
	}

	/**
	 * @param separator The separator between the key and the value. The default value is given by the
	 * {@link #KEY_VALUE_SEPARATOR} which has a value of {@value #KEY_VALUE_SEPARATOR}.
	 */
	public void setKeyValueSeparator( final String separator )
	{
		keyValueSeparator = separator;
	}

	/**
	 * @return The separator between the key and the value.
	 */
	public String getKeyValueSeparator()
	{
		return keyValueSeparator;
	}

	/**
	 * Sets the builder responsible for creating the key-value pairs from the semantic model,
	 * and that is responsible for parsing the key-value pairs into a semantic model.
	 * @param builder the {@link KeyValueBuilder} responsible for creating the key-value pairs
	 * from the semantic model, and that is responsible for parsing the key-value pairs into
	 * a semantic model.
	 */
	public void setBuilder( final KeyValueBuilder builder )
	{
		keyValueFlattener.setBuilder( builder );
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

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.PersistenceWriter#write(org.freezedry.persistence.tree.InfoNode, java.io.Writer)
	 */
	@Override
	public void write( final InfoNode rootNode, final Writer output )
	{
		final List< Pair< String, Object > > keyValuePairs = keyValueFlattener.buildKeyValuePairs( rootNode );
		try
		{
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				final String str = pair.getFirst() + " = " + pair.getSecond().toString() + Constants.NEW_LINE;
				output.write( str );
			}
		}
		catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
		
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuilder message = new StringBuilder();
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				message.append( pair.getFirst() ).append( " = " ).append( pair.getSecond().toString() ).append( Constants.NEW_LINE );
			}
			LOGGER.info( message.toString() );
		}
	}
}
