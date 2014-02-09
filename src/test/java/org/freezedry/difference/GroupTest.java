package org.freezedry.difference;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 * [Description]
 *
 * @author rphilipp
 *         1/31/14, 3:38 PM
 */
public class GroupTest
{
	@Test
	public void testGetName() throws Exception
	{
		final String groupName = "this is a test of group name";
		final Group group = new Group( groupName );
		Assert.assertEquals( "Group names", groupName, group.getName() );
	}

	@Test
	public void testGetParent() throws Exception
	{
		final Group root = new Group( "parent" );
		final Group child = new Group( "child" );
		root.addChild( child );
		Assert.assertEquals( "Parent returned for child", root, child.getParent() );
	}

	@Test
	public void testAddChild() throws Exception
	{
		final Group root = new Group( "parent" );
		final Group child = new Group( "child" );
		root.addChild( child );
		Assert.assertEquals( "Number of children", 1, root.getChildren().size() );
		Assert.assertEquals( "Child", child, root.getChildren().get( 0 ) );
	}

	@Test
	public void testGetChildren() throws Exception
	{
		testAddChild();
	}

	@Test
	public void testRemoveChild() throws Exception
	{
		final Group root = new Group( "parent" );
		final Group child = new Group( "child" );
		root.addChild( child );
		Assert.assertEquals( "Number of children", 1, root.getChildren().size() );
		Assert.assertEquals( "Child", child, root.getChildren().get( 0 ) );
		root.removeChild( child );
		Assert.assertEquals( "Number of children", 0, root.getChildren().size() );
	}

	@Test
	public void testAddValue() throws Exception
	{
		final Group group = new Group( "leaf" );
		group.addValue( "matrix[0][0]", "00" );
		group.addValue( "matrix[0][1]", "01" );
		group.addValue( "matrix[0][2]", "01" );
		group.addValue( "matrix[0][3]", "02" );
		group.addValue( "matrix[0][4]", "02" );
		group.addValue( "matrix[0][5]", "02" );

		final Map< String, Set< String >> values = group.getValues();
		Assert.assertEquals( "Number of elements", 3, values.size() );
		Assert.assertEquals( "Number of 00", 1, values.get( "00" ).size() );
		Assert.assertEquals( "Number of 01", 2, values.get( "01" ).size() );
		Assert.assertEquals( "Number of 02", 3, values.get( "02" ).size() );
	}

	@Test
	public void testGetValues() throws Exception
	{
		testAddValue();
	}

	@Test
	public void testRemoveValue() throws Exception
	{
		final Group group = new Group( "leaf" );
		group.addValue( "matrix[0][0]", "00" );
		group.addValue( "matrix[0][1]", "01" );
		group.addValue( "matrix[0][2]", "01" );
		group.addValue( "matrix[0][3]", "02" );
		group.addValue( "matrix[0][4]", "02" );
		group.addValue( "matrix[0][5]", "02" );

		final Map< String, Set< String >> values = group.getValues();
		Assert.assertEquals( "Number of elements", 3, values.size() );
		Assert.assertEquals( "Number of 00", 1, values.get( "00" ).size() );
		Assert.assertEquals( "Number of 01", 2, values.get( "01" ).size() );
		Assert.assertEquals( "Number of 02", 3, values.get( "02" ).size() );

		group.removeValue( "matrix[0][1]", "01" );
		Assert.assertEquals( "Number of 00", 1, values.get( "00" ).size() );
		Assert.assertEquals( "Number of 01", 1, values.get( "01" ).size() );
		Assert.assertEquals( "Number of 02", 3, values.get( "02" ).size() );

		group.removeValue( "matrix[0][2]", "01" );
		Assert.assertEquals( "Number of 00", 1, values.get( "00" ).size() );
		Assert.assertEquals( "Number of 01", null, values.get( "01" ) );
		Assert.assertEquals( "Number of 02", 3, values.get( "02" ).size() );
	}

	@Test
	public void testEquivalentValues() throws Exception
	{
		final Group group = new Group( "leaf" );
		group.addValue( "matrix[0][0]", "00" );
		group.addValue( "matrix[0][1]", "01" );
		group.addValue( "matrix[0][2]", "01" );
		group.addValue( "matrix[0][3]", "02" );
		group.addValue( "matrix[0][4]", "02" );
		group.addValue( "matrix[0][5]", "02" );

		final Group referenceGroup = new Group( "leaf" );
		referenceGroup.addValue( "matrix[0][4]", "01" );
		referenceGroup.addValue( "matrix[0][3]", "01" );
		referenceGroup.addValue( "matrix[0][2]", "02" );
		referenceGroup.addValue( "matrix[0][1]", "02" );
		referenceGroup.addValue( "matrix[0][0]", "02" );
		referenceGroup.addValue( "matrix[0][5]", "00" );

		Assert.assertTrue( "Groups equivalent", group.equivalentValues( referenceGroup ) );
	}

	@Test
	public void testIsLeaf() throws Exception
	{
		final Group root = new Group( "parent" );
		final Group child = new Group( "child" );
		root.addChild( child );
		Assert.assertFalse( "Root is not leaf", root.isLeaf() );
		Assert.assertTrue( "Child is leaf", child.isLeaf() );
	}

	@Test
	public void testFindGroup() throws Exception
	{
		final Group root = new Group( "name[*][*][*][*]" );
		for( int i = 0; i < 10; ++i )
		{
			final Group levelOne = new Group( "name[" + i + "][*][*][*]" );
			root.addChild( levelOne );
			for( int j = 0; j < 10; ++j )
			{
				final Group levelTwo = new Group( "name[" + i + "][" + j + "][*][*]" );
				levelOne.addChild( levelTwo );
				for( int k = 0; k < 10; ++k )
				{
					final Group levelThree = new Group( "name[" + i + "][" + j + "][" + k + "][*]" );
					levelTwo.addChild( levelThree );
					for( int n = 0; n < 10; ++n )
					{
						levelThree.addValue( "name[" + i + "][" + j + "][" + k + "][" + n + "]", Integer.toString( i * j * k + n ) );
					}
				}
			}
		}

		final Group group = root.findGroup( "name[1][2][3][*]" );
		Assert.assertNotNull( "Find name[1][2][3][*]", group );
		final Map< String, Set< String >> values = group.getValues();
		for( int n = 0; n < 10; ++n )
		{
			final String value = Integer.toString( 1 * 2 * 3 + n );
			final String key = "name[1][2][3][" + n + "]";
			Assert.assertTrue( key, values.containsKey( value ) );
			Assert.assertTrue( key, values.get( value ).contains( key ) );
		}
	}
}
