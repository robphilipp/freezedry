package org.freezedry.difference;

import junit.framework.Assert;
import org.freezedry.persistence.tests.BadPerson;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

public class ObjectDifferenceCalculatorTest {

	private Division referenceObject;
	private Division modifiedObject;

	@Before
	public void init()
	{
		try
		{
			referenceObject = createDivisionOne();
			modifiedObject = createDivisionTwo();
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
		final Map< String, ObjectDifferenceCalculator.Difference> differences = diffCalc.calculateDifference( modifiedObject, referenceObject );
		Assert.assertEquals( "Number of Differences", 19, differences.size() );
		final ObjectDifferenceCalculator.Difference age = differences.get( "Division.people[0].Person.age" );
		Assert.assertEquals( "Division.people[0].Person.age (reference.)", age.getReferenceObject(), "13" );
		Assert.assertEquals( "Division.people[0].Person.age (modified)", age.getObject(), "37" );
		final ObjectDifferenceCalculator.Difference dob = differences.get( "Division.people[0].Person.birthDate" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (reference.)", dob.getReferenceObject(), "1963-04-22" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (modified)", dob.getObject(), "2014-01-28" );

		// changed the order of the collection matrix
		ObjectDifferenceCalculator.Difference col = differences.get( "Division.collectionMatrix[0][0]" );
		Assert.assertEquals( "Division.collectionMatrix[0][0] (reference.)" , col.getReferenceObject(), "11" );
		Assert.assertEquals( "Division.collectionMatrix[0][0] (modified)" , col.getObject(), "21" );
		col = differences.get( "Division.collectionMatrix[0][1]" );
		Assert.assertEquals( "Division.collectionMatrix[0][1] (reference.)" , col.getReferenceObject(), "12" );
		Assert.assertEquals( "Division.collectionMatrix[0][1] (modified)" , col.getObject(), "23" );
		col = differences.get( "Division.collectionMatrix[0][2]" );
		Assert.assertEquals( "Division.collectionMatrix[0][2] (reference.)" , col.getReferenceObject(), "13" );
		Assert.assertEquals( "Division.collectionMatrix[0][2] (modified)" , col.getObject(), "22" );
		col = differences.get( "Division.collectionMatrix[1][0]" );
		Assert.assertEquals( "Division.collectionMatrix[1][0] (reference.)" , col.getReferenceObject(), "21" );
		Assert.assertEquals( "Division.collectionMatrix[1][0] (modified)" , col.getObject(), "12" );
		col = differences.get( "Division.collectionMatrix[1][1]" );
		Assert.assertEquals( "Division.collectionMatrix[1][1] (reference.)" , col.getReferenceObject(), "22" );
		Assert.assertEquals( "Division.collectionMatrix[1][1] (modified)" , col.getObject(), "11" );
		col = differences.get( "Division.collectionMatrix[1][2]" );
		Assert.assertEquals( "Division.collectionMatrix[1][2] (reference.)" , col.getReferenceObject(), "23" );
		Assert.assertEquals( "Division.collectionMatrix[1][2] (modified)" , col.getObject(), "13" );

		// changed the order of the evil doings
		col = differences.get( "Division.people[4].BadPerson.evilDoings[0]" );
		Assert.assertEquals( "Division.people[4].BadPerson.evilDoings[0] (reference.)" , col.getReferenceObject(), "\"wasted a beer\"" );
		Assert.assertEquals( "Division.people[4].BadPerson.evilDoings[0] (modified)" , col.getObject(), "\"disliked technology\"" );
		col = differences.get( "Division.people[4].BadPerson.evilDoings[1]" );
		Assert.assertEquals( "Division.people[4].BadPerson.evilDoings[1] (reference.)" , col.getReferenceObject(), "\"disliked technology\"" );
		Assert.assertEquals( "Division.people[4].BadPerson.evilDoings[1] (modified)" , col.getObject(), "\"added sugar to coffee\"" );
		col = differences.get( "Division.people[4].BadPerson.evilDoings[3]" );
		Assert.assertEquals( "Division.people[4].BadPerson.evilDoings[3] (reference.)" , col.getReferenceObject(), "\"added sugar to coffee\"" );
		Assert.assertEquals( "Division.people[4].BadPerson.evilDoings[3] (modified)", col.getObject(), "\"wasted a beer\"" );

		// changed the order of the lists on the leaf nodes (lowest dimension)
		col = differences.get( "Division.threeD[0][0][1]" );
		Assert.assertEquals( "Division.threeD[0][0][1] (reference.)" , col.getReferenceObject(), "1" );
		Assert.assertEquals( "Division.threeD[0][0][1] (modified)" , col.getObject(), "2" );
		col = differences.get( "Division.threeD[0][0][2]" );
		Assert.assertEquals( "Division.threeD[0][0][2] (reference.)" , col.getReferenceObject(), "2" );
		Assert.assertEquals( "Division.threeD[0][0][2] (modified)" , col.getObject(), "1" );

		// changed the order of the 2nd dimension of the lists
		col = differences.get( "Division.threeD[0][1][0]" );
		Assert.assertEquals( "Division.threeD[0][1][0] (reference.)" , col.getReferenceObject(), "10" );
		Assert.assertEquals( "Division.threeD[0][1][0] (modified)" , col.getObject(), "21" );
		col = differences.get( "Division.threeD[0][1][1]" );
		Assert.assertEquals( "Division.threeD[0][1][1] (reference.)" , col.getReferenceObject(), "11" );
		Assert.assertEquals( "Division.threeD[0][1][1] (modified)" , col.getObject(), "20" );
		col = differences.get( "Division.threeD[0][1][2]" );
		Assert.assertEquals( "Division.threeD[0][1][2] (reference.)" , col.getReferenceObject(), "12" );
		Assert.assertEquals( "Division.threeD[0][1][2] (modified)" , col.getObject(), "22" );

		col = differences.get( "Division.threeD[0][2][0]" );
		Assert.assertEquals( "Division.threeD[0][2][0] (reference.)" , col.getReferenceObject(), "20" );
		Assert.assertEquals( "Division.threeD[0][2][0] (modified)" , col.getObject(), "12" );
		col = differences.get( "Division.threeD[0][2][1]" );
		Assert.assertEquals( "Division.threeD[0][2][1] (reference.)" , col.getReferenceObject(), "21" );
		Assert.assertEquals( "Division.threeD[0][2][1] (modified)" , col.getObject(), "11" );
		col = differences.get( "Division.threeD[0][2][2]" );
		Assert.assertEquals( "Division.threeD[0][2][2] (reference.)" , col.getReferenceObject(), "22" );
		Assert.assertEquals( "Division.threeD[0][2][2] (modified)" , col.getObject(), "10" );
	}

	@Test
	public void testCalculateDifferenceListOrderIgnored() throws Exception
	{
		final ObjectDifferenceCalculator diffCalc = new ObjectDifferenceCalculator( "." ).listOrderIgnored();
		final Map< String, ObjectDifferenceCalculator.Difference> differences = diffCalc.calculateDifference( modifiedObject, referenceObject );
		Assert.assertEquals( "Number of Differences", 2, differences.size() );
		final ObjectDifferenceCalculator.Difference age = differences.get( "Division.people[0].Person.age" );
		Assert.assertEquals( "Division.people[0].Person.age (reference)", age.getReferenceObject(), "13" );
		Assert.assertEquals( "Division.people[0].Person.age (modified)", age.getObject(), "37" );
		final ObjectDifferenceCalculator.Difference dob = differences.get( "Division.people[0].Person.birthDate" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (reference)", dob.getReferenceObject(), "1963-04-22" );
		Assert.assertEquals( "Division.people[0].Person.birthDate (modified)", dob.getObject(), "2014-01-28" );
	}

	@Test
	public void testFlattenObject() throws Exception
	{
		final ObjectDifferenceCalculator diffCalc = new ObjectDifferenceCalculator( "." );
		final Map< String, Object > flattened = diffCalc.flattenObject( referenceObject );

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
		expectedKeys.put( "Division.people[4].BadPerson.evilDoings[0]", "\"wasted a beer\"" );
		expectedKeys.put( "Division.people[4].BadPerson.evilDoings[1]", "\"disliked technology\"" );
		expectedKeys.put( "Division.people[4].BadPerson.evilDoings[2]", "\"added semicolon to someones for-loop\"" );
		expectedKeys.put( "Division.people[4].BadPerson.evilDoings[3]", "\"added sugar to coffee\"" );
		expectedKeys.put( "Division.people[4].BadPerson.givenName", "\"johnny\"" );
		expectedKeys.put( "Division.people[4].BadPerson.familyName", "\"evil\"" );
		expectedKeys.put( "Division.people[4].BadPerson.age", "666" );
		expectedKeys.put( "Division.people[4].BadPerson.birthDate", null );
		expectedKeys.put( "Division.people[4].BadPerson.mood", null );
		expectedKeys.put( "Division.people[4].BadPerson.friends", null );
		expectedKeys.put( "Division.people[4].BadPerson.groups", null );
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
		expectedKeys.put( "Division.threeD[0][0][0]", "0" );
		expectedKeys.put( "Division.threeD[0][0][1]", "1" );
		expectedKeys.put( "Division.threeD[0][0][2]", "2" );
		expectedKeys.put( "Division.threeD[0][1][0]", "10" );
		expectedKeys.put( "Division.threeD[0][1][1]", "11" );
		expectedKeys.put( "Division.threeD[0][1][2]", "12" );
		expectedKeys.put( "Division.threeD[0][2][0]", "20" );
		expectedKeys.put( "Division.threeD[0][2][1]", "21" );
		expectedKeys.put( "Division.threeD[0][2][2]", "22" );
		expectedKeys.put( "Division.threeD[1][0][0]", "100" );
		expectedKeys.put( "Division.threeD[1][0][1]", "101" );
		expectedKeys.put( "Division.threeD[1][0][2]", "102" );
		expectedKeys.put( "Division.threeD[1][1][0]", "110" );
		expectedKeys.put( "Division.threeD[1][1][1]", "111" );
		expectedKeys.put( "Division.threeD[1][1][2]", "112" );
		expectedKeys.put( "Division.threeD[1][2][0]", "120" );
		expectedKeys.put( "Division.threeD[1][2][1]", "121" );
		expectedKeys.put( "Division.threeD[1][2][2]", "122" );
		expectedKeys.put( "Division.threeD[2][0][0]", "200" );
		expectedKeys.put( "Division.threeD[2][0][1]", "201" );
		expectedKeys.put( "Division.threeD[2][0][2]", "202" );
		expectedKeys.put( "Division.threeD[2][1][0]", "210" );
		expectedKeys.put( "Division.threeD[2][1][1]", "211" );
		expectedKeys.put( "Division.threeD[2][1][2]", "212" );
		expectedKeys.put( "Division.threeD[2][2][0]", "220" );
		expectedKeys.put( "Division.threeD[2][2][1]", "221" );
		expectedKeys.put( "Division.threeD[2][2][2]", "222" );

		Assert.assertEquals( "Flattened Division", expectedKeys, flattened );
	}

	/**
	 * Prints the expected fields to the console so that it can be pasted into the flatten-object test. Call this method
	 * when updating the Division class (or classes it contains).
	 */
	public void createExpectedFields()
	{
		final ObjectDifferenceCalculator diffCalc = new ObjectDifferenceCalculator( "." );
		final Map< String, Object > flattened = diffCalc.flattenObject( referenceObject );
		for( Map.Entry< String, Object > entry : flattened.entrySet() )
		{
			final StringBuilder expression = new StringBuilder( "expectedKeys.put( \"" )
					.append( entry.getKey().replaceAll( "\\\"", "\\\\\"" ) )
					.append( "\", " );
			final Object object = entry.getValue();
			if( object == null )
			{
				expression.append( "null" );
			}
			else
			{
				expression.append( "\"" )
						.append( object.toString().replaceAll( "\\\"", "\\\\\"" ) )
						.append( "\"" );
			}
			expression.append( " );" );
			System.out.println( expression.toString() );
		}
	}

	/**
	 * Creates and returns the reference division object
	 * @return The reference division object
	 * @throws ParseException
	 */
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

		final BadPerson evilJohnny = new BadPerson( "evil", "johnny", 666 );
		evilJohnny.addEvilDoing( "wasted a beer" );
		evilJohnny.addEvilDoing( "disliked technology" );
		evilJohnny.addEvilDoing( "added semicolon to someones for-loop" );
		evilJohnny.addEvilDoing( "added sugar to coffee" );
		division.addPerson( evilJohnny );

		final int size = 3;
		final int[][][] threeD = new int[ size ][ size ][ size ];
		for( int i = 0; i < 3; ++i )
		{
			for( int j=0; j < 3; ++j )
			{
				for( int k = 0; k < 3; ++k )
				{
					threeD[ i ][ j ][ k ] = i * 100 + j * 10 + k;
				}
			}
		}
		division.setThreeD( threeD );

		return division;
	}

	/**
	 * Creates the reference division object, modifies it, and then returns it
	 * @return The modified division object
	 * @throws ParseException
	 */
	private static Division createDivisionTwo() throws ParseException
	{
		final Division division = createDivisionOne();
		final Person johnny = division.getPerson( "Hernandez", "Johnny" );
		johnny.setAge( 37 );
		johnny.setBirthdate( DateUtils.createDateFromString( "2014-01-28", "yyyy-MM-dd" ) );

		// changing order of lists effect difference, but not when list order is ignored
		List<List< Integer >> collectionMatrix = Arrays.asList(
				Arrays.asList( 21, 23, 22 ),
				Arrays.asList( 12, 11, 13 ),
				Arrays.asList( 31, 32, 33 ) );
		division.setCollectionMatrix( collectionMatrix );

		// set order should cause a difference
		division.addMonth( "February", new HashSet<>( Arrays.asList( 1, 3, 2, 28 ) ) );
		division.addMonth( "March", new HashSet<>( Arrays.asList( 2, 1, 3, 31 ) ) );

		// change the order of the evil-doings
		division.removePerson( "evil", "johnny" );
		final BadPerson evilJohnny = new BadPerson( "evil", "johnny", 666 );
		evilJohnny.addEvilDoing( "disliked technology" );
		evilJohnny.addEvilDoing( "added sugar to coffee" );
		evilJohnny.addEvilDoing( "added semicolon to someones for-loop" );
		evilJohnny.addEvilDoing( "wasted a beer" );
		division.addPerson( evilJohnny );

		final int size = 3;
		final int[][][] threeD = new int[ size ][ size ][ size ];
		for( int i = 0; i < 3; ++i )
		{
			for( int j=0; j < 3; ++j )
			{
				for( int k = 0; k < 3; ++k )
				{
					threeD[ i ][ j ][ k ] = i * 100 + j * 10 + k;
				}
			}
		}
		threeD[ 0 ][ 0 ][ 1 ] = 2;
		threeD[ 0 ][ 0 ][ 2 ] = 1;

		threeD[ 0 ][ 1 ][ 0 ] = 21;
		threeD[ 0 ][ 1 ][ 1 ] = 20;
		threeD[ 0 ][ 1 ][ 2 ] = 22;
		threeD[ 0 ][ 2 ][ 0 ] = 12;
		threeD[ 0 ][ 2 ][ 1 ] = 11;
		threeD[ 0 ][ 2 ][ 2 ] = 10;
		division.setThreeD( threeD );

		return division;
	}
}
