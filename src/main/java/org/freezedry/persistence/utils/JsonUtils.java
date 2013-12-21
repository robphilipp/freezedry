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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger( JsonUtils.class );

	public static JsonType getType( final JSONObject object, final String key )
	{
		try
		{
			object.getBoolean( key );
		}
		catch( JSONException e )
		{
			
		}
		return JsonType.BOOLEAN;
	}
	
	/**
	 * Returns the {@link JSONObject} of the specified name from the specified {@link JSONObject}.
	 * @param jsonObject The {@link JSONObject} from which to return the {@link JSONObject} with the specified name.
	 * @param name The name of the {@link JSONObject} to return
	 * @return The {@link JSONObject} with the specified name.
	 */
	public static Object getValue( final JSONObject jsonObject, final String name )
	{
		Object object = null; 
		try
		{
			object = jsonObject.get( name );
		}
		catch( JSONException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to get the JSON object for specified key name." + Constants.NEW_LINE );
			message.append( "  Key Name: " + name + Constants.NEW_LINE );
			try
			{
				message.append( "  JSON Object: " + jsonObject.toString( 4 ) + Constants.NEW_LINE );
			}
			catch( JSONException e1 ) {}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		return object;
	}
	
	/**
	 * Returns the element of the specified index from the {@link JSONArray}. If the index
	 * is out of bounds, or if there is an issue retrieving the element, throws a {@link JSONException}.
	 * @param jsonArray The {@link JSONArray} from which to retrieve the element
	 * @param index The index into the {@link JSONArray}
	 * @return the element of the specified index from the {@link JSONArray}.
	 */
	public static Object getElement( final JSONArray jsonArray, final int index )
	{
		Object object = null;
		try
		{
			object = jsonArray.get( index );
		}
		catch( JSONException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to get the JSON element for specified index." + Constants.NEW_LINE );
			message.append( "  Index: " + index + Constants.NEW_LINE );
			message.append( "  Length: " + jsonArray.length() + Constants.NEW_LINE );
			try
			{
				message.append( "  JSON Array: " + jsonArray.toString( 4 ) + Constants.NEW_LINE );
			}
			catch( JSONException e1 ) {}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		return object;
	}
	
	public enum JsonType {
		BOOLEAN,
		DOUBLE,
		INT,
		LONG,
		STRING,
		JSON_ARRAY,
		OBJECT;
	}
}
