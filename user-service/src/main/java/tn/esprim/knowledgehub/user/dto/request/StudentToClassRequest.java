package tn.esprim.knowledgehub.user.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentToClassRequest {
    private Long classId;
    private Long authId;
}

