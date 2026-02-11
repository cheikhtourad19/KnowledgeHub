package tn.esprim.knowledgehub.resource.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.service.PFEService;

@RestController
@RequestMapping("/api/student/pfe")
@RequiredArgsConstructor
public class StudentPFEController {

    private final PFEService pfeService;

    // TODO: Implement PFE controller endpoints
}
