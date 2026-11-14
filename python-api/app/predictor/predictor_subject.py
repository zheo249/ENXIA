def calculate_predicted_grade(study_hours: float, type: float) -> float:
    """Calcula una nota ficticia para demostrar la separación de servicios."""

    if type == "dificil":
        predicted_grade = study_hours * 0.15*0.8
    if type == "normal":
        predicted_grade = study_hours * 0.15
    if type == "facil":
        predicted_grade = study_hours * 0.15*1.2

    if predicted_grade > 10.0:
        return 10.0
    return predicted_grade
