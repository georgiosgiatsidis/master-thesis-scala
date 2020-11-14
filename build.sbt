name := "master-thesis-scala"

version := "0.1"

scalaVersion := "2.12.12"

val configVersion = "1.4.1"
val coreNlpVersion = "3.6.0"
val sparkVersion = "3.0.1"
val jedisVersion = "3.3.0"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % configVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion classifier "models",
  "mysql" % "mysql-connector-java" % "5.1.12",
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.4.0",
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "redis.clients" % "jedis" % jedisVersion
)
