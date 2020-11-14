package com.giatsidis.spark.models

case class Tweet(
                  id: Long,
                  fullText: String,
                  location: Option[String],
                  sentiment: String,
                  createdAt: String,
                  //                  hashtags: List[String],
                  //                  user: User,
                )
