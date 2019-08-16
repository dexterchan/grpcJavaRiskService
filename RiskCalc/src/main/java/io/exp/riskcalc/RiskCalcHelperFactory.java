package io.exp.riskcalc;

public class RiskCalcHelperFactory {
    public static RiskCalcHelperInterface getRiskCalcHelper(){
        return new FakeRiskCalcHelper();
    }
}
