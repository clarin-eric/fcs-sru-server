ABSTRACT:
---------
This package implements the server-side part of the SRU/CQL protocol (SRU/S)
and conforms to SRU versin 1.1 and 1.2. The library will handle most of the
protocol related tasks for you and you'll only need to implement a few classes
to connect you search engine. However, the library will not save you from
doing your SRU/CQL homework (i.e. you'll need to have at least some
understanding of the protocol and adhere to the protocol sementics).

More Information about SRU/CQL:
  http://www.loc.gov/standards/sru/
Forthcoming standardization of SRU/CQL 2.0 (including description of 1.2)
  http://www.loc.gov/standards/sru/oasis/  


HOW TO USE:
-----------
You'll need to provide your own Servlet to initialize the SRUServer class
with a SRUServerConfig instance and dispatch the appropriate HTTP requets
to it. The SRUServerConfig requires an XML document which contains the
end-point configuration. It must conform to the "sru-server-config.xsd" schema
in the "src/main/resources/META-INF" directory.
Furthermore, you need to provide an implementation of the SRUSearchEngine
interface. This interface is the bridge between the SRU/CQL end-point and
your search engine. You'll need to implement at least the search() method.
Check the Java API docs for details.


HOW TO BUILD:
-------------
Since the Java CQL parse is not available from a official Maven repository,
you'll need to install it to your local repository using the following command:
$ mvn install:install-file  -DgroupId=org.z3950.zing -DartifactId=cql-java \
      -Dversion=1.7 -Dpackaging=jar -Dfile=cql-java.jar -DgeneratePom=true
      
Then you can build the library with the following command:
$ mvn package

And install the artifact in your own repository with the following command:
$ mvn install

Create Java API documentation for this package with the following command:
$ mvn javadoc:javadoc
