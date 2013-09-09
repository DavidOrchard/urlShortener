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

  object Main extends Application {
    new hashedUrlHelper().hashUrl("http://foo.com")
    var client1 = new urlShortenClient
    var hash = client1.hashUrl("http://foo.com")
    println("hash = " + hash)
    var url = client1.urlFromHash(hash)
    println("url = " + url)
    var clicks = client1.statsFor(hash)
    println("clicks = " + clicks.toString())
    client1.urlFromHash(hash)
    clicks = client1.statsFor(hash)
    println("clicks = " + clicks.toString())
    var client2 = new urlShortenClient
  }
