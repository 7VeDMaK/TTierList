package adlist.Repository;

import adlist.Entity.Card;
import adlist.Entity.Tag;
import adlist.Enum.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    boolean existsByName(String name);

    List<Card> findAllByRankOrderByPositionAsc(Rank rank);

    @Modifying
    @Query("UPDATE Card c SET c.rank = :rank, c.position = :position WHERE c.id = :id")
    void updateRankAndPosition(@Param("id") Long id, @Param("rank") Rank rank, @Param("position") int position);

    List<Card> findByRankOrderByPosition(Rank rank);

    List<Card> findByTagsContaining(Tag tag);



}