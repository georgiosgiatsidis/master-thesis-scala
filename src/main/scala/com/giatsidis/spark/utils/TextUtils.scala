package com.giatsidis.spark.utils

import scala.io.Source

object TextUtils {
  val stopWordsList = Source.fromInputStream(getClass.getResourceAsStream("/" + "stopwords.txt")).getLines().toList

  def cleanText(input: String, removeStopWords: Boolean = true): String = {
    val result =
      input.toLowerCase()
        .replaceAll("\n", "")
        .replaceAll("rt\\s+@\\w+", "") // Retweets
        .replaceAll("\\s+@\\w+", "") // @tags
        .replaceAll("@\\w+", "")
        .replaceAll("\\s+#\\w+", "") // #hashtags
        .replaceAll("#\\w+", "")
        .replaceAll("(?:https?|http?)://[\\w/%.-]+", "") // urls
        .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
        .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
        .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
        .replaceAll("[^ 'a-zA-Z0-9,.?!]", "") // keep only chars in the list

    if (removeStopWords) {
      result.split("\\W+") // convert to array
        .filter(!stopWordsList.contains(_)) // remove stop words
        .fold("")((a, b) => a.trim + " " + b.trim).trim // trim
    }

    result
  }

  def remove4ByteChars(input: String): String = {
    input.replaceAll("[^\\u0000-\\uFFFF]", "");
  }

}
