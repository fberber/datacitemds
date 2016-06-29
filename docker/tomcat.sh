#!/bin/sh
exec 2>&1

export JAVA_HOME /usr/lib/jvm/java-8-oracle
export CATALINA_HOME /usr/share/tomcat7
export CATALINA_BASE /var/lib/tomcat7

exec chpst -u app $CATALINA_HOME/bin/catalina.sh run
