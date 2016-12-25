FROM jboss/wildfly:10.1.0.Final

ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

USER root

ADD target/aare-server.war /opt/jboss/wildfly/standalone/deployments/
ADD docker/standalone.xml /opt/jboss/wildfly/standalone/configuration/

RUN mkdir -p /opt/jboss/wildfly/modules/com/mysql/main/
ADD docker/jdbc-driver/mysql-connector-java-5.1.40-bin.jar /opt/jboss/wildfly/modules/com/mysql/main/
ADD docker/jdbc-driver/module.xml /opt/jboss/wildfly/modules/com/mysql/main/

USER jboss
