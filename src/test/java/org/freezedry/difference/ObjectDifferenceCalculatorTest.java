package org.freezedry.difference;

import junit.framework.Assert;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.text.ParseException;
import java.util.*;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class ObjectDifferenceCalculatorTest {

	private Division division1;
	private Division division2;

	/**
	 * @return The configuration for the OSGi framework
	 */
	@Configuration
	public Option[] configuration()
	{
		return combine( combine( freezedryBundles(), logging() ), junitBundles(), cleanCaches() );
	}

	/**
	 * @return an array of options containing the core freezedry bundles to be provisioned
	 */
	private Option[] freezedryBundles()
	{
		return new Option[] {
			mavenBundle( "com.closure-sys", "freezedry" ),
			mavenBundle( "org.apache.geronimo.bundles", "json", "20090211_1" ),
			mavenBundle( "org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-io", "1.4_3" )
		};
	}

	/**
	 * @return an array of options containing the core logging bundles to be provisioned
	 */
	private Option[] logging()
	{
		return new Option[] {
				mavenBundle( "org.slf4j", "slf4j-api", "1.7.5" ),
				mavenBundle( "ch.qos.logback", "logback-core", "1.0.13" ),
				mavenBundle( "ch.qos.logback", "logback-classic", "1.0.13" )
		};
	}

	@Before
	public void init()
	{
		try
		{
			division1 = createDivisionOne();
			division2 = createDivisionTwo();
		}
		catch( ParseException e )
		{
			Assert.fail();
		}
	}

	@Test
	public void testCalculateDifference() throws Exception
	{
		final ObjectDifferenceCalculator diffCalc = new ObjectDifferenceCalculator( "." );
		final Map< String, ObjectDifferenceCalculator.Difference> difference = diffCalc.calculateDifference( division1, division2 );
		Assert.assertTrue( difference.size() == 2 );
	}

	@Test
	public void testFlattenObject() throws Exception
	{

	}

	private static Division createDivisionOne() throws ParseException
	{
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

		List<List< Integer >> collectionMatrix = Arrays.asList(
				Arrays.asList( 11, 12, 13 ),
				Arrays.asList( 21, 22, 23 ),
				Arrays.asList( 31, 32, 33 ) );
		division.setCollectionMatrix( collectionMatrix );

		List< Map< String, String > > listOfMaps = new ArrayList<>();
		Map< String, String > map = new LinkedHashMap<>();
		map.put( "color", "green" );
		map.put( "size", "large" );
		map.put( "condition", "used" );
		listOfMaps.add( map );

		map = new LinkedHashMap<>();
		map.put( "color", "red" );
		map.put( "size", "small" );
		map.put( "condition", "new" );
		listOfMaps.add( map );

		map = new LinkedHashMap<>();
		map.put( "color", "blue" );
		map.put( "size", "medium" );
		map.put( "condition", "good" );
		listOfMaps.add( map );
		division.setListOfMaps( listOfMaps );

		Map< String, Person > personMap = new LinkedHashMap<>();
		personMap.put( "funny", new Person( "Richard", "Pryor", 63 ) );
		personMap.put( "sad", new Person( "Jenny", "Jones", 45 ) );
		personMap.put( "pretty", new Person( "Ginder", "Mendez", 23 ) );
		division.setPersonMap( personMap );

		return division;
	}

	private static Division createDivisionTwo() throws ParseException
	{
		final Division division = createDivisionOne();
		final Person johnny = division.getPerson( "Hernandez", "Johnny" );
		johnny.setAge( 37 );
		johnny.setBirthdate( Calendar.getInstance() );
		return division;
	}
}
