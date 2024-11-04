package com.kiloflyers.repository;

import com.kiloflyers.model.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
	Optional<ImageEntity> findByFileName(String fileName);

	List<ImageEntity> findByFileNameAndType(String fileName, String type);
	
	// In the repository
	Optional<ImageEntity> findFirstByFileNameAndType(String fileName, String type);

}