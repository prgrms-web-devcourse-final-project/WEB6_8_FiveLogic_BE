package com.back.domain.file.video.controller;

import com.back.domain.file.video.dto.service.PresignedUrlResponse;
import com.back.domain.file.video.service.FileManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileManager fileManager;

    @Test
    @DisplayName("업로드 URL 요청 - 성공")
    @WithMockUser(roles = "ADMIN")
    void testGetUploadUrl() throws Exception {
        URL mockUrl = new URL("https://bucket.s3.amazonaws.com/123e4567-e89b-12d3-a456-426614174000");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        Mockito.when(fileManager.getUploadUrl(anyString()))
                .thenReturn(new PresignedUrlResponse(mockUrl, expiresAt));

        mockMvc.perform(get("/videos/upload")
                        .param("filename", "test.mp4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("업로드용 URL 요청완료"))
                .andExpect(jsonPath("$.data.url").value(mockUrl.toString()))
                .andExpect(jsonPath("$.data.uuid").value("123e4567-e89b-12d3-a456-426614174000"));
    }

    @Test
    @DisplayName("업로드 URL 요청 - 권한 부족")
    @WithMockUser(roles = "USER")
        // ADMIN 아님
    void testGetUploadUrl_Unauthorized() throws Exception {
        mockMvc.perform(get("/videos/upload")
                        .param("filename", "test.mp4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403 권한 부족
    }

    @Test
    @DisplayName("업로드 URL 요청 - 파일명에 확장자가 없는 경우")
    @WithMockUser(roles = "ADMIN")
    void testGetUploadUrl_NoExtension() throws Exception {
        Mockito.when(fileManager.getUploadUrl(anyString()))
                .thenThrow(new com.back.global.exception.ServiceException("400", "지원하지 않는 동영상 파일 형식입니다: testfile"));

        mockMvc.perform(get("/videos/upload")
                        .param("filename", "testfile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("지원하지 않는 동영상 파일 형식입니다: testfile"));
    }

    @Test
    @DisplayName("다운로드 URL 요청 - 성공")
    @WithMockUser
    void testGetDownloadUrls() throws Exception {
        URL mockUrl = new URL("https://bucket.s3.amazonaws.com/test.mp4");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        Mockito.when(fileManager.getDownloadUrl(anyString(), anyString()))
                .thenReturn(new PresignedUrlResponse(mockUrl, expiresAt));

        mockMvc.perform(get("/videos/download")
                        .param("uuid", "123e4567-e89b-12d3-a456-426614174000")
                        .param("resolution", "1080p")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("다운로드용 URL 요청완료"))
                .andExpect(jsonPath("$.data.url").value(mockUrl.toString()));
    }

}
