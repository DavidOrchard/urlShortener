// Copyright (C) 2013 David Orchard
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package urlShortener

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import collection.mutable.Map

/** hashData represents the url and clicks for a hash
 *
 *  public class for unit testing
 *
 *  @constructor Create a new hashData instance from a URL.
 *
 *  @param urlParam String url for the hashData.
 *
 */

class hashData(urlParam: String) {

  /** singleton wrapper around SimpleDateFormat for performance and memory optimization
   *
   *  @constructor Create the hashDataDateFormatter singleton
   */
  object hashDataDateFormatter {
    val dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH zzz");

    /** format the specified date into hour format
     *
     *  @param date Date the requested date
     *
     *  @return String the formatted date
     */
    def format(date: Date): String = {
      dateFormatter.format(date)
    }
  }

  /** Get the URL for this hashData
   *
   *  @return String the url
   */
  def getUrl(): String = urlParam

  private val dateFormatter = hashDataDateFormatter
  private var clicks = Map[String, Int]()

  /** Get the clicks structure for this hashData
   *
   *  @return Map[String, Int] a Map of date and hour strings to clicks
   */
  def getClicks(): Map[String, Int] = clicks

  /** Increment click count for given date
   *
   *  For testing purposes only
   *
   */
  private def incrementCountForDate(date: Date) = {
    val hour = dateFormatter.format(date)
    this.synchronized {
      val entry = clicks.get(hour) match {
        case None    => (hour -> 1)
        case Some(x) => (hour -> (x + 1))
      }
      clicks += entry
    }
  }

  /** Increment click count for now
   *
   */
  def incrementCountForNow(): Unit = {
    incrementCountForDate(Calendar.getInstance().getTime())
  }
}
