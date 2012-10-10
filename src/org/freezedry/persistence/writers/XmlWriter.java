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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Writes an the semantic model (starting at the {@link InfoNode} root node) to an XML file through
 * the use of the DOM tree. 
 * 
 * @author Robert Philipp
 */
public class XmlWriter implements PersistenceWriter {
	
	private static final Logger LOGGER = Logger.getLogger( XmlWriter.class );
	
	public static final String TYPE_ATTRIBUTE = "type";
	
	private boolean isDisplayTypeInfo = false;
		
	/**
	 * @return true if type information is written to the XML file
	 */
	public boolean getDisplayTypeInfo()
	{
		return isDisplayTypeInfo;
	}
	
	/**
	 * Set whether to display type info as an attribute in the XML
	 * @param isDisplay set to true to have the type info displayed as an attribute
	 */
	public void setDisplayTypeInfo( final boolean isDisplay )
	{
		isDisplayTypeInfo = isDisplay;
	}
	
	/**
	 * Writes the DOM tree to XML
	 * @param rootNode The root {@link InfoNode} of the semantic model
	 * @throws ParserConfigurationException
	 */
	public void write( final InfoNode rootNode, final Writer output )
	{
		final Document document = buildDom( rootNode );
		if( LOGGER.isInfoEnabled() )
		{
			LOGGER.info( DomUtils.toString( document ) );
		}
		
		// use a Transformer to write the output xml
		final TransformerFactory factory = TransformerFactory.newInstance();
		try
		{
			final DOMSource source = new DOMSource( document );
			final StreamResult result = new StreamResult( output );
			
			final Transformer transformer = factory.newTransformer();
			transformer.transform( source, result );
		}
		catch( TransformerException e )
		{
			throw new IllegalStateException( "Unable to transform the DOM tree to XML.", e );
		}
	}
	
	/**
	 * Builds the DOM tree from the info node tree through recursive algorithm.
	 * @param rootInfoNode The root {@link InfoNode}
	 * @return The DOM tree as a {@link Document}
	 */
	private Document buildDom( final InfoNode rootInfoNode )
	{
		// create the document-builder factory, from which to create the document-builder,
		// from which to create the document, which is the root node into which the new
		// DOM tree will be built.
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch( ParserConfigurationException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to create the DOM document builder:" + Constants.NEW_LINE );
			message.append( "  Root Persistence Name: " + rootInfoNode.getPersistName() );
			LOGGER.error( message.toString() );
			throw new IllegalStateException( message.toString(), e );
		}
		final Document document = builder.newDocument();
		
		// create the first DOM node from the info-node and add it to the document
		final Node domNode = createDomNode( rootInfoNode, document, document );
		
		// recursively build the DOM tree from the info-node tree
		buildDom( rootInfoNode, domNode, document );
		
		// once complete, then return the document (root node of the DOM tree)
		return document;
	}

	/*
	 * Recurses through the {@link InfoNode} tree and builds the DOM {@link Node} tree.
	 * @param infoNode The {@link InfoNode} from which to build the domNode
	 * @param domNode The DOM {@link Node} to which to add the new DOM {@link Node}s
	 * @param document The root node of the DOM tree (which is a {@link Document})
	 */
	private void buildDom( final InfoNode infoNode, final Node domNode, final Document document )
	{
		final List< InfoNode > children = infoNode.getChildren();
		for( InfoNode child : children )
		{
			final Node newDomNode = createDomNode( child, domNode, document );
			domNode.appendChild( newDomNode );
			
			buildDom( child, newDomNode, document );
		}
	}

	/*
	 * Creates a DOM node from the {@link InfoNode}.
	 * @param infoNode The {@link InfoNode} from which to build the domNode
	 * @param domNode The DOM {@link Node} to which to add the new DOM {@link Node}s
	 * @param document The root node of the DOM tree (which is a {@link Document})
	 * @return The DOM node representation of the {@link InfoNode}
	 */
	private Node createDomNode( final InfoNode infoNode, final Node domNode, final Document document )
	{
		final Element newDomNode = document.createElement( infoNode.getPersistName() );
		
		// set the type of the node
		if( isDisplayTypeInfo )
		{
			newDomNode.setAttribute( TYPE_ATTRIBUTE, infoNode.getClazz().getName() );
		}

		// add the node the parent
		domNode.appendChild( newDomNode );

		// if the node is a leaf node, then it has a value, and we need to
		// add a DOM text node that holds the value
		if( infoNode.isLeafNode() )
		{
			final Object value = infoNode.getValue();
			final Node textNode = document.createTextNode( value.toString() );
			newDomNode.appendChild( textNode );
		}
		
		return newDomNode;
	}

	/**
	 * For testing
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 */
	public static void main( String[] args ) throws ParserConfigurationException, ReflectiveOperationException, IOException
	{
		DOMConfigurator.configure( "log4j.xml" );

//		final Division division = new Division();
//		final PersistenceEngine engine = new PersistenceEngine();
//		final InfoNode rootNode = engine.createSemanticModel( division );
//		System.out.println( rootNode.treeToString() );
//
//		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.xml" ) ) )
//		{
//			final XmlWriter writer = new XmlWriter();
//			// uncomment this to prevent type information to be written to XML
////			writer.setDisplayTypeInfo( false );
//			writer.write( rootNode, printWriter );
//		}
//		catch( IOException e )
//		{
//			e.printStackTrace();
//		}
		
//		final int[] test = new int[] { 3, 1, 4, 1, 5, 9, 2, 6, 5, 3 };
		final int[][] test = new int[][] { { 3, 1 }, { 4, 1 }, { 5, 9 }, { 2, 6 }, { 5, 3 } };
		final PersistenceEngine engine = new PersistenceEngine();
		final InfoNode rootNode = engine.createSemanticModel( test );
		System.out.println( rootNode.treeToString() );

		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "test.xml" ) ) )
		{
			final XmlWriter writer = new XmlWriter();
			// uncomment this to prevent type information to be written to XML
//			writer.setDisplayTypeInfo( true );
			writer.write( rootNode, printWriter );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
 