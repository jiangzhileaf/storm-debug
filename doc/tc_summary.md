# tc 

## 1.概念

- qdisc(queueing discipline)，队列决定了数据发送的方式，通过队列对数据进行重新编排，整形，延时，丢弃
- filter，过滤器，将数据分类，然后按队列处理
- 调度，对数据的重新编排;队列可以决定特定数据在某些数据之前发送
- 整形，对特定数据进行延迟发送，以免超过特定流量上限
- Work-Conserving策略，只要网卡允许就不会暂停发送数据
- non-Work-Conserving策略，网卡可以延时数据发送或者丢弃
- 分类队列，队列可以包含子队列，组成一个树状队列结构
- 队列长度txqueuelen，来自网卡的配置，无法通过tc修改

## 2.队列种类

### 2.1.pfifo_fast
- pfifo_fast是linux默认队列，其实这个队列包含3个子队列
- 编号为0,1,2；队列编号越小优先级越高，优先处理高优先级队列信息
- 信息根据报文的tos字段(4bit)，将数据放进不同队列
- 但由于用户无法改变子队列的性质，所以pfifo_fast也算是无分类队列
#### 2.1.1.参数
- priomap：tos字段与队列映射规则，默认1,2,2,2,1,2,0,0,1,1,1,1,1,1,1,1 

### 2.2.TBF
- token bucket filter, 令牌桶过滤队列
- 只允许以不超过事先设定的速率到来的数据包通过，但可能允许短暂突发流量朝过设定值
- TBF 很精确,对于网络和处理器的影响都很小
- TBF 实现了一个缓冲桶，桶有装载上限，桶上不断按特定速率放入token，数据到来后，每次发送需要，取走一个token，桶满了就丢弃token
- 如果数据发送的速率超过了token的速率，则数据需要在缓冲区等待token，如果数据量太大则发生丢包;
- 如果数据发送速率等于token速率，则数据无等待通过
- 如果数据发送速率小于token速率，则数据无等待通过，多余的token累计在桶里，直到桶被装满，等待有大量数据到来就可以消耗掉;

#### 2.2.1.参数
- limit： 数据包最大缓存大小
- latency： 数据包最长等待时间,limit和latency只能设置其中一个
- burst： 桶的大小,单位字节;通常越大的带宽需要越大的桶
- mpu: 令牌的最低消耗,单位字节，默认为0,一般不需要设置
- rate： token 速率
- peakrate: 最大令牌消耗速度，可用于限制最大瞬时流量
- minburst: 峰值速率下桶大小可以适当提高minburst
- 
#### 2.2.2.例子
```sh
tc qdisc add dev eth0 handle 10: root tbf rate 0.5mbit \ 
burst 5kb latency 70ms peakrate 1mbit       \ 
minburst 1540
```

### 2.3.SQF
- SFQ(Stochastic Fairness Queueing，随机公平队列)
- 对于每一个对话的流量都会通过hash函数发送到多条FIFO的队列中，保证没一个对话都不会被其他对话淹没
- hash函数会不断改变

#### 2.3.1.参数
- perturb：多少秒后重新配置一次散列算法
- quantum：一个流至少要传输多少字节后才切换到下一个队列

#### 2.3.2例子
```sh
tc qdisc add dev ppp0 root sfq perturb 10
#tc -s -d qdisc ls
#qdisc sfq 800c: dev ppp0 quantum 1514b limit 128p flows 128/1024 perturb 10sec
# Sent 4812 bytes 62 pkts (dropped 0, overlimits 0) 
```

### 2.4.PRIO
- PRIO 队列规定是 pfifo_fast 的一种衍生物,可以自己自定子队列和过滤规则

#### 2.4.1.参数
- bands : 子队列数量
- priomap : 同pfifo_fast

#### 2.4.2.例子
```sh
# tc qdisc add dev eth0 root handle 1: prio
## 这个命令立即创建了类： 1:1, 1:2, 1:3

# tc qdisc add dev eth0 parent 1:1 handle 10: sfq
# tc qdisc add dev eth0 parent 1:2 handle 20: tbf rate 20kbit buffer 1600 limit 3000
# tc qdisc add dev eth0 parent 1:3 handle 30: sfq 
```

### 2.5.CBQ
- 太复杂

### 2.6.HTB
- HTB(Hierarchical Token Bucket, 分层的令牌桶)
- 类似一个层级的 TFB 

#### 2.6.1.参数
- rate: 限制速率
- burst： 桶大小
- ceil： 最大速率

#### 2.6.2.例子
```sh
tc qdisc add dev eth0 root handle 1: htb default 30
tc class add dev eth0 parent 1: classid 1:1 htb rate 6mbit burst 15k
tc class add dev eth0 parent 1:1 classid 1:10 htb rate 5mbit burst 15k
tc class add dev eth0 parent 1:1 classid 1:20 htb rate 3mbit ceil 6mbit burst 15k
tc class add dev eth0 parent 1:1 classid 1:30 htb rate 1kbit ceil 6mbit burst 15k 
# 在叶子节点添加类
tc qdisc add dev eth0 parent 1:10 handle 10: sfq perturb 10
tc qdisc add dev eth0 parent 1:20 handle 20: sfq perturb 10
tc qdisc add dev eth0 parent 1:30 handle 30: sfq perturb 10 
```

## 3.tc命令

- 列出已有的队列：
```sh
tc qdisc ls dev eth0
#显示详细信息
tc -s -d qdisc ls dev eth0
```

- 列出已有的class：
```sh
tc class ls dev eth2
#显示详细信息
tc -s -d class ls dev eth2
```

- 删除队列：
```sh
tc qdisc del dev eth0 root
```

- 删除class：
```sh
sudo  tc class delete dev eth2 parent 1: classid 1:18
```

- 更新class:
```sh
sudo tc class replace dev eth2 parent 1: classid 1:18 htb rate 30mbit
``` 

## 4.tc测试
```sh
# set tc
tc qdisc add dev eth2 root handle 1: htb
tc class add dev eth2 parent 1: classid 1:2 htb rate 100mbit
tc class add dev eth2 parent 1: classid 1:3 htb rate 10mbit
tc class add dev eth2 parent 1: classid 1:4 htb rate 1mbit
tc class add dev eth2 parent 1: classid 1:5 htb rate 100kbit
tc class add dev eth2 parent 1: classid 1:6 htb rate 10kbit
tc class add dev eth2 parent 1: classid 1:7 htb rate 1kbit
tc filter add dev eth2 protocol ip parent 1: prio 1 handle 1: cgroup

# set cgroup
sudo mkdir /sys/fs/cgroup/net_cls/test/
echo $$ > /sys/fs/cgroup/net_cls/test/cgroup.proc
echo '0x10004' > /sys/fs/cgroup/net_cls/test/net_cls.classid 
```

## 5.设计分组
```sh
tc qdisc add dev eth2 root handle 1: htb default 1
#不限制带宽，按优先级分为3组，
tc class add dev eth2 parent 1: classid 1:1 htb rate 100mbit ceil 100mbit prio 1
tc class add dev eth2 parent 1: classid 1:2 htb rate 100mbit ceil 100mbit prio 1
tc class add dev eth2 parent 1: classid 1:3 htb rate 100mbit ceil 100mbit prio 1
tc class add dev eth2 parent 1: classid 1:4 htb rate 100mbit ceil 100mbit prio 1
tc class add dev eth2 parent 1: classid 1:5 htb rate 100mbit ceil 100mbit prio 1
tc class add dev eth2 parent 1: classid 1:6 htb rate 100mbit ceil 100mbit prio 1
tc class add dev eth2 parent 1: classid 1:7 htb rate 100mbit ceil 100mbit prio 1
tc class add dev eth2 parent 1: classid 1:8 htb rate 200mbit ceil 200mbit prio 1
tc class add dev eth2 parent 1: classid 1:9 htb rate 200mbit ceil 200mbit prio 1
tc class add dev eth2 parent 1: classid 1:10 htb rate 200mbit ceil 200mbit prio 1
tc class add dev eth2 parent 1: classid 1:11 htb rate 200mbit ceil 200mbit prio 1
tc class add dev eth2 parent 1: classid 1:12 htb rate 200mbit ceil 200mbit prio 1
tc class add dev eth2 parent 1: classid 1:13 htb rate 500mbit ceil 500mbit prio 1
tc class add dev eth2 parent 1: classid 1:14 htb rate 500mbit ceil 500mbit prio 1
tc qdisc add dev eth2 parent 1:1 handle 2: sfq perturb 10
tc filter add dev eth2 protocol ip parent 1: prio 1 handle 1: cgroup
```

## 6.其他命令
```sh
#查看多队列网卡
#Ethernet controller的条目内容，如果有MSI-X && Enable+ && TabSize > 1
sudo lspci -vvv
```
```sh
#查看cpu状态
mpstat -P ALL
```
```sh
#查看网卡信息
sudo ethtool eth2
```
```sh
#查看中断
cat /proc/interrupts
```

## ref：
- http://www.lartc.org/LARTC-zh_CN.GB2312.pdf
- http://luxik.cdi.cz/~devik/qos/htb/manual/userg.html
- http://chenlinux.com/2013/01/06/limit-bandwidth-of-one-process/
