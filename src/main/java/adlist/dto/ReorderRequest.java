package adlist.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReorderRequest {
    private Map<String, List<Long>> rankMap;
}
