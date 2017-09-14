package org.smart4j.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet助手类
 */
public class ServletHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletHelper.class);

    // 使每个线程独自拥有一份ServletHelper实例
    private static final ThreadLocal<ServletHelper> SERVLET_HELPER_HOLDER
            = new ThreadLocal<ServletHelper>();

    private HttpServletRequest request;
    private HttpServletResponse response;

    private ServletHelper(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 初始化
     *
     * @param request
     *                  HttpServletRequest
     * @param response
     *                  HttpServletResponse
     */
    public static void init(HttpServletRequest request, HttpServletResponse response) {

        SERVLET_HELPER_HOLDER.set(new ServletHelper(request, response));

    }

    /**
     * 销毁
     */
    public static void destory() {

        SERVLET_HELPER_HOLDER.remove();

    }

    /**
     * 获取HttpServletRequest对象
     *
     * @return
     */
    public static HttpServletRequest getRequest() {

        return SERVLET_HELPER_HOLDER.get().request;

    }

    /**
     * 获取HttpServletResponse对象
     *
     * @return
     */
    public static HttpServletResponse getResponse() {

        return SERVLET_HELPER_HOLDER.get().response;

    }

    /**
     * 获取HttpSession对象
     *
     * @return
     */
    public static HttpSession getSession() {

        return getRequest().getSession();

    }

    /**
     * 获取ServletContext对象
     *
     * @return
     */
    public static ServletContext getServletContext() {

        return getRequest().getServletContext();

    }

}
