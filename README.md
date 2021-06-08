# Scala Spark Service

## Dependencies

* Scala 2.12.12
* OpenJDK 1.8 - Java is required to compile and execute Scala code.
* SBT 1.4.4 - build/rest scripts
* Apache Spark
	* Spark Streaming - Collects live tweets.
    * Spark Core - Provides distributed task dispatching and applies transformations (e.g. map, filter, reducer, join).
	* Spark MLlib - creates an ML model using Naive Bayes Classifier and predicts the sentiment of collected tweets.
* Stanford CoreNLP 3.6.0 - An alternative approach to predict the sentiment of collected tweets.
* Apache Kafka - used for real-time streams of data and real-time analysis.
    * Avro - Provides a fast serialization format.
* MySQL 5.7 - Stores collected/classified tweets.
* Redis - Publishes collected/classified tweets; used by the web socket service.
* Docker / docker-compose - all services run on docker containers to avoid problems from different local configurations.

## IntelliJ IDEA (optional)

1. Download [IntelliJ](https://www.jetbrains.com/idea/download). from the official website
2. Install the Scala plugin:  **File > Settings > Plugins.**
3. Create a new project: **Projects > New Project from existing sources**.
Then select **Import project from existing model > sbt**.
After that, rename the project and make sure that the selected JDK version is 1.8. Moreover, check that the Scala version is 2.12.* and that the sbt version used is 1.4.4. Finally click finish.
4. You are ready to go.

## Configuration and Execution

### Twitter OAuth credentials
Twitter credentials are needed to retrieve live tweets.
Keep in mind that this is a critical step and without these credentials, Spark will not be able to connect to Twitter or retrieve tweets using Twitter Streaming API. Add your unique credentials at [`application.conf`](src/main/resources/application.conf#L2-5).
If you do not have any, create a new Twitter App by visiting [Twitter Developer Page](https://dev.twitter.com/apps).

```
CONSUMER_KEY = ""
CONSUMER_SECRET = ""
ACCESS_TOKEN = ""
ACCESS_TOKEN_SECRET = ""
```

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

To stop hold Ctrl+C or run
```sh
docker-compose stop
```

replace *stop* with *down* to destroy all containers, images etc.

Note: Docker-compose file contains all the necessary dependencies (Spark, MySQL, Redis, Kafka, Zookeeper and Schema Registry).
It also, starts a Spark standalone cluster (1 master and 2 workers) automatically by running the [`Main.scala`](src/main/scala/com/giatsidis/spark/Main.scala).

##### Note
This project is part of my MSc thesis.