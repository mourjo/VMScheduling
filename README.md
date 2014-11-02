# Vicc Scheduler

This repository contains the source code skeleton for the project developed
during the Vicc lecture at University Nice Sophia Antipolis.

* Lecture website: http://users.polytech.unice.fr/~hermenie/vicc/
* Contact: fabien.hermenier@unice.fr

# The basics

This project consists in developing a custom VM Scheduler that will be evaluated using
the [cloudsim simulator](http://www.cloudbus.org/cloudsim/) and a workload extracted from [PlanetLab](https://github.com/beloglazov/planetlab-workload-traces)


## Work to do

- Develop an instance of the VmScheduler.
  This requires to implement first VmSelectionPolicy, the class that is responsible of choosing a host for a Vm.
  This requires in addition to implement a VMAllocationPolicy that can migrate VMs to reconfigure the datacenter if needed


#Datacenter characteristics

See the class org.cloudbus.cloudsim.examples.power.Constants.java

- 800 Hosts
    - 400 HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB). [Power characteristics](http://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110127-00342.html)
    - 400 HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB). [Power characteristics](http://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110124-00339.html)

- 4 Type of VMs

- Workload: 10 random days of PlanetLab

- Energy Price:

- Cost of resources for the customers: (the incomes)

- Cost of SLA violations (refunds):

- Cost of Energy. We consider "Tarif Vert C Base - Très longue utilisation, majorée". The one that is aligned with
a continuous high consumption for a company. Ete heure pleine en permanence pour simplifier les calculs:
  3,919 c€/kWh

 [Official EDF costs for large companies](http://entreprises.edf.com/fichiers/fckeditor/Commun/Entreprises/pdf/2013/baremes/Tarif_vert_01082013.pdf)


# Cost model

The objective of this project is to develop a scheduler that reduce to a minimum the Total Cost of Ownership (TCO).
This cost will be the addition of the following parameters:

## The energy price

- tarif Jaune Base

- PUE 2

- Puissance:
    - G5: 135 W max
    - G3: 169 W max
    - total server consumption: 135 * 400 + 169 * 400 = 54,000 + 67,600 = 121,600 W
    - total site consumption = " * PUE = 243,200 W
    -> tarif vert A5 (250 kW - 3MW)

- The resource cost for customers
    - X for a MIPS
    - Y for a MB of RAM
    - Z for a MBIT/s

- hiver heure pleine: 5,820 c€/kWh

- prime fixe annuelle: 74,16€/kW par an / 365 -> 0,203€ par J / kW
-
- The cost for the provider of having a SLA violation:


- Disable the log when not needed. Might be very time consuming (Java Profiler to check where the time is spent)
The objective is then to maximize:  REVENUE - ENERGY - VIOLATIONS


- Bad values (the sample scheduler): XXX euros

- Good values: ???

- On somme sur les 10 jours


- API interressantes à connaitre:

- connaitre la conso d'une VM
- connaitre la demande d'une VM
- migrer une VM
- un noeud est'il surchargé ?


## Limitations of CloudSim

- All the VMs are launched at the beginning of the experiment
- solving duration is ignored
- VM booting duration is ignored as well
- Simplistic migration model


# Penalties computation

- 10% si SLA satisfait entre 99 et 99.95% sur la journée
- 30% si SLA satisfait < 99% sur la journée
 - si les MIPS consommée sont < théorie
 - sur 1 journée, censé avoir eu XX MIPs, a recu YY

 - prix des instances: http://aws.amazon.com/fr/ec2/previous-generation/