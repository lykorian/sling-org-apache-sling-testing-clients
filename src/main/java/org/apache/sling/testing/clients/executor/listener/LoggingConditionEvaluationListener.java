package org.apache.sling.testing.clients.executor.listener;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.awaitility.core.ConditionEvaluationListener;
import org.awaitility.core.EvaluatedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging listener for condition evaluation.
 */
public final class LoggingConditionEvaluationListener implements ConditionEvaluationListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingConditionEvaluationListener.class);

    private final AtomicInteger counter = new AtomicInteger(0);

    private final Function<Object, String> valueToString;

    /**
     * Create a new condition logger.
     *
     * @param valueToString function to use to convert the condition value to a String (instead of the default
     *     <code>Object.toString</code> method) for more informative logging
     */
    public LoggingConditionEvaluationListener(final Function<Object, String> valueToString) {
        this.valueToString = valueToString;
    }

    @Override
    public void conditionEvaluated(final EvaluatedCondition condition) {
        final int count = counter.incrementAndGet();

        if (condition.isSatisfied()) {
            LOG.info("{} satisfied after {} attempt(s) in {}ms, value: {}", getConditionLog(condition), count,
                condition.getElapsedTimeInMS(), getStringValue(condition.getValue(), valueToString));
        } else {
            LOG.info("{} not satisfied after {} attempt(s), poll interval: {}ms, elapsed time: {}ms, " +
                    "remaining time: {}ms, current value: {}", getConditionLog(condition), count,
                condition.getPollInterval().toMillis(), condition.getElapsedTimeInMS(),
                condition.getRemainingTimeInMS(), getStringValue(condition.getValue(), valueToString));
        }
    }

    private String getConditionLog(final EvaluatedCondition condition) {
        return condition.hasAlias() ? "condition [" + condition.getAlias() + "]" : "condition";
    }

    private String getStringValue(final Object conditionValue, final Function<Object, String> valueToString) {
        return Optional.ofNullable(valueToString)
            .map(function -> function.apply(conditionValue))
            .orElse(conditionValue.toString());
    }
}
