package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.entity.DocumentType;
import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.application.service.DocumentTypeService;
import com.ns.iestpffaaarchives.application.service.DocumentService;
import com.ns.iestpffaaarchives.infrastructure.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DocumentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private DocumentTypeService documentTypeService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtils jwtUtils;

    @TempDir
    Path tempDir;

    private User testUser;
    private DocumentType testDocType;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testDocType = new DocumentType();
        testDocType.setId(1L);
        testDocType.setName("INFORME");
        
        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setTitle("Test Document");
        testDocument.setDescription("Test Description");
        testDocument.setFilePath("test-uuid.pdf");
        testDocument.setAuthor(testUser);
        testDocument.setType(testDocType);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void uploadDocument_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF test content".getBytes()
        );

        when(userService.getUserById(anyLong())).thenReturn(Optional.of(testUser));
        when(documentTypeService.findByName(anyString())).thenReturn(Optional.of(testDocType));
        when(documentService.createDocument(any(Document.class))).thenReturn(testDocument);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documents")
                .file(file)
                .param("title", "Test Document")
                .param("description", "Test Description")
                .param("authorId", "1")
                .param("type", "INFORME"))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void uploadDocument_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[0]
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documents")
                .file(file)
                .param("title", "Test Document")
                .param("description", "Test Description")
                .param("authorId", "1")
                .param("type", "INFORME"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("El archivo está vacío"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void uploadDocument_NoTitle() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF test content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documents")
                .file(file)
                .param("description", "Test Description")
                .param("authorId", "1")
                .param("type", "INFORME"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getDocuments_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/documents"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
