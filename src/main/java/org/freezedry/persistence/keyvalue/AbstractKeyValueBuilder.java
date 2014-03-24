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
package org.freezedry.persistence.keyvalue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.freezedry.persistence.keyvalue.renderers.decorators.StringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.keyvalue.renderers.CollectionRenderer;
import org.freezedry.persistence.keyvalue.renderers.LeafNodeRenderer;
import org.freezedry.persistence.keyvalue.renderers.MapRenderer;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.ReflectionUtils;

/**
 * {@link AbstractKeyValueBuilder} that holds information about the mapping between classes
 * and their {@link PersistenceRenderer}s, and specifies the separator to be used between the
 * elements of the key.
 * 
 * @author Robert Philipp
 */
public abstract class AbstractKeyValueBuilder implements KeyValueBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractKeyValueBuilder.class );

	public static final String KEY_ELEMENT_SEPARATOR = ":";

	private Map< Class< ? >, PersistenceRenderer > renderers;
	private PersistenceRenderer arrayRenderer;
	
	private String separator;

	private boolean isShowFullKey = false;
	
	/**
	 * Constructs a basic key-value builder that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link InfoNode} and
	 * the {@link PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link PersistenceRenderer} used to create key-value pairs for
	 * {@link InfoNode}s that represent an array.
	 * @param separator The separator between the flattened elements of the key
	 * @see AbstractKeyValueBuilder#getRenderer(Class)
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
	 * Constructs a basic key-value builder that uses the default renderers and the specified separator.
	 * @param separator The separator between the flattened elements of the key
	 * @see AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public AbstractKeyValueBuilder( final String separator )
	{
		renderers = createDefaultRenderers();
		arrayRenderer = new CollectionRenderer( this );
		this.separator = separator;
	}

	/**
	 * Constructs a basic key-value builder that uses the default renderers and separator.
	 * @see AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public AbstractKeyValueBuilder()
	{
		renderers = createDefaultRenderers();
		arrayRenderer = new CollectionRenderer( this );
		separator = KEY_ELEMENT_SEPARATOR;
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

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#setSeparator(java.lang.String)
	 */
	@Override
	public void setSeparator( final String separator )
	{
		this.separator = separator;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#setShowFullKey(boolean)
	 */
	@Override
	public void setShowFullKey( final boolean isShowFullKey )
	{
		this.isShowFullKey = isShowFullKey;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#isShowFullKey()
	 */
	@Override
	public boolean isShowFullKey()
	{
		return isShowFullKey;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#setRenderers(java.util.Map)
	 */
	@Override
	public void setRenderers( final Map< Class< ? >, PersistenceRenderer > renderers )
	{
		this.renderers = renderers;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#putRenderer(java.lang.Class, org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer)
	 */
	@Override
	public PersistenceRenderer putRenderer( final Class< ? > clazz, final PersistenceRenderer renderer )
	{
		return renderers.put( clazz, renderer );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#removeRenderer(java.lang.Class)
	 */
	@Override
	public PersistenceRenderer removeRenderer( final Class< ? > clazz )
	{
		return renderers.remove( clazz );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#setArrayRenderer(org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer)
	 */
	@Override
	public void setArrayRenderer( final PersistenceRenderer renderer )
	{
		this.arrayRenderer = renderer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#getArrayRenderer()
	 */
	@Override
	public PersistenceRenderer getArrayRenderer()
	{
		return arrayRenderer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.keyvalue.KeyValueBuilder#getRenderer(java.lang.Class)
	 */
	@Override
	public PersistenceRenderer getRenderer( final Class< ? > clazz )
	{
		return ReflectionUtils.getItemOrAncestorCopyable( clazz, renderers );
	}
	
	/**
	 * Finds the {@link PersistenceRenderer} for which the specified key matches its regular expression for keys;
	 * null if no {@link PersistenceRenderer} is found.
	 * @param key The key to test
	 * @return the {@link PersistenceRenderer} for which the specified key matches its regular expression for keys; 
	 * null if no {@link PersistenceRenderer} is found.
	 */
	public PersistenceRenderer getRenderer( final String key )
	{
		for( PersistenceRenderer renderer : renderers.values() )
		{
			if( renderer.isRenderer( key ) )
			{
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug( "Selected renderer, " + renderer.getClass().getName() + ", for key, " + key );
				}
				return renderer;
			}
		}

		// log the fact that no renderer was found
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Unable to select renderer for key, " ).append( key ).append( ", tried:" );
			for( PersistenceRenderer renderer : renderers.values() )
			{
				message.append( Constants.NEW_LINE ).append( "  " ).append( renderer.getClass().getName() );
			}
//			message.append( Constants.NEW_LINE ).append( "Using " ).append( renderers.get( String.class ).getClass().getName() )
//					.append( ", which is the default for a string" );
			LOGGER.info( message.toString() );
		}

		return null;
//		return renderers.get( String.class );
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
