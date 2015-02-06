package fr.unice.vicc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

/**
 * @author Mourjo Sen & Rares Damaschin
 */
public class AntiAffinityVmAllocationPolicy extends VmAllocationPolicy {

    private Map<String, Host> vmTable;
    private Map<Integer, List<Host>> affinityMap;

    public AntiAffinityVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        vmTable = new HashMap<>();
        affinityMap = new HashMap<Integer, List<Host>>();
    }

    public Host getHost(Vm vm) {
        return this.vmTable.get(vm.getUid());
    }

    public Host getHost(int vmId, int userId) {
        return this.vmTable.get(Vm.getUid(userId, vmId));
    }

    public boolean allocateHostForVm(Vm vm, Host host) 
    {
    	int vmClass = vm.getId()/100;
    	if(affinityMap.containsKey(vmClass))
    	{
    		
    		if (affinityMap.get(vmClass).contains(host) && host.vmCreate(vm)) 
    		{
				affinityMap.get(vm.getId()/100).remove(host);
				vmTable.put(vm.getUid(), host);
				return true;
			}
    	}
    	else
    	{
    		if (host.vmCreate(vm)) 
    		{
    			List<Host> eligibleHosts = new LinkedList<Host>();
            	eligibleHosts.addAll(getHostList());
            	eligibleHosts.remove(host);
            	affinityMap.put(vmClass, eligibleHosts);
                vmTable.put(vm.getUid(), host);
                return true;
    		}
    	}
    	
        return false;
    }

    public boolean allocateHostForVm(Vm vm) {
    	
    	int vmID = vm.getId();
    	if(affinityMap.containsKey(vmID/100))
    	{
    		//check all eligible hosts for the class
    		for (Host h : affinityMap.get(vmID/100)) 
    		{
    			if (h.vmCreate(vm))
    			{
    				affinityMap.get(vmID/100).remove(h);
    				vmTable.put(vm.getUid(), h);
    				return true;
    			}
    		}
    	}
    	else
    	{
    		for (Host h : getHostList()) 
    		{
                if (h.vmCreate(vm)) 
                {
                	List<Host> eligibleHosts = new LinkedList<Host>();
                	eligibleHosts.addAll(getHostList());
                	eligibleHosts.remove(h);
                	affinityMap.put(vmID/100, eligibleHosts);
                    vmTable.put(vm.getUid(), h);
                    return true;
                }
            }
    	}
    	return false;
    }

    public void deallocateHostForVm(Vm vm, Host host) 
    {
    	affinityMap.get(vm.getId()/100).add(host);
        vmTable.remove(vm.getUid());
        host.vmDestroy(vm);
    }

    @Override
    public void deallocateHostForVm(Vm v) 
    {
        //get the host and remove the vm
    	affinityMap.get(v.getId()/100).add(v.getHost());
        vmTable.get(v.getUid()).vmDestroy(v);
    }

    public static Object optimizeAllocation() {
        return null;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> arg0) {
        //Static scheduling, no migration, return null;
        return null;
    }
}
