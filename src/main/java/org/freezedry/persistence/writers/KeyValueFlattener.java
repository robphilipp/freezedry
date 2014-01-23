package org.freezedry.persistence.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freezedry.persistence.containers.Pair;
import org.freezedry.persistence.keyvalue.BasicKeyValueBuilder;
import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
import org.freezedry.persistence.tree.InfoNode;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * [Description]
 *
 * @author rphilipp
 *         10/7/13, 11:00 AM
 */
public class KeyValueFlattener {

	private static final Logger LOGGER = LoggerFactory.getLogger( KeyValueFlattener.class );

	private KeyValueBuilder builder;

	/**
	 * Constructs a basic key-value flattener that uses the specified renderers and separator.
	 * @param renderers The mapping between the {@link Class} represented by an {@link org.freezedry.persistence.tree.InfoNode} and
	 * the {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create the key-value pair.
	 * @param arrayRenderer The {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create key-value pairs for
	 * {@link org.freezedry.persistence.tree.InfoNode}s that represent an array.
	 * @param keySeparator The separator between the flattened elements of the key
	 * @see org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder#getRenderer(Class)
	 */
	public KeyValueFlattener( final Map< Class< ? >, PersistenceRenderer > renderers,
							  final PersistenceRenderer arrayRenderer,
						   	  final String keySeparator )
	{
		builder = new BasicKeyValueBuilder( renderers, arrayRenderer, keySeparator );
	}

	/**
	 * Constructs a basic key-value flattener that uses the default renderers and specified separator.
	 * @param keySeparator The separator between the flattened elements of the key
	 */
	public KeyValueFlattener( final String keySeparator )
	{
		builder = new BasicKeyValueBuilder( keySeparator );
	}

	/**
	 * Constructs a basic key-value flattener that uses the default renderers and separator.
	 */
	public KeyValueFlattener()
	{
		builder = new BasicKeyValueBuilder();
	}

	/**
	 * Constructs a key-value flattener using the specified key-value list builder
	 * @param builder The {@link KeyValueBuilder} used to flatten the semantic model
	 */
	public KeyValueFlattener( final KeyValueBuilder builder )
	{
		this.builder = builder;
	}

	/**
	 * The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 * @param separator The separator
	 */
	public void setKeyElementSeparator( final String separator )
	{
		builder.setSeparator( separator );
	}

	/**
	 * @return The separator between the flattened key elements. For example, suppose that a {@code Division} has a {@link java.util.List}
	 * of {@code Person} objects, called {@code people}. The the key for a person's first name may be of the form:
	 * {@code Division.people.Person[2].firstName}, or {@code Division:people:Person[2]:firstName}. The "{@code .}" and
	 * the "{@code :}" are separators.
	 */
	public String getKeyElementSeparator()
	{
		return builder.getSeparator();
	}

	/**
	 * Sets the builder responsible for creating the key-value pairs from the semantic model,
	 * and that is responsible for parsing the key-value pairs into a semantic model.
	 * @param builder the {@link KeyValueBuilder} responsible for creating the key-value pairs
	 * from the semantic model, and that is responsible for parsing the key-value pairs into
	 * a semantic model.
	 */
	public void setBuilder( final KeyValueBuilder builder )
	{
		this.builder = builder;
	}

	/**
	 * @return the {@link KeyValueBuilder} responsible for creating the key-value pairs
	 * from the semantic model, and that is responsible for parsing the key-value pairs into
	 * a semantic model.
	 */
	public KeyValueBuilder getBuilder()
	{
		return builder;
	}

	public List<Pair< String, Object >> buildKeyValuePairs( final InfoNode root )
	{
		return builder.buildKeyValuePairs( root );
	}

	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link java.util.List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link java.util.List} would have a key of the form {@code names[i].String}. I recommend
	 * against setting this to true.
	 * @param isShowFullKey true means that the full key will be persisted; false is default
	 */
	public void setShowFullKey( final boolean isShowFullKey )
	{
		builder.setShowFullKey( isShowFullKey );
	}

	/**
	 * When set to true, the full key is persisted. So for example, normally, if there is a {@link java.util.List}
	 * of {@link String} called {@code names}, then the key will have the form {@code names[i]}. When this is
	 * set to true, then the {@link java.util.List} would have a key of the form {@code names[i].String}
	 * @return true means that the full key will be persisted; false is default
	 */
	public boolean isShowFullKey()
	{
		return builder.isShowFullKey();
	}
}
