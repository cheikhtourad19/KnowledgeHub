package tn.esprim.knowledgehub.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.repository.StudentRepository;
import tn.esprim.knowledgehub.user.repository.TeacherRepository;

import java.util.List;

@Service
public class TeacherService {

    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    StudentRepository studentRepository;

    public Teacher saveTeacher(Teacher teacher){
        return teacherRepository.save(teacher);
    }

    public List<Teacher> saveTeachers(List<Teacher> teachers){
        return teacherRepository.saveAll(teachers);
    }


    public Teacher getTeacherById(Long id){
        return teacherRepository.findById(id).orElseThrow(()->new RuntimeException("Teacher not found with id: "+id));
    }
    public Teacher getTeacherByAuthId(Long authId){
        return teacherRepository.findByAuthId(String.valueOf(authId)).orElseThrow(()->new RuntimeException("Teacher not found with authId: "+authId));
    }

    public List<Teacher> getAllTeachers(){
        return teacherRepository.findAll();
    }

}
