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
package org.freezedry.persistence.containers.orderedseries;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.freezedry.persistence.utils.Require;


/**
 * 
 * @author Robert Philipp
 */
public class IntegerOrderedSeries< V > extends TaggedOrderedSeries< Integer, V > {
	/**
	 * Constructor that sets the initial capacity to the specified amount
	 * @param initialCapacity The specified initial capacity
	 */
	public IntegerOrderedSeries( final int initialCapacity )
	{
		super( initialCapacity );
	}
	
	/**
	 * Constructor that sets the initial capacity to the default amount
	 */
	public IntegerOrderedSeries()
	{
		super();
	}
	
	/**
	 * Constructs a tagged time series based on the specified tagged time series 
	 * @param series The list containing the representation of the series
	 */
	protected IntegerOrderedSeries( IntegerOrderedSeries< V > series )
	{
		super( series );
	}
	
	/**
	 * Removes the entry for the specified date. If the time doesn't exist,
	 * returns null; otherwise returns a reference to the removed value
	 * @param time The time value
	 * @return If the date doesn't exist, returns null; otherwise returns a reference to
	 * the removed object
	 */
	public Entry< Integer, V > remove( final int time )
	{
		return super.remove( time );
	}
	
	/**
	 * Returns the first time
	 * @return the first time
	 */
	public Integer getFirstTime()
	{
		Integer time = null;
		final Entry< Integer, V > entry = getFirstEntry();
		if( entry != null )
		{
			time = entry.getKey();
		}
		return time;
	}
	
	/**
	 * Returns the first value
	 * @return the first value
	 */
	public V getFirstValue()
	{
		V amount = null;
		final Entry< Integer, V > entry = getFirstEntry();
		if( entry != null )
		{
			amount = entry.getElement();
		}
		return amount;
	}
	
	/**
	 * Returns the last time
	 * @return the last time
	 */
	public Integer getLastTime()
	{
		Integer time = null;
		final Entry< Integer, V > entry = getLastEntry();
		if( entry != null )
		{
			time = entry.getKey();
		}
		return time;
	}
	
	/**
	 * Returns the last value
	 * @return the last value
	 */
	public V getLastValue()
	{
		V amount = null;
		final Entry< Integer, V > entry = getLastEntry();
		if( entry != null )
		{
			amount = entry.getElement();
		}
		return amount;
	}
	
	/**
	 * Returns the time preceding the specified time. If the specified time
	 * falls before the first time, then returns null.
	 * @param time The specified time
	 * @return the time preceding the specified time. If the specified time
	 * falls before the first time, then returns NaN.
	 */
	public Integer getPrecedingTime( final int time )
	{
		Integer precedingDate = null;
		final Entry< Integer, V > entry = getPrecedingEntry( time );
		if( entry != null )
		{
			precedingDate = entry.getKey();
		}
		return precedingDate;
	}
	
	/**
	 * Returns the value associated with the entry just preceding the specified time. If 
	 * the specified time falls before the first time, then returns null.
	 * @param time The specified time
	 * @return the value associated with the entry just preceding the specified time. If 
	 * the specified time falls before the first time, then returns null.
	 */
	public V getPrecedingValue( final int time )
	{
		V amount = null;
		final Entry< Integer, V > entry = getPrecedingEntry( time );
		if( entry != null )
		{
			amount = entry.getElement();
		}
		return amount;	
	}
	
	/**
	 * Returns the time following the specified time. If the specified time
	 * falls after the last time, then returns null.
	 * @param time The specified time
	 * @return the time following the specified time. If the specified time
	 * falls after the last time, then returns NaN.
	 */
	public Integer getFollowingTime( final int time )
	{
		Integer followingDate = null;
		final Entry< Integer, V > entry = getFollowingEntry( time );
		if( entry != null )
		{
			followingDate = entry.getKey();
		}
		return followingDate;
	}
	
	/**
	 * Returns the value associated with the entry just following the specified time. If 
	 * the specified time falls after the last time, then returns null.
	 * @param time The specified time
	 * @return the value associated with the entry just following the specified time. If 
	 * the specified time falls after the last time, then returns null.
	 */
	public V getFollowingValue( final int time )
	{
		V amount = null;
		final Entry< Integer, V > entry = getFollowingEntry( time );
		if( entry != null )
		{
			amount = entry.getElement();
		}
		return amount;	
	}

	/**
	 * Returns a string representation of the coupon stream
	 * @return a string representation of the coupon stream
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		final Iterator< Entry< Integer, V > > iterator = iterator();
		while( iterator.hasNext() )
		{
			final Entry< Integer, V > entry = iterator.next();
			buffer.append( entry.getKey().toString() + ", " );
			buffer.append( entry.getElement().toString() + "\n" );
		}
		
		return buffer.toString();
	}

	/**
	 * Converts a specified Object into a IntegerOrderedSeries< T >, assuming that is what
	 * the object represents. If the Object is not a IntegerOrderedSeries< T >, then this
	 * method throws a class cast exception.
	 * @param <T> The type parameter of the IntegerOrderedSeries
	 * @param clazz The type of the IntegerOrderedSeries
	 * @param object The Object to cast
	 * @return the object cast to a IntegerOrderedSeries< T >
	 * @throws ClassCastException if the object is not of type IntegerOrderedSeries< T >
	 */
	@SuppressWarnings( "unchecked" )
	public static < T > IntegerOrderedSeries< T > cast( final Class< T > clazz, final Object object )
	{
		// ensure that the object is at least a BasicOrderedSeries
		if( object instanceof BasicOrderedSeries< ? > )
		{
			// safe cast because IntegerOrderedSeries< ? > is reified
			final Iterator< ? > iter = ((IntegerOrderedSeries< ? >)object).iterator();
			while( iter.hasNext() )
			{
				// grab the IntegerOrderedSeries.Entry< Integer, T > as an Object
				final Object entry = iter.next();
				
				// grab the element of the entry as an Object
				final Object element = ((IntegerOrderedSeries.Entry< ?, ? >)entry).getElement();
				
				// perform a type cast to ensure that it is the same time the type of Class
				clazz.cast( element );
			}
			
			// safe because we've just cast every element in the time series to 
			// the type specified as the type parameter of Class (i.e. T). Any cast
			// class exceptions would have thrown a CastClassException
			return (IntegerOrderedSeries< T >)object;
		}
		else
		{
			throw new ClassCastException( "Object not of type: IntegerOrderedSeries< " + clazz.getSimpleName() + " >" );			
		}
	}
	
	/**
	 * Returns a copy of the specified time series
	 * @param <T> The element type held in the time series
	 * @param series The time series to copy
	 * @return a copy of the specified time series
	 */
	public static < T > IntegerOrderedSeries< T > copy( final IntegerOrderedSeries< T > series )
	{
		Require.notNull( series );
		final IntegerOrderedSeries< T > copy = new IntegerOrderedSeries< T >( series.size() );
		
		for( IntegerOrderedSeries.Entry< Integer, T > entry : series )
		{
			copy.add( entry.getKey(), entry.getElement() );
		}
		
		return copy;
	}
	
	/**
	 * Returns a copy of the specified time series, but converts all the keys
	 * to an integer.
	 * @param <T> The element type held in the time series
	 * @param series The time series to copy
	 * @return a copy of the specified time series
	 */
	public static < T > IntegerOrderedSeries< T > copy( final BasicOrderedSeries< T > series )
	{
		Require.notNull( series );
		final IntegerOrderedSeries< T > copy = new IntegerOrderedSeries< T >( series.size() );
		
		for( BasicOrderedSeries.Entry< Double, T > entry : series )
		{
			copy.add( (int)(double)entry.getKey(), entry.getElement() );
		}
		
		return copy;
	}

	/**
	 * Creates a {@link Map} representation of the {@link IntegerOrderedSeries} where the key of the map
	 * is the key in the ordered series, and the value in the map is the element in the ordered series.
	 * @param <K> The key type
	 * @param <E> The element type
	 * @param series The {@link IntegerOrderedSeries} to be converted to a map
	 * @return a {@link Map} representation of the {@link IntegerOrderedSeries} where the key of the map
	 * is the key in the ordered series, and the value in the map is the element in the ordered series.
	 */
	public static < E > Map< Integer, E > toMap( final IntegerOrderedSeries< E > series )
	{
		final Map< Integer, E > map = new LinkedHashMap< Integer, E >();
		for( IntegerOrderedSeries.Entry< Integer, E > entry : series )
		{
			map.put( entry.getKey(), entry.getElement() );
		}
		return map;
	}
	
//	/**
//	 * Creates a {@link IntegerOrderedSeries} from a {@link Map}. The keys in the ordered series will be sorted
//	 * based on the key's natural integer ordering. The elements in the ordered series are the associated values 
//	 * from the {@link Map}.
//	 * @param <K> The key type
//	 * @param <E> The element type
//	 * @param map The {@link Map} to be converted into a {@link IntegerOrderedSeries}
//	 * @return a {@link IntegerOrderedSeries} from a {@link Map}. The keys in the ordered series will be sorted
//	 * based on the key's natural integer ordering. The elements in the ordered series are the associated values 
//	 * from the {@link Map}.
//	 */
//	public static < E > IntegerOrderedSeries< E > fromMap( final Map< Integer, E > map )
//	{
//		final IntegerOrderedSeries< E > series = new IntegerOrderedSeries< E >( map.size() );
//		for( Map.Entry< Integer, E > entry : map.entrySet() )
//		{
//			series.add( entry.getKey(), entry.getValue() );
//		}
//		return series;
//	}
//
//	/**
//	 * Creates a {@link IntegerOrderedSeries} from two lists. The first list holds the keys to the ordered
//	 * series. The second list holds the elements of the series. The two lists must have the same size.
//	 * @param <E> The element type
//	 * @param keys The keys to the list
//	 * @param elements The elements of the list
//	 * @return A {@link IntegerOrderedSeries} constructed from the two specified lists
//	 */
//	public static < E > IntegerOrderedSeries< E > fromLists( final List< Integer > keys, final List< E > elements )
//	{
//		Require.equalSize( keys, elements );
//		
//		final IntegerOrderedSeries< E > series = new IntegerOrderedSeries< E >( keys.size() );
//		
//		final Iterator< Integer > keyIter = keys.iterator();
//		final Iterator< E > elemIter = elements.iterator();
//		while( keyIter.hasNext() && elemIter.hasNext() )
//		{
//			series.add( keyIter.next(), elemIter.next() );
//		}
//		
//		return series;
//	}

	/**
	 * Converts an IntegerOrderedSeries to a BasicOrderedSeries
	 * @param <T> The type parameter of the elements of the time series
	 * @param series The time series to convert
	 * @return a BasicOrderedSeries version of the IntegerOrderedSeries
	 */
	public static < T > BasicOrderedSeries< T > convertToBasicOrderedSeries( final IntegerOrderedSeries< T > series )
	{
		final BasicOrderedSeries< T > copy = new BasicOrderedSeries< T >();
		for( IntegerOrderedSeries.Entry< Integer, T > entry : series )
		{
			copy.add( (double)entry.getKey(), entry.getElement() );
		}
		return copy;
	}

	/**
	 * Creates an unmodifiable version of the time series
	 * @param <T> The type of the element
	 * @param series The time series which to wrap in an unmodifiable version
	 * @return an unmodifiable version of the time series
	 */
	public static < T > IntegerOrderedSeries< T > unmodifiable( final IntegerOrderedSeries< T > series )
	{
		return new UnmodifiableIntegerOrderedSeries< T >( series );
	}
}
