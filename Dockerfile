# FROM bde2020/spark-scala-template:3.0.1-hadoop3.2
# ENV SPARK_APPLICATION_MAIN_CLASS com.giatsidis.spark.Main
FROM bde2020/spark-submit:3.0.1-hadoop3.2
RUN mkdir -p /app
COPY ./target/scala-2.12/master-thesis-scala-assembly-0.1.jar /app/master-thesis.jar
ENV SPARK_APPLICATION_JAR_LOCATION /app/master-thesis.jar
ENV SPARK_APPLICATION_MAIN_CLASS com.giatsidis.spark.Main