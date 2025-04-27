package adlist.Controller;

import adlist.Entity.Card;
import adlist.Repository.CardRepository;
import adlist.Service.CardService;
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

@Controller
@RequestMapping("/cards")
public class CardController {

    private final CardRepository cardRepository;

    private final CardService cardService;

    public CardController(CardRepository cardRepository, CardService cardService) {
        this.cardRepository = cardRepository;
        this.cardService = cardService;
    }

    // Вывод всех карточек
    @GetMapping
    public String getAllCards(Model model) {
        model.addAttribute("cards", cardRepository.findAll());
        model.addAttribute("uploadDir", "/fileSystem/"); // Важно поставить слэш на конце
        return "cards"; // шаблон для отображения списка карточек
    }

    // Форма создания новой карточки
    @GetMapping("/new")
    public String createCardForm(Model model) {
        model.addAttribute("newCard", new Card());
        return "createCard"; // шаблон для формы создания карточки
    }

    // Создание карточки с загрузкой изображения
    @PostMapping("/new")
    public String createCard(
            @ModelAttribute("newCard") Card card,
            @RequestParam("imageFile") MultipartFile imageFile) {
        if (!imageFile.isEmpty()) {
            try {
                card.setImageUrl(cardService.uploadImageToFileSystem(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cardRepository.save(card);
        return "redirect:/cards"; // перенаправление на страницу со списком карточек
    }

    // Удаление карточки
    @GetMapping("/delete/{id}")
    public String deleteCard(@PathVariable("id") Long id) {
        cardRepository.deleteById(id);
        return "redirect:/cards";
    }
}

