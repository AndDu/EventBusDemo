package com.example.eventbusdemo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBus {

    private final ExecutorService executorService;
    //容器，保存方法
    private Map<Object, List<SubscribleMethod>> cacheMap;
    private Handler handler;

    private EventBus() {
        if (Looper.myLooper()!=Looper.getMainLooper()){
            throw new RuntimeException("必须在主线程中注册EventBus");
        }
        cacheMap = new HashMap<>();
        handler = new Handler();
       executorService = Executors.newFixedThreadPool(5);
    }

    private static volatile EventBus instance;

    public static EventBus getDefault() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }


    public void register(Object object) {

        //寻找object（）所有的带有Subscrible的方法放入到cacheMap中
        List<SubscribleMethod> list = cacheMap.get(object);
        if (list == null) {
            list = findSubscribleMethod(object);
        }
    }

    private List<SubscribleMethod> findSubscribleMethod(Object object) {

        ArrayList<SubscribleMethod> objects = new ArrayList<>();

        Class<?> aClass = object.getClass();
        while (aClass != null) {

            //凡是系统级的父类，直接跳转出去 //也可以只允许自己的包，这也是一种思路
            String name = aClass.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.") || name.startsWith("androidx.")) {
                break;
            }
            //包含父类的方法
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method : declaredMethods) {

                //Override的Retention是RetentionPolicy.SOURCE,运行时是找不到的，所以下面的override是一直为null
//            Subscrible override = method.getAnnotation(Override.class);
                Subscrible subscrible = method.getAnnotation(Subscrible.class);
                if (subscrible == null) {
                    continue;
                }

                //判断方法中的参数类型和个数
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1) {
                    Log.e("findSubscribleMethod: ", "参数过多");
                }
                ThreadMode threadMode = subscrible.threadMode();
                SubscribleMethod subscribleMethod = new SubscribleMethod(method, threadMode, object.getClass());
                objects.add(subscribleMethod);
            }
            aClass = aClass.getSuperclass();
        }
        return objects;

    }

    public void post(final Object type) {
        //直接循环cacheMap里的方法，找到对应的调用
        Set<Object> objects = cacheMap.keySet();
        Iterator<Object> iterator = objects.iterator();
        while (iterator.hasNext()) {
            final Object next = iterator.next();
            List<SubscribleMethod> list = cacheMap.get(next);
            for (final SubscribleMethod subscribleMethod : list) {
                //a(if条件前面的对象) 对象所对应的类型是不是b(if条件后面的对象)对象所对应的类信息的父类或者接口
                //和instance of有啥区别？问题
                if (subscribleMethod.getType().isAssignableFrom(type.getClass())) {
                    switch (subscribleMethod.getThreadMode()) {
                        case MAIN:
                            //主线程
                            if (Looper.getMainLooper() == Looper.myLooper()) {
                                invoke(subscribleMethod, next, type);
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribleMethod, next, type);
                                    }
                                });
                            }
                            break;
                        case BACKGROUND:

                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                invoke(subscribleMethod, next, type);
                            } else {
                                executorService.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribleMethod, next, type);
                                    }
                                });
                            }
                            break;
                    }

                }

            }
        }
    }

    private void invoke(SubscribleMethod subscribleMethod, Object next, Object type) {
        Method method = subscribleMethod.getMethod();
        try {
            method.invoke(next, type);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
