package com.giatsidis.spark

import com.giatsidis.spark.models.Tweet
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

object Tables extends JdbcProfile {

  import api._

  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweets") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def fullText: Rep[String] = column[String]("full_text")

    def location: Rep[String] = column[String]("location")

    def sentiment: Rep[String] = column[String]("sentiment")

    def createdAt: Rep[String] = column[String]("created_at")

    def * : ProvenShape[Tweet] =
      (id, fullText, location.?, sentiment, createdAt) <> (Tweet.tupled, Tweet.unapply)
  }

}