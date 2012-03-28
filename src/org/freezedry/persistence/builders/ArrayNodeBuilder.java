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

import org.apache.log4j.Logger;
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
	
	private static final Logger LOGGER = Logger.getLogger( ArrayNodeBuilder.class );
	
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

	/*
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

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.generators.Generator#generateNode(java.lang.Class, java.lang.Object, java.lang.String)
	 */
	@Override
	public InfoNode createInfoNode( final Class< ? > containingClass, final Object object, final String fieldName ) throws ReflectiveOperationException
	{
		// create the InfoNode object (we first have to determine the node type, down the road, we'll check the
		// factories for registered node generators for the Class< ? > of the object)
		final Class< ? > clazz = object.getClass();

		// create a compound node that holds the child nodes that form
		// the element of the List. For each child element, call this
		// method recursively to create the appropriate node.
		String persistName = null;
		try
		{
			persistName = ReflectionUtils.getPersistenceName( containingClass.getDeclaredField( fieldName ) );
		}
		catch( ReflectiveOperationException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Field not found in containing class:" + Constants.NEW_LINE );
			message.append( "  Containing class: " + containingClass.getName() + Constants.NEW_LINE );
			message.append( "  Field name: " + fieldName + Constants.NEW_LINE );
			LOGGER.warn( message.toString(), e );
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
			final PersistArray arrayAnnotation = containingClass.getDeclaredField( fieldName ).getAnnotation( PersistArray.class );
			if( arrayAnnotation != null && !arrayAnnotation.elementPersistName().isEmpty() )
			{
				elementPersistName = arrayAnnotation.elementPersistName();
			}
		}
		catch( ReflectiveOperationException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Field not found in containing class:" + Constants.NEW_LINE );
			message.append( "  Containing class: " + containingClass.getName() + Constants.NEW_LINE );
			message.append( "  Field name: " + fieldName + Constants.NEW_LINE );
			LOGGER.warn( message.toString(), e );
		}
		
		// run through the Collection elements, recursively calling createNode(...) to create
		// the appropriate node which to add to the newly created compound node.
		for( int i = 0; i < Array.getLength( object ); ++i )
		{
			// grab the element of the array
			final Object element = Array.get( object, i );
			
			String name;
			if( elementPersistName == null )
			{
				final Class< ? > elementClazz = element.getClass();
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
			node.addChild( createNode( clazz, element, name ) );
		}
		
		return node;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.nodes.InfoNode)
	 */
	@Override
	public Object createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// TODO figure out the generics in arrays...i.e. is Person< Location >[] legal?
//		// grab the generic type parameters from the info node, and make sure there is only one
//		// (i.e. List< Double > should have java.lang.Double as the generic type)
//		// and pull out that type
//		final List< Type > types = node.getGenericParameterTypes();
//		if( types.size() != 1 )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Can only have one generic parameter in collection." + Constants.NEW_LINE );
//			message.append( "  Number of Generic Parameters: " + types.size() + Constants.NEW_LINE );
//			message.append( "  Generic Parameters: " + Constants.NEW_LINE );
//			for( Type type : types )
//			{
//				message.append( "    " + ((Class< ? >)type).getName() + Constants.NEW_LINE );
//			}
//			throw new IllegalArgumentException( message.toString() );
//		}
//		
//		// we need to get the generic types, but this isn't so simple. if the collection is a simple collection,
//		// such as a Collection< String >, then the clazz will be a string. But if element, itself, has a generic type,
//		// such as a Collection< List< String > >, then we need to pull the generic type information
//		// from the List as well. normally, we do reflection on the field to get the
//		// generic type info, but for this List, we have no field, so we need to pull it, save it,
//		// and then when we run through the nodes, we set it into the node so we have it on the next
//		// recursive call....complicated, huh? so, the types will hold any type generic information of
//		// the elements. the clazz holds the Class of the elements. note that the ParameterizedType means that
//		// it has generic type information.
//		final Pair< Class< ? >, List< Type > > elementInfo = extractTypeInfo( types.get( 0 ) );
//		final Class< ? > elementClass = elementInfo.getFirst();
//		final List< Type > elementTypes = elementInfo.getSecond();
		
		// creates the collection...
		final Object collection = createArray( clazz.getComponentType(), node.getChildCount() );

		// run through the nodes, calling the persistence engine to create the element objects
		// and add them to the newly created collection.
		int index = 0;
		for( InfoNode element : node.getChildren() ) 
		{
			// set the value into the array
			final Object object = buildObject( containingClass, clazz.getComponentType(), null, element, node );
			Array.set( collection, index, object );
			
			// increment the index counter
			++index;
		}
		
		// return the newly created and populated collection
		return collection;
	}
	
	/*
	 * Instantiates a {@link Collection} object based on the specified {@link Class}. However, if the specified {@link Class}
	 * is an interface, then it used the default concrete {@link Collection} class found in {@link #concreteCollectionClass}. 
	 * @param clazz The {@link Class} from which to build the {@link Collection}
	 * @param length The length of the array 
	 * @return The newly constructed {@link Collection}
	 * @see #setDefaultMapClass(Class)
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
		
		// instantiate
		final Object array = Array.newInstance( classType, length );
		
		// done...
		return array;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public ArrayNodeBuilder getCopy()
	{
		return new ArrayNodeBuilder( this );
	}
}
