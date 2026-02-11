package tn.esprim.knowledgehub.resource.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.service.StageService;

@RestController
@RequestMapping("/api/student/stage")
@RequiredArgsConstructor
public class StudentStageController {

    private final StageService stageService;

    // TODO: Implement Stage controller endpoints
}
