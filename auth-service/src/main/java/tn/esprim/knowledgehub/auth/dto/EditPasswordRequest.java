package tn.esprim.knowledgehub.auth.dto;

@lombok.Data
public class EditPasswordRequest {

    private String oldPassword;
    private String newPassword;
}
