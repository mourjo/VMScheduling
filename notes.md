# Notes about the project
It was a very interesting project. It was the only practical hands-on project that we had to do this year (as Ubinet students), and it was refreshing!
Though it would've helped if the API was better documented.

## The team

- Rares Damaschin: damaschinrares@gmail.com
- Mourjo Sen: sen.mourjo@gmail.com

## Comments
Here's what we did for each scheduler (the detailed results are in the /results folder):

### Anti-affinity scheduler (antiAffinity flag)
We use a hash map to keep track of all hosts that do not run a VM from that class (all eligile hosts for a VM class). We got a VM's class by dividing its ID by 100.

Time complexity of allocation: O(1) per allocation

What is the impact of such an algorithm over the cluster hosting capacity?
If the VMs are well-distributed over the VM classes, then this strategy acts as a load balancer, thus starting up many hosts, atleast as many as the largest class.

Summary of results
- Incomes:    12398.59
- Penalties:  200.95
- Energy:     2688.44
- Revenue:    9509.21


### Load balancing scheduler (balance flag)
We sort all hosts in decreasing order of available MIPS. Then we try to allocate VMs in that order.

Time Complexity: O(n log(n)), n = Number of hosts

We implemented a BalanceObserver based on the following metric. We used the standard deviation of all hosts' percentage MIPS utilization. We used standard deviation of utilization because it shows a good variation in the load, which is what we want to measure. We used percentage utilization because different hosts have different total MIPS.
By default the observer is not called (commented) in Observers.java. It saves the logs in a separate file in the classpath.

Summary of results
- Incomes:    12398.59
- Penalties:  6.06
- Energy:     3266.29
- Revenue:    9126.24


### No SLA Violations scheduler (noViolations flag)
We allocate a VM to a host if it has a processing element with enough available MIPS to run the VM. This ensures a zero penalty accross all days.

Time Complexity: O (n*m), where n = Number of hosts, m = Number of PEs per host

Summary of results
- Incomes:    12398.59
- Penalties:  0.00
- Energy:     2868.74
- Revenue:    9529.85


### Static energy efficient scheduler (statEnergy flag)
We sort the hosts according to increasing order of availabile MIPS. Therefore we try to allocate VMs to the least available hosts first. By using getAvailableMips() we also sort the hosts by lower power model first, which is why we did not implement the sorting by power model.
We also tried sorting hosts in increasing order of maximum PE (per host) available MIPS, but this had a slightly worse result, so we did not keep it.

Time Complexity: O(n log(n)), n = Number of hosts

Summary of results
- Incomes:    12398.59
- Penalties:  1413.50
- Energy:     2604.30
- Revenue:    8380.79

### Dynamic energy efficient scheduler (dynEnergy flag)
Allocation of VMs is same as static energy scheduler. 
For migration, we tried a couple of strategies, which failed. When we debugged them, we found that calling getVmList() on a host returns an empty list. This was very unexpected because we expected it to be a basic functionality of a Host object. Because of this we used the vms list passed as a parameter and observed that they were all hosted on hosts with power model Xeon3075, which is more power consuming. Thus we assumed this will always be the case and we migrated all VMs in the vms list to lower power consuming hosts (the other hosts). And this works, see results.

Time Complexity for migration: O(n*k) where n = Number of hosts, k = Number of VMs on a host
Because migrations are being done, the total running time for the scheduler is long, but we profiled our migration function, and it is ~10 ms mostly per invocation.

Summary of results
- Incomes:    12398.59
- Penalties:  6312.94
- Energy:     2330.08
- Revenue:    3755.58


### Greedy scheduler (greedy flag)
Allocation:
	We first sort the hosts in increasing order of available MIPS, same as the static energy scheduler. Then we use the same strategy as for the no SLA violations scheduler, with a margin of 500 MIPS. This allows a few SLA violations but saves a lot of energy, maximizing revenue.

	Time Complexity: O(n log(n)), n = Number of hosts. Assuming number of PEs in a host m < log(n)
Migration:
	We tried a lot of strategies for migration, but it was never cost effective. So no migrations were performed.

Summary of results
- Incomes:    12398.59
- Penalties:  7.24
- Energy:     2754.93
- Revenue:    9636.42
