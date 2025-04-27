package adlist.Controller;

import adlist.Entity.Card;
import adlist.Entity.Tag;
import adlist.Repository.CardRepository;
import adlist.Repository.TagRepository;
import adlist.Service.CardService;
import adlist.Service.TagService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/cards")
public class CardController {

    private final CardRepository cardRepository;

    private final CardService cardService;

    private final TagRepository tagRepository;
    private final TagService tagService;

    public CardController(CardRepository cardRepository, CardService cardService, TagRepository tagRepository, TagService tagService) {
        this.cardRepository = cardRepository;
        this.cardService = cardService;
        this.tagRepository = tagRepository;
        this.tagService = tagService;
    }

    // Вывод всех карточек
    @GetMapping
    public String getAllCards(Model model) {
        model.addAttribute("cards", cardRepository.findAll());
        model.addAttribute("uploadDir", "/fileSystem/");
        model.addAttribute("tags", tagRepository.findAll());
        return "cards"; // шаблон для отображения списка карточек
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


    // Удаление карточки
    @GetMapping("/delete/{id}")
    public String deleteCard(@PathVariable("id") Long id) {
        cardRepository.deleteById(id);
        return "redirect:/cards";
    }
}

