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

/** Client that provides the implementation of the Url Shortener interface.
 *
 *  @constructor defaults to using the hashedUrlManager, can be overridden (such as for testing)
 *
 *  Hides the details of the implementation of the management of hashed urls
 */
class urlShortenClient(hashActor: Actor) extends UrlShortenTrait with hashedUrlManagerTrait with UrlValidationTrait {

  def this() = this(hashedUrlManager)

  /** singleton hashedUrlManager so only the first start is necessary */
  hashActor.start

  /** Return a unique hash for a URL
   *
   *  Has side-effect of adding the hash and new hashData to private hashDatas structure
   *
   *  @param url String the url to hash
   *
   *  @return unique hash
   */
  def hashUrl(url: String): String = {
    if (!isValidUrl(url)) null
    // Trim the url to copy for immutable message
    val resp = (hashActor !? HashUrl(url.trim))
    if (resp == null) null else resp.toString()
  }

  /** Return the URL for a hash and update the click count for the current time
   *
   *  @param hash String the hash to lookup
   *
   *  @return the URL for the hash or null if not found
   */
  def urlFromHash(hash: String): String = {
    // Trim the hash to copy for immutable message
    val resp = (hashActor !? UrlFromHash(hash.trim))
    if (resp == null) null else resp.toString()
  }

  /** return the statistics for a given hash
   *
   *  @param hash String the hash to lookup
   *
   *  @return A Map[String, Any] of date + hour strings and related counts for that time (Int not Any) or null if not found
   */
  def statsFor(hash: String): Map[String, Any] = {
    // Trim the hash to copy for immutable message
    val resp = (hashActor !? StatsForHash(hash.trim))
    if (resp == null) null else resp.asInstanceOf[Map[String, Any]]
  }
}
