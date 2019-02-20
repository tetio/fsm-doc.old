./mvnw install dockerfile:build

docker login --username=sergimaymi
docker push sergimaymi/fsm-doc


Liberty
======
export PORTIC_ENV=local
export WLP_DEBUG_ADDRESS=7778
./server debug