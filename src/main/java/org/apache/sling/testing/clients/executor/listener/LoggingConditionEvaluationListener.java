/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.testing.clients.executor.listener;

import java.util.Map;
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

    private final Map<Boolean, Function<Object, String>> valueToStringFunctions;

    /**
     * Create a new condition logger.
     *
     * @param valueToStringFunctions map containing functions to use to convert the condition value to a String
     *     (instead of the default <code>Object.toString</code> method) for more informative logging; <code>true</code>
     *     map key should contain the "satisfied" function and <code>false</code> key should contain the "not satisfied"
     *     function
     */
    public LoggingConditionEvaluationListener(final Map<Boolean, Function<Object, String>> valueToStringFunctions) {
        this.valueToStringFunctions = valueToStringFunctions;
    }

    @Override
    public void conditionEvaluated(final EvaluatedCondition condition) {
        final int count = counter.incrementAndGet();

        final Function<Object, String> valueToString = valueToStringFunctions.get(condition.isSatisfied());

        if (condition.isSatisfied()) {
            LOG.info("condition satisfied after {} attempt(s) in {}ms, value: {}", count,
                condition.getElapsedTimeInMS(), getStringValue(condition.getValue(), valueToString));
        } else {
            LOG.info("condition not satisfied after {} attempt(s), poll interval: {}ms, elapsed time: {}ms, " +
                    "remaining time: {}ms, current value: {}", count, condition.getPollInterval().toMillis(),
                condition.getElapsedTimeInMS(),
                condition.getRemainingTimeInMS(), getStringValue(condition.getValue(), valueToString));
        }
    }

    private String getStringValue(final Object conditionValue, final Function<Object, String> valueToString) {
        return Optional.ofNullable(valueToString)
            .map(function -> function.apply(conditionValue))
            .orElse(conditionValue.toString());
    }
}
