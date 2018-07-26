# ConfigurableJenaBasedOntologyLoader

This is an Eclipse Java project for a Jena-based ontology loader. It's configurable as you specify the SIB IP address and port, the smart space name and the file where the ontology is defined as input parameters when invoking the executable.

The project contains the ontologyLoader.zip file (in the directory build/distributions) that contains the scripts and jar files necessary to run the ontology loader (download this zip if you only care about running the ontology loader without building it and if you are not interested in the source code). The same zip contains a README.txt that gives you all the details you need to run the ontology loader.

In case you want to mess with the code or build the project yourself, you have to download the project (or clone/fork this repo) and import it through Eclipse. There's a gradle wrapper, so the build process is straightforward. Just open a terminal and change the working directory to that of the project, and run "./gradlew distZip".
