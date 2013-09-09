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

import org.scalatest.GivenWhenThen
import org.scalatest.PrivateMethodTester
import org.scalatest.FunSpec
import java.util.Calendar

class hashDataTest extends FunSpec with GivenWhenThen with PrivateMethodTester {
  val testURL = "http://www.foo.com"

  describe("A hashData") {
    it("Should allow a new constructor") {
      Given("A new hashData(" + testURL + ")")
      val hd = new hashData(testURL)
      Then("it should have getUrl = " + testURL)
      assert(hd.getUrl === testURL)
      And("should have a getClicks Map that is empty")
      assert(hd.getClicks().size === 0)
    }
  }
  describe("A hashData for URL " + testURL) {
    it("Should allow a click registered for now") {
      Given("hashData initialized with URL" + testURL)
      val hd = new hashData(testURL)
      When("click registered for now")
      var now = Calendar.getInstance().getTime()
      var nowDateString = hd.hashDataDateFormatter.format(now)
      hd.incrementCountForNow()
      Then("it should have count for date " + nowDateString + " == 1")
      assert(hd.getClicks()(nowDateString) === 1)
      And("it should have no other dates registered")
      assert(hd.getClicks().size === 1)
      When("Another click registered for now")
      hd.incrementCountForNow()
      Then("it should have count for date " + nowDateString + " == 2")
      assert(hd.getClicks()(nowDateString) === 2)
    }
  }
  describe("A hashData for URL " + testURL + " with a click registered for now") {
    val timeOffset = -2
    it("Should allow a click registered for now " + timeOffset + " hrs") {
      Given("hashData initialized with 1 click")
      val hd = new hashData(testURL)
      var now = Calendar.getInstance().getTime()
      var nowDateString = hd.hashDataDateFormatter.format(now)
      hd.incrementCountForNow()
      When("a click for " + timeOffset + " hrs is registered")
      var nowCalendar = Calendar.getInstance()
      nowCalendar.add(Calendar.HOUR_OF_DAY, timeOffset)
      var nowPlus2Hour = nowCalendar.getTime()
      var nowPlus2HourDateString = hd.hashDataDateFormatter.format(nowPlus2Hour)
      val incrementCountForDateFunction = PrivateMethod[String]('incrementCountForDate)
      hd invokePrivate incrementCountForDateFunction(nowPlus2Hour)
      Then("it should have count for date " + nowDateString + " == 1")
      assert(hd.getClicks()(nowDateString) === 1)
      And("it should have count for date " + nowPlus2HourDateString + " == 1")
      assert(hd.getClicks()(nowPlus2HourDateString) === 1)
      And("it should have no other dates registered")
      assert(hd.getClicks().size === 2)
    }
  }
}

