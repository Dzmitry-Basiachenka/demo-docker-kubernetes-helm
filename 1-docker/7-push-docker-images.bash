#!/usr/bin/env bash

docker login

docker push liakhaliaksandr/user-srv:1.0.0
docker push liakhaliaksandr/post-srv:1.0.0
