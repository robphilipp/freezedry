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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.annotations.PersistMap;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;


public class MapNodeBuilder extends AbstractNodeBuilder {
	
	private static final Logger LOGGER = Logger.getLogger( MapNodeBuilder.class );
	
	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Map}s and 
	 * {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public MapNodeBuilder( PersistenceEngine engine )
	{
		super( engine, createDefaultConcreteClasses() );
	}

	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Map}s and 
	 * {@link Object}s.
	 */
	public MapNodeBuilder()
	{
		super( createDefaultConcreteClasses() );
	}

	/**
	 * Copy constructor
	 * @param generator The {@link MapNodeBuilder} to copy
	 * @see #getCopy()
	 */
	public MapNodeBuilder( final MapNodeBuilder generator )
	{
		super( generator );
	}
	
	/*
	 * @return the default mapping between the {@link Collection} interfaces and their concrete classes
	 */
	private static Map< Class< ? >, Class< ? > > createDefaultConcreteClasses()
	{
		final Map< Class< ? >, Class< ? > > map = new HashMap<>();
		map.put( Map.class, LinkedHashMap.class );
		return map;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.NodeBuilder#createInfoNode(java.lang.Class, java.lang.Object, java.lang.String)
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
		
		// set the entry, key, and value persistence names to the default value of the annotation. if the
		// map field is annotated, then we over write the default values with the annotated values.
		// does the class have a @PersistMap( keyPersistName = "xxxx", valuePersistName = "yyyy", entryPeristName = "zzzz" )
		String entryPersistName = PersistMap.ENTRY_PERSIST_NAME;
		String keyPersistName = PersistMap.KEY_PERSIST_NAME;
		String valuePersistName = PersistMap.VALUE_PERSIST_NAME;
		try
		{
			final Field field = containingClass.getDeclaredField( fieldName );
			final PersistMap mapAnnotation = field.getAnnotation( PersistMap.class );
			if( mapAnnotation != null )
			{
				if( !mapAnnotation.entryPersistName().isEmpty() )
				{
					entryPersistName = mapAnnotation.entryPersistName();
				}
				if( !mapAnnotation.keyPersistName().isEmpty() )
				{
					keyPersistName = mapAnnotation.keyPersistName();
				}
				if( !mapAnnotation.valuePersistName().isEmpty() )
				{
					valuePersistName = mapAnnotation.valuePersistName();
				}
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
		
		// run through the Map entries, recursively calling createNode(...) to create
		// the appropriate node which to add to the newly created compound node.
		for( Map.Entry< ?, ? > entry : ((Map< ?, ? >)object).entrySet() )
		{
			// create the map entry node
			final InfoNode entryNode = InfoNode.createCompoundNode( "", entryPersistName, entry.getClass() );
			
			// create the key node and add it to the entry node
			entryNode.addChild( createNode( clazz, entry.getKey(), keyPersistName ) );
			
			// create the value node and add it to the entry node
			entryNode.addChild( createNode( clazz, entry.getValue(), valuePersistName ) );
			
			// add the entry node to the info node representing the map
			node.addChild( entryNode );
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
		// creates the map...
		final Map< ? super Object, ? super Object > map = createMap( clazz );

		// grab the generic type parameters from the info node, and make sure there is only one
		// (i.e. List< Double > should have java.lang.Double as the generic type) and pull out that type
		final List< Type > types = node.getGenericParameterTypes();
		if( types.size() != 2 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Must have two generic parameter in map (one for the key and one for the value):" + Constants.NEW_LINE );
			message.append( "  Number of Generic Parameters: " + types.size() + Constants.NEW_LINE );
			message.append( "  Class name: " + clazz.getName() + Constants.NEW_LINE );
			message.append( "  Field name: " + node.getFieldName() + Constants.NEW_LINE );
			message.append( "  Persist name: " + node.getPersistName() + Constants.NEW_LINE );
			message.append( "  Generic Parameters: " + Constants.NEW_LINE );
			for( Type type : types )
			{
				message.append( "    " + ((Class< ? >)type).getName() + Constants.NEW_LINE );
			}
			throw new IllegalArgumentException( message.toString() );
		}
		
		// we need to get the generic types, but this isn't so simple. if the map is a simple map,
		// such as a Map< String, String >, then the keyClass will be a string and the valueClass
		// will by a string. But if either the key or the value, themselves, have a generic type,
		// such as a Map< String, Map< String, String > >, then we need to pull the generic type information
		// from the second map as well as the first. normally, we do reflection on the field to get the
		// generic type info, but for this inner map, we have no field, so we need to pull it, save it,
		// and then when we run through the nodes, we set it into the node so we have it on the next
		// recursive call....complicated, huh? so, the keyTypes will hold any type generic information of
		// the key. the keyClass holds the Class of the key. note that the ParameterizedType means that
		// it has generic type information.
		final Pair< Class< ? >, List< Type > > keyInfo = extractTypeInfo( types.get( 0 ) );
		final Class< ? > keyClass = keyInfo.getFirst();
		final List< Type > keyTypes = keyInfo.getSecond();
		
		// same as for the key
		final Pair< Class< ? >, List< Type > > valueInfo = extractTypeInfo( types.get( 1 ) );
		final Class< ? > valueClass = valueInfo.getFirst();
		final List< Type > valueTypes = valueInfo.getSecond();
		
		// run through the nodes, calling the persistence engine to create the element objects
		// and add them to the newly created map. each info node should have an entry node, and
		// each entry node should have a key node and a value node.
		for( InfoNode entryNode : node.getChildren() )
		{
			final List< InfoNode > keyValue = entryNode.getChildren();
			if( keyValue.size() != 2 )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "The info node for this map must have two nodes. But snap! It doesn't" + Constants.NEW_LINE );
				message.append( "  Number of nodes: " + keyValue.size() + Constants.NEW_LINE );
				message.append( "  Node names: " + Constants.NEW_LINE );
				for( InfoNode childNode : keyValue )
				{
					message.append( "    " + childNode.getPersistName() + Constants.NEW_LINE );
				}
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
			
			// TODO, find the annotations to map these nodes to their key and value
			Object key = null;
			Object value = null;
			final InfoNode firstNode = keyValue.get( 0 );
			final InfoNode secondNode = keyValue.get( 1 );
			if( PersistMap.KEY_PERSIST_NAME.equals( firstNode.getPersistName() ) &&
				PersistMap.VALUE_PERSIST_NAME.equals( secondNode.getPersistName() )	)
			{
				key = buildObject( keyClass, keyTypes, firstNode, node );
				value = buildObject( valueClass, valueTypes, secondNode, node );
			}
			else if( PersistMap.KEY_PERSIST_NAME.equals( secondNode.getPersistName() ) &&
					 PersistMap.VALUE_PERSIST_NAME.equals( firstNode.getPersistName() ) )
			{
				key = buildObject( keyClass, keyTypes, secondNode, node );
				value = buildObject( valueClass, valueTypes, firstNode, node );
			}
			
			// add the new objects to the map
			map.put( key, value );
		}
		
		// return the newly created and populated collection
		return map;
	}
	
	/*
	 * Instantiates a {@link Map} object based on the specified {@link Class}. However, if the specified {@link Class}
	 * is an interface, then it used the default concrete {@link Map} class found in {@link #concreteMapClass}. 
	 * @param clazz The {@link Class} from which to build the {@link Map} 
	 * @return The newly constructed {@link Map}
	 * @see #setDefaultMapClass(Class)
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Map< ? super Object, ? super Object > createMap( final Class< ? > clazz ) throws InstantiationException, IllegalAccessException
	{
		// make sure the class isn't an interface, and if it is, then use the default concrete map class.
		Class< ? > classType = clazz;
		if( containsInterface( clazz ) )
		{
			classType = getClassForInterface( clazz );
		}
		
		// instantiate
		@SuppressWarnings( "unchecked" )
		final Map< ? super Object, ? super Object > map = Map.class.cast( classType.newInstance() );
		
		// done...
		return map;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public NodeBuilder getCopy()
	{
		return new MapNodeBuilder( this );
	}

}
