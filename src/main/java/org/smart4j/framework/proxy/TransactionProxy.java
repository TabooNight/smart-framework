package org.smart4j.framework.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Transaction;
import org.smart4j.framework.helper.DatabaseHelper;

import java.lang.reflect.Method;

/**
 * 事务代理
 */
public class TransactionProxy implements Proxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProxy.class);
    // 本地线程变量，保证统一线程中的事务控制相关逻辑只会执行一次
    private static final ThreadLocal<Boolean> FLAG_HOLDER = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public Object doProxy(ProxyChain proxyChain) throws Throwable {

        Object result;
        boolean flag = FLAG_HOLDER.get();
        Method method = proxyChain.getTargetMethod();

        if (!flag && method.isAnnotationPresent(Transaction.class)) {// 第一次执行并且存在事务注解
            FLAG_HOLDER.set(true);
            try {
                DatabaseHelper.beginTransaction();// 开启事务
                LOGGER.debug("begin transaction");
                result = proxyChain.doProxyChain();
                DatabaseHelper.commitTransaction();// 提交事务
                LOGGER.debug("commit transaction");
            } catch (Exception e) {
                DatabaseHelper.rollbackTransaction();// 回滚事务
                LOGGER.debug("rollback transaction");
                throw e;
            } finally {
                FLAG_HOLDER.remove();
            }
        } else {
            result = proxyChain.doProxyChain();
        }
        return result;

    }
}
