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

/**
 * @author Robert Philipp
 * 
 * Wraps the {@link TaggedOrderedSeries} into an unmodifiable version. Methods that allow
 * modification of the {@link TaggedOrderedSeries} throw and {@link UnsupportedOperationException}.
 * 
 * @param <T> The tag (or key) on which the order of the the element is based. The tag must implement {@link Comparable}.
 * @param <V> The value (or element) associated with the key.
 */
public class UnmodifiableTaggedOrderedSeries< T extends Comparable< T >, V > extends TaggedOrderedSeries< T, V > {

	/**
	 * @author Robert Philipp
	 * 
	 * Wraps the {@link TaggedOrderedSeries.TaggedEntry} class into an unmodifiable version. Methods that allow
	 * modification of the {@link TaggedOrderedSeries.TaggedEntry} throw and {@link UnsupportedOperationException}.
	 * 
	 * @param <K> The key (or tag) for the entry on which the order is based.
	 * @param <E> The element (or value) associated with the key.
	 */
	public static class UnmodifiableTaggedEntry< K extends Comparable< K >, E > extends TaggedEntry< K, E > {

		/**
		 * The {@link TaggedOrderedSeries.Entry} holding an item in the {@link TaggedOrderedSeries}.
		 * @param entry An item in the {@link TaggedOrderedSeries}
		 */
		public UnmodifiableTaggedEntry( final Entry< K, E > entry )
		{
			super( entry.getKey(), entry.getElement() );
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.synapse.containers.orderedseries.TaggedOrderedSeries.TaggedEntry#setElement(java.lang.Object)
		 */
		@Override
		public void setElement( final E value )
		{
			throw new UnsupportedOperationException( "Unmodifiable TaggedEntry object" );
		}
	}
	
	/**
	 * Constructor that creates an unmodifiable version of the specified {@link TaggedOrderedSeries}
	 * @param series The {@link TaggedOrderedSeries} from which to create an unmodifiable version
	 */
	public UnmodifiableTaggedOrderedSeries( final TaggedOrderedSeries< T, V > series )
	{
		super( series );
	}

	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#add(java.lang.Comparable, java.lang.Object)
	 */
	@Override
	public boolean add( final T key, final V value )
	{
		throw new UnsupportedOperationException( "Unmodifiable TaggedOrderedSeries object" );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#remove(java.lang.Comparable)
	 */
	@Override
	public Entry< T, V > remove( final T key )
	{
		throw new UnsupportedOperationException( "Unmodifiable TaggedOrderedSeries object" );		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orderedseries.TaggedOrderedSeries#clear()
	 */
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException( "Unmodifiable TaggedOrderedSeries object" );		
	}

	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getFirstEntry()
	 */
	@Override
	public Entry< T, V > getFirstEntry()
	{
		return TaggedEntry.unmodifiable( super.getFirstEntry() );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getLastEntry()
	 */
	@Override
	public Entry< T, V > getLastEntry()
	{
		return TaggedEntry.unmodifiable( super.getLastEntry() );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getFollowingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< T, V > getFollowingEntry( final T key )
	{
		return TaggedEntry.unmodifiable( super.getFollowingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getEntryOrFollowingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< T, V > getEntryOrFollowingEntry( final T key )
	{
		return TaggedEntry.unmodifiable( super.getEntryOrFollowingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getPrecedingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< T, V > getPrecedingEntry( final T key )
	{
		return TaggedEntry.unmodifiable( super.getPrecedingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getEntryOrPrecedingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< T, V > getEntryOrPrecedingEntry( final T key )
	{
		return TaggedEntry.unmodifiable( super.getEntryOrPrecedingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#get(java.lang.Comparable)
	 */
	@Override
	public Entry< T, V > get( final T key )
	{
		return TaggedEntry.unmodifiable( super.get( key ) );
	}
}
