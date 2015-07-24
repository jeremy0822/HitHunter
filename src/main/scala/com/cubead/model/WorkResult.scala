package com.cubead.model

/**
 * Created by xiaoao on 5/18/15.
 */
case class WorkResult(tenantId: String, ip: String, onuid: String, timestamp: Double, duration: Int){
  def toTBName = s"tb_${tenantId}_${onuid}"
}

case class LogItem(tenantId: String, ip: String,onuid: String, timestamp: Double){
  def toTBName = s"tb_${tenantId}_${onuid}"

  def check = tenantId.equals("0") || ip.equals("0") || onuid.equals("0") || timestamp.equals(0)
}

case class Conf(tid: String, rule: List[Int]){
  def toSteps = rule.sliding(2,2)
}