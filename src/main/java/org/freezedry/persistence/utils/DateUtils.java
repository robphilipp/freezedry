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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Robert Philipp
 */
public class DateUtils {
	
	private static final Logger LOGGER = Logger.getLogger( DateUtils.class );

	/**
	 * Returns a Calendar object based on the String representation of the date
	 * @param date The string representation of the date
	 * @param format The date format should be something like yyyy.MM.dd or yyyy-MM-dd.
	 * @return a Calendar object based on the String representation of the date
	 * @throws ParseException
	 */
	public static Calendar createDateFromString( final String date, final String format ) throws ParseException
	{
		final DateFormat formatter = new SimpleDateFormat( format );
		final Calendar cal = new GregorianCalendar();
		cal.setTime( formatter.parse( date ) );
		return cal;
	}

	/**
	 * Takes in a string representation of a date, searches the available formats,
	 * and if it finds one, creates a {@link Calendar} object representing that date.
	 * The available formats are stored in the {@link Constants#DATE_FORMAT} 
	 * @param date The string representation of the date
	 * @return The {@link Calendar} representation of the date; or null if the format is 
	 * not a valid format.
	 */
	public static Calendar createDateFromString( final String date, final List< String > formats )
	{
		Calendar calendarDate = null;
		for( String format : formats )
		{
			try 
			{
				calendarDate = DateUtils.createDateFromString( date, format );
	            break;
			} 
			catch( ParseException exception ) 
			{
				LOGGER.trace( "Attempted date format: " + format, exception );
			}
		}
		return calendarDate;
	}
	
	/**
	 * Returns a string representation of the date using the specified format (i.e. yyyy.MM.dd)
	 * @param date The date to convert
	 * @param format The specified format
	 * @return a string representation of the date using the specified format
	 */
	public static String createStringFromDate( final Calendar date, final String format )
	{
		DateFormat formatter = new SimpleDateFormat( format );
		return formatter.format( date.getTime() );
	}

	/**
	 * Returns a string representations of the dates using the specified format (i.e. yyyy.MM.dd)
	 * @param dates The list of dates to convert
	 * @param format The specified format
	 * @return a string representations of the dates using the specified format
	 */
	public static List< String > createStringsFromDates( final List< Calendar > dates, final String format )
	{
		final List< String > strings = new ArrayList< String >( dates.size() );

		for( Calendar date : dates )
		{
			strings.add( createStringFromDate( date, format ) );
		}

		return strings;
	}

	/**
	 * Converts an ISO 8601 {@link String} into a {@link Calendar}.
	 * <p>Different standards may need different levels of granularity in the date and time, so this profile 
	 * defines six levels. Standards that reference this profile should specify one or more of these levels of
	 * granularity. If a given standard allows more than one granularity, it should specify the meaning of 
	 * the dates and times with reduced precision, for example, the result of comparing two dates with different 
	 * precision.</p>
	 * 
	 * <p>The formats are as follows. Exactly the components shown here must be present, with exactly this 
	 * punctuation. Note that the "T" appears literally in the string, to indicate the beginning of the time 
	 * element, as specified in ISO 8601.</p>
	 * 
	 * <ul>
	 * 	<li>Year: {@code YYYY} (eg 1997)</li>
	 *	<li>Year and month: {@code YYYY-MM} (eg 1997-07)</li>
	 *	<li>Complete date: {@code YYYY-MM-DD} (eg 1997-07-16)</li>
	 * 	<li>Complete date plus hours and minutes: {@code YYYY-MM-DDThh:mmTZD} (eg 1997-07-16T19:20+01:00)</li>
	 * 	<li>Complete date plus hours, minutes and seconds: {@code YYYY-MM-DDThh:mm:ssTZD} (eg 1997-07-16T19:20:30+01:00)</li>
	 * 	<li>Complete date plus hours, minutes, seconds and a decimal fraction of a second: {@code YYYY-MM-DDThh:mm:ss.sTZD} 
	 *      (eg 1997-07-16T19:20:30.45+01:00)</li>
	 * </ul>
	 * where:
	 * <ul> 
	 * 	<li>{@code YYYY} = four-digit year</li>
	 * 	<li>{@code MM}   = two-digit month (01=January, etc.)</li>
	 * 	<li>{@code DD}   = two-digit day of month (01 through 31)</li>
	 * 	<li>{@code hh}   = two digits of hour (00 through 23) (am/pm NOT allowed)</li>
	 * 	<li>{@code mm}   = two digits of minute (00 through 59)</li>
	 * 	<li>{@code ss}   = two digits of second (00 through 59)</li>
	 * 	<li>{@code s}    = one or more digits representing a decimal fraction of a second</li>
	 * 	<li>{@code TZD}  = time zone designator (Z or +hh:mm or -hh:mm)</li>
	 * </ul>
	 * <p>This profile does not specify how many digits may be used to represent the decimal fraction of a second. 
	 * An adopting standard that permits fractions of a second must specify both the minimum number of digits (a
	 * number greater than or equal to one) and the maximum number of digits (the maximum may be stated to be "unlimited").</p>
	 * 
	 * <p>This profile defines two ways of handling time zone offsets:</p>
	 * 
	 * <ol>
	 * 	<li>Times are expressed in UTC (Coordinated Universal Time), with a special UTC designator ("Z").</li>
	 * 	<li>Times are expressed in local time, together with a time zone offset in hours and minutes. A time zone 
	 * 		offset of "+hh:mm" indicates that the date/time uses a local time zone which is "hh" hours and "mm" minutes
	 * 		ahead of UTC. A time zone offset of "-hh:mm" indicates that the date/time uses a local time zone which 
	 * 		is "hh" hours and "mm" minutes behind UTC.</li>
	 * </ol>
	 * <p>A standard referencing this profile should permit one or both of these ways of handling time zone offsets.</p>
	 * 
	 * @param dateString
	 * @return The {@link Calendar} object of the string representation
	 */
	public static Calendar createDateFromIso8601( final String dateString )
	{
		// construct the various elements of the ISO 8601 date format
		final String YYYY_MM = "([0-9]{4})-(1[0-2]|0[1-9])";
		final String DD = "-(3[0-1]|0[1-9]|[1-2][0-9])";
		final String hh_mm = "(2[0-3]|[01]?[0-9]):([0-5]?[0-9])";
		final String ss = ":([0-5][0-9])";
		final String s = "\\.[0-9]+";
		
		final String zulu = "Z";
		final String offset = "[+-]" + hh_mm;
		final String timeZone = "(" + zulu + "|(" + offset + "))"; 
		
		// create the regular expression that checks for the value format
		final String regex = 
			YYYY_MM + 
				"(" + 
				DD + 
					"(T" + 
						hh_mm + "(" + ss + "(" + s + ")?" + ")?" + timeZone + 
					")?" + 
				")?";
		
		// check the date string against the format to ensure it is valid, and if it is
		// valid, then search to see which pattern it matches so that we can decompose the
		// elements and create the calendar
		final Pattern pattern = Pattern.compile( "^" + regex + "$" );
		final Matcher matcher = pattern.matcher( dateString );
		final boolean isMatch = matcher.matches();
		Calendar date = null;
		if( isMatch )
		{
			// test YYYY-MM
			if( Pattern.matches( "^" + YYYY_MM + "$", dateString ) )
			{
				final String[] elements = dateString.split( "-" );
				date = new GregorianCalendar( Integer.parseInt( elements[ 0 ] ), 
						  					  Integer.parseInt( elements[ 1 ] ) - 1,
						  					  1 );
			}
			else
			// test YYYY-MM-DD
			if( Pattern.matches( "^" + YYYY_MM + DD + "$", dateString ) )
			{
				final String[] elements = dateString.split( "-" );
				date = new GregorianCalendar( Integer.parseInt( elements[ 0 ] ), 
						  					  Integer.parseInt( elements[ 1 ] ) - 1,
						  					  Integer.parseInt( elements[ 2 ] ) );
			}
			else
			// ZULU times
			// test YYYY-MM-DDThh:mmZ
			if( Pattern.matches( "^" + YYYY_MM + DD + "T" + hh_mm + "Z$", dateString ) )
			{
				final String[] elements = dateString.split( "T" );
				final String[] dates = elements[ 0 ].split( "-" );
				final String[] times = elements[ 1 ].substring( 0, elements[ 1 ].length()-1 ).split( ":" );
				date = new GregorianCalendar( Integer.parseInt( dates[ 0 ] ), 
						  					  Integer.parseInt( dates[ 1 ] ) - 1,
						  					  Integer.parseInt( dates[ 2 ] ),
						  					  Integer.parseInt( times[ 0 ] ),
						  					  Integer.parseInt( times[ 1 ] ) );
				date.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
			}
			else
			// test YYYY-MM-DDThh:mm:ssZ
			if( Pattern.matches( "^" + YYYY_MM + DD + "T" + hh_mm + ss + "Z$", dateString ) )
			{
				final String[] elements = dateString.split( "T" );
				final String[] dates = elements[ 0 ].split( "-" );
				final String[] times = elements[ 1 ].substring( 0, elements[ 1 ].length()-1 ).split( ":" );
				date = new GregorianCalendar( Integer.parseInt( dates[ 0 ] ), 
						  					  Integer.parseInt( dates[ 1 ] ) - 1,
						  					  Integer.parseInt( dates[ 2 ] ),
						  					  Integer.parseInt( times[ 0 ] ),
						  					  Integer.parseInt( times[ 1 ] ),
						  					  Integer.parseInt( times[ 2 ] ) );
				date.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
			}
			else
			// test YYYY-MM-DDThh:mm:ss.xxxZ (but we throw away the milliseconds
			if( Pattern.matches( "^" + YYYY_MM + DD + "T" + hh_mm + ss + s + "Z$", dateString ) )
			{
				final String[] elements = dateString.split( "T" );
				final String[] dates = elements[ 0 ].split( "-" );
				final String[] times = elements[ 1 ].substring( 0, elements[ 1 ].length()-1 ).split( ":" );
				final String[] seconds = times[ 2 ].split( "\\." );
				date = new GregorianCalendar( Integer.parseInt( dates[ 0 ] ), 
						  					  Integer.parseInt( dates[ 1 ] ) - 1,
						  					  Integer.parseInt( dates[ 2 ] ),
						  					  Integer.parseInt( times[ 0 ] ),
						  					  Integer.parseInt( times[ 1 ] ),
						  					  Integer.parseInt( seconds[ 0 ] ) );
				date.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
				final long ms = (long)(1000 * (Integer.parseInt( seconds[ 1 ] )) / Math.pow( 10, seconds[ 1 ].length() ));
				final long millis = date.getTimeInMillis() + ms;
				date.setTimeInMillis( millis );
			}
			// local offset times
			// test YYYY-MM-DDThh:mm[+-]hh:mm
			if( Pattern.matches( "^" + YYYY_MM + DD + "T" + hh_mm + offset + "$", dateString ) )
			{
				final String[] elements = dateString.split( "T" );
				final String[] dates = elements[ 0 ].split( "-" );
				String[] timeElements = null;
				String offsetSign;
				if( elements[ 1 ].contains( "+" ) )
				{
					timeElements = elements[ 1 ].split( "\\+" );
					offsetSign = "+";
				}
				else
				{
					timeElements = elements[ 1 ].split( "\\-" );
					offsetSign = "-";
				}
				final String[] times = timeElements[ 0 ].split( ":" );
				date = new GregorianCalendar( Integer.parseInt( dates[ 0 ] ), 
											  Integer.parseInt( dates[ 1 ] ) - 1,
											  Integer.parseInt( dates[ 2 ] ),
											  Integer.parseInt( times[ 0 ] ),
											  Integer.parseInt( times[ 1 ] ) );
				date.setTimeZone( TimeZone.getTimeZone( "GMT" + offsetSign + timeElements[ 1 ] ) );
			}
			else
			// test YYYY-MM-DDThh:mm:ss[+-]hh:mm
			if( Pattern.matches( "^" + YYYY_MM + DD + "T" + hh_mm + ss + offset + "$", dateString ) )
			{
				final String[] elements = dateString.split( "T" );
				final String[] dates = elements[ 0 ].split( "-" );
				String[] timeElements = null;
				String offsetSign;
				if( elements[ 1 ].contains( "+" ) )
				{
					timeElements = elements[ 1 ].split( "\\+" );
					offsetSign = "+";
				}
				else
				{
					timeElements = elements[ 1 ].split( "\\-" );
					offsetSign = "-";
				}
				final String[] times = timeElements[ 0 ].split( ":" );
				date = new GregorianCalendar( Integer.parseInt( dates[ 0 ] ), 
											  Integer.parseInt( dates[ 1 ] ) - 1,
											  Integer.parseInt( dates[ 2 ] ),
											  Integer.parseInt( times[ 0 ] ),
											  Integer.parseInt( times[ 1 ] ),
											  Integer.parseInt( times[ 2 ] ));
				date.setTimeZone( TimeZone.getTimeZone( "GMT" + offsetSign + timeElements[ 1 ] ) );
			}
			else
			// test YYYY-MM-DDThh:mm:ss.xxx[+-]hh:mm
			if( Pattern.matches( "^" + YYYY_MM + DD + "T" + hh_mm + ss + s + offset + "$", dateString ) )
			{
				final String[] elements = dateString.split( "T" );
				final String[] dates = elements[ 0 ].split( "-" );
				String[] timeElements = null;
				String offsetSign;
				if( elements[ 1 ].contains( "+" ) )
				{
					timeElements = elements[ 1 ].split( "\\+" );
					offsetSign = "+";
				}
				else
				{
					timeElements = elements[ 1 ].split( "\\-" );
					offsetSign = "-";
				}
				final String[] times = timeElements[ 0 ].split( ":" );
				final String[] seconds = times[ 2 ].split( "\\." );
				date = new GregorianCalendar( Integer.parseInt( dates[ 0 ] ), 
											  Integer.parseInt( dates[ 1 ] ) - 1,
											  Integer.parseInt( dates[ 2 ] ),
											  Integer.parseInt( times[ 0 ] ),
											  Integer.parseInt( times[ 1 ] ),
											  Integer.parseInt( seconds[ 0 ] ));
				date.setTimeZone( TimeZone.getTimeZone( "GMT" + offsetSign + timeElements[ 1 ] ) );
				final long ms = (long)(1000 * (Integer.parseInt( seconds[ 1 ] )) / Math.pow( 10, seconds[ 1 ].length() ));
				final long millis = date.getTimeInMillis() + ms;
				date.setTimeInMillis( millis );
			}
			else // not valid format...shouldn't have gotten here
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Invalid ISO 8601 date format. But you should have never reached this point." + Constants.NEW_LINE );
				message.append( "The fact that you got to this point is highly disturbing. So disurbing, " + Constants.NEW_LINE );
				message.append( "in fact, that I'm just going to stop, and let the returned date remain null." );
				LOGGER.warn( message.toString() );
			}
		}
		return date;
	}
}
