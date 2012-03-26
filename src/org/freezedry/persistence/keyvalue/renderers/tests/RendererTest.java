package org.freezedry.persistence.keyvalue.renderers.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.freezedry.persistence.keyvalue.BasicKeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.CollectionRenderer;
import org.freezedry.persistence.keyvalue.renderers.LeafNodeRenderer;
import org.freezedry.persistence.keyvalue.renderers.MapRenderer;
import org.junit.Test;

public class RendererTest {

	@Test
	public void testCollectionIsRenderer()
	{
		final CollectionRenderer renderer = new CollectionRenderer( new BasicKeyValueBuilder(), "[", "]" );
		assertTrue( renderer.isRenderer( "[0]" ) );
		assertTrue( renderer.isRenderer( "[0]{\"test\"}" ) );
		assertTrue( renderer.isRenderer( "people[0]" ) );
		assertTrue( renderer.isRenderer( "people[0]{\"test\"}" ) );
		assertFalse( renderer.isRenderer( "people" ) );
		assertFalse( renderer.isRenderer( "people{2}" ) );
		assertFalse( renderer.isRenderer( "people{\"test\"}[0]" ) );
	}

	@Test
	public void testMapIsRenderer()
	{
		final MapRenderer renderer = new MapRenderer( new BasicKeyValueBuilder(), "{", "}" );
		assertTrue( renderer.isRenderer( "{\"test\"}" ) );
		assertTrue( renderer.isRenderer( "{\"test\"}[0]" ) );
		assertTrue( renderer.isRenderer( "people{\"test\"}" ) );
		assertTrue( renderer.isRenderer( "people{\"test\"}[0]" ) );
		assertFalse( renderer.isRenderer( "people" ) );
		assertFalse( renderer.isRenderer( "people[2]" ) );
		assertFalse( renderer.isRenderer( "people[0]{\"test\"}" ) );
	}

	@Test
	public void testLeafIsRenderer()
	{
		final LeafNodeRenderer renderer = new LeafNodeRenderer( new BasicKeyValueBuilder() );
		assertTrue( renderer.isRenderer( "people" ) );
		assertFalse( renderer.isRenderer( "{\"test\"}" ) );
		assertFalse( renderer.isRenderer( "{\"test\"}[0]" ) );
		assertFalse( renderer.isRenderer( "people{\"test\"}" ) );
		assertFalse( renderer.isRenderer( "people{\"test\"}[0]" ) );
		assertFalse( renderer.isRenderer( "people[2]" ) );
		assertFalse( renderer.isRenderer( "people[0]{\"test\"}" ) );
	}

}
