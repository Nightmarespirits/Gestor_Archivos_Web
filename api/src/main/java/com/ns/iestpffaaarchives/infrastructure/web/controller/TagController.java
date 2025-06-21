package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.domain.entity.Tag;
import com.ns.iestpffaaarchives.application.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;
    
    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        Optional<Tag> tag = tagService.getTagById(id);
        return tag.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('DOCUMENT_READ')")
    public ResponseEntity<List<Tag>> searchTagsByName(@RequestParam String name) {
        List<Tag> tags = tagService.searchTagsByName(name);
        return ResponseEntity.ok(tags);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<?> createTag(@RequestBody Tag tag) {
        // Verificar si ya existe una etiqueta con el mismo nombre
        if (tagService.existsByName(tag.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Ya existe una etiqueta con ese nombre");
        }
        
        Tag newTag = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody Tag tagDetails) {
        Optional<Tag> tagOptional = tagService.getTagById(id);
        
        if (tagOptional.isPresent()) {
            Tag tag = tagOptional.get();
            
            // Verificar si el nuevo nombre ya existe y no es la misma etiqueta
            if (!tag.getName().equals(tagDetails.getName()) && 
                    tagService.existsByName(tagDetails.getName())) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: Ya existe una etiqueta con ese nombre");
            }
            
            tag.setName(tagDetails.getName());
            if (tagDetails.getDescription() != null) {
                tag.setDescription(tagDetails.getDescription());
            }
            
            Tag updatedTag = tagService.updateTag(tag);
            return ResponseEntity.ok(updatedTag);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_UPDATE')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        Optional<Tag> tagOptional = tagService.getTagById(id);
        
        if (tagOptional.isPresent()) {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
