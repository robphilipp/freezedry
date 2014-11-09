package org.freezedry.persistence.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cube whose "lower left-hand side" sits on the origin.
 *
 * @author Robert Philipp
 */
public class Cube {

	private final List< Double > dimensions;

	/**
	 * Constructor for an n-dimensial cube, where n is the size of the specified dimensions list
	 * @param dimensions The list of dimensions. For example, a 3-d unit cube would be ( 1, 1, 1 ).
	 */
	public Cube( final List< Double > dimensions )
	{
		this.dimensions = new ArrayList<>();
		if( dimensions != null )
		{
			this.dimensions.addAll( dimensions.stream().map( Math::abs ).collect( Collectors.toList() ) );
		}
	}

	/**
	 * Constructor for an n-dimensional cube, where n is the size of the specified dimensions list
	 * @param dimensions The list of dimensions. For example, a 3-d unit cube would be ( 1, 1, 1 ).
	 */
	public Cube( final Double...dimensions )
	{
		// the casting is to handle the case where dimensions is null, so that we can call the
		// correct constructor
		this( Arrays.asList( (Double[])dimensions ) );
	}

	/**
	 * @return The number of dimensions. For example, a 3-d cube would return 3
	 */
	public int getDimension()
	{
		return dimensions.size();
	}

	/**
	 * Returns the dimension for the specified dimension. Confused? Yeah so am I. Suppose you have a
	 * 3-d cube specified by ( pi, 2pi, 6pi). Then the first dimension would be pi, and the third
	 * dimension, 6pi.
	 * @param dimension The number of the dimension for which to return the dimension.
	 * @return The dimension of the specified dimension
	 */
	public double getDimension( final int dimension )
	{
		return dimensions.get( dimension );
	}

	/**
	 * @return a list of the dimensions.
	 */
	public List< Double > getDimensions()
	{
		return new ArrayList<>( dimensions );
	}

	/**
	 * Returns true if the specified point falls within this cube; false otherwise
	 * @param point The point to test
	 * @return true if the specified point falls within this cube; false otherwise
	 */
	public boolean isInside( final Double...point )
	{
		return isInside( Arrays.asList( point ) );
	}

	/**
	 * Returns true if the specified point falls within this cube; false otherwise
	 * @param point The point to test
	 * @return true if the specified point falls within this cube; false otherwise
	 */
	public boolean isInside( final List< Double > point )
	{
		boolean isInside = false;
		if( point.size() == dimensions.size() && !dimensions.isEmpty() )
		{
			isInside = true;
			for( int i = 0; i < dimensions.size(); ++i )
			{
				if( point.get( i ) > dimensions.get( i ) )
				{
					isInside = false;
					break;
				}
			}
		}
		return isInside;
	}

	/**
	 * Returns true if the specified cube is entirely within this cube; otherwise false. Really, this is just asking
	 * whether a point is inside the cube, because all cubes have on corner on the origin.
	 * @param cube The cube to test for containment
	 * @return true if the specified cube is entirely within this cube; otherwise false
	 */
	public boolean isContained( final Cube cube )
	{
		return isInside( cube.dimensions );
	}

	/**
	 * @return The n-dimensional volume of the cube.
	 */
	public double getVolume()
	{
		double volume = ( dimensions.size() > 0 ? 1 : 0 );
		for( Double dimension : dimensions )
		{
			volume *= dimension;
		}
		return volume;
	}
}
