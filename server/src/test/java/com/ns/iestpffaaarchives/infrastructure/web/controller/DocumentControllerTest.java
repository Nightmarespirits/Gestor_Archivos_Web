package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.application.service.DocumentService;
import com.ns.iestpffaaarchives.application.service.DocumentTypeService;
import com.ns.iestpffaaarchives.application.service.TagService;
import com.ns.iestpffaaarchives.application.service.UserService;
import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.DocumentType;
import com.ns.iestpffaaarchives.domain.entity.User;
import com.ns.iestpffaaarchives.domain.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private UserService userService;

    @Mock
    private TagService tagService;

    @Mock
    private DocumentTypeService documentTypeService;

    @InjectMocks
    private DocumentController documentController;

    @TempDir
    Path tempDir;

    private User testUser;
    private DocumentType testDocType;
    private Document testDocument;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        // Set up upload directory
        ReflectionTestUtils.setField(documentController, "uploadDir", tempDir.toString());
        
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        // Create test document type
        testDocType = new DocumentType();
        testDocType.setId(1L);
        testDocType.setName("INFORME");
        
        // Create test document
        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setTitle("Test Document");
        testDocument.setDescription("Test Description");
        testDocument.setFilePath("test-uuid.pdf");
        testDocument.setAuthor(testUser);
        testDocument.setType(testDocType);
        
        // Create test file
        String fileContent = "PDF test content";
        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                fileContent.getBytes()
        );
    }

    @Test
    void createDocument_Success() {
        // Arrange
        when(userService.getUserById(anyLong())).thenReturn(Optional.of(testUser));
        when(documentTypeService.findByName(anyString())).thenReturn(Optional.of(testDocType));
        when(documentService.createDocument(any(Document.class))).thenReturn(testDocument);
        
        // Act
        ResponseEntity<?> response = documentController.createDocument(
                testFile,
                "Test Document",
                "Test Description",
                1L,
                "INFORME",
                null
        );
        
        // Assert
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        verify(documentService, times(1)).createDocument(any(Document.class));
    }

    @Test
    void createDocument_EmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                new byte[0]
        );
        
        // Act
        ResponseEntity<?> response = documentController.createDocument(
                emptyFile,
                "Test Document",
                "Test Description",
                1L,
                "INFORME",
                null
        );
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("El archivo está vacío", response.getBody());
    }

    @Test
    void createDocument_EmptyTitle() {
        // Act
        ResponseEntity<?> response = documentController.createDocument(
                testFile,
                "",
                "Test Description",
                1L,
                "INFORME",
                null
        );
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("El título es obligatorio", response.getBody());
    }

    @Test
    void createDocument_UserNotFound() {
        // Arrange
        when(userService.getUserById(anyLong())).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<?> response = documentController.createDocument(
                testFile,
                "Test Document",
                "Test Description",
                999L,
                "INFORME",
                null
        );
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("El autor especificado no existe"));
    }

    @Test
    void createDocument_DocumentTypeNotFound() {
        // Arrange
        when(userService.getUserById(anyLong())).thenReturn(Optional.of(testUser));
        when(documentTypeService.findByName(anyString())).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<?> response = documentController.createDocument(
                testFile,
                "Test Document",
                "Test Description",
                1L,
                "TIPO_INEXISTENTE",
                null
        );
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("El tipo de documento especificado no existe"));
    }

    @Test
    void createDocument_WithTags() {
        // Arrange
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("tag1");
        
        List<String> tagNames = Collections.singletonList("tag1");
        
        when(userService.getUserById(anyLong())).thenReturn(Optional.of(testUser));
        when(documentTypeService.findByName(anyString())).thenReturn(Optional.of(testDocType));
        when(tagService.searchTagsByName(anyString())).thenReturn(Collections.singletonList(tag));
        when(documentService.createDocument(any(Document.class))).thenReturn(testDocument);
        
        // Act
        ResponseEntity<?> response = documentController.createDocument(
                testFile,
                "Test Document",
                "Test Description",
                1L,
                "INFORME",
                tagNames
        );
        
        // Assert
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());
        verify(tagService, times(1)).searchTagsByName(anyString());
        verify(documentService, times(1)).createDocument(any(Document.class));
    }
}
