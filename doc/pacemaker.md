# Storm Pacemaker

## Summary
- pacemaker 的设计比较简单;
- pacemaker 只会接管 worker 的心跳信息，supervisor，nimbus的心跳仍然由zk负责;
- 可以通过配置启动多个 pacemaker 同时提供服务，pacemaker 之间是无关连的
- worker 只会向其中一个 pacemaker 发送心跳，当前节点无法连接则会去连接其他节点，失败节点放到队尾
- 但 nimbus 获取心跳的时候，需要向所有 pacemaker 拉取心跳
- 当所有 pacemaker 都无法连接，worker 会不断轮寻尝试，nimbus 异常退出

### good
- pacemaker 只会将数据存在内存中，不需要写磁盘，节点之间不需要同步，资源消耗小

### bad
- pacemaker 挂掉可能会导致大量 topology 重启，影响集群稳定
- pacemaker 配置会分布到所有客户端机器上，基本无法修改

## TODO
1. 修复 nimbus 因所有 pacemaker 都无法连接，挂掉的 bug
2. pacemaker 最好能在 zookeeper 注册一个临时节点，worker 可以通过zk定时拉取信息，拉取间隔可以config
3. worker 在列表中选取 n 个节点发送心跳， 建议 n>1, n可以配置， 防止 pacemaker 挂掉大量任务重启

## result
- zookeeper 可以支持节点数 2000～3000， 目前集群还远达不到
- 观望
