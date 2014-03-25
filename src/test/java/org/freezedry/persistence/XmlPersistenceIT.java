package org.freezedry.persistence;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.freezedry.PaxExamTestUtils.freezedryBundles;
import static org.freezedry.PaxExamTestUtils.logging;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.OptionUtils.combine;

/**
 * [Description]
 *
 * @author rob
 *         12/28/13 4:39 PM
 */
@Ignore
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class XmlPersistenceIT extends XmlPersistenceTest {

	/**
	 * @return The configuration for the OSGi framework
	 */
	@Configuration
	public Option[] configuration()
	{
		return combine( combine( freezedryBundles(), logging() ),
				junitBundles(),
				cleanCaches() );
	}
}
