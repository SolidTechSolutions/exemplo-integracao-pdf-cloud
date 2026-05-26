package com.solidsign.examples.control;

import com.solidsign.examples.service.PdfCloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import org.springframework.web.multipart.MultipartFile;

/**
 * [EN]    REST controller that triggers PAdES (PDF) signing using cloud HSM credentials.
 *         Scans an input folder for PDF files and signs all of them.
 *
 * [PT-BR] Controller REST que dispara a assinatura PAdES (PDF) usando credenciais de HSM em nuvem.
 *         Varre uma pasta de entrada por arquivos PDF e assina todos eles.
 *
 * [ES]    Controller REST que activa la firma PAdES (PDF) usando credenciales de HSM en la nube.
 *         Escanea una carpeta de entrada por archivos PDF y firma todos ellos.
 */
@RestController
@RequestMapping("/api/pdf")
public class PdfCloudController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfCloudController.class);

    @Autowired
    private PdfCloudService service;

    // [EN]    Path to the folder containing PDF files to sign
    // [PT-BR] Caminho para a pasta contendo os arquivos PDF a assinar
    // [ES]    Ruta a la carpeta que contiene los archivos PDF a firmar
    @Value("${solidsign.batch.input-path}")
    private String inputPath;

    // [EN]    Path to the folder where the signed ZIP will be written
    // [PT-BR] Caminho para a pasta onde o ZIP assinado será gravado
    // [ES]    Ruta a la carpeta donde se escribirá el ZIP firmado
    @Value("${solidsign.batch.output-path}")
    private String outputPath;

    /**
     * [EN]    Signs all PDF files inside the configured input folder using PAdES Cloud HSM.
     *         Returns the path to the output ZIP on success.
     *
     * [PT-BR] Assina todos os PDFs da pasta de entrada configurada usando PAdES Cloud HSM.
     *         Retorna o caminho do ZIP de saída em caso de sucesso.
     *
     * [ES]    Firma todos los archivos PDF de la carpeta de entrada configurada con PAdES Cloud HSM.
     *         Devuelve la ruta del ZIP de salida en caso de éxito.
     */
    @PostMapping("/sign-cloud")
    public ResponseEntity<String> signCloudFolder() throws IOException {
        File folder = new File(inputPath);
        if (!folder.exists() || !folder.isDirectory()) {
            // [EN]    Configured input path is invalid or not a directory
            // [PT-BR] O caminho de entrada configurado é inválido ou não é um diretório
            // [ES]    La ruta de entrada configurada es inválida o no es un directorio
            return ResponseEntity.badRequest().body("Invalid input path: " + inputPath);
        }
        File[] files = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".pdf"));
        if (files == null || files.length == 0) {
            // [EN]    No PDF files found in the input folder
            // [PT-BR] Nenhum arquivo PDF encontrado na pasta de entrada
            // [ES]    No se encontraron archivos PDF en la carpeta de entrada
            return ResponseEntity.ok("No PDF files found in " + inputPath);
        }
        List<File> pdfList = Arrays.asList(files);
        LOGGER.info("Found {} PDF(s) for cloud signing.", pdfList.size());
        String result = service.signWithCloudCertificate(pdfList, outputPath);
        return result != null
                ? ResponseEntity.ok("Signed! ZIP at: " + result)
                : ResponseEntity.internalServerError().body("Failed. Check logs.");
    }

    /**
     * [EN]    Form signing endpoint for React — PAdES cloud HSM. properties ignored.
     *         signatureImages is optional.
     * [PT-BR] Endpoint de formulário para React — PAdES HSM em nuvem. properties ignorado.
     *         signatureImages é opcional.
     * [ES]    Endpoint de formulario para React — PAdES HSM en nube. properties ignorado.
     *         signatureImages es opcional.
     */
    @CrossOrigin
    @PostMapping("/sign/form")
    public ResponseEntity<byte[]> signForm(
            @RequestPart("document")                                              MultipartFile[]  documents,
            @RequestPart("authorization")                                         String           authorization,
            @RequestPart("baseUrl")                                               String           baseUrl,
            @RequestPart("cloudCredentials")                                      String           cloudCredentials,
            @RequestPart(value = "signatureImage",        required = false)       MultipartFile[]  signatureImages,
            @RequestPart(value = "profile",               required = false)       String           profile,
            @RequestPart(value = "hashAlgorithm",         required = false)       String           hashAlgorithm,
            @RequestPart(value = "sigFieldMeasurementUnit", required = false)     String           sigFieldMeasurementUnit,
            @RequestPart(value = "signatureFieldConfig",  required = false)       String           signatureFieldConfig,
            @RequestPart(value = "reason",                required = false)       String           reason,
            @RequestPart(value = "location",              required = false)       String           location,
            @RequestPart(value = "contact",               required = false)       String           contact,
            @RequestPart(value = "signatureFieldName",    required = false)       String           signatureFieldName,
            @RequestPart(value = "signatureTextConfig",   required = false)       String           signatureTextConfig,
            @RequestPart(value = "mdpPermissionLevel",    required = false)       String           mdpPermissionLevel,
            @RequestPart(value = "passwordsForDecryption", required = false)      String           passwordsForDecryption,
            @RequestPart(value = "documentInfoMetadata",  required = false)       String           documentInfoMetadata,
            @RequestPart(value = "signatureQrCodeConfig", required = false)       String           signatureQrCodeConfig
    ) throws IOException {
        List<File> tmpFiles = new ArrayList<>();
        List<File> tmpImgs  = new ArrayList<>();
        java.nio.file.Path tmpDir = java.nio.file.Files.createTempDirectory("solidsign-form-");
        try {
            for (MultipartFile mf : documents) {
                java.nio.file.Path p = tmpDir.resolve(mf.getOriginalFilename() != null ? mf.getOriginalFilename() : "doc.pdf");
                mf.transferTo(p);
                tmpFiles.add(p.toFile());
            }
            if (signatureImages != null) {
                for (MultipartFile img : signatureImages) {
                    java.nio.file.Path p = tmpDir.resolve(img.getOriginalFilename() != null ? img.getOriginalFilename() : "img");
                    img.transferTo(p);
                    tmpImgs.add(p.toFile());
                }
            }
            byte[] zip = service.signWithCloudForm(authorization, baseUrl, cloudCredentials,
                    profile, hashAlgorithm,
                    sigFieldMeasurementUnit, signatureFieldConfig,
                    reason, location, contact,
                    signatureFieldName, signatureTextConfig, mdpPermissionLevel,
                    passwordsForDecryption, documentInfoMetadata, signatureQrCodeConfig,
                    tmpFiles, tmpImgs);
            if (zip != null)
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType("application/zip"))
                        .header("Content-Disposition", "attachment; filename=\"signed_pdf.zip\"")
                        .body(zip);
            return ResponseEntity.internalServerError().build();
        } finally {
            tmpFiles.forEach(java.io.File::delete);
            tmpImgs.forEach(java.io.File::delete);
            tmpDir.toFile().delete();
        }
    }

}
