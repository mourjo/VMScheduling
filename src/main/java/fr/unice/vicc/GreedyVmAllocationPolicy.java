package fr.unice.vicc;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

/**
 * @author Mourjo Sen & Rares Damaschin
 */
public class GreedyVmAllocationPolicy extends VmAllocationPolicy {

    //To track the Host for each Vm. The string is the unique Vm identifier, composed by its id and its userId
    private Map<String, Host> vmTable;

    public GreedyVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        vmTable = new HashMap<>();
    }

    public Host getHost(Vm vm) {
        // We must recover the Host which hosting Vm
        return this.vmTable.get(vm.getUid());
    }

    public Host getHost(int vmId, int userId) {
        // We must recover the Host which hosting Vm
        return this.vmTable.get(Vm.getUid(userId, vmId));
    }

    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host.vmCreate(vm)) {
            //the host is appropriate, we track it
            vmTable.put(vm.getUid(), host);
            return true;
        }
        return false;
    }

    public boolean allocateHostForVm(Vm vm) {
    	
    	Collections.sort(getHostList(), new Comparator<Host>() {
            @Override
            public int compare(Host h1, Host h2) {
        		return (int)(h1.getAvailableMips() - h2.getAvailableMips());
        		//available mips sorts according to their power model as well, we checked.
            }
        });

    	
    	for (Host h : getHostList()) {
    		
    		boolean suitableHost = false;
    		for(Pe processingElem : h.getPeList())
    		{
    			if(vm.getMips() - 500d < processingElem.getPeProvisioner().getAvailableMips())
    			{
    				suitableHost = true;
    				break;
    			}
    		}

    		if(suitableHost)
    		{
    			if (h.vmCreate(vm)) {
    				vmTable.put(vm.getUid(), h);
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public void deallocateHostForVm(Vm vm,Host host) {
        vmTable.remove(vm.getUid());
        host.vmDestroy(vm);
    }

    @Override
    public void deallocateHostForVm(Vm v) {
        //get the host and remove the vm
        vmTable.get(v.getUid()).vmDestroy(v);
    }

    public static Object optimizeAllocation() {
        return null;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vms) {
    	/*
    	 * MIGRATIONS are costly! :\
    	 * */
    	return null;
    }
}
