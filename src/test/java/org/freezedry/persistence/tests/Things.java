package org.freezedry.persistence.tests;

/**
 * Created by rob on 3/21/14.
 */
public enum Things {
	THING_ONE( "one" ),
	THING_TWO( "2" ),
	THING_THREE( "3.14159" );

	private String name;
	Things( final String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
