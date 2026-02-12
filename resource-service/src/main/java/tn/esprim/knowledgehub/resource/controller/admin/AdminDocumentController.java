package tn.esprim.knowledgehub.resource.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.service.DocumentService;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final DocumentService documentService;


}
