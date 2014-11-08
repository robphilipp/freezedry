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

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.annotations.PersistDateAs;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.utils.ReflectionUtils;


/**
 * The {@link NodeBuilder} for building {@link InfoNode} from {@link Calendar} objects and for
 * building {@link Calendar} objects from {@link InfoNode}s.
 *  
 * @author Robert Philipp
 */
public class CalendarNodeBuilder extends AbstractLeafNodeBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger( CalendarNodeBuilder.class );
	
	public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	private String outputDateFormat = ISO_8601_DATE_FORMAT;
	private List< String > parsingDateFormats = createDefaultParsingDateFormats();
	
	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Calendar}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public CalendarNodeBuilder( final PersistenceEngine engine, final String dateFormat )
	{
		super( engine );
		this.outputDateFormat = dateFormat;
	}
	
	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Calendar}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public CalendarNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public CalendarNodeBuilder()
	{
		super();
	}
	
	/**
	 * Copy constructor
	 * @param builder The {@link CalendarNodeBuilder} to copy
	 */
	public CalendarNodeBuilder( final CalendarNodeBuilder builder )
	{
		super( builder );
		this.outputDateFormat = builder.outputDateFormat;
	}
	
	/**
	 * @return a default set of date formats for parsing {@link String}s into {@link Calendar}
	 */
	private static List< String > createDefaultParsingDateFormats()
	{
		final List< String > formats = new ArrayList<>();
		formats.add( "MMM dd, yyyy" );
		formats.add( "MMM dd, yy" );
		formats.add( "dd MMM yyyy" );
		formats.add( "dd MMM yy" );

		formats.add( "dd-MMM-yy" );
		formats.add( "dd-MMM-yyyy" );
		formats.add( "dd.MMM.yy" );
		formats.add( "dd.MMM.yyyy" );
		
		formats.add( "yyyy.MM.dd" );
		formats.add( "yyyy-MM-dd" );
		formats.add( "yyyyMMdd" );

		formats.add( "MM/dd/yyyy" );
		formats.add( "dd/MMM/yyyy" );
		
		// ISO 8601 (strict)
		// 1. Year: {@code YYYY} (eg 1997)
		// 2. Year and month: {@code YYYY-MM} (eg 1997-07)
		// 3. Complete date: {@code YYYY-MM-DD} (eg 1997-07-16)
		// 4. Complete date plus hours and minutes: YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
		// 5. Complete date plus hours, minutes and seconds: YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
		// 6. Complete date plus hours, minutes, seconds and a decimal fraction of a second: YYYY-MM-DDThh:mm:ss.sTZD 
		//    (eg 1997-07-16T19:20:30.45+01:00)
		formats.add( "yyyy" );
		formats.add( "yyyy-MM" );
		formats.add( "yyyy-MM-dd" );
		formats.add( "yyyy-MM-dd'T'HH:mmZ" );
		formats.add( "yyyy-MM-dd'T'HH:mm:ssZ" );
		formats.add( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
		return formats;
	}
	
	/**
	 * Sets the list of date formats to use when parsing a {@link String} into a {@link Calendar} object
	 * @param formats the list of date formats to use when parsing a {@link String} into a {@link Calendar} object
	 */
	public void setDateFormats( final List< String > formats )
	{
		this.parsingDateFormats = formats;
	}
	
	/**
	 * Adds a date format to use when parsing a {@link String} into a {@link Calendar} object
	 * @param format a date format to use when parsing a {@link String} into a {@link Calendar} object
	 */
	public void addDateFormat( final String format )
	{
		if( !parsingDateFormats.contains( format ) )
		{
			parsingDateFormats.add( format );
		}
	}
	
	/**
	 * Removes a date format from the list of formats to use when parsing a {@link String} into a {@link Calendar} object
	 * @param format a date format to remove from the list of formats
	 * @return true if the format was removed; false otherwise
	 */
	public boolean removeDateFormat( final String format )
	{
		return parsingDateFormats.remove( format );
	}

	/**
	 * @return the list of date formats used to parse a {@link String} into a {@link Calendar} object
	 */
	public List< String > getDateFormats()
	{
		return new ArrayList<>( parsingDateFormats );
	}
	
	/**
	 * Sets the format used to convert the {@link Calendar} (date) into a {@link String}
	 * @param format the format used to convert the {@link Calendar} (date) into a {@link String}
	 */
	public void setOutputDateFormat( final String format )
	{
		outputDateFormat = format;
	}
	
	/**
	 * @return the format used to convert the {@link Calendar} (date) into a {@link String}
	 */
	public String getOutputDateFormat()
	{
		return outputDateFormat;
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. The specified containing {@link Class}
	 * is the {@link Class} in which the specified field name lives. And the object is the value of
	 * the field name.
	 * @param containingClass The {@link Class} that contains the specified field name
	 * @param object The value of the field with the specified field name
	 * @param fieldName The name of the field for which the object is the value
	 * @return The constructed {@link InfoNode} based on the specified information
	 */
	@Override
	public InfoNode createInfoNode( final Class< ? > containingClass, final Object object, final String fieldName )
	{
		// grab the class for the object to persist
		final Class< ? > clazz = object.getClass();
		
		// when the containing class is null, then class is the root node of the semantic model, and therefore
		// there won't be a field name to with a annotation containing the persist name.
		String persistName = null;
		if( containingClass != null )
		{
			// grab the persistence name if the annotation @Persist( persistName = "xxxx" ) is specified,
			// and if the leaf is part of another class (such as a collection) it will return the field name
			persistName = ReflectionUtils.getPersistenceName( containingClass, fieldName );
		}
		if( persistName == null || persistName.isEmpty() )
		{
			persistName = fieldName;
		}

		// grab any date formatting information that may be present in and annotation
		String dateFormat = outputDateFormat;
		try
		{
			// if the field isn't found or no annotation is present, then we stay
			// with the default date format
			final Field field = ReflectionUtils.getDeclaredField( containingClass, fieldName );
			final PersistDateAs annotation = field.getAnnotation( PersistDateAs.class );
			if( annotation != null )
			{
				dateFormat = annotation.value();
			}
		}
		catch( NoSuchFieldException e ) { /* empty on purpose */ }
		
		// we must convert the object to the appropriate format
		final String date = DateUtils.createStringFromDate( (Calendar)object, dateFormat );
		
		// create a new leaf node with the new date string

		// return the node
		return InfoNode.createLeafNode( fieldName, date, persistName, clazz );
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. This method is used for objects that have
	 * an overriding node builder and are not contained within a class. For example, suppose you would like
	 * to persist an {@link java.util.ArrayList} for serialization and would like to maintain the type information.
	 * @param object The value of the field with the specified field name
	 * @return The constructed {@link InfoNode} based on the specified information
	 */
	@Override
	public InfoNode createInfoNode( final Object object, final String persistName )
	{
		// grab the class for the object to persist
		final Class< ? > clazz = object.getClass();
		
		// we must convert the object to the appropriate format
		final String date = DateUtils.createStringFromDate( (Calendar)object, ISO_8601_DATE_FORMAT );
		final InfoNode stringNode = InfoNode.createLeafNode( "value", date, "value", String.class );

		// create the root node and add the string rep of the date
		final InfoNode node = InfoNode.createRootNode( persistName, clazz );
		node.addChild( stringNode );
		
		// return the node
		return node;
	}

	/**
	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}. Note that
	 * the {@link org.freezedry.persistence.tree.InfoNode} may also contain type information about the class to generate. The specified {@link Class}
	 * overrides that value. This is done to avoid modifying the {@link org.freezedry.persistence.tree.InfoNode} tree when supplemental information becomes
	 * available.
	 * @param containingClass The {@link Class} containing the clazz, represented by the {@link InfoNode}
	 * @param clazz The {@link Class} of the object to create
	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
	 */
	@Override
	public Calendar createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node )
	{
		final String value = (String)node.getValue();
		
		// try the ISO 8601 format (strict)
		Calendar date = DateUtils.createDateFromString( value, parsingDateFormats );
		if( date != null )
		{
			LOGGER.info( ("Converted from list of allowed formats to date." + Constants.NEW_LINE) + "  Date String: " + value );
		}
		else
		{
			date = DateUtils.createDateFromIso8601( value );
			if( date != null )
			{
				LOGGER.info( ("Converted from ISO 8601 format to date." + Constants.NEW_LINE) + "  Date String: " + value );
			}
			else
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Could not convert string date to a date object." ).append( Constants.NEW_LINE );
				message.append( "  Date String: " ).append( value );
				message.append( "  Attempted the following formats:" ).append( Constants.NEW_LINE );
				for( String format : Constants.DATE_FORMATS )
				{
					message.append( "    " ).append( format ).append( Constants.NEW_LINE );
				}
				LOGGER.info( message.toString() );
			}
		}
		
		return date;
	}

	/**
	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}.
	 * This method is used for objects that have an overriding node builder and are not contained within a
	 * class. For example, suppose you would like to persist an {@link java.util.ArrayList} for serialization and would
	 * like to maintain the type information.
	 * @param clazz The {@link Class} of the object to create
	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
	 */
	@Override
	public Calendar createObject( final Class< ? > clazz, final InfoNode node )
	{
		final InfoNode valueNode = node.getChild( 0 );
		final String value = (String)valueNode.getValue();
		// try the ISO 8601 format (strict)
		Calendar date;
		try
		{
			date = DateUtils.createDateFromString( value, ISO_8601_DATE_FORMAT );
		}
		catch( ParseException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Could not convert string to ISO 8601 date" ).append( Constants.NEW_LINE );
			message.append( "  Date String: " ).append( value );
			LOGGER.info( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return date;
	}

	/**
	 * Creates and returns a copy of the object <code>x</code> that meets the following criteria
	 * <ol>
	 * 	<li>The expressions <code>x.getCopy() != x</code> evaluates as <code>true</code></li>
	 * 	<li>The expressions <code>x.getCopy().equals( x )</code> evaluates as <code>true</code></li>
	 * 	<li>The expressions <code>x.getCopy().getClass() == x.getClass()</code> evaluates as <code>true</code></li>
	 * </ol>
	 * @return a copy of the object that meets the above criteria
	 */
	@Override
	public CalendarNodeBuilder getCopy()
	{
		return new CalendarNodeBuilder( this );
	}
}
