#!/bin/bash

# Start a session message dbus that the redsib daemon and sib-tcp need to share
export $(dbus-launch)

# Start redsib daemon using a hash table in RAM to store triples. Ideally (and in production) a non-volatile storage such as Virtuoso is needed. This script is 
# to be used in a demo, thus we don't need persistent storage. Notice that the variable $smartspace_name MUST have been already defined (for instance in the 
# Dockerfile). 
redsibd --ram-hash "$smartspace_name" &

# Wait for redsib daemon to be up and running before invoking sib-tcp
sleep 3s

# Listen for messages to the SIB on port 10010 (default port nbr if no args are provided)
sib-tcp &

# Wait for the SIB TCP server to be up and running
sleep 10s

# Load the ontology and the test dataset into the SIB. These variables MUST have been already set (for instance in the Dockerfile).
./ontologyLoader/bin/ConfigurableJenaBasedOntologyLoader "$sib_host" "$sib_port" "$smartspace_name" "$ontology_and_demo_dataset_url" &