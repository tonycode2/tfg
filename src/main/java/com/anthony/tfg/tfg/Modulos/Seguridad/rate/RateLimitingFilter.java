package com.anthony.tfg.tfg.Modulos.Seguridad.rate;

import com.anthony.tfg.tfg.DTOs.Respuesta.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/auth/login";

    private final ObjectMapper objectMapper;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    @Value("${rate-limit.global.max-requests:120}")
    private int globalMaxRequests;

    @Value("${rate-limit.global.window-seconds:60}")
    private int globalWindowSeconds;

    @Value("${rate-limit.login.max-requests:10}")
    private int loginMaxRequests;

    @Value("${rate-limit.login.window-seconds:60}")
    private int loginWindowSeconds;

    /** 
     * @param request
     * @return boolean
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /** 
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long now = System.currentTimeMillis();
        String path = request.getServletPath();
        String ip = extractClientIp(request);

        boolean isLoginRequest = LOGIN_PATH.equals(path);
        int maxRequests = isLoginRequest ? loginMaxRequests : globalMaxRequests;
        int windowSeconds = isLoginRequest ? loginWindowSeconds : globalWindowSeconds;
        long windowMillis = windowSeconds * 1000L;

        String bucketType = isLoginRequest ? "LOGIN" : "GLOBAL";
        String key = bucketType + ':' + ip;
        WindowCounter windowCounter = counters.computeIfAbsent(key, ignored -> new WindowCounter());

        if (!windowCounter.allowRequest(now, maxRequests, windowMillis)) {
            writeRateLimitError(request, response, windowSeconds);
            return;
        }

        cleanupStaleCounters(now);
        filterChain.doFilter(request, response);
    }

    /** 
     * @param request
     * @param response
     * @param retryAfterSeconds
     * @throws IOException
     */
    private void writeRateLimitError(HttpServletRequest request, HttpServletResponse response, int retryAfterSeconds)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Demasiadas solicitudes. Intente nuevamente en unos segundos",
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /** 
     * @param request
     * @return String
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    /** 
     * @param now
     */
    private void cleanupStaleCounters(long now) {
        if (requestCounter.incrementAndGet() % 200 != 0) {
            return;
        }

        long maxWindow = Math.max(globalWindowSeconds, loginWindowSeconds) * 1000L;
        counters.entrySet().removeIf(entry -> now - entry.getValue().getLastSeen() > (maxWindow * 2));
    }

    private static final class WindowCounter {
        private final ArrayDeque<Long> requestTimestamps = new ArrayDeque<>();
        private volatile long lastSeen;

        synchronized boolean allowRequest(long now, int maxRequests, long windowMillis) {
            long threshold = now - windowMillis;
            while (!requestTimestamps.isEmpty() && requestTimestamps.peekFirst() <= threshold) {
                requestTimestamps.pollFirst();
            }

            lastSeen = now;
            if (requestTimestamps.size() >= maxRequests) {
                return false;
            }

            requestTimestamps.addLast(now);
            return true;
        }

        long getLastSeen() {
            return lastSeen;
        }
    }
}