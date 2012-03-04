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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities required for dealing with the DOM tree.
 * 
 * @author Robert Philipp
 */
public class DomUtils {
	
	/**
	 * Prints the DOM tree starting at the specified {@link Node}
	 * @param node The root {@link Node} for printing the DOM tree
	 */
	public static String toString( final Node node )
	{
		return toString( node, 0 );
	}
	
	/*
	 * The recursive method from printing the DOM tree.
	 * @param node The current {@link Node} to log
	 * @param level current level in the tree. Root node = 0; next level = 1, etc.
	 */
	private static String toString( final Node node, final int level )
	{
		final StringBuffer buffer = new StringBuffer();
		
		// add a level of indent
		for( int i = 0; i < level; ++i )
		{
			buffer.append( "  " );
		}
		
		// append the string version of the node to the buffer
		buffer.append( node.toString() );

		// add the attributes
		final NamedNodeMap nodeMap = node.getAttributes();
		if( nodeMap != null )
		{
			buffer.append( " (" );
			for( int i = 0; i < nodeMap.getLength(); ++i )
			{
				final Node attr = nodeMap.item( i );
				buffer.append( attr + ( i < nodeMap.getLength()-1 ? "; " : "" ) ); 
			}
			buffer.append( ")" );
		}
		buffer.append( Constants.NEW_LINE );
		
		// if there are any children, visit each one
		final NodeList list = node.getChildNodes();
		for( int i = 0; i < list.getLength(); i++ )
		{
			// get child node
			final Node childNode = list.item( i );

			// visit child node
			buffer.append( toString( childNode, level + 1 ) );
		}
		
		return buffer.toString();
	}

	/**
	 * Removes empty spaces from elements
	 * @param document The root {@link Node} of the DOM tree
	 * @return The same DOM {@link Node} for which all the empty elements have been removed
	 */
	public static Document removeFormattingTextNodes( final Document document )
	{
		final Pattern pattern = Pattern.compile( "[\\s]*" );
		return removeFormattingTextNodes( document, pattern );
	}
	
	/**
	 * Removes empty spaces from elements
	 * @param document The root {@link Node} of the DOM tree
	 * @param regexPattern The pattern describing what an empty element looks like.
	 * @return The same DOM {@link Node} for which all the empty elements have been removed
	 * @see #removeFormattingTextNodes(Document)
	 */
	public static Document removeFormattingTextNodes( final Document document, final Pattern regexPattern )
	{
		cleanTextNodes( document, regexPattern );
		return document;
	}
	
	/*
	 * Removes empty spaces from elements
	 * @param document The root {@link Node} of the DOM tree
	 * @param regexPattern The pattern describing what an empty element looks like.
	 */
	private static void cleanTextNodes( final Node node, final Pattern regexPattern )
	{
		final NodeList nodes = node.getChildNodes();
		for( int i = 0; i < nodes.getLength(); ++i )
		{
			final Node testNode = nodes.item( i );
			if( testNode.getNodeType() == Node.TEXT_NODE  )
			{
				final Matcher matcher = regexPattern.matcher( testNode.getTextContent().toString() );
				if( matcher.matches() )
				{
					node.removeChild( testNode );
					--i;
				}
			}
			else
			{
				cleanTextNodes( testNode, regexPattern );
			}
		}
	}
}
