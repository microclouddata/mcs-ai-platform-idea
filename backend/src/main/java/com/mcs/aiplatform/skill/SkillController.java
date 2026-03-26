package com.mcs.aiplatform.skill;

import com.mcs.aiplatform.common.ApiResponse;
import com.mcs.aiplatform.config.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/agents/{agentId}/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final SkillFileService skillFileService;

    // --- CRUD ---

    @GetMapping
    public ApiResponse<List<Skill>> list(@PathVariable String agentId) {
        return ApiResponse.ok(skillService.list(CurrentUser.userId(), agentId));
    }

    @PostMapping
    public ApiResponse<Skill> create(
            @PathVariable String agentId,
            @RequestBody CreateSkillRequest req) {
        return ApiResponse.ok(skillService.create(CurrentUser.userId(), agentId, req));
    }

    @GetMapping("/{skillId}")
    public ApiResponse<Skill> get(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        return ApiResponse.ok(skillService.get(CurrentUser.userId(), agentId, skillId));
    }

    @PutMapping("/{skillId}")
    public ApiResponse<Skill> update(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @RequestBody UpdateSkillRequest req) {
        return ApiResponse.ok(skillService.update(CurrentUser.userId(), agentId, skillId, req));
    }

    @DeleteMapping("/{skillId}")
    public ApiResponse<Void> delete(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        skillService.delete(CurrentUser.userId(), agentId, skillId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{skillId}/status")
    public ApiResponse<Skill> toggleStatus(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        return ApiResponse.ok(skillService.toggleStatus(CurrentUser.userId(), agentId, skillId));
    }

    // --- Scripts ---

    @GetMapping("/{skillId}/scripts")
    public ApiResponse<List<String>> listScripts(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        return ApiResponse.ok(skillFileService.listScripts(skillId));
    }

    @GetMapping("/{skillId}/scripts/{filename}")
    public ApiResponse<String> getScript(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        return ApiResponse.ok(skillFileService.readScript(skillId, filename));
    }

    @PostMapping("/{skillId}/scripts/{filename}")
    public ApiResponse<Void> saveScript(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename,
            @RequestBody String content) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        skillFileService.saveScript(skillId, filename, content);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{skillId}/scripts/{filename}")
    public ApiResponse<Void> deleteScript(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        skillFileService.deleteScript(skillId, filename);
        return ApiResponse.ok(null);
    }

    @PostMapping(value = "/{skillId}/scripts/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadScript(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @RequestParam("file") MultipartFile file) throws IOException {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        String filename = file.getOriginalFilename();
        skillFileService.saveScript(skillId, filename, new String(file.getBytes()));
        return ApiResponse.ok(filename);
    }

    // --- References ---

    @GetMapping("/{skillId}/references")
    public ApiResponse<List<String>> listReferences(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        return ApiResponse.ok(skillFileService.listReferences(skillId));
    }

    @GetMapping("/{skillId}/references/{filename}")
    public ApiResponse<String> getReference(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        return ApiResponse.ok(skillFileService.readReference(skillId, filename));
    }

    @PostMapping("/{skillId}/references/{filename}")
    public ApiResponse<Void> saveReference(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename,
            @RequestBody String content) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        skillFileService.saveReference(skillId, filename, content);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{skillId}/references/{filename}")
    public ApiResponse<Void> deleteReference(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        skillFileService.deleteReference(skillId, filename);
        return ApiResponse.ok(null);
    }

    @PostMapping(value = "/{skillId}/references/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadReference(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @RequestParam("file") MultipartFile file) throws IOException {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        String filename = file.getOriginalFilename();
        skillFileService.saveReference(skillId, filename, new String(file.getBytes()));
        return ApiResponse.ok(filename);
    }

    // --- Assets ---

    @GetMapping("/{skillId}/assets")
    public ApiResponse<List<String>> listAssets(
            @PathVariable String agentId,
            @PathVariable String skillId) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        return ApiResponse.ok(skillFileService.listAssets(skillId));
    }

    @DeleteMapping("/{skillId}/assets/{filename}")
    public ApiResponse<Void> deleteAsset(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        skillFileService.deleteAsset(skillId, filename);
        return ApiResponse.ok(null);
    }

    @PostMapping(value = "/{skillId}/assets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadAsset(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @RequestParam("file") MultipartFile file) throws IOException {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        String filename = file.getOriginalFilename();
        skillFileService.saveAsset(skillId, filename, file.getBytes());
        return ApiResponse.ok(filename);
    }

    @GetMapping("/{skillId}/assets/{filename}")
    public ResponseEntity<byte[]> downloadAsset(
            @PathVariable String agentId,
            @PathVariable String skillId,
            @PathVariable String filename) {
        skillService.get(CurrentUser.userId(), agentId, skillId);
        byte[] data = skillFileService.readAsset(skillId, filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(data);
    }
}
