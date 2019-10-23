package com.example.eventbusdemo;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //表示作用到方法；FIEe表示属性
@Retention(RetentionPolicy.RUNTIME) //RUNTIME表示运行时是能找的到的 SOURCE表示是编译时候找得到，运行时候找不到
public @interface Subscrible {

    ThreadMode threadMode() default ThreadMode.MAIN;
}
