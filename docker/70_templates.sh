#!/bin/sh
dockerize -template /home/app/docker/persistence.xml.tmpl:/home/app/src/main/resources/META-INF/persistence.xml
dockerize -template /home/app/docker/salt.properties.tmpl:/home/app/src/main/resources/META-INF/spring/salt.properties
dockerize -template /home/app/docker/database.properties.tmpl:/home/app/src/main/resources/META-INF/spring/database.properties
dockerize -template /home/app/docker/database.properties.test.tmpl:/home/app/src/test/resources/META-INF/spring/database.properties
dockerize -template /home/app/docker/handle.properties.tmpl:/home/app/src/main/resources/META-INF/spring/handle.properties
dockerize -template /home/app/docker/email.properties.tmpl:/home/app/src/main/resources/META-INF/spring/email.properties
dockerize -template /home/app/docker/xml-validator.properties.tmpl:/home/app/src/main/resources/META-INF/spring/xml-validator.properties

dockerize -template /home/app/docker/link.properties.tmpl:/home/app/src/main/webapp/WEB-INF/spring/link.properties

dockerize -template /home/app/docker/log4j.xml.tmpl:/home/app/src/main/resources/log4j.xml

# dockerize -template /home/app/docker/bugsnag.java.tmpl:/home/app/src/main/java/org/datacite/mds/util/Bugsnag.java
