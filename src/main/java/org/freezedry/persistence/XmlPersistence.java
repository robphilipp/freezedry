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

import org.freezedry.persistence.readers.XmlReader;
import org.freezedry.persistence.writers.XmlWriter;

import java.io.Reader;
import java.io.Writer;

/**
 * Convenience class that allows reading/writing persisted object from/to files. This
 * class provides an implementation of the {@link Persistence} interface that 
 * reads persisted object from a {@link Reader} and writes objects to their persisted
 * form to a {@link Writer}.<p>
 * 
 * Additionally, this class provides access to the read and write methods from its
 * parent {@link AbstractFileBasedPersistence} that accept a file name instead of a stream.
 * The code below provides and example of reading and writing an object.
 * <pre>
 * {@code
 * final XmlPersistence persistence = new XmlPersistence();
 * 
 // write the the object to its persisted to an XML file
 * persistence.write( division, "person.xml" );
 * 
 // read the persisted form of the object back into an object from the XML file
 * final Division redivision = (Division)persistence.read( Division.class, "person.xml" );
 * }</pre>
 *  
 * @author Robert Philipp
 */
public class XmlPersistence extends AbstractFileBasedPersistence {
	
	private XmlWriter xmlWriter;
	private XmlReader xmlReader;
	private boolean isDisplayTypeInfo = false;
	
	/**
	 * Set whether to display type info as an attribute in the XML
	 * @param isDisplay set to true to have the type info displayed as an attribute
	 */
	public void setDisplayTypeInfo( final boolean isDisplay )
	{
		this.isDisplayTypeInfo = isDisplay;
		getPersistenceWriter().setDisplayTypeInfo( isDisplay );
	}
	
	/**
	 * @return true if type information is written to the XML file
	 */
	public boolean isDisplayTypeInfo()
	{
		return isDisplayTypeInfo;
	}
	
	/**
	 * Creates the {@link XmlWriter} if it hasn't yet been instantiated, and returns it
	 * @return the {@link XmlWriter}
	 */
	@Override
	protected XmlWriter getPersistenceWriter()
	{
		if( xmlWriter == null )
		{
			xmlWriter = new XmlWriter();
			xmlWriter.setDisplayTypeInfo( isDisplayTypeInfo );
		}
		return xmlWriter;
	}
	
	/**
	 * Creates the {@link XmlReader} if it hasn't yet been instantiated, and returns it
	 * @return the {@link XmlReader}
	 */
	@Override
	protected XmlReader getPersistenceReader()
	{
		if( xmlReader == null )
		{
			xmlReader = new XmlReader();
		}
		return xmlReader;
	}
}
