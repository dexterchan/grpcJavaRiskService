package io.exp.riskcalc;


import io.exp.grpc.risk.ValueRequest;
import io.exp.grpc.risk.ValueResponse;

public interface RiskCalcHelperInterface {
     ValueResponse calculate(ValueRequest req);
}
