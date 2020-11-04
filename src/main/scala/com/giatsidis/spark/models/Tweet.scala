package com.giatsidis.spark.models

case class Tweet(
                  id: Long,
                  user: User,
                  createdAt: String,
                  location: Option[String],
                  fullText: String,
                  cleanedText: String,
                  hashtags: List[String],
                  retweetCount: Int,
                  language: String,
                  sentiment: String,
                )
