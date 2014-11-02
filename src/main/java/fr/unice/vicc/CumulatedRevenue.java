package fr.unice.vicc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class CumulatedRevenue extends Revenue {

    private List<Revenue> revenues;

    public CumulatedRevenue() {
        super(null, null);
        this.revenues = new ArrayList<Revenue>();
    }

    public void add(Revenue r) {
        revenues.add(r);
    }

    @Override
    public double compute() {
        double d = 0;
        for (Revenue r : revenues) {
            d+= r.compute();
        }
        return d;
    }

    @Override
    public double clientIncomes() {
        double d = 0;
        for (Revenue r : revenues) {
            d+= r.clientIncomes();
        }
        return d;
    }

    @Override
    public double penalties() {
        double d = 0;
        for (Revenue r : revenues) {
            d+= r.penalties();
        }
        return d;
    }

    @Override
    public double energyCost() {
        double d = 0;
        for (Revenue r : revenues) {
            d+= r.energyCost();
        }
        return d;
    }
}
