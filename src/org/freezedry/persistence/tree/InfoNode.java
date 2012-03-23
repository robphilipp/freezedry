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
package org.freezedry.persistence.tree;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freezedry.persistence.copyable.Copyable;
import org.freezedry.persistence.utils.Constants;

/**
 * {@link InfoNode} that holds the information about the field/class it represents. In effect,
 * the root {@link InfoNode} forms the semantic model representing the object to be persisted.
 * 
 * @author Robert Philipp
 */
public class InfoNode implements Copyable< InfoNode > {

	// tree variables
	private InfoNode parent;
	private final List< InfoNode > children;

	private final NodeType nodeType;
	private String fieldName;
	private Object value;
	private String persistName;
	private Class< ? > clazz;

	// help for parsing
	private List< Type > genericParameterTypes;
	
	// not yet implemented...to come soon.
	private Constructor< ? > constructor;
	private Map< String, Method > setFieldMap;
	private Map< String, Method > getFieldMap;
	
	private boolean isProcessed = false;
	
	/**
	 * Creates a root {@link InfoNode} with the specified persistence name and class type.
	 * @param persistName The name with which to persist the hierarchy, which is typical the class name
	 * @param clazz The type of the field
	 * @return a root {@link InfoNode}
	 */
	public static InfoNode createRootNode( final String persistName, final Class< ? > clazz )
	{
		synchronized( InfoNode.class )
		{
			return new InfoNode( NodeType.ROOT_NODE, null, null, persistName, clazz, null, null, null );
		}
	}
	
	/**
	 * Creates an {@link InfoNode} that can contain other {@link InfoNode}s, but has no value.
	 * @param fieldName The name of the field to persist
	 * @param persistName The name with which to persist the field
	 * @param clazz The type of the field
	 * @return a compound {@link InfoNode}
	 */
	public static InfoNode createCompoundNode( final String fieldName, final String persistName, final Class< ? > clazz )
	{
		synchronized( InfoNode.class )
		{
			return new InfoNode( NodeType.COMPOUND_NODE, fieldName, null, persistName, clazz, null, null, null );
		}
	}
	
	/**
	 * A leaf {@link InfoNode} that has a value, but does not contain other {@link InfoNode}s. 
	 * @param fieldName The name of the field to persist
	 * @param value The value of the node (or the value of the field it represents)
	 * @param persistName The name with which to persist the field
	 * @param clazz The type of the field
	 * @return a leaf {@link InfoNode}
	 */
	public static InfoNode createLeafNode( final String fieldName, final Object value, final String persistName, final Class< ? > clazz )
	{
		synchronized( InfoNode.class )
		{
			return new InfoNode( NodeType.LEAF_NODE, fieldName, value, persistName, clazz, null, null, null );
		}
	}
	
	/*
	 * Creates a new {@link InfoNode} and makes a copy of the data in that node. NOTE, it does NOT copy the
	 * children. To copy the children use the {@link #getCopy()} method.
	 * @param node The node from which to copy the data
	 * @return a new {@link InfoNode} and makes a copy of the data in that node
	 */
	private static InfoNode copyNodeData( final InfoNode node )
	{
		Map< String, Method > setFieldMap = null;
		if( node.setFieldMap != null )
		{
			setFieldMap = new HashMap<>( node.setFieldMap );
		}
		Map< String, Method > getFieldMap = null;
		if( node.getFieldMap != null )
		{
			getFieldMap = new HashMap<>( node.getFieldMap );
		}
		return new InfoNode( node.nodeType, 
							 node.fieldName, 
							 node.value, 
							 node.persistName, 
							 node.clazz, 
							 node.constructor,
							 setFieldMap,
							 getFieldMap );
	}
	
	/**
	 * Creates an {@link InfoNode} with the specified characteristics
	 * @param nodeType The type of {@link InfoNode} as held in the {@link #nodeType} enum.
	 * @param fieldName The name of the field to persist
	 * @param value The value of the node (or the value of the field it represents)
	 * @param persistName The name with which to persist the field
	 * @param clazz The type of the field
	 * @param constructor The constructor to use for instantiation (not yet implemented)
	 * @param setFieldMap The map that is used to determine the method that is called for setting a field (not yet implemented)
	 * @param getFieldMap The map that is used to determine the method that is called for setting a field (not yet implemented)
	 */
	private InfoNode( final NodeType nodeType, 
					  final String fieldName, 
					  final Object value, 
					  final String persistName, 
					  final Class< ? > clazz,
					  final Constructor< ? > constructor, 
					  final Map< String, Method > setFieldMap,
					  final Map< String, Method > getFieldMap )
	{
		this.nodeType = nodeType;
		this.children = new ArrayList<>();
		
		this.fieldName = fieldName;
		this.value = value;
		this.persistName = persistName;
		this.clazz = clazz;
		this.setFieldMap = setFieldMap;
		this.getFieldMap = getFieldMap;

		this.value = value;
		
		this.genericParameterTypes = new ArrayList<>();
	}

	/**
	 * @return The type of {@link InfoNode}
	 */
	public final NodeType getNodeType()
	{
		return nodeType;
	}

	/**
	 * @return the name of the field represented by this node
	 */
	public final String getFieldName()
	{
		return fieldName;
	}

	/**
	 * Sets the name of the field represented by this node
	 * @param fieldName the name of the field represented by this node
	 */
	public final void setFieldName( String fieldName )
	{
		this.fieldName = fieldName;
	}
	
	/**
	 * @return the value of this node (usually only used for leaf nodes)
	 */
	public final Object getValue()
	{
		return value;
	}

	/**
	 * @param value the value of this node (usually only used for leaf nodes)
	 */
	public final void setValue( Object value )
	{
		this.value = value;
	}

	/**
	 * @return the name used persist the field
	 */
	public final String getPersistName()
	{
		return persistName;
	}

	/**
	 * Sets the the name used persist the field
	 * @param persistName the name used persist the field
	 */
	public final void setPersistName( final String persistName )
	{
		this.persistName = persistName;
	}

	/**
	 * @return the {@link Class} of the field
	 */
	public final Class< ? > getClazz()
	{
		return clazz;
	}

	/**
	 * @param clazz the {@link Class} of the field
	 */
	public final void setClazz( final Class< ? > clazz )
	{
		this.clazz = clazz;
	}

	/**
	 * (Not yet implemented)
	 * @return the constructor to be used to instantiate the field
	 */
	public final Constructor< ? > getConstructor()
	{
		return constructor;
	}

	/**
	 * (Not yet implemented)
	 * @param constructor the constructor to be used to instantiate the field
	 */
	public final void setConstructor( Constructor< ? > constructor )
	{
		this.constructor = constructor;
	}

	/**
	 * @return the map used to determine the method for setting a specified field
	 */
	public final Map< String, Method > getSetFieldMap()
	{
		return setFieldMap;
	}

	/**
	 * @param setFieldMap the map used to determine the method for setting a specified field
	 */
	public final void setSetFieldMap( Map< String, Method > setFieldMap )
	{
		this.setFieldMap = setFieldMap;
	}

	/**
	 * @return the the map used to determine the method for getting a specified field
	 */
	public final Map< String, Method > getGetFieldMap()
	{
		return getFieldMap;
	}

	/**
	 * @param getFieldMap the map used to determine the method for getting a specified field
	 */
	public final void setGetFieldMap( Map< String, Method > getFieldMap )
	{
		this.getFieldMap = getFieldMap;
	}
	
	/**
	 * @return a list of generic type parameters held by the field of this node
	 */
	public final List< Type > getGenericParameterTypes()
	{
		return genericParameterTypes;
	}
	
	/**
	 * Allows the user to set the list of generic types held by this node. This is useful for
	 * complex types where the a generic class may not be associated with a field. For example,
	 * the inner map of a {@code Map< String, Map< String, Double > >} doesn't have a field, and
	 * during the parsing, the child nodes of the outer map can be populated with the {@code String}
	 * and {@code Double} of the inner map for later use.
	 * @param types a list of generic type parameters held by the field of this node
	 */
	public final void setGenericParameterTypes( final List< Type > types )
	{
		this.genericParameterTypes = types;
	}
	
	/**
	 * Allows the user to set the list of generic types held by this node. This is useful for
	 * complex types where the a generic class may not be associated with a field. For example,
	 * the inner map of a {@code Map< String, Map< String, Double > >} doesn't have a field, and
	 * during the parsing, the child nodes of the outer map can be populated with the {@code String}
	 * and {@code Double} of the inner map for later use.
	 * @param type a generic type parameter held by the field of this node
	 */
	public final void addGenericParameterType( final Type type )
	{
		genericParameterTypes.add( type );
	}
	
	public final void setIsProcessed( final boolean isProcessed )
	{
		this.isProcessed = isProcessed;
	}
	
	public final boolean isProcessed()
	{
		return isProcessed;
	}

	/**
	 * @return a string representation of the node
	 */
	public String nodeInfoToString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "Node Type: " + nodeType.name() + "; " );
		buffer.append( "Field Name: " + (fieldName==null?"[null]":fieldName) + "; " );
		buffer.append( "Value: " + (value==null?"[null]":value) + "; " );
		buffer.append( "Persist Name: " + (persistName == null ? "[null]" : persistName) + "; " );
		buffer.append( "Class: " + (clazz == null ? "[null]" : clazz.getSimpleName()) + "; " );
		buffer.append( "Constructor: " + (constructor==null?"[null]":constructor.toGenericString()) + "; " );
		buffer.append( "Set Field Map: " + (setFieldMap==null?"[null]":setFieldMap.toString()) + "; " );
		buffer.append( "Get Field Map: " + (getFieldMap==null?"[null]":getFieldMap.toString()) + "; " );
		return buffer.toString();
	}

	/**
	 * Adds a child to this node and reports on the success
	 * 
	 * @param child The child to add
	 * @return true if the child was added successfully; false otherwise.
	 */
	public boolean addChild( final InfoNode child )
	{
		child.parent = this;
		return children.add( child );
	}

	/**
	 * Adds the child at the specified index, or at the end if the index isn't
	 * valid.
	 * 
	 * @param index the index of the child node after which to insert the node
	 * @param child The child node to insert
	 */
	public boolean addChild( final int index, final InfoNode child )
	{
		if( index < 0 || index >= children.size() )
		{
			return addChild( child );
		}
		else
		{
			child.parent = this;
			children.add( index, child );
			return true;
		}
	}

	/**
	 * Removes the specified child tree node
	 * 
	 * @param child The child tree node to be removed
	 * @return true if the child was removed successfully; false otherwise.
	 */
	public boolean removeChild( final InfoNode child )
	{
		return children.remove( child );
	}

	/**
	 * Removes all the children from the node
	 */
	public void removeAllChildren()
	{
		children.clear();
	}

	/**
	 * @return the parent tree node
	 */
	public InfoNode getParent()
	{
		return parent;
	}

	/**
	 * @return the number of children this node has
	 */
	public int getChildCount()
	{
		return children.size();
	}

	/**
	 * @return List of children tree nodes
	 */
	public List< InfoNode > getChildren()
	{
		return Collections.unmodifiableList( children );
	}

	/**
	 * Returns the child at the specified index, or null if the index is invalid
	 * 
	 * @param index the index for the child to be returned
	 * @return the child at the specified index, or null if the index is invalid
	 */
	public InfoNode getChild( int index )
	{
		return children.get( index );
	}

	/**
	 * Moves the child at the specified index by the specified amount
	 * 
	 * @param index The index of the node to move
	 * @param amount The amount to move the node. For example, -1 mean move the
	 *        node to a lower index, or +1 moves the node to a higher index.
	 */
	public void moveChild( int index, int amount )
	{
		if( index + amount >= 0 && index + amount < children.size() )
		{
			final InfoNode child = children.remove( index );
			children.add( index + amount, child );
		}
	}

	/**
	 * @return true is this node is a leaf (i.e. has no children); false
	 *         otherwise
	 */
	public boolean hasChildren()
	{
		return children.size() == 0;
	}
	
	/**
	 * @return true if the node is a root node; false otherwise
	 */
	public boolean isRootNode()
	{
		return nodeType == NodeType.ROOT_NODE;
	}

	/**
	 * @return true if the node is a compound node; false otherwise
	 */
	public boolean isCompoundfNode()
	{
		return nodeType == NodeType.COMPOUND_NODE;
	}

	/**
	 * @return true if the node is a leaf node; false otherwise
	 */
	public boolean isLeafNode()
	{
		return nodeType == NodeType.LEAF_NODE;
	}

	/**
	 * Returns the first index of the specified node
	 * 
	 * @param node The node for which to find the index
	 * @return the first index of the specified node or -1 if the node is not found
	 */
	public int getIndexOfChild( final InfoNode node )
	{
		int index = 0;
		for( InfoNode child : children )
		{
			if( child.equals( node ) ) { return index; }
			++index;
		}
		return -1;
	}

	/**
	 * Returns true if the specified node is a child of this node; false
	 * otherwise
	 * 
	 * @param node The node to check
	 * @return true if the specified node is a child of this node; false otherwise
	 */
	public boolean containsChild( final InfoNode node )
	{
		for( InfoNode child : children )
		{
			if( child.equals( node ) ) { return true; }
		}
		return false;
	}

	/**
	 * Returns true if the specified node is a descendant of this node; false
	 * otherwise
	 * 
	 * @param descendant The descendant to check
	 * @return true if the specified node is a descendant of this node; false otherwise
	 */
	public boolean containsDescendant( final InfoNode descendant )
	{
		return recursiveContainsDescendant( descendant, this );
	}

	/*
	 * Recursive search for the specified descendant. Returns true if the
	 * subtree contains the specified descendant; false otherwise.
	 * 
	 * @param descendant The descendant to find
	 * 
	 * @param node The root node of the subtree
	 * 
	 * @return true if the subtree contains the specified descendant; false
	 * otherwise.
	 */
	private boolean recursiveContainsDescendant( final InfoNode descendant, final InfoNode node )
	{
		for( final InfoNode child : node.getChildren() )
		{
			if( child.equals( descendant ) )
			{
				return true;
			}
			else if( recursiveContainsDescendant( descendant, child ) ) { return true; }
		}
		return false;
	}

	/**
	 * Returns the TreeNode(s) that contain(s) the specified node. Searches
	 * through the subtree of this node. If this node contains that specified
	 * node, then it is added to the return list.
	 * 
	 * @param node The node to find in the subtree of this node
	 * @return A list of TreeNode(s) that contain the specified node.
	 * @see #getNodeFromDescendants(Object)
	 */
	public List< InfoNode > getNode( final InfoNode node )
	{
		List< InfoNode > nodes = null;
		if( this.equals( node ) )
		{
			nodes = new ArrayList< InfoNode >();
			nodes.add( this );
		}
		else
		{
			nodes = getNodeFromDescendants( node );
		}
		return nodes;
	}

	/**
	 * Returns the TreeNode(s) that contain(s) the specified node. Searches
	 * through the subtree of this node. Does not add this node to the list even
	 * if this node contains that specified node.
	 * 
	 * @param node The node to find in the subtree of this node
	 * @return A list of TreeNode(s) that contain the specified node.
	 * @see #getNode(Object)
	 */
	public List< InfoNode > getNodeFromDescendants( final InfoNode node )
	{
		return recursiveGetNodeFromDescendants( node, this );
	}

	/*
	 * Performs a recursive search for the descendant in the subtree specified by the node.
	 * @param descendant The node to find in the subtree specified by #node
	 * @param node The node containing the subtree to search.
	 * @return a list of the nodes running from the descendant to the specified node
	 */
	private List< InfoNode > recursiveGetNodeFromDescendants( final InfoNode descendant, final InfoNode node )
	{
		List< InfoNode > descendants = new ArrayList< InfoNode >();
		for( InfoNode child : node.getChildren() )
		{
			if( child.equals( descendant ) )
			{
				descendants.add( child );
			}
			else
			{
				descendants.addAll( recursiveGetNodeFromDescendants( descendant, child ) );
			}
		}
		return descendants;
	}

	/**
	 * Returns the all the nodes in the subtree of which this node is the root.
	 * @return the all the nodes in the subtree of which this node is the root.
	 */
	public List< InfoNode > getSubtreeAsList()
	{
		return recursiveGetSubtreeAsList( this );
	}

	/*
	 * Recursively adds all the nodes in the subtree (node) to a list and returns it
	 * @param node The root of the current subtree
	 * @return List of tree nodes in the current subtree
	 */
	private List< InfoNode > recursiveGetSubtreeAsList( final InfoNode node )
	{
		List< InfoNode > descendants = new ArrayList< InfoNode >();
		for( final InfoNode child : node.getChildren() )
		{
			descendants.add( child );
			descendants.addAll( recursiveGetSubtreeAsList( child ) );
		}

		return descendants;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer();
		rep.append( nodeInfoToString() ).append( " {" );
		rep.append( (parent != null ? parent : "root") ).append( "}: [ " );
		for( int i = 0; i < children.size(); ++i )
		{
			rep.append( children.get( i ).nodeInfoToString() ).append( (i < children.size() - 1 ? ", " : "") );
		}
		rep.append( " ]" );
		return rep.toString();
	}

	/**
	 * @return a string representation of the subtree
	 */
	public String treeToString()
	{
		return subtreeToString( this );
	}

	/*
	 * Creates a string representation from the subtree
	 * @param node The root node of the subtree
	 * @return a string representation from the subtree
	 */
	private String subtreeToString( final InfoNode node )
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( node.toString() ).append( Constants.NEW_LINE );
		for( final InfoNode child : node.getChildren() )
		{
			buffer.append( child.subtreeToString( child ) );
		}

		return buffer.toString();
	}
	
	public String simpleTreeToString()
	{
		return simpleTreeToString( this, 0 );
	}

	/*
	 * Creates a string representation from the subtree
	 * @param node The root node of the subtree
	 * @return a string representation from the subtree
	 */
	private String simpleTreeToString( final InfoNode node, final int level )
	{
		final StringBuffer buffer = new StringBuffer();
		String space = "";
		for( int i = 0; i < level; ++i )
		{
			space += "  ";
		}
		buffer.append( space );
		buffer.append( node.getPersistName() );
		buffer.append( ": field_name=" + node.getFieldName() );
		buffer.append( ", value=" + node.getValue() );
		buffer.append( ", class=" + node.getClazz() );
		buffer.append( ", type=" + node.getNodeType().toString() );
		buffer.append( ", children=" + node.getChildCount() );
		buffer.append( Constants.NEW_LINE );
		for( final InfoNode child : node.getChildren() )
		{
			buffer.append( child.simpleTreeToString( child, level+1 ) );
		}

		return buffer.toString();
	}
	
	/**
	 * Copies the entire tree from the specified node down.
	 * @param node The node from which to copy the tree
	 * @return The copied tree
	 */
	public static InfoNode getCopy( final InfoNode node )
	{
		// copy the node data (not the children, yet)
		final InfoNode copiedNode = InfoNode.copyNodeData( node );
		
		// now copy the children (recursively)
		for( InfoNode child : node.getChildren() )
		{
			copiedNode.addChild( getCopy( child ) );
		}
		return copiedNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public InfoNode getCopy()
	{
		return getCopy( this );
	}

	/**
	 * Represents the types of nodes
	 */
	public enum NodeType {
		ROOT_NODE, COMPOUND_NODE, LEAF_NODE;
	}
}
