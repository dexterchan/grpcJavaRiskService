package io.exp.grpc.risk;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class RiskServerTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();


    RiskServiceGrpc.RiskServiceBlockingStub blockingStub;

    @Before
    public void init() throws IOException {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new RiskServer.RiskImpl()).build().start());

        blockingStub = RiskServiceGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));
    }


    @Test
    public void riskImpl_replyMessage() throws Exception {

        ValueRequest req = ValueRequest.newBuilder().setTradeId("12345").setSystemDate("2017-03-01").setTradeMessage("<Trade></Trade>").build();

        ValueResponse res =
                blockingStub.calculate(req);

        assertEquals(res.getTradeId(),req.getTradeId());

    }

    @Test
    public void checkHealth() throws Exception{
        HealthCheckRequest req = HealthCheckRequest.newBuilder().build();
        HealthCheckResponse res =blockingStub.check(req);

        assertEquals(res.getStatus(), HealthCheckResponse.ServingStatus.SERVING);
    }

}