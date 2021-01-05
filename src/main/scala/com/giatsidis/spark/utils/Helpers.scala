package com.giatsidis.spark.utils

import com.giatsidis.database.models.Term
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

  def getKeywordsFromTerms(terms: List[Term]): List[String] = {
    terms.flatMap(term => term.keywords.split(","))
  }
}
