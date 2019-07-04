FROM openjdk:11-jre-slim

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED" \
    TZ=Europe/Zurich

# Add an aare user to run our application so that it doesn't need to run as root
RUN adduser --disabled-password --gecos "" --gid 0 --shell /bin/sh aare
WORKDIR /home/aare

RUN mkdir /app-data
ADD entrypoint.sh entrypoint.sh
RUN chown aare:root entrypoint.sh /app-data && chmod 775 entrypoint.sh /app-data
USER aare

ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 8080

ADD aare-*.jar /app.jar
