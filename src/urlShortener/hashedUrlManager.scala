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

import scala.actors.Actor
import scala.collection.mutable.{ Map, SynchronizedMap, HashMap }
import scala.collection.immutable.Map

/** An actor that manages the hashed URLs and their click counts
 *
 *  Nested actors are used for each method implementation to minimize blocking.
 *
 *  Some interesting implementation notes from http://www.artima.com/pins1ed/actors-and-concurrency.html
 *  As an example, imagine you wanted multiple actors to share a common mutable map. Since the map
 *  is mutable, the pure actors approach would be to create an actor that "owns" the mutable map and
 *  define a set of messages that allows other actors to access it. You could define a message for
 *  putting a key-value pair into the shared map, getting a value given a key, and so on, for all the
 *  operations you need to do on the map. In addition, you'd need to define messages for sending asynchronous
 *  responses to actors that made queries of the map. Another option, however, is to pass a thread-safe map,
 *  such as ConcurrentHashMap from the Java Concurrency Utilities, in a message to multiple actors, and
 *  let those actors use that map directly
 *
 *  Although it would be far easier and safer to implement a shared map via actors than to implement
 *  something like ConcurrentHashMap yourself, since ConcurrentHashMap already exists, you may judge
 *  it easier and as low risk to use that than to implement your own shared map with an actor.
 *  This would also mean that your responses from the shared map could be synchronous, whereas with actors
 *  they would need to be asynchronous. Scala's actors library gives you the choice.
 *
 */

object hashedUrlManager extends Actor with hashedUrlManagerTrait with UrlValidationTrait {

  private var hashDatas = new HashMap[String, hashData] with SynchronizedMap[String, hashData]

  /** Return None or Some(url) for the hash
   *
   */

  private def getUrlForHash(hash: String): Option[String] = {
    val hd = hashDatas.get(hash)
    hd match {
      case None    => None
      case Some(x) => Some(x.getUrl())
    }
  }

  /** Send a message back to the client for the hashUrl message
   *
   *  @param url String the url to hash
   *  @param sender the sender of the message
   *
   */
  private class hashUrlActor(url: String, sender: scala.actors.OutputChannel[Any]) extends Actor {
    def act {
      sender ! (if (isValidUrl(url)) hashUrl(url) else null)
    }
  }
  /** Return a unique hash for a URL
   *
   *  Has side-effect of adding the hash and new hashData to private hashDatas structure
   *
   *  @param url String the url to hash
   *
   *  @return unique hash
   */
  private def hashUrl(url: String): String = {

    val hd = new hashData(url)
    val helper = new hashedUrlHelper()

    // Generate the hash out side the getUniqueHash to minimze the thread lock time
    var hash = helper.hashUrl(url)

    this.synchronized {
      hash = helper.getUniqueHash(hash, url, getUrlForHash)
      hashDatas.put(hash, hd)
    }
    hash

  }

  /** Send a message back to the client for the urlFromHash message
   *
   *  @param hash String the hash to lookup
   *  @param sender the sender of the message
   *
   */
  private class urlFromHashActor(hash: String, sender: scala.actors.OutputChannel[Any]) extends Actor {
    def act {
      sender ! urlFromHash(hash)
    }
  }
  /** Return the URL for a hash and update the click count for the current time
   *
   *  @param hash String the hash to lookup
   *
   *  @return the URL for the hash or null if not found
   */
  private def urlFromHash(hash: String): String = {
    hashDatas.get(hash) match {
      case Some(i) => {
        i.incrementCountForNow()
        i.getUrl
      }
      case None =>
        null
    }
  }

  /** Send a message back to the client for the statsFor message
   *
   *  @param hash String the hash to lookup
   *  @param sender the sender of the message
   *
   */
  private class statsForActor(hash: String, sender: scala.actors.OutputChannel[Any]) extends Actor {
    def act {
      sender ! statsFor(hash)
    }
  }

  /** return the statistics for a given hash
   *
   *  @param hash String the hash to lookup
   *
   *  @return A Map[String, Any] of date + hour strings and related counts for that time (Int not Any) or null if not found
   */
  private def statsFor(hash: String): scala.collection.immutable.Map[String, Any] = {
    hashDatas.get(hash) match {
      case None    => null
      case Some(x) => x.getClicks.toMap
    }
  }

  /** reset the manager
   *
   *  For testing only
   */
  private def reset = {
    hashDatas = new HashMap[String, hashData] with SynchronizedMap[String, hashData]
  }

  /** main thread loop
   *
   *  Accepts HashUrl, UrlFromHash and StatsForHash messages
   */
  def act() {
    loop {
      react {
        case HashUrl(url) =>
          new hashUrlActor(url, sender).start
        case UrlFromHash(hash) =>
          new urlFromHashActor(hash, sender).start
        case StatsForHash(hash) =>
          new statsForActor(hash, sender).start
      }
    }
  }
}
