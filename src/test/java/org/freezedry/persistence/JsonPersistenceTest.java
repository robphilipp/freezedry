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
import org.freezedry.persistence.tests.*;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertTrue;

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
		assertTrue( differences == null || differences.isEmpty() );
	}

	@Test
	public void testEmptyList()
	{
		final String output = OUTPUT_DIR + "bad-person.json";

		final BadPerson person = new BadPerson( "evil", "johnny", 666 );
		persistence.write( person, output );
		final BadPerson rePerson = persistence.read( BadPerson.class, output );

		final ObjectDifferenceCalculator calculator = new ObjectDifferenceCalculator();
		final Map< String, ObjectDifferenceCalculator.Difference > differences = calculator.calculateDifference( rePerson, person );
		assertTrue( differences == null || differences.isEmpty() );
	}

	@Test
	public void testEnum()
	{
		final String output = OUTPUT_DIR + "thing-with-enum.json";
		final ThingWithEnum thing = new ThingWithEnum();
		persistence.write( thing, output );
		final ThingWithEnum rething = persistence.read( ThingWithEnum.class, output );

		final ObjectDifferenceCalculator calculator = new ObjectDifferenceCalculator();
		final Map< String, ObjectDifferenceCalculator.Difference > differences = calculator.calculateDifference( rething, thing );
		assertTrue( differences == null || differences.isEmpty() );
	}

	@Test
	public void testGenericClass()
	{
		final String output = OUTPUT_DIR + "generic-type-class.json";

		final GenericTypeClass<GenericTypeSubclass> mySubclass = new GenericTypeClass<>( new GenericTypeSubclass( "3.151592653" ) );
//		final GenericTypeClass< Double > myDouble = new GenericTypeClass<>( 3.141592653 );
		persistence.write( mySubclass, output );
		final GenericTypeClass<GenericTypeSubclass> reMySubclass = persistence.read( GenericTypeClass.class, output );

		final ObjectDifferenceCalculator calculator = new ObjectDifferenceCalculator();
		final Map< String, ObjectDifferenceCalculator.Difference > differences = calculator.calculateDifference( reMySubclass, mySubclass );
		assertTrue( differences == null || differences.isEmpty() );
	}
}
