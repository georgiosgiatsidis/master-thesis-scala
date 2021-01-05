package com.giatsidis.spark.database

import java.time.Instant

case class Tweet(
                  id: Long,
                  fullText: String,
                  location: Option[String] = None,
                  sentiment: String,
                  sentimentML: String,
                  createdAt: Instant,
                  userId: Option[Long] = None,
                )