/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.example.studentperformance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Acer
 */

// Guarda nuevas asignaturas y consulta las q ya están registradas
public class SubjectRepository {

    private final Map<Integer, Subject> subjects;
    private int nextId;

    public SubjectRepository() {
        subjects = new HashMap<Integer, Subject>();
        nextId = 1;
    }
    
    // devuelve todas las asignaturas
    public List<Subject> findAll() {
        return new ArrayList<Subject>(subjects.values());
    }

    // registra una nueva asignatura y le asigna un id automaticamente
    public Subject create(Subject subject) {
        subject.setId(nextId);
        nextId = nextId + 1;
        subjects.put(subject.getId(), subject);
        return subject;
    }
}

