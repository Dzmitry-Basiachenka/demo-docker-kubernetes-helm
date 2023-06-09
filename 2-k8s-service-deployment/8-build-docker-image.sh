#!/bin/sh

cd ../1-docker

docker build -t liakhaliaksandr/post-srv:1.0.1 ./post-srv/

docker images
