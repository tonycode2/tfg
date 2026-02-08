package com.anthony.tfg.tfg.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Configuración de Thymeleaf específica para generación de reportes PDF.
 * Registra un template resolver adicional para las plantillas de reportes.
 */
@Configuration
public class ThymeleafReportesConfig {

    @Bean
    public SpringResourceTemplateResolver reportesTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        resolver.setOrder(1);
        resolver.setResolvablePatterns(java.util.Set.of("reportes/*"));
        return resolver;
    }

    @Bean
    public SpringTemplateEngine reportesTemplateEngine(SpringResourceTemplateResolver reportesTemplateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(reportesTemplateResolver);
        engine.setEnableSpringELCompiler(true);
        return engine;
    }
}
