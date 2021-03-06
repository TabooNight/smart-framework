package org.smart4j.framework.helper;

import org.smart4j.framework.util.ReflectionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bean 助手类
 */
public final class BeanHelper {

    /**
     * 定义Bean映射（用于存放Bean类与Bean实例的映射关系）
     */
    private static final Map<Class<?>, Object> BEAN_MAP = new HashMap<Class<?>, Object>();

    static {

        Set<Class<?>> beanClassSet = ClassHelper.getBeanClassSet();
        for (Class<?> beanClass: beanClassSet) {
            Object obj = ReflectionUtil.newInstance(beanClass);
            BEAN_MAP.put(beanClass, obj);
        }

    }

    /**
     * 获取Bean映射
     *
     * @return
     */
    public static Map<Class<?>, Object> getBeanMap() {

        return BEAN_MAP;

    }

    /**
     * 获取Bean实例
     *
     * @param cls
     *              Bean类
     * @param <T>
     *              泛型
     * @return
     */
    public static <T> T getBean(Class<T> cls) {

        if (!BEAN_MAP.containsKey(cls)) {
            throw new RuntimeException("can not get bean by class:" + cls);
        }
        return (T) BEAN_MAP.get(cls);

    }

    /**
     * 设置Bean实例
     *
     * @param cls
     *              类
     * @param obj
     *              对象
     */
    public static void setBean(Class<?> cls, Object obj) {

        BEAN_MAP.put(cls, obj);

    }

}
