/*
 * Moodle Replacement - Course Management System
 * Author: Arina Agafonova
 * Language: C
 *
 * This program implements a simplified course management system
 * that handles students, exams (written or digital), and grades.
 * Input commands are read from "input.txt", output results are written to "output.txt".
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/* ----------------------- Structures ----------------------- */

// Structure to store student information
typedef struct {
    int student_id;      // Unique student ID
    char name[21];       // Name (max 20 chars)
    char faculty[31];    // Faculty (max 30 chars)
} Student;

// Structure to store exam grades
typedef struct {
    int exam_id;         // Exam ID
    int student_id;      // Student ID
    int grade;           // Grade (0-100)
} ExamGrade;

// Enum for exam type
typedef enum {
    WRITTEN,
    DIGITAL
} ExamType;

// Union for exam info
typedef union {
    int duration;        // Duration in minutes (written exam)
    char software[21];   // Software name (digital exam)
} ExamInfo;

// Structure to store exam details
typedef struct {
    int exam_id;         // Exam ID
    ExamType exam_type;  // Type of exam
    ExamInfo exam_info;  // Exam info (duration or software)
} Exam;

/* ----------------------- Global Data ----------------------- */

Student students[1000];
Exam exams[500];
ExamGrade grades[500];

int students_count = 0;
int exams_count = 0;
int grades_count = 0;

/* ----------------------- Validation Functions ----------------------- */

int isValidStudentId(int id) { return id > 0 && id < 1000; }
int isValidExamId(int id) { return id > 0 && id < 500; }
int isValidGrade(int grade) { return grade >= 0 && grade <= 100; }
int isValidDuration(int duration) { return duration >= 40 && duration <= 180; }

int isValidName(const char *name) {
    if (!(strlen(name) > 1 && strlen(name) < 21)) return 0;
    for (size_t i = 0; i < strlen(name); i++) if (!isalpha(name[i])) return 0;
    return 1;
}

int isValidFaculty(const char *faculty) {
    if (!(strlen(faculty) > 4 && strlen(faculty) < 31)) return 0;
    for (size_t i = 0; i < strlen(faculty); i++) if (!isalpha(faculty[i])) return 0;
    return 1;
}

int isValidSoftware(const char *software) {
    if (!(strlen(software) > 2 && strlen(software) < 21)) return 0;
    for (size_t i = 0; i < strlen(software); i++) if (!isalpha(software[i])) return 0;
    return 1;
}

/* ----------------------- Student Functions ----------------------- */

void addStudent(int student_id, const char *name, const char *faculty, FILE *output) {
    for (int i = 0; i < students_count; i++)
        if (students[i].student_id == student_id) { fprintf(output, "Student: %d already exists\n", student_id); return; }

    if (!isValidStudentId(student_id)) { fprintf(output, "Invalid student id\n"); return; }
    if (!isValidName(name)) { fprintf(output, "Invalid name\n"); return; }
    if (!isValidFaculty(faculty)) { fprintf(output, "Invalid faculty\n"); return; }

    students[students_count].student_id = student_id;
    strcpy(students[students_count].name, name);
    strcpy(students[students_count].faculty, faculty);
    students_count++;

    fprintf(output, "Student: %d added\n", student_id);
}

void searchStudent(int student_id, FILE *output) {
    if (!isValidStudentId(student_id)) { fprintf(output, "Invalid student id\n"); return; }
    for (int i = 0; i < students_count; i++)
        if (students[i].student_id == student_id) { fprintf(output, "ID: %d, Name: %s, Faculty: %s\n", student_id, students[i].name, students[i].faculty); return; }
    fprintf(output, "Student not found\n");
}

void deleteStudent(int student_id, FILE *output) {
    int ind = -1;
    for (int i = 0; i < students_count; i++) if (students[i].student_id == student_id) { ind = i; break; }
    if (ind == -1) { fprintf(output, "Student not found\n"); return; }

    // Delete associated grades
    for (int i = 0; i < grades_count; i++)
        if (grades[i].student_id == student_id) { for (int j = i; j < grades_count-1; j++) grades[j] = grades[j+1]; grades_count--; i--; }

    for (int i = ind; i < students_count-1; i++) students[i] = students[i+1];
    students_count--;

    fprintf(output, "Student: %d deleted\n", student_id);
}

void listAllStudents(FILE *output) {
    for (int i = 0; i < students_count; i++)
        fprintf(output, "ID: %d, Name: %s, Faculty: %s\n", students[i].student_id, students[i].name, students[i].faculty);
}

/* ----------------------- Exam Functions ----------------------- */

void addExam(int exam_id, ExamType exam_type, ExamInfo exam_info, FILE *output) {
    for (int i = 0; i < exams_count; i++)
        if (exams[i].exam_id == exam_id) { fprintf(output, "Exam: %d already exists\n", exam_id); return; }

    if (!isValidExamId(exam_id)) { fprintf(output, "Invalid exam id\n"); return; }

    if (exam_type == WRITTEN && !isValidDuration(exam_info.duration)) { fprintf(output, "Invalid duration\n"); return; }
    if (exam_type == DIGITAL && !isValidSoftware(exam_info.software)) { fprintf(output, "Invalid software\n"); return; }

    exams[exams_count].exam_id = exam_id;
    exams[exams_count].exam_type = exam_type;
    exams[exams_count].exam_info = exam_info;
    exams_count++;

    fprintf(output, "Exam: %d added\n", exam_id);
}

void updateExam(int exam_id, ExamType type, ExamInfo info, FILE *output) {
    int ind = -1; for (int i = 0; i < exams_count; i++) if (exams[i].exam_id == exam_id) { ind = i; break; }
    if (ind == -1) { fprintf(output, "Exam not found\n"); return; }

    if (type == WRITTEN && !isValidDuration(info.duration)) { fprintf(output, "Invalid duration\n"); return; }
    if (type == DIGITAL && !isValidSoftware(info.software)) { fprintf(output, "Invalid software\n"); return; }

    exams[ind].exam_type = type;
    exams[ind].exam_info = info;

    fprintf(output, "Exam: %d updated\n", exam_id);
}

/* ----------------------- Grade Functions ----------------------- */

void addGrade(int exam_id, int student_id, int grade, FILE *output) {
    int ex = -1, st = -1;
    for (int i = 0; i < exams_count; i++) if (exams[i].exam_id == exam_id) { ex = i; break; }
    for (int i = 0; i < students_count; i++) if (students[i].student_id == student_id) { st = i; break; }

    if (ex == -1) { fprintf(output, "Exam not found\n"); return; }
    if (st == -1) { fprintf(output, "Student not found\n"); return; }
    if (!isValidGrade(grade)) { fprintf(output, "Invalid grade\n"); return; }

    grades[grades_count].exam_id = exam_id;
    grades[grades_count].student_id = student_id;
    grades[grades_count].grade = grade;
    grades_count++;

    fprintf(output, "Grade %d added for the student: %d\n", grade, student_id);
}

void updateGrade(int exam_id, int student_id, int grade, FILE *output) {
    int ind = -1;
    for (int i = 0; i < grades_count; i++)
        if (grades[i].exam_id == exam_id && grades[i].student_id == student_id) { ind = i; break; }
    if (ind == -1) { fprintf(output, "Grade not found\n"); return; }
    if (!isValidGrade(grade)) { fprintf(output, "Invalid grade\n"); return; }

    grades[ind].grade = grade;
    fprintf(output, "Grade %d updated for the student: %d\n", grade, student_id);
}

void searchGrade(int exam_id, int student_id, FILE *output) {
    int ex = -1, st = -1, gr = -1;
    for (int i = 0; i < exams_count; i++) if (exams[i].exam_id == exam_id) ex = i;
    for (int i = 0; i < students_count; i++) if (students[i].student_id == student_id) st = i;
    for (int i = 0; i < grades_count; i++) if (grades[i].exam_id == exam_id && grades[i].student_id == student_id) gr = i;

    if (ex == -1) { fprintf(output, "Exam not found\n"); return; }
    if (st == -1) { fprintf(output, "Student not found\n"); return; }
    if (gr == -1) { fprintf(output, "Grade not found\n"); return; }

    fprintf(output, "Exam: %d, Student: %d, Name: %s, Grade: %d, Type: %s", exam_id, student_id,
            students[st].name, grades[gr].grade, exams[ex].exam_type == WRITTEN ? "WRITTEN" : "DIGITAL");
    if (exams[ex].exam_type == WRITTEN) fprintf(output, ", Info: %d\n", exams[ex].exam_info.duration);
    else fprintf(output, ", Info: %s\n", exams[ex].exam_info.software);
}

/* ----------------------- Main ----------------------- */

int main() {
    FILE *input = freopen("input.txt", "r", stdin);
    FILE *output = freopen("output.txt", "w", stdout);

    char line[256];
    while (fgets(line, sizeof(line), input)) {
        line[strcspn(line, "\n")] = 0;
        if (strcmp(line, "END") == 0) break;

        char *tokens[5]; int t = 0;
        char *tok = strtok(line, " ");
        while (tok) { tokens[t++] = tok; tok = strtok(NULL, " "); }

        if (strcmp(tokens[0], "ADD_STUDENT") == 0)
            addStudent(atoi(tokens[1]), tokens[2], tokens[3], output);
        else if (strcmp(tokens[0], "ADD_EXAM") == 0) {
            ExamInfo info; ExamType type;
            if (strcmp(tokens[2], "WRITTEN") == 0) { type = WRITTEN; info.duration = atoi(tokens[3]); }
            else if (strcmp(tokens[2], "DIGITAL") == 0) { type = DIGITAL; strcpy(info.software, tokens[3]); }
            else { fprintf(output, "Invalid exam type\n"); continue; }
            addExam(atoi(tokens[1]), type, info, output);
        }
        else if (strcmp(tokens[0], "ADD_GRADE") == 0)
            addGrade(atoi(tokens[1]), atoi(tokens[2]), atoi(tokens[3]), output);
        else if (strcmp(tokens[0], "UPDATE_EXAM") == 0) {
            ExamInfo info; ExamType type;
            if (strcmp(tokens[2], "WRITTEN") == 0) { type = WRITTEN; info.duration = atoi(tokens[3]); }
            else if (strcmp(tokens[2], "DIGITAL") == 0) { type = DIGITAL; strcpy(info.software, tokens[3]); }
            else { fprintf(output, "Invalid exam type\n"); continue; }
            updateExam(atoi(tokens[1]), type, info, output);
        }
        else if (strcmp(tokens[0], "UPDATE_GRADE") == 0)
            updateGrade(atoi(tokens[1]), atoi(tokens[2]), atoi(tokens[3]), output);
        else if (strcmp(tokens[0], "SEARCH_STUDENT") == 0)
            searchStudent(atoi(tokens[1]), output);
        else if (strcmp(tokens[0], "SEARCH_GRADE") == 0)
            searchGrade(atoi(tokens[1]), atoi(tokens[2]), output);
        else if (strcmp(tokens[0], "DELETE_STUDENT") == 0)
            deleteStudent(atoi(tokens[1]), output);
        else if (strcmp(tokens[0], "LIST_ALL_STUDENTS") == 0)
            listAllStudents(output);
    }

    fclose(input); fclose(output);
    return 0;
}
