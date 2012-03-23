package org.freezedry.persistence.keyvalue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.freezedry.persistence.keyvalue.renderers.CollectionRenderer;
import org.freezedry.persistence.keyvalue.renderers.LeafNodeRenderer;
import org.freezedry.persistence.keyvalue.renderers.MapRenderer;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.utils.ReflectionUtils;

public abstract class AbstractKeyValueBuilder implements KeyValueBuilder {

	protected static final String SEPARATOR = ":";

	private Map< Class< ? >, PersistenceRenderer > renderers;
	private PersistenceRenderer arrayRenderer;
	
	private String separator;
	
	/**
	 * 
	 */
	public AbstractKeyValueBuilder( final Map< Class< ? >, PersistenceRenderer > renderers, 
									final PersistenceRenderer arrayRenderer,
									final String separator )
	{
		this.renderers = renderers;
		this.arrayRenderer = arrayRenderer;
		this.separator = separator;
	}

	/**
	 * 
	 */
	public AbstractKeyValueBuilder( final String separator )
	{
		renderers = createDefaultRenderers();
		arrayRenderer = new CollectionRenderer( this );
		this.separator = separator;
	}

	/**
	 * 
	 */
	public AbstractKeyValueBuilder()
	{
		renderers = createDefaultRenderers();
		arrayRenderer = new CollectionRenderer( this );
		separator = SEPARATOR;
	}
	
	/*
	 * @return The mapping between class and their associated renderer
	 */
	private Map< Class< ? >, PersistenceRenderer > createDefaultRenderers()
	{
		final Map< Class< ? >, PersistenceRenderer > renderers = new HashMap<>();
		renderers.put( Collection.class, new CollectionRenderer( this ) );
		renderers.put( Map.class, new MapRenderer( this ) );

		renderers.put( String.class, new LeafNodeRenderer( this ) );
		
		renderers.put( Integer.class, new LeafNodeRenderer( this ) );
		renderers.put( Long.class, new LeafNodeRenderer( this ) );
		renderers.put( Short.class, new LeafNodeRenderer( this ) );
		renderers.put( Double.class, new LeafNodeRenderer( this ) );
		renderers.put( Boolean.class, new LeafNodeRenderer( this ) );
		
		renderers.put( Integer.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Long.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Short.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Double.TYPE, new LeafNodeRenderer( this ) );
		renderers.put( Boolean.TYPE, new LeafNodeRenderer( this ) );
		
		return renderers;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#getSeparator()
	 */
	@Override
	public String getSeparator()
	{
		return separator;
	}
	/**
	 * Sets the separator between the elements of the key.
	 * @param separator The separator that is placed between the elements of the key
	 */
	public void setSeparator( final String separator )
	{
		this.separator = separator;
	}
	
	public void setRenderers( final Map< Class< ? >, PersistenceRenderer > renderers )
	{
		this.renderers = renderers;
	}
	
	public void setArrayRenderer( final PersistenceRenderer renderer )
	{
		this.arrayRenderer = renderer;
	}
	
	public PersistenceRenderer getArrayRenderer()
	{
		return arrayRenderer;
	}

	/**
	 * Finds the {@link PersistenceRenderer} associated with the class. If the specified class
	 * doesn't have a renderer, then it searches for the closest parent class (inheritance)
	 * and returns that. In this case, it adds an entry to the persistence renderer map for the
	 * specified class associating it with the returned persistence renderer (performance speed-up for
	 * subsequent calls).
	 * @param clazz The class for which to find a persistence renderer
	 * @return the {@link PersistenceRenderer} associated with the class
	 */
	public PersistenceRenderer getRenderer( final Class< ? > clazz )
	{
		return ReflectionUtils.getItemOrAncestor( clazz, renderers );
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
	public boolean containsRenderer( final Class< ? > clazz )
	{
		return ( getRenderer( clazz ) != null );
	}
	
}
