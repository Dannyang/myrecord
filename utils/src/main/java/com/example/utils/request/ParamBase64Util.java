package com.example.utils.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * @author liaozhangsheng
 * @since 2023/5/11
 */
@Slf4j
public class ParamBase64Util {


    /**
     * 加密参数,将 json数据,加密成字符串 给app
     *
     * @param o 需要加密的数据
     * @return
     */
    public static String encryption(Object o) {
        String data;
        if (o == null) {
            data = StringUtils.EMPTY;
        } else {
            data = o instanceof String ? o.toString() : JSON.toJSONString(o,
                    SerializerFeature.PrettyFormat,
                    SerializerFeature.WriteNullStringAsEmpty,
                    SerializerFeature.WriteNullNumberAsZero,
                    SerializerFeature.WriteNullListAsEmpty,
                    SerializerFeature.WriteNullBooleanAsFalse,
                    //SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteBigDecimalAsPlain,
                    //非String类型转String
                    SerializerFeature.WriteNonStringValueAsString,
                    SerializerFeature.DisableCircularReferenceDetect);
        }
        if (StringUtils.isEmpty(data)) {
            return "";
        }
        String urlEncodeData = "";
        try {
            urlEncodeData = URLEncoder.encode(data, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("异常URLEncoder.encode异常了", e);
        }
        String encodeToString = new String(Base64.getEncoder().encode(urlEncodeData.getBytes(StandardCharsets.UTF_8)));
        encodeToString = new StringBuffer(encodeToString).reverse().toString();
        if (encodeToString.length() < 20) {
            return encodeToString;
        }
        String start = encodeToString.substring(0, 10);
        String body = encodeToString.substring(10, encodeToString.length() - 10);
        String end = encodeToString.substring(encodeToString.length() - 10);
        String s = end + body + start;
        String currUrl = getRequest().getRequestURI();
        log.info("curr url = {}，返回数据进行加密传输,加密前数据:{},加密后数据:{}", currUrl, data, s);
        return s;
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            throw new RuntimeException("");
        }
        return attributes.getRequest();
    }

    /**
     * 解密-app传过来的 加密的字符串解密成  ostype=1&userId=1571885&appVersionName=6.1.10
     *
     * @param encodeString 加密字符串
     * @return
     */
    public static String decrypt(String encodeString) {
        try {
            if (encodeString.length() >= 20) {
                String start = encodeString.substring(0, 10);
                String body = encodeString.substring(10, encodeString.length() - 10);
                String end = encodeString.substring(encodeString.length() - 10);
                encodeString = end + body + start;
            }
            encodeString = new StringBuffer(encodeString).reverse().toString();
            byte[] bytes = Base64.getDecoder().decode(encodeString);
            String s = new String(bytes, StandardCharsets.UTF_8);
            String replace = URLDecoder.decode(s, StandardCharsets.UTF_8.name());
            String currUrl = getRequest().getRequestURI();
            log.info("curr url = {}，传入参数进行解密处理,解密前数据:{},解密后数据:{}", currUrl, encodeString, replace);
            return replace;
        } catch (Exception e) {
            log.error("解析数据异常：", e);
        }
        return StringUtils.EMPTY;
    }

    public static Map<String, Object> decryptToMap(String encodeData) {
        Map<String, Object> map = Maps.newHashMap();
        String decrypt = decrypt(encodeData);
        if (StringUtils.isBlank(decrypt)) {
            return map;
        }
        Arrays.stream(decrypt.split("&")).forEach(s -> {
            String[] split = s.split("=", 2);
            if (split.length == 1) {
                map.put(split[0], StringUtils.EMPTY);
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

    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = "xATO9QWSn9Gb";
//        String s1 = "{\n" +
//                "    \"username\": \"caowenhao\",\n" +
//                "    \"password\": \"cd728f590e905b2f58d1f6a7e3666735\",\n" +
//                "    \"appVersion\": \"1.0.0\",\n" +
//                "    \"verificationCode\": \"zrjx\",\n" +
//                "    \"verificationCodeKey\": \"160022de777b487380b1f8b398947b91\",\n" +
//                "    \"machineCode\": \"bf36c94875394c3e82155b3920a47009\"\n" +
//                "}";
        String decrypt = decrypt(s);
        System.out.println(decrypt);
//        String encrypt = encryption(s1);
    }

}
