package io.exp.riskcalc;


import io.exp.grpc.risk.ValueRequest;
import io.exp.grpc.risk.ValueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class FakeRiskCalcHelper implements RiskCalcHelperInterface {

    private static final Logger logger = LoggerFactory.getLogger(FakeRiskCalcHelper.class);

    static final int numberOfAsset=2;
    static final int numberOfRiskSense=2;
    static final int numberOfTenor=10;

    @Override
    public ValueResponse calculate(ValueRequest req) {
        String tradeid = req.getTradeId();
        String date= req.getSystemDate();

        String tradeMsg = req.getTradeMessage();

        ValueResponse.Builder replyBuilder = ValueResponse.newBuilder().setStatus(ValueResponse.Status.SUCCESS);
        replyBuilder.setTime(new Date().toString());
        replyBuilder.setTradeId(tradeid);

        //Fill in asset

        for (int asset=0;asset<numberOfAsset;asset++){
            ValueResponse.AssetSensivity.Builder assetBuilder = ValueResponse.AssetSensivity.newBuilder();
            assetBuilder.setAssetId("asset"+asset);
            assetBuilder.setCcy("USD");
            assetBuilder.setNpv( Math.pow((-1) , (asset)) * 1000);
            for (int i=0;i<numberOfRiskSense;i++) {
                ValueResponse.Sensitivity.Builder senesBuilder = ValueResponse.Sensitivity.newBuilder();
                senesBuilder.setCcy("USD");
                senesBuilder.setRiskLabel("Risk"+i);

                for (int j=0;j<numberOfTenor;j++) {
                    ValueResponse.Sensitivity.Tenor.Builder tensorBuilder = ValueResponse.Sensitivity.Tenor.newBuilder();
                    tensorBuilder.setLabel("tenor"+j);
                    tensorBuilder.setValue((j+1)*1000 *Math.pow((-1) , (asset)) );
                    senesBuilder.addTenors(tensorBuilder);
                }
                assetBuilder.addSenseLst(senesBuilder);
            }
            replyBuilder.addAssetSensitivityLst(assetBuilder);
        }

        return replyBuilder.build();
    }

}
