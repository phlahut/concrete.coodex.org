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

package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public class AcceptableServiceLoader<Param_Type, T extends AcceptableService<Param_Type>> implements ServiceLoader<T> {

    private final static Logger log = LoggerFactory.getLogger(AcceptableServiceLoader.class);

    private final ServiceLoaderFacade<T> serviceLoaderFacade;

    public AcceptableServiceLoader(ServiceLoaderFacade<T> serviceLoaderFacade) {
        this.serviceLoaderFacade = serviceLoaderFacade;
    }

    public T getServiceInstance(Param_Type param) {
        for (T instance : getAllInstances()) {
            if (instance.accept(param))
                return instance;
        }
        try {
            T instance = serviceLoaderFacade.getDefaultProvider();
            if (instance.accept(param))
                return instance;
        }catch (Throwable th){
        }
        log.warn("no service instance accept this: {}", param);

        return null;
    }

    @Override
    public Collection<T> getAllInstances() {
        return serviceLoaderFacade.getAllInstances();
    }

    @Override
    public T getInstance(Class<? extends T> providerClass) {
        return serviceLoaderFacade.getInstance(providerClass);
    }

    @Override
    public T getInstance(String className) {
        return serviceLoaderFacade.getInstance(className);
    }

    @Override
    public T getInstance() {
        return serviceLoaderFacade.getInstance();
    }
}
