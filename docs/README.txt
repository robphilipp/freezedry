-----------------------------------------------------------------------------------
   Wiki Has Documentation
-----------------------------------------------------------------------------------
Please see the Wiki for documentation.

1. The "User Guide" page describes the framework and its usage.
2. The "Quick Start" page describes the various files, which ones to use, how 
   to get running in Eclipse

-----------------------------------------------------------------------------------
   File Overview and Directories
-----------------------------------------------------------------------------------
The directories provide three options for downloading FreezeDry. All options provide
a "log4j.xml" configuration file that may be used if you want to use logging.
1. A JAR containing all the dependencies conveniently packaged into one JAR file. 
   This is found in the directory "single_jar".
2. A set of JAR files. The FreezeDry_vx.x.x.jar contains the FreezeDry framework. The
   additional JAR files are the require dependencies.
3. The source code and an Ant build.xml file for building the source code, and creating
   the release package. The source code comes in a zip file, and starting with version
   0.2.0 also has the directories for individual file downloads.

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