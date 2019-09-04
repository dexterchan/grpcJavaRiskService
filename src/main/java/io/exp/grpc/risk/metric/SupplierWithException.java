package io.exp.grpc.risk.metric;

import com.codahale.metrics.Timer;


@FunctionalInterface
public interface SupplierWithException<T> {
    T get() throws Exception;


    public static <T> T withTimer(Timer timer, String name, SupplierWithException<T> func) {

        Timer.Context ctx = timer.time();
        try {
            T result = func.get();
            return result;

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Wrapped exception - " + name + ": " + e.getMessage(), e);

        } finally {
            ctx.stop();
        }
    }
}
