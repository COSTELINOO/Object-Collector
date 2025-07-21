package backend.api.util.multipart;

import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MultipartParser {
    // Limita maxima pentru upload (10 MB)
    private static final int MAX_UPLOAD_SIZE = 10 * 1024 * 1024; // 10 MB
    // Tipul de continut implicit pentru fisiere
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final byte[] data;
    // Delimitatorul multipart (boundary)
    private final String boundary;
    // Map cu fisierele incarcate (cheie: nume camp, valoare: MultipartFile)
    private final Map<String, MultipartFile> files = new HashMap<>();
    // Map cu campurile text din formular (cheie: nume camp, valoare: valoare text)
    private final Map<String, String> formFields = new HashMap<>();

    // Constructor care parseaza request-ul multipart
    public MultipartParser(InputStream inputStream, String contentType) throws CustomException {
        if (inputStream == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "NullInputStream",
                    "Stream de intrare null"
            );
        }

        // Verifica daca headerul Content-Type este multipart/form-data
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidContentType",
                    "Tip continut invalid, trebuie sa fie multipart/form-data"
            );
        }

        try {
            // Extrage boundary din Content-Type (delimitatorul partilor)
            int boundaryIndex = contentType.indexOf("boundary=");
            if (boundaryIndex != -1) {
                boundary = contentType.substring(boundaryIndex + "boundary=".length()).trim();
            } else {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "MissingBoundary",
                        "Boundary nu a fost gasit in Content-Type"
                );
            }
            // Citeste toate datele din InputStream in bufferul data
            data = readInputStreamWithSizeLimit(inputStream);
            // Parcurge si proceseaza fiecare parte din multipart
            parseMultipartData();
            Logger.debug("Parsare multipart finalizata: " + files.size() + " fisiere, " + formFields.size() + " campuri");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e)
        {
            throw new Exception500.InternalServerErrorException(
                    "MultipartError",
                    "ParsingFailed",
                    "Eroare la parsarea datelor multipart: " + e.getMessage(),
                    e
            );
        }
    }

    // Citeste toate datele din InputStream pana la limita maxima si le returneaza ca byte[]
    private byte[] readInputStreamWithSizeLimit(InputStream inputStream) throws IOException, CustomException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        int totalBytes = 0;

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            // Citeste pe bucati de 8192 bytes
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytes += bytesRead;
                // Daca s-a depasit limita maxima, arunca exceptie
                if (totalBytes > MultipartParser.MAX_UPLOAD_SIZE) {
                    throw new Exception400.BadRequestException(
                            "ValidationError",
                            "FileTooLarge",
                            "Dimensiunea incarcarii depaseste limita de " + (MultipartParser.MAX_UPLOAD_SIZE / (1024 * 1024)) + " MB"
                    );
                }
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        }
    }

    // Parcurge bufferul si separa fiecare parte multipart
    private void parseMultipartData() {
        // Conversie byte[] la String cu charset ISO_8859_1 (pt fisiere binare)
        String content = new String(data, StandardCharsets.ISO_8859_1);
        // Split dupa boundary (delimitatorul partilor)
        String[] parts = content.split("--" + boundary);

        for (String part : parts) {
            // Ignora partile goale sau finalul
            if (part.trim().isEmpty() || part.trim().equals("--")) continue;

            // Daca partea contine filename => este fisier
            if (part.contains("filename=")) {
                processFilePart(part);
            }
            // Daca partea contine doar name => este camp text
            else if (part.contains("name=")) {
                processFormField(part);
            }
        }
    }

    // Proceseaza o parte care contine un fisier
    private void processFilePart(String part) {
        try {
            // Extrage numele campului ("name")
            int nameStart = part.indexOf("name=\"") + 6;
            int nameEnd = part.indexOf("\"", nameStart);
            if (nameStart < 6 || nameEnd == -1) return;
            String fieldName = part.substring(nameStart, nameEnd);

            // Extrage numele fisierului original ("filename")
            int filenameStart = part.indexOf("filename=\"") + 10;
            int filenameEnd = part.indexOf("\"", filenameStart);
            if (filenameStart < 10 || filenameEnd == -1) return;
            String filename = part.substring(filenameStart, filenameEnd);

            // Daca nu exista nume de fisier, trateaza ca pe camp text
            if (filename.isEmpty()) {
                processFormField(part);
                return;
            }

            // Extrage tipul de continut (Content-Type) daca exista, altfel foloseste default
            String contentType = DEFAULT_CONTENT_TYPE;
            int contentTypeStart = part.indexOf("Content-Type: ");
            if (contentTypeStart != -1) {
                contentTypeStart += 14;
                int contentTypeEnd = part.indexOf("\r\n", contentTypeStart);
                if (contentTypeEnd != -1) {
                    contentType = part.substring(contentTypeStart, contentTypeEnd).trim();
                }
            }

            // Extrage continutul fisierului dintre headere si final
            int contentStart = part.indexOf("\r\n\r\n");
            if (contentStart != -1) {
                contentStart += 4;
                int contentEnd = part.length();
                if (part.endsWith("\r\n")) {
                    contentEnd -= 2;
                }
                byte[] fileData = part.substring(contentStart, contentEnd).getBytes(StandardCharsets.ISO_8859_1);

                // Creeaza obiectul MultipartFile si il pune in map
                MultipartFile file = new MultipartFile(fieldName, filename, contentType, fileData);
                files.put(fieldName, file);
                Logger.debug("Fisier procesat: " + fieldName + ", " + filename + ", " +
                        contentType + ", " + fileData.length + " bytes");
            }
        } catch (Exception e) {
            Logger.warning("Eroare la procesarea partii de fisier: " + e.getMessage());
        }
    }

    // Proceseaza o parte care contine un camp text
    private void processFormField(String part) {
        try {
            // Extrage numele campului ("name")
            int nameStart = part.indexOf("name=\"") + 6;
            int nameEnd = part.indexOf("\"", nameStart);
            if (nameStart < 6 || nameEnd == -1) return;
            String fieldName = part.substring(nameStart, nameEnd);

            // Extrage valoarea campului (dupa headere)
            int valueStart = part.indexOf("\r\n\r\n");
            if (valueStart != -1) {
                valueStart += 4;
                int valueEnd = part.length();
                if (part.endsWith("\r\n")) {
                    valueEnd -= 2;
                }
                String value = part.substring(valueStart, valueEnd);
                formFields.put(fieldName, value);
                Logger.debug("Camp formular procesat: " + fieldName + ", " +
                        (value.length() > 100 ? value.substring(0, 100) + "..." : value));
            }
        } catch (Exception e) {
            Logger.warning("Eroare la procesarea partii de formular: " + e.getMessage());
        }
    }

    // Returneaza fisierul incarcat pentru un anumit camp
    public MultipartFile getFile(String name) {
        return files.get(name);
    }

    // Returneaza valoarea text pentru un anumit camp
    public String getFormField(String name) {
        return formFields.get(name);
    }

    // Clasa pentru reprezentarea unui fisier incarcat
    public static class MultipartFile {
        private final String fieldName;   // numele campului din formular
        private final String filename;    // numele original al fisierului incarcat
        private final String contentType; // tipul de continut (MIME)
        private final byte[] data;        // continutul fisierului

        public MultipartFile(String fieldName, String filename, String contentType, byte[] data) {
            this.fieldName = fieldName;
            this.filename = filename;
            this.contentType = contentType;
            this.data = data;
        }

        public String getFilename() {
            return filename;
        }

        public byte[] getData() {
            return data;
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }

        public long getSize() {
            return data.length;
        }

        public String getFileExtension(String filename) {
            if (filename == null || filename.isEmpty()) {
                return "";
            }
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex >= 0 && lastDotIndex < filename.length() - 1) {
                return filename.substring(lastDotIndex + 1).toLowerCase();
            }
            return "";
        }

        @Override
        public String toString() {
            return "MultipartFile{" +
                    "fieldName='" + fieldName + "', " +
                    "filename='" + filename + "', " +
                    "contentType='" + contentType + "', " +
                    "size=" + data.length + " bytes}";
        }
    }
}