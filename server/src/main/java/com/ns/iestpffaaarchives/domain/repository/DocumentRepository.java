package com.ns.iestpffaaarchives.domain.repository;

import com.ns.iestpffaaarchives.domain.entity.Document;
import com.ns.iestpffaaarchives.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByAuthor(User author);
    List<Document> findByTitleContainingIgnoreCase(String title);
    List<Document> findByIsDeletedFalse();
    
    @Query("SELECT d FROM Document d JOIN d.tags t WHERE t.name = :tagName AND d.isDeleted = false")
    List<Document> findByTagName(String tagName);
}
