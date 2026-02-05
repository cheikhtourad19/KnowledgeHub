package tn.esprim.knowledgehub.user.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.repository.ClasseRepository;
import tn.esprim.knowledgehub.user.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository  studentRepository;
    @Autowired
    private ClasseRepository classeRepository;

    public Student saveStudent(Student student,Long classeId){
        return studentRepository.save(student);
    }


}
