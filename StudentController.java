package com.student.controller;

import com.student.dao.StudentDAO;
import com.student.model.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/students")
public class StudentController extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<Student> students = studentDAO.getAllStudents();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);

                json.append("{")
                    .append(""id":").append(s.getId()).append(",")
                    .append(""name":"").append(escape(s.getName())).append("",")
                    .append(""age":").append(s.getAge()).append(",")
                    .append(""course":"").append(escape(s.getCourse())).append(""")
                    .append("}");

                if (i < students.size() - 1) json.append(",");
            }
            json.append("]");

            response.getWriter().print(json);

        } catch (SQLException e) {
            sendError(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String ageText = request.getParameter("age");
        String course = request.getParameter("course");

        try {
            if (name == null || name.isBlank() ||
                ageText == null || course == null || course.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "All fields are required.");
                return;
            }

            int age = Integer.parseInt(ageText);

            studentDAO.addStudent(new Student(name.trim(), age, course.trim()));

            response.setContentType("application/json");
            response.getWriter().print("{"message":"Student added successfully"}");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Age must be a number.");
        } catch (SQLException e) {
            sendError(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idText = request.getParameter("id");

        try {
            int id = Integer.parseInt(idText);
            studentDAO.deleteStudent(id);

            response.setContentType("application/json");
            response.getWriter().print("{"message":"Student deleted successfully"}");

        } catch (Exception e) {
            sendError(response, e);
        }
    }

    private void sendError(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.getWriter().print("{"error":"" + escape(e.getMessage()) + ""}");
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace(""", "\"")
                    .replace("\n", " ")
                    .replace("\r", " ");
    }
}
