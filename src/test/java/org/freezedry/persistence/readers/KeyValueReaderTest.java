package org.freezedry.persistence.readers;

import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.FlatteningCollectionRenderer;
import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tree.InfoNode;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyValueReaderTest {
	@Before
	public void setUp() throws Exception
	{
		DOMConfigurator.configure( "log4j.xml" );

		final KeyValueReader reader = new KeyValueReader();
		reader.setKeyElementSeparator( "." );
//		reader.setRemoveEmptyTextNodes( false );
		final KeyValueBuilder builder = reader.getBuilder();
		builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
		final InputStream inputStream = new BufferedInputStream( new FileInputStream( "person.txt" ) );
		final Reader input = new InputStreamReader( inputStream );
		final InfoNode infoNode = reader.read( Division.class, input );
		System.out.println( infoNode.simpleTreeToString() );

		final PersistenceEngine engine = new PersistenceEngine();
		final Object reperson = engine.parseSemanticModel( Division.class, infoNode );
		System.out.println( reperson );
	}

	@Test
	public void testGetBuilder() throws Exception
	{

	}

	@Test
	public void testSetBuilder() throws Exception
	{

	}

	@Test
	public void testSetKeyValueSeparator() throws Exception
	{

	}

	@Test
	public void testGetKeyValueSeparator() throws Exception
	{

	}

	@Test
	public void testSetKeyElementSeparator() throws Exception
	{

	}

	@Test
	public void testGetKeyElementSeparator() throws Exception
	{

	}

	@Test
	public void testRead() throws Exception
	{

	}
}
