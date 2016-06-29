FROM datacite/java-tomcat
MAINTAINER Martin Fenner "mfenner@datacite.org"

ENV HANDLE_VERSION "7.3.1"

# Install handle client
RUN mkdir -p /root/.handle
VOLUME [ "/root/.handle/" ]

RUN wget http://www.handle.net/hs-source/hcj$HANDLE_VERSION.tar.gz && \
    tar -zxvf hcj$HANDLE_VERSION.tar.gz && \
    mvn install:install-file -Dfile=hcj$HANDLE_VERSION/handle-client.jar -DgroupId=handle.net -Dversion=$HANDLE_VERSION -Dpackaging=jar -DgeneratePom=true -DartifactId=hcj

# Download dependencies
WORKDIR /home/app
ADD pom.xml /home/app/pom.xml
RUN mvn dependency:go-offline

# Add Runit script for tomcat
# RUN mkdir /etc/service/tomcat
# ADD docker/tomcat.sh /etc/service/tomcat/run

# Run additional scripts during container startup (i.e. not at build time)
# Process templates using ENV variables
# Compile project
RUN mkdir -p /etc/my_init.d
COPY docker/70_templates.sh /etc/my_init.d/70_templates.sh
COPY docker/80_install.sh /etc/my_init.d/80_install.sh
COPY docker/90_run.sh /etc/my_init.d/90_run.sh

# Copy source files
ADD . /home/app/

# CMD ["/usr/share/tomcat7/bin/catalina.sh", "run"]

EXPOSE 8080
