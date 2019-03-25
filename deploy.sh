#!/usr/bin/env bash

DOCKER_LOGIN=`aws ecr get-login --no-include-email --region ap-northeast-1`
${DOCKER_LOGIN}

mvn clean package -Dmaven.test.skip=true -P qa -e

docker build -t company-service .

docker tag company-service:latest 533423936407.dkr.ecr.ap-northeast-1.amazonaws.com/company-service:latest

docker push 533423936407.dkr.ecr.ap-northeast-1.amazonaws.com/company-service:latest
