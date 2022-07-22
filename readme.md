Example project for running a Jetty server with JDK 17, Spring, Auth0 and NOT 
using spring-b**t.


## Building

Best to set the JDK you run the project with in your IDE to 17, though it 
should work withanything back to Java 8 (Gradle will download the correct 
JDK version based on the toolchain specification in build.gradle).


## Auth0 setup

The prod code and the functional tests integrate against Auth0.

The functional test suite exists as a separate Gradle source-set from unit
tests.

Before you can run the functional tests, you must setup Auth0 and configure
the Spring properties as per [auth0.md](./doc/auth0.md).

Once configured, the functonal tests can be run with the `funcTest` task.




