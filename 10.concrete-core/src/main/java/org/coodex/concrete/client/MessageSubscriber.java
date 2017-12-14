/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.client;

import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MessageSubscriber {

    private final static Logger log = LoggerFactory.getLogger(MessageSubscriber.class);


    private static final Map<String, MessageListener<?>> listeners = new HashMap<String, MessageListener<?>>();

    public static <T> void subscribe(String subject, MessageListener<T> listener) {
        synchronized (listeners) {
            listeners.put(subject, listener);
        }
    }

    public static void cancel(String subject) {
        synchronized (listeners) {
            listeners.remove(subject);
        }
    }

    @SuppressWarnings("unchecked")
    public static void next(String subject, String jsonMessage) {
        synchronized (listeners) {
            MessageListener listener = listeners.get(subject);
            if (listener == null) {
                log.warn("No message listener found for: {}", subject);
                return;
            }

            try {
                listener.onMessage(
                        JSONSerializerFactory.getInstance().parse(jsonMessage, TypeHelper.toTypeReference(
                                MessageListener.class.getTypeParameters()[0],
                                listener.getClass()
                        ))
                );
            } catch (Throwable throwable) {
                log.error(throwable.getLocalizedMessage(), throwable);
            }
        }
    }
}
