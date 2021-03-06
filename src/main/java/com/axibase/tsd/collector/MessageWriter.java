/*
 * Copyright 2016 Axibase Corporation or its affiliates. All Rights Reserved.
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

package com.axibase.tsd.collector;


import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

public interface MessageWriter<E, K, L> {
    void writeStatMessages(WritableByteChannel writer, Map<K, EventCounter<L>> diff, long deltaTime) throws IOException;

    void writeSingles(WritableByteChannel writer, CountedQueue<EventWrapper<E>> singles) throws IOException;

    void start(WritableByteChannel writer, int level, int intervalSeconds, Map<String,String> stringSettings);

    void checkPropertiesSent(WritableByteChannel writer);

    void stop();

    boolean sendErrorInstance(WritableByteChannel writer, E event);

    EventWrapper<E> createWrapper(E event, int lines);
}
