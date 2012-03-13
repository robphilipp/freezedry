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



// TODO 1. 	Use constructor class, newInstance(...)
// TODO 2.	Annotate constructor and specify the names of the constructors fields to use. 
//			This allows the reader to know which constructor to use. Names and param types must match.
// TODO 3.  And for fields, they could be annotated with a method name and args if that method should 
//			be used to load the data into the class (as opposed to setting the filed directly).
// TODO 4. 	Field names can be mapped between their field name, and their display name.
// TODO 5. 	For generics, such as List<T>, the type of the contained object must be pushed into storage,
//			or must be supplied at load time based in the context of the code being loaded. This could even be through some rule. Could be that in an inheritance hierarchy, the class is picked based on a match of all the fields to load.

@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.TYPE, ElementType.FIELD } )
public @interface Persist {
	
	String persistenceName() default "";

	Class< ? > instantiateAs() default Null.class;
//	Class< ? >[] instantiateGenericsAs() default {};
	
	Class< ? > useNodeBuilder() default Null.class;
	
	public static class Null { } 
}
