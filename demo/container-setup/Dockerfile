# Use Ubuntu 12.04 as the base image because it is the Ubuntu distro the SIB version used in this container was tested on
FROM ubuntu:12.04

# Install sudo because some installation scripts need it and vim because it's useful to look at files content to do some troubleshooting in the container. dbus 
# is needed by the redsib
RUN apt-get update && apt-get install -y sudo dbus-x11 vim 

# Install packages needed by redsibd. By reading the redsibd README they're not needed, but without them the redsibd won't start
RUN apt-get install -y autoconf libtool libexpat1-dev uuid-dev libdbus-1-dev libdbus-glib-1-dev librdf0 librdf0-dev gtk-doc-tools debhelper

# Set working directory to calc-demo, where all the files needed will be installed
WORKDIR /calcm-demo

# The demo runs different processes interacting with an instance of a redsib v0.9, hence we copy the .tar.gz associated with the aforementioned version of the 
# redsib
COPY resources/redsib_0.9.2_amd64.tar.gz .

# Extract the redsib v0.9 tar and remove it after the extraction has been completed
RUN tar -xzf redsib_0.9.2_amd64.tar.gz; rm redsib_0.9.2_amd64.tar.gz

# The sib start-up process is not trivial, thus it's embedded in the standalone script launch-sib.sh. Upon copying it to the container file system we need it
# to be executable as it will be executed
COPY resources/launch-sib.sh .
RUN chmod u+x ./launch-sib.sh

WORKDIR /calcm-demo/redsib_0.9.2_amd64

# Install the SIB (install.sh is part of the redsibd tar archive we previously extracted). During the installation, the user is asked whether to install 
# virtuoso for storing triples persistently. This is important in production, but for this small demo we don't need it, thus the variable installVirtuoso
# contains the answer (n AKA no) to whether we want Virtuoso. We feed this answer to the install process with a pipeline (|).
ARG installVirtuoso=n
RUN echo $installVirtuoso | ./install.sh 

# Expose port where the SIB will listen for tcp connections
EXPOSE 10010

# Start up the SIB when the container is started 
ENTRYPOINT ["/calcm-demo/launch-sib.sh"] 

