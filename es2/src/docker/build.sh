#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo $DIR
docker build -t "assignment3-pt2/api-gateway" $DIR/../api-gateway
docker build -t "assignment3-pt2/mongodb-microservice" $DIR/../mongodb-microservice
docker build -t "assignment3-pt2/mysql-microservice" $DIR/../mysql-microservice
docker build -t "assignment3-pt2/authentication-microservice" $DIR/../authentication-microservice
docker build -t "assignment3-pt2/puzzle-microservice" $DIR/../puzzle-microservice


