package org.freezedry.persistence.writers.keyvalue.renderers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.writers.keyvalue.KeyValueWriter;
import org.freezedry.persistence.writers.keyvalue.renderers.decorators.BooleanDecorator;
import org.freezedry.persistence.writers.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.writers.keyvalue.renderers.decorators.DoubleDecorator;
import org.freezedry.persistence.writers.keyvalue.renderers.decorators.IntegerDecorator;
import org.freezedry.persistence.writers.keyvalue.renderers.decorators.StringDecorator;

public class LeafNodeRenderer extends AbstractPersistenceRenderer {
	
	private static final Logger LOGGER = Logger.getLogger( LeafNodeRenderer.class );
	
	public Map< Class< ? >, Decorator > decorators;

	public LeafNodeRenderer( final KeyValueWriter writer, final Map< Class< ? >, Decorator > decorators )
	{
		super( writer );

		this.decorators = decorators;
	}
	
	public LeafNodeRenderer( final KeyValueWriter writer )
	{
		this( writer, createDefaultDecorators() );
	}
	
	public LeafNodeRenderer( final LeafNodeRenderer renderer )
	{
		super( renderer );
		
		// make a deep copy of the decorators
		decorators = new HashMap< Class< ? >, Decorator >();
		for( final Map.Entry< Class< ? >, Decorator > entry : renderer.decorators.entrySet() )
		{
			decorators.put( entry.getKey(), entry.getValue().getCopy() );
		}
	}
	
	private static Map< Class< ? >, Decorator > createDefaultDecorators()
	{
		final Map< Class< ? >, Decorator > decorators = new HashMap<>();
		decorators.put( String.class, new StringDecorator() );

		decorators.put( Integer.class, new IntegerDecorator() );
		decorators.put( Long.class, new IntegerDecorator() );
		decorators.put( Short.class, new IntegerDecorator() );
		decorators.put( Double.class, new DoubleDecorator() );
		decorators.put( Boolean.class, new BooleanDecorator() );

		decorators.put( Integer.TYPE, new IntegerDecorator() );
		decorators.put( Long.TYPE, new IntegerDecorator() );
		decorators.put( Short.TYPE, new IntegerDecorator() );
		decorators.put( Double.TYPE, new DoubleDecorator() );
		decorators.put( Boolean.TYPE, new BooleanDecorator() );
		
		return decorators;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.keyvalue.renderers.PersistenceRenderer#buildKeyValuePair(org.freezedry.persistence.tree.InfoNode, java.lang.String, java.util.List)
	 */
	@Override
	public void buildKeyValuePair( final InfoNode infoNode, final String key, final List< Pair< String, Object > > keyValues, final boolean isWithholdPersistName )
	{
		// ensure that the info node is a leaf
		if( !infoNode.isLeafNode() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "To render a key-value pair as a String, the info node must be a leaf node." + Constants.NEW_LINE );
			message.append( "  Current Key:" + key + Constants.NEW_LINE );
			message.append( "  InfoNode:" + Constants.NEW_LINE );
			message.append( "    Persist Name: " + infoNode.getPersistName() + Constants.NEW_LINE );
			message.append( "    Node Type: " + infoNode.getNodeType().name() + Constants.NEW_LINE );
			message.append( "    Child Nodes: " + infoNode.getChildCount() + Constants.NEW_LINE );
			message.append( "    Node Class Type: " + infoNode.getClazz().getName() + Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		String newKey = key;
		if( !isWithholdPersistName && infoNode.getPersistName() != null && !infoNode.getPersistName().isEmpty() )
		{
			newKey += ":" + infoNode.getPersistName();
		}
//		final String value = "\"" + (String)infoNode.getValue() + "\"";
		final String value = decorators.get( infoNode.getValue().getClass() ).decorate( infoNode.getValue() );
		keyValues.add( new Pair< String, Object >( newKey, value ) );
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public LeafNodeRenderer getCopy()
	{
		return new LeafNodeRenderer( this );
	}

}
