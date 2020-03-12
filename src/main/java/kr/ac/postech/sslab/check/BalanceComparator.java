package kr.ac.postech.sslab.check;

import org.javatuples.Triplet;

import java.util.Comparator;

public class BalanceComparator implements Comparator<Triplet<String, String, Integer>> {
    @Override
    public int compare(Triplet<String, String, Integer> a, Triplet<String, String, Integer> b) {
        return -Integer.compare(a.getValue2(), b.getValue2());
    }
}
