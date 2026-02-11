package tn.esprim.knowledgehub.resource.controller.public_;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.service.LibraryService;

@RestController
@RequestMapping("/api/public/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    // TODO: Implement library endpoints
}
