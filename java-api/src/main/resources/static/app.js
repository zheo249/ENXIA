/* ============================================================
   STUDENT PERFORMANCE PREDICTOR
   Frontend: app.js (Incluye HU-01, HU-03, HU-04 y HU-05)
   ============================================================ */

const form = document.getElementById("student-form");
const studentIdInput = document.getElementById("student-id");
const nameInput = document.getElementById("student-name");
const studyHoursInput = document.getElementById("study-hours");
const studentSubjectsContainer = document.getElementById("student-subjects-container");
const studentCourseSelect = document.getElementById("student-course");
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
studentCourseSelect.addEventListener("change", () => {
    if (studentIdInput.value === "") {
        return;
    }
    const student = findStudentById(studentIdInput.value);
    const courseId = Number(studentCourseSelect.value);
    const inCourse = ((student && student.enrollments) || []).filter(e => e.courseId === courseId);
    renderStudentSubjectCheckboxes(currentSubjects, inCourse);
});
subjectForm.addEventListener("submit", saveSubject);

// Carga inicial
loadSubjects();
loadStudents();
loadCourses(); // Para la HU-01 (gestión de cursos académicos) practica 7



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

function renderCourseOptions() {
    const previous = studentCourseSelect.value;
    studentCourseSelect.replaceChildren();
    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "-- Select --";
    studentCourseSelect.appendChild(placeholder);
    for (const course of currentCourses) {
        const option = document.createElement("option");
        option.value = String(course.id);
        option.textContent = course.name;
        studentCourseSelect.appendChild(option);
    }
    studentCourseSelect.value = previous;
}

function collectEnrollmentsFromForm() {
    const checkboxes = document.querySelectorAll(".subject-enrollment-checkbox:checked");
    const enrollments = [];
    const courseId = Number(studentCourseSelect.value);

    if (checkboxes.length > 0 && !courseId) {
        showMessage("Select the academic course for the chosen subjects.", true);
        studentCourseSelect.focus();
        return null;
    }

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
            courseId: courseId,
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
    let allEnrollments = enrollments;
    if (id !== "") {
        const existing = (findStudentById(id) || {}).enrollments || [];
        const selectedCourse = Number(studentCourseSelect.value);
        for (const enrollment of enrollments) {
            const previous = existing.find(e =>
                e.subjectId === enrollment.subjectId && e.courseId === enrollment.courseId);
            if (previous && previous.grade !== null && previous.grade !== undefined) {
                enrollment.grade = previous.grade;
            }
        }
        const otherCourses = existing
            .filter(enrollment => enrollment.courseId !== selectedCourse)
            .map(enrollment => ({
                subjectId: enrollment.subjectId,
                courseId: enrollment.courseId,
                studyHours: enrollment.studyHours,
                grade: enrollment.grade
            }));
        allEnrollments = otherCourses.concat(enrollments);
    }

    const student = {
        name: name,
        studyHours: generalHours,
        enrollments: allEnrollments
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
                const courseText = enr.courseName ? ` [${enr.courseName}]` : "";
                const gradeText = (enr.grade !== null && enr.grade !== undefined) ? ` — grade ${enr.grade}` : "";
                li.textContent = `${enr.subjectName}${courseText} (${diffText}) — ${enr.studyHours} h${gradeText}`;
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
    const studentEnrollments = student.enrollments || [];
    const lastCourseId = studentEnrollments.length > 0
        ? studentEnrollments[studentEnrollments.length - 1].courseId
        : "";
    studentCourseSelect.value = lastCourseId === "" ? "" : String(lastCourseId);
    renderStudentSubjectCheckboxes(currentSubjects,
        studentEnrollments.filter(enrollment => enrollment.courseId === lastCourseId));

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
    studentCourseSelect.value = "";
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

// ============================================================
// 3. GESTIÓN DE CURSOS ACADÉMICOS (HU-01) para practica 7
// ============================================================

const courseForm = document.getElementById("course-form");
const courseIdInput = document.getElementById("course-id");
const courseNameInput = document.getElementById("course-name");
const courseStartDateInput = document.getElementById("course-start-date");
const courseEndDateInput = document.getElementById("course-end-date");
const courseFormTitle = document.getElementById("course-form-title");
const cancelCourseEditButton = document.getElementById("cancel-course-edit");
const courseMessage = document.getElementById("course-message");
const courseList = document.getElementById("course-list");

let currentCourses = [];

courseForm.addEventListener("submit", saveCourse);
cancelCourseEditButton.addEventListener("click", resetCourseForm);

// Recuerda añadir también loadCourses(); arriba donde se llaman loadSubjects() y loadStudents()

// Convierte "YYYY-MM-DD" (del <input type="date">) a "DD-MM-YYYY" (para enviar al backend)
function formatToDDMMYYYY(isoDate) {
    if (!isoDate || !isoDate.includes("-")) return isoDate;
    const [year, month, day] = isoDate.split("-");
    return `${day}-${month}-${year}`;
}

// Convierte "DD-MM-YYYY" o "DD/MM/YYYY" (que llega del backend) a "YYYY-MM-DD" (para el <input type="date">)
function formatToYYYYMMDD(dmyDate) {
    if (!dmyDate) return dmyDate;
    const separator = dmyDate.includes("-") ? "-" : (dmyDate.includes("/") ? "/" : null);
    if (!separator) return dmyDate;
    const parts = dmyDate.split(separator);
    if (parts[0].length === 4) return dmyDate; // Ya viene en YYYY-MM-DD
    const [day, month, year] = parts;
    return `${year}-${month}-${day}`;
}

async function loadCourses() {
    try {
        const response = await fetch("/courses");
        if (!response.ok) {
            await showCourseApiError(response);
            return;
        }
        const courses = await response.json();
        currentCourses = courses;
        renderCourseOptions();
        renderCourses(courses);
    } catch (error) {
        showCourseMessage("Could not load academic courses.", true);
    }
}

function validateCourseForm() {
    const name = courseNameInput.value.trim();
    const startDate = courseStartDateInput.value;
    const endDate = courseEndDateInput.value;

    if (!name || !startDate || !endDate) {
        showCourseMessage("All fields (name, start date, end date) are required.", true);
        return false;
    }

    // Se compara en formato YYYY-MM-DD antes de convertir, para que la ordenación sea cronológica
    if (endDate <= startDate) {
        showCourseMessage("End date must be after start date.", true);
        return false;
    }

    return true;
}

async function saveCourse(event) {
    event.preventDefault();

    if (!validateCourseForm()) {
        return;
    }

    const id = courseIdInput.value;
    const course = {
        name: courseNameInput.value.trim(),
        // El backend Java espera fechas aaaa-mm-dd, justo el valor de <input type="date">.
        startDate: courseStartDateInput.value,
        endDate: courseEndDateInput.value
    };

    const url = id === "" ? "/courses" : "/courses/" + id;
    const method = id === "" ? "POST" : "PUT";

    try {
        const response = await fetch(url, {
            method: method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(course)
        });

        if (!response.ok) {
            await showCourseApiError(response);
            return;
        }

        resetCourseForm();
        showCourseMessage("Academic course saved.", false);
        await loadCourses();
    } catch (error) {
        showCourseMessage("Could not save the academic course.", true);
    }
}

function renderCourses(courses) {
    courseList.replaceChildren();

    if (courses.length === 0) {
        const emptyMessage = document.createElement("p");
        emptyMessage.textContent = "No academic courses yet.";
        courseList.appendChild(emptyMessage);
        return;
    }

    for (const course of courses) {
        const card = document.createElement("article");
        card.className = "student-card";

        const name = document.createElement("strong");
        name.textContent = course.name;

        const dates = document.createElement("p");
        // La API devuelve aaaa-mm-dd; al usuario se la mostramos como dd-mm-aaaa.
        dates.textContent = `From ${formatToDDMMYYYY(course.startDate)} to ${formatToDDMMYYYY(course.endDate)}`;

        const actions = document.createElement("div");
        actions.className = "student-actions";

        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.textContent = "Edit";
        editBtn.addEventListener("click", () => startEditCourse(course));

        const deleteBtn = document.createElement("button");
        deleteBtn.type = "button";
        deleteBtn.textContent = "Delete";
        deleteBtn.addEventListener("click", () => deleteCourse(course.id));

        actions.appendChild(editBtn);
        actions.appendChild(deleteBtn);

        card.appendChild(name);
        card.appendChild(dates);
        card.appendChild(actions);
        courseList.appendChild(card);
    }
}

function startEditCourse(course) {
    courseIdInput.value = course.id;
    courseNameInput.value = course.name;
    courseStartDateInput.value = formatToYYYYMMDD(course.startDate);
    courseEndDateInput.value = formatToYYYYMMDD(course.endDate);

    if (courseFormTitle) courseFormTitle.textContent = "Edit academic course";
    cancelCourseEditButton.classList.remove("hidden");
    courseNameInput.focus();
}

async function deleteCourse(courseId) {
    try {
        const response = await fetch("/courses/" + courseId, {
            method: "DELETE"
        });

        if (!response.ok) {
            await showCourseApiError(response);
            return;
        }

        showCourseMessage("Academic course deleted.", false);
        await loadCourses();
    } catch (error) {
        showCourseMessage("Could not delete the academic course.", true);
    }
}

function resetCourseForm() {
    courseForm.reset();
    courseIdInput.value = "";
    if (courseFormTitle) courseFormTitle.textContent = "Add academic course";
    cancelCourseEditButton.classList.add("hidden");
}

function showCourseMessage(text, isError) {
    courseMessage.textContent = text;
    if (isError) {
        courseMessage.classList.add("error");
    } else {
        courseMessage.classList.remove("error");
    }
}

async function showCourseApiError(response) {
    try {
        const errorData = await response.json();
        showCourseMessage(errorData.error, true);
    } catch (error) {
        showCourseMessage("Unexpected error (HTTP " + response.status + ").", true);
    }
}


// ============================================================
// 4. PESTAÑAS (Students / Subjects / Courses)
// ============================================================

/*
 * Cada botón de la barra de pestañas tiene data-tab="nombre" y cada
 * panel tiene id="tab-nombre". Al pulsar un botón se muestra su panel
 * y se ocultan los demás con la clase CSS "hidden".
 *
 * La pestaña activa se guarda en la URL (#courses, #subjects...) para
 * que al recargar la página se siga viendo la misma pestaña.
 */
const tabButtons = document.querySelectorAll(".tab-button");
const tabPanels = document.querySelectorAll(".tab-panel");

function showTab(name) {
    let found = false;
    for (const button of tabButtons) {
        if (button.dataset.tab === name) {
            found = true;
        }
    }

    // Si el nombre no existe (por ejemplo, un # raro en la URL), usamos la primera.
    if (!found) {
        name = tabButtons[0].dataset.tab;
    }

    for (const button of tabButtons) {
        const isActive = button.dataset.tab === name;
        button.classList.toggle("active", isActive);
        button.setAttribute("aria-selected", String(isActive));
    }

    for (const panel of tabPanels) {
        panel.classList.toggle("hidden", panel.id !== "tab-" + name);
    }
}

for (const button of tabButtons) {
    button.addEventListener("click", function () {
        const name = button.dataset.tab;
        showTab(name);
        history.replaceState(null, "", "#" + name);
    });
}

// Al abrir la página, mostramos la pestaña indicada en la URL (o Students).
showTab(window.location.hash.replace("#", "") || "students");
