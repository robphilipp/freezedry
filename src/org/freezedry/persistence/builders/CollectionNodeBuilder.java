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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.annotations.PersistCollection;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;


/**
 * Generates and {@link InfoNode} for {@link Collection}s. And {@link Collection}s
 * from {@link InfoNode}s.
 *  
 * @author Robert Philipp
 * 
 * @see AbstractNodeBuilder
 * @see NodeBuilder
 */
public class CollectionNodeBuilder extends AbstractNodeBuilder {

	private static final Logger LOGGER = Logger.getLogger( CollectionNodeBuilder.class );
	
	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Collection}s and 
	 * {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public CollectionNodeBuilder( final PersistenceEngine engine )
	{
		super( engine, createDefaultConcreteClasses() );
	}

	/**
	 * Default no-arg constructor
	 */
	public CollectionNodeBuilder()
	{
		super( createDefaultConcreteClasses() );
	}
	
	/**
	 * Copy constructor
	 * @param generator The {@link CollectionNodeBuilder} to copy
	 * @see #getCopy()
	 */
	public CollectionNodeBuilder( final CollectionNodeBuilder generator )
	{
		super( generator );
	}

	/*
	 * @return the default mapping between the {@link Collection} interfaces and their concrete classes
	 */
	private static Map< Class< ? >, Class< ? > > createDefaultConcreteClasses()
	{
		final Map< Class< ? >, Class< ? > > map = new HashMap<>();
		map.put( Collection.class, ArrayList.class );
		map.put( List.class, ArrayList.class );
		map.put( Set.class, HashSet.class );
		map.put( SortedSet.class, TreeSet.class );
		map.put( Queue.class, PriorityQueue.class );
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
			final PersistCollection collectionAnnotation = containingClass.getDeclaredField( fieldName ).getAnnotation( PersistCollection.class );
			if( collectionAnnotation != null && !collectionAnnotation.elementPersistName().isEmpty() )
			{
				elementPersistName = collectionAnnotation.elementPersistName();
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
		for( Object element : (Collection< ? >)object )
		{
			String name;
			if( elementPersistName == null )
			{
				name = element.getClass().getSimpleName();
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
	public Object createObject( final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// creates the collection...
		final Collection< ? super Object > collection = createCollection( clazz );

		// grab the generic type parameters from the info node, and make sure there is only one
		// (i.e. List< Double > should have java.lang.Double as the generic type)
		// and pull out that type
		final List< Type > types = node.getGenericParameterTypes();
		if( types.size() != 1 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Can only have one generic parameter in collection." + Constants.NEW_LINE );
			message.append( "  Number of Generic Parameters: " + types.size() + Constants.NEW_LINE );
			message.append( "  Generic Parameters: " + Constants.NEW_LINE );
			for( Type type : types )
			{
				message.append( "    " + ((Class< ? >)type).getName() + Constants.NEW_LINE );
			}
			throw new IllegalArgumentException( message.toString() );
		}
		
		// we need to get the generic types, but this isn't so simple. if the collection is a simple collection,
		// such as a Collection< String >, then the clazz will be a string. But if element, itself, has a generic type,
		// such as a Collection< List< String > >, then we need to pull the generic type information
		// from the List as well. normally, we do reflection on the field to get the
		// generic type info, but for this List, we have no field, so we need to pull it, save it,
		// and then when we run through the nodes, we set it into the node so we have it on the next
		// recursive call....complicated, huh? so, the types will hold any type generic information of
		// the elements. the clazz holds the Class of the elements. note that the ParameterizedType means that
		// it has generic type information.
		final Pair< Class< ? >, List< Type > > elementInfo = extractTypeInfo( types.get( 0 ) );
		final Class< ? > elementClass = elementInfo.getFirst();
		final List< Type > elementTypes = elementInfo.getSecond();
		
		// run through the nodes, calling the persistence engine to create the element objects
		// and add them to the newly created collection.
		for( InfoNode element : node.getChildren() ) 
		{
			final Object object = buildObject( elementClass, elementTypes, element, node );
			collection.add( object );
		}
		
		// return the newly created and populated collection
		return collection;
	}
	
	/*
	 * Instantiates a {@link Collection} object based on the specified {@link Class}. However, if the specified {@link Class}
	 * is an interface, then it used the default concrete {@link Collection} class found in {@link #concreteCollectionClass}. 
	 * @param clazz The {@link Class} from which to build the {@link Collection} 
	 * @return The newly constructed {@link Collection}
	 * @see #setDefaultMapClass(Class)
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Collection< ? super Object > createCollection( final Class< ? > clazz ) throws InstantiationException, IllegalAccessException
	{
		// make sure the class isn't an interface, and if it is, then use the default concrete map class.
		Class< ? > classType = clazz;
		if( containsInterface( clazz ) )
		{
			classType = getClassForInterface( clazz );
		}
		
		// instantiate
		@SuppressWarnings( "unchecked" )
		final Collection< ? super Object > collection = Collection.class.cast( classType.newInstance() );
		
		// done...
		return collection;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public CollectionNodeBuilder getCopy()
	{
		return new CollectionNodeBuilder( this );
	}

}
