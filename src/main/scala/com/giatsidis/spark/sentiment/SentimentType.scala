package com.giatsidis.spark.sentiment

trait SentimentType

case object VERY_NEGATIVE extends SentimentType

case object NEGATIVE extends SentimentType

case object NEUTRAL extends SentimentType

case object POSITIVE extends SentimentType

case object VERY_POSITIVE extends SentimentType

case object NOT_UNDERSTOOD extends SentimentType