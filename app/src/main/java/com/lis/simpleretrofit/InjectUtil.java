package com.lis.simpleretrofit;

import android.app.Activity;
import android.view.View;

import com.lis.simpleretrofit.annotation.Click;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InjectUtil {


    public static void injectClick(Activity activity) {
        Class<? extends Activity> aClass = activity.getClass();
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if(method.isAnnotationPresent(Click.class)){
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    if(annotation instanceof Click){
                        int[] value = ((Click) annotation).value();
                        for (int res : value) {
                            View view = activity.findViewById(res);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        method.invoke(activity,view);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }

    }
}
