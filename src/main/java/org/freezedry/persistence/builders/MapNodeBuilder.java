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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.annotations.PersistMap;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;

/**
 * Handles the persistence and serialization of {@link Map} objects. When use for serialization
 * (i.e. typically when maps are root objects it is for serialization), then applies a key- and
 * value-prefix to the key's and value's respectively.
 *  
 * @author Robert Philipp
 */
public class MapNodeBuilder extends AbstractNodeBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( MapNodeBuilder.class );
	
	// for maps that are root objects, these are the key and value prefixes so that upon reconstruction
	// there is a way to know which node contains the key, and which node contains the value
	private static final String KEY_PREFIX = "key";
	private static final String VALUE_PREFIX = "value";
	private static final String KEY_VALUE_SEPARATOR = ":";
	
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
		if( containingClass != null )
		{
			try
			{
				final Field field = ReflectionUtils.getDeclaredField( containingClass, fieldName );
				persistName = ReflectionUtils.getPersistenceName( field );
			}
			catch( ReflectiveOperationException e )
			{
				LOGGER.debug( ("Field not found in containing class:" + Constants.NEW_LINE) + "  Containing class: " + containingClass.getName() + Constants.NEW_LINE + "  Field name: " + fieldName + Constants.NEW_LINE );
			}
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
			final Field field = ReflectionUtils.getDeclaredField( containingClass, fieldName );
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
			LOGGER.debug( "Field not found in containing class:" + Constants.NEW_LINE +
					"  Containing class: " + (containingClass != null ? containingClass.getName() : "[null]" ) + Constants.NEW_LINE +
					"  Field name: " + fieldName + Constants.NEW_LINE );
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
	 * @see org.freezedry.persistence.builders.NodeBuilder#createInfoNode(java.lang.Object)
	 */
	@Override
	public InfoNode createInfoNode( final Object object, final String persistName ) throws ReflectiveOperationException
	{
		// create the InfoNode object (we first have to determine the node type, down the road, we'll check the
		// factories for registered node generators for the Class< ? > of the object)
		final Class< ? > clazz = object.getClass();

		// create the root node
		final InfoNode node = InfoNode.createRootNode( clazz.getName(), clazz );

		// run through the Collection elements, recursively calling createNode(...) to create
		// the appropriate node which to add to the newly created compound node.
		// run through the Map entries, recursively calling createNode(...) to create
		// the appropriate node which to add to the newly created compound node.
		for( Map.Entry< ?, ? > entry : ((Map< ?, ? >)object).entrySet() )
		{
			// create the map entry node
			final InfoNode entryNode = InfoNode.createCompoundNode( "", entry.getClass().getSimpleName(), entry.getClass() );
			
			// create the key node and add it to the entry node
			entryNode.addChild( createNode( null, entry.getKey(), KEY_PREFIX + KEY_VALUE_SEPARATOR + entry.getKey().getClass().getName() ) );
			
			// create the value node and add it to the entry node
			entryNode.addChild( createNode( null, entry.getValue(), VALUE_PREFIX + KEY_VALUE_SEPARATOR + entry.getValue().getClass().getName() ) );
			
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
	public Object createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// creates the map...
		final Map< ? super Object, ? super Object > map = createMap( clazz );

		// grab the generic type parameters from the info node, and make sure there is only one
		// (i.e. List< Double > should have java.lang.Double as the generic type) and pull out that type
		final List< Type > types = node.getGenericParameterTypes();
		if( types.size() != 2 )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Must have two generic parameter in map (one for the key and one for the value):" ).append( Constants.NEW_LINE );
			message.append( "  Number of Generic Parameters: " ).append( types.size() ).append( Constants.NEW_LINE );
			message.append( "  Class name: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
			message.append( "  Field name: " ).append( node.getFieldName() ).append( Constants.NEW_LINE );
			message.append( "  Persist name: " ).append( node.getPersistName() ).append( Constants.NEW_LINE );
			message.append( "  Generic Parameters: " ).append( Constants.NEW_LINE );
			for( Type type : types )
			{
				message.append( "    " ).append( ((Class<?>) type).getName() ).append( Constants.NEW_LINE );
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
				
		// grab the names that the semantic model uses to represent the key and value.
		final Pair< String, String > keyValueNames = getKeyValueNames( containingClass, node );
		String keyPersistenceName = keyValueNames.getFirst();
		String valuePersistenceName = keyValueNames.getSecond();
		
		// run through the nodes, calling the persistence engine to create the element objects
		// and add them to the newly created map. each info node should have an entry node, and
		// each entry node should have a key node and a value node.
		for( InfoNode entryNode : node.getChildren() )
		{
			final List< InfoNode > keyValue = entryNode.getChildren();
			if( keyValue.size() != 2 )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "The info node for this map must have two nodes. Aw snap! But it doesn't" ).append( Constants.NEW_LINE );
				message.append( "  Number of nodes: " ).append( keyValue.size() ).append( Constants.NEW_LINE );
				message.append( "  Node names: " ).append( Constants.NEW_LINE );
				for( InfoNode childNode : keyValue )
				{
					message.append( "    " ).append( childNode.getPersistName() ).append( Constants.NEW_LINE );
				}
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
			
			// the order of the elements can be reversed. For example, the key could be the second
			// element (instead of the first) and the value could be the first. We check both possibilities
			Object key = null;
			Object value = null;
			final InfoNode firstNode = keyValue.get( 0 );
			final InfoNode secondNode = keyValue.get( 1 );
			if( keyPersistenceName.equals( firstNode.getPersistName() ) &&
				valuePersistenceName.equals( secondNode.getPersistName() )	)
			{
				key = buildObject( containingClass, keyClass, keyTypes, firstNode, node );
				value = buildObject( containingClass, valueClass, valueTypes, secondNode, node );
			}
			else if( keyPersistenceName.equals( secondNode.getPersistName() ) &&
					 valuePersistenceName.equals( firstNode.getPersistName() ) )
			{
				key = buildObject( containingClass, keyClass, keyTypes, secondNode, node );
				value = buildObject( containingClass, valueClass, valueTypes, firstNode, node );
			}
			
			// add the new objects to the map
			map.put( key, value );
		}
		
		// return the newly created and populated collection
		return map;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.InfoNode)
	 */
	@Override
	public Object createObject( final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException
	{
		// creates the map...
		final Map< ? super Object, ? super Object > map = createMap( clazz );

		// run through the nodes, calling the persistence engine to create the element objects
		// and add them to the newly created map. each info node should have an entry node, and
		// each entry node should have a key node and a value node.
		for( InfoNode entryNode : node.getChildren() )
		{
			final List< InfoNode > keyValue = entryNode.getChildren();
			if( keyValue.size() != 2 )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "The info node for this map must have two nodes. But snap! It doesn't" ).append( Constants.NEW_LINE );
				message.append( "  Number of nodes: " ).append( keyValue.size() ).append( Constants.NEW_LINE );
				message.append( "  Node names: " ).append( Constants.NEW_LINE );
				for( InfoNode childNode : keyValue )
				{
					message.append( "    " ).append( childNode.getPersistName() ).append( Constants.NEW_LINE );
				}
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
			
			// the order of the elements can be reversed. For example, the key could be the second
			// element (instead of the first) and the value could be the first. We check both possibilities
			final InfoNode firstNode = keyValue.get( 0 );
			final InfoNode secondNode = keyValue.get( 1 );
			Pair< Object, Object > keyValuePair = null;
			if( firstNode.getPersistName().startsWith( KEY_PREFIX + KEY_VALUE_SEPARATOR ) && 
				secondNode.getPersistName().startsWith( VALUE_PREFIX + KEY_VALUE_SEPARATOR ) )
			{
				keyValuePair = getKeyValuePair( firstNode, secondNode, node );
			}
			else if( firstNode.getPersistName().startsWith( VALUE_PREFIX + KEY_VALUE_SEPARATOR ) && 
					 secondNode.getPersistName().startsWith( KEY_PREFIX + KEY_VALUE_SEPARATOR ) )
			{
				keyValuePair = getKeyValuePair( secondNode, firstNode, node );
			}

			// add the new objects to the map
			if( keyValuePair != null )
			{
				map.put( keyValuePair.getFirst(), keyValuePair.getSecond() );
			}
		}
		
		// return the newly created and populated collection
		return map;
	}
	
	/**
	 * Creates a key-value pair containing the object built from the key node and value node.  
	 * @param keyNode The node containing the key
	 * @param valueNode The node containing the value
	 * @param parentNode The parent node that contains both the key and value nodes
	 * @return A key-value pair containing the object built from the key node and value node.
	 * @throws ReflectiveOperationException
	 */
	private Pair< Object, Object > getKeyValuePair( final InfoNode keyNode, final InfoNode valueNode, final InfoNode parentNode ) throws ReflectiveOperationException
	{
		final Class< ? > keyClass = Class.forName( keyNode.getPersistName().split( Pattern.quote( KEY_VALUE_SEPARATOR ) )[ 1 ] );
		final List< Type > keyTypes = Arrays.asList( (Type)keyClass );
		final Object key = buildObject( null, keyClass, keyTypes, keyNode, parentNode );

		final Class< ? > valueClass = Class.forName( valueNode.getPersistName().split( Pattern.quote( KEY_VALUE_SEPARATOR ) )[ 1 ] );
		final List< Type > valueTypes = Arrays.asList( (Type)valueClass );
		final Object value = buildObject( null, valueClass, valueTypes, valueNode, parentNode );
		
		return new Pair<>( key, value );
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
	
	/**
	 * Returns the key and value names for the {@link Map}. These are either default values, or, if
	 * the field has a {@link PersistMap} annotation, it may have overridden either the key or value name.
	 * For example, suppose the user has annotated the field with 
	 * {@code @PersistMap(entryPersistName="client",keyPersistName="endPoint",valuePersistName="weight")}.
	 * Then the info node of the children would contain "endpoint" instead of the expected "Key" for the
	 * key name, and "weight" instead of the expected "Value" for the value. And the we need to know that
	 * so we can deal with it.
	 * @param containingClass The {@link Class} containing the field represented by the specified node
	 * @param node The node representing the field
	 * @return The key and value names associated with the {@link Map}
	 */
	private Pair< String, String > getKeyValueNames( final Class< ? > containingClass, final InfoNode node )
	{
		// create the pair containing the default key and value names
		final Pair< String, String > keyValue = new Pair<>( PersistMap.KEY_PERSIST_NAME, PersistMap.VALUE_PERSIST_NAME );
		
		// attempt to grab the field with the node's persistence name (in this case this would be the
		// field name)
		final String persistName = node.getPersistName();
		Field field;
		try
		{
			field = ReflectionUtils.getDeclaredField( containingClass, persistName );
		}
		catch( NoSuchFieldException e )
		{
			field = ReflectionUtils.getFieldForPersistenceName( containingClass, node.getPersistName() );
		}
		
		// if a field was found, then see if that field has an annotation, and if that annotation overrides
		// the key name and/or the value name
		if( field != null )
		{
			final PersistMap mapAnnotation = field.getAnnotation( PersistMap.class );
			if( mapAnnotation != null )
			{
				final String keyName = mapAnnotation.keyPersistName();
				if( keyName != null && !keyName.isEmpty() )
				{
					keyValue.setFirst( keyName );
				}
				
				final String valueName = mapAnnotation.valuePersistName();
				if( valueName != null && !valueName.isEmpty() )
				{
					keyValue.setSecond( valueName );
				}
			}
		}
		
		return keyValue;
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
