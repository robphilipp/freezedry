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
package org.freezedry.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.freezedry.persistence.tree.InfoNode;

/**
 * Annotation for modifying the persistence (marshaling and unmarshaling) of {@link Map} objects.
 * The {@link PersistMap} annotation differs from the {@link PersistCollection} annotation by defining
 * default names for the {@link Map.Entry}, its keys, and its values. The semantic model for the {@link Map}
 * is to create an Entry {@link InfoNode} for each map entry, and hang a key {@link InfoNode} and a value 
 * {@link InfoNode} off of that Entry node. This means, though, that we need to be able to distinguish which
 * {@link InfoNode} is the key and which one is the value.
 *  
 * @author Robert Philipp
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface PersistMap {

	public static final String ENTRY_PERSIST_NAME = "MapEntry";
	public static final String KEY_PERSIST_NAME = "Key";
	public static final String VALUE_PERSIST_NAME = "Value";
	
	String entryPersistName() default ENTRY_PERSIST_NAME;
	
	String keyPersistName() default KEY_PERSIST_NAME;
	Class< ? > keyType() default Null.class;
	
	String valuePersistName() default VALUE_PERSIST_NAME;
	Class< ? > valueType() default Null.class;
	
	public static class Null { } 
}
