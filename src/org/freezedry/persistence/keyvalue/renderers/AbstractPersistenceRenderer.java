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

import java.util.HashMap;
import java.util.Map;

import org.freezedry.persistence.keyvalue.renderers.decorators.BooleanDecorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.DoubleDecorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.IntegerDecorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.freezedry.persistence.utils.Require;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.writers.PersistenceWriter;

/**
 * Abstract class that holds the information about the {@link KeyValueBuilder} and
 * the {@link Decorator}s.
 * 
 * @author Robert Philipp
 */
public abstract class AbstractPersistenceRenderer implements PersistenceRenderer {

	private final KeyValueBuilder builder;
	private final Map< Class< ? >, Decorator > decorators;

	/**
	 * Abstract class that holds the information about the {@link KeyValueBuilder} and the {@link Decorator}s.
	 * @param builder The {@link KeyValueBuilder} used to flatten out the semantic model into a 
	 * list of key-value pairs. This builder will call the 
	 * {@link #buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, String, java.util.List, boolean)}
	 * method of the classes implementing this class as part of a recursive algorithm to flatten the 
	 * semantic model.
	 * @param decorators The mapping between the classes and their decorators. The decorators format the
	 * strings, ints, doubles, etc. For example, by default, strings are surrounded by quotes.
	 */
	public AbstractPersistenceRenderer( final KeyValueBuilder builder, final Map< Class< ? >, Decorator > decorators )
	{
		Require.notNull( builder );
		this.builder = builder;
		this.decorators = decorators;
	}
	
	/**
	 * Constructor for the {@link PersistenceRenderer}s that stores the associated. Uses the default
	 * mapping for classes and their decorators. 
	 * {@link PersistenceWriter} needed for recursion.
	 * @param builder The associated {@link PersistenceWriter}
	 * @see Decorator
	 */
	public AbstractPersistenceRenderer( final KeyValueBuilder builder )
	{
		this( builder, createDefaultDecorators() );
	}
	
	/**
	 * Copy constructor
	 * @param renderer
	 */
	public AbstractPersistenceRenderer( final AbstractPersistenceRenderer renderer )
	{
		this.builder = renderer.builder;
		
		// make a deep copy of the decorators
		decorators = new HashMap< Class< ? >, Decorator >();
		for( final Map.Entry< Class< ? >, Decorator > entry : renderer.decorators.entrySet() )
		{
			decorators.put( entry.getKey(), entry.getValue().getCopy() );
		}
	}
	
	/*
	 * @return creates and returns the default mapping between classes and their decorators
	 */
	private static Map< Class< ? >, Decorator > createDefaultDecorators()
	{
		final Map< Class< ? >, Decorator > decorators = new HashMap<>();
		decorators.put( String.class, new StringDecorator() );

		decorators.put( Integer.class, new IntegerDecorator() );
		decorators.put( Long.class, new IntegerDecorator() );
		decorators.put( Short.class, new IntegerDecorator() );
		decorators.put( Double.class, new DoubleDecorator() );
		decorators.put( Boolean.class, new BooleanDecorator() );

		decorators.put( Integer.TYPE, new IntegerDecorator() );
		decorators.put( Long.TYPE, new IntegerDecorator() );
		decorators.put( Short.TYPE, new IntegerDecorator() );
		decorators.put( Double.TYPE, new DoubleDecorator() );
		decorators.put( Boolean.TYPE, new BooleanDecorator() );
		
		return decorators;
	}
	
	/**
	 * Adds a new {@link Decorator} for the specified class. If the specified class
	 * already had a {@link Decorator}, then overwrites that association and returns
	 * the previous {@link Decorator}. Recall that {@link Decorator}s format keys
	 * and values. For example, by default, {@link String}s are surrounded with quotes.
	 * @param clazz The class to associate with the specified {@link Decorator}
	 * @param decorator The {@link Decorator} to associated with the class.
	 * @return the {@link Decorator} previously associated with the specified {@link Class}.
	 */
	public Decorator addDecorator( final Class< ? > clazz, final Decorator decorator )
	{
		return decorators.put( clazz, decorator );
	}
	
	/**
	 * Removes the {@link Decorator} associated with the specified class.
	 * @param clazz The {@link Class} for which to remove the {@link Decorator}
	 * @return the {@link Decorator} previously associated with the specified {@link Class}
	 */
	public Decorator removeDecorator( final Class< ? > clazz )
	{
		return decorators.remove( clazz );
	}
	
	/**
	 * Finds the {@link Decorator} associated with the class. If the specified class
	 * doesn't have a decorator, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the decorator map for the
	 * specified class associating it with the returned decorator (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a decorator
	 * @return the {@link Decorator} associated with the class
	 */
	public Decorator getDecorator( final Class< ? > clazz )
	{
		return ReflectionUtils.getItemOrAncestor( clazz, decorators );
	}

	/**
	 * Finds the {@link PersistenceRenderer} associated with the class. If the specified class
	 * doesn't have a renderer, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the persistence renderer map for the
	 * specified class associating it with the returned persistence renderer (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a persistence renderer
	 * @return the true if a persistence renderer was found; false otherwise
	 */
	public boolean containsDecorator( final Class< ? > clazz )
	{
		return ( getDecorator( clazz ) != null );
	}
	
	/**
	 * @return The persistence builder associated with this renderer for use in recursion.
	 */
	protected KeyValueBuilder getPersistenceWriter()
	{
		return builder;
	}
}
