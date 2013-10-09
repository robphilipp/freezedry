package org.freezedry.persistence.utils;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.builders.DoubleNodeBuilder;
import org.freezedry.persistence.tests.BadPerson;
import org.freezedry.persistence.utils.tests.Fconcrete;
import org.freezedry.persistence.utils.tests.circle.*;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.RunnableScheduledFuture;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReflectionUtilsTest {
	@Before
	public void setUp() throws Exception
	{
		DOMConfigurator.configure( "log4j.xml" );

		final List< Field > fields = ReflectionUtils.getAllDeclaredFields( DoubleNodeBuilder.class );
		for( Field field : fields )
		{
			System.out.println( field.getName() );
		}

		System.out.println( ReflectionUtils.getDeclaredField( BadPerson.class, "givenName" ) );
		System.exit( 0 );

//		System.out.println( "List -> Collection: distance = " + ReflectionUtils.calculateClassDistance( List.class, Collection.class, -1 ) );
//		System.out.println( "List -> Iterable: distance = " + ReflectionUtils.calculateClassDistance( List.class, Iterable.class, -1 ) );
//		System.out.println( "RunnableScheduledFuture -> Comparable: distance = " + ReflectionUtils.calculateClassDistance( RunnableScheduledFuture.class, Comparable.class, -1 ) );
//		System.out.println( "D -> A: distance = " + ReflectionUtils.calculateClassDistance( D.class, A.class, -1 ) );
//		System.out.println( "D -> B: distance = " + ReflectionUtils.calculateClassDistance( D.class, B.class, -1 ) );
//		System.out.println( "D -> Aprime: distance = " + ReflectionUtils.calculateClassDistance( D.class, Aprime.class, -1 ) );
//		System.out.println( "Econcrete -> A: distance = " + ReflectionUtils.calculateClassDistance( Econcrete.class, A.class, -1 ) );
//		System.out.println( "Econcrete -> B: distance = " + ReflectionUtils.calculateClassDistance( Econcrete.class, B.class, -1 ) );
//		System.out.println( "Econcrete -> D: distance = " + ReflectionUtils.calculateClassDistance( Econcrete.class, D.class, -1 ) );
//		System.out.println( "Econcrete -> Aprime: distance = " + ReflectionUtils.calculateClassDistance( Econcrete.class, Aprime.class, -1 ) );
//		System.out.println( "Fconcrete -> A: distance = " + ReflectionUtils.calculateClassDistance( Fconcrete.class, A.class, -1 ) );
//		System.out.println( "Econcrete -> Econcrete: distance = " + ReflectionUtils.calculateClassDistance( Econcrete.class, Econcrete.class, -1 ) );
//		System.out.println( "Fconcrete -> Econcrete: distance = " + ReflectionUtils.calculateClassDistance( Fconcrete.class, Econcrete.class, -1 ) );
//		System.out.println( "Gconcrete -> Econcrete: distance = " + ReflectionUtils.calculateClassDistance( Gconcrete.class, Econcrete.class, -1 ) );
	}

	@Test
	public void testIsClassOrSuperclass() throws Exception
	{

	}

	@Test
	public void testIsSuperclass() throws Exception
	{

	}

	@Test
	public void testCalculateClassDistance() throws Exception
	{

	}

	@Test
	public void testGetPersistenceName() throws Exception
	{

	}

	@Test
	public void testGetFieldForPersistenceName() throws Exception
	{

	}

	@Test
	public void testGetFieldNameForPersistenceName() throws Exception
	{

	}

	@Test
	public void testGetMostSpecificClass() throws Exception
	{

	}

	@Test
	public void testGetNodeBuilderClass() throws Exception
	{

	}

	@Test
	public void testHasNodeBuilderAnnotation() throws Exception
	{

	}

	@Test
	public void testGetItemOrAncestorCopyable() throws Exception
	{

	}

	@Test
	public void testGetItemOrAncestor() throws Exception
	{

	}

	@Test
	public void testGetAllDeclaredFields() throws Exception
	{

	}

	@Test
	public void testGetDeclaredField() throws Exception
	{

	}

	@Test
	public void testCast() throws Exception
	{

	}
}
