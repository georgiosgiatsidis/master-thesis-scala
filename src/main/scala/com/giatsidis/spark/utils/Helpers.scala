package com.giatsidis.spark.utils

import org.apache.spark.rdd.RDD
import twitter4j.Status

object Helpers {
  def applyFilters(rdd: RDD[Status]): RDD[Status] = {
    rdd
      .filter(_.getLang == "en")
      .filter(!_.isRetweet)
      .filter(_.getHashtagEntities.toList.length < 5)
      .filter(_.getText.length > 20)
  }
}
