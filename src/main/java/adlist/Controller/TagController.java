package adlist.Controller;

import adlist.Entity.Card;
import adlist.Entity.Tag;
import adlist.Repository.CardRepository;
import adlist.Repository.TagRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/tags")
public class TagController {
    private final TagRepository tagRepository;
    private final CardRepository cardRepository;

    public TagController(TagRepository tagRepository, CardRepository cardRepository) {
        this.tagRepository = tagRepository;
        this.cardRepository = cardRepository;
    }

    @GetMapping
    public String getTags(Model model) {
        model.addAttribute("tags", tagRepository.findAll());
        model.addAttribute("newTag", new Tag()); // Для формы
        return "tags";
    }

    @PostMapping
    public String createTag(@ModelAttribute("newTag") Tag tag, Model model) {
        if (tag.getName() != null && !tag.getName().trim().isEmpty()) {
            boolean exists = tagRepository.existsByName(tag.getName().trim());
            if (!exists) {
                tagRepository.save(tag);
            } else {
                model.addAttribute("error", "Tag already exists!");
            }
        }
        return "redirect:/tags";
    }

    @PostMapping("/delete/{id}")
    public String deleteTag(@PathVariable("id") Long id) {
        Tag tag = tagRepository.findById(id).orElse(null);
        if (tag != null) {
            // Удалим связь тега с карточками
            List<Card> cardsWithTag = cardRepository.findByTagsContaining(tag);
            for (Card card : cardsWithTag) {
                card.getTags().remove(tag);
            }
            cardRepository.saveAll(cardsWithTag); // Сохраняем изменения
            tagRepository.delete(tag); // Удаляем тег
        }
        return "redirect:/tags";
    }


    @PostMapping("/rename/{id}")
    public String renameTag(@PathVariable("id") Long id, @RequestParam("name") String newName) {
        Tag tag = tagRepository.findById(id).orElse(null);
        if (tag != null && newName != null && !newName.trim().isEmpty()) {
            tag.setName(newName.trim());
            tagRepository.save(tag);
        }
        return "redirect:/tags";
    }

}

