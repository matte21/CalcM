To run the ontology loader, run the script 
bin/ConfigurableJenaBasedOntologyLoader if you're on Linux/OS X
or bin/ConfigurableJenaBasedOntologyLoader.bat on Windows.

To display usage information, run the aforementioned scripts without
any arguments or with "-h" as the only argument.

When you run the script, there are four mandatory invocation arguments,
unless you're trying to display its usage information. These arguments 
are:

- SIB IP/host: The IP/host name of the machine where the SIB runs

- SIB port number: The port number the SIB is listening on.

- smart space name: The name of the smart space the ontology 
will be loaded into 

- ontology definition URL: The complete URL at which the ontology
is defined. In case this is a file on your machine (e.g. a .owl
file), notice that the URL must be complete: the protocol and 
the absolute path must be used:

file:///home/user1/Documents/ontology.owl ---- valid
/home/user1/Documents/ontology.owl ----------- invalid
	
 