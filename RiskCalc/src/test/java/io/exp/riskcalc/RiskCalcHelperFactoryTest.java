package io.exp.riskcalc;

import io.exp.grpc.risk.RiskRequest;
import io.exp.grpc.risk.RiskResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class RiskCalcHelperFactoryTest {

    @Test
    public void getRiskCalcHelper() {
        RiskCalcHelperInterface calc = RiskCalcHelperFactory.getRiskCalcHelper();

        RiskRequest r = RiskRequest.newBuilder().setTradeId("12345").setSystemDate("2017-03-01").build();

        RiskResponse res=calc.calculateRisk(r);
        assertEquals(res.getTradeId(),r.getTradeId());

        assertEquals(res.getRiskCount(),10);
    }
}