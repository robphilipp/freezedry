/*
 * Copyright 2013 Robert Philipp, InvestLab Technology LLC
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
package org.freezedry.difference;

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.writers.KeyValueMapWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Calculates the difference between two object of the same type and lists the flattened properties that differ. Also
 * provides access to flatten objects into key-value pairs.
 *
 * @author rphilipp
 *         10/7/13, 1:27 PM
 */
public class ObjectDifferenceCalculator {

	private static Logger LOGGER = LoggerFactory.getLogger( ObjectDifferenceCalculator.class );

	private static Pattern LIST_PATTERN = Pattern.compile( "(.)*(\\[[\\d]+\\])+(.)*" );

	private final KeyValueMapWriter mapWriter;
	private final PersistenceEngine persistenceEngine = new PersistenceEngine().withPersistNullValues();
	private boolean isListOrderIgnored = false;

	/**
	 * Constructs a basic key-value writer that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link org.freezedry.persistence.tree.InfoNode} and
	 * the {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create key-value pairs for
	 * {@link org.freezedry.persistence.tree.InfoNode}s that represent an array.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @see org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public ObjectDifferenceCalculator( final Map<Class<?>, PersistenceRenderer> renderers,
									   final PersistenceRenderer arrayRenderer,
									   final String keySeparator )
	{
		mapWriter = new KeyValueMapWriter( renderers, arrayRenderer, keySeparator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and specified separator.
	 * @param keySeparator The separator between the flattened elements of the key
	 */
	public ObjectDifferenceCalculator( final String keySeparator )
	{
		mapWriter = new KeyValueMapWriter( keySeparator );
	}

	/**
	 * Constructs a basic key-value writer that uses the default renderers and separator.
	 */
	public ObjectDifferenceCalculator()
	{
		mapWriter = new KeyValueMapWriter();
	}

	/**
	 * Constructs a key-value writer using the specified key-value list builder
	 * @param builder The {@link org.freezedry.persistence.keyvalue.KeyValueBuilder} used to flatten the semantic model
	 */
	public ObjectDifferenceCalculator( final KeyValueBuilder builder )
	{
		mapWriter = new KeyValueMapWriter( builder );
	}

	public ObjectDifferenceCalculator listOrderIgnored()
	{
		this.isListOrderIgnored = true;
		return this;
	}

	public ObjectDifferenceCalculator listOrderMatters()
	{
		this.isListOrderIgnored = false;
		return this;
	}

	/**
	 * If set to {@code true} then tells the {@link org.freezedry.persistence.PersistenceEngine} to persist null values.
	 * By default the {@link org.freezedry.persistence.PersistenceEngine} does not persist null values.
	 * @param isPersistNullValues Whether or not to persist fields that have null values
	 */
	public void setPersistNullValues( final boolean isPersistNullValues )
	{
		persistenceEngine.setPersistNullValues( isPersistNullValues );
	}

	/**
	 * Calculates the difference between the two specified objects and returns the difference as key-value pairs. The
	 * {@link Difference} object holds the value of the {@code object}'s property and the {@code referenceObjects}'s property
	 * that differ.
	 * @param modifiedObject The new object to compare against the reference object
	 * @param referenceObject The reference object against which to compare the object
	 * @param <T> The object's and the reference object's type.
	 * @return A {@link Map} that holds the flattened property names of all the properties that are different between the
	 * object and the reference object. For each entry in the map, the {@link Difference} holds the object's property
	 * value and the reference object's property value
	 */
	public final < T > Map< String, Difference > calculateDifference( final T modifiedObject, final T referenceObject )
	{
		final Map< String, Object > objectMap = flattenObject( modifiedObject );
		final Map< String, Object > referenceObjectMap = flattenObject( referenceObject );

		final Set< String > keys = new LinkedHashSet<>();
		keys.addAll( objectMap.keySet() );
		keys.addAll( referenceObjectMap.keySet() );

		final Map< String, Difference > difference = new LinkedHashMap<>();
		for( String key : keys )
		{
			final Object modifiedValue = objectMap.get( key );
			final Object referenceValue = referenceObjectMap.get( key );
			if( ( modifiedValue != null && referenceValue != null && !modifiedValue.toString().equals( referenceValue.toString() ) ) ||
				( modifiedValue != null && referenceValue == null ) ||
				( modifiedValue == null && referenceValue != null ) )
			{
				difference.put( key, new Difference( modifiedValue, referenceValue ) );
			}
		}

		if( isListOrderIgnored )
		{
			ignoreListOrder( difference );
		}

		return difference;
	}

	/**
	 * Flattens the object into key-value pairs.
	 * @param object The object to flatten
	 * @return The flattened version of the object
	 * @see KeyValueBuilder
	 */
	public final Map< String, Object > flattenObject( final Object object )
	{
		final InfoNode rootNode = persistenceEngine.createSemanticModel( object );
		return mapWriter.createMap( rootNode );
	}

	/**
	 * Removes differences where the difference is only due to the place within a list the difference occurs.
	 * @param differences A map holding differences between two objects and the keys representing the flattened field names
	 * @return The map with differences removed that are only due to their placement in a list
	 */
	private Map< String, Difference > ignoreListOrder( final Map< String, Difference > differences )
	{
		final Map< String, Map< String, Difference > > classifications = classifyLists( differences );
		return differences;
	}

	/**
	 * Extracts and returns any {@link org.freezedry.difference.ObjectDifferenceCalculator.Difference} objects that
	 * represent a list or array. Whether the {@link org.freezedry.difference.ObjectDifferenceCalculator.Difference}
	 * represents a list is determined by the regular expression {@link #LIST_PATTERN}.
	 * @param differences The map of differences and their associated, flattened field name
	 * @return T
	 */
	private Map< String, Difference > extractLists( final Map< String, Difference > differences )
	{
		final Map< String, Difference > lists = new HashMap<>();
		for( Map.Entry< String, Difference > entry : differences.entrySet() )
		{
			if( LIST_PATTERN.matcher( entry.getKey() ).matches() )
			{
				lists.put( entry.getKey(), entry.getValue() );
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.warn( entry.getKey() + "->" + entry.getValue() );
				}
			}
		}
		return lists;
	}

	/**
	 * Classifies the lists into groups representing the same field
	 * @param differences
	 * @return
	 */
	private Map< String, Map< String, Difference > > classifyLists( final Map< String, Difference > differences )
	{
		final Map< Pattern, Map< String, Difference > > mainGroups = new LinkedHashMap<>();

		// finds the list-patterns and classifies each of the field names accordingly
		for( Map.Entry< String, Difference > entry : differences.entrySet() )
		{
			// for keys that fit the list pattern, we classify them in groups that represent the same fields
			if( LIST_PATTERN.matcher( entry.getKey() ).matches() )
			{
				// see if any of the patterns match the current key, if not, then we add the new pattern to the map
				// along with the key and its value
				boolean isClassified = false;
				for( Map.Entry< Pattern, Map< String, Difference > > classEntry : mainGroups.entrySet() )
				{
					if( classEntry.getKey().matcher( entry.getKey() ).matches() )
					{
						classEntry.getValue().put( entry.getKey(), entry.getValue() );
						isClassified = true;
						break;
					}
				}
				if( !isClassified )
				{
					final Map< String, Difference > difference = new HashMap<>();
					difference.put( entry.getKey(), entry.getValue() );
					final Pattern pattern = Pattern.compile( entry.getKey().replaceAll( "\\[[\\d]+\\]", "\\\\[[\\\\d]+\\\\]" ) );
					mainGroups.put( pattern, difference );
				}
			}
		}

		// convert the indexes for the regex patterns (the group names) to "[*]"
		final Map< String, Map< String, Difference > > groups = new HashMap<>( mainGroups.size() );
		for( Map.Entry< Pattern, Map< String, Difference > > entry : mainGroups.entrySet() )
		{
			final String key = entry.getKey().pattern().replaceAll( Pattern.quote( "\\[[\\d]+\\]" ), "[*]" );
			groups.put( key, entry.getValue() );
		}
		return groups;
	}

	private Pattern createClassifier( final String flattenedField )
	{
		return Pattern.compile( flattenedField.replaceAll( "\\[[\\d]+\\]", "\\\\[[\\\\d]+\\\\]" ) );
	}

	/**
	 * Class representing the difference between the object's and the reference object's value.
	 */
	public final static class Difference {

		private final Object object;
		private final Object referenceObject;

		/**
		 * The holds the value of a property of the object and the reference object
		 * @param modifiedObject The value of the object's property
		 * @param referenceObject The value of the reference object's property
		 */
		public Difference( final Object modifiedObject, final Object referenceObject )
		{
			this.object = modifiedObject;
			this.referenceObject = referenceObject;
		}

		/**
		 * @return The object's value
		 */
		public Object getObject()
		{
			return object;
		}

		/**
		 * @return The reference object's value
		 */
		public Object getReferenceObject()
		{
			return referenceObject;
		}

		/**
		 * @return a string representation of the difference
		 */
		@Override
		public String toString()
		{
			return "Modified Object: " + (object == null ? "[null]" : object.toString()) + "; " +
					"Reference Object: " + (referenceObject == null ? "[null]" : referenceObject.toString());
		}
	}
}
