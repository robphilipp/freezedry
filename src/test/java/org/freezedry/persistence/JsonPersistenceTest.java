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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.freezedry.difference.ObjectDifferenceCalculator;
import org.freezedry.persistence.tests.Division;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.freezedry.PaxExamTestUtils.freezedryBundles;
import static org.freezedry.PaxExamTestUtils.logging;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class JsonPersistenceTest extends AbstractPersistenceTest {

	private final JsonPersistence persistence = new JsonPersistence();

	/**
	 * @return The configuration for the OSGi framework
	 */
	@Configuration
	public Option[] configuration()
	{
//		((Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME )).setLevel( Level.WARN );
		return combine( combine( freezedryBundles(), logging() ),
				junitBundles(),
				cleanCaches() );
	}

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
