package org.freezedry.difference;

import junit.framework.Assert;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.freezedry.persistence.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/7/13
 * Time: 8:41 PM
 * To change this template use File | Settings | File Templates.
 */
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

	public static void main( String...args ) throws ParseException
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.WARN );

		//
//		System.out.println( "Complex Object" );
//		long start = System.currentTimeMillis();
//		final ObjectDifferenceCalculator diffCalc = new ObjectDifferenceCalculator( "." );
//		Map< String, ObjectDifferenceCalculator.Difference> difference = null;
//		long iters = 1_000;
//		for( int i = 0; i < iters; ++i )
//		{
//			difference = diffCalc.calculateDifference( division, division2 );
//		}
//		for( Map.Entry< String, ObjectDifferenceCalculator.Difference> entry : difference.entrySet() )
//		{
//			System.out.println( entry.getKey() + ": " + entry.getValue().toString() );
//		}
//		System.out.println( (double)( System.currentTimeMillis() - start ) / iters + " ms per comparison");
//
//		//
//		System.out.println( "Simple Object" );
//		Account account1 = new Account( 12345L, "firmABCD", "office123", "12345", "cash", "individual", "USD", "US", true, Calendar.getInstance().getTime() );
//		Account account2 = new Account( 12345L, "firmABC", "office123", "12345", "cash", "individual", "USD", "US", true, Calendar.getInstance().getTime() );
//		start = System.currentTimeMillis();
//		for( int i = 0; i < iters; ++i )
//		{
//			difference = diffCalc.calculateDifference( account1, account2 );
//		}
//		for( Map.Entry< String, ObjectDifferenceCalculator.Difference> entry : difference.entrySet() )
//		{
//			System.out.println( entry.getKey() + ": " + entry.getValue().toString() );
//		}
//		System.out.println( (double)( System.currentTimeMillis() - start ) / iters + " ms per comparison");
	}

	static class Account {
		private Long accountId;
		private String firmCode;
		private String officeCode;
		private String accountNumber;
		private String accountType;
		private String tradingType;
		private String baseCurrency;
		private String countryCode;
		private Boolean accountActive;
		private Date creationDate;

		Account( Long accountId, String firmCode, String officeCode, String accountNumber, String accountType, String tradingType, String baseCurrency, String countryCode, Boolean accountActive, Date creationDate )
		{
			this.accountId = accountId;
			this.firmCode = firmCode;
			this.officeCode = officeCode;
			this.accountNumber = accountNumber;
			this.accountType = accountType;
			this.tradingType = tradingType;
			this.baseCurrency = baseCurrency;
			this.countryCode = countryCode;
			this.accountActive = accountActive;
			this.creationDate = creationDate;
		}
	}

}
