/*
 * Copyright 2015 Axibase Corporation or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * https://www.axibase.com/atsd/axibase-apache-2.0.pdf
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.axibase.tsd.collector.log4j;

import com.axibase.tsd.collector.SendMessageTrigger;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Nikolay Malevanny.
 */
public class Log4jEventTrigger extends SendMessageTrigger<LoggingEvent>{
    public static final Level DEFAULT_LEVEL = Level.WARN;
    private Level level = DEFAULT_LEVEL;

    private boolean definedSendMultiplier = false;

    public Log4jEventTrigger() {
        super();
    }

    public Log4jEventTrigger(int every) {
        super();
        setEvery(every);
    }

    @Override
    public boolean onEvent(LoggingEvent event) {
        return event != null && event.getLevel().isGreaterOrEqual(level) && super.onEvent(event);
    }

    @Override
    public String resolveKey(LoggingEvent event) {
        return event.getLoggerName();
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void setSendMultiplier(double sendMultiplier) {
        super.setSendMultiplier(sendMultiplier);
        definedSendMultiplier = true;
    }

    @Override
    public void init() {
        if (!definedSendMultiplier) {
            if (level.isGreaterOrEqual(Level.ERROR)) {
                setSendMultiplier(ERROR_SKIP_MULTIPLIER);
            } else if (level.isGreaterOrEqual(Level.WARN)) {
                setSendMultiplier(WARN_SKIP_MULTIPLIER);
            } else {
                setSendMultiplier(INFO_SKIP_MULTIPLIER);
            }
        }
    }
}