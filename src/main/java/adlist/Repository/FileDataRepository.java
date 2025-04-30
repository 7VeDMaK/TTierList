package adlist.Repository;

import adlist.Entity.FileData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileDataRepository extends JpaRepository<FileData, Integer> {
    Optional<FileData> findByName(String fileName);

    boolean existsByName(String originalFilename);
}
