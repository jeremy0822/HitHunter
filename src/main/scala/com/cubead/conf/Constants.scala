package com.cubead.conf

/**
 * Created by xiaoao on 5/21/15.
 */
object Constants {
  val CLEANER_MESSAGE = "expire"
  val CACHE_UPDATE_ALL = "ALL"
  var CACHE_SOURCE_URL = ""
  val KAFKA_CLIENT_ID = "HitHunter"
  val kAFKA_START_CMD = "start"
  val kAFKA_STOP_CMD = "stop"

  var kAFKA_HOST = ""
  var kAFKA_PORT = 9092
  var kAFKA_TIMEOUT = 100000
  var kAFKA_BUFFER = 64 * 1024
  var kAFKA_ZOOKEEPER = ""
  var kAFKA_GROUPID = "grtca"
  var kAFKA_TOPIC = "topic-rtca"
  var kAFKA_OFFSET = 0L

  val Timestamp = "Timestamp"
  val Browser = "Browser"
  val Request = "Request"
  val Cookie = "Cookie"
  val Tenant = "ca_tenant"
  val OWN_UID = "OWN_UID"

  val HR_KEY = "hc_resuts"
  val TENANT_SET = "tenants_set"
  val IP_PRE = "ts_ip_"
}
