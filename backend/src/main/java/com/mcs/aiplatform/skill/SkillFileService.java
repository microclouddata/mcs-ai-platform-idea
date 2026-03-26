package com.mcs.aiplatform.skill;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages file resources for skills stored on disk.
 *
 * Directory layout under app.storage.local-path:
 * skills/{skillId}/
 *   scripts/     — executable code files
 *   references/  — documentation files
 *   assets/      — templates and static resources
 */
@Slf4j
@Service
public class SkillFileService {

    private final Path storageRoot;

    public SkillFileService(@Value("${app.storage.local-path}") String localPath) {
        this.storageRoot = Path.of(localPath).resolve("skills");
    }

    // --- Lifecycle ---

    public void initSkillDirectory(String skillId) {
        try {
            Files.createDirectories(skillDir(skillId).resolve("scripts"));
            Files.createDirectories(skillDir(skillId).resolve("references"));
            Files.createDirectories(skillDir(skillId).resolve("assets"));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create skill directory for " + skillId, e);
        }
    }

    public void deleteSkillDirectory(String skillId) {
        Path dir = skillDir(skillId);
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException e) {
                    log.warn("Could not delete {}: {}", p, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("Failed to delete skill directory {}: {}", dir, e.getMessage());
        }
    }

    // --- Scripts ---

    public List<String> listScripts(String skillId) {
        return listFiles(skillDir(skillId).resolve("scripts"));
    }

    public String readScript(String skillId, String filename) {
        return readText(skillDir(skillId).resolve("scripts").resolve(sanitize(filename)));
    }

    public void saveScript(String skillId, String filename, String content) {
        writeText(skillDir(skillId).resolve("scripts").resolve(sanitize(filename)), content);
    }

    public void deleteScript(String skillId, String filename) {
        deleteFile(skillDir(skillId).resolve("scripts").resolve(sanitize(filename)));
    }

    // --- References ---

    public List<String> listReferences(String skillId) {
        return listFiles(skillDir(skillId).resolve("references"));
    }

    public String readReference(String skillId, String filename) {
        return readText(skillDir(skillId).resolve("references").resolve(sanitize(filename)));
    }

    public void saveReference(String skillId, String filename, String content) {
        writeText(skillDir(skillId).resolve("references").resolve(sanitize(filename)), content);
    }

    public void deleteReference(String skillId, String filename) {
        deleteFile(skillDir(skillId).resolve("references").resolve(sanitize(filename)));
    }

    // --- Assets ---

    public List<String> listAssets(String skillId) {
        return listFiles(skillDir(skillId).resolve("assets"));
    }

    public byte[] readAsset(String skillId, String filename) {
        try {
            return Files.readAllBytes(skillDir(skillId).resolve("assets").resolve(sanitize(filename)));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read asset " + filename, e);
        }
    }

    public void saveAsset(String skillId, String filename, byte[] content) {
        try {
            Path path = skillDir(skillId).resolve("assets").resolve(sanitize(filename));
            Files.createDirectories(path.getParent());
            Files.write(path, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save asset " + filename, e);
        }
    }

    public void deleteAsset(String skillId, String filename) {
        deleteFile(skillDir(skillId).resolve("assets").resolve(sanitize(filename)));
    }

    // --- Helpers ---

    private Path skillDir(String skillId) {
        return storageRoot.resolve(skillId);
    }

    private List<String> listFiles(Path dir) {
        if (!Files.exists(dir)) return List.of();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list files in " + dir, e);
        }
    }

    private String readText(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("File not found: " + path.getFileName(), e);
        }
    }

    private void writeText(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write file " + path.getFileName(), e);
        }
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", path, e.getMessage());
        }
    }

    /** Strip path traversal characters from a filename. */
    private String sanitize(String filename) {
        return Path.of(filename).getFileName().toString();
    }
}
