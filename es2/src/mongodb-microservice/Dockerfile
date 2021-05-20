FROM java:8-jre

ENV VERTICLE_FILE target/mongodb-microservice-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 8083

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar mongodb-microservice-fat.jar -cluster -conf docker.json"]
