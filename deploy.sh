#!/usr/bin/env bash

DOCKER_LOGIN=`aws ecr get-login --no-include-email --region ap-northeast-1`
${DOCKER_LOGIN}

mvn clean package -Dmaven.test.skip=true -P qa -e

docker build -t company-service .

docker tag company-service:latest 533423936407.dkr.ecr.ap-northeast-1.amazonaws.com/company-service:latest

docker push 533423936407.dkr.ecr.ap-northeast-1.amazonaws.com/company-service:latest

aws ecs update-service --cluster Services --region ap-northeast-1 --service company-service --task-definition company-service:4 --desired-count 1 --force-new-deployment

