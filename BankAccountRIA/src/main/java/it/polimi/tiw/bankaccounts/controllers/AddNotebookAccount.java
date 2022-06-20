package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.NotebookDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;

/**
 * Servlet implementation class AddNotebookAccount
 */
@WebServlet("/AddNotebookAccount")
@MultipartConfig
public class AddNotebookAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public AddNotebookAccount() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
		connection = ConnectionHandler.getConnection(context);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		if (user == null) {
			response.setStatus(401);
			return;
		}

		Integer accountId;

		try {
			accountId = Integer.parseInt(request.getParameter("accountId"));
		} catch (NumberFormatException e) {
			accountId = null;
		}

		if (accountId == null) {
			response.setStatus(500);
			response.getWriter().println("Parametri errati o mancanti");
			return;
		}

		NotebookDAO notebookDAO = new NotebookDAO(connection);
		try {
			notebookDAO.addNotebookAccount(user.getId(), accountId);
		} catch (SQLException e) {
			response.setStatus(500);
			response.getWriter().println("Errore di connessione al database");
			return;
		}

		response.setStatus(200);
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
