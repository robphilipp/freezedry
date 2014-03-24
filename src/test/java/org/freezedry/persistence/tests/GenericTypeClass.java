package org.freezedry.persistence.tests;

/**
 * Created by rob on 3/23/14.
 */
public class GenericTypeClass< T extends Number >
{
	private T number;
	private final double pi = 3.141592653;

	public GenericTypeClass( final T number )
	{
		this.number = number;
	}

	public T get()
	{
		return number;
	}
}
