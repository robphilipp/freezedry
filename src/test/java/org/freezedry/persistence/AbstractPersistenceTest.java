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

import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.Constants;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * [Description]
 *
 * @author rob
 *         10/12/13 4:19 PM
 */
public abstract class AbstractPersistenceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractPersistenceTest.class );

	protected final static String OUTPUT_DIR = "src/test/output/";
	protected Division division;

	@Before
	public void setUp() throws Exception
	{
		try
		{
			// make sure the directory exists, if it doesn't, then create it
			final File file = new File( OUTPUT_DIR );
			if( !file.exists() )
			{
				final boolean isCreated = file.mkdirs();
				if( !isCreated )
				{
					final StringBuilder error = new StringBuilder();
					error.append( "Test output directory doesn't exist, and unable to create the directory: " ).append( Constants.NEW_LINE )
							.append( "  Directory: " ).append( file.toString() ).append( Constants.NEW_LINE );
					throw new IOException( error.toString() );
				}
				LOGGER.info( "Create directory: " + file.toString() );
			}
			LOGGER.info( "Test output directory: " + file.toString() );

			// create the object to persist
			division = new Division();
			final Person johnny = new Person( "Hernandez", "Johnny", 13 );
			johnny.addFriend( "Polly (A)", "bird" );
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

			List<List< Integer >> collectionMatrix = new ArrayList<>();
			collectionMatrix.add( new ArrayList<>( Arrays.asList( 11, 12, 13 ) ) );
			collectionMatrix.add( new ArrayList<>( Arrays.asList( 21, 22, 23 ) ) );
			collectionMatrix.add( new ArrayList<>( Arrays.asList( 31, 32, 33 ) ) );
			division.setCollectionMatrix( collectionMatrix );

			Map< String, Person > personMap = new LinkedHashMap<>();
			personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
			personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
			personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
			division.setPersonMap( personMap );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
