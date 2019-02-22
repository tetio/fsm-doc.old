JAR
spring-boot:run -Pjar
i coma a variables d'entorn cal indicar que
PORTIC_ENV=springboot-local

WAR
clean package -Pwar
en l'entorn d'execició del tomcat/liberty
export PORTIC_ENV=local

-------------------

./mvnw install dockerfile:build

docker login --username=sergimaymi
docker push sergimaymi/fsm-doc


Liberty
======
export PORTIC_ENV=local
export WLP_DEBUG_ADDRESS=7778
./server debug