package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author Mourjo Sen & Rares Damaschin
 */
public class DynamicEnergyVmAllocationPolicy extends VmAllocationPolicy {

    //To track the Host for each Vm. The string is the unique Vm identifier, composed by its id and its userId
    private Map<String, Host> vmTable;

    public DynamicEnergyVmAllocationPolicy(List<? extends Host> list) {
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
            }
        });
    	
    	
        //First fit algorithm, run on the first suitable node
        for (Host h : getHostList()) {
            if (h.vmCreate(vm)) {
                //track the host
                vmTable.put(vm.getUid(), h);
                return true;
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
    	List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
    	
    	
    	
    	Collections.sort(getHostList(), new Comparator<Host>() {
            @Override
            public int compare(Host h1, Host h2) {	
            	return (int)(h2.getAvailableMips() - h1.getAvailableMips());
            }
        });
    	
    	
    	for (Host h : getHostList())
    	{
    		for(Vm v : h.getVmList())
    		{
    			for(int i = getHostList().indexOf(h) + 1; i < getHostList().size(); i++)
    			{
    				if(getHostList().get(i).getAvailableMips() > v.getCurrentRequestedTotalMips())
    				{
    					Map<String, Object> m1 = new HashMap<String, Object>();
    					m1.put("vm", v);
    			        m1.put("host", getHostList().get(i));
    			    	map.add(m1);
    				}
    			}
    		}
    		Collections.sort(getHostList().subList(getHostList().indexOf(h)+1, getHostList().size()), new Comparator<Host>() {
                @Override
                public int compare(Host h1, Host h2) {	
                	return (int)(h2.getAvailableMips() - h1.getAvailableMips());
                }
            });
    	}
    	
    	return map;
        

    }
}
