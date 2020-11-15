package com.giatsidis.spark.models

import java.time.Instant

case class Tweet(
                  id: Long,
                  fullText: String,
                  location: Option[String],
                  sentiment: String,
                  createdAt: Instant,
                  //                  hashtags: List[String],
                  user: User,
                )
