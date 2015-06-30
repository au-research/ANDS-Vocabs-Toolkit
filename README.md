# ANDS Vocabulary Services Toolkit

## What the toolkit does

The toolkit implements a vocabulary repository, and provides an
interface to a vocabulary portal.

## Toolkit functionality

* Get toolkit information
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

