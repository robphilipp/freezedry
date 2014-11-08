![develop](https://travis-ci.org/robphilipp/freezedry.svg?branch=develop)
freezedry
=========

Java persistence framework: no need for binding files, no-arg constructors, or annotations.

-----------------------------------------------------------------------------------
   Website Has Documentation
-----------------------------------------------------------------------------------
Please see the Website (http://robphilipp.github.io/freezedry) for documentation.

1. The "Overview Guide" page describes the framework and its usage.
2. The "Quick Start" page describes the various files and how to get up and 
   running quickly.

-----------------------------------------------------------------------------------
   Contiuous Integration
-----------------------------------------------------------------------------------
I use cloudbees for CI. You can view the results of master and develop branch
builds at https://robphilipp.ci.cloudbees.com/.

-----------------------------------------------------------------------------------
   Changes from version 0.2.8 to version 0.2.9
-----------------------------------------------------------------------------------
1. freezedry jar is now an OSGi bundle
2. Added pax:exam integration tests (felix by default)
  a. "mvn clean test" runs unit tests
  b. "mvn clean verify" runs integration tests
3. Fixed bug in key-value persistence for maps of maps

-----------------------------------------------------------------------------------
   Changes from version 0.2.7 to version 0.2.8
-----------------------------------------------------------------------------------
1. Added serialization capabilities that allows objects to be marshaled and
   unmarshaled to and from XML, JSON, or key-value pairs. A standard Java object
   serializer is thrown in for good measure. (This are the serializer classes
   used in diffusive (http://github.com/robphilipp/diffusive).
2. Fixed MapRenderer so that the map's keys can take on values other than the
   regex [a-zA-Z_0-9] would allow. Now any printable character can be used as
   a map key.
3. Added test cases for serialization and persistance.

-----------------------------------------------------------------------------------
   Changes from version 0.2.5 to version 0.2.7
-----------------------------------------------------------------------------------
Updates to the build and the use of FreezeDry, and added an object difference utility.

1. Converted from Ant build to maven.
   FreezeDry is now available through maven central.

```
<dependency>
    <groupid>com.closure-sys</groupid>
    <artifactid>freezedry</artifactid>
    <version>0.2.7</version>
</dependency></code></pre>
```

2. Added ObjectDifferenceCalculator that compares two objects of the same type and
   shows which fields are different. Uses FreezeDry's key-value pair writer to generate
   the comparison.

-----------------------------------------------------------------------------------
   Changes from version 0.2.4 to version 0.2.5
-----------------------------------------------------------------------------------
Mainly improved the use of FreezeDry for serialization of objects that have an
associated NodeBuilder.

1. Added the ability to persist objects that have an associated NodeBuilder as root
   objects as described by the NodeBuilder. For example, when persisting an ArrayList
   as a member of another object, the ArrayList uses the CollectionNodeBuilder to
   create and parse the InfoNode objects. In version 0.2.4, however, when the
   ArrayList was the root object (i.e. not a member of another object) then the
   ArrayList was persisted as if it were any other object, rather than by using
   the CollectionNodeBuilder. Version 0.2.5 fixes this.

2. Changed the NodeBuilder interface to have two additional methods that override the
   createInfoNode(Class, Object, String) method and the createObject(Class, Class,
   InfoNode) method. These new methods are called when an object has an associated
   NodeBuilder and is a root object.

-----------------------------------------------------------------------------------
   Changes from version 0.2.3 to version 0.2.4
-----------------------------------------------------------------------------------
Mainly bug fix related to inheritance, completed TODO for setting the persist
names of map elements, added ability to specify that a field is ignored (i.e.
not persisted), and added test cases.

1. When using @PersistMap with one or both of the options:
    * keyPersistName
    * valuePersistName
   the java.lang.Map was not reconstructed properly from its persisted state. I
   finished a "TODO" that allows you to use the persistence name for the key and value.
2. Fixed bug where the fields from the ancestor objects (parent and parent's
   parent, etc) where not getting persisted.
3. Added an ignore option to the @Persist annotation that takes a boolean value of true 
   or false. If the option is set to true, the FreezeDry will ignore that field.
4. Added test cases to the unit tests.

-----------------------------------------------------------------------------------
   Changes from version 0.2.2 to version 0.2.3
-----------------------------------------------------------------------------------
Mainly provided fixes that allow FreezeDry to be use more effectively as a serialization
engine. Mostly this work was done for the Diffusive project.

1. Provided the capability to persist arrays as top level objects. For example, if 
   you would like to directly persist an int[][] (i.e. not a field in some other 
   class) you can now do this.
2. Fixed a minor issue where primitive types were being automatically wrapped in their 
   Java types when specifying their names as a type attribute or as the
   element names. For example, a double element had its type attribute or name
   set to Double instead of double.

-----------------------------------------------------------------------------------
   Changes from version 0.2.1 to version 0.2.2
-----------------------------------------------------------------------------------
1. Removed the restriction that the persisted class requires a no-arg constructor.
2. Fixed bug the prevented Java types from being persisted as the top level element.
3. Added a Char/char node builded (was missing before, oversight)
4. Updated code so that "static final" fields are persisted by default, and are never 
   set based on the persisted value (if the value is persisted). static final fields
   take their value from the class' source code.	
5. Small bug fixes.

-----------------------------------------------------------------------------------
   Changes from version 0.2.0 to version 0.2.1
-----------------------------------------------------------------------------------
1. Fixed a small bug in the KeyValuePersistence class. The method to return the 
   PersistenceBuider returned it from the PersistenceWriter. Modifying the mapping 
   for the PersistenceRenderers in that PersistenceBuilder only applied to the builder 
   for that writer. It did not modify the builder for the PersistenceReader. The 
   PersistenceBuilder for the reader then used a different mapping, in some cases 
   causing errors. This was a disconnect. Renamed the method, and now the user must 
   explicitly set both (until I can find a better solution).

-----------------------------------------------------------------------------------
   Changes from version 0.1.0 to version 0.2.0
-----------------------------------------------------------------------------------
1. In preparation for DynamoDB persistence, added a key-value reader and writer and 
   all the code necessary to convert the semantic model into key-value pairs, and parse 
   key-value pairs into the semantic model. 
   
   The key-value code, found in the "keyvalue" package also contains Renderers and 
   Decorators that allow a fair amount of easy customization. The Renderers are similar 
   to the NodeBuilder except that they work on converting between InfoNodes and key-value 
   pairs. 
   
   The Decorators provide a convenient way to decorate items. For example, if a key or 
   value is a String, then by default it would be decorated (and undecorated) by surrounding
   the string with quotes, by the StringDecorator. Renderers and Decorators can be 
   extended to alter the behavior of the default FreezeDry. For example, the 
   FlatteningCollectionRenderer changes the default behavior of the CollectionRenderer.
   
2. Added Persistence classes as a convenience. Now you can persist objects and reconstitute
   them from their persisted form through the use of the Persistence classes. For example,
   there is an XmlPersistence, a JsonPersistence, and a KeyValuePersistence. In the
   FreezeDryCloud project (to be added shortly) there are Persistence classes to persist
   directly to S3 buckets. These can easily be extended. And the previous approach of
   using the PersistenceEngine is still available.
   
3. Renamed the persistence readers and writers to PersistenceReader and PersistenceWriter
   to avoid confusion with java.io.Reader and java.io.Writer.
   
4. Changed the signature of the PersistenceReader to use java.io.Reader instead of the
   InputStream. And changed the signature of the PersistenceWriter to use the java.io.Writer
   instead of the PrintWriter.
   
5. Changed the InfoNode to implement Copyable< InfoNode > and provide a deep copy of the
   semantic model (i.e. the subtree of the InfoNode on which the copy is requested).
   
6. Refactored code to clean it up and remove repeated code or generalize patterns. 

7. Fixed several bugs related to annotations, arrays of arrays.

8. Added test cases.
