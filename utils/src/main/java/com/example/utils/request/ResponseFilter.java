package com.example.utils.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ResponseFilter implements Filter {
    private List<String> excludedUrls = Lists.newArrayList();

    @Override
    public void init(FilterConfig filterConfig) {
        String param = filterConfig.getInitParameter("CommonConstant.EXCLUDE_URL_KEY");
        if (StringUtils.isNotEmpty(param)) {
            this.excludedUrls = Arrays.asList(param.split(","));
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest originRequest = (HttpServletRequest) servletRequest;
        if (excludedUrls.contains(originRequest.getRequestURI()) || StringUtils.isEmpty(originRequest.getParameter("CommonConstant.ENCODE_DATA_KEY"))) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(servletRequest, responseWrapper);
        byte[] content = responseWrapper.getResponseData();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("encodeData", ParamBase64Util.encryption(new String(content)));
        writeResponse(response, response.getStatus(), JSON.toJSONString(jsonObject));
    }

    private void writeResponse(HttpServletResponse response, int status, String json) {
        try {
            response.reset();//很重要
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "*");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(status);
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error("写入数据到response失败", e);
        }
    }


    @Override
    public void destroy() {
        // 不需要
    }
}
