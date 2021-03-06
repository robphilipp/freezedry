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
 */
public class UnmodifiableBasicOrderedSeries< V > extends BasicOrderedSeries< V > {

	public static class UnmodifiableTaggedEntry< K extends Comparable< K >, E > extends TaggedEntry< K, E > {

		public UnmodifiableTaggedEntry( final Entry< K, E > entry )
		{
			super( entry.getKey(), entry.getElement() );
		}
		
		/*
		 * (non-Javadoc)
		 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries.TaggedEntry#setElement(java.lang.Object)
		 */
		@Override
		public void setElement( final E value )
		{
			throw new UnsupportedOperationException( "Unmodifiable TaggedEntry object" );
		}
	}
	
	public UnmodifiableBasicOrderedSeries( final BasicOrderedSeries< V > series )
	{
		super( series );
	}

	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#add(java.lang.Comparable, java.lang.Object)
	 */
	@Override
	public boolean add( final Double key, final V value )
	{
		throw new UnsupportedOperationException( "Unmodifiable TaggedOrderedSeries object" );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#remove(java.lang.Comparable)
	 */
	@Override
	public Entry< Double, V > remove( final Double key )
	{
		throw new UnsupportedOperationException( "Unmodifiable TaggedOrderedSeries object" );		
	}

	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getFirstEntry()
	 */
	@Override
	public Entry< Double, V > getFirstEntry()
	{
		return TaggedEntry.unmodifiable( super.getFirstEntry() );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getLastEntry()
	 */
	@Override
	public Entry< Double, V > getLastEntry()
	{
		return TaggedEntry.unmodifiable( super.getLastEntry() );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getFollowingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< Double, V > getFollowingEntry( final Double key )
	{
		return TaggedEntry.unmodifiable( super.getFollowingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getEntryOrFollowingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< Double, V > getEntryOrFollowingEntry( final Double key )
	{
		return TaggedEntry.unmodifiable( super.getEntryOrFollowingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getPrecedingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< Double, V > getPrecedingEntry( final Double key )
	{
		return TaggedEntry.unmodifiable( super.getPrecedingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#getEntryOrPrecedingEntry(java.lang.Comparable)
	 */
	@Override
	public Entry< Double, V > getEntryOrPrecedingEntry( final Double key )
	{
		return TaggedEntry.unmodifiable( super.getEntryOrPrecedingEntry( key ) );
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.synapse.containers.orederedseries.TaggedOrderedSeries#get(java.lang.Comparable)
	 */
	@Override
	public Entry< Double, V > get( final Double key )
	{
		return TaggedEntry.unmodifiable( super.get( key ) );
	}
}
