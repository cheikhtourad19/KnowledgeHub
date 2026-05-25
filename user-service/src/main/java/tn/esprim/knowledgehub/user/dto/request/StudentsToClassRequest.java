package tn.esprim.knowledgehub.user.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentsToClassRequest {
    private Long classId;
    private List<Long> authIds;
}
