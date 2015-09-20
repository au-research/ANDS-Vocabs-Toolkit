# ANDS Vocabulary Services Toolkit

## What the toolkit does

The Toolkit implements a vocabulary repository, and provides an
interface to a vocabulary portal.

## Installation

### Configuration files

#### conf/toolkit.properties

This is the main configuration file for the Toolkit.

Make a copy of `conf/toolkit.properties.sample` and modify it to suit.

You will find another file `conf/version.properties` containing
version information. Do not modify this file. It will be updated when
you do an Ant build and copied into the generated WAR file.

#### logback.xml

This is the logging configuration for the Toolkit.

#### metadatarewritemap.conf

This is the configuration file for metadata rewriting.
It is in Windows INI format.
The supported sections are defined in
`src/main/java/au/org/ands/vocabs/toolkit/provider/transform/PropertyRewriterTransformProvider.java`,
where the values are put into the field `metadataToLookFor` in a
static initializer. For example, `dcterms:title`, `dcterms:description`.

#### ELDA Config template

This is the template used in the generation of configuration ('spec')
files as used by the ELDA library.
Please refer to `conf/ANDS-ELDAConfig-template.ttl.sample` for an
example.

#### ELDA XSL transform

This is the XSL transform used by SISSVoc to generate HTML pages.
Copy lda/resources/default/transform/ands-ashtml-sissvoc.xsl into
the resources/default/transform directory of your deployed instance
of SISSVoc.


### ant war

You need to use the Ant build script `build.xml` to build the WAR file.

The script assumes the existence of a directory `tomcatlib` containing
a JAR file for the Servlet API.

    mkdir tomcatlib
    cd tomcatlib
    cp /path/to/servlet/api/servlet-api.jar .
    cd ..
    ant war


### Tomcat deployment

The Toolkit should be deployed in Tomcat. (Deployment to other
containers may be possible; it has not been tested.)

TODO

## Toolkit functionality

The Toolkit provides the following functions:

* Get Toolkit information
  * System "health check"
* Get metadata
* Harvest
  * PoolParty
  * Sesame
  * SPARQL endpoint
  * File
* Transform
  * Create a Solr document for indexing
  * Create a tree of SKOS concepts in JSON format
  * Create a list of SKOS concepts in JSON format
* Import
  * Upload data into a Sesame repository
* Publish
  * Create a configuration file for use with the Elda library
* Unpublish
* Unimport
* Unharvest
* Backup
  * Download project exports from PoolParty

## Toolkit API

The API is accessed via HTTP.

## Technology

This section provides some background information on the technology
used to implement the Toolkit.

### Java

The Toolkit has been developed using Java 7, and a Java 7 Runtime
Environment is required. Specifically, the code uses try-with-resource
blocks, multi-catch exception blocks, and the new File IO API.

### JPA and Hibernate

The Toolkit uses JPA for database access.  Hibernate is used as the
implementation provider for JPA.  The JPA layer provides database
independence. Nevertheless, because ANDS software has for a long time
used MySQL, the MySQL JDBC driver is included, and the software has
only been tested using MySQL.

### JAX-RS and Jersey

The HTTP-based API is implemented as a set of restlets using
JAX-RS. Strictly speaking, the interface is not RESTful.
Jersey is used as the implementation provider for JAX-RS.

### Sesame

The OpenRDF Sesame libraries are used extensively to implement the
Toolkit features.

### Apache Commons

The Toolkit uses Apache Commons components. Currently, Configuration,
IO, and Lang (both versions 2 and 3) are used. (Commons IO is required
by Sesame, but it is also invoked directly from the Toolkit code.)

## Licence

The Toolkit is licensed under the Apache License, version 2.0. See
`LICENSE` for details.
