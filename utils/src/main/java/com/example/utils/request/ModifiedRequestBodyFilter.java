package com.example.utils.request;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ModifiedRequestBodyFilter implements Filter {

    private List<String> excludedUrls = Lists.newArrayList();
    @Override
    public void init(FilterConfig filterConfig) {
        //todo
        String param = filterConfig.getInitParameter("");
        if (StringUtils.isNotEmpty(param)) {
            this.excludedUrls = Arrays.asList(param.split(","));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest originRequest = (HttpServletRequest) request;
        if (excludedUrls.contains(originRequest.getRequestURI()) || StringUtils.isEmpty(originRequest.getParameter(""))) {
            chain.doFilter(originRequest, response);
        } else {
            ModifiedRequestBodyWrapper xssRequest = new ModifiedRequestBodyWrapper((HttpServletRequest) request);
            chain.doFilter(xssRequest, response);
        }
    }

    @Override
    public void destroy() {
        // 不需要
    }



}
