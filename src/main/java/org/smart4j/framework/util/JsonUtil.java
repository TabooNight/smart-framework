package org.smart4j.framework.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 流操作工具类
 */
public final class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将 POJO 转为 JSON
     *
     * @param obj
     *              POJO
     * @param <T>
     *              泛型
     * @return
     */
    public static <T> String toJson(T obj) {

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.error("convert POJO to JSON failure", e);
            throw new RuntimeException(e);
        }
        return json;

    }

    /**
     * 将 JSON 转为 POJO
     *
     * @param json
     *              json字符串
     * @param type
     *              POJO
     * @param <T>
     *              泛型
     * @return
     */
    public static <T> T fromJson(String json, Class<T> type) {

        T pojo;
        try {
            pojo = OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            LOGGER.error("convert JSON to POJO failure", e);
            throw new RuntimeException(e);
        }
        return pojo;

    }

}
