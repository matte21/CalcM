#!/bin/bash

# Start a session message dbus that the redsib daemon and sib-tcp need to share
export $(dbus-launch)

# Start redsib daemon using a hash table in RAM to store triples. Ideally (and in production) a non-volatile storage such as Virtuoso is needed. This script is 
# to be used in a demo, thus we don't need persistent storage
redsibd --ram-hash &

# wait for redsib daemon to be up and running before invoking sib-tcp
sleep 3s

# listen for messages to the SIB on port 10010 (default port nbr if no args are provided)
sib-tcp