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

import org.apache.log4j.Logger;
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
public class DateNodeBuilder extends AbstractLeafNodeBuilder {

	private static final Logger LOGGER = Logger.getLogger( DateNodeBuilder.class );
	
	public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	private String outputDateFormat = ISO_8601_DATE_FORMAT;
	private List< String > parsingDateFormats = createDefaultParsingDateFormats();
	
	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Calendar}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public DateNodeBuilder( final PersistenceEngine engine, final String dateFormat )
	{
		super( engine );
		this.outputDateFormat = dateFormat;
	}
	
	/**
	 * Constructs the {@link NodeBuilder} for going between {@link Calendar}s and 
	 * back to {@link Object}s.
	 * @param engine The {@link PersistenceEngine}
	 */
	public DateNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}
	
	/**
	 * Default no-arg constructor
	 */
	public DateNodeBuilder()
	{
		super();
	}
	
	/**
	 * Copy constructor
	 * @param builder The {@link DateNodeBuilder} to copy
	 */
	public DateNodeBuilder( final DateNodeBuilder builder )
	{
		super( builder );
		this.outputDateFormat = builder.outputDateFormat;
	}
	
	/*
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

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.LeafNodeBuilder#createInfoNode(java.lang.Class, java.lang.Object, java.lang.String)
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
			field.getAnnotations();
			if( annotation != null )
			{
				dateFormat = annotation.value();
			}
		}
		catch( NoSuchFieldException e ) {}
		
		// we must convert the object to the appropriate format
		final String date = DateUtils.createStringFromDate( (Calendar)object, dateFormat );
		
		// create a new leaf node with the new date string
		final InfoNode node = InfoNode.createLeafNode( fieldName, date, persistName, clazz );
		
		// return the node
		return node;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.NodeBuilder#createInfoNode(java.lang.Object)
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

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.infonodes.NodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.nodes.InfoNode)
	 */
	@Override
	public Calendar createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node )
	{
		final String value = (String)node.getValue();
		
		// try the ISO 8601 format (strict)
		Calendar date = DateUtils.createDateFromString( value, parsingDateFormats );
		if( date != null )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Converted from list of allowed formats to date." + Constants.NEW_LINE );
			message.append( "  Date String: " + value );
			LOGGER.info( message.toString() );
		}
		else
		{
			date = DateUtils.createDateFromIso8601( value );
			if( date != null )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Converted from ISO 8601 format to date." + Constants.NEW_LINE );
				message.append( "  Date String: " + value );
				LOGGER.info( message.toString() );
			}
			else
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Could not convert string date to a date object." + Constants.NEW_LINE );
				message.append( "  Date String: " + value );
				message.append( "  Attempted the following formats:" + Constants.NEW_LINE );
				for( String format : Constants.DATE_FORMATS )
				{
					message.append( "    " + format + Constants.NEW_LINE );
				}
				LOGGER.info( message.toString() );
			}
		}
		
		return date;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.builders.AbstractLeafNodeBuilder#createObject(java.lang.Class, org.freezedry.persistence.tree.InfoNode)
	 */
	@Override
	public Calendar createObject( final Class< ? > clazz, final InfoNode node )
	{
		final InfoNode valueNode = node.getChild( 0 );
		final String value = (String)valueNode.getValue();
		// try the ISO 8601 format (strict)
		Calendar date = null;
		try
		{
			date = DateUtils.createDateFromString( value, ISO_8601_DATE_FORMAT );
		}
		catch( ParseException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Could not convert string to ISO 8601 date" + Constants.NEW_LINE );
			message.append( "  Date String: " + value );
			LOGGER.info( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return date;
	}

	/*
	 * (non-Javadoc)
	 * @see com.synapse.copyable.Copyable#getCopy()
	 */
	@Override
	public DateNodeBuilder getCopy()
	{
		return new DateNodeBuilder( this );
	}
}
