# Vicc project: homemade VM Schedulers

This project aims at developing different VM schedulers for a given IaaS cloud. Each schedule will have meaningful properties for either the cloud customers or the cloud provider.

The implementation and the evaluation will be made over the IaaS cloud simulator (CloudSim) [http://www.cloudbus.org/cloudsim/] using real workload and a datacenter with realistic characteristics.

This project is individual and will be evaluated accordingly. 

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


## Usefull resources:

- CloudSim (FAQ)[https://code.google.com/p/cloudsim/wiki/FAQ#Policies_and_algorithms]
- CloudSim (API)[http://www.cloudbus.org/cloudsim/doc/api/index.html]
- CloudSim source code ...

## Exercices

For this project, you will have to develop various VM schedulers (_VmAllocationPolicy_ in CloudSim terminology) and a few observers that check if your schedulers behave correctly.

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
	

