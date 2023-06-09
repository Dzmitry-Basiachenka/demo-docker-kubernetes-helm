#!/usr/bin/env bash

docker build -t liakhaliaksandr/user-srv:1.0.0 ./user-srv/
docker build -t liakhaliaksandr/post-srv:1.0.0 ./post-srv/

docker images
