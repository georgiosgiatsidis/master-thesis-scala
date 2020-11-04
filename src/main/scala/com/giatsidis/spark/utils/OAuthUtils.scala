package com.giatsidis.spark.utils

import com.giatsidis.spark.Config

object OAuthUtils {
  def init() = {
    System.setProperty("twitter4j.oauth.consumerKey", Config.consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", Config.consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", Config.accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", Config.accessTokenSecret)
  }
}
