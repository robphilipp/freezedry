package org.freezedry.persistence.tests;

/**
 *
 * Created by rob on 3/23/14.
 */
public class GenericTypeSubclass extends Number
{
	private final String myAdditionalParam;

	public GenericTypeSubclass( final String myParam )
	{
		this.myAdditionalParam = myParam;
	}

	public String getMyAdditionalParam()
	{
		return myAdditionalParam;
	}

	@Override
	public int intValue()
	{
		return 3;
	}

	@Override
	public long longValue()
	{
		return 3L;
	}

	@Override
	public float floatValue()
	{
		return 3.141592653F;
	}

	@Override
	public double doubleValue()
	{
		return 3.141592653;
	}
}
