package io.exp.grpc.risk;

import com.google.protobuf.Timestamp;
import io.exp.grpc.risk.Service.PricingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RiskClientTest {

    PricingService pricingservice;
    RiskClient client=new RiskClient();

    static final int numberOfAsset=2;
    static final int numberOfRiskSense=2;
    static final int numberOfTenor=10;


    @Before
    public void testCalculateRisk(){
        pricingservice = mock(PricingService.class);
        client.setPricingservice(pricingservice);
    }

    @Test
    public void calculateRisk() {
        ValueRequest.Builder req = ValueRequest.newBuilder();
        String mytradeid="12345";
        String systemdate="2017-03-01";
        String trademsg = "<Trade></Trade>";
        req.setTradeId(mytradeid);

        long millis = System.currentTimeMillis();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000).setNanos((int) ((millis % 1000) * 1000000)).build();
        req.setSystemDate(timestamp);
        req.setTradeMessage(trademsg);
        req.setOutputType(ValueRequest.OUTPUT.ALL);
        req.setRunType(ValueRequest.RUNTYPE.FO);

        when(pricingservice.calculateRisk(req.build())).thenAnswer(
                invocation->{
                    ValueRequest v = (ValueRequest)(invocation.getArgument(0));
                    String tradeid = v.getTradeId();
                    ValueResponse.Builder replyBuilder = ValueResponse.newBuilder().setStatus(ValueResponse.Status.SUCCESS);
                    replyBuilder.setTime(timestamp);
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
        );

        ValueResponse res=client.calculateRisk(systemdate,mytradeid,trademsg);

        Assert.assertNotNull(res);
        Assert.assertSame(res.getTradeId(),mytradeid);
        Assert.assertEquals(res.getAssetSensitivityLstCount(), numberOfAsset);
        res.getAssetSensitivityLstList().forEach(
                asset->{
                    Assert.assertEquals(asset.getSenseLstCount(),numberOfRiskSense);
                    asset.getSenseLstList().forEach(
                            sense->{
                                Assert.assertEquals(sense.getTenorsCount(), numberOfTenor);
                            }
                    );
                }
        );
    }
}