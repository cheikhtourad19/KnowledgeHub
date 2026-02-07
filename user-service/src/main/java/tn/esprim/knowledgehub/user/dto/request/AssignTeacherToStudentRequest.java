package tn.esprim.knowledgehub.user.dto.request;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssignTeacherToStudentRequest {
    private Long teacherId;
    private Long studentId;
}
