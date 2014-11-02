package fr.unice.vicc;

import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyRandomSelection;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NaiveVmScheduler implements VmScheduler {

    private List<PowerHost> hosts;

    public NaiveVmScheduler(List<PowerHost> hosts) {
        this.hosts = hosts;
    }

    @Override
    public VmAllocationPolicy getVmAllocationPolicy() {
        //return new VmAllocationPolicySimple(hosts);
        //return new PowerVmAllocationPolicySimple(hosts);
        return new PowerVmAllocationPolicyMigrationStaticThreshold(hosts, new PowerVmSelectionPolicyRandomSelection(), 0.8);

    }

    @Override
    public String getName() {
        return "naive";
    }
}
