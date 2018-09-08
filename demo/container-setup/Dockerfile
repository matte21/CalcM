# Use Ubuntu 12.04 as the base image because it is the Ubuntu distro the SIB version used in this container was tested on
FROM ubuntu:12.04

# Install sudo because some installation scripts need it and vim because it's useful to look at files content to do some troubleshooting in the container. dbus 
# is needed by the redsib.
RUN apt-get update && \
    apt-get install -y sudo vim dbus-x11 && \
    apt-get clean 

# Install packages needed by redsibd. By reading the redsibd README they're not needed, but without them the redsibd won't start
RUN apt-get update && \
    apt-get install -y autoconf libtool libexpat1-dev uuid-dev libdbus-1-dev libdbus-glib-1-dev librdf0 librdf0-dev gtk-doc-tools debhelper && \
    apt-get clean

# Install Java 8 as the study room application components are written in Java 8
RUN apt-get update && \
    apt-get install -y python-software-properties && \
    add-apt-repository -y ppa:webupd8team/java && \
    apt-get update && \
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get clean

# Set working directory to calc-demo, where all the files needed will be installed
WORKDIR /calcm-demo

# The demo runs different processes interacting with an instance of a redsib v0.9, hence we copy the .tar.gz associated with the aforementioned version of the 
# redsib
COPY resources/redsib_0.9.2_amd64.tar.gz .

# Extract the redsib v0.9 tar and remove it after the extraction has been completed
RUN tar -xzf redsib_0.9.2_amd64.tar.gz; rm redsib_0.9.2_amd64.tar.gz

WORKDIR /calcm-demo/redsib_0.9.2_amd64

# Install the SIB (install.sh is part of the redsibd tar archive we previously extracted). During the installation, the user is asked whether to install 
# virtuoso for storing triples persistently. This is important in production, but for this small demo we don't need it, thus the variable installVirtuoso
# contains the answer (n AKA no) to whether we want Virtuoso. We feed this answer to the install process with a pipeline (|).
ARG installVirtuoso=n
RUN echo $installVirtuoso | ./install.sh 

WORKDIR /calcm-demo

# In the demo we need the SIB to store the study room ontology and the dataset for the demo (study room instances, etc...). We can load all these items into
# the SIB with the ConfigurableJenaBasedOntologyLoader. Thus, we put it into the container the corrosponding archive, extract it, make its launch script 
# executable and remove the archive as we no longer need it.
COPY resources/ontologyLoader.tar.gz .
RUN tar -xzf ontologyLoader.tar.gz && \
    chmod u+x ./ontologyLoader/bin/ConfigurableJenaBasedOntologyLoader && \
    rm ontologyLoader.tar.gz   

# The sib start-up process is not trivial, thus it's embedded in the standalone script init-sib-and-aggregators.sh. Upon copying it to the container file 
# system we need it to be executable as it will be executed
COPY resources/init-sib-and-aggregators.sh .
RUN chmod u+x ./init-sib-and-aggregators.sh

# Set environment variables needed by the script init-sib-and-aggregators.sh
ENV sib_host=localhost
ENV sib_port=10010
ENV smartspace_name=demo-smartspace 
ENV ontology_and_demo_dataset_url=file:///calcm-demo/ontologyLoader/ontology-and-demo-dataset.owl

# Expose port where the SIB will listen for tcp connections
EXPOSE $sib_port

# When the container is started, the script init-sib-and-aggregators.sh is executed.
ENTRYPOINT ["/calcm-demo/init-sib-and-aggregators.sh"] 

######################################
#              v0.8.6                #
######################################