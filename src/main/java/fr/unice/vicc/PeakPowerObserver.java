package fr.unice.vicc;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class PeakPowerObserver extends SimEntity {

    public static final int OBSERVE = 728078;

    private List<PowerHost> hosts;

    private double peak;

    private float delay;

    public static final float DEFAULT_DELAY = 1;

    public PeakPowerObserver(List<PowerHost> hosts) {
        this(hosts, DEFAULT_DELAY);
    }

    public PeakPowerObserver(List<PowerHost> hosts, float delay) {
        super("PeakPowerObserver");
        this.hosts = hosts;
        this.delay = delay;
    }


    private double getPower() {
        double p = 0;
        for (PowerHost h : hosts) {
            p += h.getPower();
        }
        return p;
    }

    @Override
    public void processEvent(SimEvent ev) {
        switch(ev.getTag()) {
            case OBSERVE:
                double cur = getPower();
                if (cur > peak) {
                    peak = cur;
                }
                send(this.getId(), delay, OBSERVE, null);
        }
    }

    public double getPeak() {
        return peak;
    }

    @Override
    public void shutdownEntity() {
        Log.printLine(getName() + " is shutting down...");
    }

    @Override
    public void startEntity() {
        Log.printLine(getName() + " is starting...");
        //Send the signal to create the VMs
        send(this.getId(), delay, OBSERVE, null);
    }

}
