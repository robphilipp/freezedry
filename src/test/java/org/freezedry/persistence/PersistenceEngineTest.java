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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 10/8/13
 * Time: 7:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistenceEngineTest {

	@Test
	public void testAddNodeBuilder() throws Exception
	{
		final PersistenceEngine engine = new PersistenceEngine();
		engine.removeNodeBuilder( String.class );
		assertFalse( engine.containsNodeBuilder( String.class ) );

		engine.addNodeBuilder( String.class, new StringNodeBuilder() );
		assertTrue( engine.containsNodeBuilder( String.class ) );
	}
}
