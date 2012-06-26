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
package org.freezedry.persistence.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * Writes the semantic model, specified by the root {@link InfoNode} to the specified {@link PrintWriter}
 * as a JSON string.
 * 
 * @author Robert Philipp
 */
public class JsonWriter implements PersistenceWriter {

	private static final Logger LOGGER = Logger.getLogger( JsonWriter.class );
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.PersistenceWriter#write(org.freezedry.persistence.tree.InfoNode, java.io.PrintWriter)
	 */
	@Override
	public void write( final InfoNode rootNode, final Writer output )
	{
		Pair< String, JSONObject > keyValue = null;
		try
		{
//			if( rootNode.hasChildren() )
//			{
				// build (recursively) the JSON object contained by the the class represented in the root node
				keyValue = buildJsonObject( rootNode );
				
				// write the JSON string to the output stream. the JSON string has uses the 
				// persistence name of the root node as the key, and the value is the JSON object
				// that we just created
				new JSONWriter( output )
					.object()
						.key( keyValue.getFirst() )
						.value( keyValue.getSecond() )
					.endObject();
//			}
//			else
//			{
//				new JSONWriter( output )
//					.object()
//						.key( rootNode.getPersistName() )
//						.value( rootNode.getValue() )
//					.endObject();
//			}
		}
		catch( JSONException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to write the JSON object to the output stream." + Constants.NEW_LINE );
			if( keyValue == null )
			{
				message.append( "** Failed to construct the JSON object from the semantic model **" + Constants.NEW_LINE );
				message.append( "  Semantic Model: " + Constants.NEW_LINE );
				message.append( rootNode.treeToString() );
			}
			else
			{
				message.append( "  Key Name: " + keyValue.getFirst() + Constants.NEW_LINE );
				message.append( "  JSON Object: " + Constants.NEW_LINE );
				message.append( keyValue.getSecond().toString() );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
	}
	
	/*
	 * Builds the JSON object and returns the key name and {@link JSONObject} body representation
	 * of the semantic model. Calls the recursive method {@link #buildJsonObject(InfoNode, JSONObject)}.
	 * @param rootNode The root {@link InfoNode} of the semantic model.
	 * @return The (key, value) pair representing the JSON key and the JSON body
	 */
	private Pair< String, JSONObject >  buildJsonObject( final InfoNode rootNode )
	{
		// grab the root key
		final String rootKey = rootNode.getPersistName();
		
		// create the JSON object representation of the semantic model
		final JSONObject jsonObject = new JSONObject();
			
		// recursively build the JSON object form the semantic model
		buildJsonObject( rootNode, jsonObject );

		// return the key and json object as a pair
		return new Pair< String, JSONObject >( rootKey, jsonObject );
	}
	
	/*
	 * Recursive method that builds up the {@link JSONObject} from the {@link InfoNode} tree (the
	 * semantic model).
	 * @param node The current {@link InfoNode} in the semantic model tree.
	 * @param jsonObject The {@link JSONObject} currently being worked on. This could be the root
	 * object or one that has been created during the recursion.
	 */
	private void buildJsonObject( final InfoNode node, final JSONObject jsonObject )
	{
		// loop through the children of the current info node, building up the json object,
		// and building new ones for compound or root nodes.
		for( InfoNode currentNode : node.getChildren() )
		{
			try
			{
				// for leaf nodes we just add to the current json object.
				if( currentNode.isLeafNode() )
				{
					jsonObject.accumulate( currentNode.getPersistName(), currentNode.getValue() );
				}
				// for compound or root nodes we add a newly created json object (which recursively
				// calls this method again)
				else if( currentNode.isCompoundfNode() || currentNode.isRootNode() )
				{
					jsonObject.accumulate( currentNode.getPersistName(), createJsonObject( currentNode ) );
				}
			}
			catch( JSONException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Problem constructing node:" + Constants.NEW_LINE );
				message.append( "  Node Persistence Name: " + currentNode.getPersistName() + Constants.NEW_LINE );
				message.append( "  Node Type: " + currentNode.getNodeType().toString() + Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString(), e );
			}
		}
	}
	
	/*
	 * Recursive method that is called from, and then calls, {@link #buildJsonObject(InfoNode, JSONObject)} to
	 * build the {@link JSONObject} from the specified {@link InfoNode}.
	 * @param node The {@link InfoNode} from which to create the {@link JSONObject}
	 * @return the {@link JSONObject} representation of the specified {@link InfoNode}
	 */
	private JSONObject createJsonObject( final InfoNode node )
	{
		final JSONObject jsonObject = new JSONObject();
		buildJsonObject( node, jsonObject );
		return jsonObject;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		try
		{
			DOMConfigurator.configure( "log4j.xml" );
	
			final Division division = new Division();
			final Person johnny = new Person( "Hernandez", "Johnny", 13 );
//			johnny.addFriend( "Polly", "bird" );
//			johnny.addFriend( "Sparky", "dog" );
//			for( int i = 0; i < 10; ++i )
//			{
//				johnny.addMood( Math.sin( Math.PI / 4 * i ) );
//			}
//			Map< String, String > group = new LinkedHashMap<>();
//			group.put( "one", "ONE" );
//			group.put( "two", "TWO" );
//			group.put( "three", "THREE" );
//			johnny.addGroup( "numbers", group );
//	
//			group = new LinkedHashMap<>();
//			group.put( "a", "AY" );
//			group.put( "b", "BEE" );
//			johnny.addGroup( "letters", group );
//			
//			johnny.setBirthdate( DateUtils.createDateFromString( "1963-04-22", "yyyy-MM-dd" ) );
//			
			division.addPerson( johnny );
			
	
			division.addPerson( new Person( "Prosky", "Julie", 15 ) );
			division.addPerson( new Person( "Jones", "Janet", 13 ) );
			division.addPerson( new Person( "Ghad", "Booda", 17 ) );
			
//			division.addMonth( "January", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//			division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 2, 3, 28 ) ) );
//			division.addMonth( "March", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//			division.addMonth( "April", new HashSet<>( Arrays.asList( 1, 2, 3, 30 ) ) );
	
			final PersistenceEngine engine = new PersistenceEngine();
			
			final InfoNode rootNode = engine.createSemanticModel( division );
			System.out.println( rootNode.treeToString() );
	
			
			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "test.json" ) ) )
			{
				final JsonWriter writer = new JsonWriter();
				writer.write( rootNode, printWriter );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
