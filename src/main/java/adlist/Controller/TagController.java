package adlist.Controller;

import adlist.Entity.Tag;
import adlist.Repository.TagRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tags")
public class TagController {
    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
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
    public String deleteTag(@PathVariable Long id) {
        tagRepository.deleteById(id);
        return "redirect:/tags";
    }
}

