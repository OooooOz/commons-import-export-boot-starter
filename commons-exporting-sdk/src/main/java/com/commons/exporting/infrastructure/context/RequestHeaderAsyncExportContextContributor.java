package com.commons.exporting.infrastructure.context;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 基于请求头的异步导出上下文贡献器。
 * <p>
 * 当业务系统依赖 {@link RequestContextHolder} / {@link HttpServletRequest} 获取请求头（例如 {@code orgCode}）时，
 * 该贡献器会在任务提交线程中提取当前请求头，并在异步导出线程中恢复一个轻量级的请求对象，
 * 使 {@code AsyncExportHandler.queryPage(...)} 中的既有请求头读取逻辑保持可用。
 */
public class RequestHeaderAsyncExportContextContributor implements AsyncExportContextContributor {

    @Override
    public AsyncExportContextSnapshot capture() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return AsyncExportContextSnapshot.noop();
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (request == null) {
            return AsyncExportContextSnapshot.noop();
        }
        final RequestHeaderSnapshot snapshot = RequestHeaderSnapshot.from(request);
        return new AsyncExportContextSnapshot() {
            @Override
            public void restore() {
                RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(snapshot.toRequest()));
            }

            @Override
            public void clear() {
                RequestAttributes current = RequestContextHolder.getRequestAttributes();
                try {
                    if (current instanceof ServletRequestAttributes) {
                        ((ServletRequestAttributes) current).requestCompleted();
                    }
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            }
        };
    }

    private static final class RequestHeaderSnapshot {
        private final Map<String, List<String>> headers;
        private final String method;
        private final String protocol;
        private final String scheme;
        private final String serverName;
        private final int serverPort;
        private final String requestUri;
        private final String contextPath;
        private final String servletPath;
        private final String pathInfo;
        private final String queryString;
        private final String remoteAddr;
        private final String remoteHost;
        private final String characterEncoding;
        private final Locale locale;
        private final boolean secure;

        private RequestHeaderSnapshot(Map<String, List<String>> headers,
                                      String method,
                                      String protocol,
                                      String scheme,
                                      String serverName,
                                      int serverPort,
                                      String requestUri,
                                      String contextPath,
                                      String servletPath,
                                      String pathInfo,
                                      String queryString,
                                      String remoteAddr,
                                      String remoteHost,
                                      String characterEncoding,
                                      Locale locale,
                                      boolean secure) {
            this.headers = headers;
            this.method = method;
            this.protocol = protocol;
            this.scheme = scheme;
            this.serverName = serverName;
            this.serverPort = serverPort;
            this.requestUri = requestUri;
            this.contextPath = contextPath;
            this.servletPath = servletPath;
            this.pathInfo = pathInfo;
            this.queryString = queryString;
            this.remoteAddr = remoteAddr;
            this.remoteHost = remoteHost;
            this.characterEncoding = characterEncoding;
            this.locale = locale;
            this.secure = secure;
        }

        private static RequestHeaderSnapshot from(HttpServletRequest request) {
            Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                List<String> values = new ArrayList<>();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues != null && headerValues.hasMoreElements()) {
                    values.add(headerValues.nextElement());
                }
                headers.put(headerName, Collections.unmodifiableList(values));
            }
            return new RequestHeaderSnapshot(
                    Collections.unmodifiableMap(headers),
                    request.getMethod(),
                    request.getProtocol(),
                    request.getScheme(),
                    request.getServerName(),
                    request.getServerPort(),
                    request.getRequestURI(),
                    request.getContextPath(),
                    request.getServletPath(),
                    request.getPathInfo(),
                    request.getQueryString(),
                    request.getRemoteAddr(),
                    request.getRemoteHost(),
                    request.getCharacterEncoding(),
                    request.getLocale(),
                    request.isSecure());
        }

        private HttpServletRequest toRequest() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    new HeaderOnlyHttpServletRequestInvocationHandler(this));
        }
    }

    private static final class HeaderOnlyHttpServletRequestInvocationHandler implements InvocationHandler {
        private final RequestHeaderSnapshot snapshot;
        private final Map<String, Object> attributes = new HashMap<>();

        private HeaderOnlyHttpServletRequestInvocationHandler(RequestHeaderSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                return invokeObjectMethod(proxy, methodName, args);
            }
            if ("getHeader".equals(methodName)) {
                return firstHeader(args == null ? null : (String) args[0]);
            }
            if ("getHeaders".equals(methodName)) {
                return Collections.enumeration(headers(args == null ? null : (String) args[0]));
            }
            if ("getHeaderNames".equals(methodName)) {
                return Collections.enumeration(snapshot.headers.keySet());
            }
            if ("getIntHeader".equals(methodName)) {
                String value = firstHeader(args == null ? null : (String) args[0]);
                return hasText(value) ? Integer.parseInt(value) : -1;
            }
            if ("getDateHeader".equals(methodName)) {
                return parseDateHeader(firstHeader(args == null ? null : (String) args[0]));
            }
            if ("getAttribute".equals(methodName)) {
                return attributes.get(args == null ? null : args[0]);
            }
            if ("setAttribute".equals(methodName)) {
                if (args != null && args.length > 1) {
                    attributes.put((String) args[0], args[1]);
                }
                return null;
            }
            if ("removeAttribute".equals(methodName)) {
                if (args != null && args.length > 0) {
                    attributes.remove(args[0]);
                }
                return null;
            }
            if ("getAttributeNames".equals(methodName)) {
                return Collections.enumeration(attributes.keySet());
            }
            if ("getMethod".equals(methodName)) {
                return defaultString(snapshot.method, "POST");
            }
            if ("getProtocol".equals(methodName)) {
                return defaultString(snapshot.protocol, "HTTP/1.1");
            }
            if ("getScheme".equals(methodName)) {
                return defaultString(snapshot.scheme, "http");
            }
            if ("getServerName".equals(methodName)) {
                return defaultString(snapshot.serverName, "localhost");
            }
            if ("getServerPort".equals(methodName)) {
                return snapshot.serverPort > 0 ? snapshot.serverPort : 80;
            }
            if ("getRequestURI".equals(methodName)) {
                return defaultString(snapshot.requestUri, "/");
            }
            if ("getRequestURL".equals(methodName)) {
                return new StringBuffer(buildRequestUrl());
            }
            if ("getContextPath".equals(methodName)) {
                return defaultString(snapshot.contextPath, "");
            }
            if ("getServletPath".equals(methodName)) {
                return defaultString(snapshot.servletPath, "");
            }
            if ("getPathInfo".equals(methodName)) {
                return snapshot.pathInfo;
            }
            if ("getQueryString".equals(methodName)) {
                return snapshot.queryString;
            }
            if ("getRemoteAddr".equals(methodName)) {
                return defaultString(snapshot.remoteAddr, "127.0.0.1");
            }
            if ("getRemoteHost".equals(methodName)) {
                return defaultString(snapshot.remoteHost, "localhost");
            }
            if ("getCharacterEncoding".equals(methodName)) {
                return defaultString(snapshot.characterEncoding, "UTF-8");
            }
            if ("getLocale".equals(methodName)) {
                return snapshot.locale == null ? Locale.getDefault() : snapshot.locale;
            }
            if ("getLocales".equals(methodName)) {
                return Collections.enumeration(Collections.singletonList(snapshot.locale == null ? Locale.getDefault() : snapshot.locale));
            }
            if ("isSecure".equals(methodName)) {
                return snapshot.secure;
            }
            if ("getDispatcherType".equals(methodName)) {
                return DispatcherType.REQUEST;
            }
            if ("getParameterMap".equals(methodName)) {
                return Collections.emptyMap();
            }
            if ("getParameterNames".equals(methodName)) {
                return Collections.enumeration(Collections.<String>emptyList());
            }
            if ("getParameterValues".equals(methodName)) {
                return null;
            }
            if ("getContentLength".equals(methodName)) {
                return 0;
            }
            if ("getContentLengthLong".equals(methodName)) {
                return 0L;
            }
            return defaultValue(method.getReturnType());
        }

        private Object invokeObjectMethod(Object proxy, String methodName, Object[] args) {
            if ("toString".equals(methodName)) {
                return "HeaderOnlyHttpServletRequest" + snapshot.headers;
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
                return proxy == (args == null ? null : args[0]);
            }
            return null;
        }

        private List<String> headers(String headerName) {
            if (!hasText(headerName)) {
                return Collections.emptyList();
            }
            List<String> values = snapshot.headers.get(headerName);
            return values == null ? Collections.<String>emptyList() : values;
        }

        private String firstHeader(String headerName) {
            List<String> values = headers(headerName);
            return values.isEmpty() ? null : values.get(0);
        }

        private long parseDateHeader(String value) {
            if (!hasText(value)) {
                return -1L;
            }
            try {
                return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
            } catch (Exception ignored) {
                return -1L;
            }
        }

        private String buildRequestUrl() {
            StringBuilder builder = new StringBuilder();
            builder.append(defaultString(snapshot.scheme, "http"))
                    .append("://")
                    .append(defaultString(snapshot.serverName, "localhost"));
            int port = snapshot.serverPort > 0 ? snapshot.serverPort : 80;
            if (!("http".equalsIgnoreCase(snapshot.scheme) && port == 80)
                    && !("https".equalsIgnoreCase(snapshot.scheme) && port == 443)) {
                builder.append(':').append(port);
            }
            builder.append(defaultString(snapshot.requestUri, "/"));
            return builder.toString();
        }

        private Object defaultValue(Class<?> returnType) {
            if (returnType == null || returnType == Void.TYPE) {
                return null;
            }
            if (returnType == Boolean.TYPE) {
                return false;
            }
            if (returnType == Integer.TYPE) {
                return 0;
            }
            if (returnType == Long.TYPE) {
                return 0L;
            }
            if (returnType == Double.TYPE) {
                return 0D;
            }
            if (returnType == Float.TYPE) {
                return 0F;
            }
            if (returnType == Short.TYPE) {
                return (short) 0;
            }
            if (returnType == Byte.TYPE) {
                return (byte) 0;
            }
            if (returnType == Character.TYPE) {
                return (char) 0;
            }
            return null;
        }

        private String defaultString(String value, String defaultValue) {
            return hasText(value) ? value : defaultValue;
        }

        private boolean hasText(String value) {
            return value != null && value.trim().length() > 0;
        }
    }
}

