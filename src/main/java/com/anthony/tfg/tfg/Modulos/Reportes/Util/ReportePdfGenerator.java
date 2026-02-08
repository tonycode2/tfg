package com.anthony.tfg.tfg.Modulos.Reportes.Util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

/**
 * Utility to render Thymeleaf templates into PDF using Flying Saucer.
 */
@Component
public class ReportePdfGenerator {

    private final SpringTemplateEngine reportesTemplateEngine;

    public ReportePdfGenerator(SpringTemplateEngine reportesTemplateEngine) {
        this.reportesTemplateEngine = reportesTemplateEngine;
    }

    /**
     * Render a template located under `templates/reportes/{templateName}.html` into a PDF.
     * @param templateName template file name without path or suffix, e.g. "reporte-planilla"
     * @param variables template variables map
     * @return PDF as byte[]
     * @throws Exception on rendering errors
     */
    public byte[] generarPdf(String templateName, Map<String, Object> variables) throws Exception {
        Context ctx = new Context();
        if (variables != null) ctx.setVariables(variables);

        String html = reportesTemplateEngine.process("reportes/" + templateName, ctx);
        // Inline CSS and images to make the PDF rendering self-contained
        html = inlineCss(html);
        html = inlineImages(html);

        // Base URL for resolving any remaining relative resources
        String baseUrl = new ClassPathResource("static/").getURL().toString();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html, baseUrl);
        renderer.layout();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            renderer.createPDF(baos);
            baos.flush();
            return baos.toByteArray();
        }
    }

    private String inlineCss(String html) {
        try {
            ClassPathResource cssRes = new ClassPathResource("static/css/reportes.css");
            if (cssRes.exists()) {
                try (InputStream is = cssRes.getInputStream(); Scanner s = new Scanner(is, StandardCharsets.UTF_8.name())) {
                    String css = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                    // Inject CSS into head before </head>
                    return html.replaceFirst("(?i)</head>", "<style>" + css + "</style></head>");
                }
            }
        } catch (Exception e) {
            // ignore and continue without inlined css
        }
        return html;
    }

    private String inlineImages(String html) {
        try {
            // Common logo path used in templates: /logo.png or /static/logo.png or logo.png
            ClassPathResource logo = new ClassPathResource("static/logo.png");
            if (logo.exists()) {
                try (InputStream is = logo.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String dataUri = "data:image/png;base64," + base64;
                    html = html.replaceAll("(?i)src=\"/logo.png\"", "src=\"" + dataUri + "\"");
                    html = html.replaceAll("(?i)src=\"logo.png\"", "src=\"" + dataUri + "\"");
                    html = html.replaceAll("(?i)src=\"/static/logo.png\"", "src=\"" + dataUri + "\"");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return html;
    }
}
