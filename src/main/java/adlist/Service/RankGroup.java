package adlist.Service;

import adlist.Entity.Card;
import adlist.Enum.Rank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RankGroup {
    private Rank rank;
    private List<Card> cards;

    // Конструктор, геттеры, сеттеры
}
