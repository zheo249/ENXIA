/* ============================================================
   STUDENT PERFORMANCE PREDICTOR
   Frontend: app.js (Incluye HU-01, HU-03, HU-04 y HU-05)
   ============================================================ */

const form = document.getElementById("student-form");
const studentIdInput = document.getElementById("student-id");
const nameInput = document.getElementById("student-name");
const studyHoursInput = document.getElementById("study-hours");
const studentSubjectsContainer = document.getElementById("student-subjects-container");
const formTitle = document.getElementById("form-title");
const cancelEditButton = document.getElementById("cancel-edit");
const studentList = document.getElementById("student-list");
const message = document.getElementById("message");

const subjectForm = document.getElementById("subject-form");
const subjectNameInput = document.getElementById("subject-name");
const subjectDifficultySelect = document.getElementById("subject-difficulty");
const subjectMessage = document.getElementById("subject-message");
const subjectList = document.getElementById("subject-list");

let currentStudents = [];
let currentSubjects = [];

const SUBJECT_DIFFICULTIES = ["EASY", "MEDIUM", "HARD"];
const SUBJECT_LABELS = {
    EASY: "Fácil",
    MEDIUM: "Media",
    HARD: "Difícil"
};

form.addEventListener("submit", saveStudent);
cancelEditButton.addEventListener("click", resetForm);
subjectForm.addEventListener("submit", saveSubject);

// Carga inicial
loadSubjects();
loadStudents();


// ============================================================
// 1. ESTUDIANTES Y MATRÍCULAS (HU-04)
// ============================================================

async function loadStudents() {
    try {
        const response = await fetch("/students");
        const students = await response.json();
        currentStudents = students;
        renderStudents(students);
    } catch (error) {
        showMessage("Could not load students.", true);
    }
}

function renderStudentSubjectCheckboxes(subjects, selectedEnrollments = []) {
    studentSubjectsContainer.replaceChildren();

    if (!subjects || subjects.length === 0) {
        const empty = document.createElement("p");
        empty.className = "muted";
        empty.textContent = "No subjects registered yet. Add subjects below first.";
        studentSubjectsContainer.appendChild(empty);
        return;
    }

    const enrollmentMap = new Map();
    for (const enr of selectedEnrollments) {
        enrollmentMap.set(enr.subjectId, enr.studyHours);
    }

    for (const subject of subjects) {
        const row = document.createElement("div");
        row.className = "enrollment-row";

        const label = document.createElement("label");
        label.className = "enrollment-label";

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.className = "subject-enrollment-checkbox";
        checkbox.value = String(subject.id);

        const isSelected = enrollmentMap.has(subject.id);
        checkbox.checked = isSelected;

        const diffLabel = SUBJECT_LABELS[subject.difficulty] || subject.difficulty;
        const textSpan = document.createElement("span");
        textSpan.textContent = `${subject.name} (${diffLabel})`;

        label.appendChild(checkbox);
        label.appendChild(textSpan);

        const hoursInput = document.createElement("input");
        hoursInput.type = "number";
        hoursInput.min = "0";
        hoursInput.step = "0.5";
        hoursInput.placeholder = "Hours";
        hoursInput.className = "enrollment-hours-input";
        hoursInput.id = `enrollment-hours-${subject.id}`;
        hoursInput.disabled = !isSelected;
        if (isSelected) {
            hoursInput.value = String(enrollmentMap.get(subject.id));
        }

        checkbox.addEventListener("change", () => {
            hoursInput.disabled = !checkbox.checked;
            if (checkbox.checked) {
                if (hoursInput.value === "") {
                    hoursInput.value = "0";
                }
                hoursInput.focus();
            } else {
                hoursInput.value = "";
            }
            updateGeneralHoursFromEnrollments();
        });

        hoursInput.addEventListener("input", updateGeneralHoursFromEnrollments);

        row.appendChild(label);
        row.appendChild(hoursInput);
        studentSubjectsContainer.appendChild(row);
    }
}

function updateGeneralHoursFromEnrollments() {
    const checkboxes = document.querySelectorAll(".subject-enrollment-checkbox:checked");
    if (checkboxes.length === 0) {
        return;
    }
    let total = 0;
    checkboxes.forEach(cb => {
        const hoursEl = document.getElementById(`enrollment-hours-${cb.value}`);
        const val = parseFloat(hoursEl.value);
        if (!isNaN(val) && val >= 0) {
            total += val;
        }
    });
    studyHoursInput.value = String(total);
}

function collectEnrollmentsFromForm() {
    const checkboxes = document.querySelectorAll(".subject-enrollment-checkbox:checked");
    const enrollments = [];

    for (const cb of checkboxes) {
        const subjectId = Number(cb.value);
        const hoursEl = document.getElementById(`enrollment-hours-${subjectId}`);
        const rawVal = hoursEl.value.trim();
        const studyHours = Number(rawVal);

        if (rawVal === "" || isNaN(studyHours) || studyHours < 0) {
            showMessage("Study hours for each selected subject must be a non-negative number.", true);
            hoursEl.focus();
            return null;
        }

        enrollments.push({
            subjectId: subjectId,
            studyHours: studyHours
        });
    }

    return enrollments;
}

async function saveStudent(event) {
    event.preventDefault();

    const name = nameInput.value.trim();
    if (name === "") {
        showMessage("Student name is required.", true);
        return;
    }

    const enrollments = collectEnrollmentsFromForm();
    if (enrollments === null) {
        return;
    }

    const generalHours = Number(studyHoursInput.value);
    if (isNaN(generalHours) || generalHours < 0) {
        showMessage("Study hours cannot be negative.", true);
        return;
    }

    const id = studentIdInput.value;
    const student = {
        name: name,
        studyHours: generalHours,
        enrollments: enrollments
    };

    const url = id === "" ? "/students" : "/students/" + id;
    const method = id === "" ? "POST" : "PUT";

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(student)
        });

        if (!response.ok) {
            await showApiError(response);
            return;
        }

        resetForm();
        showMessage("Student saved.", false);
        await loadStudents();

    } catch (error) {
        showMessage("Could not save the student.", true);
    }
}

function renderStudents(students) {
    studentList.replaceChildren();

    if (students.length === 0) {
        const emptyMessage = document.createElement("p");
        emptyMessage.textContent = "No students yet.";
        studentList.appendChild(emptyMessage);
        return;
    }

    for (let index = 0; index < students.length; index++) {
        const student = students[index];

        const card = document.createElement("article");
        card.className = "student-card";

        const name = document.createElement("strong");
        name.textContent = student.name;

        const hours = document.createElement("p");
        hours.textContent = "Total study hours: " + student.studyHours;

        card.appendChild(name);
        card.appendChild(hours);

        // Mostrar asignaturas matriculadas y horas por asignatura (HU-04)
        if (student.enrollments && student.enrollments.length > 0) {
            const enrTitle = document.createElement("p");
            enrTitle.textContent = "Enrolled subjects:";
            card.appendChild(enrTitle);

            const ul = document.createElement("ul");
            ul.className = "enrollment-list";
            for (const enr of student.enrollments) {
                const li = document.createElement("li");
                const diffText = SUBJECT_LABELS[enr.difficulty] || enr.difficulty || "";
                li.textContent = `${enr.subjectName} (${diffText}) — ${enr.studyHours} h`;
                ul.appendChild(li);
            }
            card.appendChild(ul);
        } else {
            const noSubjects = document.createElement("p");
            noSubjects.className = "muted";
            noSubjects.textContent = "No subjects enrolled.";
            card.appendChild(noSubjects);
        }

        const prediction = document.createElement("p");
        prediction.className = "prediction";
        prediction.id = "prediction-" + student.id;

        const actions = document.createElement("div");
        actions.className = "student-actions";

        actions.appendChild(createButton("Edit", student.id, startEdit));
        actions.appendChild(createButton("Delete", student.id, deleteStudent));
        actions.appendChild(createButton("Predict grade", student.id, requestPrediction));

        card.appendChild(prediction);
        card.appendChild(actions);
        studentList.appendChild(card);
    }
}

function createButton(label, studentId, clickHandler) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.dataset.studentId = String(studentId);
    button.addEventListener("click", clickHandler);
    return button;
}

function findStudentById(studentId) {
    for (let index = 0; index < currentStudents.length; index++) {
        if (String(currentStudents[index].id) === studentId) {
            return currentStudents[index];
        }
    }
    return null;
}

function startEdit(event) {
    const studentId = event.currentTarget.dataset.studentId;
    const student = findStudentById(studentId);

    if (student === null) {
        showMessage("Student not found.", true);
        return;
    }

    studentIdInput.value = student.id;
    nameInput.value = student.name;
    studyHoursInput.value = student.studyHours;
    renderStudentSubjectCheckboxes(currentSubjects, student.enrollments || []);

    formTitle.textContent = "Edit student";
    cancelEditButton.classList.remove("hidden");
    nameInput.focus();
}

async function deleteStudent(event) {
    const studentId = event.currentTarget.dataset.studentId;

    try {
        const response = await fetch("/students/" + studentId, {
            method: "DELETE"
        });

        if (!response.ok) {
            await showApiError(response);
            return;
        }

        showMessage("Student deleted.", false);
        await loadStudents();
    } catch (error) {
        showMessage("Could not delete the student.", true);
    }
}

async function requestPrediction(event) {
    const studentId = event.currentTarget.dataset.studentId;
    const result = document.getElementById("prediction-" + studentId);
    result.textContent = "Requesting prediction...";

    try {
        const response = await fetch("/students/" + studentId + "/prediction");
        if (!response.ok) {
            const errorData = await response.json();
            result.textContent = errorData.error;
            return;
        }

        const prediction = await response.json();
        result.replaceChildren();

        if (prediction.predictions && prediction.predictions.length > 0) {
            const title = document.createElement("strong");
            title.textContent = "Predicted grades by subject:";
            result.appendChild(title);

            const ul = document.createElement("ul");
            ul.className = "enrollment-list";
            for (const item of prediction.predictions) {
                const li = document.createElement("li");
                const diffLabel = SUBJECT_LABELS[item.difficulty] || item.difficulty;
                li.textContent = `${item.subjectName} (${diffLabel}, ${item.studyHours} h): ${item.predictedGrade}`;
                ul.appendChild(li);
            }
            result.appendChild(ul);
        } else {
            result.textContent = "Predicted grade: " + prediction.predictedGrade;
        }
    } catch (error) {
        result.textContent = "Could not request a prediction.";
    }
}

function resetForm() {
    form.reset();
    studentIdInput.value = "";
    studyHoursInput.value = "0";
    renderStudentSubjectCheckboxes(currentSubjects, []);
    formTitle.textContent = "Add student";
    cancelEditButton.classList.add("hidden");
}

async function showApiError(response) {
    const errorData = await response.json();
    showMessage(errorData.error, true);
}

function showMessage(text, isError) {
    message.textContent = text;
    if (isError) {
        message.classList.add("error");
    } else {
        message.classList.remove("error");
    }
}


// ============================================================
// 2. ASIGNATURAS (HU-03 + sincronización con HU-04)
// ============================================================

async function saveSubject(event) {
    event.preventDefault();

    if (!validateSubjectForm()) {
        return;
    }

    const subject = {
        name: subjectNameInput.value.trim(),
        difficulty: subjectDifficultySelect.value
    };

    try {
        const response = await fetch("/subjects", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(subject)
        });

        if (!response.ok) {
            await showSubjectApiError(response);
            return;
        }

        subjectForm.reset();
        showSubjectMessage("Subject saved.", false);
        await loadSubjects();
    } catch (error) {
        showSubjectMessage("Could not save the subject.", true);
    }
}

function validateSubjectForm() {
    if (subjectNameInput.value.trim() === "") {
        showSubjectMessage("Name is required.", true);
        return false;
    }
    if (!SUBJECT_DIFFICULTIES.includes(subjectDifficultySelect.value)) {
        showSubjectMessage("Select a difficulty.", true);
        return false;
    }
    return true;
}

function showSubjectMessage(text, isError) {
    subjectMessage.textContent = text;
    if (isError) {
        subjectMessage.classList.add("error");
    } else {
        subjectMessage.classList.remove("error");
    }
}

async function showSubjectApiError(response) {
    try {
        const errorData = await response.json();
        showSubjectMessage(errorData.error, true);
    } catch (error) {
        showSubjectMessage("Unexpected error (HTTP " + response.status + ").", true);
    }
}

function renderSubjects(subjects) {
    subjectList.replaceChildren();

    if (subjects.length === 0) {
        const emptyMessage = document.createElement("p");
        emptyMessage.textContent = "No subjects yet.";
        subjectList.appendChild(emptyMessage);
        return;
    }

    for (let index = 0; index < subjects.length; index++) {
        const subject = subjects[index];

        const card = document.createElement("article");
        card.className = "student-card";

        const name = document.createElement("strong");
        name.textContent = subject.name;

        const difficulty = document.createElement("p");
        difficulty.textContent = "Difficulty: " +
            (SUBJECT_LABELS[subject.difficulty] || subject.difficulty);

        card.appendChild(name);
        card.appendChild(difficulty);
        subjectList.appendChild(card);
    }
}

async function loadSubjects() {
    try {
        const response = await fetch("/subjects");
        if (!response.ok) {
            await showSubjectApiError(response);
            return;
        }

        const subjects = await response.json();
        currentSubjects = subjects;
        renderSubjects(subjects);
        renderStudentSubjectCheckboxes(subjects);
    } catch (error) {
        showSubjectMessage("Could not load subjects.", true);
    }
}
