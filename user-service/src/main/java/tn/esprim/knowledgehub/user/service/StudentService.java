package tn.esprim.knowledgehub.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.user.entity.Classe;
import tn.esprim.knowledgehub.user.entity.Student;
import tn.esprim.knowledgehub.user.entity.Teacher;
import tn.esprim.knowledgehub.user.repository.ClasseRepository;
import tn.esprim.knowledgehub.user.repository.StudentRepository;
import tn.esprim.knowledgehub.user.repository.TeacherRepository;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository  studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private ClasseRepository classeRepository;


    public Student saveStudent(Student student){
        return studentRepository.save(student);
    }
    public List<Student> saveStudents(List<Student> students){
        return studentRepository.saveAll(students);
    }
    public void deleteClasse(Long id){
        List<Student> students = getStudentsInClasse(id);
        students.forEach(student -> student.setClasse(null));
        studentRepository.saveAll(students);
        classeRepository.deleteById(id);
    }
    public void deleteStudent(Long id){
        Student student = studentRepository.findByAuthId(String.valueOf(id)).orElseThrow(()->new RuntimeException("Student not found with id: "+id));
        studentRepository.deleteById(student.getStudentId());
    }
    public List<Student> getAllStudents(){
        return studentRepository.findAllWithClasse();
    }
    public Classe createClasse(Classe classe){
        return classeRepository.save(classe);
    }
    public Student addStudentToClasse(Long studentId, Long classeId){
        Student student = studentRepository.findById(studentId).orElseThrow(()->new RuntimeException("Student not found with id: "+studentId));
        Classe classe = classeRepository.findById(classeId).orElseThrow(()->new RuntimeException("could not add student "+studentId +" because Classe not found with id: "+classeId));
        student.setClasse(classe);
        classe.getStudents().add(student);
        classeRepository.save(classe);
        return studentRepository.save(student);
    }
    public List<Classe> getAllClasses(){
        return classeRepository.findAll();
    }
    public Classe getClasseById(Long classeId){
        return classeRepository.findById(classeId).orElseThrow(()->new RuntimeException("Classe not found with id: "+classeId));
    }
    public List<Student> getStudentsInClasse(Long classeId){
        return classeRepository.findById(classeId).orElseThrow(()->new RuntimeException("Classe not found with id: "+classeId)).getStudents();
    }

    public Student getStudentByAuthId(String authId) {
        return studentRepository.findByAuthId(authId)
                .orElseThrow(() -> new RuntimeException("Student not found with authId: " + authId));
    }

    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found with email: " + email));
    }

    public Student getStudentById(Long id){
        return studentRepository.findByIdWithClasse(id).orElseThrow(()->new RuntimeException("Student not found with id: "+id));
    }


}
