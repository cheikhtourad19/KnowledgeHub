package tn.esprim.knowledgehub.resource.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.service.PFAService;

@RestController
@RequestMapping("/api/student/pfa")
@RequiredArgsConstructor
public class StudentPFAController {

    private final PFAService pfaService;

    // TODO: Implement PFA controller endpoints
}
