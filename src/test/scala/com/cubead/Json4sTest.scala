package com.cubead

import com.cubead.model.Conf
import org.json4s._

/**
 * Created by xiaoao on 5/26/15.
 */
object Json4sTest extends App{
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import org.json4s.JsonDSL._
  implicit val formats = DefaultFormats

//  import org.json4s.jackson.JsonMethods._
//  import com.fasterxml.jackson.annotation.JsonValue
  val json =
    """
      [
         {"tid": "baidu", "rule": [1,1,2,2]},
        {"tid": "xiaoao", "rule": [1,3,2,23]}
      ]
    """.stripMargin

  val o = parse(json).extract[List[Conf]]
  println(o)
}
