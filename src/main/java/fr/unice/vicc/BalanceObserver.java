package fr.unice.vicc;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerHost;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A component connected to the simulator.

 * Regularly, the component is called to check the instantaneous load balancing of the datacenter.
 * @author Mourjo Sen & Rares Damaschin
 */
public class BalanceObserver extends SimEntity
{
    /** The custom event id, must be unique. */
    public static final int OBSERVE = 728079;

    private List<PowerHost> hosts;

    private double stddev;

    private float delay;

    public static final float DEFAULT_DELAY = 1;
    
    private PrintWriter pw;

    public BalanceObserver(List<PowerHost> hosts) {
        this(hosts, DEFAULT_DELAY);
    }

    public BalanceObserver(List<PowerHost> hosts, float delay) {
        super("BalanceObserver");
        this.hosts = hosts;
        this.delay = delay;
    }


    /**
     * Get the datacenter instantaneous power.
     * @return a number in Watts
     */
    private void getStdDevMips() {
        double mean = 0;
        stddev = 0;

        for (PowerHost h : hosts) {
            mean += (h.getTotalMips() - h.getAvailableMips()) / h.getTotalMips();
        }
        mean = mean/(double)hosts.size();
        
        for (PowerHost h : hosts) {
            stddev += (((h.getTotalMips() - h.getAvailableMips()) / h.getTotalMips() - mean) 
            			* ((h.getTotalMips() - h.getAvailableMips()) / h.getTotalMips() - mean)); 
        }
        stddev /= (double)hosts.size();
        
        stddev =  Math.sqrt(stddev);
    }

    /*
    * This is the central method to implement.
    * CloudSim is event-based.
    * This method is called when there is an event to deal in that object.
    * In practice: create a custom event (here it is called OBSERVE) with a unique int value and deal with it.
     */
    @Override
    public void processEvent(SimEvent ev) {
        //I received an event
        switch(ev.getTag()) {
            case OBSERVE: 
            	getStdDevMips();
            	getBalanceMetric();
                //Observation loop, re-observe in `delay` seconds
                send(this.getId(), delay, OBSERVE, null);
        }
    }

    /**
     * Get the peak power consumption.
     * @return a number of Watts
     */
    public double getBalanceMetric() {
    	pw.println(stddev);
    	if(Math.random() < 0.1)
	    pw.flush();
        return stddev;
    }

    @Override
    public void shutdownEntity() {
        Log.printLine(getName() + " is shutting down...");
        pw.close();
    }

    @Override
    public void startEntity() {
    	try
    	{
    		//logging is done in a separate file in the classpath
	    	pw = new PrintWriter(new BufferedWriter (new FileWriter("balanceLog.txt", false)));
	        send(this.getId(), delay, OBSERVE, null);
    	}
    	catch(IOException e)
    	{}
    }
}
