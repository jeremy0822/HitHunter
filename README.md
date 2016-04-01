# 恶意ip实时阻击项目

# 开发环境  
实时恶意点击监测方法，采用Scala（2.11.6）编写，采用Akka框架，依赖于spray，redis，kafka，async-http-client，json4s等包，整体比较简单，代码行数不多。

# 程序设计思路:  
1.从kafka读取实时数据（读取间隔可在application.conf中配置）；
2.然后格式化算法需要的数据；
3.继而从进入核心的计算逻辑（主要依赖redis，如果后续达到瓶颈可以增加redis或抽出来自己来写）；
4.然后把监测到的恶意点击结果保存；
同时：
1.程序启动时会一次加载所有企业配置数据到本地，根据企业数目调整缓存策略；
2.程序启动时会有一个监听9000端口的程序来读取配置数据的变更；
3.有个后台actor定期清理过期数据（暂定两小时）；

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

# 程序文件说明：  
Application.scala 为程序入口
actors包中是所有的主要actor：
1.有cache建立维护；
2.kafka数据读取；
3.cleaner过期数据的清理；
4.httpservice是http web监听服务；
5.Parser是ca数据格式化，过滤等功能可在这里添加；
6.worker主题处理逻辑，依赖redis；
7.result结果保存

conf包是配置包，基本常量配置；
http包http web处理监听
model所需要的实体
utils 工具方法包

