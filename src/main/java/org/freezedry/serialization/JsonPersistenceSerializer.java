/*
 * Copyright 2012 Robert Philipp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freezedry.serialization;

import org.freezedry.persistence.JsonPersistence;

/**
 * Serializes objects into JSON and deserializes JSON back into objects using the
 * FreezeDry framework's {@link JsonPersistence} engine.
 * 
 * @author Robert Philipp
 */
public class JsonPersistenceSerializer extends PersistenceSerializer {

	/**
	 * Constructs an object serializer that uses the FreezeDry persistence framework to 
	 * serialize the objects into and out of JSON
	 */
	public JsonPersistenceSerializer()
	{
		super( new JsonPersistence() );
	}
	
}
