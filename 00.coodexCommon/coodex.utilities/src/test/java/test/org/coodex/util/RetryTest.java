/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.util;

import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class RetryTest {

    private final static Logger log = LoggerFactory.getLogger(RetryTest.class);


    public static void main(String[] args) {
        Retry.newBuilder()
//                .maxTimes(3) // 最大尝试次数
//                .initDelay(20, TimeUnit.SECONDS) // 第一次执行延迟时间，默认为0
//                .next(new Retry.TimeUnitNextDelay(TimeUnit.SECONDS) { // 延迟策略
//                    @Override
//                    protected long delay(int times) {
//                        return 5;
//                    }
//                })
//                .scheduler(ExecutorsHelper.newSingleThreadScheduledExecutor("test"))// 指定线程池
                .build()
//                .handle(new Retry.AllFailedHandle() { // 当任务尝试数超出最大阈值依然失败时的handle
//                    @Override
//                    public void allFailed(Calendar start, int times) {
//                        log.info("all failed");
//                    }
//                })
                .execute(new Retry.Task() { // 要多次尝试执行的任务
                    @Override
                    public boolean run(int times) throws Exception {
                        log.debug("times: {}", times);
                        if (times % 2 == 0) {
                            throw new RuntimeException("mock exception:" + times);
                        }
                        return times == 5;
                    }
                });
    }
}
