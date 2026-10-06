package adlist.Repository;

import adlist.Entity.Card;
import adlist.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByTagsContaining(Tag tag);
}
