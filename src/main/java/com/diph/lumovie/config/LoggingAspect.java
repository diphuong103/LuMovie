package com.diph.lumovie.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.diph.lumovie.controller..*(..)) || " +
            "execution(* com.diph.lumovie.service..*(..))")
    public Object logMethodCall(ProceedingJoinPoint pjp) throws Throwable {
        String className  = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        log.info(">>> [{}.{}()]", className, methodName);
        Object result = pjp.proceed();
        log.info("<<< [{}.{}()] done", className, methodName);

        return result;
    }
}