package io.exp.riskcalc;


import io.exp.grpc.risk.ValueRequest;
import io.exp.grpc.risk.ValueResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class RiskCalcHelperFactoryTest {

    @Test
    public void getRiskCalcHelper() {
        RiskCalcHelperInterface calc = RiskCalcHelperFactory.getRiskCalcHelper();

        ValueRequest r = ValueRequest.newBuilder().setTradeId("12345").setSystemDate("2017-03-01").build();

        ValueResponse res=calc.calculate(r);
        assertEquals(res.getTradeId(),r.getTradeId());

    }
}