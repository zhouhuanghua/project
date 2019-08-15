package cn.zhh.admin.aspect;

import cn.zhh.admin.dto.rsp.Response;
import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.util.BusinessException;
import cn.zhh.common.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author Zhou Huanghua
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理Throwable异常
     *
     * @param t 异常对象
     * @return 统一Response
     */
    @ExceptionHandler(Throwable.class)
    public Response handleThrowable(Throwable t) {
        String msg = ThrowableUtils.getThrowableStackTrace(t);
        log.error("统一处理未知异常：{}", msg);
        return Response.err(ErrorEnum.UN_KNOW, msg);
    }

    /**
     * 处理业务异常
     *
     * @param e 异常对象
     * @return 统一Response
     */
    @ExceptionHandler(BusinessException.class)
    public Response handleBusinessException(BusinessException e) {
        String msg = e.getMessage();
        log.error("统一处理业务异常：{}", msg);
        return Response.err(e.getCode(), msg);
    }
}
