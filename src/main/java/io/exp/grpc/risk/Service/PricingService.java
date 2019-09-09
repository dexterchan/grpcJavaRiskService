package io.exp.grpc.risk.Service;

import io.exp.grpc.risk.ValueRequest;
import io.exp.grpc.risk.ValueResponse;

@FunctionalInterface
public interface PricingService {
    public ValueResponse calculateRisk(ValueRequest request);
}
