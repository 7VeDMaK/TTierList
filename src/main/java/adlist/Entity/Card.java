package adlist.Entity;

import adlist.Enum.Rank;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rank rank;

    @Column(name = "position")
    private int position; // 🔥 Добавлено поле позиции

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "source_link")
    private String sourceLink;

    @Lob
    private String description;

    @ManyToMany
    @JoinTable(
            name = "card_tags",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
