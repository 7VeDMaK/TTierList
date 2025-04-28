package adlist.Service;

import adlist.Entity.Card;
import adlist.Entity.FileData;
import adlist.Repository.CardRepository;
import adlist.Repository.FileDataRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

@Service
public class CardService {

    private final FileDataRepository fileDataRepository;

    private final CardRepository cardRepository;

    @Getter
    @Value("${file.upload-dir}")
    private String uploadDir;

    public CardService(FileDataRepository fileDataRepository, CardRepository cardRepository) {
        this.fileDataRepository = fileDataRepository;
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

    public Card updateCard(Long id, Card card, MultipartFile imageFile) throws IOException {
        // Находим существующую карточку
        Card existingCard = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid card ID: " + id));

        // Обновляем данные карточки
        existingCard.setName(card.getName());
        existingCard.setRank(card.getRank());
        existingCard.setSourceLink(card.getSourceLink());
        existingCard.setDescription(card.getDescription());

        // Обрабатываем изображение, если оно новое
        if (!imageFile.isEmpty()) {
            existingCard.setImageUrl(uploadImageToFileSystem(imageFile));  // загрузка изображения
        }

        // Сохраняем обновленную карточку
        return cardRepository.save(existingCard);
    }

    public String uploadImageToFileSystem(MultipartFile file) throws IOException {
        // Получаем абсолютный путь до папки, где запущено приложение
        Path currentDir = Paths.get("").toAbsolutePath();
        System.out.println(currentDir);
        Path uploadPath = currentDir.resolve(uploadDir);
        System.out.println(uploadPath);

        // Создаём папку, если её нет
        File uploadFolder = uploadPath.toFile();
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // Полный путь до нового файла
        String filePath = uploadPath.resolve(file.getOriginalFilename()).toString();

        fileDataRepository.save(FileData.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .build());

        // Сохраняем файл
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
