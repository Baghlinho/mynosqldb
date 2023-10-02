#!/bin/bash

num_nodes=$1
pwd
if [[ ! $num_nodes =~ ^[1-9][0-9]*$ ]]
then
  echo "Number of nodes should be a positive integer"
  exit 1
fi

docker image inspect nosqlnode:latest > /dev/null 2>&1
if [ $? -ne 0 ]
then
  docker build -t nosqlnode ..
  sleep 10
fi

docker network inspect cluster > /dev/null 2>&1
if [ $? -ne 0 ]; then
  docker network create --subnet=10.1.4.0/28 cluster
fi

docker container inspect bootstrap > /dev/null 2>&1
if [ $? -eq 0 ]
then
  docker start bootstrap
else
  docker run -d -p 8000:8080 \
    --name bootstrap \
    --network cluster \
    --ip 10.1.4.10 \
    -e NODE_ID=0 -e NUM_NODES="$num_nodes" -e BOOTSTRAP=yes \
    nosqlnode
  sleep 3
fi

sleep 2

for ((i = 1; i <= num_nodes; i++))
do
  docker container inspect worker_"$i" > /dev/null 2>&1
  if [ $? -eq 0 ]
  then
      docker start worker_"$i"
  else
    docker run -d -p $((8000 + i)):8080 \
      --name worker_"$i" \
      --network cluster \
      --ip 10.1.4.1"$i" \
      -e NODE_ID="$i" \
      nosqlnode
  fi
done

echo "The cluster is ready with $num_nodes worker nodes."