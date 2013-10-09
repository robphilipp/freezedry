package org.freezedry.persistence.writers;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.tree.InfoNode;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 8:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonWriterTest {
	@Before
	public void setUp() throws Exception
	{
		try
		{
			DOMConfigurator.configure( "log4j.xml" );

			final Division division = new Division();
			final Person johnny = new Person( "Hernandez", "Johnny", 13 );
//			johnny.addFriend( "Polly", "bird" );
//			johnny.addFriend( "Sparky", "dog" );
//			for( int i = 0; i < 10; ++i )
//			{
//				johnny.addMood( Math.sin( Math.PI / 4 * i ) );
//			}
//			Map< String, String > group = new LinkedHashMap<>();
//			group.put( "one", "ONE" );
//			group.put( "two", "TWO" );
//			group.put( "three", "THREE" );
//			johnny.addGroup( "numbers", group );
//
//			group = new LinkedHashMap<>();
//			group.put( "a", "AY" );
//			group.put( "b", "BEE" );
//			johnny.addGroup( "letters", group );
//
//			johnny.setBirthdate( DateUtils.createDateFromString( "1963-04-22", "yyyy-MM-dd" ) );
//
			division.addPerson( johnny );


			division.addPerson( new Person( "Prosky", "Julie", 15 ) );
			division.addPerson( new Person( "Jones", "Janet", 13 ) );
			division.addPerson( new Person( "Ghad", "Booda", 17 ) );

//			division.addMonth( "January", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//			division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 2, 3, 28 ) ) );
//			division.addMonth( "March", new HashSet<>( Arrays.asList( 1, 2, 3, 31 ) ) );
//			division.addMonth( "April", new HashSet<>( Arrays.asList( 1, 2, 3, 30 ) ) );

			final PersistenceEngine engine = new PersistenceEngine();

//			final InfoNode rootNode = engine.createSemanticModel( division );
			final int[] test = new int[] { 3, 1, 4, 1, 5, 9, 2, 6, 5, 3 };
//			final int[][] test = new int[][] { { 3, 1 }, { 4, 1 }, { 5, 9 }, { 2, 6 }, { 5, 3 } };
			final InfoNode rootNode = engine.createSemanticModel( test );
			System.out.println( rootNode.treeToString() );


			try( final PrintWriter printWriter = new PrintWriter( new FileWriter( "test.json" ) ) )
			{
				final JsonWriter writer = new JsonWriter();
				writer.write( rootNode, printWriter );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	@Test
	public void testWrite() throws Exception
	{

	}
}
