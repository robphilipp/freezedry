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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * @author Robert Philipp
 */
public class Require {

	/**
	 * Throws exception if the value is not greater than or equal to zero
	 * @param <T> Extends from Number. Must have a doubleValue() method 
	 * @param value The value to check
	 * @param name The name of the field
	 * @throw IllegalArgumentException if the number is not greater than or equal to zero
	 */
	public static < T extends Number > void nonNegative( T value, String name )
	{
		if( value.doubleValue() < 0 )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Value must be greater than or equal to zero: " + name + "\n" );
			message.append( "  Specified value for " + name + ": " + value.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Throws exception if the numerator is not evenly divisible by the denominator. For example, if
	 * we want to ensure that the payment frequency per year is evenly divisible in the year, then
	 * we divide 12 (number of months, numerator) by the frequency (payments per year, denominator. 
	 * It is divisible if the remainder is zero. And the call would be {@link #divisible( 12, payFrequency )}
	 * @param numerator The top part of the fraction
	 * @param denominator The bottom part of the fraction
	 * @throws IllegalArgumentException if the numerator is not evenly divisible by the denominator
	 */
	public static void divisible( final int numerator, final int denominator )
	{
		if( numerator % denominator != 0 )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Numerator is not evenly divisible by the denominator.\n" );
			message.append( "  numerator: " + numerator + "\n" );
			message.append( "  denominator: " + denominator + "\n" );
			message.append( "Calculated ratio: " + numerator / (double)denominator );
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the sizes of the first and second collections are equal
	 * @param <T> Value type
	 * @param first The first collection
	 * @param second The second collection
	 */
	public static void equalSize( final Collection< ? >... collections )
	{
		if( collections != null && collections.length > 0 )
		{
			Require.notNull( collections[ 0 ] );
		}
		
		for( int i = 1; i < collections.length; ++i )
		{
			Require.notNull( collections[ i ] );
			
			if( collections[ 0 ].size() != collections[ i ].size() )
			{
				StringBuffer message = new StringBuffer();
				message.append( "The collections must all have the same size." ).append( Constants.NEW_LINE );
				message.append( "Specified sizes:" ).append( Constants.NEW_LINE );
				for( int j = 0; j < collections.length; ++j )
				{
					message.append( "  Size of argument " ).append( j ).append( " = " );
					message.append( collections[ j ].size() ).append( Constants.NEW_LINE );;
				}
				
				throw new IllegalArgumentException( message.toString() );
			}
		}
	}
	
	/**
	 * Asserts that the types are the same.
	 * @param test The object to test
	 * @param type The desired type
	 * @throws IllegalArgumentException if the objects don't have the same type
	 */
	public static void typesEqual( Object test, Object type )
	{
		if( test.getClass() != type.getClass() )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Objects are not of the same type.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			message.append( "  type: " + type.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the specified object is a Double
	 * @param test The test object
	 * @throws IllegalArgumentException if the test object is not a Double
	 */
	public static void typeDouble( Object test )
	{
		if( !( test instanceof Double ) )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not is not of type Double.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the specified object is a Integer
	 * @param test The test object
	 * @throws IllegalArgumentException if the test object is not a Integer
	 */
	public static void typeInteger( Object test )
	{
		if( !( test instanceof Integer ) )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not is not of type Integer.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the specified object is a Boolean
	 * @param test The test object
	 * @throws IllegalArgumentException if the test object is not a Boolean
	 */
	public static void typeBoolean( Object test )
	{
		if( !( test instanceof Boolean ) )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not is not of type Boolean.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the specified object is a String
	 * @param test The test object
	 * @throws IllegalArgumentException if the test object is not a String
	 */
	public static void typeString( Object test )
	{
		if( !( test instanceof String ) )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not is not of type String.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the specified object is a Calendar
	 * @param test The test object
	 * @throws IllegalArgumentException if the test object is not a Calendar
	 */
	public static void typeCalendar( Object test )
	{
		if( !( test instanceof Calendar ) )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not is not of type Calendar.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the specified object is a Calendar or Date
	 * @param test The test object
	 * @throws IllegalArgumentException if the test object is not a Calendar
	 */
	public static void typeCalendarOrDate( Object test )
	{
		if( !( test instanceof Calendar ) && !( test instanceof Date ) )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not is not of type Calendar.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Asserts that the specified object is a List< ? >
	 * @param test The test object
	 * @throws IllegalArgumentException if the test object is not a List< ? >
	 */
	public static void typeList( Object test )
	{
		if( !( test instanceof List< ? > ) )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object not is not of type List< T >.\n" );
			message.append( "  test: " + test.getClass() + "\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
	
	/**
	 * Tests whether the specified object is null.
	 * @param <T> The object type
	 * @param object The object to test
	 * @throws IllegalArgumentException if the object is null
	 */
	public static < T > void notNull( T object )
	{
		if( object == null )
		{
			StringBuffer message = new StringBuffer();
			message.append( "Object cannot be null\n" );
			
			throw new IllegalArgumentException( message.toString() );
		}
	}
}
