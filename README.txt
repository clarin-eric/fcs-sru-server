ABSTRACT:
---------
This package implements the server-side part of the SRU/CQL protocol (SRU/S)
and conforms to SRU version 1.1 and 1.2. The library will handle most of the
protocol related tasks for you and you'll only need to implement a few classes
to connect you search engine. However, the library will not save you from
doing your SRU/CQL homework (i.e. you'll need to have at least some
understanding of the protocol and adhere to the protocol semantics).
Furthermore, you need to have at least some basic understanding of Java web
application development (Servlets in particular) to use this library.

More Information about SRU/CQL:
  http://www.loc.gov/standards/sru/
Forthcoming standardization of SRU/CQL 2.0 (includes description of 1.2)
  http://www.loc.gov/standards/sru/oasis/  


HOW TO USE:
-----------
The implementation is designed to make very minimal assumptions about the
environment it's deployed in. For interfacing with your search engine, you
need to implement the SRUSearchEngine interface. At minimum, you'll need
to implement at least the search() method. Please check the Java API
documentation for further details about this interface.
The SRUServer implements the SRU protocol an used your supplied search engine
implementation to talk to your search engine. The SRUServer is configured
using a SRUServerConfig instance. The SRUServerConfig reads an XML document,
which contains the (static) server configuration. It must conform to the
"sru-server-config.xsd" schema in the "src/main/resources/META-INF" directory.
You can either write your own Servlet implementation to drive the SRUServer or
can use supplied SRUServerServlet from the "de.clarin.sru.server.utils"
package. If you do so, your search engine needs to inherit from the abstract
class SRUSearchEngineBase. Check the Java API documentation (and the code) of
these classes for more information.
Of course, you can use frameworks like Spring or similar to assemble your
web application.
An usage example for this library is available from:
  http://clarin.ids-mannheim.de/downloads/clarin/
NB: you cannot deploy and use it, because it uses internal IDS services, but
it should give you a basic idea how to use the library. 


HOW TO BUILD:
-------------
1. Build the library with the following command:
$ mvn package

2a. Either install the artifact in your own repository with the following
    command, if you use Maven for you project:
$ mvn install

2b. Or use the "sru-server-$VERSION.jar" file created within the
    "target/" directory, if you do not use Maven for your project.

The Java API documentation is created within the "target/apidocs" directory
and is also zipped up into the "sru-server-$VERSION-javadoc.jar" archive.
