package org.apache.sling.testing.clients.executor.resiliency;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.sling.testing.clients.executor.builder.ConditionFactoryBuilder;
import org.apache.sling.testing.clients.executor.config.RetryConfig;
import org.awaitility.core.ConditionFactory;
import org.jetbrains.annotations.NotNull;

import static org.hamcrest.Matchers.is;

public class ResiliencyHelper {

    private final ConditionFactory defaultRetry;

    public ResiliencyHelper(@NotNull final ConditionFactory conditionFactory) {
        defaultRetry = conditionFactory;
    }

    public ResiliencyHelper(@NotNull final RetryConfig retryConfig) {
        defaultRetry = ConditionFactoryBuilder.getInstance(retryConfig).build();
    }

    /**
     * Retry the request until it exists (i.e. returns a non-null response).
     *
     * @param request request
     * @return response
     */
    public final <T> T retryUntilExists(@NotNull final Callable<T> request) {
        return retryUntilCondition(request, Objects::nonNull);
    }

    public final <T> T retryUntilCondition(@NotNull final Callable<T> request, @NotNull final Predicate<T> condition) {
        return retryUntilCondition(request, condition, defaultRetry);
    }

    public final <T> T retryUntilCondition(@NotNull final Callable<T> request, @NotNull final Predicate<T> condition,
        @NotNull final ConditionFactory retry) {
        return retry.until(request, condition);
    }

    public final boolean retryUntilTrue(@NotNull final Callable<Boolean> supplier) {
        return retryUntilTrue(supplier, defaultRetry);
    }

    public final boolean retryUntilTrue(@NotNull final Callable<Boolean> supplier,
        @NotNull final ConditionFactory retry) {
        return retry.until(supplier, is(true));
    }
}
