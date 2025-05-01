package adlist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardTagUpdateDTO {
    private Long cardId;
    private List<Long> tagIds;
}
