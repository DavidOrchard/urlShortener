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

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec

/** urlShortenClient tests
 *
 *  Using FlatSpec for fun
 */

class urlShortenClientTest extends FlatSpec with BeforeAndAfter {
  var client: urlShortenClient = null
  val testURL = "http://www.foo.com"
  val testHash = "cu80Au"

  before {
    client = new urlShortenClient()
  }

  after {
    client = null
  }

  "client" should "not be null" in {
    assert(client !== null)
  }

  val client2 = new urlShortenClient()

  "client2" should "not be null" in {
    assert(client2 !== null)
  }

  it should "not equal client" in {
    assert(client !== client2)
  }

  "Hash of " + testURL + "" should " equal " + testHash in {
    assert(testHash === client.hashUrl(testURL))
  }

  "Url for " + testHash + "" should " equal " + testURL in {
    assert(testURL === client.urlFromHash(testHash))
  }

  "Stats for " + testHash + "" should " be 1 long" in {
    assert(1 === client.statsFor(testHash).size)
  }

  var hd = new urlShortenClient(hashedUrlManagerMock)
  "HashManagerMock" should "respond with HashUrl for HashUrl message" in {
    assert(hd.hashUrl("foo") === "HashUrl")
  }
  "HashManagerMock" should "respond with UrlFromHash for UrlFromHash message" in {
    assert(hd.urlFromHash("foo") === "UrlFromHash")
  }
  "HashManagerMock" should "respond with null for StatsFor method" in {
    assert(hd.statsFor("foo") == null)
  }

}

