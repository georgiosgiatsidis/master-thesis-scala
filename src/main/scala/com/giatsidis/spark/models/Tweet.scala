package com.giatsidis.spark.models

import java.time.Instant

case class Tweet(
                  id: Long,
                  fullText: String,
                  location: Option[String],
                  sentiment: String,
                  sentimentML: String,
                  createdAt: Instant,
                  hashtags: List[Hashtag],
                  user: User,
                  categories: List[Term],
                )
