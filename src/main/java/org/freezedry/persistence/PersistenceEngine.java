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
package org.freezedry.persistence;

import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.builders.*;
import org.freezedry.persistence.readers.PersistenceReader;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.freezedry.persistence.writers.PersistenceWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Creates a semantic model of the specified {@link Object}, and creates a specified {@link Object} form
 * a specified semantic model. The semantic model is a tree that holds the information about each element 
 * (field) in a {@link InfoNode}. The {@link InfoNode} information is a complete set of information needed 
 * to persist and reconstruct the specified {@link Object}.<p>
 * 
 * The {@link PersistenceEngine} allows you to specify {@link NodeBuilder}s that are responsible for converting
 * an object into the semantic model, and a semantic model back into an object. By default, a set of node builders
 * are created and added to the {@link PersistenceEngine} at construction. However, through the 
 * {@link #addNodeBuilder(Class, NodeBuilder)} method, you can specify which node builder to use for which class.
 * For example, for and instance of the {@link PersistenceEngine}, {@code engine} you can map a {@link Map} to a 
 * {@link MapNodeBuilder}, so that all {@link Map} objects use the {@link MapNodeBuilder} to convert between the 
 * semantic model and an object, by<p>
 * {@code engine.addNodeBuilder( Map.class, new MapNodeBuilder() );}<p>
 * Importantly, in this case, the {@link PersistenceEngine} will use the {@link MapNodeBuilder} for all objects that 
 * implement {@link Map}. That means that a {@link LinkedHashMap} will also use {@link MapNodeBuilder}. Suppose you
 * want to have the {@link LinkedHashMap} used a different {@link NodeBuilder}, say {@code LinkedMapNodeBuilder} (which
 * doesn't exist, but you could write one), then you would perform the mapping as above:<p>
 * {@code engine.addNodeBuilder( LinkedHashMap.class, new LinkedMapNodeBuilder() );}<p>
 * And now, {@link HashMap} would use the {@link MapNodeBuilder}, but {@link LinkedHashMap} would use the 
 * {@code LinkedMapNodeBuilder}.<p>
 * 
 * To customize the behavior of any {@link NodeBuilder} you write, you may also define annotations that works in
 * conjunction with your {@link NodeBuilder}. For example, the {@link org.freezedry.persistence.annotations.PersistCollection} annotation works in
 * conjunction with the {@link CollectionNodeBuilder}. The field annotation argument {@code elementPersistName}
 * allows you to define the name that is used to persist each element, and the {@code elementType} allows you to define
 * the type ({@link Class}) of each element. Or as another example, the {@link org.freezedry.persistence.annotations.PersistDateAs} annotation allows
 * you to define the format with which a date is persisted.
 * 
 * By default, class constants (i.e. static final fields) are not persisted. Use the {@link #setPersistClassConstants(boolean)}
 * to change the default behavior. Note that class constants are not set when reconstructing the class from the persisted
 * form. In other words, the class constants always take their value from the class source code value.
 * 
 * When parsing the semantic model, this class will attempt to instantiate an object of the specified class by calling the 
 * constructor with the smallest number of arguments. If a no-argument constructor exists, then that is what will be called.
 * Constructors that have arguments will be passed null objects, and then the fields will be set reflectively through the 
 * {@link Field#set(Object, Object)} method. This means that if the constructor performs checks against null, you will have 
 * a problem. (TODO allow passing default values to the constructor so that the instantiation passes its checks).
 * 
 * @see InfoNode
 * @see NodeBuilder
 * @see PersistenceWriter
 * @see PersistenceReader
 * @see Persist
 * 
 * @author Robert Philipp
 */
public class PersistenceEngine {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( PersistenceEngine.class );

	// todo make this a configuration that can be passed in
	public static final String GENERIC_TYPE_SEPARATOR = "___";
	
	private static final Map< Class< ? >, Object > PRIMITIVE_TYPES = createPrimitives();
	private static final Set< Class< ? > > NON_ROOT_OBJECTS = nonRootObjects();
	
	private final Map< Class< ? >, NodeBuilder > nodeBuilders;
	private ArrayNodeBuilder genaralArrayNodeBuilder;
	private EnumNodeBuilder generalEnumNodeBuilder;
	private boolean isPersistClassConstants = false;
	private boolean isPersistNullValues = false;
	
	private final Map< Class< ? >, Object > defaultInstances;

	private String genericTypeSeparator = GENERIC_TYPE_SEPARATOR;
	
	/**
	 * Constructs a {@link PersistenceEngine} with the default {@link InfoNode} info node builders
	 */
	public PersistenceEngine()
	{
		this.nodeBuilders = createDefaultNodeBuilders();
		this.genaralArrayNodeBuilder = new ArrayNodeBuilder( this );
		this.generalEnumNodeBuilder = new EnumNodeBuilder( this );
		this.defaultInstances = createDefaultInstances();
	}
	
	/**
	 * @return a {@link Map} containing the {@link Class}es and their associated {@link InfoNode} {@link Generator}
	 */
	private Map< Class< ? >, NodeBuilder > createDefaultNodeBuilders()
	{
		// the map holding the class-to-info node builder mapping
		final Map< Class< ? >, NodeBuilder > builders = new LinkedHashMap<>();
		
		// primitive node info node builders (these are intended to create leaf nodes)
		builders.put( Integer.TYPE, new IntegerNodeBuilder( this ) );
		builders.put( Double.TYPE, new DoubleNodeBuilder( this ) );
		builders.put( Float.TYPE, new FloatNodeBuilder( this ) );
		builders.put( Long.TYPE, new LongNodeBuilder( this ) );
		builders.put( Short.TYPE, new ShortNodeBuilder( this ) );
		builders.put( Boolean.TYPE, new BooleanNodeBuilder( this ) );
		builders.put( Character.TYPE, new CharacterNodeBuilder( this ) );
		builders.put( Byte.TYPE, new ByteNodeBuilder( this ) );
		
		builders.put( Integer.class, new IntegerNodeBuilder( this ) );
		builders.put( Double.class, new DoubleNodeBuilder( this ) );
		builders.put( Float.class, new FloatNodeBuilder( this ) );
		builders.put( Long.class, new LongNodeBuilder( this ) );
		builders.put( Short.class, new ShortNodeBuilder( this ) );
		builders.put( Boolean.class, new BooleanNodeBuilder( this ) );
		builders.put( Character.class, new CharacterNodeBuilder( this ) );
		builders.put( Byte.class, new ByteNodeBuilder( this ) );
		builders.put( String.class, new StringNodeBuilder( this ) );
		
		// other leaf nodes
		builders.put( Calendar.class, new DateNodeBuilder( this ) );
		
		// collection info node builder (specific info node builder)
		builders.put( Collection.class, new CollectionNodeBuilder( this ) );
		
		// map info node builder  (specific info node builder)
		builders.put( Map.class, new MapNodeBuilder( this ) );
		
		// 
		return builders;
	}
	
	/**
	 * @return a map containing a primitive type and the wrapper object used to represent that primitive type
	 */
	private static Map< Class< ? >, Object > createPrimitives()
	{
		final Map< Class< ? >, Object > primitives = new HashMap<>();
		primitives.put( Integer.TYPE, 0 );
		primitives.put( Long.TYPE, (long) 0 );
		primitives.put( Short.TYPE, Short.MAX_VALUE );
		primitives.put( Double.TYPE, 0.0 );
		primitives.put( Float.TYPE, new Float( 0.0 ) );
		primitives.put( Boolean.TYPE, true );
		primitives.put( Byte.TYPE, Byte.MAX_VALUE );
		primitives.put( Character.TYPE, '0' );
		return primitives;
	}
	
	/**
	 * @return a {@link Set} containing the primitive type wrapper objects and {@link String}. These
	 * are the objects that can't be root objects
	 */
	private static Set< Class< ? > > nonRootObjects()
	{
		return new HashSet<>();
	}
	
	/**
	 * @return a map containing a primitive type and the wrapper object used to represent that primitive type
	 */
	private static Map< Class< ? >, Object > createDefaultInstances()
	{
		final Map< Class< ? >, Object > defaults = new HashMap<>();
		defaults.putAll( createPrimitives() );
		defaults.put( Integer.class, 0 );
		defaults.put( Long.class, (long) 0 );
		defaults.put( Short.class, Short.MAX_VALUE );
		defaults.put( Double.class, 0.0 );
		defaults.put( Float.class, new Float( 0.0 ) );
		defaults.put( Boolean.class, true );
		defaults.put( Byte.class, Byte.MAX_VALUE );
		defaults.put( Character.class, '0' );
		return defaults;
	}
	
	/**
	 * When set to {@link #isPersistClassConstants} is set true then class constants (i.e. static final fields)
	 * are persisted. Otherwise, no node is created for class constants
	 * @param isPersistClassConstants True to persist class constants; false otherwise
	 */
	public void setPersistClassConstants( final boolean isPersistClassConstants )
	{
		this.isPersistClassConstants = isPersistClassConstants;
	}

	/**
	 * When set to {@code true} null values will be persisted. The default behaviour is not to persist null values.
	 * @param isPersistNullValues whether or not to persist null values.
	 */
	public void setPersistNullValues( final boolean isPersistNullValues )
	{
		this.isPersistNullValues = isPersistNullValues;
	}

	/**
	 * Tells the persistence engine to persist null values
	 * @return This {@link org.freezedry.persistence.PersistenceEngine}
	 */
	public PersistenceEngine withPersistNullValues()
	{
		this.isPersistNullValues = true;
		return this;
	}

	/**
	 * Tells the persistence engine to persist class constants
	 * @return This {@link org.freezedry.persistence.PersistenceEngine}
	 */
	public PersistenceEngine withPersistClassConstants()
	{
		this.isPersistClassConstants = true;
		return this;
	}

	/**
	 * Sets the separator used when the a class has a generic type for which we need to store the {@link java.lang.Class}
	 * of the member instance so that the object can be reconstructed.
	 * @param separator The separator used when the a class has a generic type
	 * @return This {@link org.freezedry.persistence.PersistenceEngine} for chaining
	 */
	public PersistenceEngine withGenericTypeSeparator( final String separator )
	{
		if( separator != null && !separator.isEmpty() )
		{
			this.genericTypeSeparator = separator;
		}
		return this;
	}

	/**
	 * @return the separator used when the a class has a generic type for which we need to store the {@link java.lang.Class}
	 * of the member instance so that the object can be reconstructed.
	 */
	public String getGenericTypeSeparator()
	{
		return genericTypeSeparator;
	}

	/**
	 * Adds a {@link NodeBuilder} to be used for generating {@link InfoNode}s for the specified {@link Class}
	 * @param clazz The {@link Class} of the object to persist and, therefore, for which to generate a node
	 * @param builder The {@link NodeBuilder} used to generate {@link InfoNode}s for the specified {@link Class}
	 * @return The {@link NodeBuilder} that used to be associated with the specified {@link Class}; or null if
	 * no {@link NodeBuilder} was previously associated with the specified {@link Class}
	 */
	public NodeBuilder addNodeBuilder( final Class< ? > clazz, final NodeBuilder builder )
	{
		return nodeBuilders.put( clazz, builder );
	}
	
	/**
	 * Finds the {@link NodeBuilder} associated with the class. If the specified class
	 * doesn't have a info node builder, then it searches for the closest parent class (inheritance)
	 * and returns true. In this case, it adds an entry to the info node builders map for the
	 * specified class associating it with the returned info node builder (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a info node builder
	 * @return the true if a info node builder was found; false otherwise
	 */
	public boolean containsNodeBuilder( final Class< ? > clazz )
	{
		return ( getNodeBuilder( clazz ) != null );
	}
	
	/**
	 * Returns true if the specified field name of the specified {@link Class} has a {@link NodeBuilder} annotation;
	 * false otherwise. 
	 * @param clazz The specified {@link Class}
	 * @param fieldName The field name to check for a {@link NodeBuilder} annotation.
	 * @return true if the specified field name of the specified {@link Class} has a {@link NodeBuilder} annotation;
	 * false otherwise.
	 */
	public boolean containsAnnotatedNodeBuilder( final Class< ? > clazz, final String fieldName )
	{
		boolean contains = false;
		try
		{
			if( clazz != null )
			{
				contains = ReflectionUtils.hasNodeBuilderAnnotation( clazz, fieldName );
			}
		}
		catch( IllegalArgumentException e ) { /* swallow the exception */ }
		
		return contains;
	}

	/**
	 * Finds the {@link NodeBuilder} associated with the class. If the specified class
	 * doesn't have a info node builder, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the info node builders map for the
	 * specified class associating it with the returned info node builder (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a info node builder
	 * @return the {@link NodeBuilder} associated with the class
	 */
	public NodeBuilder getNodeBuilder( final Class< ? > clazz )
	{
		return ReflectionUtils.getItemOrAncestorCopyable( clazz, nodeBuilders );
	}
	
	/**
	 * Returns true if the specified {@link Class} is in the {@link #NON_ROOT_OBJECTS} {@link Set} 
	 * @param clazz The {@link Class} to find in the {@link #NON_ROOT_OBJECTS} {@link Set}
	 * @return true if the {@link Class} is a non-root object (i.e. an object that shouldn't be a
	 * root element in the semantic model.
	 */
	public boolean isForbiddenRootObject( final Class< ? > clazz )
	{
		boolean isNonRootObject = false;
		if( NON_ROOT_OBJECTS.contains( clazz ) )
		{
			isNonRootObject = true;
		}
		else
		{
			for( Class< ? > element : NON_ROOT_OBJECTS )
			{
				if( ReflectionUtils.calculateClassDistance( clazz, element ) > -1 )
				{
					isNonRootObject = true;
					
					// add the value for faster look-up next time
					NON_ROOT_OBJECTS.add( clazz );
					
					break;
				}
			}
		}
		return isNonRootObject;
	}
	
	/**
	 * Returns true if the specified {@link Class} is an allowed root object (i.e. the root node in
	 * the semantic model); false otherwise
	 * @param clazz The {@link Class} to test
	 * @return true if the specified {@link Class} is an allowed root object (i.e. the root node in
	 * the semantic model); false otherwise
	 */
	public boolean isAllowedRootObject( final Class< ? > clazz )
	{
		return !isForbiddenRootObject( clazz );
	}
	
	/**
	 * Returns a default instance of the type specified by the class
	 * @param clazz The class for which to return an instance based on the default instances defined in the
	 *              default instances map
	 * @return The default instance
	 */
	private Object getDefaultInstance( final Class< ? > clazz )
	{
		return ReflectionUtils.getItemOrAncestor( clazz, defaultInstances );
	}

	/**
	 * Removes the {@link NodeBuilder} associated with the specified {@link InfoNode}
	 * @param clazz The {@link Class} of the object to persist for which the associated {@link NodeBuilder}
	 * will be removed.
	 * @return The removed {@link NodeBuilder} associated with the specified {@link Class}; if the specified
	 * {@link Class} was not found in the {@link Map}, then returns null.
	 */
	public NodeBuilder removeNodeBuilder( final Class< ? > clazz )
	{
		return nodeBuilders.remove( clazz );
	}
	
	/**
	 * Sets the default {@link NodeBuilder} used for arrays ({@code String[]}, {@code int[]}, {@code Object[]}, etc)
	 * for objects for which a specific {@link NodeBuilder} hasn't been specified.
	 * You can add a specific array {@link NodeBuilder} to using {@link #addNodeBuilder(Class, NodeBuilder)} method.
	 * For example:<p>
	 * {@code builder.addNodeBuilder( String[].class, new ArrayNodeBuilder() )}<p>
	 * maps all {@code String[]} objects to use the {@link ArrayNodeBuilder}. 
	 * @param builder The default {@link NodeBuilder} to use for arrays for which a specific {@link NodeBuilder}
	 * hasn't been specified.
	 */
	public void setGeneralArrayNodeBuilder( final ArrayNodeBuilder builder )
	{
		this.genaralArrayNodeBuilder = builder;
	}

	/**
	 * Sets the default {@link NodeBuilder} used for enums, for which objects for which a specific {@link NodeBuilder}
	 * hasn't been specified. You can add a specific enum {@link NodeBuilder} to using {@link #addNodeBuilder(Class, NodeBuilder)} method.
	 * For example:<p>
	 * {@code builder.addNodeBuilder( String[].class, new EnumNodeBuilder() )}<p>
	 * maps all {@code String[]} objects to use the {@link EnumNodeBuilder}.
	 * @param builder The default {@link NodeBuilder} to use for arrays for which a specific {@link NodeBuilder}
	 * hasn't been specified.
	 */
	public void setGeneralEnumNodeBuilder( final EnumNodeBuilder builder )
	{
		this.generalEnumNodeBuilder = builder;
	}

	/**
	 * Searches through the existing node builders to see if they are of the specified {@link Class}.
	 * If it doesn't find one, then it instantiates a new {@link NodeBuilder} and sets its {@link PersistenceEngine}
	 * to this object.
	 * @param clazz The {@link NodeBuilder} {@link Class} to be used for instantiating the object
	 * @param fieldName The name of the field for which to get the node builder
	 * @return a {@link NodeBuilder} object associated with the specified node builder {@link Class}
	 */
	private NodeBuilder getAnnotatedNodeBuilder( final Class< ? > clazz, final String fieldName )
	{
		// grab the node builder class from the field annotation
		final Class< ? > nodeBuilderClass = ReflectionUtils.getNodeBuilderClass( clazz, fieldName );

		// first we check to see if the node builder has already been constructed, in which case
		// we use it.
		NodeBuilder nodeBuilder = null;
		for( Map.Entry< Class< ? >, NodeBuilder > entry : nodeBuilders.entrySet() )
		{
			if( entry.getValue().getClass().equals( nodeBuilderClass ) )
			{
				nodeBuilder = entry.getValue();
				break;
			}
		}
		
		// if the node builder is still null at this point, then we need to instantiate one.
		if( nodeBuilder == null )
		{
			try
			{
				nodeBuilder = (NodeBuilder)nodeBuilderClass.newInstance();
				nodeBuilder.setPersistenceEngine( this );
			}
			catch( InstantiationException | IllegalAccessException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Unable to instantiate the NodeBuilder class." ).append( Constants.NEW_LINE );
				message.append( "  NodeBuilder Class: " ).append( nodeBuilderClass.getName() );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		return nodeBuilder;
	}
	
	/**
	 * Creates a semantic model of the specified {@link Object}. The semantic model is a tree
	 * that holds the information about each element (field) in a {@link InfoNode}. The {@link InfoNode}
	 * information is a complete set of information needed to persist and reconstruct the 
	 * specified {@link Object}.
	 * @param object The object which to convert into a semantic model
	 * @return The root {@link InfoNode} of the tree representing the semantic model
	 * @see InfoNode
	 */
	public final InfoNode createSemanticModel( final Object object )
	{
		// create the root node of the tree, which holds the information about the
		// object we are being asked to persist.
		final Class< ? > clazz = object.getClass();
		
		InfoNode rootNode;
		
		// if the object is an array of one or more dimensions, then we need to replace the
		// "[]" which the array suffix (by default = "Array"). Then we use the array node
		// builder to create the semantic model.
		if( clazz.isArray() )
		{
			final String persistName = clazz.getSimpleName().replaceAll( "\\[\\]", genaralArrayNodeBuilder.getCompoundArrayNameSuffix() );
			try
			{
				rootNode = genaralArrayNodeBuilder.createInfoNode( object, persistName );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Error building the root node" ).append( Constants.NEW_LINE );
				message.append( "  Class: " ).append( clazz.getName() );
				LOGGER.error( message.toString(), e );
				throw new IllegalArgumentException( message.toString(), e );
			}
		}
		else if( clazz.isEnum() )
		{
			rootNode = generalEnumNodeBuilder.createInfoNode( object, clazz.getSimpleName() );
		}
		// if the override node builder map contains a node builder for this specific class, and
		// the class is allowed to be a root object, then we'll use it
		else if( containsNodeBuilder( clazz ) && isAllowedRootObject( clazz ) )
		{
			try
			{
				rootNode = getNodeBuilder( clazz ).createInfoNode( object, clazz.getName() );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Error building the root node" ).append( Constants.NEW_LINE );
				message.append( "  Class: " ).append( clazz.getName() );
				LOGGER.error( message.toString(), e );
				throw new IllegalArgumentException( message.toString(), e );
			}
		}
		else
		{
			rootNode = InfoNode.createRootNode( clazz.getSimpleName(), clazz );
			
			// run through the methods building up the semantic model, which is a tree
			// that holds the information about each element in a node. the node information
			// is a complete set of information needed to persist and reconstruct an object
			addNodes( rootNode, object );
		}
		
		// return the root node of the tree
		return rootNode;
	}
	
	/*
	 * Recurses its way down through the objects creating the semantic model. Uses reflection
	 * and the persistence annotations to construct the model.
	 * @param currentNode The current {@link InfoNode} in the semantic tree
	 * @param object The object to convert into a node
	 * @return The added {@link InfoNode}, which may have child {@link InfoNode}s
	 */
	private InfoNode addNodes( final InfoNode currentNode, final Object object )
	{
		// grab the object's type
		final Class< ? > clazz = object.getClass();
		
		// run through the fields associated with object's Class< ? >, create the nodes for
		// each field, and add that node to the current node. recall that this is a recursive
		// algorithm where createNode(...) may call this method recursively.
		final List< Field > fields = ReflectionUtils.getAllDeclaredFields( clazz );
		for( final Field field : fields )
		{
			// if the field has a @Persist annotation, and the ignore is true, then
			// simply ignore the field
			final Persist persistAnnotation = field.getAnnotation( Persist.class );
			if( persistAnnotation != null && persistAnnotation.ignore() )
			{
				continue;
			}
			
			// if the field is a class constant (i.e. static final) then don't add the node
			final int modifiers = field.getModifiers();
			if( Modifier.isFinal( modifiers ) && Modifier.isStatic( modifiers ) && !isPersistClassConstants )
			{
				continue;
			}
			
			try
			{
				// make sure that we can access the field
				field.setAccessible( true );
				
				// create and add the node representing this object to the current node, unless the
				// node has a null value.
				final Object fieldObject = field.get( object );
				if( fieldObject != null || isPersistNullValues )
				{
					currentNode.addChild( createNode( clazz, fieldObject, field.getName() ) );
				}
			}
			catch( IllegalAccessException e )
			{
				LOGGER.error( "Attempted to access a field that doesn't exist." + Constants.NEW_LINE + "  Object " + clazz.getName() + Constants.NEW_LINE + "  Field Name: " + field.getName() + Constants.NEW_LINE, e );
			}
		}
		return currentNode;
	}
	
	/**
	 * Creates a {@link InfoNode} for the specified {@link Object}, recursively, calling either {@link #addNodes(InfoNode, Object)}
	 * or itself to construct {@link InfoNode}s and add them to the tree.
	 * @param object The object for which to create the node
	 * @param fieldName The name of the field for which to create the node
	 * @return the top-level {@link InfoNode} for the object
	 */
	public final InfoNode createNode( final Class< ? > containingClass, final Object object, final String fieldName )
	{
		InfoNode node;
		if( object == null )
		{
			return InfoNode.createLeafNode( fieldName, null, fieldName, Object.class );
		}

		// create the InfoNode object (we first have to determine the node type, down the road, we'll check the
		// factories for registered node info node builders for the Class< ? > of the object)
		final Class< ? > clazz = object.getClass();
		
		// construct the node. There are several cases to consider:
		// 0. the field is annotated with a specified node builder in mind
		// 1. the object is intended to be a leaf node: create a leaf InfoNode object
		//    with the appropriately populated values, and we're done.
		// 2. the object is of a special type: use the appropriate node factory 
		//    to construct the appropriate node
		// 3. neither of the first two conditions are met: simply call the addNodes(...)
		//    method recursively to construct the nodes.
		// 4. the object is an array, and the array class hasn't been specified in the node 
		//    builder map, then we must treat it as a generic array.
		// 5. the object is an enum, and must be treated as an enum, which may have a special get name method
		// the first two cases are handled by the info node builders in the info node builders map. even
		// the leaf node info node builders may need to be overridden. the third case is handled
		// by the compound node
		if( containsAnnotatedNodeBuilder( containingClass, fieldName ) )
		{
			final NodeBuilder builder = getAnnotatedNodeBuilder( containingClass, fieldName );
			try
			{
				node = builder.createInfoNode( containingClass, object, fieldName );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Node Builder failed to create InfoNode:" ).append( Constants.NEW_LINE );
				message.append( "  Builder: " ).append( builder.getClass().getName() ).append( Constants.NEW_LINE );
				message.append( "  Containing Class Name: " ).append( containingClass.getName() ).append( Constants.NEW_LINE );
				message.append( "  Object: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
				message.append( "  Field Name: " ).append( fieldName ).append( Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else if( containsNodeBuilder( clazz ) )
		{
			final NodeBuilder builder = getNodeBuilder( clazz );
			try
			{
				// if the containing class is null, then this is a root object and we
				// must use the root object version of the createInfoNode method
				if( containingClass == null )
				{
					node = builder.createInfoNode( object, fieldName );
				}
				else
				{
					node = builder.createInfoNode( containingClass, object, fieldName );
				}
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Node Builder failed to create InfoNode:" ).append( Constants.NEW_LINE );
				message.append( "  Builder: " ).append( builder.getClass().getName() ).append( Constants.NEW_LINE );
				message.append( "  Containing Class Name: " ).append( containingClass == null ? "[null]" : containingClass.getName() ).append( Constants.NEW_LINE );
				message.append( "  Object: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
				message.append( "  Field Name: " ).append( fieldName ).append( Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else if( clazz.isArray() )
		{
			try
			{
				node = genaralArrayNodeBuilder.createInfoNode( containingClass, object, fieldName );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Default Array Node Builder failed to create InfoNode:" ).append( Constants.NEW_LINE );
				message.append( "  Builder: " ).append( genaralArrayNodeBuilder.getClass().getName() ).append( Constants.NEW_LINE );
				message.append( "  Containing Class Name: " ).append( containingClass.getName() ).append( Constants.NEW_LINE );
				message.append( "  Object: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
				message.append( "  Field Name: " ).append( fieldName ).append( Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else if( clazz.isEnum() )
		{
			node = generalEnumNodeBuilder.createInfoNode( containingClass, object, fieldName );
		}
		else
		{
			// check to see if the field has a generic type. for example, suppose that the containing
			// class is defined as: class A< T extends B > { .... }
			// if class A has a member defined as: T member
			// then we want to store the member's acutal class.
			String persistName = fieldName;
			try
			{
				if( containingClass != null && containingClass.getDeclaredField( fieldName ).getGenericType() != null )
				{
					final String className = object.getClass().getName().replace( ".", "_" );
					persistName += genericTypeSeparator + className;
				}
			}
			catch( NoSuchFieldException e )
			{
				/* this is empty purposely */
			}

			// create a new compound node to holds this, since it isn't a leaf node, and
			// call (recursively) the addNodes(...) method to add the nodes representing the
			// fields of this object to the newly created compound node.
			final InfoNode compoundNode = InfoNode.createCompoundNode( fieldName, persistName, clazz );
			node = addNodes( compoundNode, object );
		}
		
		// then call addNodes(...) with the newly created node
		return node;
	}
	
	/**
	 * Parses the semantic model (a.k.a. content tree) represented by the {@link InfoNode} tree hanging
	 * off the specified root {@link InfoNode}. The {@link Class} of the returned object is the more specific
	 * of the specified {@link Class} and the {@link Class} that may be held in the root {@link InfoNode}. 
	 * The {@link InfoNode}s don't require all the information in each node to be complete. At a minimum
	 * it needs the field name (or the persist name if that is the same as the field name) and for leaf nodes,
	 * the value of the node. The {@link PersistenceEngine} will use reflection to fill in missing information.
	 * However there are limits to what can be obtained by reflection. For example, if the field is a {@link List},
	 * then the actual concrete {@link Class} type must be available somehow.
	 * @param clazz The specified {@link Class} of the object to create
	 * @param rootNode The root {@link InfoNode} representing the semantic model
	 * @return the object represented by the semantic model
	 */
	public Object parseSemanticModel( final Class< ? > clazz, final InfoNode rootNode )
	{
		Object object;
		
		if( clazz.isArray() )
		{
			try
			{
				object = genaralArrayNodeBuilder.createObject( null, clazz, rootNode );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Error creating object" ).append( Constants.NEW_LINE );
				message.append( "  Class: " ).append( clazz.getName() );
				LOGGER.error( message.toString(), e );
				throw new IllegalArgumentException( message.toString(), e );
			}
		}
		else if( clazz.isEnum() )
		{
			object = generalEnumNodeBuilder.createObject( null, clazz, rootNode );
		}
		// if the override node builders contains a node builder for this specific class, then we'll use it
		else if( containsNodeBuilder( clazz ) && isAllowedRootObject( clazz ) )
		{
			try
			{
				object = getNodeBuilder( clazz ).createObject( clazz, rootNode );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Error creating object" ).append( Constants.NEW_LINE );
				message.append( "  Class: " ).append( clazz.getName() );
				LOGGER.error( message.toString(), e );
				throw new IllegalArgumentException( message.toString(), e );
			}
		}
		else
		{
			// instantiate the object and build it recursively
			object = instantiate( clazz, rootNode );
			buildObject( object, rootNode );
		}
		
		return object;
	}
	
	/**
	 * Instantiates an object of the specified class by calling the constructor with the smallest
	 * number of arguments. If a no-arg constructor exists, then that is what will be called. Constructors
	 * that have arguments will be passed null objects, and then the fields will be set reflectively
	 * through the {@link Field#set(Object, Object)} method. This means that if the constructor performs
	 * checks against null, you will have a problem. 
	 * @param clazz The {@link Class} to instantiate 
	 * @param rootNode The root {@link InfoNode} of the semantic tree containing the information about the
	 * class to instantiate
	 * @return The instantiated object.
	 */
	private Object instantiate( final Class< ? > clazz, final InfoNode rootNode )
	{
		// grab the Class for the root node based on the specified class and the root node
		final Class< ? > rootClass = ReflectionUtils.getMostSpecificClass( clazz, rootNode );
		
		// instantiate the object represented by the root node
		Object object;
		try
		{
			// find the constructor, and if it exists, then use it to create a new instance
			// grab the constructors for the class. find the constructor with the smallest
			// number of arguments, and create dummy arguments, and call the newInstance method
			// on that constructor (the fields will get set with the appropriate values)
			final Constructor< ? >[] constructors = rootClass.getConstructors();
			Object instance;
			if( ( instance = getDefaultInstance( clazz ) ) != null )
			{
				object = instance;
			}
			else if( rootClass.isEnum() )
			{
				object = rootClass.getEnumConstants();
			}
			else if( constructors.length > 0 )
			{
				// set the minimum number of constructor params to the largest possible
				int minNumParams = Integer.MAX_VALUE;
				
				// create the constructor reference and the list of arguments associated
				// with the constructor that has the smallest number of parameters
				Constructor< ? > minParamConstructor = null;
				Class< ? >[] minParamTypes = null;
				
				// search for the constructor with the smallest number of parameters and store
				// it and the parameter types
				for( Constructor< ? > constructor : constructors )
				{
					final Class< ? >[] paramTypes = constructor.getParameterTypes();
					if( paramTypes.length < minNumParams )
					{
						minNumParams = paramTypes.length;
						minParamConstructor = constructor;
						minParamTypes = paramTypes;
					}
				}

				// if no constructor was found, then throw an exception
				if( minParamConstructor == null )
				{
					final StringBuilder message = new StringBuilder();
					message.append( "Could not find an appropriate constructor to instantiate the Class." ).append( Constants.NEW_LINE )
							.append( "  Specified Class Name: " ).append( clazz.getName() ).append( Constants.NEW_LINE )
							.append( "  Number of Constructors Searched: " ).append( constructors.length );
					LOGGER.error( message.toString() );
					throw new IllegalArgumentException( message.toString() );
				}

				// create the parameter array (use default values that will get overriden
				// during the object creation anyway)
				final Object[] params = createConstructorParameters( minParamTypes );
				
				// create the new instance using the constructor and its default parameters
				object = minParamConstructor.newInstance( params );
			}
			else if( rootClass.isArray() )
			{
				final Class< ? > componentType = rootClass.getComponentType();
				object = Array.newInstance( componentType, rootNode.getChildCount() );
			}
			else
			{
				object = rootClass.newInstance();
			}
		}
		catch( IllegalAccessException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Failed to instantiate object from Class. Either the Class or the" ).append( Constants.NEW_LINE );
			message.append( "nullary constructor were not available." ).append( Constants.NEW_LINE );
			message.append( "  Specified Class Name: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
			message.append( "  Most Specific Class Name: " ).append( rootClass.getName() ).append( Constants.NEW_LINE );
			message.append( "  InfoNode: " ).append( rootNode.getClass().getName() ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		catch( InstantiationException | InvocationTargetException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Failed to instantiate object from Class. This Class represents an " ).append( Constants.NEW_LINE );
			message.append( "abstract class, an interface, an array class, a primitive type, or " ).append( Constants.NEW_LINE );
			message.append( "void; or the class has no nullary constructor; or the instantiation " ).append( Constants.NEW_LINE );
			message.append( "failed for some other reason." ).append( Constants.NEW_LINE );
			message.append( "  Specified Class Name: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
			message.append( "  Most Specific Class Name: " ).append( rootClass.getName() ).append( Constants.NEW_LINE );
			message.append( "  InfoNode: " ).append( rootNode.getClass().getName() ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		
		return object;
	}
	
	/*
	 * Creates an array of default parameters for the constructor. If the arguments
	 * are primitives, the creates the object of the correct type, otherwise, simply
	 * type casts the null.
	 * @param paramTypes The {@link Class} for constructor parameters  
	 * @return an {@link Object} array containing the default parameters for the constructor 
	 */
	private Object[] createConstructorParameters( final Class< ? >[] paramTypes )
	{
		final Object[] params = new Object[ paramTypes.length ];
		for( int i = 0; i < paramTypes.length; ++i )
		{
			final Class< ? > type = paramTypes[ i ];
			if( type.isPrimitive() )
			{
				params[ i ] = PRIMITIVE_TYPES.get( type );
			}
			else
			{
				params[ i ] = type.cast( null );
			}
		}
		
		return params;
	}
	
	/*
	 * Builds the specified {@link Object} by setting the fields it contains, using the information in 
	 * the specified {@link InfoNode}. The fields in the specified {@link Object} are objects that must be 
	 * instantiated before being set. This method is part of the recursive algorithm that walks down the
	 * semantic model (a.k.a. content tree) and builds the object. 
	 * @param object The containing object whose fields to build into objects
	 * @param currentNode The current {@link InfoNode} in the semantic model.
	 * @return The specified {@link Object} with all its fields set.
	 */
	private Object buildObject( final Object object, final InfoNode currentNode )
	{
		// 1. create the object for the specified clazz
		// 2. create the objects for the fields recursively
		final Class< ? > clazz = object.getClass();
		
		for( InfoNode node : currentNode.getChildren() )
		{
			// find the name of the field from the info node
			final String persistName = node.getPersistName();
			String fieldName = node.getFieldName();
			if( fieldName == null || fieldName.isEmpty() )
			{
				final Field field = ReflectionUtils.getFieldForPersistenceName( clazz, persistName );
				if( field != null )
				{
					fieldName = field.getName();
				}
				else
				{
					fieldName = persistName;
				}
			}

			// grab the class' field
			try
			{
				// check to see if this type may be a generic type. in that case, the field name is listed first, and
				// then its class is appended, so we grab the field name and set it to the first part, and tell the node
				// that it should instantiate this class (note: this may be overridden by annotations)
				if( fieldName.contains( genericTypeSeparator ) )
				{
					final String[] components = fieldName.split( Pattern.quote( genericTypeSeparator ) );
					fieldName = components[ 0 ];
					final String genericType = components[ 1 ].replace( '_', '.' );

					// if the generic type has a value, then hand it to the node
					if( !genericType.isEmpty() )
					{
						try
						{
							node.setClazz( Class.forName( genericType ) );
						}
						catch( ClassNotFoundException e )
						{
							final String warn = "Attempted to load the class for the generic type specified, but was unable to. " + Constants.NEW_LINE +
									"  Object class: " + object.getClass().getSimpleName() + Constants.NEW_LINE +
									"  Field name: " + fieldName + Constants.NEW_LINE +
									"  Generic Type: " + genericType;
							LOGGER.warn( warn, e );
						}
					}
				}
				final Field field = ReflectionUtils.getDeclaredField( clazz, fieldName );

				// grab the generic parameter type of the field and add it to the info node
				final Type type = field.getGenericType();
				if( type instanceof ParameterizedType )
				{
					final List< Type > types = Arrays.asList( ((ParameterizedType)type).getActualTypeArguments() );
					node.setGenericParameterTypes( types );
				}

				// see if the field has a @Persist( instantiateAs = XXXX.class ) annotation
				final Persist annotation = field.getAnnotation( Persist.class );
				if( annotation != null )
				{
					// if no class information is stored in the node, or if the class stored in the
					// node is a super class of the instantiate type, then use the instantiate type
					final Class< ? > instantiateType = annotation.instantiateAs();
					if( !instantiateType.equals( Persist.Null.class ) )
					{
						final Class< ? > nodeClazz = node.getClazz();
						if( nodeClazz == null || ReflectionUtils.isClassOrSuperclass( nodeClazz, instantiateType ) )
						{
							node.setClazz( instantiateType );
						}
					}
				}
	
				// create the object
				final Class< ? > newClass = ReflectionUtils.getMostSpecificClass( field.getType(), node );
				final Class< ? > containingClass = field.getDeclaringClass();
				final Object newObject = createObject( containingClass, newClass, node );
	
				// set the field to be accessible (override the accessor)
				field.setAccessible( true );
	
				// if the field has a "static final" modifier, then we don't set the field
				// because it is a class constant
				final int modifiers = field.getModifiers();
				try
				{
					if( Modifier.isStatic( modifiers ) && Modifier.isFinal( modifiers ) )
					{
						if( LOGGER.isInfoEnabled() )
						{
							final String message = "Ignoring field because it has \"static final\" modifiers:" + Constants.NEW_LINE +
									"  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE +
									"  Field Name: " + fieldName + Constants.NEW_LINE +
									"  Field Modifiers: " + Modifier.toString( modifiers ) + Constants.NEW_LINE;
							LOGGER.info( message );
						}
					}
					else
					{
						// set the fields value
						field.set( object, newObject );
					}
				}
				catch( IllegalAccessException e )
				{
					final StringBuilder message = new StringBuilder();
					message.append( "Attempted to perform an invalid operation on field:" ).append( Constants.NEW_LINE );
					message.append( "  Field Name: " ).append( fieldName ).append( Constants.NEW_LINE );
					message.append( "  Field Modifiers: " ).append( Modifier.toString( field.getModifiers() ) ).append( Constants.NEW_LINE );
					message.append( "  Containing Class: " ).append( object.getClass().getName() ).append( Constants.NEW_LINE );
					LOGGER.error( message.toString(), e );
					throw new IllegalStateException( message.toString(), e );
				}

			}
			catch( NoSuchFieldException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Attempted to retrieve field for an invalid field name:" ).append( Constants.NEW_LINE );
				message.append( "  Field Name: " ).append( fieldName ).append( Constants.NEW_LINE );
				message.append( "  Containing Class: " ).append( object.getClass().getName() ).append( Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		
		return object;
	}
	
	/**
	 * Creates and returns the object represented by the specified {@link InfoNode} and {@link Class}. The method
	 * chooses the most specific type (i.e. farthest down on the inheritance hierarchy) found in the {@link InfoNode}
	 * or the {@link Class}. The specified {@link InfoNode} doesn't need to hold the type information, if such 
	 * information is not available.
	 * @param clazz The {@link Class} type of the object to create (unless the one found in the {@link InfoNode} is
	 * more specific).
	 * @param currentNode The {@link InfoNode} containing information about the object to be created
	 * @return The newly minted object
	 */
	public Object createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode currentNode )
	{
		// grab the field name from the node
		String fieldName = currentNode.getFieldName();
		if( fieldName == null )
		{
			final String persistName = currentNode.getPersistName();
			if( containingClass != null )
			{
				fieldName = ReflectionUtils.getFieldNameForPersistenceName( containingClass, persistName );
			}
			if( fieldName == null )
			{
				fieldName = persistName;
			}
		}
		
		// find the node builder need to create the object. if there is
		// no node builder, then instantiate the object and make a recursive call
		// to the buildObject(...) method.
		Object object;
		if( containsAnnotatedNodeBuilder( containingClass, fieldName ) )
		{
			final NodeBuilder builder = getAnnotatedNodeBuilder( containingClass, fieldName );
			try
			{
				object = builder.createObject( containingClass, clazz, currentNode );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Node Builder failed to create object from Class and InfoNode:" ).append( Constants.NEW_LINE );
				message.append( "  Class Name: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
				message.append( "  Builder: " ).append( builder.getClass().getName() ).append( Constants.NEW_LINE );
				message.append( "  InfoNode: " ).append( currentNode.toString() ).append( Constants.NEW_LINE );
				LOGGER.error( message.toString(), e );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else if( containsNodeBuilder( clazz ) )
		{
			final NodeBuilder builder = getNodeBuilder( clazz );
			try
			{
				// if the containing class is null, then this is a root node, and so we must use
				// the root node version of the create object class
				if( containingClass == null )
				{
					object = builder.createObject( clazz, currentNode );
				}
				else
				{
					object = builder.createObject( containingClass, clazz, currentNode );
				}
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Node Builder failed to create object from Class and InfoNode:" ).append( Constants.NEW_LINE );
				message.append( "  Class Name: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
				message.append( "  Builder: " ).append( builder.getClass().getName() ).append( Constants.NEW_LINE );
				message.append( "  InfoNode: " ).append( currentNode.toString() ).append( Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else if( clazz.isArray() )
		{
			try
			{
				object = genaralArrayNodeBuilder.createObject( containingClass, clazz, currentNode );
			}
			catch( ReflectiveOperationException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Default Array Node Builder failed to create object from Class and InfoNode:" ).append( Constants.NEW_LINE );
				message.append( "  Class Name: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
				message.append( "  Builder: " ).append( genaralArrayNodeBuilder.getClass().getName() ).append( Constants.NEW_LINE );
				message.append( "  InfoNode: " ).append( currentNode.toString() ).append( Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else if( clazz.isEnum() )
		{
			object = generalEnumNodeBuilder.createObject( containingClass, clazz, currentNode );
		}
		else
		{
			// create the new object and then build it recursively
			final Object newObject = instantiate( clazz, currentNode );
			object = buildObject( newObject, currentNode );
		}
		return object;
	}
}
