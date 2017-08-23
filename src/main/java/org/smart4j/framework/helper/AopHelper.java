package org.smart4j.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Aspect;
import org.smart4j.framework.annotation.Service;
import org.smart4j.framework.proxy.AspectProxy;
import org.smart4j.framework.proxy.Proxy;
import org.smart4j.framework.proxy.ProxyManager;
import org.smart4j.framework.proxy.TransactionProxy;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 方法拦截助手类
 */
public class AopHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AopHelper.class);

    static {
        try {
            Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
            Map<Class<?>, List<Proxy>> targetMap = createTargetMap(proxyMap);
            for (Map.Entry<Class<?>, List<Proxy>> targetEntry: targetMap.entrySet()) {
                Class<?> targetClass = targetEntry.getKey();
                List<Proxy> proxyList = targetEntry.getValue();
                Object proxy = ProxyManager.createProxy(targetClass, proxyList);
                BeanHelper.setBean(targetClass, proxy);
            }
        } catch (Exception e) {
            LOGGER.error("aop failure", e);
        }
    }

    /**
     * 获取代理类及其目标类集合之间的映射关系，一个代理类可对应多个目标类
     *
     * @return
     *
     * @throws Exception
     */
    private static Map<Class<?>, Set<Class<?>>> createProxyMap() throws Exception {

        Map<Class<?>, Set<Class<?>>> proxyMap = new HashMap<Class<?>, Set<Class<?>>>();
        addAspectProxy(proxyMap);// 普通切面代理
        addTransactionProxy(proxyMap);// 事务代理
        return proxyMap;

    }

    /**
     * 普通切面代理
     *
     * @param proxyMap
     *                  代理类及其目标类集合之间的映射关系
     * @throws Exception
     */
    private static void addAspectProxy(Map<Class<?>, Set<Class<?>>> proxyMap) throws Exception {

        Set<Class<?>> proxyClassSet = ClassHelper.getClassSetBySuper(AspectProxy.class);
        for (Class<?> proxyClass: proxyClassSet) {
            if (proxyClass.isAnnotationPresent(Aspect.class)) {
                Aspect aspect = proxyClass.getAnnotation(Aspect.class);
                Set<Class<?>> targetClassSet = createTargetClassSet(aspect);
                proxyMap.put(proxyClass, targetClassSet);
            }
        }

    }

    /**
     * 事务代理
     *
     * @param proxyMap
     *                  代理类及其目标类集合之间的映射关系
     */
    private static void addTransactionProxy(Map<Class<?>, Set<Class<?>>> proxyMap) {

        Set<Class<?>> serviceClassSet = ClassHelper.getClassSetByAnnotation(Service.class);
        proxyMap.put(TransactionProxy.class, serviceClassSet);

    }

    /**
     * 获取Aspect注解中设置的注解类
     *
     * @param aspect
     *                  Aspect注解类对象
     * @return
     *
     * @throws Exception
     */
    private static Set<Class<?>> createTargetClassSet(Aspect aspect) throws Exception {

        Set<Class<?>> targetClassSet = new HashSet<Class<?>>();
        Class<? extends Annotation> annotation = aspect.value();
        if (annotation != null && !annotation.equals(Aspect.class)) {// 该注解类不是Aspect类
            // 调用ClassHelper#getClassSetByAnnotation方法获取相关类，并把这些类放入目标类集合中
            targetClassSet.addAll(ClassHelper.getClassSetByAnnotation(annotation));
        }
        return targetClassSet;

    }

    /**
     * 分析目标类与代理对象列表之间的映射关系
     *
     * @param proxyMap
     *                  代理类及其目标类集合之间的映射关系
     * @return
     *
     * @throws Exception
     */
    private static Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>, Set<Class<?>>> proxyMap)
            throws Exception {

        Map<Class<?>, List<Proxy>> targetMap = new HashMap<Class<?>, List<Proxy>>();
        for (Map.Entry<Class<?>, Set<Class<?>>> proxyEntry: proxyMap.entrySet()) {
            Class<?> proxyClass = proxyEntry.getKey();
            Set<Class<?>> targetClassSet = proxyEntry.getValue();
            for (Class<?> targetClass: targetClassSet) {
                Proxy proxy = (Proxy) proxyClass.newInstance();
                if (targetMap.containsKey(targetClass)) {
                    targetMap.get(targetClass).add(proxy);
                } else {
                    List<Proxy> proxyList = new ArrayList<Proxy>();
                    proxyList.add(proxy);
                    targetMap.put(targetClass, proxyList);
                }
            }
        }
        return targetMap;

    }

}
