#!/usr/bin/env bash

mvn clean
mvn clean package -Dmaven.test.skip=true -Plocal -e
docker build -t company-service .
docker run -p 8081:8081 company-service --spring.profiles.active=local
