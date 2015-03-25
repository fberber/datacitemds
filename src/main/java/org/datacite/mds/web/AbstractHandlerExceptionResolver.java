package org.datacite.mds.web;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractHandlerExceptionResolver extends DefaultHandlerExceptionResolver {

    protected Class<?>[] handlersClasses;

    @Override
    protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            Class clazz = hm.getMethod().getDeclaringClass();
            for (Class<?> handlerClass : handlersClasses) {
                if (handlerClass.isAssignableFrom(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }
}
