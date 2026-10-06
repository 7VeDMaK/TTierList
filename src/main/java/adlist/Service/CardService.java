package adlist.Service;

import adlist.Entity.Card;
import adlist.Entity.FileData;
import adlist.Entity.Tag;
import adlist.Enum.Rank;
import adlist.Repository.CardRepository;
import adlist.Repository.FileDataRepository;
import adlist.Repository.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CardService {

    private final FileDataRepository fileDataRepository;
    private final TagRepository tagRepository;

    private final CardRepository cardRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public CardService(FileDataRepository fileDataRepository, TagRepository tagRepository, CardRepository cardRepository) {
        this.fileDataRepository = fileDataRepository;
        this.tagRepository = tagRepository;
        this.cardRepository = cardRepository;
    }

    public void updateCardTags(Long cardId, List<Long> tagIds) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        List<Tag> tags = tagRepository.findAllById(tagIds);
        card.setTags(new HashSet<>(tags));
        cardRepository.save(card);
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

    @Transactional
    public void reorderCards(Map<String, List<Long>> rankMap) {
        for (Map.Entry<String, List<Long>> entry : rankMap.entrySet()) {
            Rank rank = Rank.valueOf(entry.getKey());
            List<Long> ids = entry.getValue();
            for (int i = 0; i < ids.size(); i++) {
                Long cardId = ids.get(i);
                Optional<Card> optionalCard = cardRepository.findById(cardId);
                if (optionalCard.isPresent()) {
                    Card card = optionalCard.get();
                    card.setRank(rank);
                    card.setPosition(i);
                    cardRepository.save(card); // save обновит, если id уже есть
                }
            }
        }
    }

    public String uploadImageToFileSystem(MultipartFile file) throws IOException {
        // Получаем абсолютный путь до папки, где запущено приложение
        Path currentDir = Paths.get("").toAbsolutePath();
        Path uploadPath = currentDir.resolve(uploadDir);

        // Создаём папку, если её нет
        File uploadFolder = uploadPath.toFile();
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        // Полный путь до нового файла
        String filePath = uploadPath.resolve(file.getOriginalFilename()).toString();

        if (!fileDataRepository.existsByName(file.getOriginalFilename())) {

            fileDataRepository.save(FileData.builder()
                    .name(file.getOriginalFilename())
                    .type(file.getContentType())
                    .build());

            // Сохраняем файл
            file.transferTo(new File(filePath));
            return file.getOriginalFilename();
        }
        return null;

    }

    public void addTag(Long cardId, Long tagId) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        Tag tag = tagRepository.findById(tagId).orElseThrow();
        card.getTags().add(tag);
        cardRepository.save(card);
    }

    public void removeTag(Long cardId, Long tagId) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        Tag tag = tagRepository.findById(tagId).orElseThrow();
        card.getTags().remove(tag);
        cardRepository.save(card);
    }

    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<FileData> fileData = fileDataRepository.findByName(fileName);
        String filePath = fileData.get().getName();
        Path fullPath = Paths.get(uploadDir, fileName);
        byte[] images = Files.readAllBytes(fullPath);
        return images;
    }
}
