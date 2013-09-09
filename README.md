#Problem
Develop a simple library to be used in a URL shortener service.

##Problem to solve
This shortens URLs so that they can be included in social network messages but also keeps the messages within a character limit as specified by a social network.

##Satisfaction Criteria
* Thread safety
* Three public-facing methods
  * hashUrl(url: String): String - take a URL and return a base 62 hash, as short as possible
  * urlFromHash(hash: String): String - given a hash, return the URL associated with it (this is considered a "click")
  * statsFor(hash: String): Map[String, Any] - provide basic click statistics for any given hash
* It supports parallel requests

### Bonus points for
* use of actors and/or agents
* functional Scala

## Coding Requirements
 * Clean code
 * Scala/Java docs in code
 * Performant
 * Thread safe
 * Error handling
 * Well abstracted
 * Unit tested
 
# Design

The solution uses scala actors extensively.  There is a scala actor called the hashedUrlManager aka server that manages the hashing data structure.  Internally, there is a scala actor for each method and the hashedUrlManager distributes each request to a new instance of each scala actor.  This is designed for optimal scalability and performance, particularly as it is maximally parallelizable and it is as non-blocking as possible.  

The client sends messages to the hashedUrlManager using case classes defined in the hashedUrlManagerTrait.  This is the client/server interface or contract.  The client is the urlShortenerClient class.  The requirement is for 3 public facing methods and these are embodied in the UrlShortenerTrait.  The urlShortener class is instantiated by applications, and may be instantiated multiple times.  Note, this is not built internally as an Actor, or using Futures, because these are inherently synchronous calls and should block.  

There are a number of helper objects and classes.  The UrlValidationTrait contains an isValid method to determine if a URL is a valid URL or not.  It is used by both the client and the server.  hashData is a class who's instances represent the url and clicks for a given hash.  It is used by the hashedUrlManager.  hashedUrlHelper class provides a number of hashing utility functions: hashUrl returns a hash for a given url, nextHashUrl generates the next hash for a given hash (in the extremely rare cases of hash collisions), and getUniqueHash uses those 2 functions and a passed in isUniqueFunction to (with great functional programming gusto) create a unique hash for a url.  The hashedUrlHelper is a class to ensure that multiple hashes can be calculated at the same time, and it uses a helper object for the base 62 alphabet to ensure minimal stack and heap usage.

The class/object/trait summary is:
* UrlShortenerTrait - the 3 public facing methods signatures
* UrlShortenerClient - class that implements the UrlShortenerTrait by sending messages to the hashedUrlManager
* hashedUrlManagerTrait - the interface to the hashedUrlManagerTrait, specifically the case classes that the hashedUrlManager responds to
* hashedUrlManager - the Actor (aka server) that manages the hashing of urls and collection of statistics
* hashData - encapsulates information about a hash, specifically urls and click counts per hour
* hashedUrlHelper - helper class for generating unique hashes
* urlValidationTrait - helper trait to mixin for validating urls

Entity Relationship Diagram, interaction diagrams, class models, and other diagrams are omitted.

## Thread safety

It is worth examining the thread safety of the operations.  The hashDatas structure is a mutable Map with the synchronized trait mixed so it is thread safe.

The methods and their hashData operations that operate on hashDatas are:

###hashUrl
Reads then a write of a new hashData.  Potentially there could be an identical hash generated for a different url at the same time, 
and the write of the 2nd hashdata overrides the first.  The hashedUrlManager.hashUrl has a this.synchronized around the read and the write.

###urlForHash
Read then a write of a + 1 count.  Potentially there could be interleaved urlForHash counts where the first thread gets the count, 
the 2nd thread gets the count, they both increment and write and a +1 is lost.  The actual read/write is done in hashData incrementCountForDateMethod,
so it has a this.synchronized to lock for the read and the write.  Performance is increased by having the initial request to hash a URL done
outside the lock.  This does complicate the code slightly but it optimizes for the common case of non-conflicting hashes.

###statsFor
Read only

The methods of different types cannot interleave resulting in incorrect state.  For example, a hashUrl that hasn't written the hashData
structure gets swapped with an urlForHash of the exact same just generated hash will return empty.  

## A few design notes

I don't like the use of null pointers from the public methods, but given the interface that is all that is available.  I would have preferred a more scala like Option style.  I could have insulated the server from the use of nulls but I thought it was simpler to re-use the methods signature in both the client and the server, an 80/20 call.  

There is an extensive url validity test, the best I could find.  But there are no other code safety, such as XSS, tests done.

The private reset method on the hashedUrlManager is required for testing to ensure the test starts with a fresh server.  The restart does not function if the actor is running, yet an exit within the actor ends the test.  

The hashData is exposed as public only so that it can be unit tested. Unlike methods, I could not find a way to access private classes in scalatest.

I didn't bother to build a grunt task to run tests

# Test

There are unit tests for each of the classes, object, or concrete traits: hashDataTest, hashedUrlHelperTest, hashedUrlManagerTest, urlShortenerClientTest, and urlValidationTrait test.  There is a hashedUrlManagerMock that the urlShortenerClientTest uses.  There is a urlValidationLoadTest that runs a hundred method requests in parallel.  The LoadTest spawns a large number of Actors to do each method invoke, and then waits until they have all returned. The urlShortenerLoadTest shows how an actor could be used to wrap each client method.  

For some fun, FunSpec and Flatspec styles are used.

A concrete test plan that lists each and every test scenario is omitted.

# Documentation

scaladoc generated docs are in /docs

# Installation

Built with:
* scala 2.10.2 in eclipse
* eclipse 4.2
* scalatest 2.0M6
* scaladoc
* scalastyle

The bin directory is added to the eclipse buildpath