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

import org.freezedry.difference.ObjectDifferenceCalculator;
import org.freezedry.persistence.tests.Division;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class JsonPersistenceTest extends AbstractPersistenceTest {

	private final JsonPersistence persistence = new JsonPersistence();

	@Test
	public void testPersistence() throws Exception
	{
		final String output = OUTPUT_DIR + "division.json";

		persistence.write( division, output );

		final Division redivision = persistence.read( Division.class, output );
		final ObjectDifferenceCalculator calculator = new ObjectDifferenceCalculator();
		final Map< String, ObjectDifferenceCalculator.Difference > differences = calculator.calculateDifference( redivision, division );
		junit.framework.Assert.assertTrue( differences == null || differences.isEmpty() );
	}
}
