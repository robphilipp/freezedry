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

import org.freezedry.persistence.readers.JsonReader;
import org.freezedry.persistence.writers.JsonWriter;

public class JsonPersistence extends AbstractFileBasedPersistence {

	private JsonWriter jsonWriter;
	private JsonReader jsonReader;
	
	/*
	 * Creates the {@link JsonWriter} if it hasn't yet been instantiated, and returns it
	 * @return the {@link JsonWriter}
	 */
	protected JsonWriter getPersistenceWriter()
	{
		if( jsonWriter == null )
		{
			jsonWriter = new JsonWriter();
		}
		return jsonWriter;
	}

	/*
	 * Creates the {@link JsonReader} if it hasn't yet been instantiated, and returns it
	 * @return the {@link JsonReader}
	 */
	protected JsonReader getPersistenceReader()
	{
		if( jsonReader == null )
		{
			jsonReader = new JsonReader();
		}
		return jsonReader;
	}
}
