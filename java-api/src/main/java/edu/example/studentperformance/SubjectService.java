/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.example.studentperformance;
import java.util.List;
/**
 *
 * @author Acer
 */

public class SubjectService {

    private final SubjectRepository repository;

    public SubjectService(SubjectRepository repository) {
        this.repository = repository;
    }

    public List<Subject> getAllSubjects() {
        return repository.findAll();
    }

    public Subject createSubject(Subject subject) {

        if (subject == null) {
            throw new IllegalArgumentException("La asignatura no puede estar vacía");
        }

        if (subject.getName() == null || subject.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (!"EASY".equals(subject.getDifficulty())
                && !"MEDIUM".equals(subject.getDifficulty())
                && !"HARD".equals(subject.getDifficulty())) {
            throw new IllegalArgumentException("La dificultad debe ser fácil, media o difícil");
        }

        return repository.create(subject);
    }
}
