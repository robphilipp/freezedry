package org.freezedry.persistence.readers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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

public class KeyValueReaderTest {

	protected final static String OUTPUT_DIR = "src/test/output/";

//	@Before
//	public void setUp() throws Exception
//	{
//		DOMConfigurator.configure( "log4j.xml" );
//		Logger.getRootLogger().setLevel( Level.ERROR );
//
//		final KeyValueReader reader = new KeyValueReader();
//		reader.setKeyElementSeparator( "." );
////		reader.setRemoveEmptyTextNodes( false );
//		final KeyValueBuilder builder = reader.getBuilder();
//		builder.putRenderer( Collection.class, new FlatteningCollectionRenderer( builder ) );
//		final InputStream inputStream = new BufferedInputStream( new FileInputStream( OUTPUT_DIR + "person.txt" ) );
//		final Reader input = new InputStreamReader( inputStream );
//		final InfoNode infoNode = reader.read( Division.class, input );
////		System.out.println( infoNode.simpleTreeToString() );
//
//		final PersistenceEngine engine = new PersistenceEngine();
//		final Object reperson = engine.parseSemanticModel( Division.class, infoNode );
////		System.out.println( reperson );
//	}
//
//	@Test
//	public void testGetBuilder() throws Exception
//	{
//
//	}
//
//	@Test
//	public void testSetBuilder() throws Exception
//	{
//
//	}
//
//	@Test
//	public void testSetKeyValueSeparator() throws Exception
//	{
//
//	}
//
//	@Test
//	public void testGetKeyValueSeparator() throws Exception
//	{
//
//	}
//
//	@Test
//	public void testSetKeyElementSeparator() throws Exception
//	{
//
//	}
//
//	@Test
//	public void testGetKeyElementSeparator() throws Exception
//	{
//
//	}
//
//	@Test
//	public void testRead() throws Exception
//	{
//
//	}
}
