package com.jeonlog.exhibition_recommender.common.metric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {

    private final MetricRecorder recorder;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer paramNames = new DefaultParameterNameDiscoverer();

    @Around("@annotation(countView)")
    public Object around(ProceedingJoinPoint pjp, CountView countView) throws Throwable {
        Object result = pjp.proceed();

        try {
            EvaluationContext ctx = buildContext(pjp, result);

            if (!evalBool(countView.condition(), ctx)) {
                return result;
            }

            Object id = parser.parseExpression(countView.idExpr()).getValue(ctx);
            if (id == null) {
                return result;
            }

            recorder.recordEvent(countView.action(), countView.type(), id, countView.rank());
        } catch (Exception e) {
            log.warn("[METRIC] aspect eval failed type={} action={} reason={}",
                    countView.type(), countView.action(), e.getMessage());
        }

        return result;
    }

    private EvaluationContext buildContext(ProceedingJoinPoint pjp, Object result) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        StandardEvaluationContext ctx = new StandardEvaluationContext();

        String[] names = paramNames.getParameterNames(method);
        Object[] args = pjp.getArgs();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                ctx.setVariable(names[i], args[i]);
            }
        }
        ctx.setVariable("result", result);
        return ctx;
    }

    private boolean evalBool(String expr, EvaluationContext ctx) {
        if (expr == null || expr.isBlank() || "true".equals(expr)) return true;
        Expression parsed = parser.parseExpression(expr); // 문자열을 명령으로 변환해라
        Boolean v = parsed.getValue(ctx, Boolean.class); // 변환된 명령을 실행해라 (값은 불리안)
        return Boolean.TRUE.equals(v); 
    }
}
