#!/bin/bash

mkdir -p logs

mvn -pl api-gateway spring-boot:run > logs/api-gateway.log 2>&1 &
mvn -pl notification-client spring-boot:run > logs/notification-client.log 2>&1 &
mvn -pl auth-service spring-boot:run > logs/auth-service.log 2>&1 &
mvn -pl user-service spring-boot:run > logs/user-service.log 2>&1 &
mvn -pl resource-service spring-boot:run > logs/resource-service.log 2>&1 &
mvn -pl notification-service spring-boot:run > logs/notification-service.log 2>&1 &

echo "All services started 🚀"
