package fr.unice.vicc;

import org.cloudbus.cloudsim.VmAllocationPolicy;

/**
 * @author Fabien Hermenier
 */
public interface VmScheduler {

    VmAllocationPolicy getVmAllocationPolicy();

    String getName();
}
