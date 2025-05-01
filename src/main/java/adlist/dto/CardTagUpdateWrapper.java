package adlist.dto;

import lombok.Data;

import java.util.List;

@Data
public class CardTagUpdateWrapper {
    private List<CardTagUpdateDTO> cards;

    // геттеры и сеттеры
}
