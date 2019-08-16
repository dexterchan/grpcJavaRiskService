package io.exp.riskcalc;

import io.exp.grpc.risk.RiskRequest;
import io.exp.grpc.risk.RiskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class FakeRiskCalcHelper implements RiskCalcHelperInterface {

    private static final Logger logger = LoggerFactory.getLogger(FakeRiskCalcHelper.class);

    static final int numberOfTenor=10;

    public  RiskResponse calculateRisk(RiskRequest req){
        String tradeid = req.getTradeId();
        String date= req.getSystemDate();

        String tradeMsg = req.getTradeMessage();

        //Fake the trade message

        RiskResponse.Builder replyBuilder = RiskResponse.newBuilder().setStatus(RiskResponse.Status.SUCCESS);
        replyBuilder.setTime(new Date().toString());
        replyBuilder.setTradeId(tradeid);
        replyBuilder.setCcy("USD");
        replyBuilder.setNpv(1000);


        for (int i=0;i<numberOfTenor;i++) {
            RiskResponse.Tenor.Builder tenor = RiskResponse.Tenor.newBuilder();
            tenor.setLabel("tenor"+i);
            tenor.setValue((i+1)*1000);
            replyBuilder.addRisk(tenor);
        }

        return replyBuilder.build();
    }
}
