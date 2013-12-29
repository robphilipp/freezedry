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

import junit.framework.Assert;
import org.freezedry.persistence.builders.StringNodeBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 7:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistenceEngineTest {

	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void testSetPersistClassConstants() throws Exception
	{

	}

	@Test
	public void testAddNodeBuilder() throws Exception
	{
		final PersistenceEngine engine = new PersistenceEngine();
		engine.removeNodeBuilder( String.class );
		Assert.assertFalse( engine.containsNodeBuilder( String.class ) );

		engine.addNodeBuilder( String.class, new StringNodeBuilder() );
		Assert.assertTrue( engine.containsNodeBuilder( String.class ) );
	}

	@Test
	public void testContainsNodeBuilder() throws Exception
	{

	}

	@Test
	public void testContainsAnnotatedNodeBuilder() throws Exception
	{

	}

	@Test
	public void testGetNodeBuilder() throws Exception
	{

	}

	@Test
	public void testIsForbiddenRootObject() throws Exception
	{

	}

	@Test
	public void testIsAllowedRootObject() throws Exception
	{

	}

	@Test
	public void testRemoveNodeBuilder() throws Exception
	{

	}

	@Test
	public void testSetGeneralArrayNodeBuilder() throws Exception
	{

	}

	@Test
	public void testCreateSemanticModel() throws Exception
	{

	}

	@Test
	public void testCreateNode() throws Exception
	{

	}

	@Test
	public void testParseSemanticModel() throws Exception
	{

	}

	@Test
	public void testCreateObject() throws Exception
	{

	}
}
