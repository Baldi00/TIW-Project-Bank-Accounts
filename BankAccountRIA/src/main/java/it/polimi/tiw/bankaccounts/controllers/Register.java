package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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
@MultipartConfig
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
			response.sendError(505, "Parameters incomplete");
			return;
		}

		// OWASP Validation Regular Expression
		String emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
		boolean validEmailAddress = Pattern.compile(emailRegexPattern).matcher(email).matches();

		if (!validEmailAddress) {
			response.setStatus(500);
			response.getWriter().println("Indirizzo email non valido");
			return;
		}

		UserDAO userDAO = new UserDAO(connection);
		try {
			boolean userAlreadyPresent = userDAO.checkIfUserPresent(email);
			if (userAlreadyPresent) {
				response.setStatus(500);
				response.getWriter().println("Indirizzo email gia' presente, scegline un altro");
				return;
			}
		} catch (SQLException e) {
			response.setStatus(500);
			response.getWriter().println("Errore di accesso al database");
			return;
		}

		if (password.length() < 8) {
			response.setStatus(500);
			response.getWriter().println("La password è troppo corta");
			return;
		}

		if (!password.equals(repeatedPassword)) {
			response.setStatus(500);
			response.getWriter().println("Le due password non coincidono");
			return;
		}

		try {
			User user = userDAO.registerUser(name, surname, email, password);
			BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
			bankAccountDAO.createVoidAccount(user);
			if (user != null) {
				response.setStatus(200);
			} else {
				response.setStatus(500);
				response.getWriter().println("Utente non valido");
			}
		} catch (SQLException e) {
			response.setStatus(500);
			response.getWriter().println("Errore di accesso al database");
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
