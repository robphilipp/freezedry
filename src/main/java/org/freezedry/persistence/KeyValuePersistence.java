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
package org.freezedry.persistence;

import org.freezedry.persistence.keyvalue.KeyValueBuilder;
import org.freezedry.persistence.readers.KeyValueReader;
import org.freezedry.persistence.writers.KeyValueWriter;

public class KeyValuePersistence extends AbstractFileBasedPersistence {

	private KeyValueReader keyValueReader;
	private KeyValueWriter keyValueWriter;
	
	/**
	 * Sets the {@link KeyValueBuilder} that is used by the writer and reader to create 
	 * and parse the key-value pairs from the persistence source
	 * @param builder The {@link KeyValueBuilder} used by the writer and reader to create 
	 * and parse the key-value pairs from the persistence source
	 */
	public void setKeyValueBuilder( final KeyValueBuilder builder )
	{
		getPersistenceWriter().setBuilder( builder );
		getPersistenceReader().setBuilder( builder );
	}
	
	/**
	 * @return The {@link KeyValueBuilder} used by the writer and reader to create 
	 * and parse the key-value pairs from the persistence source
	 */
	public KeyValueBuilder getKeyValueWriterBuilder()
	{
		return getPersistenceWriter().getBuilder();
	}
	
	/**
	 * @return The {@link KeyValueBuilder} used by the writer and reader to create 
	 * and parse the key-value pairs from the persistence source
	 */
	public KeyValueBuilder getKeyValueReaderBuilder()
	{
		return getPersistenceReader().getBuilder();
	}
	
	/**
	 * The separator used between the elements of the key. NOT to be confused with the separator
	 * between the key and value.
	 * @param separator The separator used between the elements of the key
	 */
	public void setKeySeparator( final String separator )
	{
		getPersistenceWriter().setKeyElementSeparator( separator );
		getPersistenceReader().setKeyElementSeparator( separator );
	}
	
	/**
	 * @return The separator used between the elements of the key
	 */
	public String getKeySeparator()
	{
		return getPersistenceWriter().getKeyElementSeparator();
	}
	
	/**
	 * The separator used between the key and the value. NOT to be confused with the separator
	 * between the elements of the key.
	 * @param separator The separator used between the key and the value.
	 */
	public void setKeyValueSeparator( final String separator )
	{
		getPersistenceWriter().setKeyValueSeparator( separator );
		getPersistenceReader().setKeyValueSeparator( separator );
	}
	
	/**
	 * @return The separator used between the key and the value.
	 */
	public String getKeyValueSeparator()
	{
		return getPersistenceWriter().getKeyValueSeparator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.AbstractPersistence#getImplementationSpecificWriter()
	 */
	@Override
	protected KeyValueWriter getPersistenceWriter()
	{
		if( keyValueWriter == null )
		{
			keyValueWriter = new KeyValueWriter();
		}
		return keyValueWriter;
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.AbstractPersistence#getImplementationSpecificReader()
	 */
	@Override
	protected KeyValueReader getPersistenceReader()
	{
		if( keyValueReader == null )
		{
			keyValueReader = new KeyValueReader();
		}
		return keyValueReader;
	}
}
