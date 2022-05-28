# KVPS
键值对儿数据库

1) 启动 KVSAPPlication：/kvps/src/main/java/gbench/whccb/kvps/apl/KVPSApplication.java
2) 运行 JUnit 测试程序: /kvps/src/test/java/gbench/sandbox/weihai/DevOpsJunit.java

注：
1) KVPS 的实现可以采用多种方式：REDIS，文件系统，关系数据库的日志表 等。这是 提供了一个 演示版的 文件系统的实现（为何要用文件系统呢？只因它足够的简单，
特别是我只需30min就可以给出一个实现）。我是希望最终可以通过网盘来实现KVPS的 ，如果 在系统上线 时候，网盘不可用，建议 暂用 REDIS 来进行代替。
本质就是 采用一个 二级指针 **PTR  来进行资源访问。
演示代码里的资源存放路径是：E:/slicee/temp/snowwhite/kvps/devops/proj/，这是一个层级结构，也就是KEY本身的编码方式也是具有意义的，这是文件系统的天然好处。
2) REST API CONTROLLER (KVS API IMPLEMENATION): /kvps/src/main/java/gbench/whccb/kvps/controller/MediaController.java
3) APIS:  
  3.1) LIST 资源列表 http://localhost:8090/snowwhite/media/file/list?key=E:/slicee/temp/snowwhite/  
  3.2) WITE 资源 PUT: http://localhost:8090/snowwhite/media/file/write?key=E:/slicee/temp/snowwhite/kvps/devops/proj/proj001.json  
  3.3) READ 资源 GET : http://localhost:8090/snowwhite/media/file/download?key=E:/slicee/temp/snowwhite/kvps/devops/proj/proj001.json    

这里为了演示需要，强调了文件的特征，KEY采用了文件的全路径，在实际引用场景里KEY应该是一个UUID的字符串，层级属性（实际的物理存储的信息）只有在URL中才能够体现出来：
比如：http://localhost:8090/snowwhite/media/file/write?key=E:/slicee/temp/snowwhite/kvps/devops/proj/proj001.json
4) ITSM 与 DEVOPS 理论上 只需要一个接口，类似于：SEND({msgname,keys:[key1,key2,....]}).
我们可以通过 msgname 来判断事件的类型(即是什么事而)，各个keys的数据意义（类似于前端编程里按钮的click(event)事件的event.target），然后通过 KVPS 进行具体的
实体数据 （master data) 的获取。进而 实现 相应的业务。我们的程序可以采用，一下的逻辑对事件进行处理：  
```java   
// 为了简化类型书写，这里使用 java 11 的 编码风格
final var msgname = message.str("msgname");  
final var params = message.llS("keys").map(kvps::get).map(IRecord:REC).toArray(IRecord[]::new);  
switch(msgname){  
  case "reqdec":{ //  
    // 需求分析  
    final var  proj = params[1]; // 项目信息 的 json, IRecord 可以理解为一个 JAVA 实现 的 JS的Object 模型。  
    final var  reqdoc = params[2]; // 需求文档 的 json  
    handle_reqdec(proj,reqdoc); // 需求分解的具体逻辑  
    break;  
  }  
  case "uatents":{ //  
    // 验收申请  
    handle_reqdec(params);  
    break;  
  }  
  default:{
    // I don't know 
    // so,
    // do nothing
  }
}  
```
5) SEND接口 除了自己实现（它只是简单的接收一下参数而已），也可采用 消息中间件 ( 
  各种MQ，比如：RocketMQ,RabbitMQ,Kafka,ActiveMQ 等，甚至 Zookeeper 这样的协调服务（ 用在这里其实是有些大材小用了，徒增运维工作量的，所以这是提一下），
  但思想却一致的，就是 键/值托管:系统（DEVOPS，ITSM）拿键，KVPS存值，以不变应万变 ），  
  对我们（DEVELOPER）来说，这里的唯一约束，就是确定哪一个是自己喜欢的（你到底爱谁），有时选择太多，特别是我们自己的思想并不坚定或者或脑袋有些糊涂的时候，也是一种麻烦，哈哈) 
进行替代，这样 ITSM 就与 DEVOPS 彻底解耦了。

6) 为何不在SEND接口中 直接带上 对象数据呢？这种 用消息 只传一个KEY 的 间接通信 的 方式 是不是 一种脱裤子放屁 的 费二遍手续 呢？
我们认为不是这样的，因为我们 传输的数据是 主要是 MASTER DATA 也就是 业务主数据，是 跨 业务会话SESSION 的 业务实体，他们 在各个系统
比如 DEVOPS , 与 ITSM 都是 持续变化，因此 如果 不采用 这种 唯一主索引（KEY）的方式在KVPS 进行 共享 的 对象实体，那么 势必 要在 DevOps
与 ITSM 中 分别 维护&同步 其 状态，这个 就 类似于 集中式 版本管理系统 CVS，SVN，以及 GIT 这样的 中心仓库的模型了，我们把  系统间 需要
暴露的 对象实体 采用 REPOSITORY 的方式 在KVPS 进行中心式的版本管理，而 各个业务系统（DEVOPS，ITSM）在使用时 自动到 KVPS中进行pull的来
获取新版本的办法，其实是极大的简化了，系统并行 所 带来的 状态冲突。（并发编程的同步问题，大家可以借鉴一下里面的复杂性）。所以，此处的间接
传递数据，其实 并不是 低效，相反还很高效。同时，由于 两个系统（ITSM，DEVOPS）交互场景不多，这种间接带来的损耗就微乎其微多了，这也是 为何 
KVPS 我们也可采用文件系统来进行实现。
以上 就是 就我推荐 KVPS 的 理由。
