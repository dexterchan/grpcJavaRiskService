package io.exp.grpc.risk;

import io.exp.riskcalc.RiskCalcHelperFactory;
import io.exp.riskcalc.RiskCalcHelperInterface;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

public class RiskServer {
    private static final Logger logger = LoggerFactory.getLogger(RiskServer.class);
    private Server server;
    private  static int PORT=9001;

    public static void main(String[] args) throws IOException, InterruptedException {

        final RiskServer server = new RiskServer();
        if(args.length>0){
            PORT = Integer.parseInt(args[0]);
        }
        logger.info("Port use:"+PORT);

        server.start();
        server.blockUntilShutdown();
    }


    private void start() throws IOException {
        /* The port on which the server should run */

        server = ServerBuilder.forPort(PORT)
                .addService(new RiskImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.error("*** shutting down gRPC server since JVM is shutting down");
                RiskServer.this.stop();
                logger.error("*** server shut down");
            }
        });
    }
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class RiskImpl extends RiskServiceGrpc.RiskServiceImplBase {
        @Override
        public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
            HealthCheckResponse res = HealthCheckResponse.newBuilder().setStatus(HealthCheckResponse.ServingStatus.SERVING).build();
            responseObserver.onNext(res);
            responseObserver.onCompleted();
        }

        @Override
        public void calculateRisk(RiskRequest request, StreamObserver<RiskResponse> responseObserver) {
            RiskCalcHelperInterface calc = RiskCalcHelperFactory.getRiskCalcHelper();

            RiskResponse res=calc.calculateRisk(request);
            responseObserver.onNext(res);
            responseObserver.onCompleted();

        }
    }
}
