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
package org.freezedry.persistence;

import java.io.Reader;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.freezedry.persistence.readers.JsonReader;
import org.freezedry.persistence.readers.PersistenceReader;
import org.freezedry.persistence.readers.XmlReader;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.writers.JsonWriter;
import org.freezedry.persistence.writers.PersistenceWriter;
import org.freezedry.persistence.writers.XmlWriter;

/**
 * Abstract class implementing the {@link Persistence} interface, and managing the {@link PersistenceEngine}.<p>
 * 
 * Implementing subclasses must implement two methods:
 * <ul>
 * 	<ol>{@link #getPersistenceReader()}</ol>
 * 	<ol>{@link #getPersistenceWriter()}</ol>
 * </ul>
 * For example, the {@link XmlPersistence} class will return {@link XmlReader} and {@link XmlWriter}
 * objects, respectively. And the {@link JsonPersistence} class will return {@link JsonReader} and {@link JsonWriter}
 * objects, respectively.
 *  
 * @author Robert Philipp
 */
public abstract class AbstractPersistence implements Persistence {

	private static final Logger LOGGER = Logger.getLogger( AbstractPersistence.class );
	
	private final PersistenceEngine engine;

	/**
	 * Default constructor for persistence of objects
	 */
	public AbstractPersistence()
	{
		engine = new PersistenceEngine();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.Persistence#getPersistenceEngine()
	 */
	@Override
	public PersistenceEngine getPersistenceEngine()
	{
		return engine;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.Persistence#write(java.lang.Object, java.io.Writer)
	 */
	@Override
	public void write( final Object object, final Writer writer )
	{
		// create the semantic model
		final InfoNode rootNode = getPersistenceEngine().createSemanticModel( object );
		if( LOGGER.isInfoEnabled() )
		{
			LOGGER.info( rootNode.simpleTreeToString() );
		}

		// write out XML
		getPersistenceWriter().write( rootNode, writer );
	}
	
	/**
	 * @return the {@link PersistenceWriter} specific to the subclass implementation of the 
	 * {@link Persistence} interface
	 */
	abstract protected PersistenceWriter getPersistenceWriter();

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.Persistence#read(java.lang.Class, java.io.Reader)
	 */
	@Override
	public Object read( final Class< ? > clazz, final Reader reader )
	{
		final InfoNode rootNode = getPersistenceReader().read( clazz, reader );
		if( LOGGER.isInfoEnabled() )
		{
			LOGGER.info( rootNode.simpleTreeToString() );
		}
		
		return getPersistenceEngine().parseSemanticModel( clazz, rootNode );
	}

	/**
	 * @return the {@link PersistenceReader} specific to the subclass implementation of the 
	 * {@link Persistence} interface
	 */
	abstract protected PersistenceReader getPersistenceReader();
}
