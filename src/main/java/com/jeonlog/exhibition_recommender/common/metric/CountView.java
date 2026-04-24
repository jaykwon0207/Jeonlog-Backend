package com.jeonlog.exhibition_recommender.common.metric;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CountView {

    String type();

    String idExpr();

    Action action() default Action.VIEW;

    String condition() default "true";

    boolean rank() default true;
}
