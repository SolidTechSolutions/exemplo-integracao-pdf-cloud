package com.solidsign.examples.service;

import com.solidsign.examples.response.SignResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;
import java.util.zip.*;

/**
 * [EN]    Service that signs PDF (PAdES) documents using a cloud HSM certificate.
 *         Calls the SolidSign API endpoint: POST /solidsign/dsig/pdf/sign-hsm-cloud
 *         Supports visual signature image and field configuration.
 *
 * [PT-BR] Serviço que assina documentos PDF (PAdES) usando um certificado HSM em nuvem.
 *         Chama o endpoint da API SolidSign: POST /solidsign/dsig/pdf/sign-hsm-cloud
 *         Suporta imagem de assinatura visual e configuração de campo.
 *
 * [ES]    Servicio que firma documentos PDF (PAdES) usando un certificado HSM en la nube.
 *         Llama al endpoint de la API SolidSign: POST /solidsign/dsig/pdf/sign-hsm-cloud
 *         Soporta imagen de firma visual y configuración de campo.
 */
@Service
public class PdfCloudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfCloudService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // [EN]    Base URL of the SolidSign API
    // [PT-BR] URL base da API SolidSign
    // [ES]    URL base de la API SolidSign
    @Value("${solidsign.api.base-url}")
    private String baseUrl;

    // [EN]    Authorization header value (Bearer token)
    // [PT-BR] Valor do header Authorization (token Bearer)
    // [ES]    Valor del header Authorization (token Bearer)
    @Value("${solidsign.api.authorization}")
    private String authorization;

    // [EN]    Signature profile (e.g. ADRB, ADRT, ADRC, ADRA)
    // [PT-BR] Perfil de assinatura (ex: ADRB, ADRT, ADRC, ADRA)
    // [ES]    Perfil de firma (p.ej. ADRB, ADRT, ADRC, ADRA)
    @Value("${solidsign.sig.profile}")
    private String profile;

    // [EN]    Hash algorithm (SHA256, SHA384, SHA512)
    // [PT-BR] Algoritmo de hash (SHA256, SHA384, SHA512)
    // [ES]    Algoritmo de hash (SHA256, SHA384, SHA512)
    @Value("${solidsign.sig.hashAlgorithm}")
    private String hashAlgorithm;

    // [EN]    Measurement unit for visual signature field (PIXELS or CENTIMETERS)
    // [PT-BR] Unidade de medida para o campo de assinatura visual (PIXELS ou CENTIMETERS)
    // [ES]    Unidad de medida para el campo de firma visual (PIXELS o CENTIMETERS)
    @Value("${solidsign.sig.sigFieldMeasurementUnit}")
    private String sigFieldMeasurementUnit;

    // [EN]    JSON array describing the visual signature field(s)
    // [PT-BR] Array JSON descrevendo o(s) campo(s) de assinatura visual
    // [ES]    Array JSON que describe el/los campo(s) de firma visual
    @Value("${solidsign.sig.signatureFieldConfig}")
    private String signatureFieldConfig;

    // [EN]    Comma-separated list of signature image file paths (one per document or repeated)
    // [PT-BR] Lista de caminhos de imagem separados por vírgula (um por documento ou repetido)
    // [ES]    Lista de rutas de imagen de firma separadas por coma (una por documento o repetida)
    @Value("${solidsign.sig.signatureImagePaths}")
    private List<String> signatureImagePaths;

    // [EN]    Cloud HSM credentials as JSON (uuidCert, hsmToken, hsmServiceUrl)
    // [PT-BR] Credenciais do HSM em nuvem como JSON (uuidCert, hsmToken, hsmServiceUrl)
    // [ES]    Credenciales del HSM en la nube como JSON (uuidCert, hsmToken, hsmServiceUrl)
    @Value("${solidsign.cloud.credentials}")
    private String cloudCredentials;

    /**
     * [EN]    Signs the given PDF files using a cloud HSM and returns the path of the output ZIP.
     * [PT-BR] Assina os arquivos PDF informados usando um HSM em nuvem e retorna o caminho do ZIP de saída.
     * [ES]    Firma los archivos PDF dados con un HSM en la nube y devuelve la ruta del ZIP de salida.
     *
     * @param pdfFiles
     *   [EN]    list of PDF files to sign
     *   [PT-BR] lista de arquivos PDF a assinar
     *   [ES]    lista de archivos PDF a firmar
     * @param outputDir
     *   [EN]    destination folder for the output ZIP
     *   [PT-BR] pasta de destino para o ZIP de saída
     *   [ES]    carpeta de destino para el ZIP de salida
     * @return
     *   [EN]    path of the generated ZIP, or null on error
     *   [PT-BR] caminho do ZIP gerado, ou null em caso de erro
     *   [ES]    ruta del ZIP generado, o null en caso de error
     */
    public String signWithCloudCertificate(List<File> pdfFiles, String outputDir) throws IOException {
        LOGGER.info("Starting PAdES Cloud signing for {} document(s).", pdfFiles.size());

        // [EN]    Build the full endpoint URL from the base URL
        // [PT-BR] Constrói a URL completa do endpoint a partir da URL base
        // [ES]    Construye la URL completa del endpoint a partir de la URL base
        String url = baseUrl + "/solidsign/dsig/pdf/sign-hsm-cloud";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", authorization);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // [EN]    Attach each PDF indexed as document[0], document[1], ...
        // [PT-BR] Anexa cada PDF indexado como document[0], document[1], ...
        // [ES]    Adjunta cada PDF indexado como document[0], document[1], ...
        for (int i = 0; i < pdfFiles.size(); i++) {
            body.add("document[" + i + "]", new FileSystemResource(pdfFiles.get(i)));
        }

        // [EN]    Cloud HSM credentials
        // [PT-BR] Credenciais do HSM em nuvem
        // [ES]    Credenciales del HSM en la nube
        body.add("cloudCredentials", cloudCredentials);

        // [EN]    Signature parameters
        // [PT-BR] Parâmetros de assinatura
        // [ES]    Parámetros de firma
        body.add("profile",                 profile);
        body.add("hashAlgorithm",           hashAlgorithm);
        body.add("sigFieldMeasurementUnit", sigFieldMeasurementUnit);
        body.add("signatureFieldConfig",    signatureFieldConfig);

        // [EN]    Optional visual signature images (one per document, indexed)
        // [PT-BR] Imagens de assinatura visual opcionais (uma por documento, indexadas)
        // [ES]    Imágenes de firma visual opcionales (una por documento, indexadas)
        if (signatureImagePaths != null) {
            for (int i = 0; i < signatureImagePaths.size(); i++) {
                File imgFile = new File(signatureImagePaths.get(i).trim());
                if (imgFile.exists()) {
                    body.add("signatureImage[" + i + "]", new FileSystemResource(imgFile));
                }
            }
        }

        try {
            ResponseEntity<SignResponse> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), SignResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                byte[] zip = downloadAndZip(response.getBody(), pdfFiles);
                new File(outputDir).mkdirs();
                String out = outputDir + "/signed_pdf_cloud_" + System.currentTimeMillis() + ".zip";
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(zip);
                }
                LOGGER.info("PAdES Cloud signing complete. Output: {}", out);
                return out;
            }
        } catch (HttpStatusCodeException e) {
            LOGGER.error("SolidSign API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during PAdES Cloud signing: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * [EN]    Downloads each signed document from the SolidSign response links and packages them into a ZIP.
     * [PT-BR] Baixa cada documento assinado dos links da resposta SolidSign e os empacota em um ZIP.
     * [ES]    Descarga cada documento firmado de los enlaces de respuesta SolidSign y los empaqueta en un ZIP.
     */
    private byte[] downloadAndZip(SignResponse resp, List<File> originals) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorization);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            for (int i = 0; i < resp.documents.size(); i++) {
                String downloadUrl = resp.documents.get(i).links.stream()
                        .filter(l -> "self".equals(l.rel))
                        .findFirst()
                        .map(l -> l.href)
                        .orElse(null);
                if (downloadUrl == null) continue;
                ResponseEntity<byte[]> r = restTemplate.exchange(
                        downloadUrl, HttpMethod.GET, entity, byte[].class);
                if (r.getStatusCode() == HttpStatus.OK) {
                    zos.putNextEntry(new ZipEntry("signed_" + originals.get(i).getName()));
                    zos.write(r.getBody());
                    zos.closeEntry();
                }
            }
        }
        return baos.toByteArray();
    }
}
