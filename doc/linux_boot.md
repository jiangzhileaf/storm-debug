# linux 启动

## sysvinit
- 大部分linux启动之后，运行init程序，读取/etc/inittab
- 根据runlevel N，线性运行/etc/rcN.d目录下程序
- rcN.d目录下，程序文件名的格式为： S/K + NN + NAME，
- init会杀掉所有以K开头的程序，启动以S开头的程序，
- 按照NN的大小，从低到高开始启动/停止程序
- /etc/rcN.d 目录下的程序都会软链接到 /etc/init.d
- sysvinit 线性启动，效率低
- sysvinit 服务是预设的，不能实时启动


## upstart
- Upstart基于事件机制的,可以按需启动服务,性能较好,兼容sysvinit
- rc-sysinit任务获取runlevel，rc任务启动/etc/rcN.d程序，完成兼容
- 所有的init作业都必须放置于目录/etc/init/之下，由upstart扫描执行，
- /etc/init下有很多***.conf 文件，每个conf文件都描述了一个job，
- 这个文件中指出作业什么start，什么时候stop，主进程是什么等


## 总结

##### 三个目录
- /etc/rcN.d/ 是System V init系统启动时查找的服务程序目录。
- /etc/init.d/ 目录是System V init系统真正的服务程序所在地。
- /etc/init/ 是Upstart系统寻找作业配置文件的地方。

##### 两个文件
- /etc/inittab 是System V init系统的配置文件，其中有设置默认的运行级别。
- /etc/rc.local 是一个用户常用来添加系统启动脚本的地方。

##### 一个命令
- service 是用来操作System V init脚本或Upstart作业的命令接口。

## 模块加载
module-init-tools.conf

## ref
https://www.hi-linux.com/posts/45475.html