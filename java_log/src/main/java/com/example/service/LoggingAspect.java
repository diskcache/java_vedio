package com.example.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // 拦截所有Controller层方法
  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void controllerLayer() {}

  // 拦截所有Service层方法
  @Pointcut("within(@org.springframework.stereotype.Service *)")
  public void serviceLayer() {}

  @Around("controllerLayer() || serviceLayer()")
  public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
      String methodName = joinPoint.getSignature().getName();
      String className = joinPoint.getTarget().getClass().getSimpleName();
      logger.info("Entering: {}.{}()", className, methodName);

      long start = System.currentTimeMillis();
      Object result = joinPoint.proceed(); // 执行原方法
      long duration = System.currentTimeMillis() - start;

      logger.info("Exiting: {}.{}() | Time: {} ms", className, methodName, duration);
      return result;
  }
}
