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
```
DB_HOST = "localhost"
DB_PORT = "3306"
DB_NAME = "thesis"
DB_USERNAME = "root"
DB_PASSWORD = "root"
```

