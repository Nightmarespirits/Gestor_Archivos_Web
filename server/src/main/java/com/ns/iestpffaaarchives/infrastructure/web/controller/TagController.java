package com.ns.iestpffaaarchives.infrastructure.web.controller;

import com.ns.iestpffaaarchives.domain.entity.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

// Repositorio para Tag
@Repository
interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);
    List<Tag> findByNameContainingIgnoreCase(String name);
}

// Servicio para Tag
@Service
class TagService {
    
    private final TagRepository tagRepository;
    
    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }
    
    public List<Tag> searchTagsByName(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }
    
    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }
    
    public Tag updateTag(Tag tag) {
        return tagRepository.save(tag);
    }
    
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
    
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
}

@RestController
@RequestMapping("/api/tags")
// Eliminado @CrossOrigin(origins = "*") para usar la configuración global
public class TagController {

    private final TagService tagService;
    
    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }
    
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        Optional<Tag> tag = tagService.getTagById(id);
        return tag.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Tag>> searchTagsByName(@RequestParam String name) {
        List<Tag> tags = tagService.searchTagsByName(name);
        return ResponseEntity.ok(tags);
    }
    
    @PostMapping
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
            
            Tag updatedTag = tagService.updateTag(tag);
            return ResponseEntity.ok(updatedTag);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
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
