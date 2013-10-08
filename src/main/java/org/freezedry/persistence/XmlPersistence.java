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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.readers.XmlReader;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.freezedry.persistence.writers.XmlWriter;

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
	
	/*
	 * Creates the {@link XmlWriter} if it hasn't yet been instantiated, and returns it
	 * @return the {@link XmlWriter}
	 */
	protected XmlWriter getPersistenceWriter()
	{
		if( xmlWriter == null )
		{
			xmlWriter = new XmlWriter();
			xmlWriter.setDisplayTypeInfo( isDisplayTypeInfo );
		}
		return xmlWriter;
	}
	
	/*
	 * Creates the {@link XmlReader} if it hasn't yet been instantiated, and returns it
	 * @return the {@link XmlReader}
	 */
	protected XmlReader getPersistenceReader()
	{
		if( xmlReader == null )
		{
			xmlReader = new XmlReader();
		}
		return xmlReader;
	}
	
	public static void main( String[] args )
	{
		try
		{
			DOMConfigurator.configure( "log4j.xml" );
			
			XmlPersistence persist = new XmlPersistence();
			persist.setDisplayTypeInfo( true );
			persist.write( "this is a test", "test.xml" );
//			persist.write( new Double( 3.14 ), "test.xml" );
//			final List< Double > list = new ArrayList<>( Arrays.asList( 3.14, 2.7, 111.11 ) );
//			persist.write( list, "test.xml" );
			
			System.out.println( "Read: " + persist.read( String.class, "test.xml" ) );
//			System.out.println( "Read: " + persist.read( Double.class, "test.xml" ) );
//			final List< Double > relist = persist.read( ArrayList.class, "test.xml" );
//			for( Double item : relist )
//			{
//				System.out.println( "Read: " + item );
//			}
			System.exit( 0 );
	
			// create the object to persist
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
	
			int[][] arrayMatrix = { { 11, 12, 13 }, { 21, 22, 23 }, { 31, 32, 33 } };
			division.setArrayMatrix( arrayMatrix );
			
			List< List< Integer > > collectionMatrix = new ArrayList<>();
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 11, 12, 13 ) ) );
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 21, 22, 23 ) ) );
			collectionMatrix.add( new ArrayList< Integer >( Arrays.asList( 31, 32, 33 ) ) );
			division.setCollectionMatrix( collectionMatrix );
			
			Map< String, Person > personMap = new LinkedHashMap<>();
			personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
			personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
			personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
			division.setPersonMap( personMap );
			
			final XmlPersistence persistence = new XmlPersistence();
			persistence.setDisplayTypeInfo( true );
			persistence.write( division, "person.xml" );
			// two ways to override the output behavior programmatically
			// override the default date setting and replace the date node builder with the new one
//			final PersistenceEngine engine = persistence.getPersistenceEngine();
//			final DateNodeBuilder dateNodeBuilder = new DateNodeBuilder( engine, "yyyy-MM-dd" );
//			engine.addNodeBuilder( Calendar.class, dateNodeBuilder );
			// -- OR --
//			((DateNodeBuilder)engine.getNodeBuilder( Calendar.class )).setOutputDateFormat( "yyyy-MM-dd" );
			
			final Division redivision = persistence.read( Division.class, "person.xml" );
			System.out.println( redivision.toString() );
			
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
