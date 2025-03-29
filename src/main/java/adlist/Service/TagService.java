package adlist.Service;

import adlist.Entity.Tag;
import adlist.Repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Tag createTag(String name) {
        if (tagRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Tag already exists");
        }
        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }
}

