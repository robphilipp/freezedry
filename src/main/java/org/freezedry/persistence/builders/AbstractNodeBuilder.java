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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;


/**
 * Abstract implementation of the {@link NodeBuilder} interface. Abstract class for the classes used 
 * to generate {@link InfoNode}s. These {@link NodeBuilder}s are used by the {@link PersistenceEngine} 
 * to create {@link InfoNode} from {@link Object} and vice-versa. These {@link NodeBuilder}s must fit 
 * in with the recursive method used by the {@link PersistenceEngine#createNode(Class, Object, String)}
 * method.
 * 
 * This class does two things:
 * <ul>
 * 	<li>Manages the reference to the {@link PersistenceEngine}</li>
 * 	<li>Provides a convenience method {@link #createNode(Class, Object, String)} that simply
 * 		calls the {@link PersistenceEngine#createNode(Class, Object, String)} method.</li>
 * </ul>
 * 
 * @author Robert Philipp
 *
 * @see NodeBuilder
 */
public abstract class AbstractNodeBuilder implements NodeBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractNodeBuilder.class );
	
	private PersistenceEngine persistanceEngine;

	private Map< Class< ? >, Class< ? > > interfaceToClassMap;
	
	/**
	 * Constructs the {@link AbstractNodeBuilder} with a reference to the {@link PersistenceEngine}
	 * it will use for creating {@link InfoNode}s.
	 * @param engine The {@link PersistenceEngine}
	 * @param interfaceToClassMapping The mapping between interfaces and classes to instantiate
	 */
	public AbstractNodeBuilder( final PersistenceEngine engine, final Map< Class< ? >, Class< ? > > interfaceToClassMapping )
	{
		this.persistanceEngine = engine;
		this.interfaceToClassMap = interfaceToClassMapping;
	}
	
	/**
	 * Constructs the {@link AbstractNodeBuilder} with a reference to the {@link PersistenceEngine}
	 * it will use for creating {@link InfoNode}s.
	 * @param interfaceToClassMapping The mapping between interfaces and classes to instantiate
	 */
	public AbstractNodeBuilder( final Map< Class< ? >, Class< ? > > interfaceToClassMapping )
	{
		this.interfaceToClassMap = interfaceToClassMapping;
	}
	
	/**
	 * Default no-arg constructor
	 */
	public AbstractNodeBuilder()
	{
	}
	
	/**
	 * Copy constructor
	 * @param generator the {@link AbstractGeneratorGenerator} to copy
	 */
	public AbstractNodeBuilder( final AbstractNodeBuilder generator )
	{
		this( generator.persistanceEngine, generator.interfaceToClassMap );
	}

	/**
	 * @return The {@link PersistenceEngine} used by this {@link NodeBuilder}
	 */
	public PersistenceEngine getPersistenceEngine()
	{
		return persistanceEngine;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.generators.Generator#setPersistenceEngine(org.freezedry.persistence.PersistenceEngine)
	 */
	@Override
	public void setPersistenceEngine( final PersistenceEngine engine )
	{
		this.persistanceEngine = engine;
	}
	
	/**
	 * Returns true if the interface-to-class mapping contains the specified interface; false otherwise
	 * @param clazz The interface to check
	 * @return true if the interface-to-class mapping contains the specified interface; false otherwise
	 */
	public boolean containsInterface( final Class< ? > clazz )
	{
		boolean contains = false;
		if( interfaceToClassMap != null && interfaceToClassMap.containsKey( clazz ) )
		{
			contains = true;
		}
		return contains;
	}
	
	/**
	 * Adds and interface to class mapping used when constructing a collection.
	 * @param interfaceClass The interface {@link Class}
	 * @param concreteClass The concrete {@link Class} to use to instantiate the specified interface
	 */
	public final void addInterfaceToClassMapping( final Class< ? > interfaceClass, final Class< ? > concreteClass )
	{
		if( !concreteClass.isInterface() && interfaceClass.isInterface() )
		{
			interfaceToClassMap.put( interfaceClass, concreteClass );
		}
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Specified class cannot be an interface." + Constants.NEW_LINE );
			message.append( "  Specified Class: " + concreteClass.getName() );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Returns the concrete class for the specified interface. Or null if it doesn't exist
	 * @param clazz The interface for which to return the concrete class
	 * @return the concrete class for the specified interface. Or null if it doesn't exist
	 */
	public Class< ? > getClassForInterface( final Class< ? > clazz )
	{
		Class< ? > concreteClass = null;
		if( interfaceToClassMap != null )
		{
			concreteClass = interfaceToClassMap.get( clazz );
		}
		return concreteClass;
	}
	
	/**
	 * @return The mapping between the interfaces and the concrete classes that will be used.
	 */
	public final Map< String, String > getInterfaceToClassMapping()
	{
		final Map< String, String > mapping = new LinkedHashMap<>();
		for( Map.Entry< Class< ? >, Class< ? > > entry : interfaceToClassMap.entrySet() )
		{
			mapping.put( entry.getKey().getName(), entry.getValue().getName() );
		}
		return mapping;
	}

	/**
	 * Creates an {@link InfoNode} by calling back to {@link PersistenceEngine#createNode(Class, Object, String)}. This allows
	 * the recursion to continue, and removes the responsibility of working through the object hierarchy from the implementation
	 * of node generator.
	 * @param containingClass The {@link Class} that contains the field of the specified name, and value
	 * @param object The value of the field with the specified name
	 * @param fieldName The name of the field
	 * @return An {@link InfoNode} representing the field.
	 * @throws ReflectiveOperationException
	 */
	protected InfoNode createNode( final Class< ? > containingClass, final Object object, final String fieldName ) throws ReflectiveOperationException
	{
		return persistanceEngine.createNode( containingClass, object, fieldName );
	}
	
	/**
	 * Extracts the type information from the specified {@link Type}. Recall that the type could
	 * be a {@link Class} or a {@link ParameterizedType}. In the latter case, we must also
	 * extract the type information of the {@link ParameterizedType}, since it may also 
	 * have generic parameters. And in either case, we must return the {@link Class} associated
	 * with the specified {@link Type}
	 * @param type The {@link Type} from which to get the {@link Class} and the parameterized information.
	 * @return the {@link Class} associated with the {@link Type} and any type information associated
	 * with the {@link Type} if it is an instance of the {@link ParameterizedType}
	 */
	protected Pair< Class< ? >, List< Type > > extractTypeInfo( final Type type )
	{
		List< Type > types = null;
		Class< ? > clazz = null;
		if( type instanceof ParameterizedType )
		{
			final ParameterizedType parameterizedType = (ParameterizedType)type;
			types = Arrays.asList( parameterizedType.getActualTypeArguments() );
			clazz = (Class< ? >)parameterizedType.getRawType();
		}
		else
		{
			clazz = (Class< ? >)type;
		}
		return new Pair< Class< ? >, List< Type > >( clazz, types );
	}
	
	/**
	 * Builds the most specific object for the specified {@link Class} and {@link InfoNode}. For example,
	 * if the specified {@link Class} is {@link Map} and the {@link InfoNode} contains a {@link Class} 
	 * {@link LinkedHashMap}, then it will create a {@link LinkedHashMap}. Also updates the node with any
	 * additional generic type information it will need for further processing. 
	 * @param containingClass The class containing the field to instantiate.
	 * @param clazz The specified {@link Class} to instantiate.
	 * @param types The generic type information
	 * @param node The {@link InfoNode} associated with the specified {@link Class}
	 * @param parentNode The parent node to the specified {@link InfoNode}
	 * @return The newly created object
	 * @throws ReflectiveOperationException
	 */
	protected Object buildObject( final Class< ? > containingClass,
								  final Class< ? > clazz, 
								  final List< Type > types, 
								  final InfoNode node, 
								  final InfoNode parentNode ) throws ReflectiveOperationException
	{
		// grab the node and then get the most specific class from either the keyClass or the
		// one found in the infoNode, and that is the one we'll instantiate
		final Class< ? > specificClazz = ReflectionUtils.getMostSpecificClass( clazz, node );
		
		// if there are keyTypes, then we know that we have generic type information (which we
		// stored above--see the really long comment) that we need to add to the node.
		updateNode( node, types, parentNode );
		
		// create the new object
		final Object key = getPersistenceEngine().createObject( containingClass, specificClazz, node );
		
		return key;
	}
	
	/**
	 * Updates the specified node with information that will be need for downstream processing. This is
	 * mostly needed when the {@link Map} contains other classes that have generic types. For example, when
	 * a {@code Map< String, Map< String, Double > >} is processed, then we supplement the node holding
	 * the {@code Map< String, Double >} with type information, the name of the outer {@link Map}'s field,
	 * and any information about the type to instantiate based on the outer {@link Map}'s annotations.
	 * @param node The {@link InfoNode} to supplement.
	 * @param types The type information of the inner generic class
	 * @param parentNode The parent to the specified {@link InfoNode}
	 */
	protected void updateNode( final InfoNode node, final List< Type > types, final InfoNode parentNode )
	{
		if( types != null && !types.isEmpty() )
		{
			// supplement the node with its generic type information
			node.setGenericParameterTypes( types );
		}
	}

}
