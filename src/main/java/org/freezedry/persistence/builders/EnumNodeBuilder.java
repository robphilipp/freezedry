package org.freezedry.persistence.builders;

import org.freezedry.persistence.PersistenceEngine;
import org.freezedry.persistence.tree.InfoNode;
import org.freezedry.persistence.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 *
 * Created by rob on 3/22/14.
 */
public class EnumNodeBuilder extends AbstractLeafNodeBuilder
{
	private static final Logger LOGGER = LoggerFactory.getLogger( EnumNodeBuilder.class );

	/**
	 * Constructs the {@link NodeBuilder} for going between enums and back to {@link Object}s.
	 * @param engine The {@link org.freezedry.persistence.PersistenceEngine}
	 */
	public EnumNodeBuilder( final PersistenceEngine engine )
	{
		super( engine );
	}

	/**
	 * Copy constructor
	 * @param builder The {@link org.freezedry.persistence.builders.EnumNodeBuilder} to copy
	 */
	public EnumNodeBuilder( final EnumNodeBuilder builder )
	{
		super( builder );
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. The specified containing {@link Class}
	 * is the {@link Class} in which the specified field name lives. And the object is the value of
	 * the field name.
	 * @param containingClass The {@link Class} that contains the specified field name
	 * @param object The value of the field with the specified field name
	 * @param fieldName The name of the field for which the object is the value
	 * @return The constructed {@link InfoNode} based on the specified information
	 * @throws ReflectiveOperationException
	 */
	@Override
	public InfoNode createInfoNode( final Class<?> containingClass, final Object object, final String fieldName )
	{
		// when the containing class is null, then class is the root node of the semantic model, and therefore
		// there won't be a field name to with a annotation containing the persist name.
		String persistName = null;
		if( containingClass != null )
		{
			// grab the persistence name if the annotation @Persist( persistName = "xxxx" ) is specified,
			// and if the leaf is part of another class (such as a collection) it will return the field name
			persistName = ReflectionUtils.getPersistenceName( containingClass, fieldName );
		}
		if( persistName == null || persistName.isEmpty() )
		{
			persistName = fieldName;
		}

		// todo create an annotation that specifies which method to call on the enum to get the value. (See the DateNodeBuilder)
		// NOTE: must do the same for creating the object

		// create a new leaf node with the value of the enum
		return InfoNode.createLeafNode( fieldName, ((Enum)object).name(), persistName, object.getClass() );
	}

	/**
	 * Generates an {@link InfoNode} from the specified {@link Object}. This method is used for objects that have
	 * an overriding node builder and are not contained within a class. For example, suppose you would like
	 * to persist an {@link java.util.ArrayList} for serialization and would like to maintain the type information.
	 * @param object The value of the field with the specified field name
	 * @return The constructed {@link InfoNode} based on the specified information
	 * @throws ReflectiveOperationException
	 */
	@Override
	public InfoNode createInfoNode( final Object object, final String persistName )
	{
		// create the root node and add the string rep of the date
		final InfoNode node = InfoNode.createRootNode( persistName, object.getClass() );
		node.addChild( InfoNode.createLeafNode( "value", ((Enum)object).name(), "value", String.class ) );

		// return the node
		return node;
	}
	/**
	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}. Note that
	 * the {@link com.sun.org.apache.xalan.internal.lib.NodeInfo} may also contain type information about the class to generate. The specified {@link Class}
	 * overrides that value. This is done to avoid modifying the {@link com.sun.org.apache.xalan.internal.lib.NodeInfo} tree when supplemental information becomes
	 * available.
	 * @param containingClass The {@link Class} containing the clazz, represented by the {@link InfoNode}
	 * @param clazz The {@link Class} of the object to create
	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
	 */
	@Override
	public Enum createObject( final Class<?> containingClass, final Class<?> clazz, final InfoNode node )
	{
		final String value = (String)node.getValue();
		Enum enumObject = null;
		for( Object constant : clazz.getEnumConstants() )
		{
			if( ((Enum)constant).name().equals( value ) )
			{
				enumObject = (Enum)constant;
				break;
			}
		}
		return enumObject;
	}

	/**
	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}.
	 * This method is used for objects that have an overriding node builder and are not contained within a
	 * class. For example, suppose you would like to persist an {@link java.util.ArrayList} for serialization and would
	 * like to maintain the type information.
	 * @param clazz The {@link Class} of the object to create
	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
	 */
	@Override
	public Enum createObject( final Class< ? > clazz, final InfoNode node )
	{
		return createObject( null, clazz, node.getChild( 0 ) );
	}

	/**
	 * Creates and returns a copy of the object <code>x</code> that meets the following criteria
	 * <ol>
	 * 	<li>The expressions <code>x.getCopy() != x</code> evaluates as <code>true</code></li>
	 * 	<li>The expressions <code>x.getCopy().equals( x )</code> evaluates as <code>true</code></li>
	 * 	<li>The expressions <code>x.getCopy().getClass() == x.getClass()</code> evaluates as <code>true</code></li>
	 * </ol>
	 * @return a copy of the object that meets the above criteria
	 */
	@Override
	public NodeBuilder getCopy()
	{
		return new EnumNodeBuilder( this );
	}
}
