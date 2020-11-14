package com.giatsidis.spark

import slick.jdbc.JdbcProfile
import slick.lifted.{ForeignKeyQuery, ProvenShape}

object Tables extends JdbcProfile {

  import api._

  case class User(id: Long, screenName: String, profileImageURLHttps: String)

  class Users(tag: Tag) extends Table[User](tag, "users") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def screenName: Rep[String] = column[String]("screen_name")

    def profileImageURLHttps: Rep[String] = column[String]("profile_image_url_https")

    def * : ProvenShape[User] =
      (id, screenName, profileImageURLHttps) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  case class Tweet(id: Long, fullText: String, location: Option[String] = None, sentiment: String, createdAt: String, userId: Long)

  class Tweets(tag: Tag) extends Table[Tweet](tag, "tweets") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def fullText: Rep[String] = column[String]("full_text")

    def location: Rep[String] = column[String]("location")

    def sentiment: Rep[String] = column[String]("sentiment")

    def createdAt: Rep[String] = column[String]("created_at")

    def userId: Rep[Long] = column[Long]("user_id")

    def user: ForeignKeyQuery[Users, User] =
      foreignKey("user_fk", userId, users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * : ProvenShape[Tweet] =
      (id, fullText, location.?, sentiment, createdAt, userId) <> (Tweet.tupled, Tweet.unapply)
  }

  val tweets = TableQuery[Tweets]

}