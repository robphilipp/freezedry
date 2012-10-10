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
package org.freezedry.persistence.readers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.JsonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reads the JSON string from the specifed {@link InputStream} and converts it to the semantic model.
 * JSON has the following format (taken from <a href="http://json.org/">json.org</a>):<p>
 * <pre>
 * object
 *    {}
 *    { members }
 *     
 * members
 *    pair
 *    pair , members
 *    
 * pair
 *    string : value
 *    
 * array
 *    []
 *    [ elements ]
 *    
 * elements
 *    value
 *    value , elements
 *    
 * value
 *    string
 *    number
 *    object
 *    array
 *    true
 *    false
 *    null
 * </pre> 
 * @see PersistenceReader
 * @see XmlReader
 * 
 * @author rob
 */
public class JsonReader implements PersistenceReader {

	private static final Logger LOGGER = Logger.getLogger( JsonReader.class );

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.readers.PersistenceReader#read(java.lang.Class, java.io.InputStream)
	 */
	@Override
	public InfoNode read( final Class< ? > clazz, final Reader input )
	{
		// load the json string from the input stream into a json object
		final JSONObject jsonObject = createRootJsonObject( clazz, input );
			
		// the first/top of the json string must be the class name, so there should only be one key,
		// and one value associated with that key { "root_key" : { members } }, where members is
		// defined by { pair, members } and pair is defined by "key", "value" (see class documentation).
		// we still have to add the value as a node.
		final InfoNode rootNode = createRootNode( jsonObject, clazz );
		
		// grab the value and deal with one of the three possibilities:
		// 1. the value is a json object and therefore has name-value pairs
		// 2. the value is a json array and therefore has elements
		// 3. the value is neither, and therefore is a simple value
		// In the first two cases, we build out the remainder of the nodes recursively. In
		// the third case, we set in the value and class in root node and we're done.
		final Object value = JsonUtils.getValue( jsonObject, rootNode.getPersistName() );
		if( value instanceof JSONObject )
		{
			buildInfoNode( (JSONObject)value, rootNode );
		}
		else if( value instanceof JSONArray )
		{
			buildInfoNode( (JSONArray)value, clazz.getSimpleName(), rootNode );
		}
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Invalid JSON representation of a class. Root node must have at least one named element." + Constants.NEW_LINE );
			message.append( "  JSON String: " + Constants.NEW_LINE );
			try{ message.append( jsonObject.toString( 2 ) ); } catch( JSONException e ) {}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		return rootNode;
	}
	
	/*
	 * Creates the {@link InfoNode} for each of the key-value pairs found in {@link JSONObject} and
	 * adds them to the specified parent {@link InfoNode}, recursively.
	 * @param jsonObject The {@link JSONObject} from which to create the new {@link InfoNode}s and add
	 * them to the specified parent {@link InfoNode}.
	 * @param infoNode The parent {@link InfoNode} to which to add the new {@link InfoNode}s.
	 * @see #buildInfoNode(JSONArray, String, InfoNode)
	 */
	private static void buildInfoNode( final JSONObject jsonObject, final InfoNode infoNode )
	{
		// grab a list of the element names held in the json object, and then grab the
		// json object for each name
		final List< String > names = getJsonNames( jsonObject );
		for( String name : names )
		{
			final Object object = JsonUtils.getValue( jsonObject, name );
			
			// build the info node and add it to its parent
			final InfoNode newInfoNode = createInfoNode( name, object, infoNode );
			if( newInfoNode != null )
			{
				infoNode.addChild( newInfoNode );
			}
		}
	}

	/*
	 * Creates the {@link InfoNode} for each element in the {@link JSONArray} and adds it to the specified
	 * parent {@link InfoNode} recursively.
	 * @param jsonArray The {@link JSONArray} containing the elements from which to create {@link InfoNode}s and
	 * add them to the parent {@link InfoNode}.
	 * @param nodeName The name to assign to the new nodes. This is the name of the list in the JSON document.
	 * @param infoNode The parent {@link InfoNode} to which to add the new {@link InfoNode}s.
	 * @see #buildInfoNode(JSONObject, InfoNode)
	 */
	private static void buildInfoNode( final JSONArray jsonArray, final String nodeName, final InfoNode infoNode )
	{
		// run through the elements of the json array (recursively) building info nodes
		for( int i = 0; i < jsonArray.length(); ++i )
		{
			final Object object = JsonUtils.getElement( jsonArray, i );
			
			// build the info node and add it to its parent info node
			final InfoNode newInfoNode = createInfoNode( nodeName, object, infoNode );
			infoNode.addChild( newInfoNode );
		}
	}

	/*
	 * Part of the recursive algorithm that creates {@link InfoNode}s. If the specified {@code value} represents a leaf
	 * node, then we create it and return. However, if the {@code value} represents a compound node, such as a {@link JSONObject}
	 * or a {@link JSONArray}, then we recurse, calling the appropriate {@code buildInfoNode(...)} method. These methods will
	 * in turn call back to this method. It all ends when the specified {@code value} is a leaf node.
	 * @param nodeName The persistence name to assign to the new {@link InfoNode}
	 * @param value The {@code value} of the node which could be a {@link JSONObject}, a {@link JSONArray}, or a plain object
	 * which represents a leaf node.
	 * @param parentNode The parent {@link InfoNode} to which the new node would be added. This is only used when the
	 * {@code value} is a {@link JSONArray}, in which case the elements are added to this parent node and no new node
	 * is created.
	 * @return The newly created {@link InfoNode}
	 * @see #buildInfoNode(JSONObject, InfoNode)
	 * @see #buildInfoNode(JSONArray, String, InfoNode)
	 */
	private static InfoNode createInfoNode( final String nodeName, final Object value, final InfoNode parentNode )
	{
		// there are three possibilities:
		// 1. the value is a json object, in which case the info node is compound
		// 2. the value is a json array, in which case the info node is compound
		// 3. the value is one of the remaining types (boolean, string, int, double, long, etc)
		//    in which case the info node is a leaf
		InfoNode node = null;
		if( value instanceof JSONObject )
		{
			node = InfoNode.createCompoundNode( null, nodeName, null );
			buildInfoNode( (JSONObject)value, node );
		}
		else if( value instanceof JSONArray )
		{
			buildInfoNode( (JSONArray)value, nodeName, parentNode );
		}
		else
		{
			node = InfoNode.createLeafNode( null, value, nodeName, null );
		}
		return node;
	}
	
	/*
	 * Returns a list of names from the {@link JSONArray} of element names
	 * @param names the {@link JSONArray} of element names
	 * @return The list of element names 
	 */
	private static List< String > getJsonNames( final JSONObject jsonObject )
	{
		// grab the names of the elements
		final JSONArray names = jsonObject.names();
		
		// create the list of names
		final List< String > jsonNames = new ArrayList<>();
		for( int i = 0; i < names.length(); ++i )
		{
			try
			{
				jsonNames.add( names.getString( i ) );
			}
			catch( JSONException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Unable to get the key name from the JSON array." + Constants.NEW_LINE );
				message.append( "  Number Elements: " + names.length() + Constants.NEW_LINE );
				message.append( "  Names: " + Constants.NEW_LINE );
				for( int j = 0; j < names.length(); ++j )
				{
					try
					{
						message.append( "    " + names.getString( j ) + Constants.NEW_LINE );
					}
					catch( JSONException e2 )
					{
						message.append( "    ** missed name **" + Constants.NEW_LINE );
					}
				}
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
		}
		return jsonNames;
	}
	
	/*
	 * Reads the source JSON string from the {@link InputStream} and create the root JSON object
	 * @param clazz the {@link Class} to reconstitute
	 * @param input The {@link InputStream} from which to read the JSON source
	 * @return A string representation of the JSON source
	 */
	private static JSONObject createRootJsonObject( final Class< ? > clazz, final Reader input )
	{
		// parse the source json string into a json object
		String source = null;
		JSONObject jsonObject = null;
		try( final Scanner scanner = new Scanner( input ) )
		{
			source = scanner.useDelimiter( "\\A" ).next();
		
			jsonObject = new JSONObject( source );
		}
		catch( NoSuchElementException e ) 
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to load the JSON from the input stream" + Constants.NEW_LINE );
			message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
		catch( JSONException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to parse JSON string into a JSON object for further processing" + Constants.NEW_LINE );
			message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
			message.append( "  Source JSON String: " + Constants.NEW_LINE );
			message.append( source );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
	
		return jsonObject;
	}

	/*
	 * Creates the root {@link InfoNode} from the root element of the JSON object
	 * @param jsonObject The {@link JSONObject} to be converted into the semantic model
	 * @param clazz The {@link Class} of the object to ultimately build.
	 * @return The root {@link InfoNode}
	 */
	private static InfoNode createRootNode( final JSONObject jsonObject, final Class< ? > clazz )
	{
		// the first/top of the json string must be the class name, so there should only be one key
		final JSONArray names = jsonObject.names();
		if( names.length() != 1 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Root element of the JSON string must have exactly one element representing" + Constants.NEW_LINE );
			message.append( "the class into which the semantic model will be converted." + Constants.NEW_LINE );
			message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
			message.append( "  Number Elements: " + names.length() + Constants.NEW_LINE );
			message.append( "  Names: " + Constants.NEW_LINE );
			for( int i = 0; i < names.length(); ++i )
			{
				try
				{
					message.append( "    " + names.getString( i ) + Constants.NEW_LINE );
				}
				catch( JSONException e )
				{
					message.append( "    ** missed name **" + Constants.NEW_LINE );
				}
			}
			message.append( "  Source JSON Object: " + Constants.NEW_LINE );
			message.append( jsonObject.toString() );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// grab the root name, now that we've confirmed only one element
		String rootName = null;
		try
		{
			rootName = names.getString( 0 );
		}
		catch( JSONException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Couldn't get the root name from the JSON object." + Constants.NEW_LINE );
			message.append( jsonObject.toString() );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// create and return the root info node
		return InfoNode.createRootNode( rootName, clazz );
	}
	
	public static void main( String[] args ) throws FileNotFoundException
	{
		DOMConfigurator.configure( "log4j.xml" );
		
//		final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.json" ) );
//		final Reader input = new InputStreamReader( inputStream );
//		final JsonReader reader = new JsonReader();
//		final InfoNode infoNode = reader.read( Division.class, input );
//		System.out.println( infoNode.simpleTreeToString() );
//		
//		final PersistenceEngine engine = new PersistenceEngine();
//		final Object reperson = engine.parseSemanticModel( Division.class, infoNode );
//		System.out.println( reperson );
		
		final InputStream inputStream = new BufferedInputStream( new FileInputStream( "test.json" ) );
		final Reader input = new InputStreamReader( inputStream );
		final JsonReader reader = new JsonReader();
		final Class< ? > inputClazz = int[].class;
		final InfoNode infoNode = reader.read( inputClazz, input );
		System.out.println( infoNode.simpleTreeToString() );
		
		final PersistenceEngine engine = new PersistenceEngine();
		final Object reperson = engine.parseSemanticModel( inputClazz, infoNode );
		System.out.println( reperson );
	}

}
