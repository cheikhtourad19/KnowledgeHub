package tn.esprim.knowledgehub.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.repository.StudentRepository;
import tn.esprim.knowledgehub.user.repository.TeacherRepository;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository  studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;



    public Student saveStudent(Student student){
        return studentRepository.save(student);
    }
    public List<Student> saveStudents(List<Student> students){
        return studentRepository.saveAll(students);
    }
    public List<Student> getAllStudents(){
        return studentRepository.findAll();
    }
    public Student deAssignTeacherFromStudent(Long teacherId, Long studentId){
        Student student = studentRepository.findById(studentId).orElseThrow(
                ()->new RuntimeException("Student not found with id: "+studentId)
        );
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
                ()->new RuntimeException("Teacher not found with id: "+teacherId)
        );

        if(!student.getEncandrants().contains(teacher)){
            return student;
        }
        student.getEncandrants().remove(teacher);
        return studentRepository.save(student);
    }
    public Student assignTeacherToStudent(Long teacherId, Long studentId){
        Student student = studentRepository.findById(studentId).orElseThrow(
                ()->new RuntimeException("Student not found with id: "+studentId)
        );
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(
                ()->new RuntimeException("Teacher not found with id: "+teacherId)
        );

        if(student.getEncandrants().contains(teacher)){
            return student;
        }
        student.getEncandrants().add(teacher);
        return studentRepository.save(student);
    }

    public Student dessignAllTeacherFromStudent(Long studentId){
        Student student = studentRepository.findById(studentId).orElseThrow(
                ()->new RuntimeException("Student not found with id: "+studentId)
        );
        student.getEncandrants().clear();
        return studentRepository.save(student);
    }




}
