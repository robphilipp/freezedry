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

import java.security.InvalidParameterException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.freezedry.persistence.containers.orderedseries.UnmodifiableTaggedOrderedSeries.UnmodifiableTaggedEntry;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.Require;


/**
 * Dec 23, 2009
 * 
 * Ordered time series based on the natural ordering of keys (T). Provides insertion into the time series
 * based on natural ordering of the keys. Allows forward and reverse iteration from specified starting points.
 * 
 * All keys must implement Comparable< T > and follow the intended semantics.
 *
 * @author Robert Philipp
 */
public class TaggedOrderedSeries< T extends Comparable< T >, V > extends AbstractCollection< TaggedOrderedSeries.Entry< T, V > > {
		
	// default initial capacity of the list
	private static final int DEFAULT_INITIAL_CAPACITY = 100;
	
	// the list of time series entries
	private List< Entry< T, V > > series;

	/**
	 * Constructor that sets the initial capacity to the specified amount
	 * @param initialCapacity The specified initial capacity
	 */
	public TaggedOrderedSeries( final int initialCapacity )
	{
		Require.nonNegative( initialCapacity, "Initial Capacity" );
		series = new ArrayList< Entry< T, V > >( initialCapacity );
	}
	
	/**
	 * Constructor that sets the initial capacity to the default amount
	 */
	public TaggedOrderedSeries()
	{
		this( DEFAULT_INITIAL_CAPACITY );
	}
	
	/**
	 * Constructs a tagged time series based on the specified tagged time series 
	 * @param series The list containing the representation of the series
	 */
	protected TaggedOrderedSeries( TaggedOrderedSeries< T, V > series )
	{
		Require.notNull( series );
		this.series = series.series;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
    public Iterator< Entry< T, V > > iterator()
    {
		return new ForwardIterator< T, V >( series );
    }
	
	/**
	 * Returns an iterator that starts at the time series key on or following
	 * the specified key (i.e. the Iterator.next() method will return
	 * the time series following the specified key if the specified key does
	 * not fall on a time series key, otherwise if the specified key is on a cash
	 * flow key, then that will be the next return time series).
	 * @param key The specfied key
	 * @return an iterator that starts at the time series key following
	 * the specified key (i.e. the Iterator.next() method will return
	 * the time series following the specified key).
	 */
	public Iterator< Entry< T, V > > iterator( final T key )
	{
		int index = findIndexForKeyOrFollowingKey(key );
		return new ForwardIterator< T, V >( series, index );
	}
	
	/**
	 * Returns an iterator that starts at the last time series and progresses
	 * to the first one with each call the {@link ReverseIterator.next()}
	 * @return an iterator that starts at the last time series and progresses
	 * to the first one
	 */
	public Iterator< Entry< T, V > > reverseIterator()
	{
		return new ReverseIterator< T, V >( series );
	}

	/**
	 * Returns a reverse iterator that starts at the specified key if that key
	 * is a time series key, or on the preceding time series key if the specified 
	 * key is not a time series key.
	 * @param key The specified key
	 * @return a reverse iterator that starts at the specified key if that key
	 * is a time series key, or on the preceding time series key if the specified 
	 * key is not a time series key.
	 */
	public Iterator< Entry< T, V > > reverseIterator( final T key )
	{
		int index = findIndexForKeyOrPrecedingKey( key );
		return new ReverseIterator< T, V >( series, index );
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
    public int size()
    {
	    return series.size();
    }
	
	/**
	 * Appends the specified series to the end of the this series
	 * @param series The series to append
	 */
	public void append( TaggedOrderedSeries< T, V > series )
	{
		for( TaggedOrderedSeries.Entry< T, V > entry : series )
		{
			add( entry.getKey(), entry.getElement() );
		}
	}
		
	/**
	 * Adds a time series entry (key, value(s)) if the key doesn't already exist in 
	 * the time-series. If the key doesn't already exist, and it was successfully added,
	 * then returns true; otherwise, returns false
	 * @param key The time series key
	 * @param value The value of the time series on this key
	 * @return true if the time series entry didn't already exist and was successfully 
	 * added; false otherwise
	 */
	public boolean add( final T key, final V value )
	{
		boolean wasAdded = false;
		if( series.isEmpty() )
		{
			wasAdded = series.add( new TaggedEntry< T, V >( key, value ) );
		}
		else
		{
			// if the new key goes onto the end, the binary search ends up
			// being inefficient, and just checking, and then adding speeds
			// the add method up considerably when the alogorithm using the
			// time series is adding things in order.
			if( key.compareTo( getLastEntry().getKey() ) > 0 )
			{
				wasAdded = series.add( new TaggedEntry< T, V >( key, value ) );								
			}
			else if( key.compareTo( getFirstEntry().getKey() ) < 0 )
			{
				series.add( 0, new TaggedEntry< T, V >( key, value ) );
				wasAdded = true;
			}
			else
			{
				final int index = findIndexForKeyOrFollowingKey( key );
				
				// if the index comes back as -1, then we add this on to the end.
				// in this case, the key followed the last time series key
				if( index == -1 )
				{
					wasAdded = series.add( new TaggedEntry< T, V >( key, value ) );				
				}
				else
				{
					// if the key doesn't already exist as a time series key, then add it
					if( findIndex( key ) == -1 )
					{	
						series.add( index, new TaggedEntry< T, V >( key, value ) );
						wasAdded = true;
					}
				}
			}
		}
		
		return wasAdded;
	}
	
	/**
	 * Removes the time series entry for the specified key. If the key doesn't exist,
	 * returns null; otherwise returns a reference to the removed object
	 * @param key The specified key
	 * @return If the key doesn't exist, returns null; otherwise returns a reference to
	 * the removed object
	 */
	public Entry< T, V > remove( final T key )
	{
		Entry< T, V > entry = null;
		int index;
		if( ( index = findIndex( key ) ) != -1 )
		{
			entry = series.remove( index );
		}
		return entry;
	}
	
	/**
	 * Sets the value associated with the key and returns the old value. If the key is not found 
	 * in the current ordered series, the returns null.
	 * @param key The key for which to the set the value
	 * @param value The new value 
	 * @return The previous value; or null if the key wasn't in the ordered series.
	 */
	public V set( final T key, final V value )
	{
		V oldValue = null;
		int index;
		if( ( index = findIndex( key ) ) != -1 )
		{
			final Entry< T, V > entry = series.get( index );
			oldValue = entry.getElement();
			entry.setElement( value );
		}
		
		return oldValue;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override
	public void clear()
	{
		series.clear();
	}
			
	/**
	 * Returns an ordered list of dates that correspond to the time series
	 * @return an ordered list of dates that correspond to the time series
	 */
	public List< T > getKeyList()
	{
		List< T > keys = new ArrayList< T >();
		for( Entry< T, V > entry : series )
		{
			keys.add( entry.getKey() );
		}
		
		return Collections.unmodifiableList( keys );
	}

	/**
	 * Returns a list of the time series values in ascending order aligned
	 * with the time series dates
	 * @return a list of the time series values in ascending order aligned
	 * with the time series dates
	 */
	public List< V > getElementList()
	{
		List< V > elements = new ArrayList< V >();
		for( Entry< T, V > entry : series )
		{
			elements.add( entry.getElement() );
		}
		
		return Collections.unmodifiableList( elements );
	}
	
	/**
	 * Returns the index of the specified key
	 * @param key The key for which to find the index
	 * @return the index of the specified key
	 */
	public int indexOf( T key )
	{
		return getKeyList().indexOf( key );
	}
	
	/*
	 * Performs a binary search to find the index on or after the specified key.
	 * If there is a time series entry with the specified key, then it returns that index.
	 * If the parameter <code>insertion</code> is set to true, then if the specified key
	 * doesn't exist, it returns the index of the following time series key, or -1 if the 
	 * specified key falls beyond the last time series key. If the parameter 
	 * <code>insertion</code> is set to false, then if the specified key doesn't exist,
	 * it returns -1.
	 * @param key the key as an integer in the yyyyMMdd format
	 * @param insertion set to true for finding the nearest time series key
	 * @param nextDate set to true for returning the next time series as the nearest time series key.
	 * When false it returns the previous time series key as the nearest time series key. This 
	 * parameter is only used when the <code>insertion</code> parameter is true.
	 * @return If there is a time series entry with the specified key, then it returns that index.
	 * If the parameter <code>insertion</code> is set to true, then if the specified key
	 * doesn't exist, it returns the index of the following time series key, or -1 if the 
	 * specified key falls beyond the last time series key. If the parameter 
	 * <code>insertion</code> is set to false, then if the specified key doesn't exist,
	 * it returns -1.
	 */
	private int binarySearch( final T key, final boolean insertion, final boolean nextKey )
	{
		// upper and lower bounds that the index can take
		int lowerBound = 0;
		int upperBound = series.size()-1;

		int index = -1;
		
		// keep bisecting the region in which the specified key falls
		// until we find the key, or until the bounds invert
		while( lowerBound <= upperBound )
		{
			int midPoint = ( upperBound + lowerBound ) / 2;
			
			// grab the index
			T searchKey = series.get( midPoint ).getKey();
			
			//if( key == searchDate )
			if( key.compareTo( searchKey ) == 0 )
			{
				index = midPoint;
				break;
			}
			else
			//if( key < searchDate )
			if( key.compareTo( searchKey ) < 0 )
			{
				upperBound = midPoint - 1;
			}
			else
			{
				lowerBound = midPoint + 1;
			}
		}
		
		// if the bounds have inverted, and lower bound is less than then
		// number of elements in the list, then the index is the next element
		// in the list
		if( insertion && nextKey && index == -1 && lowerBound < series.size() )
		{
			index = lowerBound;
		}

		if( insertion && !nextKey && index == -1 )
		{
			final int lastIndex = series.size()-1;
			if( key.compareTo( ((TaggedEntry< T, V >)series.get( lastIndex )).getKey() ) > 0 )
			{
				index = lastIndex;
			}
			else
			if( lowerBound < series.size() )
			{
				index = upperBound;
			}
		}

		return index;
	}
		
	/*
	 * Returns the index of the specified key if it exists. If the specified key 
	 * doens't exist, it returns the index of the next key (i.e. the key in the time series
	 * the follows the specified key), or -1 if the specified key is past all the 
	 * time series dates.
	 * @param key the specified key
	 * @return the index of the specified key if it exists. If the specified key 
	 * doens't exist, it returns the index of the next key (i.e. the time series key
	 * the follows the specified key), or -1 if the specified key is past all the 
	 * time series dates.
	 */
	private int findIndexForKeyOrFollowingKey( final T key )
	{
		return binarySearch( key, true, true );
	}
	
	/*
	 * Returns the index of the specified key if it exists. If the specified key 
	 * doens't exist, it returns the index of the previous key (i.e. the time series key
	 * the precedes the specified key), or -1 if the specified key is before all the 
	 * time series dates.
	 * @param key the key as an integer in the yyyyMMdd format
	 * @return the index of the specified key if it exists. If the specified key 
	 * doens't exist, it returns the index of the previous key (i.e. the time series key
	 * the precedes the specified key), or -1 if the specified key is before all the 
	 * time series dates.
	 */
	private int findIndexForKeyOrPrecedingKey( final T key )
	{
		return binarySearch( key, true, false );
	}
		
	/*
	 * Returns the index of the specified key. If the key doesn't exist, returns -1
	 * @param key the key as an integer in the yyyyMMdd format
	 * @return the index of the specified key. If the key doesn't exist, returns -1
	 */
	private int findIndex( final T key )
	{
		return binarySearch( key, false, true );
	}
	
	/*
	 * Returns the time series key following the specified key. If the key falls on 
	 * an existing time series key, then it will return the next key. If that next key
	 * is beyond the last time series key, returns -1.
	 * @param key The specified key
	 * @return the time series key following the specified key. If the key falls on 
	 * an existing time series key, then it will return the next key. If that next key
	 * is beyond the last time series key, returns -1.
	 */
	private int findIndexForFollowingKey( final T key )
	{
		// grab the index that either falls on the key or comes next
		// if that time series doesn't exist
		int index = findIndexForKeyOrFollowingKey( key );
		
		// if there is a key or follow on key, we need to determine if
		// the specified key exists, in which case we need to increment
		// the index to the next key.
		if( index != -1 )
		{
			int index2 = findIndex( key );
			
			// if the key exists, increment, but if this is the last key,
			// then we say that there is no key after.
			if( index2 != -1 )
			{
				index++;
				if( index >= size() )
				{
					index = -1;
				}
			}
		}
		return index;
	}
	
	/*
	 * Returns the time series key preceding the specified key. If the key falls on 
	 * an existing time series key, then it will return the next key. If that preceding key
	 * is before the first time series key, returns -1.
	 * @param key The specified key
	 * @return the time series key preceding the specified key. If the key falls on 
	 * an existing time series key, then it will return the next key. If that preceding key
	 * is before the first time series key, returns -1.
	 */
	private int findIndexForPrecedingKey( final T key )
	{
		// grab the index that either falls on the key or precedes the key
		// if that time series doesn't exist
		int index = findIndexForKeyOrPrecedingKey( key );
		
		// if there is a key or preceding key, we need to determine if
		// the specified key exists, in which case we need to decrement to
		// the index to the previous key.
		if( index != -1 )
		{
			int index2 = findIndex( key );
			
			// if the key exists, decrement, but if this is the first key,
			// then we say that there is no key before.
			if( index2 != -1 )
			{
				index--;
				if( index < 0 )
				{
					index = -1;
				}
			}
		}
		return index;
	}

	/**
	 * Returns the first element of the time series or null if the are no 
	 * entries in the time series.
	 * @return the first element of the time series or null if the are no 
	 * entries in the time series.
	 */
	public Entry< T, V > getFirstEntry()
	{
		Entry< T, V > entry = null;
		if( !series.isEmpty() )
		{
			entry = series.get( 0 );
		}
		return entry;
	}
		
	/**
	 * Returns the element of the last time series entry, or null if there are
	 * no time series entries.
	 * @return the element of the last time series entry, or null if there are
	 * no time series entries.
	 */
	public Entry< T, V > getLastEntry()
	{
		Entry< T, V > entry = null;
		if( !series.isEmpty() )
		{
			entry = series.get( size()-1 );
		}
		return entry;
	}
		
	/**
	 * Returns the time series entry following the entry with the specified key. If the
	 * specified key follows the last time series entry, then returns null.
	 * @param key The specified key
	 * @return the next entry in the time series following the specified key. If the
	 * specified key follows the last time series key, then returns null.
	 */
	public Entry< T, V > getFollowingEntry( final T key )
	{
		Entry< T, V > entry = null;
		
		final int index = findIndexForFollowingKey( key );
		if( index != -1 )
		{
			entry = series.get( index );
		}
		
		return entry;
	}
	
	/**
	 * Returns the time series entry or the entry following the entry with the specified key. If the
	 * specified key follows the last time series entry, then returns null.
	 * @param key The specified key
	 * @return the entry or the next entry in the time series following the specified key. If the
	 * specified key follows the last time series key, then returns null.
	 */
	public Entry< T, V > getEntryOrFollowingEntry( final T key )
	{
		Entry< T, V > entry = null;
		
		final int index = findIndexForKeyOrFollowingKey( key );
		if( index != -1 )
		{
			entry = series.get( index );
		}
		
		return entry;
	}
	
	/**
	 * Returns the time series entry preceding the entry with the specified key. If the
	 * specified key precedes the first time series entry, then returns null.
	 * @param key The specified key
	 * @return the time series entry preceding the entry with the specified key. If the
	 * specified key precedes the first time series entry, then returns null.
	 */
	public Entry< T, V > getPrecedingEntry( final T key )
	{
		Entry< T, V > entry = null;
		
		final int index = findIndexForPrecedingKey( key );
		if( index != -1 )
		{
			entry = series.get( index );
		}
		
		return entry;
	}
	
	/**
	 * Returns the time series entry or the entry preceding the entry with the specified key. If the
	 * specified key precedes the first time series entry, then returns null.
	 * @param key The specified key
	 * @return the time series entry or the preceding preceding the entry with the specified key. If the
	 * specified key precedes the first time series entry, then returns null.
	 */
	public Entry< T, V > getEntryOrPrecedingEntry( final T key )
	{
		Entry< T, V > entry = null;
		
		final int index = findIndexForKeyOrPrecedingKey( key );
		if( index != -1 )
		{
			entry = series.get( index );
		}
		
		return entry;
	}
	
	/**
	 * Returns the number of remaining time series entries that fall on or after the specified key.
	 * @param key The specified key
	 * @param inclusive If set to true and key is contained in the time series, then it will
	 * include the key in the number of remaining keys. In other words, if true, returns the 
	 * number of keys that are greater or equal to the specified key. If false, returns the
	 * number of keys that are strictly greater to the specified key.
	 * @return the number of remaining time series entries that fall on or after the specified key.
	 */
	public int getNumFollowingKeys( final T key, final boolean inclusive )
	{
		int numKeys = 0;
		
		int index;
		if( !inclusive )
		{
			index = findIndexForFollowingKey( key );
		}
		else
		{
			index = findIndexForKeyOrFollowingKey( key );
		}
		
		if( index != -1 )
		{
			numKeys = size() - index;
		}
		
		return numKeys;
	}
	
	/**
	 * Returns the number of remaining time series entries that fall after the specified key.
	 * @param key The specified key
	 * @return the number of remaining time series entries that fall after the specified key.
	 */
	public int getNumFollowingKeys( final T key )
	{
		return getNumFollowingKeys( key, false );
	}
	
	/**
	 * Returns the number of remaining time series entries that fall on or before the specified key.
	 * @param key The specified key
	 * @param inclusive If set to true and key is contained in the time series, then it will
	 * include the key in the number of remaining keys. In other words, if true, returns the 
	 * number of keys that are less than or equal to the specified key. If false, returns the
	 * number of keys that are strictly less to the specified key.
	 * @return the number of remaining time series entries that fall on or before the specified key.
	 */
	public int getNumPrecedingKeys( final T key, final boolean inclusive )
	{
		int numKeys = 0;
		
		int index;
		if( !inclusive )
		{
			index = findIndexForPrecedingKey( key );
		}
		else
		{
			index = findIndexForKeyOrPrecedingKey( key );
		}
		
		if( index != -1 )
		{
			numKeys = index+1;
		}
		
		return numKeys;
	}
	
	/**
	 * Returns the number of entries that precede the specified key.
	 * @param key The specified key
	 * @return the number of remaining time series entries that precede the specified key.
	 */
	public int getNumPrecedingKeys( final T key )
	{
		return getNumPrecedingKeys( key, false );
	}

	/**
	 * Returns true if the time series contains the specified key; false otherwise
	 * @param key The specified key
	 * @return true if the time series contains the specified key; false otherwise
	 */
	public boolean contains( final T key )
	{
		return ( binarySearch( key, false, false ) != -1 );
	}
	
	/**
	 * Returns the entry associated with the specified key, or null if the 
	 * key doesn't exist in the time series.
	 * @param key The specified key
	 * @return The entry associated with the specified key, or null if the 
	 * key doesn't exist in the time series.
	 */
	public Entry< T, V > get( final T key )
	{
		Entry< T, V > entry = null;
		
		final int index = binarySearch( key, false, false );
		if( index != -1 )
		{
			entry = series.get( index );
		}
		
		return entry;
	}

	/**
	 * Bounds the tagged time series by the lower and upper bounds. Includes the
	 * lower bound, but exludes the upper bound.
	 * @param lowerBound The lower bound
	 * @param upperBound The upper bound
	 */
	public TaggedOrderedSeries< T, V > getBounded( final T lowerBound, final T upperBound )
	{
		final int lowerIndex = findIndexForKeyOrFollowingKey( lowerBound );
		final int upperIndex = findIndexForPrecedingKey( upperBound );

		return getSublist( lowerIndex, upperIndex );
	}

	/**
	 * Bounds the tagged time series by the lower and upper bounds. Includes the
	 * endpoints.
	 * @param lowerBound The lower bound
	 * @param upperBound The upper bound
	 */
	public TaggedOrderedSeries< T, V > getBoundedInclusive( final T lowerBound, final T upperBound )
	{
		final int lowerIndex = findIndexForKeyOrFollowingKey( lowerBound );
		final int upperIndex = findIndexForKeyOrPrecedingKey( upperBound );
		
		return getSublist( lowerIndex, upperIndex );
	}

	/**
	 * Bounds the tagged time series by the lower and upper bounds. Excludes the
	 * endpoints.
	 * @param lowerBound The lower bound
	 * @param upperBound The upper bound
	 */
	public TaggedOrderedSeries< T, V > getBoundedExlusive( final T lowerBound, final T upperBound )
	{
		final int lowerIndex = findIndexForFollowingKey( lowerBound );
		final int upperIndex = Math.max( lowerIndex, findIndexForPrecedingKey( upperBound ) );

		return getSublist( lowerIndex, upperIndex );
	}

	/*
	 * Returns the sublist between the lower and upper indexes. Includes the lower
	 * and upper bounds.
	 * @param lower The lower index.
	 * @param upper The upper index.
	 * @return The sublist containing the Entry objects
	 */
	private TaggedOrderedSeries< T, V > getSublist( final int lower, final int upper )
	{
		if( lower < 0 || upper < lower )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Lower and upper bound indexes must be greater than 0. And the\n" );
			message.append( "upper bound index must be greater than or equal to the lower bound\n" );
			message.append( "index.\n" );
			message.append( "  lower bound index: " + lower + "\n" );
			message.append( "  upper bound index: " + upper + "\n" );
			throw new InvalidParameterException( message.toString() );
		}

		TaggedOrderedSeries< T, V > sublist = new TaggedOrderedSeries< T, V >( upper - lower );
		for( int i = lower; i <= upper; ++i )
		{
			sublist.add( series.get( i ) );
		}

		return sublist;
	}
	
	/**
	 * Returns true if the series have the same keys and elements, and is the same size
	 * @param series The series to test against this one
	 * @return true if the series are the same; false otherwise
	 */
	public boolean seriesEqual( TaggedOrderedSeries< T, V > series )
	{
		boolean isEqual = true;
		if( this.series.size() == series.size() )
		{
			for( Entry< T, V > entry : this.series )
			{
				final Entry< T, V > testEntry = series.get( entry.getKey() );
				if( !( testEntry != null && testEntry.getElement().equals( entry.getElement() ) ) )
				{
					isEqual = false;
				}
			}
		}
		else
		{
			isEqual = false;
		}
		return isEqual;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		for( Entry< T, V > entry : series )
		{
			buffer.append( entry.toString() ).append( Constants.NEW_LINE );
		}
		
		return buffer.toString();
	}
	
	/**
	 * Converts a specified Object into a TaggedOrderedSeries< K, E >, assuming that is what
	 * the object represents. If the Object is not a TaggedOrderedSeries< K, E >, then this
	 * method throws a class cast exception.
	 * @param <K> The type parameter for the key of the TaggedOrderedSeries
	 * @param <E> The type parameter for the element of the TaggedOrderedSeries
	 * @param keyClazz The type for the key of the TaggedOrderedSeries
	 * @param elementClazz The type for the element of the TaggedOrderedSeries
	 * @param object The Object to cast
	 * @return the object cast to a TaggedOrderedSeries< K, E >
	 * @throws ClassCastException if the object is not of type TaggedOrderedSeries< k, E >
	 */
	@SuppressWarnings( "unchecked" )
	public static < K extends Comparable< K >, E > TaggedOrderedSeries< K, E > cast( Class< K > keyClazz, Class< E > elementClazz, Object object )
	{
		// ensure that the object is at least a TaggedOrderedSeries
		if( object instanceof TaggedOrderedSeries< ?, ? > )
		{
			// safe cast because TaggedOrderedSeries< ?, ? > is reified
			Iterator< ? > iter = ((org.freezedry.persistence.containers.orderedseries.TaggedOrderedSeries< ?, ? >)object).iterator();
			while( iter.hasNext() )
			{
				// grab the BasicOrderedSeries.Entry< Double, T > as an Object
				Object entry = iter.next();
				
				// grab the element of the entry as an Object
				Object key = ((TaggedOrderedSeries.Entry< ?, ? >)entry).getKey();
				Object element = ((TaggedOrderedSeries.Entry< ?, ? >)entry).getElement();
				
				// perform a type cast to ensure that it is the same time the type of Class
				keyClazz.cast( key );
				elementClazz.cast( element );
			}
			
			// safe because we've just cast every element in the time series to 
			// the type specified as the type parameter of Class (i.e. T). Any cast
			// class exceptions would have thrown a CastClassException
			return (TaggedOrderedSeries< K, E >)object;
		}
		else
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not of type: TaggedOrderedSeries< " ).append( keyClazz.getSimpleName() )
				   .append( ", " ).append( elementClazz.getSimpleName() ).append( " >" );
			throw new ClassCastException( message.toString() );			
		}
	}

	/**
	 * Returns the minimum value of the elements in the time series. Requires
	 * that the element parameter type implement {@link Number} and {@link Comparable}.
	 * @param <K> Key type must implement {@link Comparable}.
	 * @param <E> Element type must must implement {@link Number} and {@link Comparable}.
	 * @param series Tagged time-series
	 * @return the minimum value of the elements in the time series
	 */
	public static < K extends Comparable< K >, E extends Number & Comparable< E > > E min( final TaggedOrderedSeries< K, E > series )
	{
		return Collections.min( series.getElementList() );
	}

	/**
	 * Returns the maximum value of the elements in the time series. Requires
	 * that the element parameter type implement {@link Number} and {@link Comparable}.
	 * @param <K> Key type must implement {@link Comparable}.
	 * @param <E> Element type must must implement {@link Number} and {@link Comparable}.
	 * @param series Tagged time-series
	 * @return the maximum value of the elements in the time series
	 */
	public static < K extends Comparable< K >, E extends Number & Comparable< E > > E max( final TaggedOrderedSeries< K, E > series )
	{
		return Collections.max( series.getElementList() );
	}
	
	/**
	 * Returns a copy of the specified time series
	 * @param <K> Key type must implement {@link Comparable}.
	 * @param <E> Element type must must implement {@link Number} and {@link Comparable}.
	 * @param series The time series to copy
	 * @return a copy of the specified time series
	 */
	public static < K extends Comparable< K >, E > TaggedOrderedSeries< K, E > copy( final TaggedOrderedSeries< K, E > series )
	{
		Require.notNull( series );
		TaggedOrderedSeries< K, E > copy = new TaggedOrderedSeries< K, E >( series.size() );
		
		for( TaggedOrderedSeries.Entry< K, E > entry : series )
		{
			copy.add( entry.getKey(), entry.getElement() );
		}
		
		return copy;
	}
	
	/**
	 * Creates an unmodifiable version of the tagged time series
	 * @param <K> The keys in the time series
	 * @param <E> The elements of the time series
	 * @param series The tagged time series which to wrap in an unmodifiable version
	 * @return an unmodifiable version of the tagged time series
	 */
	public static < K extends Comparable< K >, E > TaggedOrderedSeries< K, E > unmodifiable( final TaggedOrderedSeries< K, E > series )
	{
		return new UnmodifiableTaggedOrderedSeries< K, E >( series );
	}
	
	/**
	 * Creates a {@link Map} representation of the {@link TaggedOrderedSeries} where the key of the map
	 * is the key in the ordered series, and the value in the map is the element in the ordered series.
	 * @param <K> The key type
	 * @param <E> The element type
	 * @param series The {@link TaggedOrderedSeries} to be converted to a map
	 * @return a {@link Map} representation of the {@link TaggedOrderedSeries} where the key of the map
	 * is the key in the ordered series, and the value in the map is the element in the ordered series.
	 */
	public static < K extends Comparable< K>, E > Map< K, E > toMap( TaggedOrderedSeries< K, E > series )
	{
		final Map< K, E > map = new LinkedHashMap< K, E >();
		for( TaggedOrderedSeries.Entry< K, E > entry : series )
		{
			map.put( entry.getKey(), entry.getElement() );
		}
		return map;
	}
	
	/**
	 * Creates a {@link TaggedOrderedSeries} from a {@link Map}. The keys in the ordered series will be sorted
	 * based on the key's {@link Comparable#compareTo(Object)} method. The elements in the ordered series are the 
	 * associated values from the {@link Map}.
	 * @param <K> The key type
	 * @param <E> The element type
	 * @param map The {@link Map} to be converted into a {@link TaggedOrderedSeries}
	 * @return a {@link TaggedOrderedSeries} from a {@link Map}. The keys in the ordered series will be sorted
	 * based on the key's {@link Comparable#compareTo(Object)} method. The elements in the ordered series are the 
	 * associated values from the {@link Map}.
	 */
	public static < K extends Comparable< K >, E > TaggedOrderedSeries< K, E > fromMap( final Map< K, E > map )
	{
		final TaggedOrderedSeries< K, E > series = new TaggedOrderedSeries< K, E >( map.size() );
		for( Map.Entry< K, E > entry : map.entrySet() )
		{
			series.add( entry.getKey(), entry.getValue() );
		}
		return series;
	}
	
	/**
	 * Creates a {@link TaggedOrderedSeries} from two lists. The first list holds the keys to the ordered
	 * series. The second list holds the elements of the series. The two lists must have the same size.
	 * @param <K> The key type which must implement {@link Comparable}
	 * @param <E> The element type
	 * @param keys The keys to the list
	 * @param elements The elements of the list
	 * @return A {@link TaggedOrderedSeries} constructed from the two specified lists
	 */
	public static < K extends Comparable< K >, E > TaggedOrderedSeries< K, E > fromLists( List< K > keys, List< E > elements )
	{
		Require.equalSize( keys, elements );
		
		final TaggedOrderedSeries< K, E > series = new TaggedOrderedSeries< K, E >( keys.size() );
		
		Iterator< K > keyIter = keys.iterator();
		Iterator< E > elemIter = elements.iterator();
		while( keyIter.hasNext() && elemIter.hasNext() )
		{
			series.add( keyIter.next(), elemIter.next() );
		}
		
		return series;
	}

	/**
	 * Interface for time series entries
	 *
	 * @param <T> The time series value type. For example, it could be a Double or a 
	 * List< Double >
	 */
	public interface Entry< K, E > extends Comparable< Entry< K, E > > {
		
		/**
		 * Returns the key held by this time series entry
		 * @return the key held by this time series entry
		 */
		K getKey();
		
		/**
		 * Returns the value held by this time series entry
		 * @return the value held by this time series entry
		 */
		E getElement();
		
		/**
		 * Sets the value of the element
		 * @param value The value of the element
		 */
		void setElement( E value );
	}

	/*
	 * Implementation of the time series Entry for the time series class.
	 *
	 * @param <T> The time series value type. For example, it could be a Double or a 
	 * List< Double >
	 */
	public static class TaggedEntry< K extends Comparable< K >, E > implements Entry< K, E > {
		
		// for the hashCode method
		private volatile int hashCode;

		private K key;
		private E element;
		
		/**
		 * Constructor that sets the time series key and value. Holds a copy of the key, but
		 * only a reference of the value type
		 * @param key The time series key
		 * @param element The time series value
		 */
		public TaggedEntry( K key, E element )
		{
			this.key = key;
			this.element = element;
		}

		/*
		 * (non-Javadoc)
		 * @see com.synapse.finance.calculator.cashflow.CashFlow.Entry#getDate()
		 */
		@Override
		public K getKey()
		{
			return key;
		}
					
		/*
		 * (non-Javadoc)
		 * @see com.synapse.finance.calculator.cashflow.CashFlow.Entry#getValue()
		 */
		@Override
		public E getElement()
		{
			return element;
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries.Entry#setElement(java.lang.Object)
		 */
		@Override
		public void setElement( E value )
		{
			element = value;
			hashCode = 0;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
        public int compareTo( Entry< K, E > entry )
        {
	        return key.compareTo( entry.getKey() );
        }

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals( Object object )
		{
			if( !(object instanceof Entry< ?, ? >) )
			{
				return false;
			}
			
			if( ((Entry< ?, ? >)object).getKey().equals( key ) &&
				((Entry< ?, ? >)object).getElement().equals( element ) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see org.sun.java.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			int result = hashCode;
			if( result == 0 )
			{
				result = 17;
				result = 31 * result + ( key == null ? 0 : key.hashCode() );
				result = 31 * result + ( element == null ? 0 : element.hashCode() );
				hashCode = result;
			}
			return hashCode;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "(" + key.toString() + ", " + element.toString() + ")";
		}
		
		/**
		 * Returns an unmodifiable version of the tagged entry
		 * @param <k> the key of the entry
		 * @param <e> the element held within the entry
		 * @param entry The tagged entry
		 * @return an unmodifiable version of the tagged entry
		 */
		public static < k extends Comparable< k >, e > UnmodifiableTaggedEntry< k, e > unmodifiable( final Entry< k, e > entry )
		{
			return new UnmodifiableTaggedEntry< k, e >( entry );
		}
	}

	/*
	 * Forward iterator for iterating from the first to last time series.
	 * 
	 * @param <T> The time series value type
	 */
	public static class ForwardIterator< K, E > implements Iterator< Entry< K, E > > {

		private int index;
		private List< Entry< K, E > > list;

		/**
		 * Constructs an iterator that starts at the specified index
		 * @param list The list over which to iterate
		 * @param index The starting index
		 */
		public ForwardIterator( List< Entry< K, E > > list, final int index )
		{
			// if the index is out of bounds, set it so it appears
			// to be finished iterating
			if( index < 0 || index >= list.size() )
			{
				this.index = list.size() - 1;
			}
			else
			// otherwise, the index is valid, and so set it so that the next
			// element returned will be the correct one.
			{
				this.index = index - 1;
			}
			this.list = list;
		}

		/**
		 * Constructor that accepts a reference to the list over which to
		 * iterate
		 * @param list The list over which to iterate
		 */
		public ForwardIterator( List< Entry< K, E > > list )
		{
			this( list, 0 );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return (index + 1 < list.size());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Entry< K, E > next()
		{
			index++;
			if( index < list.size() )
			{
				return list.get( index );
			}
			else
			{
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	/*
	 * Iterator that allows iteration from the last time series to the first
	 * 
	 * @param <T> The time series value type
	 */
	public static class ReverseIterator< K, E > implements Iterator< Entry< K, E > > {

		private int index;
		private List< Entry< K, E > > list;

		/**
		 * Constructor that creates an iterator that starts at the specified index
		 * @param list The list over which to iterate
		 * @param index The specified index
		 */
		public ReverseIterator( List< Entry< K, E > > list, final int index )
		{
			if( index < 0 )
			{
				this.index = -1;
			}
			else
			if( index >= list.size() )
			{
				this.index = list.size();
			}
			else
			{
				this.index = index+1;
			}
			this.list = list;
		}

		/**
		 * Consructor that accepts the list over which to iterate
		 * @param list The list over which to iterate
		 */
		public ReverseIterator( List< Entry< K, E > > list )
		{
			this( list, list.size() );
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return (index > 0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Entry< K, E > next()
		{
			index--;
			if( index >= 0 )
			{
				return list.get( index );
			}
			else
			{
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
