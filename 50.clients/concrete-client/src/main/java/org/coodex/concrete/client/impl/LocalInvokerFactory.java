/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.Invoker;
import org.coodex.concrete.client.InvokerFactory;
import org.coodex.concrete.client.RxInvoker;

public class LocalInvokerFactory implements InvokerFactory {

    public static final String LOCAL = "local";

    @Override
    public Invoker getSyncInvoker(Destination destination) {
        return new LocalInvoker(destination);
    }

    @Override
    public RxInvoker getRxInvoker(Destination destination) {
        return new SyncToRxInvoker(new LocalInvoker(destination));
    }

    @Override
    public boolean accept(Destination param) {
//        return param.getLocation().equalsIgnoreCase(LOCAL) && !param.isAsync();
        /*&& !param.isAsync()*/
        return param instanceof LocalDestination;
    }
}
