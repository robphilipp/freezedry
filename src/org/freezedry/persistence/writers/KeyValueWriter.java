package org.freezedry.persistence.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.DateUtils;

public class KeyValueWriter implements PersistenceWriter {

	private static final Logger LOGGER = Logger.getLogger( KeyValueWriter.class );
	
	private KeyValueBuilder builder;
	
	/**
	 * 
	 */
	public KeyValueWriter( final Map< Class< ? >, PersistenceRenderer > renderers, 
						   final PersistenceRenderer arrayRenderer,
						   final String separator )
	{
		builder = new KeyValueBuilder( renderers, arrayRenderer, separator );
	}

	/**
	 * 
	 */
	public KeyValueWriter( final String separator )
	{
		builder = new KeyValueBuilder( separator );
	}

	/**
	 * 
	 */
	public KeyValueWriter()
	{
		builder = new KeyValueBuilder();
	}
	
	public void setSeparator( final String separator )
	{
		builder.setSeparator( separator );
	}
	
	public String getSeparator()
	{
		return builder.getSeparator();
	}
	
	public void setShowFullKey( final boolean isShowFullKey )
	{
		builder.setShowFullKey( isShowFullKey );
	}
	
	public boolean isShowFullKey()
	{
		return builder.isShowFullKey();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.writers.PersistenceWriter#write(org.freezedry.persistence.tree.InfoNode, java.io.Writer)
	 */
	@Override
	public void write( final InfoNode rootNode, final Writer output )
	{
		final List< Pair< String, Object > > keyValuePairs = builder.buildKeyValuePairs( rootNode );
		try
		{
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				final String str = pair.getFirst() + " = " + pair.getSecond().toString() + Constants.NEW_LINE;
				output.write( str );
			}
		}
		catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
		
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			for( final Pair< String, Object > pair : keyValuePairs )
			{
				message.append( pair.getFirst() + " = " + pair.getSecond().toString() + Constants.NEW_LINE );
			}
			LOGGER.info( message.toString() );
		}
	}
	
	/**
	 * For testing
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static void main( String[] args ) throws ParserConfigurationException, ReflectiveOperationException, IOException, ParseException
	{
		DOMConfigurator.configure( "log4j.xml" );

		final Division division = new Division();
		final Person johnny = new Person( "Hernandez", "Johnny", 13 );
		johnny.addFriend( "Polly", "bird" );
		johnny.addFriend( "Sparky", "dog" );
		for( int i = 0; i < 10; ++i )
		{
			johnny.addMood( Math.sin( Math.PI / 4 * i ) );
		}
		Map< String, String > group = new LinkedHashMap<>();
		group.put( "one", "ONE" );
		group.put( "two", "TWO" );
		group.put( "three", "THREE" );
		johnny.addGroup( "numbers", group );

		group = new LinkedHashMap<>();
		group.put( "a", "AY" );
		group.put( "b", "BEE" );
		johnny.addGroup( "letters", group );
		
		johnny.setBirthdate( DateUtils.createDateFromString( "1963-04-22", "yyyy-MM-dd" ) );
		
		division.addPerson( johnny );

		division.addPerson( new Person( "Prosky", "Julie", 15 ) );
		division.addPerson( new Person( "Jones", "Janet", 13 ) );
		division.addPerson( new Person( "Ghad", "Booda", 17 ) );
		
		division.addMonth( "January", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
		division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 2, 3, 28 ) ) );
		division.addMonth( "March", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
		division.addMonth( "April", new HashSet<>( Arrays.asList( 1, 2, 3, 30 ) ) );
		
		division.setCarNames( new String[] { "civic", "tsx", "accord" } );
		
		
		final PersistenceEngine engine = new PersistenceEngine();
		final InfoNode rootNode = engine.createSemanticModel( division );
		System.out.println( rootNode.treeToString() );

		try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "person.txt" ) ) )
		{
			final KeyValueWriter writer = new KeyValueWriter();
//			writer.setShowFullKey( true );
			writer.setSeparator( "." );
			writer.write( rootNode, printWriter );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
