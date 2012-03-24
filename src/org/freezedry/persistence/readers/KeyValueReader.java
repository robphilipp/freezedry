package org.freezedry.persistence.readers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;

public class KeyValueReader implements PersistenceReader {

	private static final Logger LOGGER = Logger.getLogger( KeyValueReader.class );
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.readers.PersistenceReader#read(java.lang.Class, java.io.Reader)
	 */
	@Override
	public InfoNode read( final Class< ? > clazz, final Reader input )
	{
		// read the file into a list of key-value pairs
		final List< Pair< String, String > > keyValues = readKeyValuePairs( input );
		
		return null;
	}
	
	private static List< Pair< String, String > > readKeyValuePairs( final Reader input )
	{
		// read the stream into a string buffer, which we'll process into key-value pairs
		final StringBuffer buffer = new StringBuffer();
		char[] charBuffer = null;
		try
		{
			int charsRead;
			do
			{
				charBuffer = new char[ 1024 ];
				charsRead = input.read( charBuffer );
				buffer.append( charBuffer );
			}
			while( charsRead != -1 );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to read from input stream." + Constants.NEW_LINE );
			message.append( "  Characters read before failure:" + Constants.NEW_LINE );
			message.append( buffer.toString() + Constants.NEW_LINE );
			message.append( "  Characters in char buffer before failure:" + Constants.NEW_LINE );
			message.append( charBuffer );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		
		// create a list of lines
		final List< String > lines = Arrays.asList( buffer.toString().split( "\\n" ) );
		
		// separate the lines into keys and values
		final List< Pair< String, String > > pairs = new ArrayList<>();
		for( String line : lines )
		{
			// if the line is empty, or full of only spaces, then we disregard it.
			if( !line.trim().isEmpty() )
			{
				final String[] keyValue = line.split( "=" );
				pairs.add( new Pair< String, String >( keyValue[ 0 ].trim(), keyValue[ 1 ].trim() ) );
			}
		}
		
		return pairs;
	}

	public static void main( String[] args ) throws FileNotFoundException
	{
		DOMConfigurator.configure( "log4j.xml" );
		
		final KeyValueReader reader = new KeyValueReader();
//		reader.setRemoveEmptyTextNodes( false );
		final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.txt" ) );
		final Reader input = new InputStreamReader( inputStream );
		final InfoNode infoNode = reader.read( Division.class, input );
		System.out.println( infoNode.simpleTreeToString() );
		
		final PersistenceEngine engine = new PersistenceEngine();
		final Object reperson = engine.parseSemanticModel( Division.class, infoNode );
		System.out.println( reperson );
	}
}
