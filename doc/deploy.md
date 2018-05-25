# Deploy

## 1 cgroup

### 1.1 ubuntu14.04 cgroup-bi:0.38-1ubuntu2

```sh
# check is allow need subsystem loaded 
cat /proc/cgroups

# if net_cls not exist check whether can loaded
grep CGROUP /boot/config-`uname -r` | grep CONFIG_NET_CLS_CGROUP
find /lib/modules/`uname -r` -iname "*cgroup*"

# tell kernel to load net_cls subsystem
sudo modprobe cls_cgroup

# tell kernel load cls at the next reboot
sudo bash -c "echo cls_cgroup >> /etc/modules"

# install cgroup-bin
sudo apt-get install cgroup-bin -y

# manully mount cgroup vfs
sudo mount -t tmpfs cgroup /sys/fs/cgroup
sudo mkdir /sys/fs/cgroup/cpu
sudo mount -t cgroup cpu -ocpu /sys/fs/cgroup/cpu

sudo mkdir /sys/fs/cgroup/memory
sudo mount -t cgroup memory -omemory /sys/fs/cgroup/memory

sudo mkdir /sys/fs/cgroup/net_cls/
sudo mount -t cgroup net_cls/ -onet_cls/ /sys/fs/cgroup/net_cls

# create the storm subgroup and change the own
sudo mkdir /sys/fs/cgroup/cpu/storm
sudo mkdir /sys/fs/cgroup/memory/storm
sudo mkdir /sys/fs/cgroup/net_cls/storm
sudo chown hiido:hiido -R  /sys/fs/cgroup/cpu/storm
sudo chown hiido:hiido -R  /sys/fs/cgroup/memory/storm
sudo chown hiido:hiido -R  /sys/fs/cgroup/net_cls/storm

# change the defaut value to avoid exception from MemoryCore.getPhysicalUsageLimit()
echo 4611686018427387904 > /sys/fs/cgroup/memory/storm/memory.limit_in_bytes
```

### 1.2 ubuntu12.04 cgroup-bi:0.37.1-1ubuntu10.1
```sh
# check is allow need subsystem loaded 
cat /proc/cgroups

# if net_cls not exist check whether can loaded
grep CGROUP /boot/config-`uname -r` | grep CONFIG_NET_CLS_CGROUP
find /lib/modules/`uname -r` -iname "*cgroup*"

# tell kernel to load net_cls subsystem
sudo modprobe cls_cgroup

# tell kernel load cls at the next reboot
sudo bash -c "echo cls_cgroup >> /etc/modules"

# install cgroup-bin
sudo apt-get install cgroup-bin -y

# modify /etc/cgconfig.conf
# content as below
"
mount {
	cpuset	= /sys/fs/cgroup/cpuset;
	cpu     = /sys/fs/cgroup/cpu;
	cpuacct	= /sys/fs/cgroup/cpuacct;
	memory	= /sys/fs/cgroup/memory;
	devices	= /sys/fs/cgroup/devices;
	freezer	= /sys/fs/cgroup/freezer;
	net_cls	= /sys/fs/cgroup/net_cls;
	blkio	= /sys/fs/cgroup/blkio;
}
"

# restart the cgroup service
sudo service cgconfig restart

# if not ok please check /etc/init/cgconfig.conf
```

## 2 tc
```sh
# machine max bandwith is 100mbit
# default 10mbit with Fairness queue
# else with 10mbit，20mbit，50mbit three level queue

# root queue
sudo tc qdisc add dev eth2 root handle 1: htb default 1
sudo tc class add dev eth2 parent 1: classid 1: htb rate 100mbit

# default queue with Stochastic Fairness Queueing
sudo tc class add dev eth2 parent 1: classid 1:1 htb rate 10mbit
sudo tc qdisc add dev eth2 parent 1:1 handle 2: sfq perturb 10

# add queue which should be  monopolized by worker
sudo tc class add dev eth2 parent 1: classid 1:2 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:3 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:4 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:5 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:6 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:7 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:8 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:9 htb rate 10mbit
sudo tc class add dev eth2 parent 1: classid 1:10 htb rate 10mbit

sudo tc class add dev eth2 parent 1: classid 1:11 htb rate 20mbit
sudo tc class add dev eth2 parent 1: classid 1:12 htb rate 20mbit
sudo tc class add dev eth2 parent 1: classid 1:13 htb rate 20mbit
sudo tc class add dev eth2 parent 1: classid 1:14 htb rate 20mbit
sudo tc class add dev eth2 parent 1: classid 1:15 htb rate 20mbit

sudo tc class add dev eth2 parent 1: classid 1:16 htb rate 50mbit
sudo tc class add dev eth2 parent 1: classid 1:17 htb rate 50mbit

# add filter control tc with cgroup
sudo tc filter add dev eth2 protocol ip parent 1:0 prio 1 handle 1: cgroup
```

## storm

```sh
# depoly zookeeper first
# depoly storm tar
# modify the conf
# start nimbus， supervisor， ui
```


