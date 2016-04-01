# 恶意ip实时阻击项目
# 项目描述
在互联网SEM广告营销系统中，网页浏览者的每次广告点击都会产生计费，而由此造成的恶意点击会对客户带来严重的经济损失；另一方面由于商家会在自己网站上对来访者的浏览点击行为信息做记录以便分析调整自己的网站，而恶意点击会不利于商家对来访者的行为进行分析，因此恶意点击检测算法非常重要，关乎一个厂商的信誉和营销分析。因此恶意点击检测显得尤为必要，目前来讲大部分厂商采用的是事后分析检测，这种也是使用较多的一种，百度凤巢系统就采用的这种方法。在大数据实时或准实时分析的时代，不仅影响用户体验，这种事后检测的机制越来越不为用户所接受。

# 开发环境
恶意点击实时监测方法，采用Scala（2.11.6）编写，采用Akka框架，依赖于spray，redis，kafka，async-http-client，json4s等包，整体比较简单，代码行数不多。

# 程序设计思路 
* 从kafka读取实时数据（读取间隔可在application.conf中配置）；
* 然后格式化算法需要的数据；
* 继而从进入核心的计算逻辑（主要依赖redis，如果后续达到瓶颈可以增加redis或抽出来自己来写）；
* 然后把监测到的恶意点击结果保存；
同时：
* 程序启动时会一次加载所有企业配置数据(恶意点击的阶梯频率)到本地，根据企业数目调整缓存策略；
* 程序启动时会有一个监听9000端口的程序来读取配置数据的变更；
* 有个后台actor定期清理过期数据（暂定两小时）；

# 配置文件说明  
app {
  name = "hithunter"  程序名
  hit-conf-url = "http://192.168.3.184:8181/ncs/guardforbidips/getHcRule.do" 读取配置项的地址
  clean-duration = 600        清理过期数据的时间间隔
  kafka-read-interval = 5     kafka读取间隔
  host = "172.16.1.201"          服务绑定地址
  port = 9000                 服务绑定端口
}

akka {
  loglevel = INFO             log级别
}

kafka {
  connection = "192.168.3.227"     kafka地址
  port = 9092                      kafka端口
  zookeeper = "192.168.3.226:2181" zookeeper地址
  groupId = "grtca"                kafka groupid
  topic = "topic-rtca"             kafka的topic
}

redis {
  host = "192.168.3.87"     redis地址
  port = 6379                redis端口
}

# 程序文件说明  
Application.scala 为程序入口
actors包中是所有的主要actor：
* 有cache建立维护；
* kafka数据读取；
* cleaner过期数据的清理；
* httpservice是http web监听服务；
* Parser是ca数据格式化，过滤等功能可在这里添加；
* worker主题处理逻辑，依赖redis；
* result结果保存
