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

import org.scalatest.PrivateMethodTester
import org.scalatest.GivenWhenThen
import org.scalatest.FunSpec
import java.util.Calendar

/** hashedUrlManagerTests
 *
 *  It is deliberate that there is not a test client separate from the urlShortenClientTest to test the message sending to
 *  hashedUrlManager as it largely duplicates the same code and appears fairly needless extra work.
 *
 *  Using FunSpec for Fun
 */

class hashedUrlManagerTests extends FunSpec with PrivateMethodTester with GivenWhenThen {

  val testURL = "http://www.foo.com"
  val testHash = "cu80Au"
  val testHash2 = "cu80Aud"

  var hashActor = hashedUrlManager

  val hashUrlMethod = PrivateMethod[String]('hashUrl)
  val urlFromHashMethod = PrivateMethod[String]('urlFromHash)
  val statsForMethod = PrivateMethod[Map[String, Any]]('statsFor)
  val resetMethod = PrivateMethod('reset)

  var hash: String = null
  // I couldn't use restart, or a stop method then a hashActor.restart or a hashActor = hashedUrlManager
  // probably ok as akka doesn't have restart

  describe("A hashManager") {
    describe("without hash " + testHash) {
      it("Should have null return from click count for hash " + testHash) {
        hashActor.start
        hashActor invokePrivate resetMethod()
        var stats = hashActor invokePrivate statsForMethod(testHash)
        assert(stats === null)
      }

      it("Should have null URL for " + testHash + " hash") {
        val url = hashActor invokePrivate urlFromHashMethod(testHash)
        assert(url === null)
      }

      it("should allow an URL of " + testURL + " to be hashed") {
        val hash = hashActor invokePrivate hashUrlMethod(testURL)
        assert(hash != null)
        And("should have a hash result of " + testHash)
        assert(testHash === hash)
      }
      // No URL for hash
      it("Should have URL equal null for " + testHash2 + " hash") {
        val url = hashActor invokePrivate urlFromHashMethod(testHash2)
        assert(url === null)
      }
    }

    describe("with hash " + testHash) {
      it("Should have URL equal " + testURL + " for hash " + testHash) {
        val url = hashActor invokePrivate urlFromHashMethod(testHash)
        assert(testURL === url)
      }
      it("Should have click count for hash " + testHash + " at time now equal 1") {
        var stats = hashActor invokePrivate statsForMethod(testHash)
        var now = Calendar.getInstance().getTime()
        var nowDateString = new hashData(testURL).hashDataDateFormatter.format(now)
        assert(stats.get(nowDateString) === Some(1))
      }

      it("Click count after 2 invokes for hash " + testHash + " equal 2") {
        val url = hashActor invokePrivate urlFromHashMethod(testHash)
        var stats = hashActor invokePrivate statsForMethod(testHash)
        var now = Calendar.getInstance().getTime()
        var nowDateString = new hashData(testURL).hashDataDateFormatter.format(now)
        assert(stats.get(nowDateString) === Some(2))
      }
    }
  }
}
