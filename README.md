# Vicc project: homemade VM Schedulers

This project aims at developing different Vm schedulers for a given IaaS cloud. This was done as a graduate-level project at [Univeristy of Nice](http://unice.fr/). Each scheduler has meaningful properties for either the cloud customers or the cloud provider.

The implementation was made over the IaaS cloud simulator [CloudSim](http://www.cloudbus.org/cloudsim/). The simulator replays a workload extracted from Emulab, on a datacenter having realistic characteristics. 


## Dev team

- Rares Damaschin: damaschinrares@gmail.com
- Mourjo Sen: sen.mourjo@etu.unice.fr
- With help from Dr Fabien Hermenier: https://github.com/fhermeni


Some useful resources:

- CloudSim [FAQ](https://code.google.com/p/cloudsim/wiki/FAQ#Policies_and_algorithms)
- CloudSim [API](http://www.cloudbus.org/cloudsim/doc/api/index.html)
- CloudSim [source code](cloudsim-3.0.3-src.tar.gz)
- CloudSim [mailing-list](https://groups.google.com/forum/#!forum/cloudsim)

## Setting up the environment

Requires Java 7 + [maven](http://maven.apache.org) environment to develop.

First, clone this repository. The project directory is organized as follows:
```sh
$ tree
 |- src #the source code
 |- repository #external dependencies
 |- planetlab #the workload to process
 |-cloudsim-3.0.3-src.tar.gz # simulator sources
 \- pom.xml # maven project descriptor
```
Second, check everything is working by typing `mvn install` in the root directory
Third, Integrate the project with your IDE if needed

## How to test

`fr.unice.vicc.Main` is the entry point. It can be launch from your IDE or using the command `mvn compile exec:java`.

```sh
Usage: Main scheduler [day]
```

- `scheduler` is the identifier of the scheduler to test, prefixed by `--`.
- `day` is optional, it is one of the workload day (see folders in `planetlab`). When `all` is indicated all the days are replayed sequentially.

By default, the output is written in a log file in the `logs` folder.

If you execute the program through `mvn exec:java`, then the arguments are provided using the 'sched' and the 'day' properties.

- To execute the simulator using the `naive` scheduler and all the days:
`mvn compile exec:java -Dsched=naive -Dday=all`
- to replay only day `20110303`: `mvn compile exec:java -Dsched=naive -Dday=20110303`

## Project Tasks

You will find various VM schedulers and a few observers that check if the schedulers behave correctly.

Integration of the schedulers within the codebase is done inside the class `VmAllocationPolicyFactory`. To deploy new schedulers, change this class, with the appropriate option. See source code for details.

### A naive scheduler to start (`naive` flag)

This first scheduler was aimed only at discovering the CloudSim API. This scheduler simply places each `Vm` to the first `Host` with enough free capacity. That is, first fit policy. Use this simple scheduler as a base for implementing others.

	
### Anti-affinity scheduler (`antiAffinity` flag)

This VM scheduler considers VMs running replicated applications. To make them fault-tolerant to hardware failure, the customer expects to have the replicas running on distinct hosts.

This scheduler (`antiAffinity` flag) places the Vms with regard to their affinity. Assumed client requirement: All VMs with an id between [0-99] should be on distinct nodes, as should VMs having an id between [100-199], [200-299], ... .

We use a hash map to keep track of all hosts that do not run a VM from a class (ie, one map entry = all eligible hosts for a VM class). We got a VM's class by dividing its ID by 100.

Time complexity of allocation per allocation: `O(q)`, q = Number of eligible hosts for a VM class 

##### What is the impact of such an algorithm over the cluster hosting capacity?
If the VMs are well-distributed over the VM classes, then this strategy acts as a load balancer, thus starting up many hosts, atleast as many as the largest class.

##### Summary of results

- Incomes:    € 12398.59
- Penalties:  € 200.95
- Energy:     € 2688.44
- Revenue:    € 9509.21

### Load balancing scheduler (`balance` flag)

Balancing the load is useful to avoid to alter specific hosts prematurely. It is also convenient to minimize the probability of saturating a host.

Idea: To perform load balancing with regard to the MIPS available on each host. This should observe fewer penalties with regard to the naive scheduler. 

We sort all hosts in decreasing order of available MIPS. Then we try to allocate VMs in that order.

Time Complexity per allocation: `O(n log(n))`, n = Number of hosts

We implemented a BalanceObserver to check if the load balancing indeed works or not based on the following metric. We used the standard deviation of all hosts' percentage MIPS utilization. We used standard deviation of utilization because it shows a good variation in the load, which is what we want to measure. We used percentage utilization because different hosts have different total MIPS.
By default the observer is not called (commented) in `Observers.java`. It saves the logs in a separate file in the classpath.

##### Summary of results

- Incomes:    € 12398.59
- Penalties:  € 6.06
- Energy:     € 3266.29
- Revenue:    € 9126.24

### No SLA Violations scheduler (`noViolations` flag)

For a practical understanding of what an SLA violation is in this project, take a look at the `Revenue` class. Basically, there is a SLA violation when the associated VM is asking for more MIPS it is possible to get on its host.

If the SLA is not met then the provider must pay penalties to the client. It is, therefore, not desirable to have violations to attract customers and maximize the revenues.

This scheduler that ensures there are no SLA violations (see results). 

We allocate a VM to a host if it has a processing element with enough available MIPS to run the VM. This ensures a zero penalty across all days.

Time Complexity per allocation: `O (n*m)`, where n = Number of hosts, m = Number of PEs* per host
*PE = Processing element (cores in a CPU)

##### Summary of results

- Incomes:    € 12398.59
- Penalties:  € 0.00
- Energy:     € 2868.74
- Revenue:    € 9529.85


### Energy-efficient schedulers

#### Static version (`statEnergy` flag)

This scheduler reduces the overall energy consumption without relying on VM migrations. The resulting simulation consumes less energy than all the previous schedulers.

We sort the hosts according to increasing order of available MIPS. Therefore we try to allocate VMs to the least available hosts first. By using `getAvailableMips()` we also sort the hosts by lower power model first, which is why we did not implement the sorting by power model.
We also tried sorting hosts in increasing order of maximum PE* (per host) available MIPS, but this had a slightly worse result, so we did not keep it.
*PE = Processing element (cores in a CPU)

Time Complexity per allocation: `O(n log(n))`, n = Number of hosts

##### Summary of results

- Incomes:    € 12398.59
- Penalties:  € 1413.50
- Energy:     € 2604.30
- Revenue:    € 8380.79

#### Dynamic version (`dynEnergy` flag)

This works like the previous scheduler but is modified to rely on VM migration to continuously improve the Vm placement. The resulting simulation consumes less energy than the static version (although there may be more violations).

`optimizeAllocation()` is implemented to notify which VM to migrate. The returned list is the sequence of migrations to perform. Each entry is a map that only contains the Vm to migrate (key `vm`) and the destination host (key `host`). For example:

```java
public List<Map<String,Object>> optimizeAllocation(List<Vm> vms) {
	List <Map<String,Object> map = new ArrayList<>();
	Map<String,Object> m1 = new HashMap<>();
	m1.put("vm", vms.get(0));
	m1.put("host", getHostList().get(0));
	map.add(m1);
	Map<String,Object> m2 = ...
	...
	return map;
}
```

Allocation of VMs is same as static energy scheduler. 
For migration, we tried a couple of strategies, which failed. When we debugged them, we found that calling `getVmList()` on a host returns an empty list. This was very unexpected because we expected it to be a basic functionality of a Host object. Because of this we used the `vms` list passed as a parameter and observed that they were all hosted on hosts with power model Xeon3075, which is more power consuming. Thus we assumed this will always be the case and we migrated all VMs in the `vms` list to lower power consuming hosts (the other hosts). And this works, see results.

Time Complexity per migration: `O(n*k)` where n = Number of hosts, k = Number of VMs on a host
Because migrations are being done, the total running time for the scheduler is long, but we profiled our migration function, and it is ~10 ms mostly per invocation.

##### Summary of results

- Incomes:    € 12398.59
- Penalties:  € 6312.94
- Energy:     € 2330.08
- Revenue:    € 3755.58



### Greedy scheduler (`greedy` flag)

This scheduler maximizes the revenue (finally!). It is important to provide a good tradeoff between energy savings and penalties for SLA violation. For this, some tradeoffs have to be made, for example, between enery consumption, SLA violation prevention and laod balancing.

#### Allocation:
We first sort the hosts in increasing order of available MIPS, same as the static energy scheduler. Then we use the same strategy as for the no SLA violations scheduler, with a margin of 500 MIPS. This allows a few SLA violations but saves a lot of energy, maximizing revenue.

Time Complexity per allocation: `O(n log(n))`, n = Number of hosts. Assuming number of PEs (in a host) m < log(n)

#### Migration:
We tried a lot of strategies for migration, but it was never cost effective. So no migrations were performed.

##### Summary of results

- Incomes:    € 12398.59
- Penalties:  € 7.24
- Energy:     € 2754.93
- Revenue:    € 9636.42
