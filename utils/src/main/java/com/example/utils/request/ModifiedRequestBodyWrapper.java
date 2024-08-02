package com.example.utils.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Slf4j
public class ModifiedRequestBodyWrapper extends HttpServletRequestWrapper {
    private final String modifiedBody;
    private final Map<String, String[]> params = new HashMap<>();

    public ModifiedRequestBodyWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 修改请求体内容
         modifiedBody = modifyRequestBody(getRequestBody(request));
//        // 先把request原有参数装进去
        Enumeration<String> attributeNames = request.getParameterNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeVal = request.getParameter(attributeName);
            params.put(attributeName, new String[]{attributeVal.toString()});
        }
        Map<String, Object> map = toParamMap(getEncodeParam(request));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            if (null != object) {
                params.put(key, new String[]{object.toString()});
            }
        }
    }

    private Map<String, Object> toParamMap(String modifiedBody) {
        Map<String, Object> map = Maps.newHashMap();
        if (StringUtils.isBlank(modifiedBody)) {
            return map;
        }
        Arrays.stream(modifiedBody.split("&")).forEach(s -> {
            String[] split = s.split("=", 2);
            if (split.length == 1) {
                map.put(split[0], "");
            }
            if (split.length > 1) {
                try {
                    map.put(split[0], URLDecoder.decode(split[1], StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    map.put(split[0], split[1]);
                }
            }
        });
        return map;
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(modifiedBody.toString().getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // sonar
            }
        };
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (ServletInputStream inputStream = request.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                stringBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }
        return stringBuilder.toString();
    }

    private String getEncodeParam(HttpServletRequest request) {
        String parameter = request.getParameter("");
        return ParamBase64Util.decrypt(parameter);
    }

    private String modifyRequestBody(String originalBody) {
        if (StringUtils.isEmpty(originalBody)) {
            return StringUtils.EMPTY;
        }
        JSONObject jsonData = JSON.parseObject(originalBody);
        return ParamBase64Util.decrypt(jsonData.getString(""));
    }


    @Override
    public String getParameter(String name) {
        String[] strings = params.get(name);
        if (null != strings && strings.length > 0) {
            return strings[0];
        }
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return params;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return new Vector<>(params.keySet()).elements();
    }

    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }
}