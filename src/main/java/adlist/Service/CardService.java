package adlist.Service;

import adlist.Entity.Card;
import adlist.Entity.FileData;
import adlist.Repository.CardRepository;
import adlist.Repository.FileDataRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private FileDataRepository fileDataRepository;

    private final CardRepository cardRepository;

    @Getter
    @Value("${file.upload-dir}")
    private String uploadDir;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    // Проверка и создание новой карты (без работы с изображением)
    public Card createCard(Card card) {
        if (card.getName() == null || card.getName().isEmpty()) {
            throw new IllegalArgumentException("Card name cannot be empty");
        }
        if (cardRepository.existsByName(card.getName())) {
            throw new IllegalArgumentException("Card with this name already exists");
        }
        return cardRepository.save(card);
    }

    public String uploadImageToFileSystem(MultipartFile file) throws IOException {
        String filePath = uploadDir + file.getOriginalFilename();
        fileDataRepository.save(FileData.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType()).build());

        file.transferTo(new File(filePath));

        return file.getOriginalFilename();
    }

    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<FileData> fileData = fileDataRepository.findByName(fileName);
        String filePath = fileData.get().getName();
        Path fullPath = Paths.get(uploadDir, fileName);
        byte[] images = Files.readAllBytes(fullPath);
        return images;
    }
}
