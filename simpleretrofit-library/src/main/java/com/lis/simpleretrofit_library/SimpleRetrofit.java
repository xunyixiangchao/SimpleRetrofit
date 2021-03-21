package com.lis.simpleretrofit_library;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class SimpleRetrofit {
    HttpUrl baseUrl;
    Call.Factory callFactory;
    Map<Method, ServiceMethod> mServiceMethodCache = new HashMap<>();

    public SimpleRetrofit(Builder builder) {
        this.baseUrl = HttpUrl.parse(builder.baseUrl);
        this.callFactory = builder.callFactory;
    }

    public <T> T create(Class<T> service) {

        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //解析这个method上所有的注解
                ServiceMethod serviceMethod = loadServiceMethod(method);
                //args是参数
                return serviceMethod.invoke(args);
            }
        });
    }

    private ServiceMethod loadServiceMethod(Method method) {
        //先不上锁，避免synchronized的性能损耗
        ServiceMethod result = mServiceMethodCache.get(method);
        if (result == null) {
            //多线程下避免重复解析
            synchronized (mServiceMethodCache) {
                result = mServiceMethodCache.get(method);
                if (result == null) {
                    result = new ServiceMethod.Builder(this, method).build();
                    mServiceMethodCache.put(method, result);
                }
            }
        }
        return result;
    }

    public static final class Builder {
        String baseUrl;
        private Call.Factory callFactory;

        public Builder() {
        }

        public Builder baseUrl(String url) {
            this.baseUrl = url;
            return this;
        }

        public Builder callFactory(Call.Factory factory) {
            this.callFactory = factory;
            return this;
        }

        public SimpleRetrofit build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }
            okhttp3.Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                this.callFactory = new OkHttpClient();
            }
            return new SimpleRetrofit(this);
        }
    }


}
