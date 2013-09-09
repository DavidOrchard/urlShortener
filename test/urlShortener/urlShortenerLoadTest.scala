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
import scala.actors.Actor

/** Runs load test against the urlShortener
 *
 */
class urlShortenerLoadTest extends FunSpec {
  val testURL = "http://www.foo.com/"
  val highLoad = 100

  abstract class urlShortenerLoadClient extends Actor {
    val hashActor = new urlShortenClient()

  }

  class hashUrlLoadClient(url: String) extends urlShortenerLoadClient {

    def act():Unit = {
      loop {
        react {
          case msg : Any => {
            var hash = hashActor.hashUrl(url)
            reply(hash)
            exit
          }
        }
      }
    }
  }

  class urlForHashLoadClient(url: String) extends urlShortenerLoadClient {
    var hash = hashActor.hashUrl(url)
    def act():Unit = {
      loop {
        react {
          case msg : Any => {
            var respUrl = hashActor.urlFromHash(hash)
            reply(respUrl)
            exit
          }
        }
      }
    }
  }

  class statsForLoadClient(url: String) extends urlShortenerLoadClient {
    var hash = hashActor.hashUrl(url)
    def act():Unit = {
      loop {
        react {
          case msg : Any => {
            var stats = hashActor.statsFor(hash)
            reply(stats)
            exit
          }
        }
      }
    }
  }

  class clientRunner extends Actor {
    def act() {
      var numMessagesReceived = 0
        def replyWhenNecessary(msg: Any, sender: scala.actors.OutputChannel[Any]) {
          numMessagesReceived += 1
          if (numMessagesReceived == highLoad) {
            sender ! numMessagesReceived
            exit
          }
        }
      var clientArray: Array[urlShortenerLoadClient] = Array()

      var hash = ""
      // Make 10 sets of requests, first request is hashForUrl, next 8 are UrlForHash, last is statsFor
      for (i <- 0 to highLoad) {
        if (i % 10 == 0) {
          clientArray +:= new hashUrlLoadClient(testURL + (i / 10))
        } else if (i % 10 == 9) {
          clientArray +:= new urlForHashLoadClient(testURL + (i / 10))
        } else {
          clientArray +:= new statsForLoadClient(testURL + (i / 10))
        }
      }

      for (i <- 0 to highLoad) {
        clientArray(i).start
      }
      for (i <- 0 to highLoad) {
        clientArray(i) ! "hi"
      }

      var send: scala.actors.OutputChannel[Any] = null
      loop {
        react {
          case "Start"  => send = sender
          case msg: Any => replyWhenNecessary(msg, send)
        }
      }
    }

  }

  describe("A hashManager") {
    it("should handle many concurrent various requests") {
      val timeout = 5000
      val clientRunner = new clientRunner
      clientRunner.start
      val count = (clientRunner !? (timeout, "Start"))

      assert(count.getOrElse(0) === highLoad)
    }
  }
}
