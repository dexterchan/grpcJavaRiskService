package io.exp.riskcalc;


import com.google.protobuf.Timestamp;
import io.exp.grpc.risk.ValueRequest;
import io.exp.grpc.risk.ValueResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class RiskCalcHelperFactoryTest {

    @Test
    public void getRiskCalcHelper() {
        RiskCalcHelperInterface calc = RiskCalcHelperFactory.getRiskCalcHelper();

        long millis = System.currentTimeMillis();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000).setNanos((int) ((millis % 1000) * 1000000)).build();

        ValueRequest r = ValueRequest.newBuilder().setTradeId("12345").setSystemDate(timestamp).build();

        ValueResponse res=calc.calculate(r);
        assertEquals(res.getTradeId(),r.getTradeId());

    }
}