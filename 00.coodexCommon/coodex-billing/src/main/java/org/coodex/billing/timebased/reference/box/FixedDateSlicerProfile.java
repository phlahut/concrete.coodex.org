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

package org.coodex.billing.timebased.reference.box;

import org.coodex.billing.timebased.reference.SlicerProfile;

public class FixedDateSlicerProfile implements SlicerProfile {
    private final String startTime;

    public FixedDateSlicerProfile() {
        this(null);
    }

    public FixedDateSlicerProfile(String startTime) {
        this.startTime = startTime == null ? "00:00" : startTime;
    }

    public String getStartTime() {
        return startTime;
    }
}