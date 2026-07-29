package com.example.urlshortener.repository;
import com.example.urlshortener.entity.DecisionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface DecisionHistoryRepository extends JpaRepository<DecisionHistory, UUID> { }
