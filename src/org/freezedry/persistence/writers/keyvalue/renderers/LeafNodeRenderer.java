package org.freezedry.persistence.writers.keyvalue.renderers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.containers.orderedseries.IntegerOrderedSeries;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;
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
		
		// create the front part of the key
		String newKey = key;
		if( !isWithholdPersistName && infoNode.getPersistName() != null && !infoNode.getPersistName().isEmpty() )
		{
			newKey += getPersistenceWriter().getSeparator() + infoNode.getPersistName();
		}
		
		// find the decorator, if one exists, that is associated with the class
//		final String value = decorators.get( infoNode.getValue().getClass() ).decorate( infoNode.getValue() );
		final Object object = infoNode.getValue();
		final Class< ? > clazz = object.getClass();
		String value;
		if( containsDecorator( clazz ) )
		{
			value = getDecorator( clazz ).decorate( object );
		}
		else
		{
			value = object.toString();
		}
		keyValues.add( new Pair< String, Object >( newKey, value ) );
	}

	/**
	 * Finds the {@link Decorator} associated with the class. If the specified class
	 * doesn't have a decorator, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the decorator map for the
	 * specified class associating it with the returned decorator (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a decorator
	 * @return the {@link Decorator} associated with the class
	 */
	public Decorator getDecorator( final Class< ? > clazz )
	{
		// simplest case is that the info node builders map has an entry for the class
		Decorator decorator = decorators.get( clazz );
		
		// if the info node builder didn't have a direct entry, work our way up the inheritance
		// hierarchy, and find the closed parent class, assigning it its associated info node builder
		if( decorator == null )
		{
			// run through the available info node builders holding the distance (number of levels in the
			// inheritance hierarchy) they are from the specified class
			final IntegerOrderedSeries< Class< ? > > hierarchy = new IntegerOrderedSeries<>();
			for( Map.Entry< Class< ? >, Decorator > entry : decorators.entrySet() )
			{
				final Class< ? > targetClass = entry.getKey();
				final int level = ReflectionUtils.calculateClassDistance( clazz, targetClass );
				if( level > -1 )
				{
					hierarchy.add( level, targetClass );
				}
			}
			
			// if one or more parent classes were found, then take the first one,
			// which is the closest one, grab its info node builder, and add an entry for the
			// specified class to the associated info node builder for faster subsequent look-ups
			if( !hierarchy.isEmpty() )
			{
				final Class< ? > closestParent = hierarchy.getFirstValue();
				decorator = decorators.get( closestParent );
				decorators.put( clazz, decorator.getCopy() );
			}
		}
		return decorator;
	}

	/**
	 * Finds the {@link PersistenceRenderer} associated with the class. If the specified class
	 * doesn't have a renderer, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the persistence renderer map for the
	 * specified class associating it with the returned persistence renderer (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a persistence renderer
	 * @return the true if a persistence renderer was found; false otherwise
	 */
	public boolean containsDecorator( final Class< ? > clazz )
	{
		return ( getDecorator( clazz ) != null );
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
