package io.exp.riskcalc;

import io.exp.grpc.risk.RiskRequest;
import io.exp.grpc.risk.RiskResponse;

public interface RiskCalcHelperInterface {
     RiskResponse calculateRisk(RiskRequest req);
}
