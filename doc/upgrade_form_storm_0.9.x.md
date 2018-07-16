# storm 2.0 升级

## 1 jar需要重新编译
storm 0.9.x 之间是使用 backtype.storm 作为包前缀， storm 1.x 之后都改为 org.apache.storm

- 替换 import 包名前缀
```java
// 将 backtype.storm 替换为 org.apache.storm
// 其他代码不用修改

// 旧
import backtype.storm.spout.SpoutOutputCollector;     
import backtype.storm.task.TopologyContext;           
import backtype.storm.topology.OutputFieldsDeclarer;  
import backtype.storm.topology.base.BaseRichSpout;    
//...

// 新
import org.apache.storm.spout.SpoutOutputCollector;  
import org.apache.storm.task.TopologyContext;  
import org.apache.storm.topology.OutputFieldsDeclarer;  
import org.apache.storm.topology.base.BaseRichSpout;
//...
```

- 修改 pom 文件

```xml

<!-- 将 storm-core 依赖，替换为 storm-client 依赖，可能有jar 包冲突修要修复 -->

<!-- 旧 -->
<dependency>
    <groupId>org.apache.storm</groupId>
    <artifactId>storm-core</artifactId>
    <version>0.9.1-incubating</version>
    <scope>provided</scope>
</dependency>

<!-- 新 -->
<dependency>
    <groupId>org.apache.storm</groupId>
    <artifactId>storm-client</artifactId>
    <version>2.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

## 2 storm 集群节点更新

### 2.1 节点部署可以参考 deploy 文档

### 2.2 具体部署流程
1. 先搭建一个新的zookeeper集群
2. 部署好nimbus
3. 在其中一台机器上，部署新的supervisor，新旧supervisor使用不同的端口slot
4. 停掉旧集群任务，将任务提交到新集群，检查任务稳定后，停掉旧的supervisor
5. 重复3，直到全部任务迁移完成
6. 完成全部任务迁移后，停掉nimbus