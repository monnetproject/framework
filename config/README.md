Monnet Configuration Tool
=========================

This is a simple tool used to load configuration across all Monnet components.

Configuration can be obtained as follows:

   Properties prop = Configurator.getConfig("eu.monnetproject.app");
   props.getProperty("key1");

Configuration can be set by the following method

 1. A property from the environment using `System.getProperty`
   * For convenience this can also be set by calling `Configurator.setConfig`
 2. A file in the current working directory called, for example `load/eu.monnetproject.app.cfg`
 3. A file in a JAR on the classpath at `/load/eu.monnetproject.app.cfg`
 4. A file in a WAR at `/classes/load/eu.monnetproject.app.cfg`

