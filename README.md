# Vicc project: homemade VM Schedulers

This project aims at developing different VM schedulers for a given IaaS cloud. Each schedule will have meaningful properties for either the cloud customers or the cloud provider.

The implementation and the evaluation will be made over the IaaS cloud simulator [CloudSim](http://www.cloudbus.org/cloudsim/) using real workload and a datacenter with realistic characteristics. 

Some usefull resources:

- CloudSim [FAQ](https://code.google.com/p/cloudsim/wiki/FAQ#Policies_and_algorithms)
- CloudSim [API](http://www.cloudbus.org/cloudsim/doc/api/index.html)
- CloudSim source code ...

## Setting up the environment

You must have a working Java 7 + maven environment to code and Git to manage the sources. No IDE is necessary but feel free to use it

1. clone this repository. The project directory is organized as follow:
```sh
|- src #the source code
|- repository #external dependencies
|- planetlab #the workload
\- pom.xml # maven project descriptor
```
2. check everything is working by typing `mvn install` in the root directory
3. Integrate the project with your IDE if needed


## Exercices

For this project, you will have to develop various VM schedulers (`VmAllocationPolicy` in CloudSim terminology) and a few observers that check if your schedulers behave correctly.

To integrate your schedulers within the codebase, you will have to declare your schedulers inside the class `VmAllocationPolicyFactory`.

### A naive scheduler to start

This first scheduler aims only at discovering the CloudSim API. This scheduler is a simple `FirstFit` algorithm that place each `Vm` to the first `Host` with enough free capacity.

The (FAQ) indicates you have to extend `VmAllocationPolicy`. This class allows to compute the placement for a VM but also to indicate where a Vm is running

1. Just create the new class, integrate it into `VmAllocationPolicyFactory`. The flag to call this scheduler for the command line interface (CLI) will be "naive". Test if the integration is correct. The code shall crash in your class but that's normal at this level.
2. Implements the easy part of the scheduler that is to indicate where a Vm runs. This is done by the `getHost(Vm)` and the `getHost(int, int)` methods

3. Implements `allocateHostForVm`, the method to force a given `Vm` to be hosted on a given `Host`. Look at the method `Host.vmCreate(Vm)`.

4. Implements `deallocateHostForVm`, the method that remove a running `Vm` from its hosting node

5. The scheduler is static. `optimizeAllocation` must returns `null`

6. Now, let's implement `allocateHostForVm(Vm)` that is the main method of this class. As we said, the scheduler is very simple, it just schedule the `Vm` on the first appropriate `Host`
 
7. Test your simulator on a single day (look at the `Main` class to get the appropriate parameters). If all simulation runs successfully, all the VMs have been scheduled, all the applications (Cloudlet in the CloudSim terminology) ran, and the provider revenues are displayed.

8. Test your simulator on all the days consequently.

At this level, it is ok to have penalties due to SLA violations
	
### Support for Highly-Available applications

Let consider the VMs run replicated applications. To make them fault-tolerant to hardware failure, the customer expects to have the replicas running on distinct hosts.

1. Implement a new VM Scheduler (flag `antiAffinity`) that place VMs with regards to their affinity. In practice, all Vms with an id between [0-100] must be on distinct nodes, the same with VMs having an id between [101-200], [201-300], ... .

2. To check the scheduler is effective, implements an observer. A sample one is `PeakPowerObserver`. Basically, your observer must be called every simulated second to confirm VMs of a same group are all hosted on distinct nodes or to indicate which of the Vms are co-located despite the constraint.  

### Get rid of SLA violations

For a practical understanding of what a SLA violation is here, look at the `Revenue` class. Basically, there is a SLA violation when the Vm is requiring more MIPS it can get on its node.

If the SLA is not met then the provider must pay penalties to the client. It is then not desirable to have violations as it reduces the provider revenues.

Develop a scheduler that ensures their is no SLA violation (`noViolations` flag).
Practically, the scheduler must ensure chosen hosts will have at any moment 1 free processing unit (Pes in CloudSim terminology) with enough MIPS capacity to support the peak demand in terms of mips (`Vm.getMips()`) for all the Vms they host.

Your scheduler is effective when you can succesfully simulates all the days, with the `Revenue` class reporting no refundings.

### Balance the load

Balancing the load is usefull to avoid to alter specific nodes prematurely. It is also convenient to minimize the probabilities of saturating a node.

1. Develop a scheduler that perform load balacing (`balance` load) and ensures their is no SLA violation
2. Develop an observer to evaluate every second the balancing rate. The observer must output the balancing rate 

### Energy-efficient algorithm

#### static version

Develop a scheduling algorithm (`statEnergy` flag) that reduced the overall energy consumption without relying on Vm migration.

#### dynamic version

Copy the previous scheduler and modify to rely on Vm migration to continuously improve the Vm placement (`dynEnergy` flag). This can be achieved using VM migration. To do so, you must implement `optimizeAllocation` to indicate where to relocate the Vms.

1. Develop a first version that only saves more energy than the static version
2. Develop an advanced version that maximize the revenues. It is then important to provide a good mix between energy savings and penalties for SLA violation