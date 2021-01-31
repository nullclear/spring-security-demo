package dev.yxy.global;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class Response {
    //总状态码
    private static final int SUCCESS = 0;
    private static final int NOT_SUCCESS = 1;

    /**
     * 成功
     *
     * @return json
     */
    public static String success() {
        ResponseData result = new ResponseData(SUCCESS, null, null, null);
        return toJson(result);
    }

    /**
     * 成功
     *
     * @param data 成功数据
     * @return json
     */
    public static String success(@Nullable Object data) {
        ResponseData result = new ResponseData(SUCCESS, null, null, data);
        return toJson(result);
    }

    /**
     * 成功
     *
     * @param msg  成功的信息
     * @param data 成功数据
     * @return json
     */
    public static String success(@Nullable String msg, @Nullable Object data) {
        ResponseData result = new ResponseData(SUCCESS, null, msg, data);
        return toJson(result);
    }

    /**
     * 成功
     *
     * @param code 成功状态码
     * @param msg  成功的信息
     * @param data 成功数据
     * @return json
     */
    public static String success(@Nullable Integer code, @Nullable String msg, @Nullable Object data) {
        ResponseData result = new ResponseData(NOT_SUCCESS, code, msg, data);
        return toJson(result);
    }

    /**
     * 失败
     *
     * @return json
     */
    public static String failure() {
        ResponseData result = new ResponseData(NOT_SUCCESS, null, null, null);
        return toJson(result);
    }

    /**
     * 失败
     *
     * @param data 失败数据
     * @return json
     */
    public static String failure(@Nullable Object data) {
        ResponseData result = new ResponseData(NOT_SUCCESS, null, null, data);
        return toJson(result);
    }

    /**
     * 失败
     *
     * @param msg  失败的信息
     * @param data 失败数据
     * @return json
     */
    public static String failure(@Nullable String msg, @Nullable Object data) {
        ResponseData result = new ResponseData(NOT_SUCCESS, null, msg, data);
        return toJson(result);
    }

    /**
     * 失败
     *
     * @param code 失败状态码
     * @param msg  失败的信息
     * @param data 失败数据
     * @return json
     */
    public static String failure(@Nullable Integer code, @Nullable String msg, @Nullable Object data) {
        ResponseData result = new ResponseData(NOT_SUCCESS, code, msg, data);
        return toJson(result);
    }

    /**
     * 异常
     *
     * @param msg 异常信息
     * @return json
     */
    public static String exception(@Nullable String msg) {
        ResponseData result = new ResponseData(NOT_SUCCESS, null, msg, null);
        return toJson(result);
    }

    /**
     * 异常
     *
     * @param code 异常状态码
     * @param msg  异常信息
     * @return json
     */
    public static String exception(int code, @Nullable String msg) {
        ResponseData result = new ResponseData(NOT_SUCCESS, code, msg, null);
        return toJson(result);
    }

    /**
     * 异常
     *
     * @param msg  异常信息
     * @param data 异常数据
     * @return json
     */
    public static String exception(@Nullable String msg, @Nullable Object data) {
        ResponseData result = new ResponseData(NOT_SUCCESS, null, msg, data);
        return toJson(result);
    }

    /**
     * 异常
     *
     * @param code 异常状态码
     * @param msg  异常信息
     * @param data 异常数据
     * @return json
     */
    public static String exception(@Nullable Integer code, @Nullable String msg, @Nullable Object data) {
        ResponseData result = new ResponseData(NOT_SUCCESS, code, msg, data);
        return toJson(result);
    }

    // 线程隔离的 ObjectMapper
    private static final ThreadLocal<ObjectMapper> THREAD_LOCAL = ThreadLocal.withInitial(Response::init);

    /**
     * 初始化 ObjectMapper
     */
    @NotNull
    private static ObjectMapper init() {
        ObjectMapper mapper = new ObjectMapper();
        //设置可见性
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        //设置序列时排除null
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //设置日期格式化
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //设置地区
        mapper.setLocale(Locale.CHINA);
        //设置时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        //设置空值不报错
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //设置未知属性不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //设置允许单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //设置char数组转为json数组
        mapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
        return mapper;
    }

    /**
     * ObjectMapper 实例
     */
    @NotNull
    private static ObjectMapper instance() {
        ObjectMapper mapper = THREAD_LOCAL.get();
        if (mapper == null) {
            mapper = init();
            THREAD_LOCAL.set(mapper);
        }
        return mapper;
    }

    /**
     * 可以将任意类型对象转为Json
     *
     * @param obj 任意类型对象
     * @return json格式数据
     */
    @Nullable
    private static String toJson(@Nullable Object obj) {
        if (null == obj) {
            return null;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).toString();
        }
        try {
            return instance().writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 响应数据类
     */
    protected static class ResponseData implements Serializable {
        private static final long serialVersionUID = 7268900673527494837L;
        //总状态
        @MagicConstant(intValues = {SUCCESS, NOT_SUCCESS})
        private int status;
        //状态码
        @Nullable
        private Integer code;
        //信息
        @Nullable
        private String msg;
        //数据
        @Nullable
        private Object data;

        public ResponseData(@MagicConstant(intValues = {SUCCESS, NOT_SUCCESS}) int status, @Nullable Integer code, @Nullable String msg, @Nullable Object data) {
            this.status = status;
            this.code = code;
            this.msg = msg;
            this.data = data;
        }

        @MagicConstant(intValues = {SUCCESS, NOT_SUCCESS})
        public int getStatus() {
            return status;
        }

        public void setStatus(@MagicConstant(intValues = {SUCCESS, NOT_SUCCESS}) int status) {
            this.status = status;
        }

        @Nullable
        public Integer getCode() {
            return code;
        }

        public void setCode(@Nullable Integer code) {
            this.code = code;
        }

        @Nullable
        public String getMsg() {
            return msg;
        }

        public void setMsg(@Nullable String msg) {
            this.msg = msg;
        }

        @Nullable
        public Object getData() {
            return data;
        }

        public void setData(@Nullable Object data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "{"
                    + "\"status\":"
                    + status
                    + ",\"code\":"
                    + code
                    + ",\"msg\":\""
                    + msg + '\"'
                    + ",\"data\":"
                    + data
                    + "}";
        }
    }
}
