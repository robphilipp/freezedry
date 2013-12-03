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

import org.freezedry.persistence.tests.Division;
import org.freezedry.persistence.tests.Person;
import org.junit.Assert;
import org.junit.Test;

public class KeyValuePersistenceTest extends AbstractPersistenceTest {

	private final KeyValuePersistence persistence = new KeyValuePersistence();

	@Test
	public void testPersistence() throws Exception
	{
		final String output = OUTPUT_DIR + "division.txt";

		persistence.setKeySeparator( "." );
		persistence.write( division, output );

		Assert.assertTrue( division.equals( persistence.read( Division.class, output ) ) );
	}
}
