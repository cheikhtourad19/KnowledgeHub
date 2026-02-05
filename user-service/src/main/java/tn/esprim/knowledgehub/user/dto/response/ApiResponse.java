package tn.esprim.knowledgehub.user.dto.response;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiResponse<T> {
    private String message;
    private boolean success;
    private T data;
}
