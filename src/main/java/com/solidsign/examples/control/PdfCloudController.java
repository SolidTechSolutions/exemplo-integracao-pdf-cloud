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
}
