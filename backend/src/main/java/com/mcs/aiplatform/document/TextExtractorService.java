package com.mcs.aiplatform.document;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Service
public class TextExtractorService {

    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            ".txt", ".md", ".json", ".csv", ".xml", ".yaml", ".yml",
            ".html", ".htm", ".js", ".ts", ".java", ".py", ".sh", ".sql"
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp", ".svg"
    );

    // ── MultipartFile path (HTTP upload, synchronous) ──────────────────────

    public String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        return extractFromBytes(file.getBytes(), fileName, file.getSize());
    }

    // ── Path-based extraction (Kafka consumer, async processing) ──────────

    /**
     * Extracts text from a file that has already been persisted to disk.
     * Used by {@code DocumentProcessingConsumer} to process uploads asynchronously.
     *
     * @param filePath     path to the stored file
     * @param originalName original filename (used to detect file type by extension)
     */
    public String extractTextFromPath(Path filePath, String originalName) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);
        String fileName = originalName == null ? filePath.getFileName().toString() : originalName.toLowerCase();
        long size = Files.size(filePath);
        return extractFromBytes(bytes, fileName, size);
    }

    // ── Shared extraction logic ────────────────────────────────────────────

    private String extractFromBytes(byte[] bytes, String fileName, long size) throws IOException {
        for (String ext : TEXT_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }

        if (fileName.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        }

        if (fileName.endsWith(".docx")) {
            try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(bytes));
                 XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
                return extractor.getText();
            }
        }

        for (String ext : IMAGE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return "[Image attached: " + fileName + " (" + (size / 1024) + " KB)]";
            }
        }

        // Fallback: try reading as UTF-8 text
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
