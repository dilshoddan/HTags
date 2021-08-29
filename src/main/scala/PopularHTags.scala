package miu.bdt

import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import scalax.chart.module.ChartFactories

import scala.swing.Orientation


object PopularHTags {

  var ds = new org.jfree.data.category.DefaultCategoryDataset
  var chart = ChartFactories.BarChart(ds)

  def showChart(): Unit = {

    chart.orientation = Orientation.Horizontal
    chart.show()
  }

  def updateChart(data: (String, Int)): Unit = {
    val plainS = data._1.replaceAll("\n", "").replaceAll(" ", "")
    println("Hashtag: " + plainS + "   " + "Count: " + data._2)
    ds.addValue(data._2, data._1, "")
    //    chart.plot.setDataset(ds)

  }

  /** Makes sure only ERROR messages get logged to avoid log spam. */
  def setupLogging() = {
    import org.apache.log4j.{Level, Logger}
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)
  }

  /** Configures Twitter service credentials using twiter.txt in the main workspace directory. Use the path where you saved the authentication file */
  def setupTwitter() = {

    import scala.io.Source
    for (line <- Source.fromFile("data/twitter.txt").getLines) {
      val fields = line.split(" ")
      if (fields.length == 2) {
        System.setProperty("twitter4j.oauth." + fields(0), fields(1))

      }
    }
  }

  // Main function where the action happens

  def main(args: Array[String]) {

    showChart()

    // Configure Twitter credentials using twitter.txt
    setupTwitter()

    // Set up a Spark streaming context named "PopularHashtags" that runs locally using
    // all CPU cores and one-second batches of data
    val ssc = new StreamingContext("local[*]", "PopularHashtags", Seconds(1))

    // Get rid of log spam (should be called after the context is set up)
    setupLogging()

    // Create a DStream from Twitter using our streaming context
    val tweets = TwitterUtils.createStream(ssc, None)

    // Now extract the text of each status update into DStreams using map()
    val statuses = tweets.map(status => status.getText())

    // Blow out each word into a new DStream
    val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

    // Now eliminate anything that's not a hashtag
    val hashtags = tweetwords.filter(word => word.startsWith("#"))

    // Map each hashtag to a key/value pair of (hashtag, 1) so we can count them by adding up the values
    val hashtagKeyValues = hashtags.map(hashtag => (hashtag, 1))

    // Now count them up over a 5 minute window sliding every one second
    val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow((x, y) => x + y, (x, y) => x - y, Seconds(300), Seconds(6))


    // Sort the results by the count values
    val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))

    // Print the top 10
//    sortedResults.print
    sortedResults.foreachRDD(rdd => rdd.take(10).foreach(updateChart))

    // Set a checkpoint directory, and kick it all off
    ssc.checkpoint("/home/dan/test")
    ssc.start()

    ssc.awaitTermination()

  }

}
