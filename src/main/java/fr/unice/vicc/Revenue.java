package fr.unice.vicc;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.power.PowerDatacenter;

/**
 * Compute the revenue of the provider after 1 day of exercice.
 * The revenue is computed based on the energy consumption and the associated contract, the Vm hourly costs and
 * the SLA violations.
 * @author Fabien Hermenier
 *
 */
public class Revenue {

    private int [] subscriptions = {42, 48, 54, 60, 66, 72, 78, 84, 90, 96, 102, 108, 120, 132, 144, 156, 168, 180, 192, 204, 216, 228, 240};


    private PeakPowerObserver peakObs;

    private PowerDatacenter dc;

    private double ANNUAL_KW_COST = 74.16;
    private double DAILY_KW_COST = ANNUAL_KW_COST / 100;

    private double KWH_PRICE = 4.871 / 100; /* €/kWh */

    private double PUBLIC_SERVICE_TAX = 1.650/100; /* €/kWh */

    private double DEPT_SERVICE_TAX = 0.300/100; /* €/kWh */

    private double CITY_SERVICE_TAX = 0.633/100; /* €/kWh */

    private double TVA = 0.20;

    private double pue = 2;

    public Revenue(PeakPowerObserver po, PowerDatacenter dc) {
        this.dc = dc;
        this.peakObs = po;
    }

    public double compute() {
        return clientIncomes() - energyCost() - penalties();
    }

    public double clientIncomes() {
        double i = 0;
        for (Vm v : dc.getVmList()) {
            double hourlyCost = getCost(v);
            i += hourlyCost * 24;
        }
        return i;
    }

    private double missingMips(VmStateHistoryEntry e, double d) {
        double want = e.getRequestedMips() * d;
        double got = e.getAllocatedMips() * d;
        if (got < want) {
            return want - got;
        }
        return 0;
    }

    private double getCost(Vm v) {
        for (int i = 0; i < Helper.VM_TYPES; i++) {
            if (v.getMips() == Helper.VM_MIPS[i]) {
                return Helper.PRIZES[i];
            }
        }
        throw new IllegalArgumentException("No type for Vm " + v.getId());
    }

    public double penalties() {
        double p = 0;
        for (Vm v : dc.getVmList()) {
            p += refund(v, availability(v));
        }
        return p;
    }

    public double refund(Vm v, double availability) {

        double hourlyCost = getCost(v);
        //10% per point missing
        double missing = 100 - availability;
        return hourlyCost * (0.10 * missing * missing) * 24;
    }

    public double availability(Vm v) {
            double totalMissing = 0;
            double prev = 0;
            for (VmStateHistoryEntry e : v.getStateHistory()) {
                double diff = e.getTime() - prev;
                prev = e.getTime();
                totalMissing += missingMips(e, diff);
            }
            double totalAllocated = v.getMips() * Constants.SIMULATION_LIMIT;
            double availabilityPct = (totalAllocated - totalMissing) / totalAllocated * 100;
        return availabilityPct;
    }

    private int powerToSubscribe(double p) {
        for (int subscription : subscriptions) {
            if (subscription > p / 1000) {
                return subscription;
            }
        }
        throw new IllegalArgumentException("The power " + p + " exceeds the 'tarif jaune' domain");
    }

    public double energyCost() {
        double subscription = powerToSubscribe(peakObs.getPeak() * pue);

        double energy = (pue * dc.getPower()) / (3600 * 1000); /*kWh*/
        double cost = DAILY_KW_COST * subscription; //Fix part of the price
        cost += energy *  KWH_PRICE;//energy

        //taxes
        cost += (PUBLIC_SERVICE_TAX * energy);
        cost += (DEPT_SERVICE_TAX * energy);
        cost += (CITY_SERVICE_TAX * energy);

        return cost + cost * TVA;
    }

    @Override
    public String toString() {
        return "Incomes:    " + String.format("%.2f", clientIncomes()) + "€\n" +
               "Penalties:  " + String.format("%.2f", penalties()) + "€\n" +
               "Energy:     " + String.format("%.2f", energyCost()) + "€\n" +
               "Revenue:    " + String.format("%.2f", compute()) + "€\n";
    }
}
