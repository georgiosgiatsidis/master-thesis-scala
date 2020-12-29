name := "master-thesis-scala"

version := "0.1"

scalaVersion := "2.12.12"

resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/"
)

val configVersion = "1.4.1"
val coreNlpVersion = "3.6.0"
val sparkVersion = "3.0.1"
val jedisVersion = "3.3.0"
val slickVersion = "3.3.3"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % configVersion,
  "com.typesafe.slick" %% "slick" % slickVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion classifier "models",
  "mysql" % "mysql-connector-java" % "5.1.12",
  "org.apache.avro" % "avro" % "1.9.2",
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.4.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "redis.clients" % "jedis" % jedisVersion,
  "io.confluent" % "kafka-avro-serializer" % "5.5.0",
)
