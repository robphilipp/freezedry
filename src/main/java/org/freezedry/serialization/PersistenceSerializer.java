/*
 * Copyright 2012 Robert Philipp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freezedry.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.Persistence;
import org.freezedry.persistence.utils.Constants;

/**
 * Serializes objects into a serializes for specified by the {@link Persistence} engine, and deserializes
 * that form back into objects using the same {@link Persistence} engine.
 * 
 * @author Robert Philipp
 */
public class PersistenceSerializer implements Serializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( PersistenceSerializer.class );

	private Persistence persistence;
	
	/**
	 * Constructs an object serializer that uses the FreezeDry persistence framework to 
	 * serialize the objects
	 * @param persistence The {@link Persistence} used to serialize and deserialize objects
	 */
	public PersistenceSerializer( final Persistence persistence )
	{
		this.persistence = persistence;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.serializer.Serializer#serialize(java.lang.Object, java.io.OutputStream)
	 */
	@Override
	public synchronized void serialize( final Object object, final OutputStream output )
	{
		// convert the OutputStream to a Writer
		try( final OutputStreamWriter out = new OutputStreamWriter( output ) )
		{
			// have the Persistence write the object to the output stream
			if( object != null )
			{
				persistence.write( object, out );
			}
		
			out.close();
		}
		catch( IOException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Unable to serialize object to output stream:" ).append( Constants.NEW_LINE );
			message.append( "  Output Stream Type: " ).append( output.getClass().getName() ).append( Constants.NEW_LINE );
			message.append( "  Object Type: " ).append( object != null ? object.getClass().getName() : null ).append( Constants.NEW_LINE );
			message.append( "  Object: " ).append( object != null ? object.toString() : null ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.serializer.Serializer#deserialize(java.io.InputStream, java.lang.Class)
	 */
	@Override
	public synchronized < T > T deserialize( final InputStream input, final Class< T > clazz )
	{
		T object;
		// convert the InputStream to a Reader
		try( final InputStreamReader in = new InputStreamReader( input ) )
		{
			// create the object from the input stream
			object = persistence.read( clazz, in );
		}
		catch( IOException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Unable to serialize object to output stream:" ).append( Constants.NEW_LINE );
			message.append( "  Input Stream Type: " ).append( input.getClass().getName() ).append( Constants.NEW_LINE );
			message.append( "  Object Type: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		
		return object;
	}
}
