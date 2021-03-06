package com.bob.scala.webapi.aop

import java.lang.reflect.Method

import com.bob.scala.webapi.utils.LoggerObject
import com.bob.scala.webapi.utils.StringImplicit.RichFormatter
import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect}
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by bob on 16/2/29.
  */
@Component
@Aspect
class ControllerLogAop extends LoggerObject {

  @Autowired
  var objectMapper: ObjectMapper = _

  @Around("execution(* com.bob.*.webapi.controller.*.*(..))")
  def doAroundMapper(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val signature: MethodSignature = proceedingJoinPoint.getSignature.asInstanceOf[MethodSignature]
    val method: Method = signature.getMethod
    val className = proceedingJoinPoint.getTarget.getClass.getName

    val start = System.currentTimeMillis()
    LOGGER.info("executing controller %s method %s request params %s and time is %s".format(className, method.getName,
      objectMapper.writeValueAsString(proceedingJoinPoint.getArgs), start))

    var result: Object = null
    try {
      result = proceedingJoinPoint.proceed
    }
    catch {
      case throwable: Throwable => {
        throw throwable
      }
    }
    val end = System.currentTimeMillis()
    if (LOGGER.isInfoEnabled) {
      if (!result.isInstanceOf[rx.Observable[_]]) {
        LOGGER.info("executed controller #{controllername} method #{methodname} response #{response} and total time is #{time} ms"
          .richFormat(
            Map("controllername" -> className,
              "methodname" -> method.getName,
              "response" -> objectMapper.writeValueAsString(method.getReturnType.cast(result)),
              "time" -> (end - start))))
      }
    }
    else {
      LOGGER.info("executed controller #{controllername} method #{methodname} response #{response} and total time is #{time} ms"
        .richFormat(
          Map("controllername" -> className,
            "methodname" -> method.getName,
            "time" -> (end - start))))
    }
    result
  }
}