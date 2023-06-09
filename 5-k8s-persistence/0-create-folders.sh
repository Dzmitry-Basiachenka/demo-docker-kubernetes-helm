#!/bin/sh

cd

mkdir -p user-db
chmod 777 user-db
chown -R mongodb:mongodb user-db

mkdir -p post-db
chmod 777 post-db
chown -R mongodb:mongodb post-db
