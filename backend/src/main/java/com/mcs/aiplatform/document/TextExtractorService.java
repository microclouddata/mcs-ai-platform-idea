package com.mcs.aiplatform.document;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class TextExtractorService {

    private static final java.util.Set<String> TEXT_EXTENSIONS = java.util.Set.of(
            ".txt", ".md", ".json", ".csv", ".xml", ".yaml", ".yml",
            ".html", ".htm", ".js", ".ts", ".java", ".py", ".sh", ".sql"
    );

    private static final java.util.Set<String> IMAGE_EXTENSIONS = java.util.Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp", ".svg"
    );

    public String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

        for (String ext : TEXT_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        }

        if (fileName.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        }

        for (String ext : IMAGE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return "[Image attached: " + file.getOriginalFilename() + " (" + (file.getSize() / 1024) + " KB)]";
            }
        }

        // fallback: try reading as UTF-8 text
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }
}
