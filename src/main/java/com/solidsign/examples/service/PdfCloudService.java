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

    // [EN]    Reason for signing — displayed in the PDF signature properties
    // [PT-BR] Motivo da assinatura — exibido nas propriedades da assinatura PDF
    // [ES]    Motivo de la firma — se muestra en las propiedades de la firma PDF
    @Value("${solidsign.sig.reason}")
    private String reason;

    // [EN]    Signing location — displayed in the PDF signature properties
    // [PT-BR] Local da assinatura — exibido nas propriedades da assinatura PDF
    // [ES]    Lugar de firma — se muestra en las propiedades de la firma PDF
    @Value("${solidsign.sig.location}")
    private String location;

    // [EN]    Contact information of the signer — displayed in the PDF signature properties
    // [PT-BR] Informações de contato do assinante — exibido nas propriedades da assinatura PDF
    // [ES]    Información de contacto del firmante — se muestra en las propiedades de la firma PDF
    @Value("${solidsign.sig.contact}")
    private String contact;

    // [EN]    Optional: name of a pre-existing signature field in the PDF to target
    // [PT-BR] Opcional: nome de um campo de assinatura pré-existente no PDF a utilizar
    // [ES]    Opcional: nombre de un campo de firma preexistente en el PDF
    // @Value("${solidsign.sig.signatureFieldName:}")
    // private String signatureFieldName;

    // [EN]    Optional: text overlays as JSON array (pageNumber, coordinateX, coordinateY, text, fontSize, textColor)
    // [PT-BR] Opcional: sobreposições de texto como array JSON (pageNumber, coordinateX, coordinateY, text, fontSize, textColor)
    // [ES]    Opcional: superposiciones de texto como array JSON (pageNumber, coordinateX, coordinateY, text, fontSize, textColor)
    // @Value("${solidsign.sig.signatureTextConfig:}")
    // private String signatureTextConfig;

    // [EN]    Optional: MDP permission level for certification signature (1=no changes, 2=forms, 3=annotations)
    // [PT-BR] Opcional: nível de permissão MDP para assinatura de certificação (1=sem alterações, 2=formulários, 3=anotações)
    // [ES]    Opcional: nivel de permiso MDP para firma de certificación (1=sin cambios, 2=formularios, 3=anotaciones)
    // @Value("${solidsign.sig.mdpPermissionLevel:}")
    // private String mdpPermissionLevel;

    // [EN]    Optional: JSON array of per-document open passwords for encrypted PDFs (e.g. ["pwd1","pwd2",null])
    // [PT-BR] Opcional: array JSON de senhas por documento para PDFs criptografados (ex: ["senha1","senha2",null])
    // [ES]    Opcional: array JSON de contraseñas por documento para PDFs cifrados (p.ej. ["pwd1","pwd2",null])
    // @Value("${solidsign.sig.passwordsForDecryption:}")
    // private String passwordsForDecryption;

    // [EN]    Optional: JSON map of PDF document metadata (title, author, subject, keywords, creator)
    // [PT-BR] Opcional: mapa JSON de metadados do documento PDF (title, author, subject, keywords, creator)
    // [ES]    Opcional: mapa JSON de metadatos del documento PDF (title, author, subject, keywords, creator)
    // @Value("${solidsign.sig.documentInfoMetadata:}")
    // private String documentInfoMetadata;

    // [EN]    Optional: QR code config as JSON array — CANNOT be used together with signatureFieldConfig
    // [PT-BR] Opcional: configuração de QR code como array JSON — NÃO pode ser usado junto com signatureFieldConfig
    // [ES]    Opcional: configuración de código QR como array JSON — NO puede usarse junto con signatureFieldConfig
    // @Value("${solidsign.sig.signatureQrCodeConfig:}")
    // private String signatureQrCodeConfig;

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
        body.add("reason",                  reason);
        body.add("location",                location);
        body.add("contact",                 contact);

        // [EN]    Optional parameters — uncomment to use
        // [PT-BR] Parâmetros opcionais — descomente para usar
        // [ES]    Parámetros opcionales — descomente para usar
        // body.add("signatureFieldName",     signatureFieldName);
        // body.add("signatureTextConfig",    signatureTextConfig);
        // body.add("mdpPermissionLevel",     mdpPermissionLevel);
        // body.add("passwordsForDecryption", passwordsForDecryption);
        // body.add("documentInfoMetadata",   documentInfoMetadata);
        // body.add("signatureQrCodeConfig",  signatureQrCodeConfig);

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

    // ─── Form endpoint (all params from request, properties ignored) ──────────

    /**
     * [EN]    Signs PDF documents via PAdES cloud HSM with all parameters supplied by the caller.
     * [PT-BR] Assina PDFs via PAdES HSM em nuvem com todos os parâmetros fornecidos pelo chamador.
     * [ES]    Firma PDFs vía PAdES HSM en la nube con todos los parámetros suministrados por el llamador.
     *
     * @return ZIP bytes with signed documents, or null on error
     */
    public byte[] signWithCloudForm(String auth, String apiBaseUrl, String cloudCredentials,
                                     String profile, String hashAlgorithm,
                                     String sigFieldMeasurementUnit, String signatureFieldConfig,
                                     String reason, String location, String contact,
                                     String signatureFieldName, String signatureTextConfig,
                                     String mdpPermissionLevel, String passwordsForDecryption,
                                     String documentInfoMetadata, String signatureQrCodeConfig,
                                     List<File> pdfFiles, List<File> signatureImages) throws IOException {
        LOGGER.info("PAdES Cloud form signing for {} PDF(s).", pdfFiles.size());
        String signUrl = apiBaseUrl + "/solidsign/dsig/pdf/sign-hsm-cloud";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", auth);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (int i = 0; i < pdfFiles.size(); i++) body.add("document[" + i + "]", new FileSystemResource(pdfFiles.get(i)));
        body.add("cloudCredentials", cloudCredentials);
        if (profile != null && !profile.isBlank())                     body.add("profile",                 profile);
        if (hashAlgorithm != null && !hashAlgorithm.isBlank())         body.add("hashAlgorithm",           hashAlgorithm);
        if (sigFieldMeasurementUnit != null && !sigFieldMeasurementUnit.isBlank()) body.add("sigFieldMeasurementUnit", sigFieldMeasurementUnit);
        if (signatureFieldConfig != null && !signatureFieldConfig.isBlank()) body.add("signatureFieldConfig", signatureFieldConfig);
        if (reason != null && !reason.isBlank())                       body.add("reason",                  reason);
        if (location != null && !location.isBlank())                   body.add("location",                location);
        if (contact != null && !contact.isBlank())                     body.add("contact",                 contact);
        if (signatureFieldName != null && !signatureFieldName.isBlank()) body.add("signatureFieldName",    signatureFieldName);
        if (signatureTextConfig != null && !signatureTextConfig.isBlank()) body.add("signatureTextConfig", signatureTextConfig);
        if (mdpPermissionLevel != null && !mdpPermissionLevel.isBlank()) body.add("mdpPermissionLevel",    mdpPermissionLevel);
        if (passwordsForDecryption != null && !passwordsForDecryption.isBlank()) body.add("passwordsForDecryption", passwordsForDecryption);
        if (documentInfoMetadata != null && !documentInfoMetadata.isBlank()) body.add("documentInfoMetadata", documentInfoMetadata);
        if (signatureQrCodeConfig != null && !signatureQrCodeConfig.isBlank()) body.add("signatureQrCodeConfig", signatureQrCodeConfig);
        if (signatureImages != null) {
            for (int i = 0; i < signatureImages.size(); i++)
                body.add("signatureImage[" + i + "]", new FileSystemResource(signatureImages.get(i)));
        }
        try {
            ResponseEntity<SignResponse> resp = restTemplate.postForEntity(
                    signUrl, new HttpEntity<>(body, headers), SignResponse.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                SignResponse signResp = resp.getBody();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                    HttpHeaders dh = new HttpHeaders();
                    dh.set("Authorization", auth);
                    HttpEntity<Void> de = new HttpEntity<>(dh);
                    for (int i = 0; i < signResp.documents.size(); i++) {
                        String dlUrl = signResp.documents.get(i).links.stream()
                                .filter(l -> "self".equals(l.rel)).findFirst()
                                .map(l -> l.href).orElse(null);
                        if (dlUrl == null) continue;
                        ResponseEntity<byte[]> r = restTemplate.exchange(
                                dlUrl, HttpMethod.GET, de, byte[].class);
                        if (r.getStatusCode() == HttpStatus.OK) {
                            zos.putNextEntry(new ZipEntry("signed_" + pdfFiles.get(i).getName()));
                            zos.write(r.getBody());
                            zos.closeEntry();
                        }
                    }
                }
                return baos.toByteArray();
            }
        } catch (HttpStatusCodeException e) {
            LOGGER.error("SolidSign API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            LOGGER.error("Unexpected error in PAdES Cloud form signing: {}", e.getMessage(), e);
        }
        return null;
    }

}
