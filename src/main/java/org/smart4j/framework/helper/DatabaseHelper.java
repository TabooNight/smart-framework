package org.smart4j.framework.helper;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.PropsUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 数据库操作助手类
 */
public class DatabaseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    // 数据库配置
    private static final ThreadLocal<Connection> CONNECTION_HOLDER;
    private static final QueryRunner QUERY_RUNNER;
    private static final BasicDataSource DATA_SOURCE;

    static {
        CONNECTION_HOLDER = new ThreadLocal<Connection>();
        QUERY_RUNNER = new QueryRunner();

        Properties conf = PropsUtil.loadProps("smart.properties");
        String driver = conf.getProperty("smart.framework.jdbc.driver");
        String url = conf.getProperty("smart.framework.jdbc.url");
        String username = conf.getProperty("smart.framework.jdbc.username");
        String password = conf.getProperty("smart.framework.jdbc.password");

        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setDriverClassName(driver);
        DATA_SOURCE.setUrl(url);
        DATA_SOURCE.setUsername(username);
        DATA_SOURCE.setPassword(password);
    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    public static Connection getConnection() {

        Connection conn = CONNECTION_HOLDER.get();// 从ThreadLocal寻找Connection，没有则创建
        if (conn == null) {
            try {
                conn = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("get connection failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);// 将新的Connection放入ThreadLocal中
            }
        }
        return conn;

    }

    /**
     * 查询实体列表
     *
     * @param entityClass
     *                      实体对象
     * @param sql
     *                      SQL语句
     * @param params
     *                      参数
     * @param <T>
     *                      泛型
     * @return
     */
    public static <T> List<T> queryEntityList(Class<T> entityClass, String sql, Object... params) {

        List<T> entityList;
        Connection conn = getConnection();
        try {
            entityList = QUERY_RUNNER.query(conn, sql, new BeanListHandler<T>(entityClass), params);
        } catch (SQLException e) {
            LOGGER.error("query entity list failure", e);
            throw new RuntimeException(e);
        }
        return entityList;

    }

    /**
     * 查询实体
     *
     * @param entityClass
     *                      实体类
     * @param sql
     *                      SQL语句
     * @param params
     *                      参数
     * @param <T>
     *                      泛型
     * @return
     */
    public static <T> T queryEntity(Class<T> entityClass, String sql, Object... params) {

        T entity = null;
        Connection conn = getConnection();
        try {
            QUERY_RUNNER.query(conn, sql, new BeanHandler<T>(entityClass), params);
        } catch (SQLException e) {
            LOGGER.error("query entity failure", e);
            throw new RuntimeException(e);
        }
        return entity;

    }

    /**
     * 执行更新语句(update、insert、delete)
     *
     * @param sql
     *                  SQL语句
     * @param params
     *                  参数
     * @return
     */
    public static int executeUpdate(String sql, Object... params) {

        int rows = 0;
        Connection conn = getConnection();
        try {
            rows = QUERY_RUNNER.update(conn, sql, params);
        } catch (SQLException e) {
            LOGGER.error("execute update failure", e);
            throw new RuntimeException(e);
        }
        return rows;

    }

    /**
     * 插入实体
     *
     * @param entityClass
     *                      实体类
     * @param fieldMap
     *                      参数
     * @param <T>
     *                      泛型
     * @return
     */
    public static <T> boolean insertEntity(Class<T> entityClass, Map<String, Object> fieldMap) {

        if (CollectionUtil.isEmpty(fieldMap)) {
            LOGGER.error("can not insert entity: fieldMap is empty");
            return false;
        }

        String sql = "INSERT INTO " + getTableName(entityClass);
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String fieldName: fieldMap.keySet()) {
            columns.append(fieldName).append(", ");
            values.append("?, ");
        }
        columns.replace(columns.lastIndexOf(","), columns.length(), ")");
        values.replace(columns.lastIndexOf(","), columns.length(), ")");
        sql += columns + " VALUES " + values;

        Object[] params = fieldMap.values().toArray();
        return executeUpdate(sql, params) == 1;

    }

    /**
     * 更新实体
     *
     * @param entityClass
     *                      实体类
     * @param id
     *                      ID
     * @param fieldMap
     *                      参数
     * @param <T>
     *                      泛型
     * @return
     */
    public static <T> boolean updateEntity(Class<T> entityClass, long id, Map<String, Object> fieldMap) {

        if (CollectionUtil.isEmpty(fieldMap)) {
            LOGGER.error("can not update entity: fieldMap is empty");
            return false;
        }

        String sql = "UPDATE " + getTableName(entityClass) + " SET ";
        StringBuilder columns = new StringBuilder();
        for (String fieldName: fieldMap.keySet()) {
            columns.append(fieldName).append("=?, ");
        }

        sql += columns.substring(0, columns.lastIndexOf(",")) + "WHERE id = ?";

        List<Object> paramList = new ArrayList<Object>();
        paramList.add(fieldMap.values());
        paramList.add(id);
        Object[] params = paramList.toArray();

        return executeUpdate(sql, params) == 1;

    }

    /**
     * 删除实体
     *
     * @param entityClass
     *                      实体类
     * @param id
     *                      ID
     * @param <T>
     *                      泛型
     * @return
     */
    public static <T> boolean deleteEntity(Class<T> entityClass, long id) {

        String sql = "DELETE FROM " + getTableName(entityClass) + " WHERE id = ?";
        return executeUpdate(sql, id) == 1;

    }

    /**
     * 执行SQL文件
     *
     * @param sqlFile
     *                  SQL文件
     */
    public static void executeSqlFile(String sqlFile) {

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sqlFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String sql;
        try {
            while ((sql = reader.readLine()) != null) {
                executeUpdate(sql);
            }
        } catch (IOException e) {
            LOGGER.error("execute sql file failure", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 开启事务
     */
    public static void beginTransaction() {

        Connection conn = getConnection();// 获取数据库连接
        if (conn != null) {
            try {
                conn.setAutoCommit(false);// 设置为手动提交事务
            } catch (SQLException e) {
                LOGGER.error("begin transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);// 将数据库连接放入本地线程变量中
            }
        }

    }

    /**
     * 提交事务
     */
    public static void commitTransaction() {

        Connection conn = getConnection();// 获取数据库连接
        if (conn != null) {
            try {
                conn.commit();// 提交事务
                conn.close();// 关闭连接
            } catch (SQLException e) {
                LOGGER.error("commit transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();// 将数据库连接从本地线程变量中移除
            }
        }

    }

    /**
     * 回滚事务
     */
    public static void rollbackTransaction() {

        Connection conn = getConnection();// 获取数据库连接
        if (conn != null) {
            try {
                conn.rollback();// 回滚事务
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("rollback transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();// 将数据库连接从本地线程变量中移除
            }
        }

    }

    /**
     * 获取表名
     *
     * @param entityClass
     *                      实体对象
     * @return
     */
    private static <T> String getTableName(Class<T> entityClass) {

        return entityClass.getSimpleName();

    }

}
