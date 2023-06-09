#!/usr/bin/env bash

cd user-srv
java -jar -Dspring.profiles.active=local build/libs/user-service-0.0.1-SNAPSHOT.jar
