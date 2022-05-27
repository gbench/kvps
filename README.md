# kvps
键值对数据库

1) 启动 KVSAPPlication：/kvps/src/main/java/gbench/whccb/kvps/apl/KVPSApplication.java
2) 运行 JUnit 测试程序: /kvps/src/test/java/gbench/sandbox/weihai/DevOpsJunit.java

注：
1) KVPs 的实现可以采用多种方式：REDIS，文件系统，关系数据库的日志表 等。这是 提供了一个 演示版的 文件系统的实现（为何要用文件系统呢，只因它足够简单）。
我是希望最终可以通过网盘来实现KVPS的 ，如果 在系统上线 时候，网盘不可用，建议 暂用 REDIS 来进行代替。本质就是 采用一个 二级指针 **PTR  来进行资源访问。
演示代码里的资源存放路径是：E:/slicee/temp/snowwhite/kvps/devops/proj/，这是一个层级结构，也就是KEY本身的编码方式也是具有意义的，这是文件系统的天然好处。
2) REST API CONTROLLER (KVS API IMPLEMENATION): /kvps/src/main/java/gbench/whccb/kvps/controller/MediaController.java
3) APIS:  
  3.1) LIST 资源列表 http://localhost:8090/snowwhite/media/file/list?key=E:/slicee/temp/snowwhite/  
  3.2) WITE 资源 PUT: http://localhost:8090/snowwhite/media/file/write?key=E:/slicee/temp/snowwhite/kvps/devops/proj/proj001.json  
  3.3) READ 资源 GET : http://localhost:8090/snowwhite/media/file/download?key=E:/slicee/temp/snowwhite/kvps/devops/proj/proj001.json    

这里为了演示需要，强调了文件特征，KEY采用了文件的全路径，在实际引用场景KEY应该是一个UUID的字符串，层级属性（切实物理存储的信息）只有在URL中才能够体现出来：
E:/slicee/temp/snowwhite/kvps/devops/proj/proj001.json  
4) ITSM 与 DEVOPS 理论上 只需要一个接口，类似于：SEND({msgname,keys:[key1,key2,....]}).
我们可以通过msgname 判断，事件的类型，一个 各个keys的数据毅力，然后通过 KVPS 进行具体的 实体数据 （master data) 的获取。进而 实现 相应的业务。
我们程序可以采用：  
```java   
// 为了简化类型书写，这里使用 java 11 的 编码风格
final var msgname = message.str("msgname");  
const var params = message.llS("keys").map(kvps::get).map(IRecord:REC).toArray(IRecord[]::new);  
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
}  
```
5) SEND接口 也可采用 消息中间件(各种MQ，比如：RocketMQ,RabbitMQ,Kafka,ActiveMQ 等，甚至 Zookeeper 这样的协调服务
这里的唯一约束确定那一个是自己喜欢的，有时选择太多也是一种麻烦，哈哈) 进行替代，这样 ITSM 就与 DEVOPS 彻底解耦了。

6) 为何不再SEND接口中 直接带上 对象数据呢？这种 用消息 只传一个KEY 的 间接通信的方式 是不是 一种脱裤子放屁 的 费二遍手续 呢？
我们认为不是这样的，因为我们 传输的 数据是 主要是 MASTER DATA 也就是 业务主数据，是 跨 业务会话SESSION的 业务实体，他们 在各个系统
比如 DEVOPS , 与 ITSM 都是 持续变化，因此 如果 不采用 这种 唯一主索引（KEY）的方式在KVPS 进行 共享 对象实体，那么 势必 要在 DevOps
与 ITSM 中 分别 维护并同步状态，这个 就 类似于 集中式 版本管理系统 CVS，SVN，以及 GIT 这样的 中心仓库的模型了，我们把  系统间 需要
暴露的 对象实体 采用 REPOSITORY 的方式 在KVPS 进行中心式版本管理，而 各个业务系统（DEVOPS，ITSM）在使用的 自动到 KVPS中进行pull的来
获取新版本的办法，其实极大的简化了，系统并行 所 带来的 状态冲突。（并发编程的同步问题，大家可以借鉴一下里面的复杂性）。所以 此处间接
传递数据，其实 并不是 低效，相反还很高效。同时，由于 两个系统（ITSM，DEVOPS）交互场景不多，这种间接带来的损耗就微乎其微多了。
以上 就是 我推荐 KVPS 的 理由。
