/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package src.main.scala

import java.sql.Timestamp
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.streaming.GroupStateTimeout
import org.apache.spark.sql.streaming.GroupState
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
 * Counts words in UTF8 encoded, '\n' delimited text received from the network over a
 * sliding window of configurable duration. Each line from the network is tagged
 * with a timestamp that is used to determine the windows into which it falls.
 *
 * Goal:
 *   Analize/Identify the behavior of the watermark when using flatMapGroupsWithState
 *   with a timeout
 *
 * Usage: 
 *   WordCountWithStateAndTimeout <hostname> <port> [<watermark duration>]
 *   [<state timeout>]
 *
 * <hostname> and <port> 
 *    describe the TCP server that Structured Streaming
 *    would connect to receive data.
 * <watermark duration> 
 *    Gives the size of watermark window, specified as integer number of seconds.
 *    Default value: 300  
 * <state timeout> 
 *    Gives the timeout of the flatMapWithState output, specified as integer number of seconds.
 *    Default value: 320
 */
case class WordKey(timestamp: Timestamp, word: String)

object WordCountWithStateAndTimeout {
  var timeout: Integer = 0

  def wordCounterByTimestamp(key: WordKey,
                      values: Iterator[Row],
                      state: GroupState[Integer]) = {

      if (state.exists && state.hasTimedOut) {
        val result = state.get 
        state.remove()

        Iterator((key.timestamp, key.word, result))
      } else {
        val counter: Integer = if (state.exists) {
          values.size + state.get
        } else {
          values.size
        }
        state.update(counter)
        state.setTimeoutTimestamp(key.timestamp.getTime, s"$timeout seconds")

        Iterator.empty
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.err.println("Usage: WordCountWithStateAndTimeout <hostname> <port>" +
        " [<window duration in seconds>]")
      System.exit(1)
    }

    val host = args(0)
    val port = args(1).toInt
    val windowSize = if (args.length >= 3 ) args(2).toInt else 300
    timeout = if (args.length >= 4 ) args(3).toInt else 320

    println("PARAMETERS:")
    println("   host       : " + host)
    println("   port       : " + port)
    println("   windowSize : " + windowSize)
    println("   timeout    : " + timeout)

    val spark = SparkSession
      .builder
      .appName("WordCountWithStateAndTimeout")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._

    // Create DataFrame representing the stream of input lines from connection to host:port
    val lines = spark.readStream
      .format("socket")
      .option("host", host)
      .option("port", port)
      .option("includeTimestamp", false)
      .load()

    val df = lines.as[String].map( value =>
      (value.split(",")(0), value.split(",")(1))
    ).toDF("timestamp", "contend")

    val words = df.as[(Timestamp, String)].flatMap(line => 
      line._2.split(" ").map(word => (word, line._1))
    ).toDF("word", "timestamp")

    // // Group the data by window and word and compute the count of each group
    val windowedCounts = words
      .withWatermark("timestamp", s"$windowSize seconds")
      .groupByKey( r => {
          WordKey(r.getTimestamp(1), r.getString(0))
        }
    ).flatMapGroupsWithState(
      OutputMode.Append(),
      GroupStateTimeout.EventTimeTimeout)(wordCounterByTimestamp)

    val query = windowedCounts.writeStream
      .outputMode("append")
      .format("console")
      .option("truncate", "false")
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .start()

    query.awaitTermination()
  }
}