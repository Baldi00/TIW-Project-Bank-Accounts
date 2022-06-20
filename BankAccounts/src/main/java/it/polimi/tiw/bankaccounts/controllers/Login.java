package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.UserDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public Login() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
		connection = ConnectionHandler.getConnection(context);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");

		if (email == null || password == null) {
			response.sendError(400, "Parameters incomplete");
			return;
		}

		UserDAO userDAO = new UserDAO(connection);
		try {
			User user = userDAO.checkUserCredentials(email, password);
			if (user != null) {
				HttpSession session = request.getSession();
				session.setAttribute("user", user);
				String path = getServletContext().getContextPath() + "/HomePage";
				response.sendRedirect(path);
			} else {
				String path = getServletContext().getContextPath() + "/LoginRegisterPage?login=failed";
				response.sendRedirect(path);
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
