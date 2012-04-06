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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.freezedry.persistence.utils.Constants;

/**
 * Abstract class that provides methods to read the persisted form of the object
 * from a file with the specified name, and write an object in its persisted form to a file 
 * with a specified name.<p>
 * 
 * This class converts the file names into a {@link PrintWriter} and {@link InputStreamReader}
 * an forwards the calls to the {@link #write(Object, java.io.Writer)} and {@link #read(Class, Reader)}
 * methods implemented by the subclasses, respectively.
 *   
 * @author Robert Philipp
 */
public abstract class AbstractFileBasedPersistence extends AbstractPersistence {

	private static final Logger LOGGER = Logger.getLogger( XmlPersistence.class );

	/**
	 * Writes the specified object into the file using the persistence mechanism implemented in
	 * the concrete subclasses. 
	 * @param object The object to be persisted
	 * @param fileName The name of the file into which to persist the object
	 */
	public void write( final Object object, final String fileName )
	{
		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( fileName ) ) )
		{
			write( object, printWriter );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to open the file for writing" + Constants.NEW_LINE );
			message.append( "  File Name: " + fileName + Constants.NEW_LINE );
			message.append( "  Object: " + object.toString() );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
	}
	
	/**
	 * Reads an object from the persisted form in the file with the specified name, using the specifed
	 * class as the template for the object
	 * @param clazz The {@link Class} from which to create the object from its persisted form
	 * @param fileName The name of the file holding the persisted object
	 * @return The reconstitued object read from the file 
	 */
	public < T > T read( final Class< T > clazz, final String fileName )
	{
		Object object = null;
		try( final InputStream inputStream = new BufferedInputStream( new FileInputStream( fileName ) ) )
		{
			final Reader input = new InputStreamReader( inputStream );
			object = read( clazz, input );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to open the file for reading" + Constants.NEW_LINE );
			message.append( "  File Name: " + fileName + Constants.NEW_LINE );
			message.append( "  Class: " + clazz.getName() );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return clazz.cast( object );
	}
}
