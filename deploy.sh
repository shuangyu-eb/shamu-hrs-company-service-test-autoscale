#!/usr/bin/env bash

DOCKER_LOGIN=`aws ecr get-login --no-include-email --region ap-northeast-1`
${DOCKER_LOGIN}

mvn clean package -Dmaven.test.skip=true -P dev -e

docker build -t jiaqi-test .

docker tag jiaqi-test:latest 533423936407.dkr.ecr.ap-northeast-1.amazonaws.com/jiaqi-test:latest

docker push 533423936407.dkr.ecr.ap-northeast-1.amazonaws.com/jiaqi-test:latest

