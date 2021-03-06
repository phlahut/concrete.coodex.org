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

package org.coodex.concrete.s2.injectable;

import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.s2.adaptor.AccountCopier;
import org.coodex.concrete.s2.adaptor.NamedAccountCopier;
import org.coodex.concrete.s2.api.AccountInfo;
import org.coodex.concrete.s2.api.WhoAmI;
import org.coodex.util.LazySelectableServiceLoader;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class WhoAmIImpl implements WhoAmI {


    private static final AccountCopier DEFAULT_ACCOUNT_COPIER = new NamedAccountCopier();
    @SuppressWarnings("rawtypes")
    private static LazySelectableServiceLoader<Account, AccountCopier> ACCOUNT_COPIER_LOADER =
            new LazySelectableServiceLoader<Account, AccountCopier>(DEFAULT_ACCOUNT_COPIER) {
            };
    @Inject
    private Token token;
//            new Singleton<>(() ->
//                    new SelectableServiceLoader<Account, AccountCopier>(DEFAULT_ACCOUNT_COPIER) {
//                    });

    @SuppressWarnings("rawtypes")
    @Override
    public AccountInfo get() {
        Account account = token.currentAccount();
        return IF.isNull(
                ACCOUNT_COPIER_LOADER.select(IF.isNull(account, ErrorCodes.NONE_ACCOUNT, token.getTokenId())),
                ErrorCodes.NONE_IMPLEMENTS_FOUND_FOR,
                AccountCopier.class.getName(),
                account
        ).copy(account);
    }
}
