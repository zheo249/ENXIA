def calculate_predicted_grade(study_hours: float, type: float) -> float:
    """Calcula una nota ficticia para demostrar la separación de servicios."""

    if type == "dificil":
        predicted_grade = study_hours * 0.8
    if type == "media":
        predicted_grade = study_hours * 1.0
    if type == "facil":
        predicted_grade = study_hours * 1.2

    if predicted_grade > 100.0:
        return 100.0
    return predicted_grade
