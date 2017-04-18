FROM openshift/wildfly-101-centos7

ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8
ENV TZ "Europe/Zurich"

ADD docker/jdbc-driver/mysql-connector-java-5.1.40-bin.jar /wildfly/modules/com/mysql/main/
ADD docker/jdbc-driver/module.xml /wildfly/modules/com/mysql/main/

ADD target/aare-server.war /wildfly/standalone/deployments/
ADD docker/standalone.xml /wildfly/standalone/configuration/

CMD $STI_SCRIPTS_PATH/run
