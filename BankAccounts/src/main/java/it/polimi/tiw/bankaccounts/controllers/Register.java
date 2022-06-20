package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.BankAccountDAO;
import it.polimi.tiw.bankaccounts.dao.UserDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;

/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public Register() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
		connection = ConnectionHandler.getConnection(context);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String repeatedPassword = request.getParameter("repeatedPassword");

		if (name == null || surname == null || email == null || password == null || repeatedPassword == null
				|| name.equals("") || surname.equals("") || email.equals("") || password.equals("")
				|| repeatedPassword.equals("")) {
			response.sendError(400, "Parameters incomplete");
			return;
		}

		// OWASP Validation Regular Expression
		String emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
		boolean validEmailAddress = Pattern.compile(emailRegexPattern).matcher(email).matches();

		if (!validEmailAddress) {
			String path = getServletContext().getContextPath() + "/LoginRegisterPage?register=invalidEmail";
			response.sendRedirect(path);
			return;
		}

		UserDAO userDAO = new UserDAO(connection);
		try {
			boolean userAlreadyPresent = userDAO.checkIfUserPresent(email);
			if (userAlreadyPresent) {
				String path = getServletContext().getContextPath() + "/LoginRegisterPage?register=emailAlreadyPresent";
				response.sendRedirect(path);
				return;
			}
		} catch (SQLException e) {
			response.sendError(500, "Database access failed");
			return;
		}

		if (password.length() < 8) {
			String path = getServletContext().getContextPath() + "/LoginRegisterPage?register=passwordTooShort";
			response.sendRedirect(path);
			return;
		}

		if (!password.equals(repeatedPassword)) {
			String path = getServletContext().getContextPath() + "/LoginRegisterPage?register=passwordDifferent";
			response.sendRedirect(path);
			return;
		}

		try {
			User user = userDAO.registerUser(name, surname, email, password);
			BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
			bankAccountDAO.createVoidAccount(user);
			if (user != null) {
				String path = getServletContext().getContextPath() + "/LoginRegisterPage?register=success";
				response.sendRedirect(path);
			} else {
				response.sendError(404, "Invalid user");
			}
		} catch (SQLException e) {
			response.sendError(500, "Database access failed");
		}
	}

	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
