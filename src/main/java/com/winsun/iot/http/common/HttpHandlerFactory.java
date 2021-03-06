package com.winsun.iot.http.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HttpHandlerFactory {

    private Set<HttpController> controllers;

    private Map<HttpController, HttpMap> controllerMap = new HashMap<>();

    public HttpHandlerFactory(Set<HttpController> controllerSet) {
        this.controllers = controllerSet;
        for (HttpController controller : controllerSet) {
            HttpMap map = controller.getClass().getAnnotation(HttpMap.class);
            if (map == null) {
                continue;
            }
            controllerMap.put(controller, map);
        }
    }

    public HttpController match(String uri, HttpMethod method) {

        for (Map.Entry<HttpController, HttpMap> entry : controllerMap.entrySet()) {
            String pattern = entry.getValue().value();
            boolean match = macth(uri, pattern);
            if (match) {
                if (entry.getValue().method() == HttpMethod.ALL || entry.getValue().method() == method){
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private boolean macth(String uri, String pattern) {
        if (Objects.equals(uri, pattern)) {
            return true;
        }
        return false;
    }
}
