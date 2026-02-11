package tn.esprim.knowledgehub.resource.controller.teacher;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.service.PFAService;
import tn.esprim.knowledgehub.resource.service.PFEService;
import tn.esprim.knowledgehub.resource.service.StageService;

@RestController
@RequestMapping("/api/teacher/review")
@RequiredArgsConstructor
public class TeacherReviewController {

    private final PFAService pfaService;
    private final PFEService pfeService;
    private final StageService stageService;

    // TODO: Implement teacher review endpoints
}
