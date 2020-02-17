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

package org.coodex.billing.timebased;

import org.coodex.billing.Adjustment;
import org.coodex.billing.Bill;
import org.coodex.billing.Calculator;
import org.coodex.billing.Revision;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.coodex.util.Common.*;

public abstract class AbstractTimeBasedCalculator<C extends TimeBasedChargeable> implements Calculator<C> {

    private final static Logger log = LoggerFactory.getLogger(AbstractTimeBasedCalculator.class);

    private AcceptableServiceLoader<String, BillingModel<C>> billingModels = new AcceptableServiceLoader<String, BillingModel<C>>() {
    };

    private AcceptableServiceLoader<C, BillingRuleRepository<C>> ruleRepos = new AcceptableServiceLoader<C, BillingRuleRepository<C>>(
            new BillingRuleRepository<C>() {
                @Override
                public Collection<BillingRule> getRulesBy(C chargeable) {
                    return Collections.EMPTY_LIST;
                }

                @Override
                public boolean accept(C param) {
                    return true;
                }
            }
    ) {
    };

    @SuppressWarnings("WeakerAccess")
    protected abstract TimeUnit getTimeUnit();

    @Override
    public Bill<C> calc(C chargeable) {
        BillingRuleRepository<C> ruleRepository = ruleRepos.select(chargeable);
        if (ruleRepository == null)
            return calcWithARule(chargeable);

        BillingRule[] rules = ruleRepository.getRulesBy(chargeable).toArray(new BillingRule[0]);
        if (rules.length > 1) { // 多条规则
            Arrays.sort(rules); // 按照生效期排序
            List<C> consumptions = new ArrayList<C>();
            boolean onlyOnceAdded = false;
            Calendar end = chargeable.getPeriod().getEnd();
            for (int x = rules.length - 1; x >= 0; x--) {// 由后向前分段
                BillingRule rule = rules[x];
                if (chargeable.getPeriod().getEnd().after(rule.getStart())) { // 消费结束时间大于规则有效期则说明生效
                    Calendar start = max(chargeable.getPeriod().getStart(), rule.getStart());// 以消费开始时间和规则生效期之大者作为分段的开始时间
                    if (chargeable.getPeriod().getStart().after(end))
                        break; // 消费开始时间大于当前段的目标结束时间时，说明本条及之前的都不适用

                    C consumption = copyChangeable(chargeable, !onlyOnceAdded);
                    consumption.setModel(rule.getModel());
                    consumption.setModelParam(rule.getModelParam());
                    consumption.setPeriod(Period.BUILDER.create((Calendar) start.clone(), (Calendar) end.clone()));
                    consumptions.add(consumption);
                    end = rule.getStart(); //将前一条的目标结束时间设置为本条规则生效时间
                    onlyOnceAdded = true;
                }
            }

            switch (consumptions.size()) {
                case 0:
                    break;
                case 1:
                    chargeable = consumptions.get(0);
                    break;
                default:
                    Bill<C> bill = new Bill<C>(chargeable);
                    for (int x = consumptions.size() - 1; x >= 0; x--) {
                        Bill<C> consumptionBill = calcWithARule(consumptions.get(x));
                        bill.addAllDetails(consumptionBill.getDetails());
                    }
                    return bill;
            }

        } else if (rules.length == 1) { // 只有一条规则
            chargeable.setModel(rules[0].getModel());
            chargeable.setModelParam(rules[0].getModelParam());
        }
        return calcWithARule(chargeable);
    }


    /**
     * ！！！ 自行实现，默认实现不靠谱
     *
     * @param chargeable   chargeable
     * @param withOnlyOnce 是否增加{@link OnlyOnce}的抵扣
     * @return 根据参数复制一个新的消费对象，用于多规则场景
     */
    protected C copyChangeable(C chargeable, boolean withOnlyOnce) {
        try {
            return deepCopy(chargeable);
        } catch (Throwable e) {
            throw runtimeException(e);
        }
    }

    private Bill<C> calcWithARule(C chargeable) {
        // 提取计费模型
        BillingModel<C> billingModel = billingModels.select(chargeable.getModel());
        if (billingModel == null) {
            throw new RuntimeException("NONE BillingModel Found for " + chargeable.getModel());
        }

        // 记账
        BillCalc<C> billCalc = new BillCalc<C>(chargeable, billingModel);
        Bill<C> bill = billCalc.getBill(getTimeUnit());

        // 账单总额调整
        return adjustBill(bill, billCalc.adjustments);
    }

    /**
     * TODO 使用Provider方式聚合
     *
     * @param bill        需要调整的账单
     * @param adjustments 调整项目
     * @return 调整后账单
     */
    @SuppressWarnings("WeakerAccess")
    protected Bill<C> adjustBill(Bill<C> bill, List<Adjustment<C>> adjustments) {
        if (adjustments == null || adjustments.size() == 0)
            return bill;

        for (Adjustment adjustment : adjustments) {
            //noinspection unchecked
            long amount = adjustment.adjust(bill);
            if (amount != 0) {
                bill.addDetail(new Bill.AdjustDetail(amount, adjustment.getName(), adjustment));
            }
        }
        return bill;
    }


    public static class TimeBasedDetailImpl implements TimeBasedDetail {
        private final Period period;
        private final long amount;
        private final Revision revision;
        private final String item;

        public TimeBasedDetailImpl(Period period, long amount, String item) {
            this(period, amount, null, item);
        }

        @SuppressWarnings("WeakerAccess")
        public TimeBasedDetailImpl(Period period, long amount, Revision revision, String item) {
            this.period = period;
            this.amount = amount;
            this.revision = revision;
            this.item = item;
        }

        @Override
        public Period getPeriod() {
            return period;
        }

        @Override
        public long getAmount() {
            return amount;
        }

        @Override
        public String item() {
            return item;
        }

        @Override
        public Revision usedRevision() {
            return revision;
        }

    }

    private static class BillCalc<C extends TimeBasedChargeable> {
        private final C chargeable;
        private final BillingModel<C> billingModel;

        private List<WholeTimeRevision> wholeTimeRevisions = new ArrayList<WholeTimeRevision>();
        private List<FragmentRevision> fragmentRevisions = new ArrayList<FragmentRevision>();
        private List<Adjustment<C>> adjustments = new ArrayList<Adjustment<C>>();

        private BillCalc(C chargeable, BillingModel<C> billingModel) {
            this.chargeable = chargeable;
            this.billingModel = billingModel;
            initRevisions(chargeable);
        }

        Bill<C> getBill(TimeUnit unit) {
            TimeBasedBill<C> bill = new TimeBasedBill<C>(chargeable);
            if (chargeable.getPeriod().duration(unit) == 0) return bill;

            List<Period> chargePeriods = new ArrayList<Period>();
            chargePeriods.add(chargeable.getPeriod());

            chargePeriods = beforeSlice(unit, bill, chargePeriods);
            if (Period.durationOf(chargePeriods, unit) == 0) {
                return bill;
            }

            chargePeriods = Section.merge(chargePeriods, Period.BUILDER);


            BillingModel.Instance<C> instance = billingModel.create(chargeable);
            Period chargePeriod = Period.BUILDER.create(
                    chargePeriods.get(0).getStart(),
                    chargePeriods.get(chargePeriods.size() - 1).getEnd()
            );
            boolean slice = true; // 是否切分，如果whole time算法已处理，则不进行切分
            if (instance.getWholeTimeAlgorithm() != null) {
                //noinspection unchecked
                List<Bill.Detail> details = instance.getWholeTimeAlgorithm().calc(chargePeriods, chargeable);
                if (details != null && details.size() > 0) {
                    bill.addAllDetails(details);
                    slice = false;
                }
            }

            if (slice) {
                List<BillingModel.Fragment<C>> fragments = instance.slice(chargePeriod, chargeable);
                for (BillingModel.Fragment<C> fragment : fragments) {
                    if (fragment.getAlgorithm() != null) {
                        List<Period> chargeFragment = Section.intersect(
                                Collections.singletonList(fragment.getPeriod()),
                                chargePeriods,
                                Period.BUILDER
                        );

                        // 分段内调整
                        for (FragmentRevision fragmentRevision : fragmentRevisions) {
                            if (fragmentRevision.accept(chargeFragment)) {
                                chargeFragment = revised(bill, chargeFragment, fragmentRevision);
                                if (Period.durationOf(chargeFragment, unit) == 0)
                                    return bill;
                            }
                        }
                        List<Bill.Detail> details = fragment.getAlgorithm().calc(chargeFragment, chargeable);
                        if (details != null && details.size() > 0) {
                            bill.addAllDetails(details);
                        }
                    } else {
                        bill.addDetail(new TimeBasedDetailImpl(
                                // TODO i18n
                                fragment.getPeriod(), 0, null, "no algorithm found."
                        ));
                    }
                }
            }
            return bill;
        }

        private List<Period> beforeSlice(TimeUnit unit, TimeBasedBill<C> bill, List<Period> chargePeriods) {
            // 切分前调减
            for (WholeTimeRevision revision : wholeTimeRevisions) {
                chargePeriods = revised(bill, chargePeriods, revision);
                if (Period.durationOf(chargePeriods, unit) == 0) {
                    return chargePeriods;
                }
            }
            return chargePeriods;
        }

        private List<Period> revised(TimeBasedBill<C> bill, List<Period> chargePeriods, TimeBasedRevision revision) {
            List<Period> revised = revision.revised(chargePeriods);
            if (revised != null && revised.size() > 0) {
                for (Period period : revised) {
                    bill.addDetail(new TimeBasedDetailImpl(period, 0, revision, revision.getName()));
                }
                chargePeriods = Section.sub(chargePeriods, revised, Period.BUILDER);
            }
            return chargePeriods;
        }

        private void initRevisions(C chargeable) {
            // 调整规则区分
            if (chargeable.getRevisions() != null && chargeable.getRevisions().size() > 0) {
                for (Revision revision : chargeable.getRevisions()) {
                    if (revision == null) continue;

                    if (revision instanceof WholeTimeRevision) {
                        wholeTimeRevisions.add((WholeTimeRevision) revision);
                    } else if (revision instanceof FragmentRevision) {
                        fragmentRevisions.add((FragmentRevision) revision);
                    } else if (revision instanceof Adjustment) {
                        //noinspection unchecked
                        adjustments.add((Adjustment<C>) revision);
                    } else {
                        log.warn("unsupported Revision: {}, {}", revision.getClass().getName(), revision.getName());
                    }
                }
            }
        }
    }

}
