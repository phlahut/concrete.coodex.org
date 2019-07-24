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

package org.coodex.concrete.common;

import org.coodex.util.Common;

public abstract class ClassifiableAccountFactory implements AcceptableAccountFactory<ClassifiableAccountID> {

    private Integer[] types = null;

    protected abstract Integer[] getSupportTypes();

    private Integer[] getTypes() {
        if (types == null) {
            types = getSupportTypes();
        }
        return types;
    }

    @Override
    public final boolean accept(ClassifiableAccountID param) {
        return param != null && param.getCategory() != null && Common.inArray(param.getCategory(), getTypes());
    }

}
