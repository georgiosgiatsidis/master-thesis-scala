package com.giatsidis.spark

import java.time.Instant

import com.giatsidis.spark.database.Hashtag
import com.giatsidis.spark.database.Tweet
import com.giatsidis.spark.database.User
import slick.jdbc.JdbcProfile
import slick.lifted.{ForeignKeyQuery, ProvenShape}

object Tables extends JdbcProfile {

  import api._

  class Users(tag: Tag) extends Table[User](tag, "users") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def screenName: Rep[String] = column[String]("screen_name")

    def profileImageURLHttps: Rep[String] = column[String]("profile_image_url_https")

    def * : ProvenShape[User] =
      (id, screenName, profileImageURLHttps) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweets") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def fullText: Rep[String] = column[String]("full_text")

    def location: Rep[Option[String]] = column[Option[String]]("location")

    def sentiment: Rep[String] = column[String]("sentiment")

    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def userId: Rep[Option[Long]] = column[Option[Long]]("user_id")

    def user: ForeignKeyQuery[Users, User] =
      foreignKey("user_fk", userId, users)(_.id.?, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[Tweet] =
      (id, fullText, location, sentiment, createdAt, userId) <> (Tweet.tupled, Tweet.unapply)
  }

  val tweets = TableQuery[Tweets]

  class Hashtags(tag: Tag) extends Table[Hashtag](tag, "hashtags") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def text: Rep[String] = column[String]("text")

    def * : ProvenShape[Hashtag] =
      (id, text) <> (Hashtag.tupled, Hashtag.unapply)
  }

  val hashtags = TableQuery[Hashtags]

  case class TweetHashtag(id: Int, tweetId: Long, hashtagId: Int)

  class TweetHashtags(tag: Tag) extends Table[TweetHashtag](tag, "TweetHashtags") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def tweetId: Rep[Long] = column[Long]("tweetId")

    def hashtagId: Rep[Int] = column[Int]("hashtagId")

    def tweet: ForeignKeyQuery[Tweets, Tweet] = foreignKey("author_fk", tweetId, tweets)(_.id)

    def hashtag: ForeignKeyQuery[Hashtags, Hashtag] = foreignKey("book_fk", hashtagId, hashtags)(_.id)

    def * : ProvenShape[TweetHashtag] =
      (id, tweetId, hashtagId) <> (TweetHashtag.tupled, TweetHashtag.unapply)

  }

}