package org.freezedry;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * [Description]
 *
 * @author rob
 *         12/26/13 3:29 PM
 */
public class PaxExamTestUtils {

	/**
	 * @return an array of options containing the core freezedry bundles to be provisioned
	 */
	public static Option[] freezedryBundles()
	{
		return new Option[] {
				mavenBundle( "com.closure-sys", "freezedry" ),
				mavenBundle( "org.apache.geronimo.bundles", "json", "20090211_1" ),
				mavenBundle( "org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-io", "1.4_3" )
		};
	}

	/**
	 * @return an array of options containing the core logging bundles to be provisioned
	 */
	public static Option[] logging()
	{
		return new Option[] {
				mavenBundle( "org.slf4j", "slf4j-api", "1.7.5" ),
				mavenBundle( "ch.qos.logback", "logback-core", "1.0.13" ),
				mavenBundle( "ch.qos.logback", "logback-classic", "1.0.13" ),
				systemProperty("logback.configurationFile").value( "file:" + PathUtils.getBaseDir() + "/src/test/resources/logback-test.xml" ),
				systemProperty( "org.ops4j.pax.logging.DefaultServiceLog.level" ).value( "WARN" )
		};
	}
}
