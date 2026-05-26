# 🇧🇷 SolidSign API - Exemplo de Assinatura PDF com Certificado em Nuvem (Batch Mode)

Este projeto demonstra a integração com a **SolidSign API** para realizar a assinatura digital PAdES de múltiplos documentos PDF em lote, utilizando um certificado armazenado em HSM/Nuvem (PSC). A chave privada permanece no HSM do provedor e nunca é transmitida.

## Estrutura do Projeto

* **Controller:** Atua como gatilho para escanear a pasta de entrada local e gerenciar o processo de assinatura PAdES com certificado em nuvem.
* **Service:** Orquestra a chamada para a API SolidSign, trata erros 400/500 e salva o arquivo ZIP com os PDFs assinados no armazenamento local.

## Configuração (application.properties)

| Atributo | Descrição | Exemplo / Valor |
| :--- | :--- | :--- |
| `solidsign.api.base-url` | URL base da SolidSign API (sem o caminho). | `https://solidsign.com.br` |
| `solidsign.api.authorization` | Token JWT de autorização (Bearer). | `Bearer eyJhbGciOiJIUzI1...` |
| `solidsign.batch.input-path` | Pasta local contendo os PDFs originais para assinatura. | `C:/Users/User/Desktop/input_pdfs` |
| `solidsign.batch.output-path` | Pasta onde o arquivo ZIP com os PDFs assinados será salvo. | `C:/Users/User/Desktop/signed_results` |
| `solidsign.cloud.credentials` | JSON com as credenciais do HSM em nuvem: `uuidCert`, `hsmToken` e `hsmServiceUrl`. | `{"uuidCert":"...","hsmToken":"...","hsmServiceUrl":"https://hsm.provedor.com"}` |
| `solidsign.sig.hashAlgorithm` | Algoritmo de hash criptográfico (SHA256, SHA384, SHA512). | `SHA256` |
| `solidsign.sig.profile` | Perfil/padrão da assinatura PAdES (Adobe/ICP-Brasil/ETSI). | `PDF_BASIC`, `ADRT`, `PADES_B`, etc. |
| `solidsign.sig.sigFieldMeasurementUnit` | Unidade de medida para as coordenadas do campo de assinatura. | `PIXELS` ou `MILLIMETERS` |
| `solidsign.sig.signatureFieldConfig` | JSON com coordenadas e dimensões do campo de assinatura visual. | `[{"pageNumber":1,"coordinateX":200,"coordinateY":380,"width":200,"height":80}]` |
| `solidsign.sig.signatureImagePaths` | Caminho(s) para a(s) imagem(ns) de carimbo de assinatura. | `C:/img/stamp.png` |
| `solidsign.sig.reason` | Motivo da assinatura (visível no painel de assinaturas do PDF). | `Assinatura digital` |
| `solidsign.sig.location` | Local geográfico onde o documento foi assinado. | `Brasil` |
| `solidsign.sig.contact` | Informações de contato do assinante. | `contato@empresa.com.br` |
| `solidsign.sig.signatureFieldName` *(opcional)* | Nome de um campo de assinatura pré-existente no PDF. | `SignatureField1` |
| `solidsign.sig.signatureTextConfig` *(opcional)* | JSON com texto personalizado exibido no campo de assinatura visual. | `[{"pageNumber":1,"coordinateX":200,"coordinateY":460,"text":"Assinado","fontSize":10,"textColor":"BLACK"}]` |
| `solidsign.sig.mdpPermissionLevel` *(opcional)* | Nível MDP para assinaturas de certificação (1=sem alterações, 2=formulários, 3=anotações). | `1` |
| `solidsign.sig.passwordsForDecryption` *(opcional)* | Array JSON de senhas para abrir PDFs criptografados. | `["senha1"]` |
| `solidsign.sig.documentInfoMetadata` *(opcional)* | JSON com metadados do documento PDF (título, autor, assunto, palavras-chave). | `{"title":"Contrato","author":"Empresa"}` |
| `solidsign.sig.signatureQrCodeConfig` *(opcional)* | JSON com configurações do QR Code a ser embutido no campo de assinatura visual. | `{"pageNumber":1,"coordinateX":10,"coordinateY":10,"width":60,"height":60}` |
| `solidsign.sig.policyVersion` *(opcional)* | OID da política de assinatura ICP-Brasil. | `2.16.76.1.7.1.11.1.3` |

## Stack
1. Java 17
2. SpringBoot 3.4.x+
3. Maven 3.x.x+
4. Logback (para logging dos erros)

## Como Executar

1. **Obter credenciais:** Solicite ao seu provedor HSM as credenciais `uuidCert`, `hsmToken` e `hsmServiceUrl`.
2. **Configurar:** Preencha `solidsign.cloud.credentials` com o JSON das credenciais e os demais parâmetros em `src/main/resources/application.properties`.
3. **Compilar:** `mvn clean install`
4. **Iniciar:** `mvn spring-boot:run`
5. **Testar:** Envie um POST para `http://localhost:8080/api/pdf/sign-cloud`. O sistema processará automaticamente todos os PDFs encontrados na pasta de entrada.

## Tratamento de Erros
O sistema intercepta erros **400 Bad Request** e **500 Internal Server Error** e loga o JSON detalhado da SolidSign para facilitar o debug de credenciais ou parâmetros inválidos.

---

# 🇬🇧 SolidSign API - PDF Cloud Certificate Signature Example (Batch Mode)

This project demonstrates the integration with the **SolidSign API** to perform PAdES digital signatures on multiple PDF documents in batch mode, using a certificate stored in an HSM/Cloud (PSC). The private key stays in the HSM provider and is never transmitted.

## Project Structure

* **Controller:** Acts as a trigger to scan the local input folder and manage the PAdES cloud signing process.
* **Service:** Orchestrates the SolidSign API calls, handles 400/500 errors, and saves the ZIP file with signed PDFs to local storage.

## Configuration (application.properties)

| Attribute | Description | Example / Value |
| :--- | :--- | :--- |
| `solidsign.api.base-url` | Base URL of the SolidSign API (without path). | `https://solidsign.com.br` |
| `solidsign.api.authorization` | Authorization JWT Token (Bearer). | `Bearer eyJhbGciOiJIUzI1...` |
| `solidsign.batch.input-path` | Local folder containing the original PDFs for signing. | `C:/Users/User/Desktop/input_pdfs` |
| `solidsign.batch.output-path` | Local folder where the signed ZIP file will be saved. | `C:/Users/User/Desktop/signed_results` |
| `solidsign.cloud.credentials` | JSON with HSM cloud credentials: `uuidCert`, `hsmToken` and `hsmServiceUrl`. | `{"uuidCert":"...","hsmToken":"...","hsmServiceUrl":"https://hsm.provider.com"}` |
| `solidsign.sig.hashAlgorithm` | Cryptographic hash algorithm (SHA256, SHA384, SHA512). | `SHA256` |
| `solidsign.sig.profile` | PAdES signature profile/standard (Adobe/ICP-Brasil/ETSI). | `PDF_BASIC`, `ADRT`, `PADES_B`, etc. |
| `solidsign.sig.sigFieldMeasurementUnit` | Measurement unit for signature field coordinates. | `PIXELS` or `MILLIMETERS` |
| `solidsign.sig.signatureFieldConfig` | JSON with visual signature field coordinates and dimensions. | `[{"pageNumber":1,"coordinateX":200,"coordinateY":380,"width":200,"height":80}]` |
| `solidsign.sig.signatureImagePaths` | Path(s) to the signature stamp image(s). | `C:/img/stamp.png` |
| `solidsign.sig.reason` | Reason for the signature (visible in the PDF signature panel). | `Digital signature` |
| `solidsign.sig.location` | Geographic location where the document was signed. | `Brazil` |
| `solidsign.sig.contact` | Signer contact information. | `contact@company.com` |
| `solidsign.sig.signatureFieldName` *(optional)* | Name of a pre-existing signature field in the PDF. | `SignatureField1` |
| `solidsign.sig.signatureTextConfig` *(optional)* | JSON with custom text displayed in the visual signature field. | `[{"pageNumber":1,"coordinateX":200,"coordinateY":460,"text":"Signed","fontSize":10,"textColor":"BLACK"}]` |
| `solidsign.sig.mdpPermissionLevel` *(optional)* | MDP level for certification signatures (1=no changes, 2=forms, 3=annotations). | `1` |
| `solidsign.sig.passwordsForDecryption` *(optional)* | JSON array of passwords to open encrypted PDFs. | `["password1"]` |
| `solidsign.sig.documentInfoMetadata` *(optional)* | JSON with PDF document metadata (title, author, subject, keywords). | `{"title":"Contract","author":"Company"}` |
| `solidsign.sig.signatureQrCodeConfig` *(optional)* | JSON with QR Code configuration to embed in the visual signature field. | `{"pageNumber":1,"coordinateX":10,"coordinateY":10,"width":60,"height":60}` |
| `solidsign.sig.policyVersion` *(optional)* | ICP-Brasil signature policy OID. | `2.16.76.1.7.1.11.1.3` |

## Stack
1. Java 17
2. SpringBoot 3.4.x+
3. Maven 3.x.x+
4. Logback (for error logging)

## How to Run

1. **Get credentials:** Request `uuidCert`, `hsmToken` and `hsmServiceUrl` from your HSM provider.
2. **Configure:** Fill `solidsign.cloud.credentials` with the credentials JSON and set the remaining parameters in `src/main/resources/application.properties`.
3. **Build:** `mvn clean install`
4. **Start:** `mvn spring-boot:run`
5. **Test:** Send a POST request to `http://localhost:8080/api/pdf/sign-cloud`. The application will automatically process all PDFs found in the input folder.

## Error Handling
The system intercepts **400 Bad Request** and **500 Internal Server Error** responses and logs the detailed JSON from SolidSign to assist in debugging invalid credentials or parameters.

---

# 🇪🇸 SolidSign API - Ejemplo de Firma PDF con Certificado en la Nube (Modo Batch)

Este proyecto demuestra la integración con la **SolidSign API** para realizar la firma digital PAdES de múltiplos documentos PDF en lote, usando un certificado almacenado en HSM/Nube (PSC). La clave privada permanece en el HSM del proveedor y nunca se transmite.

## Estructura del Proyecto

* **Controller:** Actúa como disparador para escanear la carpeta local de entrada y gestionar el proceso de firma PAdES con certificado en la nube.
* **Service:** Orquestra las llamadas a la API SolidSign, gestiona errores 400/500 y guarda el archivo ZIP con los PDFs firmados en el almacenamiento local.

## Configuración (application.properties)

| Atributo | Descripción | Ejemplo / Valor |
| :--- | :--- | :--- |
| `solidsign.api.base-url` | URL base de la SolidSign API (sin la ruta). | `https://solidsign.com.br` |
| `solidsign.api.authorization` | Token JWT de autorización (Bearer). | `Bearer eyJhbGciOiJIUzI1...` |
| `solidsign.batch.input-path` | Carpeta local que contiene los PDF originales para firmar. | `C:/Users/User/Desktop/input_pdfs` |
| `solidsign.batch.output-path` | Carpeta local donde se guardará el archivo ZIP firmado. | `C:/Users/User/Desktop/signed_results` |
| `solidsign.cloud.credentials` | JSON con las credenciales del HSM en la nube: `uuidCert`, `hsmToken` y `hsmServiceUrl`. | `{"uuidCert":"...","hsmToken":"...","hsmServiceUrl":"https://hsm.proveedor.com"}` |
| `solidsign.sig.hashAlgorithm` | Algoritmo de hash criptográfico (SHA256, SHA384, SHA512). | `SHA256` |
| `solidsign.sig.profile` | Perfil/estándar de firma PAdES (Adobe/ICP-Brasil/ETSI). | `PDF_BASIC`, `ADRT`, `PADES_B`, etc. |
| `solidsign.sig.sigFieldMeasurementUnit` | Unidad de medida para las coordenadas del campo de firma. | `PIXELS` o `MILLIMETERS` |
| `solidsign.sig.signatureFieldConfig` | JSON con coordenadas y dimensiones del campo de firma visual. | `[{"pageNumber":1,"coordinateX":200,"coordinateY":380,"width":200,"height":80}]` |
| `solidsign.sig.signatureImagePaths` | Ruta(s) a la(s) imagen(es) de sello de firma. | `C:/img/sello.png` |
| `solidsign.sig.reason` | Motivo de la firma (visible en el panel de firmas del PDF). | `Firma digital` |
| `solidsign.sig.location` | Ubicación geográfica donde se firmó el documento. | `Brasil` |
| `solidsign.sig.contact` | Información de contacto del firmante. | `contacto@empresa.com` |
| `solidsign.sig.signatureFieldName` *(opcional)* | Nombre de un campo de firma preexistente en el PDF. | `SignatureField1` |
| `solidsign.sig.signatureTextConfig` *(opcional)* | JSON con texto personalizado en el campo de firma visual. | `[{"pageNumber":1,"coordinateX":200,"coordinateY":460,"text":"Firmado","fontSize":10,"textColor":"BLACK"}]` |
| `solidsign.sig.mdpPermissionLevel` *(opcional)* | Nivel MDP para firmas de certificación (1=sin cambios, 2=formularios, 3=anotaciones). | `1` |
| `solidsign.sig.passwordsForDecryption` *(opcional)* | Array JSON de contraseñas para abrir PDFs cifrados. | `["contraseña1"]` |
| `solidsign.sig.documentInfoMetadata` *(opcional)* | JSON con metadatos del documento PDF (título, autor, asunto, palabras clave). | `{"title":"Contrato","author":"Empresa"}` |
| `solidsign.sig.signatureQrCodeConfig` *(opcional)* | JSON con configuración del código QR a incrustar en el campo de firma visual. | `{"pageNumber":1,"coordinateX":10,"coordinateY":10,"width":60,"height":60}` |
| `solidsign.sig.policyVersion` *(opcional)* | OID de la política de firma ICP-Brasil. | `2.16.76.1.7.1.11.1.3` |

## Stack
1. Java 17
2. SpringBoot 3.4.x+
3. Maven 3.x.x+
4. Logback (para el registro de errores)

## Cómo Ejecutar

1. **Obtener credenciales:** Solicite al proveedor HSM las credenciales `uuidCert`, `hsmToken` y `hsmServiceUrl`.
2. **Configurar:** Complete `solidsign.cloud.credentials` con el JSON de credenciales y configure los demás parámetros en `src/main/resources/application.properties`.
3. **Compilar:** `mvn clean install`
4. **Iniciar:** `mvn spring-boot:run`
5. **Probar:** Envíe una solicitud POST a `http://localhost:8080/api/pdf/sign-cloud`. La aplicación procesará automáticamente todos los PDFs encontrados en la carpeta de entrada.

## Gestión de Errores
El sistema intercepta errores **400 Bad Request** y **500 Internal Server Error** y registra el JSON detallado de SolidSign para facilitar la depuración de credenciales o parámetros inválidos.
