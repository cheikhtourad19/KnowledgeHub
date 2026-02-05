package tn.esprim.knowledgehub.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.repository.TeacherRepository;

import java.util.List;

@Service
public class TeacherService {

    @Autowired
    TeacherRepository teacherRepository;

    public Teacher saveTeacher(Teacher teacher){
        return teacherRepository.save(teacher);
    }

    public List<Teacher> saveTeachers(List<Teacher> teachers){
        return teacherRepository.saveAll(teachers);
    }
}
