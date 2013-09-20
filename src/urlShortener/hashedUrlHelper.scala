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

/** hashedUrlHelper contains helper methods for generating hashes
 *
 *  @constructor a singleton object for hashedUrlHelper
 *
 */
package urlShortener

import scala.Array.canBuildFrom
import scala.math.floor

/** Helper class for hashing URLs
 *
 */

class hashedUrlHelper {
  import hashedUrlHelper._

  /** hashUrl generates a hash for a given String in base62 encoding
   *
   *  @param url String the url (really any string) to be hashed
   *
   *  @return the base62 encoded hash
   */
  def hashUrl(url: String): String = {
    var hash = ""

    // hashCode can be negative
    var dividend = Math.abs(url.hashCode())

    while (dividend > 0) {

      //  The magic # 62 is in the code rather than calculated from the alphabet or as a const because that's just what it is!

      // Manually calculate the remainder instead of % to avoid another / call during the typical x % y = x - y * floor(x / y)
      var tempdividend = floor(dividend / 62).toInt
      var remainder = dividend - (62 * tempdividend).toInt
      dividend = tempdividend

      /** There are a variety of performance testing that could be done on the hash string generator
       *
       *  This algorithm does not use an intermediate hashdigit structure with string generation after
       *
       *  Some other questions and options
       *  Is prepend more expensive in memory or cpu than append?
       *  They are both linear according to http://www.scala-lang.org/docu/files/collections-api/collections_40.html
       *  That means that the hash string generation is O(n + n-1 + n-2 ..) where n is the size result
       *  Which seems acceptable for something quick and with small sizes of n
       *
       *  Other implementations store the remainders (but they have to work on an arry) and construct the hash with a foreach
       *  Another style is to use string formatting or even string interpolation on a hashdigits
       *
       */

      hash +:= base62_alphabet(remainder)
    }

    hash
  }

  /** nextHashUrl generate the next hash for a given hash in case of hash collisions
   *  by adding a random base62 encoding character
   *
   *  A more deterministic approach is a depth limiting breadth first search through the base 62 chars,
   *  but hey, let's not go crazy
   *
   *  @param hash String the hash that has collided
   *
   *  @return String a new hash with a random character added
   */
  def nextHashUrl(hash: String): String = {
    hash + hashedUrlHelper.base62_alphabet(new scala.util.Random().nextInt(base62_alphabet.length))
  }

  /** Returns a unique hash for a url with no side-effects
   *
   *  In the common case, the function does a hash lookup, gets a None result, and returns the passed in hash
   *  If the passed in hash is an existing hash, then generate a new one by appending a random character in the
   *  base62 set until no existing has is found.
   *
   *  The generating of the first hash is not done in this function because the calling function locks
   *  the data store.
   *
   *  @param url String the url to hash
   *  @param Unit getUrlFunction the function to use for uniqueness determination
   *  @param hd Object the object to insert for the found unique hash
   *
   *  @return String unique hash
   *
   */
  def insertUniqueHash( url: String, getOrInsertFunction: (String, Object) => Option[String], hd: Object): String = {

    var hash:String = hashUrl(url)
    var urlForHash = getOrInsertFunction(hash, hd)

    while (urlForHash != None && urlForHash.get != url) {
      hash = nextHashUrl(hash)
      urlForHash = getOrInsertFunction(hash, hd)
    }
    hash

    /** An attempt at using for loop with generator, but conditional definitions don't seem possible
     *  for( urlForHash <- getOrInsertFunction(hash, hd);
          if urlForHash != None && urlForHash != url {
            hash = nextHashUrl(hash)
          };
          if urlForHash == None
          )
        yield hash;
     *
     */
  }
}

/** companion object so the alphabet isn't created per instance
 *
 */
object hashedUrlHelper {
  val base62_alphabet = Array(
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

}
