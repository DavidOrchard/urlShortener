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

import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen

/** hashedUrlHelperTest
 *
 *  FunSpec for fun
 */
class hashedUrlHelperTest extends FunSpec with GivenWhenThen {

  val testURL = "http://www.foo.com"
  val testURL2 = "http://www.foo2.com"
  val testHash = "cu80Au"
  var hashDatas: collection.mutable.Map[String, String] = null
  def getUrlForHash(hash: String): Option[String] = {
    val hd = hashDatas.get(hash)
    hd match {
      case None    => None
      case Some(x) => Some(x)
    }
  }

  val hd = new hashedUrlHelper()

  describe("A hashUrHelper.hashUrl(" + testURL + ")") {
    it("should generate " + testHash + " for " + testURL) {
      var hash = new hashedUrlHelper().hashUrl(testURL)
      assert(testHash === hash)
      And("nextHashUrl should not generate " + testHash + " for " + testURL)
      var hash2 = new hashedUrlHelper().nextHashUrl(testHash)
      assert(testHash !== hash2)
      assert(hash2.length === testHash.length + 1)
    }

    describe("with a isUniqueHash structure with hash " + testHash) {
      it("should generate " + testHash + " for " + testURL) {
        Given("hashData structure with hash " + testHash + " in the structure")
        hashDatas = collection.mutable.Map[String, String]()
        hashDatas.put(testHash, testURL)
        Then("getUniqueHash with testURL" + testURL + " should return hash " + testHash)
        var hash = hd.hashUrl(testURL)
        hash = hd.getUniqueHash(hash, testURL, getUrlForHash)
        assert(testHash === hash)
      }
    }
  }
  describe("A hashUrHelper.hashUrl(" + testURL2 + ")") {
    describe("with a isUniqueHash structure with hash " + testHash) {
      it("should generate " + testHash + " for " + testURL) {
        Given("hashData structure with hash " + testHash + " in the structure")
        hashDatas = collection.mutable.Map[String, String]()
        hashDatas.put(testHash, testURL2)
        Then("getUniqueHash with testURL " + testURL + " should not return hash " + testHash)
        var hash = hd.hashUrl(testURL)
        hash = hd.getUniqueHash(hash, testURL, getUrlForHash)
        assert(testHash !== hash)
        And(" it should be one longer")
        assert(hash.length === testHash.length + 1)
        When("That new unique Hash is added")
        hashDatas.put(hash, hash)
        Then("getUniqueHash should return a unique hash that is unique from " + testHash + " and " + hash)
        var hash2 = hd.hashUrl(testURL)
        hash2 = hd.getUniqueHash(hash2, testURL, getUrlForHash)
        assert(hash2 !== hash)
        assert(hash2 !== testHash)
        And("it should be the same length or 1 longer than " + hash)
        assert(hash2.length == hash.length || hash2.length == hash.length + 1)
      }
    }
  }
}
