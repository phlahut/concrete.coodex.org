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

package test.org.coodex.intf;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.ServiceTiming;
import test.org.coodex.intf.pojo.BV;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Created by davidoff shen on 2016-09-02.
 */
@ConcreteService
public interface AspectTest {

    //    @ConcreteService
//    @NotService
    @ServiceTiming({"rule1"})
    void test1(
            @NotNull(message = "参数不能为 null")
                    String a,
            @Valid
            @NotNull(message = "BV cannot be NULL.")
                    BV bv);
}
