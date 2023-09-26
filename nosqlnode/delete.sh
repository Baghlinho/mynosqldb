#!/bin/sh

docker rm $(docker ps -aq -f network=cluster)