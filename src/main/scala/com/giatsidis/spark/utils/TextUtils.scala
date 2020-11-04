package com.giatsidis.spark.utils

object TextUtils {
  def cleanText(input: String): String = {
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
  }
}
