package org.smart4j.framework.bean;

import org.smart4j.framework.util.CastUtil;

import java.util.Map;

/**
 * 请求参数对象
 */
public class Param {

    private Map<String, Object> paramMap;

    public Param(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    /**
     * 根据参数名获取Long型参数值
     *
     * @param name
     *              参数名
     * @return
     */
    public long getLong(String name) {

        return CastUtil.castLong(paramMap.get(name));

    }

    /**
     * 获取所有字段信息
     *
     * @return
     */
    public Map<String, Object> getMap() {

        return paramMap;

    }

}
