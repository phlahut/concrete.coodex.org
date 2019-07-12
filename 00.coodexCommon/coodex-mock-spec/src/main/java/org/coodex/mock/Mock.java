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

package org.coodex.mock;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来修饰定制的模拟器注解，例如：
 * <pre>
 * {@literal @}Mock
 * {@literal @}interface MyMocker{}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Mock {

    /**
     * 当需要注入的模拟器找不到时如何处理
     */
    enum NotFound {
        /**
         * 忽略，使用默认模拟器
         */
        IGNORE,
        /**
         * 警告，通过日志框架输出警告信息，使用默认模拟器
         */
        WARN,
        /**
         * 抛异常
         */
        ERROR
    }

    /**
     * <pre>
     * 用来修饰模拟器声明配置信息，声明模拟器的方式有两种，优先级如下：
     *
     *   如果配置信息的属性的值类型是`@Mock`修饰的，则以此属性名为key，以此属性值声明上下文模拟器，
     *   这种方式主要用于公用类，可以将共用类中明确类型的需要被注入的模拟器逐一定义出来，
     *   开发者根据实际情况指定模拟器即可
     *
     *   属性上有`@Mock`修饰的注解时，则以此属性名为`key`，以此属性上的注解声明上下文模拟器，
     *   适用于共用类中，属性类型不明确的，由开发者在外层根据具体情况指定将用到的模拟器
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface Declaration {
    }

    /**
     * 定义一个序列模拟器
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @interface Sequence {
        /**
         * @return 上下文中的名字
         */
        String name();

        /**
         * @return 模拟器的类型，需要有无参的构造方法
         */
        Class<? extends SequenceMocker> mocker();
    }

    /**
     * 定义一组序列模拟器
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @interface Sequences {
        /**
         * @return 所有可能被下文中用到的序列模拟器
         */
        Sequence[] value();
    }

    /**
     * <pre>
     * 用来修饰属性，注入上下文中的模拟器
     * 重名的模拟器优先级上：
     *     在集合、数组的属性模拟上，上下文中同名的序列模拟器优先级高于其他单值模拟器
     *     多个相同类型（单值并且与当前需要模拟的类型匹配的、序列）模拟器，就近原则，越靠近将要模拟的属性则越优先
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @interface Inject {

        /**
         * @return 注入哪个模拟器
         */
        String value();

        /**
         * @return 需要注入的模拟器不存在时如何处理，默认提示警告信息，使用此类型的默认模拟器
         */
        NotFound notFound() default NotFound.WARN;
    }

    /**
     * 用来定义多维（含一维）集合、数组的维度模拟信息，确定各维度的数组大小
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Dimension {

        /**
         * @return >0 表示固定值，-1表示允许为空，否则按照random(min, max)，默认0
         */
        int size() default 0;

        /**
         * @return size <=0 时，模拟此维度大小的下界
         */
        int min() default 1;

        /**
         * @return size <=0 时，模拟此维度大小的上界
         */
        int max() default 5;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Dimensions {

        /**
         * @return 当前属性上，多维度集合、数组的大小设置，按value的数组下标+1确定对应维度
         */
        Dimension[] value();

        /**
         * 相同维度的集合数组是否大小一致，默认一致。
         * <p>
         * 例如：
         * <pre>{@literal @}{@link Dimensions}(
         *      value = { {@literal @}{@link Dimension}(size=2),{@literal @}{@link Dimension}(min=3,max=10)) },
         *      same = true
         * )
         * String[][][] string3d;
         *
         * 模拟结果，String[0].length == String[1].length
         * <p>
         * same 为 false 时，有可能不等
         * </pre>
         *
         * @return 相同维度的集合数组是否大小一致，默认一致
         */
        boolean same() default true;
    }

    /**
     * 用于修饰类似于Map.Entry<K,V>类型的数据的键模拟器的注解
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE})
    @interface Key {
    }

    /**
     * 用于修饰类似于Map.Entry<K,V>类型的数据的值模拟器的注解
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE})
    @interface Value {
    }

    /**
     * 使用指定`yaml`或`json`文件模拟数据，优先级最高
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Designated {
        /**
         * @return 指定`yaml`或`json`文件
         */
        String resource();
    }

    /**
     * 用来指定pojo的关联模拟策略，也就是被修饰的属性可以根据所依赖的属性值进行运算，最大可能保障模拟数据的真实性
     */
    @interface Relation {

    }

    /**
     * 相同类型模拟的深度，比如
     * <pre>
     *     class A {
     *         public A a;
     *     }
     * </pre>
     */
    @interface Depth{
        int value() default 2;
    }


    /**
     * <pre>
     * 单值模拟时，是否模拟null
     * 对基础类型无效
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @interface Nullable {
        /**
         * @return null几率，默认5%
         */
        double probability() default 0.05d;
    }

    /**
     * <pre>
     * 布尔单值模拟器,支持类型:
     * boolean, Boolean: 布尔值 true, false
     * byte, int, short, long及其包装类: 默认true - 1; false - 0，可通过intTrue和intFalse更改
     * char及其包装类: 默认 true - T; false - F，可通过charTrue和charFalse更改
     * String: 默认true - "true"; false - "false"，可通过strTrue, strFalse更改
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Mock
    @interface Boolean {
        /**
         * @return 模拟值为真的百分比，默认50%
         */
        double probabilityOfTrue() default 0.5d;

        int intTrue() default 1;

        int intFalse() default 0;

        char charTrue() default 'T';

        char charFalse() default 'F';

        java.lang.String strTrue() default "true";

        java.lang.String strFalse() default "false";
    }


    /**
     * 数据单值模拟器，支持类型：byte, short, int, long, float, double及其包装类
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Mock
    @interface Number {
        /**
         * <pre>
         * 指定模拟范围
         * 范围包括两种：连续范围，单值范围
         * 连续范围规则如下
         * '['  - 表示一个连续范围开始，且包含此值
         * '('  - 表示一个连续范围开始，不包含此值
         * ']'  - 表示一个连续范围结束，且包含此值
         * ')'  - 表示一个连续范围结束，不包含此值
         * 连续范围的起止值使用 ',' 分隔
         *
         * 例如 (-100.0f, 200.5f]
         *
         * 单值范围直接用数值描述
         *
         * 多个单值范围或连续范围使用 ',' 分割
         *
         * 各个范围不需要有序，各自模拟的权重，单值为1，连续范围则根据此连续范围内整数的个数来确定，0个权重为1，否则权重连续范围内的整数个数
         *
         *
         * 例如：
         * 10,[-1,5],8,(20,30),35
         *
         * byte,short,int,long及其包装类，以0x开头则表示以16进制解析
         *
         * </pre>
         *
         * @return 模拟范围
         */
        java.lang.String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Mock
    @interface Char {
        java.lang.String range() default "";
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Mock
    @interface String {

        int length() default 10;

        java.lang.String[] range() default {};

        java.lang.String txtResource() default "";
    }

}
