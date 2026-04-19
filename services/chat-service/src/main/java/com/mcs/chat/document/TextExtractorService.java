package com.mcs.chat.document;

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

    public String extractText(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        byte[] bytes = file.getBytes();

        for (String ext : TEXT_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }

        if (fileName.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(bytes)) {
                return new PDFTextStripper().getText(document);
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
                return "[Image attached: " + fileName + " (" + (file.getSize() / 1024) + " KB)]";
            }
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}