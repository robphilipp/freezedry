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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Calculates the difference between two object of the same type and lists the flattened properties that differ. Also
 * provides access to flatten objects into key-value pairs.
 *
 * @author rphilipp
 *         10/7/13, 1:27 PM
 */
public class ObjectDifferenceCalculator {

	private final KeyValueMapWriter mapWriter;
	private final PersistenceEngine persistenceEngine = new PersistenceEngine();

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

	/**
	 * Calculates the difference between the two specified objects and returns the difference as key-value pairs. The
	 * {@link Difference} object holds the value of the {@code object}'s property and the {@code referenceObjects}'s property
	 * that differ.
	 * @param object The new object to compare against the reference object
	 * @param referenceObject The reference object against which to compare the object
	 * @param <T> The object's and the reference object's type.
	 * @return A {@link Map} that holds the flattened property names of all the properties that are different between the
	 * object and the reference object. For each entry in the map, the {@link Difference} holds the object's property
	 * value and the reference object's property value
	 */
	public final < T > Map< String, Difference > calculateDifference( final T object, final T referenceObject )
	{
		final Map< String, Object > objectMap = flattenObject( object );
		final Map< String, Object > referenceObjectMap = flattenObject( referenceObject );

		final Set< String > keys = new LinkedHashSet<>();
		keys.addAll( objectMap.keySet() );
		keys.addAll( referenceObjectMap.keySet() );

		final Map< String, Difference > difference = new LinkedHashMap<>();
		for( String key : keys )
		{
			final Object newValue = objectMap.get( key );
			final Object oldValue = referenceObjectMap.get( key );
			if( ( newValue != null && oldValue != null && !newValue.toString().equals( oldValue.toString() ) ) ||
				( newValue != null && oldValue == null ) ||
				( newValue == null && oldValue != null ) )
			{
				difference.put( key, new Difference( newValue, oldValue ) );
			}
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
	 * Class representing the difference between the object's and the reference object's value.
	 */
	public final static class Difference {

		private final Object object;
		private final Object referenceObject;

		/**
		 * The holds the value of a property of the object and the reference object
		 * @param object The value of the object's property
		 * @param referenceObject The value of the reference object's property
		 */
		public Difference( final Object object, final Object referenceObject )
		{
			this.object = object;
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
			final StringBuilder builder = new StringBuilder();
			builder.append( "Object: " ).append( object == null ? "[null]" : object.toString() ).append( "; " )
					.append( "Reference Object: " ).append( referenceObject == null ? "[null]" : referenceObject.toString() );
			return builder.toString();
		}
	}
}
