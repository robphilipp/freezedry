package org.freezedry.persistence.writers.keyvalue.renderers;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freezedry.persistence.annotations.PersistMap;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.freezedry.persistence.writers.keyvalue.KeyValueWriter;
import org.freezedry.persistence.writers.keyvalue.renderers.decorators.StringDecorator;

public class MapRenderer extends AbstractPersistenceRenderer {

	private static final Logger LOGGER = Logger.getLogger( MapRenderer.class );

	private static StringDecorator KEY_DECORATOR = new StringDecorator( "{", "}" );
	
	private String mapEntryName = PersistMap.ENTRY_PERSIST_NAME;
	private String mapKeyName = PersistMap.KEY_PERSIST_NAME;
	private String mapValueName = PersistMap.VALUE_PERSIST_NAME;
	
	private StringDecorator keyDecorator;

	/**
	 * 
	 * @param writer
	 */
	public MapRenderer( final KeyValueWriter writer, final StringDecorator indexDecorator )
	{
		super( writer );
		
		this.keyDecorator = indexDecorator.getCopy();
	}

	/**
	 * 
	 * @param writer
	 */
	public MapRenderer( final KeyValueWriter writer )
	{
		this( writer, KEY_DECORATOR );
	}
	
	/**
	 * Copy constructor
	 * @param renderer The {@link MapRenderer} to copy
	 */
	public MapRenderer( final MapRenderer renderer )
	{
		super( renderer );
		
		this.keyDecorator = renderer.keyDecorator.getCopy();
	}

	public void setMapEntryName( final String name )
	{
		this.mapEntryName = name;
	}
	
	public String getMapEntryName()
	{
		return mapEntryName;
	}

	public void setMapKeyName( final String name )
	{
		this.mapKeyName = name;
	}

	public String getMapKeyName()
	{
		return mapKeyName;
	}

	public void setMapValueName( final String name )
	{
		this.mapValueName = name;
	}

	public String getMapValueName()
	{
		return mapValueName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.keyvalue.renderers.PersistenceRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersistName )
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
					newKey += keyDecorator.decorate( keyNode.getValue() );
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
				getPersistenceWriter().createKeyValuePairs( valueNode, newKey, keyValues, true );
				
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
	
	private String createKey( final String key, final InfoNode node )
	{
		String newKey = key;
		if( node.getPersistName() != null && !node.getPersistName().isEmpty() )
		{
			newKey += getPersistenceWriter().getSeparator() + node.getPersistName();
		}
		return newKey;
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
