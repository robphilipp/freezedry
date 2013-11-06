package org.freezedry.persistence.keyvalue.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.freezedry.persistence.containers.Pair;

public class KeyValueUtils {

	/**
	 * Strips the first key-element from each key in the specified {@link List} of key-value pairs. Returns
	 * a new list that contains the key-value pairs for which the keys have been stripped of their first element.
	 * @param keyValues The list of key-value pairs
	 * @param keyElementSeparator The separator used between the elements of the key.
	 * @return a new list that contains the key-value pairs for which the keys have been stripped of their first element.
	 */
	public static List< Pair< String, String > > stripFirstKeyElement( final List< Pair< String, String > > keyValues, final String keyElementSeparator )
	{
		final List< Pair< String, String > > strippedKeyValues = new ArrayList<>();
		for( Pair< String, String > pair : keyValues )
		{
			// strip the first element from the key
			final String strippedKey = stripFirstKeyElement( pair.getFirst(), keyElementSeparator );
			
			// add the new key and the old value to the list of stripped keys
			strippedKeyValues.add( new Pair< String, String >( strippedKey, pair.getSecond() ) );
		}
		
		return strippedKeyValues;
	}
	
	/**
	 * 
	 * @param key
	 * @param separator
	 * @return
	 */
	public static String stripFirstKeyElement( final String key, final String separator )
	{
		// grab the elements of the key
		final String[] elements = key.split( Pattern.quote( separator ) );
		
		// create a key that has the first key element stripped off
		final StringBuffer strippedKey = new StringBuffer();
		for( int i = 1; i < elements.length; ++i )
		{
			strippedKey.append( elements[ i ] );
			if( i < elements.length-1 )
			{
				strippedKey.append( separator );
			}
		}
		
		return strippedKey.toString();
	}
	
	/**
	 * Returns the first key element for the specified key and separator
	 * @param key The key from which to pull the first element
	 * @param separator The key element separator
	 * @return the first key element for the specified key and separator
	 */
	public static String getFirstKeyElement( final String key, final String separator )
	{
		return key.split( Pattern.quote( separator ) )[ 0 ];
	}
	

}
