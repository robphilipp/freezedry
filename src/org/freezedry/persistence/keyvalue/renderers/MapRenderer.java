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
package org.freezedry.persistence.keyvalue.renderers;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.freezedry.persistence.annotations.PersistMap;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;

/**
 * Renders the subtree of the semantic model that represents a {@link Map}. {@link Map}s are rendered
 * as the map's persistence name followed by the decorated key. For example, a {@code Map< String, Double >}
 * would be persisted in the following format when using the default decorator and settings:
 * <code><pre>
 * Person.friends{"Polly"} = "bird"
 * Person.friends{"Sparky"} = "dog"
 * </pre></code>
 * or for a more complicated map such as {@code Map< String, Map< String, String > >}:
 * <code><pre>
 * Person.groups{"numbers"}{"one"} = "ONE"
 * Person.groups{"numbers"}{"two"} = "TWO"
 * Person.groups{"numbers"}{"three"} = "THREE"
 * Person.groups{"letters"}{"a"} = "AY"
 * Person.groups{"letters"}{"b"} = "BEE"
 * </pre></code>
 * 
 * @author Robert Philipp
 */
public class MapRenderer extends AbstractPersistenceRenderer {

	private static final Logger LOGGER = Logger.getLogger( MapRenderer.class );

	private static String OPEN = "{";
	private static String CLOSE = "}";
	
	private String mapEntryName = PersistMap.ENTRY_PERSIST_NAME;
	private String mapKeyName = PersistMap.KEY_PERSIST_NAME;
	private String mapValueName = PersistMap.VALUE_PERSIST_NAME;
	
	private final StringDecorator keyDecorator;
	private final String decorationRegex;
	private final Pattern decorationPattern;
	private final String validationRegex;
	private final Pattern validationPattern;

	/**
	 * Constructs a key-value {@link MapRenderer} for renderering the key-values for a {@link Map}
	 * @param builder The {@link KeyValueBuilder} that makes calls to this renderer. Recall that this
	 * is part of a recursive algorithm.
	 * @param keyDecorator The decorator for the key. For example, it may surround the key with "<code>{</code>"
	 * and "<code>}</code>". 
	 */
	public MapRenderer( final KeyValueBuilder builder, final String openKey, final String closeKey )
	{
		super( builder );
		
		// create the decorator for the key
		this.keyDecorator = new StringDecorator( openKey, closeKey );
		
		// create the regular expression that determines if a string is renderered by this class
		// people[0] is a collection, but people{"test"}[0] is a map< string, list< integer > >
		// so we want to check that only word characters precede the "["
		final String open = Pattern.quote( openKey );
		final String close = Pattern.quote( closeKey );
		
		// create and compile the regex pattern for the decoration
		decorationRegex = open + "\\p{Punct}?\\w+\\p{Punct}?" + close;
		decorationPattern = Pattern.compile( decorationRegex );
		
		// create and compile the regex pattern for validating the complete key
		validationRegex = "\\w+" + decorationRegex + "|^" + decorationRegex;
		validationPattern = Pattern.compile( validationRegex );
	}

	/**
	 * Constructs a key-value {@link MapRenderer} for renderering the key-values for a {@link Map}.
	 * Decorates the key with the default decorations.
	 * @param builder The {@link KeyValueBuilder} that makes calls to this renderer. Recall that this
	 * is part of a recursive algorithm.
	 */
	public MapRenderer( final KeyValueBuilder builder )
	{
		this( builder, OPEN, CLOSE );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link MapRenderer} to copy
	 */
	public MapRenderer( final MapRenderer renderer )
	{
		super( renderer );
		
		this.keyDecorator = renderer.keyDecorator.getCopy();
		this.decorationRegex = renderer.decorationRegex;
		this.decorationPattern = renderer.decorationPattern;
		this.validationRegex = renderer.validationRegex;
		this.validationPattern = renderer.validationPattern;
	}

	/**
	 * @param name the persistence name in the {@link InfoNode}s representing {@link Map.Entry}
	 * The default value is {@link PersistMap.ENTRY_PERSIST_NAME} 
	 */
	public void setMapEntryName( final String name )
	{
		this.mapEntryName = name;
	}
	
	/**
	 * @return the persistence name in the {@link InfoNode}s representing {@link Map.Entry}
	 */
	public String getMapEntryName()
	{
		return mapEntryName;
	}

	/**
	 * @param name the persistence name in the {@link InfoNode}s representing {@link Map} keys
	 * The default value is {@link PersistMap.KEY_PERSIST_NAME}
	 */
	public void setMapKeyName( final String name )
	{
		this.mapKeyName = name;
	}

	/**
	 * @return the persistence name in the {@link InfoNode}s representing {@link Map} keys
	 */
	public String getMapKeyName()
	{
		return mapKeyName;
	}

	/**
	 * @param name the persistence name in the {@link InfoNode}s representing {@link Map} values
	 * The default value is {@link PersistMap.VALUE_PERSIST_NAME}
	 */
	public void setMapValueName( final String name )
	{
		this.mapValueName = name;
	}

	/**
	 * @return the persistence name in the {@link InfoNode}s representing {@link Map} values
	 */
	public String getMapValueName()
	{
		return mapValueName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, 
								   final String key, 
								   final List< Pair< String, Object > > keyValues, 
								   final boolean isWithholdPersistName )
	{
		// [Division:months{January}[0], 1]
		// [Division:months{January}[1], 2]
		// [Division:systems{ALM}, "Investments and Capital Markets Division"]
		// [Division:systems{SAP}, "Single Family Division"]
		for( InfoNode node : infoNode.getChildren() )
		{
			// the node should be a MapEntry class, if not, then we've got problems, which
			// we will not hesitate to report to the proper authorities.
			if( ReflectionUtils.isSuperclass( Map.Entry.class, node.getClazz() ) )
			{
				// there should be two nodes hanging off the MapEntry: the key and the value.
				// each of these may have their own subnodes.
				final List< InfoNode > entryNodes = node.getChildren();
				if( entryNodes.size() != 2 )
				{
					final StringBuffer message = new StringBuffer();
					message.append( "The MapRenderer expects MapEntry nodes to have exactly 2 subnodes." + Constants.NEW_LINE );
					message.append( "  Number of subnodes: " + entryNodes.size() + Constants.NEW_LINE );
					message.append( "  Persist Name: " + node.getPersistName() );
					LOGGER.error( message.toString() );
					throw new IllegalStateException( message.toString() );
				}
				
				// find the info node that holds the key, and the info node that holds the value
				InfoNode keyNode = null;
				InfoNode valueNode = null;
				final String name1 = entryNodes.get( 0 ).getPersistName(); 
				final String name2 = entryNodes.get( 1 ).getPersistName();
				if( name1.equals( mapKeyName ) && name2.equals( mapValueName ) )
				{
					keyNode = entryNodes.get( 0 );
					valueNode = entryNodes.get( 1 );
				}
				else if( name2.equals( mapKeyName ) && name1.equals( mapValueName ) )
				{
					keyNode = entryNodes.get( 1 );
					valueNode = entryNodes.get( 0 );
				}
				else
				{
					final StringBuffer message = new StringBuffer();
					message.append( "The MapRenderer expects MapEntry nodes to have a key subnode and a value subnode." + Constants.NEW_LINE );
					message.append( "  Required Key Subnode Persist Name: " + mapKeyName + Constants.NEW_LINE );
					message.append( "  Required Value Subnode Persist Name: " + mapValueName + Constants.NEW_LINE );
					message.append( "  First Node's Persist Name: " + name1 + Constants.NEW_LINE );
					message.append( "  Second Node's Persist Name: " + name2 );
					LOGGER.error( message.toString() );
					throw new IllegalStateException( message.toString() );
				}
				
				// no we can continue to parse the nodes.
				String newKey = createKey( key, infoNode );
				if( keyNode.isLeafNode() )
				{
					String value;
					final Object object = keyNode.getValue();
					final Class< ? > clazz = object.getClass();
					if( containsDecorator( clazz ) )
					{
						value = getDecorator( clazz ).decorate( object );
					}
					else
					{
						value = object.toString();
					}
					newKey += keyDecorator.decorate( value );
				}
				else
				{
					// TODO currently we have a slight problem here for compound keys.
					final StringBuffer message = new StringBuffer();
					message.append( "The MapRenderer doesn't allow compound (composite) keys at this point." + Constants.NEW_LINE );
					message.append( "  Current Key: " + newKey + Constants.NEW_LINE );
					LOGGER.error( message.toString() );
					throw new IllegalStateException( message.toString() );
				}
				
				// create the key-value pair and return it. we know that we have value node, and that
				// the persistence name of the value is "Value" or something else set by the user. we
				// don't want to write that out, so we simply remove the value from the node.
				valueNode.setPersistName( "" );
				getPersistenceBuilder().createKeyValuePairs( valueNode, newKey, keyValues, true );
				
				// mark the node as processed so that it doesn't get processed again
				node.setIsProcessed( true );
			}
			else
			{
				final StringBuffer message = new StringBuffer();
				message.append( "The MapRenderer expects the root node of the map to have only subnodes of type MapEntry." + Constants.NEW_LINE );
				message.append( "  InfoNode Type: " + (node.getClazz() == null ? "[null]" : node.getClazz().getName()) + Constants.NEW_LINE );
				message.append( "  Persist Name: " + node.getPersistName() );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString() );
			}
		}
	}

	/*
	 * Creates a new key based on the specified key and the persistence name found in the info node
	 * @param key The current key
	 * @param node The {@link InfoNode}
	 * @return a new key based on the specified key and the persistence name found in the info node
	 */
	private String createKey( final String key, final InfoNode node )
	{
		String newKey = key;
		if( node.getPersistName() != null && !node.getPersistName().isEmpty() )
		{
			newKey += getPersistenceBuilder().getSeparator() + node.getPersistName();
		}
		return newKey;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#buildInfoNode(org.freezedry.persistence.tree.InfoNode, java.util.List)
	 */
	@Override
	public void buildInfoNode( final InfoNode parentNode, final List< Pair< String, String > > keyValues )
	{
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#isRenderer(java.lang.String)
	 */
	@Override
	public boolean isRenderer( String keyElement )
	{
		return validationPattern.matcher( keyElement ).find();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer#getGroupName(java.lang.String)
	 */
	@Override
	public String getGroupName( final String key )
	{
		final Matcher matcher = decorationPattern.matcher( key );
		String group = null;
		if( matcher.find() )
		{
			group = key.substring( 0, matcher.start() );
		}
		return group;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public MapRenderer getCopy()
	{
		return new MapRenderer( this );
	}
}
