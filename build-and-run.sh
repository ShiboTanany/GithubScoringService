#!/bin/bash

IMAGE_NAME="github-score-service"
CONTAINER_NAME="github-score-container"
PORT=8080
echo "Building Docker image..."
docker build -t $IMAGE_NAME .

if [ $? -ne 0 ]; then
  echo "Docker build failed, exiting."
  exit 1
fi

# Check if container is already running and remove it
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping and removing existing container..."
  docker stop $CONTAINER_NAME
  docker rm $CONTAINER_NAME
fi

echo "Running container $CONTAINER_NAME..."
docker run -d --name $CONTAINER_NAME -p $PORT:8080 $IMAGE_NAME

if [ $? -eq 0 ]; then
  echo "Application is running at http://localhost:$PORT"
else
  echo "Failed to start container."
  exit 1
fi
