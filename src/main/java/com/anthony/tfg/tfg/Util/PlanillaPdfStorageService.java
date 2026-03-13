package com.anthony.tfg.tfg.Util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.anthony.tfg.tfg.Exceptions.BadRequestException;

@Service
public class PlanillaPdfStorageService {

    private final Path fileStorageLocation;

    /**
     * Inicializa el servicio con sus dependencias principales.
     * @param uploadDir parametro de entrada de la operacion.
     */
    public PlanillaPdfStorageService(@Value("${files.planillas-dir:uploads/planillas}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new BadRequestException("No se pudo crear la carpeta de almacenamiento de PDFs: " + ex.getMessage());
        }
    }

    /**
     * Gestiona el almacenamiento y la recuperacion de archivos.
     * @param file parametro de entrada de la operacion.
     * @param detalleId parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public String storePdf(MultipartFile file, Long detalleId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo PDF es requerido");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = ".pdf";
        int dot = originalFileName.lastIndexOf('.');
        if (dot > -1) {
            extension = originalFileName.substring(dot);
        }

        if (!".pdf".equalsIgnoreCase(extension)) {
            throw new BadRequestException("El archivo debe ser un PDF");
        }

        String fileName = "planilla-" + detalleId + extension.toLowerCase();

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new BadRequestException("No se pudo almacenar el PDF: " + ex.getMessage());
        }
    }

    /**
     * Gestiona el almacenamiento y la recuperacion de archivos.
     * @param fileName parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BadRequestException("Archivo no encontrado: " + fileName);
        } catch (MalformedURLException ex) {
            throw new BadRequestException("Archivo no encontrado: " + fileName);
        }
    }

    /**
     * Elimina un registro por su identificador.
     * @param fileName parametro de entrada de la operacion.
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // No interrumpir el flujo por fallo al eliminar; solo registrar
        }
    }
}
