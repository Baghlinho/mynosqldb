#!/bin/sh

docker stop $(docker ps -aq -f network=cluster)