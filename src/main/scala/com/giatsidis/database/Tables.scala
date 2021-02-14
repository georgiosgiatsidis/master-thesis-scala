package com.giatsidis.database

import java.time.Instant

import com.giatsidis.database.models.{Hashtag, Term, Tweet, User}
import slick.jdbc.JdbcProfile
import slick.lifted.{ForeignKeyQuery, ProvenShape}

object Tables extends JdbcProfile {

  import api._

  class Users(tag: Tag) extends Table[User](tag, "Users") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def screenName: Rep[String] = column[String]("screenName")

    def profileImageURLHttps: Rep[String] = column[String]("profileImageUrlHttps")

    def * : ProvenShape[User] =
      (id, screenName, profileImageURLHttps) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  class Tweets(tag: Tag) extends Table[Tweet](tag, "Tweets") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def fullText: Rep[String] = column[String]("fullText")

    def location: Rep[Option[String]] = column[Option[String]]("location")

    def sentiment: Rep[String] = column[String]("sentiment")

    def sentimentML: Rep[String] = column[String]("sentimentML")

    def createdAt: Rep[Instant] = column[Instant]("createdAt")

    def userId: Rep[Option[Long]] = column[Option[Long]]("userId")

    def user: ForeignKeyQuery[Users, User] =
      foreignKey("tweets_ibfk_1", userId, users)(_.id.?, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[Tweet] =
      (id, fullText, location, sentiment, sentimentML, createdAt, userId) <> (Tweet.tupled, Tweet.unapply)
  }

  val tweets = TableQuery[Tweets]

  class Hashtags(tag: Tag) extends Table[Hashtag](tag, "Hashtags") {
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

    def tweet: ForeignKeyQuery[Tweets, Tweet] = foreignKey("tweethashtags_ibfk_1", tweetId, tweets)(_.id)

    def hashtag: ForeignKeyQuery[Hashtags, Hashtag] = foreignKey("tweethashtags_ibfk_2", hashtagId, hashtags)(_.id)

    def * : ProvenShape[TweetHashtag] =
      (id, tweetId, hashtagId) <> (TweetHashtag.tupled, TweetHashtag.unapply)

  }

  class Terms(tag: Tag) extends Table[Term](tag, "Terms") {
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val name = column[String]("name")
    val keywords = column[String]("keywords")

    def * : ProvenShape[Term] = (id.?, name, keywords) <> (Term.tupled, Term.unapply)
  }

  val terms = TableQuery[Terms]

  case class TweetTerm(id: Int, tweetId: Long, termId: Int)

  class TweetTerms(tag: Tag) extends Table[TweetTerm](tag, "TweetTerms") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def tweetId: Rep[Long] = column[Long]("tweetId")

    def termId: Rep[Int] = column[Int]("termId")

    def tweet: ForeignKeyQuery[Tweets, Tweet] = foreignKey("tweetterms_ibfk_1", tweetId, tweets)(_.id)

    def hashtag: ForeignKeyQuery[Terms, Term] = foreignKey("tweetterms_ibfk_2", termId, terms)(_.id)

    def * : ProvenShape[TweetTerm] =
      (id, tweetId, termId) <> (TweetTerm.tupled, TweetTerm.unapply)
  }

}
