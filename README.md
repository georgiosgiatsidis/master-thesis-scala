# Scala Spark Service

## Dependencies

* OpenJDK 1.8
* Scala 2.12.12
* SBT 1.4.4
* Apache Spark
    * Spark Core
	* Spark Streaming
	* Spark MLlib
* Apache Kafka
    * Avro
* Stanford CoreNLP 3.6.0
* MySQL 5.7
* Redis
* Docker - docker-compose

## IntelliJ IDEA (optional)

1. Download [IntelliJ](https://www.jetbrains.com/idea/download). from the official website
2. Install the Scala plugin:  **File > Settings > Plugins.**
3. Create a new project: **Projects > New Project from existing sources**.
Then select **Import project from existing model > sbt**.
After that, rename the project and make sure that the selected JDK version is 1.8. Moreover, check that the Scala version is 2.12.* and that the sbt version used is 1.4.4. Finally click finish.
4. You are ready to go.

## Configuration

### Twitter OAuth credentials
Twitter credentials are needed to retrieve live tweets.
Keep in mind that this is a critical step and without these credentials, Spark will not be able to connect to Twitter or retrieve tweets using Twitter Streaming API. Add your unique credentials at [`application.conf`](src/main/resources/application.conf#L2-5).
If you do not have any, create a new Twitter App by visiting [Twitter Developer Page](https://dev.twitter.com/apps).

### MySQL
Add MySQL credentials at [`application.conf`](src/main/resources/application.conf).
```
DB_HOST = "localhost"
DB_PORT = "3307"
DB_NAME = "thesis"
DB_USERNAME = "root"
DB_PASSWORD = "root"
```

### Redis
Add Redis credentials at [`application.conf`](src/main/resources/application.conf).
```
REDIS_HOST = "localhost"
REDIS_PORT = "6380"
REDIS_CHANNEL = "app:tweets"
```

Add Redis config at [`application.conf`](src/main/resources/application.conf).

### Kafka
```
KAFKA_SERVERS="localhost:9092"
KAFKA_TOPIC="tweets"
AVRO_SCHEMA_REGISTRY_URL="http://localhost:8081"
```

### Docker
Run the docker enviroment via docker-compose.
```sh
docker-compose up --build
```

Note: Docker-compose file contains all the necessary dependencies (Spark, MySQL, Redis, Kafka, Zookeeper and Schema Registry).
It starts a Spark standalone cluster (1 master and 2 workers) automatically by running the [`Main.scala`](src/main/scala/com/giatsidis/spark/Main.scala).
