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

/** Interface for the Url shortening methods */

trait UrlShortenTrait {

  /** Return a unique hash for a URL
   *
   *  @param url String the url to hash
   *
   *  @return unique hash
   */
  def hashUrl(url: String): String

  /** Return the URL for a hash and update the click count for the current time
   *
   *  @param hash String the hash to lookup
   *
   *  @return the URL for the hash or null if not found
   */
  def urlFromHash(hash: String): String

  /** return the statistics for a given hash
   *
   *  @param hash String the hash to lookup
   *
   *  @return A Map[String, Any] of date + hour strings and related counts for that time (Int not Any) or null if not found
   */
  def statsFor(hash: String): Map[String, Any]
}
