FROM java:8-jre

ENV VERTICLE_FILE target/mysql-microservice-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 8084

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar mysql-microservice-fat.jar -cluster -conf docker.json"]
