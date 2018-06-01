# Storm Scheduler Class Summary

## Contents:

- [Storm Scheduler Class Summary](#storm-scheduler-class-summary)
    - [Contents:](#contents)
    - [1.BlacklistScheduler](#1blacklistscheduler)
        - [1.1.DefaultBlacklistStrategy](#11defaultblackliststrategy)
    - [2.DefaultScheduler](#2defaultscheduler)
    - [3.EvenScheduler](#3evenscheduler)
    - [4.IsolationScheduler](#4isolationscheduler)
    - [5.MultitenantScheduler](#5multitenantscheduler)
        - [5.1.config](#51config)
        - [5.2.RoundRobinSlotScheduler](#52roundrobinslotscheduler)
    - [6.ResourceAwareScheduler](#6resourceawarescheduler)
        - [6.1.config](#61config)
        - [6.2.Priority Strategy](#62priority-strategy)
            - [6.2.1.DefaultSchedulingPriorityStrategy](#621defaultschedulingprioritystrategy)
            - [6.2.2.FIFOSchedulingPriorityStrategy](#622fifoschedulingprioritystrategy)
        - [6.3.Aware Strategy](#63aware-strategy)
            - [6.3.1.DefaultResourceAwareStrategy](#631defaultresourceawarestrategy)
            - [6.3.2.ConstraintSolverStrategy](#632constraintsolverstrategy)
            - [6.3.3.GenericResourceAwareStrategy](#633genericresourceawarestrategy)

-----------------------------------------------------

## 1.BlacklistScheduler 


- blacklist scheduler is design with decorator pattern.
- it is default scheduler and user just can choose its underlying scheduler.
- BlacklistScheduler compare the supervisor set between cache and the new cluster's to get the bad supervisor host. and then update the cluster badlist host
- if the supervisor disappear exceed tolerance Time, it would be delete from cache

### 1.1.DefaultBlacklistStrategy

- if supervisor is bad, it would stay in blacklist until reach the resume time
- if worker is not enough, scheduler will release some bad supervisor

-----------------------------------------------------

## 2.DefaultScheduler

- storm default scheduler, it is simple;

``` 
for needAssignTopologys
    check bad slot;
    free them;
    assign topology;
```

- it call EvenScheduler lib to schedule

-----------------------------------------------------

## 3.EvenScheduler


- it is a schedule tool lib,
- also implement the Ischeduler interface,
- it can be used as a scheduler

-----------------------------------------------------

## 4.IsolationScheduler

- *never use in code

```
for each isolated topology:
   compute even distribution of executors -> workers on the number of \ workers specified for the topology.
   
   compute distribution of workers to machines
   
   determine host -> list of [slot, topology id, executors]
   
   iterate through hosts and: a machine is good if:
        1. only running workers from one isolated topology
        
        2. all workers running on it match one of the distributions of executors for that topology
        
        3. matches one of the # of workers blacklist the good hosts and remove those workers from the list of need to be assigned workers otherwise unassign all other workers for isolated topologies if assigned
```


- Schedule process:
```
get host -> all assignable worker slots for non-blacklisted machines (assigned or not assigned)

will then have a list of machines that need to be assigned (machine -> [topology, list of list of executors])

match each spec to a machine (who has the right number of workers), 

free everything else on that machine and assign those slots (do one topology at a time)

blacklist all machines who had production slots defined

log isolated topologies who weren't able to get enough slots / machines

run default scheduler on isolated topologies that didn't have enough slots + non-isolated topologies on remaining machines

set blacklist to what it was initially
```

- use config to control some topology would monopolize how many machine isolation.scheduler.machines = { "topology-name": 5 }

- isolated topology has the highest priority

- after assign all isolated topology, 
- then use the DefaultScheduler to allocate the rest topology

-----------------------------------------------------

## 5.MultitenantScheduler

- user which have configed would have particular slots as a isolated pool,
the other user would share the rest slots as a default pool.

- it would allocate topology in isolated pool first, then the default pool.

### 5.1.config

- multitenant.scheduler.user.pools: A map from the user name to the number of machines that should that user is allowed to use
- topology.isolate.machines: The number of machines that should be used by this topology to isolate it from all others
- topology.spread.components : Array of components that scheduler should try to place on separate hosts

### 5.2.RoundRobinSlotScheduler
- MultitenantScheduler internal scheduler

- it would create for each topology, when it created, it would save the spread componet and group the executor to slots, and order to assign each group of slots to node and one spread executor, and the last slot would assign with the rest of the spread executors

```
workerNum = 2

comp1 => [exec1_1,exec1_2]
comp2 => [exec2_1,exec2_2,exec2_3]
comp3 => [exec3_1_spread,exec3_2_speard,exec3_3_speard]

slots1 => [exec1_1,exec2_1,exec2_3]
slots2 => [exec1_2,exec2_2]
spread => [exec3_1_spread,exec3_2_speard]

node1 => slots1 + exec3_1_spread
node2 => slots2 + exec3_2_speard + exec3_3_speard
```

-----------------------------------------------------

## 6.ResourceAwareScheduler

- ResourceAwareScheduler is the most complex scheduler, it is used to schduler by cluster resource, also provide Multitenant function.
- it can be seperated to two parts, sort the topology which need to schedule with priority strategy, and schedule them with scheduler strategy.
- if the resource is not enough, it would try to release the lower priority topology from low to high. 
- then if the resource is still not enough, just throw error message 

- it will use storm.network.topography.plugin to get the supervisor rack infomation, and try to allocate all topology executor in same rack

### 6.1.config
- resource.aware.scheduler.user.pools         => config user resource pool
- resource.aware.scheduler.priority.strategy  => config topology priority strategy 
- storm.network.topography.plugin             => given a list of supervisor hostnames, this class would return a list of rack names and is used in the resource aware scheduler.
- topology.scheduler.strategy                 => config real schedule strategy
- topology.scheduler.favored.nodes            => A list of host names that this topology would prefer to be scheduled on (no guarantee)
- topology.scheduler.unfavored.nodes          => A list of host names that this topology would prefer to NOT be scheduled on (no guarantee)
- topology.ras.constraints                    => used by ConstraintSolverStrategy, A List of pairs (also a list) of components that cannot coexist in the same worker
- topology.spread.components                  => Array of components that scheduler should try to place on separate hosts
- topology.ras.constraint.max.state.search    => used by ConstraintSolverStrategy, max search time

### 6.2.Priority Strategy

#### 6.2.1.DefaultSchedulingPriorityStrategy

- first，topology group by user，

- topology sort by priority （the smaller， the higher ） and then startup timestamp （the bigger， the higher）

- second，the user sort by its highest priority topology score.

- score calc:
```
wouldBeCpu = assignedCpu + topologyTotalRequestedCpu;
wouldBeMem = assignedMemory + topologyTotalResquestedMemory;
cpuScore = (wouldBeCpu - guaranteedCpu)/availableCpu;
memScore = (wouldBeMem - guaranteedMemory)/availableMemory;
score = max(cpuScore, memScore)
```

- final priority:
```
higher user -> lower user
after one user finish then next user
higher topology -> lower topology
```

#### 6.2.2.FIFOSchedulingPriorityStrategy

- FIFOSchedulingPriorityStrategy is child of DefaultSchedulingPriorityStrategy

- topology just sort by startup timestamp （the bigger， the higher）

- score calc: topology uptime

- the rest flow is the same as DefaultSchedulingPriorityStrategy

### 6.3.Aware Strategy

#### 6.3.1.DefaultResourceAwareStrategy

- unsigned Executors sort by connections number, then its child and parent oredered by connections, then next executors

- rack is the physical machine network, storm try to allocate executors into same rack which need to connect with each other

- calc the rack total resouce, available resouce, and the use percentage rock sort by the existing executor num, then sort by min resource uesd percetage, last sort by resouce average used percetage

- nodes sort by rock ,then sort by the existing executor num, then sort by min resource uesd percetage, last sort by resouce average used percetage

- then adjust the order with favored nodes and unfavored nodes

- allocate the executor in sorted order to the nodes also in sorted order

#### 6.3.2.ConstraintSolverStrategy

- ConstraintSolverStrategy provide two constraint which we can control, 
- first, two componet' executor could not allocate in same worker,
- second, two executor of same component could not allocate in same worker.

- it would use Recursion to search the result, you can define the max search time in config, default is 100,000

```
*constraintMatrix, comp1.comp3.value =1 means  comp1 and comp3 could not in same worker
comp1 => { comp1 => 0, comp2 => 0, comp3 => 1}
comp2 => { comp1 => 0, comp2 => 0, comp3 => 1}
comp3 => { comp1 => 1, comp2 => 1, comp3 => 0}

executor sort by component constraint count 

nodes sort by rock ,
then sort by the existing executor num, 
then sort by min resource uesd percetage, 
last sort by resouce average used percetage,
then adjust the order with favored nodes and unfavored nodes

search(executor):
    for node in sortedNodes:
        for slot in availableSlot(node):
            if execuor assign to node resource and constraint are valid, then
                assign executor to node
                if all success,return success.
                result = search(next executor)
                if result is success, return success
                if exceed limit, return fail 
                backtrack
```

#### 6.3.3.GenericResourceAwareStrategy

- very similar with DefaultResourceAwareStrategy,
- it sort nodes very time after allocate a executor.
- it may get the better result, with the worse performance.
