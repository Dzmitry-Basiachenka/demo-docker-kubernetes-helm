#!/usr/bin/env bash

cd post-srv
java -jar -Dspring.profiles.active=local build/libs/post-service-0.0.1-SNAPSHOT.jar
