// package com.cubead

// import org.specs2.mutable.Specification
// import spray.testkit.Specs2RouteTest
// import spray.http._
// import StatusCodes._

// class MyServiceSpec extends Specification with RedisHelper {
//   def actorRefFactory = system
  
//   "RedisHelper" should {

//     "return a greeting for GET requests to the root path" in {
//       zadd("tbtest", "123", "123") ~> check {
//         zget[String]("tbtest", "123") must contain("123")
//       }
//     }
//   }
// }
