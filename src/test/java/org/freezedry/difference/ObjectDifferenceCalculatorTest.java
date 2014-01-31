package org.freezedry.difference;

import junit.framework.Assert;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

public class ObjectDifferenceCalculatorTest {

	private Division division1;
	private Division division2;

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
		final Map< String, ObjectDifferenceCalculator.Difference> differences = diffCalc.calculateDifference( division1, division2 );
		Assert.assertEquals( "Number of Differences", differences.size(), 8 );
		final ObjectDifferenceCalculator.Difference age = differences.get( "Division.people[0].Person.age" );
		Assert.assertEquals( "Division.people[0].Person.age (modified)", age.getObject(), "13" );
		Assert.assertEquals( "Division.people[0].Person.age (reference)", age.getReferenceObject(), "37" );
		final ObjectDifferenceCalculator.Difference dob = differences.get( "Division.people[0].Person.birthDate" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (modified)", dob.getObject(), "1963-04-22" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (reference)", dob.getReferenceObject(), "2014-01-28" );

		ObjectDifferenceCalculator.Difference col = differences.get( "Division.collectionMatrix[0][0]" );
		Assert.assertEquals( "Divisoin.collectionMatrix[0][0] (modified)" , col.getObject(), "11" );
		Assert.assertEquals( "Divisoin.collectionMatrix[0][0] (reference)" , col.getReferenceObject(), "21" );
		col = differences.get( "Division.collectionMatrix[0][1]" );
		Assert.assertEquals( "Divisoin.collectionMatrix[0][1] (modified)" , col.getObject(), "12" );
		Assert.assertEquals( "Divisoin.collectionMatrix[0][1] (reference)" , col.getReferenceObject(), "23" );
		col = differences.get( "Division.collectionMatrix[0][2]" );
		Assert.assertEquals( "Divisoin.collectionMatrix[0][2] (modified)" , col.getObject(), "13" );
		Assert.assertEquals( "Divisoin.collectionMatrix[0][2] (reference)" , col.getReferenceObject(), "22" );
		col = differences.get( "Division.collectionMatrix[1][0]" );
		Assert.assertEquals( "Divisoin.collectionMatrix[1][0] (modified)" , col.getObject(), "21" );
		Assert.assertEquals( "Divisoin.collectionMatrix[1][0] (reference)" , col.getReferenceObject(), "12" );
		col = differences.get( "Division.collectionMatrix[1][1]" );
		Assert.assertEquals( "Divisoin.collectionMatrix[1][1] (modified)" , col.getObject(), "22" );
		Assert.assertEquals( "Divisoin.collectionMatrix[1][1] (reference)" , col.getReferenceObject(), "11" );
		col = differences.get( "Division.collectionMatrix[1][2]" );
		Assert.assertEquals( "Divisoin.collectionMatrix[1][2] (modified)" , col.getObject(), "23" );
		Assert.assertEquals( "Divisoin.collectionMatrix[1][2] (reference)" , col.getReferenceObject(), "13" );

	}

	@Test
	public void testCalculateDifferenceListOrderIgnored() throws Exception
	{
		final ObjectDifferenceCalculator diffCalc = new ObjectDifferenceCalculator( "." ).listOrderIgnored();
		final Map< String, ObjectDifferenceCalculator.Difference> differences = diffCalc.calculateDifference( division1, division2 );
		Assert.assertEquals( "Number of Differences", differences.size(), 2 );
//		Assert.assertEquals( "Number of Differences", differences.size(), 8 );
		final ObjectDifferenceCalculator.Difference age = differences.get( "Division.people[0].Person.age" );
		Assert.assertEquals( "Division.people[0].Person.age (modified)", age.getObject(), "13" );
		Assert.assertEquals( "Division.people[0].Person.age (reference)", age.getReferenceObject(), "37" );
		final ObjectDifferenceCalculator.Difference dob = differences.get( "Division.people[0].Person.birthDate" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (modified)", dob.getObject(), "1963-04-22" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (reference)", dob.getReferenceObject(), "2014-01-28" );
	}

	@Test
	public void testFlattenObject() throws Exception
	{
		final ObjectDifferenceCalculator diffCalc = new ObjectDifferenceCalculator( "." );
		final Map< String, Object > flattened = diffCalc.flattenObject( division1 );

		final Map< String, Object > expectedKeys = new HashMap<>();
		expectedKeys.put( "Division.people[0].Person.givenName", "\"Johnny\"" );
		expectedKeys.put( "Division.people[0].Person.familyName", "\"Hernandez\"" );
		expectedKeys.put( "Division.people[0].Person.age", "13" );
		expectedKeys.put( "Division.people[0].Person.birthDate", "1963-04-22" );
		expectedKeys.put( "Division.people[0].Person.Mood[0]", "0.0" );
		expectedKeys.put( "Division.people[0].Person.Mood[1]", "0.7071067811865475" );
		expectedKeys.put( "Division.people[0].Person.Mood[2]", "1.0" );
		expectedKeys.put( "Division.people[0].Person.Mood[3]", "0.7071067811865476" );
		expectedKeys.put( "Division.people[0].Person.Mood[4]", "1.2246467991473532E-16" );
		expectedKeys.put( "Division.people[0].Person.Mood[5]", "-0.7071067811865475" );
		expectedKeys.put( "Division.people[0].Person.Mood[6]", "-1.0" );
		expectedKeys.put( "Division.people[0].Person.Mood[7]", "-0.7071067811865477" );
		expectedKeys.put( "Division.people[0].Person.Mood[8]", "-2.4492935982947064E-16" );
		expectedKeys.put( "Division.people[0].Person.Mood[9]", "0.7071067811865474" );
		expectedKeys.put( "Division.people[0].Person.friends{\"Polly\"}", "\"bird\"" );
		expectedKeys.put( "Division.people[0].Person.friends{\"Sparky\"}", "\"dog\"" );
		expectedKeys.put( "Division.people[0].Person.groups{\"numbers\"}{\"one\"}", "\"ONE\"" );
		expectedKeys.put( "Division.people[0].Person.groups{\"numbers\"}{\"two\"}", "\"TWO\"" );
		expectedKeys.put( "Division.people[0].Person.groups{\"numbers\"}{\"three\"}", "\"THREE\"" );
		expectedKeys.put( "Division.people[0].Person.groups{\"letters\"}{\"a\"}", "\"AY\"" );
		expectedKeys.put( "Division.people[0].Person.groups{\"letters\"}{\"b\"}", "\"BEE\"" );
		expectedKeys.put( "Division.people[1].Person.givenName", "\"Julie\"" );
		expectedKeys.put( "Division.people[1].Person.familyName", "\"Prosky\"" );
		expectedKeys.put( "Division.people[1].Person.age", "15" );
		expectedKeys.put( "Division.people[1].Person.birthDate", null );
		expectedKeys.put( "Division.people[1].Person.mood", null );
		expectedKeys.put( "Division.people[1].Person.friends", null );
		expectedKeys.put( "Division.people[1].Person.groups", null );
		expectedKeys.put( "Division.people[2].Person.givenName", "\"Janet\"" );
		expectedKeys.put( "Division.people[2].Person.familyName", "\"Jones\"" );
		expectedKeys.put( "Division.people[2].Person.age", "13" );
		expectedKeys.put( "Division.people[2].Person.birthDate", null );
		expectedKeys.put( "Division.people[2].Person.mood", null );
		expectedKeys.put( "Division.people[2].Person.friends", null );
		expectedKeys.put( "Division.people[2].Person.groups", null );
		expectedKeys.put( "Division.people[3].Person.givenName", "\"Booda\"" );
		expectedKeys.put( "Division.people[3].Person.familyName", "\"Ghad\"" );
		expectedKeys.put( "Division.people[3].Person.age", "17" );
		expectedKeys.put( "Division.people[3].Person.birthDate", null );
		expectedKeys.put( "Division.people[3].Person.mood", null );
		expectedKeys.put( "Division.people[3].Person.friends", null );
		expectedKeys.put( "Division.people[3].Person.groups", null );
		expectedKeys.put( "Division.months{\"January\"}[0]", "1" );
		expectedKeys.put( "Division.months{\"January\"}[1]", "2" );
		expectedKeys.put( "Division.months{\"January\"}[2]", "3" );
		expectedKeys.put( "Division.months{\"January\"}[3]", "31" );
		expectedKeys.put( "Division.months{\"April\"}[0]", "1" );
		expectedKeys.put( "Division.months{\"April\"}[1]", "2" );
		expectedKeys.put( "Division.months{\"April\"}[2]", "3" );
		expectedKeys.put( "Division.months{\"April\"}[3]", "30" );
		expectedKeys.put( "Division.months{\"February\"}[0]", "1" );
		expectedKeys.put( "Division.months{\"February\"}[1]", "2" );
		expectedKeys.put( "Division.months{\"February\"}[2]", "3" );
		expectedKeys.put( "Division.months{\"February\"}[3]", "28" );
		expectedKeys.put( "Division.months{\"March\"}[0]", "1" );
		expectedKeys.put( "Division.months{\"March\"}[1]", "2" );
		expectedKeys.put( "Division.months{\"March\"}[2]", "3" );
		expectedKeys.put( "Division.months{\"March\"}[3]", "31" );
		expectedKeys.put( "Division.carNames[0]", "\"civic\"" );
		expectedKeys.put( "Division.carNames[1]", "\"tsx\"" );
		expectedKeys.put( "Division.carNames[2]", "\"accord\"" );
		expectedKeys.put( "Division.arrayMatrix[0][0]", "11" );
		expectedKeys.put( "Division.arrayMatrix[0][1]", "12" );
		expectedKeys.put( "Division.arrayMatrix[0][2]", "13" );
		expectedKeys.put( "Division.arrayMatrix[1][0]", "21" );
		expectedKeys.put( "Division.arrayMatrix[1][1]", "22" );
		expectedKeys.put( "Division.arrayMatrix[1][2]", "23" );
		expectedKeys.put( "Division.arrayMatrix[2][0]", "31" );
		expectedKeys.put( "Division.arrayMatrix[2][1]", "32" );
		expectedKeys.put( "Division.arrayMatrix[2][2]", "33" );
		expectedKeys.put( "Division.collectionMatrix[0][0]", "11" );
		expectedKeys.put( "Division.collectionMatrix[0][1]", "12" );
		expectedKeys.put( "Division.collectionMatrix[0][2]", "13" );
		expectedKeys.put( "Division.collectionMatrix[1][0]", "21" );
		expectedKeys.put( "Division.collectionMatrix[1][1]", "22" );
		expectedKeys.put( "Division.collectionMatrix[1][2]", "23" );
		expectedKeys.put( "Division.collectionMatrix[2][0]", "31" );
		expectedKeys.put( "Division.collectionMatrix[2][1]", "32" );
		expectedKeys.put( "Division.collectionMatrix[2][2]", "33" );
		expectedKeys.put( "Division.personMap{\"funny\"}.givenName", "\"Pryor\"" );
		expectedKeys.put( "Division.personMap{\"funny\"}.familyName", "\"Richard\"" );
		expectedKeys.put( "Division.personMap{\"funny\"}.age", "63" );
		expectedKeys.put( "Division.personMap{\"funny\"}.birthDate", null );
		expectedKeys.put( "Division.personMap{\"funny\"}.mood", null );
		expectedKeys.put( "Division.personMap{\"funny\"}.friends", null );
		expectedKeys.put( "Division.personMap{\"funny\"}.groups", null );
		expectedKeys.put( "Division.personMap{\"sad\"}.givenName", "\"Jones\"" );
		expectedKeys.put( "Division.personMap{\"sad\"}.familyName", "\"Jenny\"" );
		expectedKeys.put( "Division.personMap{\"sad\"}.age", "45" );
		expectedKeys.put( "Division.personMap{\"sad\"}.birthDate", null );
		expectedKeys.put( "Division.personMap{\"sad\"}.mood", null );
		expectedKeys.put( "Division.personMap{\"sad\"}.friends", null );
		expectedKeys.put( "Division.personMap{\"sad\"}.groups", null );
		expectedKeys.put( "Division.personMap{\"pretty\"}.givenName", "\"Mendez\"" );
		expectedKeys.put( "Division.personMap{\"pretty\"}.familyName", "\"Ginder\"" );
		expectedKeys.put( "Division.personMap{\"pretty\"}.age", "23" );
		expectedKeys.put( "Division.personMap{\"pretty\"}.birthDate", null );
		expectedKeys.put( "Division.personMap{\"pretty\"}.mood", null );
		expectedKeys.put( "Division.personMap{\"pretty\"}.friends", null );
		expectedKeys.put( "Division.personMap{\"pretty\"}.groups", null );
		expectedKeys.put( "Division.listOfMaps[0]{\"color\"}", "\"green\"" );
		expectedKeys.put( "Division.listOfMaps[0]{\"size\"}", "\"large\"" );
		expectedKeys.put( "Division.listOfMaps[0]{\"condition\"}", "\"used\"" );
		expectedKeys.put( "Division.listOfMaps[1]{\"color\"}", "\"red\"" );
		expectedKeys.put( "Division.listOfMaps[1]{\"size\"}", "\"small\"" );
		expectedKeys.put( "Division.listOfMaps[1]{\"condition\"}", "\"new\"" );
		expectedKeys.put( "Division.listOfMaps[2]{\"color\"}", "\"blue\"" );
		expectedKeys.put( "Division.listOfMaps[2]{\"size\"}", "\"medium\"" );
		expectedKeys.put( "Division.listOfMaps[2]{\"condition\"}", "\"good\"" );
		expectedKeys.put( "Division.crazySet[0]", "\"hypochondria\"" );
		expectedKeys.put( "Division.crazySet[1]", "\"delusions\"" );
		expectedKeys.put( "Division.crazySet[2]", "\"manic-depression\"" );

		// used to generate the expect results when needed
//		for( Map.Entry< String, Object > entry : flattened.entrySet() )
//		{
//			final StringBuilder expression = new StringBuilder( "expectedKeys.put( \"" )
//					.append( entry.getKey().replaceAll( "\\\"", "\\\\\"" ) )
//					.append( "\", " );
//			final Object object = entry.getValue();
//			if( object == null )
//			{
//				expression.append( "null" );
//			}
//			else
//			{
//				expression.append( "\"" )
//						.append( object.toString().replaceAll( "\\\"", "\\\\\"" ) )
//						.append( "\"" );
//			}
//			expression.append( " );" );
//			System.out.println( expression.toString() );
//		}

		Assert.assertEquals( "Flattened Division", expectedKeys, flattened );

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

		division.addToCrazySet( "hypochondria" );
		division.addToCrazySet( "delusions" );
		division.addToCrazySet( "manic-depression" );

		return division;
	}

	private static Division createDivisionTwo() throws ParseException
	{
		final Division division = createDivisionOne();
		final Person johnny = division.getPerson( "Hernandez", "Johnny" );
		johnny.setAge( 37 );
		johnny.setBirthdate( DateUtils.createDateFromString( "2014-01-28", "yyyy-MM-dd" ) );

		List<List< Integer >> collectionMatrix = Arrays.asList(
				Arrays.asList( 21, 23, 22 ),
				Arrays.asList( 12, 11, 13 ),
				Arrays.asList( 31, 32, 33 ) );
		division.setCollectionMatrix( collectionMatrix );

		return division;
	}
}
