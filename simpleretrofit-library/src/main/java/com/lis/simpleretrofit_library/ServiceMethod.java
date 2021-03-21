package com.lis.simpleretrofit_library;


import com.lis.simpleretrofit_library.annotation.Field;
import com.lis.simpleretrofit_library.annotation.GET;
import com.lis.simpleretrofit_library.annotation.POST;
import com.lis.simpleretrofit_library.annotation.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 记录请求类型，参数，完整地址等
 */
public class ServiceMethod {

    private final String httpMethod;
    private final String relativeUrl;
    private final boolean hasBody;
    private final ParameterHandler[] parameterHandlers;
    private FormBody.Builder formBuild;
    private final Call.Factory callFactory;
    HttpUrl baseUrl;
    HttpUrl.Builder httpBuilder;

    public ServiceMethod(Builder builder) {
        baseUrl = builder.retrofit.baseUrl;
        callFactory = builder.retrofit.callFactory;
        this.httpMethod = builder.httpMethod;
        relativeUrl = builder.relativeUrl;
        hasBody = builder.hasBody;
        parameterHandlers = builder.parameterHandlers;

        //如果有请求体，创建一个okhttp的请求体
        if (hasBody) {
            formBuild = new FormBody.Builder();
        }
    }

    public Object invoke(Object[] args) {
        /**
         * 1.处理请求的地址和参数
         */
        for (int i = 0; i < parameterHandlers.length; i++) {
            ParameterHandler parameterHandler = parameterHandlers[i];
            //handler本来记录了key，现在传入了value
            parameterHandler.apply(this, args[i].toString());
        }

        //获取最终请求地址
        HttpUrl url;
        if (httpBuilder == null) {
            httpBuilder = baseUrl.newBuilder(relativeUrl);
        }
        url = httpBuilder.build();

        //请求体
        FormBody formBody = null;
        if (formBuild != null) {
            formBody = formBuild.build();
        }
        Request request = new Request.Builder().url(url).method(httpMethod, formBody).build();
        Call call = callFactory.newCall(request);
        return call;
    }

    //post把key value放到请求体中
    public void addFieldParameter(String key, String value) {
        formBuild.add(key, value);
    }

    //get 请求 把key,value拼接到url
    public void addQueryParameter(String key, String value) {
        if (httpBuilder == null) {
            httpBuilder = baseUrl.newBuilder(relativeUrl);
        }
        httpBuilder.addQueryParameter(key, value);
    }

    public static class Builder {
        private final SimpleRetrofit retrofit;
        private final Annotation[] annotations;
        private final Annotation[][] parameterAnnotations;
        private String httpMethod;
        private String relativeUrl;
        private boolean hasBody;
        ParameterHandler[] parameterHandlers;

        public Builder(SimpleRetrofit retrofit, Method method) {
            this.retrofit = retrofit;
            //获取方法上的注解
            annotations = method.getAnnotations();
            //获取方法参数上的所有注解(一个参数可以有多个注解，一个方法可以有多个参数)
            parameterAnnotations = method.getParameterAnnotations();


        }

        public ServiceMethod build() {
            /**
             * 1。解析方法上的注解，只处理Post与Get
             */
            for (Annotation annotation : annotations) {
                if (annotation instanceof POST) {
                    //记录当前请求方式
                    this.httpMethod = "POST";
                    //记录url
                    this.relativeUrl = ((POST) annotation).value();
                    //有没有请求体
                    this.hasBody = true;
                } else if (annotation instanceof GET) {
                    this.httpMethod = "GET";
                    this.relativeUrl = ((GET) annotation).value();
                    this.hasBody = false;
                }
            }
            /**
             * 2。解析方法参数的注解
             */
            int length = parameterAnnotations.length;
            parameterHandlers = new ParameterHandler[length];

            for (int i = 0; i < length; i++) {
                //一个参数上所有的注解
                Annotation[] annotations = parameterAnnotations[i];
                //处理参数上的每一个注解
                for (Annotation annotation : annotations) {
                    // todo:可以加一个判断，如果是GET，又解析到Field可以提示使用者使用Query注解
                    if (annotation instanceof Field) {
                        //注解上的value：请求参数的key
                        String value = ((Field) annotation).value();
                        parameterHandlers[i] = new ParameterHandler.FieldParameterHandler(value);
                    } else if (annotation instanceof Query) {
                        String value = ((Query) annotation).value();
                        parameterHandlers[i] = new ParameterHandler.QueryParameterHandler(value);
                    }
                }
            }
            return new ServiceMethod(this);
        }
    }
}
