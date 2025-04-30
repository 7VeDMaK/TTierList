package adlist.Service;

import adlist.Entity.Card;
import adlist.Entity.FileData;
import adlist.Enum.Rank;
import adlist.Repository.CardRepository;
import adlist.Repository.FileDataRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public void resizeImage(String inputPath, String outputPath) throws IOException {
        final int SIZE = 512;
        File inputFile = new File(inputPath);
        BufferedImage originalImage = ImageIO.read(inputFile);

        // Создание нового изображения с указанным размером
        Image scaledImage = originalImage.getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH);
        BufferedImage bufferedImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        // Перерисовываем изображение в новый буфер с нужным размером
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();

        // Сохраняем измененное изображение
        File outputFile = new File(outputPath);
        ImageIO.write(bufferedImage, "jpg", outputFile); // Используйте подходящий формат
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

    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        Optional<FileData> fileData = fileDataRepository.findByName(fileName);
        String filePath = fileData.get().getName();
        Path fullPath = Paths.get(uploadDir, fileName);
        byte[] images = Files.readAllBytes(fullPath);
        return images;
    }
}
