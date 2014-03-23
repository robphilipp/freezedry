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
package org.freezedry.persistence.builders;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.annotations.PersistArray;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;

/**
 * Generates and {@link InfoNode} for arrays. And arrays
 * from {@link InfoNode}s.
 *  
 * @author Robert Philipp
 * 
 * @see AbstractNodeBuilder
 * @see NodeBuilder
 */
public class ArrayNodeBuilder extends AbstractNodeBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( ArrayNodeBuilder.class );
	
	public static final String COMPOUND_ARRAY_NAME_SUFFIX = "Array";
	
	private String compoundArrayNameSuffix = COMPOUND_ARRAY_NAME_SUFFIX;
	
	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Collection}s and 
	 * {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public ArrayNodeBuilder( final PersistenceEngine engine )
	{
		super( engine, createDefaultConcreteClasses() );
	}

	/**
	 * Default no-arg constructor
	 */
	public ArrayNodeBuilder()
	{
		super( createDefaultConcreteClasses() );
	}
	
	/**
	 * Copy constructor
	 * @param generator The {@link ArrayNodeBuilder} to copy
	 * @see #getCopy()
	 */
	public ArrayNodeBuilder( final ArrayNodeBuilder generator )
	{
		super( generator );
	}
	
	/**
	 * Sets the suffix for replacing the "{@code []}" when the element of an array is itself an
	 * array (or an array of arrays). For example, for an {@code int[][]} called {@code matrix}
	 * the name of the array of {@code int[]} has the persist and field name {@code matrix}. But
	 * the actual elements (which are arrays) in this case would have the name {@code int[]}. And
	 * for XML and JSON, this is a problem. So by default, the name for the elements is converted
	 * from {@code int[]} to {@code intArray}. Using this method, you can change the "{@code Array}"
	 * suffix to any valid string suffix you like.
	 * @param suffix The suffix the replaces the "{@code []}" for compound arrays.
	 */
	public void setCompoundArrayNameSuffix( final String suffix )
	{
		this.compoundArrayNameSuffix = suffix;
	}
	
	/**
	 * Returns the suffix for replacing the "{@code []}" when the element of an array is itself an
	 * array (or an array of arrays). For example, for an {@code int[][]} called {@code matrix}
	 * the name of the array of {@code int[]} has the persist and field name {@code matrix}. But
	 * the actual elements (which are arrays) in this case would have the name {@code int[]}. And
	 * for XML and JSON, this is a problem. So by default, the name for the elements is converted
	 * from {@code int[]} to {@code intArray}.
	 * @return The suffix the replaces the "{@code []}" for compound arrays.
	 */
	public String getCompoundArrayNameSuffix()
	{
		return compoundArrayNameSuffix;
	}

	/**
	 * @return the default mapping between the {@link Collection} interfaces and their concrete classes
	 */
	private static Map< Class< ? >, Class< ? > > createDefaultConcreteClasses()
	{
		final Map< Class< ? >, Class< ? > > map = new HashMap<>();
		
		// basic collections
		map.put( Collection.class, ArrayList.class );
		map.put( List.class, ArrayList.class );
		map.put( Set.class, HashSet.class );
		map.put( SortedSet.class, TreeSet.class );
		map.put( Queue.class, PriorityQueue.class );
		
		// map
		map.put( Map.class, LinkedHashMap.class );
		
		// number
		map.put( Number.class, Double.class );
		
		return map;
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. The specified containing {@link Class}
	 * is the {@link Class} in which the specified field name lives. And the object is the value of
	 * the field name.
	 * @param containingClass The {@link Class} that contains the specified field name
	 * @param object The value of the field with the specified field name
	 * @param fieldName The name of the field for which the object is the value
	 * @return The constructed {@link InfoNode} based on the specified information
	 * @throws ReflectiveOperationException
	 */
	@Override
	public InfoNode createInfoNode( final Class< ? > containingClass, final Object object, final String fieldName ) throws ReflectiveOperationException
	{
		// create the InfoNode object (we first have to determine the node type, down the road, we'll check the
		// factories for registered node generators for the Class< ? > of the object)
		final Class< ? > clazz = object.getClass();

		// create a compound node that holds the child nodes that form the element of the List. For each child element, call this
		// method recursively to create the appropriate node. If the containing class is null (i.e. for int[], String[], etc) then
		// we use the specified field name.
		String persistName = null;
		if( containingClass != null && !containingClass.isArray() )
		{
			try
			{
				final Field field = ReflectionUtils.getDeclaredField( containingClass, fieldName );
				persistName = ReflectionUtils.getPersistenceName( field );
			}
			catch( ReflectiveOperationException e )
			{
				LOGGER.warn( "Field not found in containing class:" + Constants.NEW_LINE +
						"  Containing class: " + containingClass.getName() + Constants.NEW_LINE +
						"  Field name: " + fieldName + Constants.NEW_LINE, e );
			}
		}
		if( persistName == null || persistName.isEmpty() )
		{
			persistName = fieldName;
		}
		final InfoNode node = InfoNode.createCompoundNode( fieldName, persistName, clazz );
		
		// grab the annotations for this field and see if the persist name is specified
		// does the class have a @PersistCollection( elementPersistName = "xxxx" )
		String elementPersistName = null;
		try
		{
			// grab the array annotation if the containing class isn't null. If the containing class is null,
			// then later in the code we set the name for which to persist the elements to the classes simple
			// name with the compound array name suffix
			PersistArray arrayAnnotation = null;
			if( containingClass != null && !containingClass.isArray() )
			{
				final Field field = ReflectionUtils.getDeclaredField( containingClass, fieldName );
				arrayAnnotation = field.getAnnotation( PersistArray.class );
			}
			if( arrayAnnotation != null && !arrayAnnotation.elementPersistName().isEmpty() )
			{
				elementPersistName = arrayAnnotation.elementPersistName();
			}
		}
		catch( ReflectiveOperationException e )
		{
			LOGGER.warn( "Field not found in containing class:" + Constants.NEW_LINE +
					"  Containing class: " + containingClass.getName() + Constants.NEW_LINE +
					"  Field name: " + fieldName + Constants.NEW_LINE, e );
		}
		
		// run through the Collection elements, recursively calling createNode(...) to create
		// the appropriate node which to add to the newly created compound node.
		for( int i = 0; i < Array.getLength( object ); ++i )
		{
			// grab the element of the array
			final Class< ? > elementClazz = object.getClass().getComponentType();
			
			String name;
			if( elementPersistName == null )
			{
				name = elementClazz.getSimpleName();
				if( elementClazz.isArray() )
				{
					name = name.replaceAll( "\\[\\]", compoundArrayNameSuffix );
				}
			}
			else
			{
				name = elementPersistName;
			}
			
			// grab the element and create the node. however, because the element may be a primitive
			// we need to set the node's class type to the actual element node. if we don't do this
			// then, for example, all ints will become Integers and if we ask for the type to be 
			// set within the node, the type will be incorrect
			final Object element = Array.get( object, i );
			final InfoNode elementNode = createNode( clazz, element, name );
			elementNode.setClazz( elementClazz );
			
			// add the new node as a child to the parent
			node.addChild( elementNode );
		}
		
		return node;
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. This method is used for objects that have
	 * an overriding node builder and are not contained within a class. For example, suppose you would like
	 * to persist an {@link ArrayList} for serialization and would like to maintain the type information.
	 * @param object The value of the field with the specified field name
	 * @return The constructed {@link InfoNode} based on the specified information
	 * @throws ReflectiveOperationException
	 */
	@Override
	public InfoNode createInfoNode( final Object object, final String persistName ) throws ReflectiveOperationException
	{
		// create the InfoNode object (we first have to determine the node type, down the road, we'll check the
		// factories for registered node generators for the Class< ? > of the object)
		final Class< ? > clazz = object.getClass();

		// create the compound name
		final InfoNode node = InfoNode.createCompoundNode( persistName, persistName, clazz );
		
		// run through the Collection elements, recursively calling createNode(...) to create
		// the appropriate node which to add to the newly created compound node.
		for( int i = 0; i < Array.getLength( object ); ++i )
		{
			// grab the array's element type, and then its fully qualified name
			final Class< ? > elementClazz = object.getClass().getComponentType();
			final String name = elementClazz.getSimpleName();
			
			// grab the element and create the node. however, because the element may be a primitive
			// we need to set the node's class type to the actual element node. if we don't do this
			// then, for example, all ints will become Integers and if we ask for the type to be 
			// set within the node, the type will be incorrect
			final Object element = Array.get( object, i );
			final InfoNode elementNode = createNode( null, element, name );
			elementNode.setClazz( elementClazz );
			
			// add the new node as a child to the parent
			node.addChild( elementNode );
		}
		
		return node;
	}

	/**
	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}. Note that
	 * the {@link org.freezedry.persistence.tree.InfoNode} may also contain type information about the class to generate. The specified {@link Class}
	 * overrides that value. This is done to avoid modifying the {@link org.freezedry.persistence.tree.InfoNode} tree when supplemental information becomes
	 * available.
	 * @param containingClass The {@link Class} containing the clazz, represented by the {@link InfoNode}
	 * @param clazz The {@link Class} of the object to create
	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
	 */
	@Override
	public Object createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// creates the collection...
		final Object collection = createArray( clazz.getComponentType(), node.getChildCount() );

		// run through the nodes, calling the persistence engine to create the element objects
		// and add them to the newly created collection.
		int index = 0;
		for( InfoNode element : node.getChildren() ) 
		{
			Object object;
			if( element.getClazz() != null && element.getClazz().isArray() )
			{
				object = createObject( containingClass, element.getClazz(), element );
			}
			else
			{
				// set the value into the array
				object = buildObject( containingClass, clazz.getComponentType(), null, element, node );
			}
			Array.set( collection, index, object );
			
			// increment the index counter
			++index;
		}
		
		// return the newly created and populated collection
		return collection;
	}

	/**
	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}.
	 * This method is used for objects that have an overriding node builder and are not contained within a
	 * class. For example, suppose you would like to persist an {@link ArrayList} for serialization and would
	 * like to maintain the type information.
	 * @param clazz The {@link Class} of the object to create
	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
	 * @throws ReflectiveOperationException
	 */
	@Override
	public Object createObject( Class< ? > clazz, InfoNode node ) throws ReflectiveOperationException
	{
		return createObject( null, clazz, node );
	}

	/**
	 * Instantiates an array object based on the specified {@link Class}.
	 * @param clazz The {@link Class} from which to build the array
	 * @param length The length of the array 
	 * @return The newly constructed array
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Object createArray( final Class< ? > clazz, final int length ) throws InstantiationException, IllegalAccessException
	{
		// make sure the class isn't an interface, and if it is, then use the default concrete map class.
		Class< ? > classType = clazz;
		if( containsInterface( clazz ) )
		{
			classType = getClassForInterface( clazz );
		}
		
		// done...
		return Array.newInstance( classType, length );
	}

	/**
	 * Creates and returns a copy of the object <code>x</code> that meets the following criteria
	 * <ol>
	 * 	<li>The expressions <code>x.getCopy() != x</code> evaluates as <code>true</code></li>
	 * 	<li>The expressions <code>x.getCopy().equals( x )</code> evaluates as <code>true</code></li>
	 * 	<li>The expressions <code>x.getCopy().getClass() == x.getClass()</code> evaluates as <code>true</code></li>
	 * </ol>
	 * @return a copy of the object that meets the above criteria
	 */
	@Override
	public ArrayNodeBuilder getCopy()
	{
		return new ArrayNodeBuilder( this );
	}
}
