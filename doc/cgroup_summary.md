# cgroup docs

## 1 cgroup概念

### 1.1 cgroup
- cgroup是一个树状结构
- 系统中的进程组织成一颗一颗独立的树
- 树的每个节点是一个进程组

### 1.2 subsystem 
- subsystem一般来说就是一个资源控制器，一个内核模块
- 现在内核提供了12种subsystem，但是cgroup并没有限定subsystem行为，可能是对资源的限制，对资源进行统计，打标签
 
### 1.3 cgroup 与 subsystem 绑定
- 一个subsystem可以绑定到一颗cgroup树
- 但是多个subsystem可以绑定到同一颗cgroup树节点


## 2 操作
- 检测cgroup可用模块
```sh 
cat /proc/cgroups
```

- 启动net_cls模块
```sh
# check net_cls in kernel
grep CGROUP /boot/config-`uname -r` | grep CONFIG_NET_CLS_CGROUP

# find the modules name
find /lib/modules/`uname -r` -iname "*cgroup*"

# tell kernel to load net_cls subsystem
sudo modprobe cls_cgroup

# tell kernel load cls at the next reboot
sudo bash -c "echo cls_cgroup >> /etc/modules"
```

- 简单部署cgroup
```sh
# install cgroup-bin
# 安装完之后，重启会自动加载
# 加载可以通过 /etc/init/cgroup-lite.conf，/bin/cgroups-mount修改
sudo apt-get install cgroup-bin -y
# 加载net_cls模块
sudo modprobe cls_cgroup
# 绑定 net_cls 模块
sudo mkdir /sys/fs/cgroup/net_cls
sudo mount -t cgroup -o net_cls net_cls /sys/fs/cgroup/net_cls
```

- 将所有 subsystem 绑定到 cgroup 根节点（不建议）
```sh
mkdir /sys/fs/cgroup/
# 名字路径可以按需选择
#        类型    名字         路径
mount -t cgroup cgroup_root /sys/fs/cgroup
```

- 挂载一颗和cpu subsystem关联的cgroup树到/sys/fs/cgroup/cpu
```sh
mkdir /sys/fs/cgroup/
mount -t tmpfs cgroup_root /sys/fs/cgroup
mkdir /sys/fs/cgroup/cpu
#                  subsystem 名字 路径
mount -t cgroup -o cpu cpu /sys/fs/cgroup/cpu
```

- 控制cpu使用率
```sh
# 创建一个子树
mkdir /sys/fs/cgroup/cpu/test
cd /sys/fs/cgroup/cpu/test

# 限制只能使用1个CPU（每250ms能使用250ms的CPU时间）
echo 250000 > cpu.cfs_quota_us
echo 250000 > cpu.cfs_period_us

# 限制使用2个CPU（内核）（每500ms能使用1000ms的CPU时间，即使用两个内核）
echo 1000000 > cpu.cfs_quota_us 
echo 500000 > cpu.cfs_period_us 

# 限制使用1个CPU的20%（每50ms能使用10ms的CPU时间，即使用一个CPU核心的20%）
echo 10000 > cpu.cfs_quota_us
echo 50000 > cpu.cfs_period_us

# 将命令行进程加入组
echo $$ > cgroup.procs

## 运行一个消耗cpu的shell脚本
x=0
while [ True ];do
    x=$x+1
done;

## 查看进程cpu使用
top
```

## 3 P.S.
- ubuntu12.04 cgroup-bi:0.37.1-1ubuntu10.1 : cgconfig 系统重启会自动挂载
- ubuntu14.04 cgroup-bi:0.38-1ubuntu2 : cgroup-lite 会自动挂载所有子系统到 /sys/fs/cgroup

## 4 Ref

- find and load net_cls - https://serverfault.com/questions/485919/cannot-find-network-subsystem-in-cgroup-on-ubuntu-12-04-lts
- 官方文档 - https://www.kernel.org/doc/Documentation/cgroup-v1/cgroups.txt
- Cgroup概述 - https://segmentfault.com/a/1190000006917884
- 限制cgroup的CPU使用 - https://segmentfault.com/a/1190000008323952
- cgconfig service - https://www.systutorials.com/docs/linux/man/5-cgconfig.conf
- load memory - https://askubuntu.com/questions/417215/how-does-kernel-support-swap-limit?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa