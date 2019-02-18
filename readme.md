./mvnw install dockerfile:build

docker login --username=sergimaymi
docker push sergimaymi/fsm-doc