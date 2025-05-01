package adlist.Controller;

import adlist.Entity.Card;
import adlist.Entity.Tag;
import adlist.Enum.Rank;
import adlist.Repository.CardRepository;
import adlist.Repository.TagRepository;
import adlist.Service.CardService;
import adlist.Service.RankGroup;
import adlist.Service.TagService;
import adlist.dto.CardTagUpdateDTO;
import adlist.dto.CardTagUpdateWrapper;
import adlist.dto.ReorderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cards")
public class CardController {

    private final CardRepository cardRepository;

    private final CardService cardService;

    private final TagRepository tagRepository;


    public CardController(CardRepository cardRepository, CardService cardService, TagRepository tagRepository, TagService tagService) {
        this.cardRepository = cardRepository;
        this.cardService = cardService;
        this.tagRepository = tagRepository;
    }

    // Вывод всех карточек
    @GetMapping
    public String getAllCards(Model model) {
        List<Card> cards = cardRepository.findAll();

        // Группируем карты по Rank
        Map<Rank, List<Card>> groupedCards = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank));

        // Формируем упорядоченный список групп рангов
        List<RankGroup> rankGroups = Arrays.stream(Rank.values())
                .map(rank -> {
                    List<Card> sortedCards = groupedCards.getOrDefault(rank, List.of()).stream()
                            .sorted(Comparator.comparingInt(Card::getPosition))
                            .collect(Collectors.toList());
                    return new RankGroup(rank, sortedCards);
                })
                .collect(Collectors.toList());


        model.addAttribute("cards", cards);
        model.addAttribute("rankGroups", rankGroups);
        model.addAttribute("uploadDir", "/fileSystem/");

        return "cards"; // шаблон для отображения списка карточек
    }

    @GetMapping("info")
    public String getAllCards2(Model model) {
        List<Card> cards = cardRepository.findAll();

        // Группируем карты по Rank
        Map<Rank, List<Card>> groupedCards = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank));

        // Формируем упорядоченный список групп рангов
        List<RankGroup> rankGroups = Arrays.stream(Rank.values())
                .map(rank -> {
                    List<Card> sortedCards = groupedCards.getOrDefault(rank, List.of()).stream()
                            .sorted(Comparator.comparingInt(Card::getPosition))
                            .collect(Collectors.toList());
                    return new RankGroup(rank, sortedCards);
                })
                .collect(Collectors.toList());

        model.addAttribute("allTags", tagRepository.findAll());
        model.addAttribute("cards", cards);
        model.addAttribute("rankGroups", rankGroups);
        model.addAttribute("uploadDir", "/fileSystem/");

        return "cards2"; // шаблон для отображения списка карточек
    }

    @GetMapping("{id}")
    public String showCardDetails(@PathVariable Long id, Model model) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid card ID: " + id));
        model.addAttribute("card", card);
        model.addAttribute("uploadDir", "/fileSystem/");
        return "card-details"; // имя html-файла
    }


    // Форма создания новой карточки
    @GetMapping("/new")
    public String createCardForm(Model model) {
        model.addAttribute("newCard", new Card());
        model.addAttribute("tags", tagRepository.findAll());
        return "createCard"; // шаблон для формы создания карточки
    }

    // Создание карточки с загрузкой изображения
    @PostMapping("/new")
    public String createCard(
            @ModelAttribute("newCard") Card card,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds) {

        if (!imageFile.isEmpty()) {
            try {
                card.setImageUrl(cardService.uploadImageToFileSystem(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(tagIds);
            card.setTags(new HashSet<>(tags)); // устанавливаем теги в карточку
        }

        cardRepository.save(card);
        return "redirect:/cards";
    }
    
    // Контроллер для редактирования карточки
    @GetMapping("/edit/{id}")
    public String editCardForm(@PathVariable("id") Long id, Model model) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid card ID: " + id));
        model.addAttribute("card", card); // Передаем текущую карточку в форму
        model.addAttribute("tags", tagRepository.findAll()); // Загружаем все теги для выбора
        return "editCard"; // Шаблон для формы редактирования
    }

    @PostMapping("/addTag")
    public ResponseEntity<?> addTagToCard(@RequestBody Map<String, Long> payload) {
        cardService.addTag(payload.get("cardId"), payload.get("tagId"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/removeTag")
    public ResponseEntity<?> removeTagFromCard(@RequestBody Map<String, Long> payload) {
        cardService.removeTag(payload.get("cardId"), payload.get("tagId"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderCards(@RequestBody ReorderRequest request) {
        try {
            cardService.reorderCards(request.getRankMap());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении порядка");
        }
    }


    // Обработка обновления карточки
    @PostMapping("/edit/{id}")
    public String updateCard(@PathVariable("id") Long id,
                             @ModelAttribute("card") Card updatedCard,
                             @RequestParam(value = "tagIds", required = false) Set<Long> tagIds,
                             @RequestParam("imageFile") MultipartFile imageFile) {
        // 1. Загружаем существующую карточку из базы
        Card existingCard = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid card ID: " + id));

        // 2. Обновляем нужные поля
        existingCard.setName(updatedCard.getName());
        existingCard.setRank(updatedCard.getRank());
        existingCard.setSourceLink(updatedCard.getSourceLink());
        existingCard.setDescription(updatedCard.getDescription());

        // 3. Обновляем теги
        if (tagIds != null) {
            Set<Tag> selectedTags = new HashSet<>(tagRepository.findAllById(tagIds));
            existingCard.setTags(selectedTags);
        } else {
            existingCard.getTags().clear(); // Если ничего не выбрано — очищаем теги
        }

        // 4. Обновляем картинку (если нужно)
        try {
            cardService.updateCard(id, existingCard, imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/error";
        }

        return "redirect:/cards";
    }


    // Удаление карточки
    @PostMapping("/delete/{id}")
    public String deleteCard(@PathVariable("id") Long id) {
        cardRepository.deleteById(id);
        return "redirect:/cards";
    }

    @GetMapping("/bulk-edit-tags")
    public String showBulkEditPage(Model model) {
        List<Card> cards = cardRepository.findAll();
        List<Tag> tags = tagRepository.findAll();

        List<CardTagUpdateDTO> dtos = cards.stream().map(card -> {
            CardTagUpdateDTO dto = new CardTagUpdateDTO();
            dto.setCardId(Long.valueOf(card.getId()));
            dto.setTagIds(card.getTags().stream().map(Tag::getId).map(Long::valueOf).toList());
            return dto;
        }).toList();

        CardTagUpdateWrapper wrapper = new CardTagUpdateWrapper();
        wrapper.setCards(dtos);

        model.addAttribute("cards", cardRepository.findAll());
        model.addAttribute("wrapper", wrapper);
        model.addAttribute("tags", tags);
        return "bulk-edit-tags";
    }


    @PostMapping("/bulk-update-tags")
    public String updateTagsBulk(@RequestParam("tagPairs") String[] tagPairs) {
        // Сборка Map<cardId, List<tagId>>
        Map<Long, List<Long>> cardTagsMap = new HashMap<>();

        for (String pair : tagPairs) {
            String[] parts = pair.split("]");
            if (parts.length != 2) continue;

            Long cardId = Long.valueOf(parts[0]);
            Long tagId = Long.valueOf(parts[1]);

            cardTagsMap.computeIfAbsent(cardId, k -> new ArrayList<>()).add(tagId);
        }

        // Обновляем карты
        for (Map.Entry<Long, List<Long>> entry : cardTagsMap.entrySet()) {
            cardService.updateCardTags(entry.getKey(), entry.getValue());
        }

        return "redirect:/cards";
    }




    public Long extractCardId(String key) {
        Pattern pattern = Pattern.compile("tagsMap\\[(\\d+)]");
        Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) {
            return Long.valueOf(matcher.group(1));
        }
        return null; // или выбросить исключение, если нужно
    }

}

