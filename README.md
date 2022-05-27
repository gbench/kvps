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
const msgname = message.str("msgname");  
const params = message.llS("keys").map(kvps::get).map(IRecord:REC).toArray(IRecord[]::new);  
switch(msgname){  
  case "reqdec":{ //  
    // 需求分析  
    const IRecord proj = params[1]; // 项目信息 的 json, IRecord 可以理解为一个 JAVA 实现 的 JS的Object 模型。  
    const IRecord reqdoc = params[2]; // 需求文档 的 json  
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
5) SEND接口也可采用消息中间件进行替代，这样 ITSM 就与 DEVOPS 彻底解耦了。