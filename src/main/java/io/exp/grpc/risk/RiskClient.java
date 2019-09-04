package io.exp.grpc.risk;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;
import com.google.protobuf.util.JsonFormat;
import io.exp.grpc.risk.metric.SupplierWithException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.exp.grpc.risk.metric.SupplierWithException.withTimer;

public class RiskClient {
    private static final Logger logger = LoggerFactory.getLogger(RiskClient.class);

    private final ManagedChannel channel;

    private final RiskServiceGrpc.RiskServiceBlockingStub blockingStub;
    private static int PORT=9001;

    private static Timer totalTimer = new Timer(new UniformReservoir());
    private static Timer grpcTimer = new Timer(new UniformReservoir());

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

    public ValueResponse calculateRisk(String systemdate, String tradeid,String tradeMessage){
        logger.debug("trying to ping server");
        ValueRequest.Builder req = ValueRequest.newBuilder();
        req.setTradeId(tradeid);
        req.setSystemDate(systemdate);
        req.setTradeMessage(tradeMessage);
        req.setOutputType(ValueRequest.OUTPUT.ALL);
        req.setRunType(ValueRequest.RUNTYPE.FO);

        ValueResponse res=null;

        try{
            res=withTimer(grpcTimer, "grpc call", () -> {
                return blockingStub.calculate(req.build());
            });

            logger.debug("Status:"+res.getStatus());
        }catch(StatusRuntimeException e) {
            logger.warn( "RPC failed: {0}", e.getStatus());
            throw  e;
        }
        return res;
    }

    public static void main(String[] args) throws Exception {

        final int numOfTime=10000;
        MetricRegistry registry = new MetricRegistry();
        registry.register("trigger calculate request", totalTimer);
        registry.register("grpc request",grpcTimer);

        Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(logger)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);

        if(args.length>0){
            PORT = Integer.parseInt(args[0]);
        }

        RiskClient client = new RiskClient("localhost", PORT);
        try {

            if(!client.healthCheck()){
                logger.info("Not able to connect quit application");
                return;
            }

            for(int i=0;i<numOfTime;i++) {

                ValueResponse res = SupplierWithException.withTimer(totalTimer, "creationToRetrieval", () -> {
                    return client.calculateRisk("2017-03-01", "12345", "<Trade></Trade>");
                });
                logger.debug(res.getTradeId());
                for (ValueResponse.AssetSensivity asset : res.getAssetSensitivityLstList()) {
                    logger.debug(asset.getAssetId() + ":" + asset.getCcy() + ":" + asset.getNpv());
                    for (ValueResponse.Sensitivity sens : asset.getSenseLstList()) {
                        logger.debug(sens.getCcy() + ":" + sens.getRiskLabel());
                        for (ValueResponse.Sensitivity.Tenor t : sens.getTenorsList()) {
                            logger.debug(t.getLabel() + "," + t.getValue());
                        }
                    }
                }
            }

            //String jsonString = "";
//            JsonFormat.parser().ignoringUnknownFields().merge(jsonString,res.toBuilder());


            //String JsonStr=JsonFormat.printer().print(res);
            //logger.info(JsonStr);

        } finally {
            client.shutdown();
        }
    }

}
