package io.exp.grpc.risk;

import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RiskClient {
    private static final Logger logger = LoggerFactory.getLogger(RiskClient.class);

    private final ManagedChannel channel;

    private final RiskServiceGrpc.RiskServiceBlockingStub blockingStub;
    private static int PORT=9001;

    public RiskClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    /** Construct client for accessing HelloWorld server using the existing channel. */
    RiskClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = RiskServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }


    public boolean healthCheck(){
        HealthCheckRequest req = HealthCheckRequest.newBuilder().build();
        HealthCheckResponse res=null;
        try {
            res = blockingStub.check(req);
            logger.info("health check status:"+res.getStatus());
            if(res.getStatus() == HealthCheckResponse.ServingStatus.SERVING){
                return true;
            }else{
                return false;
            }
        }catch(StatusRuntimeException se){
            logger.info("unable to connect:"+se.getMessage());
            return false;
        } catch(Exception ex){
            logger.warn("failed to connect:"+ex.getMessage());
            return false;
        }
    }

    public RiskResponse calculateRisk(String systemdate, String tradeid,String tradeMessage){
        logger.debug("trying to ping server");
        RiskRequest.Builder req = RiskRequest.newBuilder();
        req.setTradeId(tradeid);
        req.setSystemDate(systemdate);
        req.setTradeMessage(tradeMessage);

        RiskResponse res=null;

        try{
             res =
                    blockingStub.calculateRisk(req.build());
            logger.info("Status:"+res.getStatus());
        }catch(StatusRuntimeException e) {
            logger.warn( "RPC failed: {0}", e.getStatus());
            throw  e;
        }
        return res;
    }

    public static void main(String[] args) throws Exception {

        if(args.length>0){
            PORT = Integer.parseInt(args[0]);
        }

        RiskClient client = new RiskClient("localhost", PORT);
        try {

            if(!client.healthCheck()){
                logger.info("Not able to connect quit application");
                return;
            }


            RiskResponse res = client.calculateRisk("2017-03-01","12345","<Trade></Trade>");

            //String jsonString = "";
//            JsonFormat.parser().ignoringUnknownFields().merge(jsonString,res.toBuilder());
            logger.info(res.getTradeId());
            for (int i=0;i<res.getRiskCount();i++){
                RiskResponse.Tenor t=res.getRisk(i);
                logger.info(t.getLabel()+","+t.getValue());
            }
            String JsonStr=JsonFormat.printer().print(res);
            logger.info(JsonStr);

        } finally {
            client.shutdown();
        }
    }
}
