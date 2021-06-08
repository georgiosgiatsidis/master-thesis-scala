package com.giatsidis.spark

import com.giatsidis.spark.sentiment.mllib.MLlibSentimentAnalyzer
import com.giatsidis.spark.sentiment.stanford.StanfordSentimentAnalyzer
import org.apache.spark.sql.SparkSession

object Test {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder
      .master(sys.env.getOrElse("SPARK_MASTER", "local[*]"))
      .appName(this.getClass.getSimpleName)
      .getOrCreate()

    val model = MLlibSentimentAnalyzer.createNBModel(sparkSession.sparkContext)

    val list = List(
      "Probably the best buy this month",
      "Mr. Wonderful is smart, be like Mr. Wonderful and diversify your portfolio with some #Bitcoin",
      "wow very good, good luck on the project",
      "so the 8 million people who die from air pollution each year due to fossil fuels, and the environmental damage from oil spills were not enough to catalyze a renewable energy revolution. but #bitcoin will",
      "The current price of #eth is $1,590.90, an increase of +1.2%, a 24 hr volume of $29.41B, and a market cap of $182.88B",
      "Bro that's a life changing amount of money. Thanks for doing something like this and best of luck to whoever wins!",
      "I wanted to report that I just got the BTC! I am happy right now!",
      "Just got those BTC, fast and easy. Nice!",
      "Your ETH is gonna grow to 30k each in 5 years, that’s what’s gonna happen",
      "I think if oil etc is traded in BTC vs USD it will boom. I think that happens before Central Bank holds any.",
      "Bitcoin has been unstoppable. But it’s just he beginning of a whole new industry",
    )

    list.foreach(x => {
      println(x + " & " + StanfordSentimentAnalyzer.detectSentiment(x) + " & " + MLlibSentimentAnalyzer.detectSentiment(x, model)) + " \\\\ "
    })
  }
}
