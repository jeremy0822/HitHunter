package com.cubead

import com.cubead.conf.Constants
import com.cubead.utils.{CacheManager, CacheBuilderHelper}

/**
 * Created by xiaoao on 5/26/15.
 */
object CacheTest extends  App{
  Constants.CACHE_SOURCE_URL = ""
  CacheBuilderHelper.buildCache()
  val tenantId = ""
  CacheBuilderHelper.buildCacheById(tenantId)
  val conf = CacheManager.getConf(tenantId)
  println(conf.rule)

}
