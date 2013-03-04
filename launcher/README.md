Monnet Launcher Tool
====================

Executes an OSGi component like it was a command line script. The script must implement the 
interface `eu.monnetproject.framework.launcher.Command`. To execute the command simply start 
an OSGi container with the system property `exec.mainClass` and optionally `exec.args` set.

An example using (OSGi Run Maven Plugin)[http://github.com/monnetproject/maven-plugins/osgirun] 

   mvn osgirun:run -Dexec.mainClass=eu.monnetproject.App -Dexec.args="blah blah"

This tool is based on BeInformed's [OSGi System State plugin](http://github.com/beinformed/osgitest)
