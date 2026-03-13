package com.anthony.tfg.tfg.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DebugMultipartLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(DebugMultipartLoggingFilter.class); // Using INFO level to ensure visibility in logs

    /** 
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("/api/incapacidades".equals(uri) && "POST".equalsIgnoreCase(method)) {
            try {
                Map<String, String[]> params = request.getParameterMap();
                String paramsLog = params.entrySet().stream()
                        .map(e -> e.getKey() + "=" + Arrays.toString(e.getValue()))
                        .collect(Collectors.joining(", "));
                logger.info("[DEBUG-INC] Request parameters: {}", paramsLog);

                // Avoid calling Servlet API getParts() directly (it may trigger container parsing and
                // interfere with Spring's MultipartResolver / data binding). Instead, if Spring has
                // already wrapped the request as a MultipartHttpServletRequest, enumerate files safely.
                try {
                    if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest multiReq) {
                        String partsLog = multiReq.getMultiFileMap().keySet().stream()
                                .map(name -> name + "(files=" + multiReq.getMultiFileMap().get(name).size() + ")")
                                .collect(Collectors.joining(", "));
                        logger.info("[DEBUG-INC] Multipart parts (files only): {}", partsLog);
                    } else {
                        logger.info("[DEBUG-INC] Skipping parts enumeration to avoid interfering with multipart processing");
                    }
                } catch (Exception e) {
                    // Not fatal - log and continue
                    logger.info("[DEBUG-INC] Could not enumerate multipart parts: {}", e.getMessage());
                }
            } catch (Exception e) {
                logger.warn("[DEBUG-INC] Error while logging multipart request: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
