package com.example.agenticpos.repository;

import com.example.agenticpos.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    List<Location> findByActiveTrue();
    
    Optional<Location> findByIdAndActiveTrue(Long id);
    
    Optional<Location> findByNameAndActiveTrue(String name);
    
    @Query("SELECT l FROM Location l WHERE l.active = true ORDER BY l.name")
    List<Location> findAllActiveOrderByName();
}
