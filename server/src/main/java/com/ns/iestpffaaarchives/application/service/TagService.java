package com.ns.iestpffaaarchives.application.service;

import com.ns.iestpffaaarchives.domain.entity.Tag;
import com.ns.iestpffaaarchives.domain.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {
    
    private final TagRepository tagRepository;
    
    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Tag> searchTagsByName(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Transactional
    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }
    
    @Transactional
    public Tag updateTag(Tag tag) {
        return tagRepository.save(tag);
    }
    
    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
}