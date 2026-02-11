package tn.esprim.knowledgehub.resource.controller.teacher;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.service.DocumentService;

@RestController
@RequestMapping("/api/teacher/documents")
@RequiredArgsConstructor
public class TeacherDocumentController {

    private final DocumentService documentService;

    // TODO: Implement teacher document endpoints
}
