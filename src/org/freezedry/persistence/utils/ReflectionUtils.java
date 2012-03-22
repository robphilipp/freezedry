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
package org.freezedry.persistence.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RunnableScheduledFuture;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.builders.NodeBuilder;
import org.freezedry.persistence.containers.orderedseries.IntegerOrderedSeries;
import org.freezedry.persistence.copyable.Copyable;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.tests.Fconcrete;
import org.freezedry.persistence.utils.tests.circle.A;
import org.freezedry.persistence.utils.tests.circle.Aprime;
import org.freezedry.persistence.utils.tests.circle.B;
import org.freezedry.persistence.utils.tests.circle.D;
import org.freezedry.persistence.utils.tests.circle.Econcrete;
import org.freezedry.persistence.utils.tests.circle.Gconcrete;

/**
 * Utilities used for common activities that require reflection
 * 
 * @author Robert Philipp
 */
public class ReflectionUtils {
	
	private static final Logger LOGGER = Logger.getLogger( ReflectionUtils.class );

	/**
	 * Returns true if the specified super class is a super class of the specified class; false otherwise
	 * @param superClass The specified super class
	 * @param clazz The specified class
	 * @return true if the specified super class is a super class of the specified class; false otherwise
	 */
	public static boolean isSuperclass( final Class< ? > superClass, final Class< ? > clazz )
	{
		return ( calculateClassDistance( clazz, superClass, -1 ) > 0 );
	}
	
	/**
	 * Calculates the largest distance between the specified {@link Class} and the target {@link Class}. Only
	 * the following combinations are allowed (because they're the only ones the make sense.
	 * <ul>
	 * 	<li>Both the specified class and the target class are interfaces.</li>
	 * 	<li>Neither the specified class nor the target class are interfaces.</li>
	 * 	<li>The specified class is not an interface, and the target class is an interface.</li>
	 * </ul>
	 * In one of these three conditions isn't met, then this method returns -1. And if the specified class 
	 * does not ultimately implement or extend the targe class, then this method returns -1.<p>
	 * 
	 * As an example consider the diagram:
	 * <pre>{@code  .
	 *   a
	 *   |
	 *   b
	 *   |  a
	 *   C /
	 *   |/
	 *   D
	 * }</pre>
	 * The small letters refer to an interface ({@code a} and {@code b} are interfaces) and the capital letters
	 * refer to classes ({@code C} and {@code D} are classes). In this diagram, the class {@code D} extends {@code C}
	 * and implements {@code a}. The class {@code C}, in turn, implements the interface {@code b}, which extends 
	 * the interface {@code a}. In this example, the largest calculated distance between the class {@code D} and 
	 * the interface {@code a} is 3. Similarly, the largest distance between the class {@code C} and the interface
	 * {@code a} is 2. 
	 * @param clazz The specified class or interface from which to calculate the distance 
	 * @param targetClass The target class or interface to which to calculate the distance
	 * @return the largest distance between the specified and target class; or -1 if specified class ins't in the
	 * hierarchy of the target class
	 * @see #calculateClassDistance(Class, Class, int)
	 */
	public static int calculateClassDistance( final Class< ? > clazz, final Class< ? > targetClass )
	{
		return calculateClassDistance( clazz, targetClass, -1 );
	}
	
	/*
	 * Recursive method that calculates the largest distance between the specified and target class.
	 * @param clazz The specified class or interface from which to calculate the distance 
	 * @param targetClass The target class or interface to which to calculate the distance
	 * @param level The distance at which the current calculation is. 
	 * @return the largest distance between the specified and target class; or -1 if specified class ins't in the
	 * hierarchy of the target class
	 * @see #calculateClassDistance(Class, Class, int)
	 */
	private static int calculateClassDistance( final Class< ? > clazz, final Class< ? > targetClass, final int level )
	{
		// there are 3 possible cases we have to look at:
		// 1. both the clazz and the target class are interfaces
		// 2. the clazz is not an interface and the target class is an interface
		// 3. neither the clazz or the target class are interfaces
		if( clazz.isInterface() && targetClass.isInterface() )
		{
			return getInterfaceDistance( clazz, targetClass, level );
		}
		else if( targetClass.isInterface() )
		{
			return getClassToInterfaceDistance( clazz, targetClass, level );
		}
		else
		{
			return getClassToClassDistance( clazz, targetClass, level );
		}
	}

	/*
	 * Calculates the largest distance between two interfaces. Both the specified {@link Class} and the target {@link Class}
	 * must be interfaces. If either one isn't, then returns -1. For example consider the text below:
	 * <pre>{@code  .
	 *   A
	 *   |
	 *   B
	 *   |  A
	 *   C /
	 *   |/
	 *   D
	 * }</pre>
	 * The calculated distance between the interfaces D and A is 3 because that is the farther D is away from A.
	 * @param clazz The most specified interface (i.e. the one that possibly extends from the target) 
	 * @param target The target interface (i.e. the most general interface, which the specified interface may extends)
	 * @param level The current calculated distance between the interfaces (starts at level = -1) 
	 * @return The largest possible distance between the two interfaces; or -1 if the specified {@link Class} doesn't
	 * extends from the target {@link Class} 
	 * @see #calculateClassDistance(Class, Class, int)
	 */
	private static int getInterfaceDistance( final Class< ? > clazz, final Class< ? > target, final int level )
	{
		// both the clazz and the target must be interfaces
		if( !clazz.isInterface() || !target.isInterface() )
		{
			return -1;
		}
		
		// if the clazz and target are equal, then we're done, return that they are 
		// at the same level. if this is the first call then level+1 = -1 + 1 = 0
		if( clazz.equals( target ) )
		{
			return level+1;
		}
		
		// grab the list of interfaces held by the clazz
		final List< Class< ? > > interfaces = Arrays.asList( clazz.getInterfaces() );
		
		// find the interface that is the farthest up the tree
		final Set< Integer > distances = new HashSet<>();
		for( final Class< ? > superInterface : interfaces )
		{
			// recursive call to each super interface, to see if it has the target as its
			// super interface
			final int distance = getInterfaceDistance( superInterface, target, level+1 );
			
			// add the distance that it found (which is -1 if not found)
			distances.add( distance );
			
			// log the progress
			if( LOGGER.isDebugEnabled() )
			{
				String space = "  ";
				for( int i = 0; i < distance; ++i )
				{
					space += "  ";
				}
				LOGGER.debug( space + superInterface.getName() + "(" + distance + ")" );
			}
		}

		// if the set, distances, is empty, then we didn't find the interface, and so
		// we need to return that fact, i.e. distance = -1, otherwise, we return the 
		// largest distance that we found.
		int distance = -1;
		if( !distances.isEmpty() )
		{
			distance = Collections.max( distances );
		}
		return distance;
	}
	
	/*
	 * Calculates the largest distance between a {@link Class} (that is not and interface) and the target {@link Class}
	 * (which is the interface). Returns -1 if the target is not an interface of the specified {@link Class} or
	 * it's super classes or super interfaces.
	 * @param clazz The {@link Class} from which to calculate the distance to the interface. Cannot be an interface
	 * @param target The target {@link Class} to which to calculate the distance. Must be an interface
	 * @param level The current calculated distance. Starts at -1. 
	 * @return he largest distance between a {@link Class} (that is not and interface) and the target {@link Class}
	 * (which is the interface).
	 * @see #calculateClassDistance(Class, Class, int)
	 */
	private static int getClassToInterfaceDistance( final Class< ? > clazz, final Class< ? > target, final int level )
	{
		// if the clazz is null, then we're done, didn't find anything...
		if( clazz == null )
		{
			return -1;
		}
		
		// the clazz cannot be an interface, and the target must be an interface
		if( clazz.isInterface() || !target.isInterface() )
		{
			return -1;
		}

		// set to hold the distances to the target interface for each tested interface
		final Set< Integer > distances = new HashSet<>();
		
		// first, find the distance from the clazz's interfaces to the target
		final List< Class< ? > > interfaces = Arrays.asList( clazz.getInterfaces() );
		for( final Class< ? > superInterface : interfaces )
		{
			distances.add( getInterfaceDistance( superInterface, target, level+1 ) );
		}
		
		// recursive call to get the super class and check it for interfaces
		distances.add( getClassToInterfaceDistance( clazz.getSuperclass(), target, level+1 ) );
		
		// if the set, distances, is empty, then we didn't find the interface, and so
		// we need to return that fact, i.e. distance = -1, otherwise, we return the 
		// largest distance that we found.
		int distance = -1;
		if( !distances.isEmpty() )
		{
			distance = Collections.max( distances );
		}
		return distance;
	}

	/*
	 * Calculates the distance between to {@link Class}es, neither of which can be interfaces; -1 if the specified
	 * {@link Class} doesn't ultimately derive from the target.
	 * @param clazz The most specific {@link Class} which may ultimately derive from the target 
	 * @param target The most general {@link Class} from which the specified {@link Class} may ultimately derive
	 * @param level The current calculated level.
	 * @return the distance between to {@link Class}es, neither of which can be interfaces; -1 if the specified
	 * {@link Class} doesn't ultimately derive from the target.
	 * @see #calculateClassDistance(Class, Class, int)
	 */
	private static int getClassToClassDistance( final Class< ? > clazz, final Class< ? > target, final int level )
	{
		// if the clazz is an object, then we've reached the end
		// if the clazz is null, then we're done..
		// if the clazz or the target are interfaces, then we're in the wrong method.
		if( clazz == Object.class || clazz == null || clazz.isInterface() || target.isInterface() )
		{
			return -1;
		}
		
		// if the clazz equals the target, then return the level + 1, and we're done
		if( clazz.equals( target ) )
		{
			return level+1;
		}
		
		// recursive call to this method to look at the super class in the same way.
		return getClassToClassDistance( clazz.getSuperclass(), target, level+1 );
	}
	
	/**
	 * Returns the persistence name for the specified {@link Field}, or the specified field name if 
	 * no persistence name is specified in the annotation:<br>
	 * {@code @Persist( persistName = "xxxx" )}
	 * @param clazz The {@link Class} containing the field of the specified name
	 * @param fieldName The field name from which to extract the persistence name from the annotation
	 * @return the persistence name for the specified {@link Field}, or the specified field name if 
	 * no persistence name is specified in the annotation
	 * @throws ReflectiveOperationException
	 */
	public static String getPersistenceName( final Class< ? > clazz, final String fieldName )
	{
		String persistName = null; 
		try
		{
			// try for the field, if it isn't there it'll throw an exception.
			// then we have to assume that we're part of a collection or similar compound
			// object that has an array of stuff.
			final Field field = clazz.getDeclaredField( fieldName );
			persistName = getPersistenceName( field );
			if( persistName == null || persistName.isEmpty() )
			{
				persistName = fieldName;
			}
		}
		catch( ReflectiveOperationException e )
		{
			// no field has this name, so just return the field name
			persistName = fieldName; 
		}
		return persistName;
	}

	/**
	 * Returns the persistence name for the specified {@link Field}, or null if no persistence
	 * name is specified in the annotation:<br>
	 * {@code @Persist( persistName = "xxxx" )}
	 * @param field The {@link Field} for which to return the persistence name
	 * @return the persistence name for the specified {@link Field}, or null if no persistence
	 */
	public static String getPersistenceName( final Field field )
	{
		// see if the field has a @Persist( instantiateAs = XXXX.class ) annotation
		final Persist annotation = field.getAnnotation( Persist.class );
		String persistName = null;
		if( annotation != null )
		{
			persistName = annotation.persistenceName();
		}
		return persistName;
	}
	
	/**
	 * Returns the {@link Field} which is annotated with the specified persistence name, or null if
	 * no {@link Field} has the annotation.
	 * @param clazz The {@link Class} potentially containing the field with the specified persistence name
	 * @param persistName The persistence name to find
	 * @return the {@link Field} which is annotated with the specified persistence name, or null if
	 * no {@link Field} has the annotation. 
	 */
	public static Field getFieldForPersistenceName( final Class< ? > clazz, final String persistName )
	{
		final List< Field > fields = Arrays.asList( clazz.getDeclaredFields() );
		Field foundField = null;
		for( final Field field : fields )
		{
			// grab the persistence name if the annotation @Persist( persistName = "xxxx" ) is specified
			if( persistName.equals( getPersistenceName( field ) ) )
			{
				foundField = field;
				break;
			}
		}
		
		return foundField;
	}
	
	/**
	 * Returns the name of the {@link Field} which is annotated with the specified persistence name, or null if
	 * no {@link Field} has the annotation.
	 * @param clazz The {@link Class} potentially containing the field with the specified persistence name
	 * @param persistName The persistence name to find
	 * @return the name of the {@link Field} which is annotated with the specified persistence name, or null if
	 * no {@link Field} has the annotation. 
	 */
	public static String getFieldNameForPersistenceName( final Class< ? > clazz, final String persistName )
	{
		final List< Field > fields = Arrays.asList( clazz.getDeclaredFields() );
		String fieldName = null;
		for( final Field field : fields )
		{
			// grab the persistence name if the annotation @Persist( persistName = "xxxx" ) is specified
			if( persistName.equals( getPersistenceName( field ) ) )
			{
				fieldName = field.getName();
				break;
			}
		}
		
		return fieldName;
	}
	
	/**
	 * Returns the {@link Class} of the {@link InfoNode} object. If the specified {@link Class} is a subtype of the
	 * {@link Class} in the {@link InfoNode}, then it uses the specified {@link Class}. And vice versa.
	 * If the specified {@link Class} is not a subtype of the {@link Class} found in the {@link InfoNode}, then
	 * it returns the {@link Class} from the {@link InfoNode}. If the {@link Class} found in the {@link InfoNode}
	 * is null, then returns the specified {@link Class}.
	 * @param clazz The specified {@link Class} to parse
	 * @param node The {@link InfoNode}
	 * @return The most specific {@link Class} when the specified {@link Class} is a subtype of the {@link Class}
	 * found in the {@link InfoNode} object. Otherwise, returns the {@link Class} found in the {@link InfoNode} object.
	 * If the {@link Class} found in the {@link InfoNode} is null, then returns the specified {@link Class}.
	 */
	public static Class< ? > getMostSpecificClass( final Class< ? > clazz, final InfoNode node )
	{
		// create an instance of the root object
		Class< ? > rootClass = node.getClazz();
		if( rootClass != null && !rootClass.equals( clazz ) )
		{
			if( ReflectionUtils.calculateClassDistance( clazz, rootClass ) > 0  )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "The specified class derives from the class specified in the root node: using root node class."  + Constants.NEW_LINE);
				message.append( "  Specified Class: " + clazz + Constants.NEW_LINE );
				message.append( "  Root Node Class: " + rootClass + Constants.NEW_LINE );
				LOGGER.info( message.toString() );
				rootClass = clazz;
			}
			else if( ReflectionUtils.calculateClassDistance( rootClass, clazz ) > 0 )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "The class in the root node derives from the specified class: using specified class." + Constants.NEW_LINE );
				message.append( "  Specified Class: " + clazz + Constants.NEW_LINE );
				message.append( "  Root Node Class: " + rootClass + Constants.NEW_LINE );
				LOGGER.info( message.toString() );
			}
			else
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Specified class and the class found in the root node differ: using class specified in the root node." + Constants.NEW_LINE );
				message.append( "  Specified Class: " + clazz + Constants.NEW_LINE );
				message.append( "  Root Node Class: " + rootClass + Constants.NEW_LINE );
				LOGGER.warn( message.toString() );
			}
		}
		else
		{
			rootClass = clazz;
		}
		return rootClass;
	}
	
	/**
	 * Retrieves the {@link NodeBuilder} {@link Class} associated with the field name of the specified
	 * {@link Class}. If the field name isn't part of the specified class, then returns an exception.
	 * Returns {@link Persist.Null} if no {@link NodeBuilder} {@link Class} was specified in the annotation.
	 * @param clazz The specified {@link Class} that contains the specified field name
	 * @param fieldName The name of the field for which to return the {@link NodeBuilder} {@link Class}.
	 * @return The {@link NodeBuilder} {@link Class} associated with the annotation, or {@link Persist.Null} if no
	 * {@link NodeBuilder} {@link Class} was specified in the annotation.
	 */
	public static Class< ? > getNodeBuilderClass( final Class< ? > clazz, final String fieldName )
	{
		Class< ? > nodeBuilderClass = null; 
		try
		{
			// try for the field, if it isn't there it'll throw an exception.
			nodeBuilderClass = getNodeBuilderClass( clazz.getDeclaredField( fieldName ) );
		}
		catch( ReflectiveOperationException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The specified class does not have a field with the specified name."  + Constants.NEW_LINE);
			message.append( "  Specified Class: " + clazz + Constants.NEW_LINE );
			message.append( "  Specified Field Name: " + fieldName );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return nodeBuilderClass;
	}

	/**
	/**
	 * Retrieves the {@link NodeBuilder} {@link Class} associated with the field name of the specified
	 * {@link Class}. If the field name isn't part of the specified class, then returns an exception.
	 * Returns {@link Persist.Null} if no {@link NodeBuilder} {@link Class} was specified in the annotation.
	 * @param field The field for which to retrieve the associated {@link NodeBuilder}.
	 * @return The {@link NodeBuilder} {@link Class} associated with the annotation, or {@link Persist.Null} if no
	 * {@link NodeBuilder} {@link Class} was specified in the annotation.
	 */
	public static Class< ? > getNodeBuilderClass( final Field field )
	{
		// see if the field has a @Persist( instantiateAs = XXXX.class ) annotation
		final Persist annotation = field.getAnnotation( Persist.class );
		Class< ? > nodeBuilderClass = null; 
		if( annotation != null )
		{
			nodeBuilderClass = annotation.useNodeBuilder();
		}
		return nodeBuilderClass;
	}
	
	/**
	 * Returns true if the specified field name of the specified {@link Class} has a {@link NodeBuilder} annotation;
	 * false otherwise. 
	 * @param clazz The specified {@link Class}
	 * @param fieldName The field name to check for a {@link NodeBuilder} annotation.
	 * @return true if the specified field name of the specified {@link Class} has a {@link NodeBuilder} annotation;
	 * false otherwise.
	 */
	public static boolean hasNodeBuilderAnnotation( final Class< ? > clazz, final String fieldName )
	{
		boolean hasAnnotation = false;
		try
		{
			// try to get the field or skip over the next line to catch the no such field exception
			final Field field = clazz.getDeclaredField( fieldName );
			
			// has the field, but does it have the annotation
			final Class< ? > builderClass = getNodeBuilderClass( field );
			hasAnnotation = ( builderClass != Persist.Null.class ) && ( builderClass != null );
		}
		catch( NoSuchFieldException e ) {}
		return hasAnnotation;
	}
	
	/**
	 * Finds the item associated with the specified {@link Class}. If the specified class
	 * doesn't have an associated item, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the items map for the
	 * specified class associating it with the returned item (performance speed-up for
	 * subsequent calls).
	 * @param clazz The {@link Class} for which to find a item
	 * @return the item associated with the specified {@link Class}
	 */
	public static < T extends Copyable< T > > T getItemOrAncestor( final Class< ? > clazz, final Map< Class< ? >, T > items )
	{
		// simplest case is that the info node builders map has an entry for the class
		T item = items.get( clazz );
		
		// if the info node builder didn't have a direct entry, work our way up the inheritance
		// hierarchy, and find the closed parent class, assigning it its associated info node builder
		if( item == null )
		{
			// run through the available info node builders holding the distance (number of levels in the
			// inheritance hierarchy) they are from the specified class
			final IntegerOrderedSeries< Class< ? > > hierarchy = new IntegerOrderedSeries<>();
			for( Map.Entry< Class< ? >, T > entry : items.entrySet() )
			{
				final Class< ? > targetClass = entry.getKey();
				final int level = ReflectionUtils.calculateClassDistance( clazz, targetClass );
				if( level > -1 )
				{
					hierarchy.add( level, targetClass );
				}
			}
			
			// if one or more parent classes were found, then take the first one,
			// which is the closest one, grab its info node builder, and add an entry for the
			// specified class to the associated info node builder for faster subsequent look-ups
			if( !hierarchy.isEmpty() )
			{
				final Class< ? > closestParent = hierarchy.getFirstValue();
				item = items.get( closestParent );
				items.put( clazz, item.getCopy() );
			}
		}
		return item;
	}

	
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );

		System.out.println( "List -> Collection: distance = " + calculateClassDistance( List.class, Collection.class, -1 ) );
		System.out.println( "List -> Iterable: distance = " + calculateClassDistance( List.class, Iterable.class, -1 ) );
		System.out.println( "RunnableScheduledFuture -> Comparable: distance = " + calculateClassDistance( RunnableScheduledFuture.class, Comparable.class, -1 ) );
		System.out.println( "D -> A: distance = " + calculateClassDistance( D.class, A.class, -1 ) );
		System.out.println( "D -> B: distance = " + calculateClassDistance( D.class, B.class, -1 ) );
		System.out.println( "D -> Aprime: distance = " + calculateClassDistance( D.class, Aprime.class, -1 ) );
		System.out.println( "Econcrete -> A: distance = " + calculateClassDistance( Econcrete.class, A.class, -1 ) );
		System.out.println( "Econcrete -> B: distance = " + calculateClassDistance( Econcrete.class, B.class, -1 ) );
		System.out.println( "Econcrete -> D: distance = " + calculateClassDistance( Econcrete.class, D.class, -1 ) );
		System.out.println( "Econcrete -> Aprime: distance = " + calculateClassDistance( Econcrete.class, Aprime.class, -1 ) );
		System.out.println( "Fconcrete -> A: distance = " + calculateClassDistance( Fconcrete.class, A.class, -1 ) );
		System.out.println( "Econcrete -> Econcrete: distance = " + calculateClassDistance( Econcrete.class, Econcrete.class, -1 ) );
		System.out.println( "Fconcrete -> Econcrete: distance = " + calculateClassDistance( Fconcrete.class, Econcrete.class, -1 ) );
		System.out.println( "Gconcrete -> Econcrete: distance = " + calculateClassDistance( Gconcrete.class, Econcrete.class, -1 ) );
	}
}
