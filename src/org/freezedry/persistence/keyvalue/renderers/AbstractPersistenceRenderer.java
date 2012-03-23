package org.freezedry.persistence.keyvalue.renderers;

import java.util.HashMap;
import java.util.Map;

import org.freezedry.persistence.keyvalue.renderers.decorators.BooleanDecorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.Decorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.DoubleDecorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.IntegerDecorator;
import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.freezedry.persistence.utils.Require;
import org.freezedry.persistence.keyvalue.BasicKeyValueBuilder;
import org.freezedry.persistence.writers.PersistenceWriter;

public abstract class AbstractPersistenceRenderer implements PersistenceRenderer {

	private final BasicKeyValueBuilder writer;
	
	public Map< Class< ? >, Decorator > decorators;

	public AbstractPersistenceRenderer( final BasicKeyValueBuilder writer, final Map< Class< ? >, Decorator > decorators )
	{
		Require.notNull( writer );
		this.writer = writer;
		this.decorators = decorators;
	}
	
	/**
	 * Constructor for the {@link PersistenceRenderer}s that stores the associated 
	 * {@link PersistenceWriter} needed for resursion.
	 * @param writer The associated {@link PersistenceWriter}
	 */
	public AbstractPersistenceRenderer( final BasicKeyValueBuilder writer )
	{
		this( writer, createDefaultDecorators() );
	}
	
	/**
	 * Copy constructor
	 * @param renderer
	 */
	public AbstractPersistenceRenderer( final AbstractPersistenceRenderer renderer )
	{
		this.writer = renderer.writer;
		
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
		return ReflectionUtils.getItemOrAncestor( clazz, decorators );
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
	
	/**
	 * @return The persistence writer associated with this renderer for use in recursion.
	 */
	protected BasicKeyValueBuilder getPersistenceWriter()
	{
		return writer;
	}
}
