package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.bankaccounts.beans.Notebook;
import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.NotebookDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;

/**
 * Servlet implementation class GetNotebookAccountsData
 */
@WebServlet("/GetNotebookAccountsData")
@MultipartConfig
public class GetNotebookAccountsData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetNotebookAccountsData() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
		connection = ConnectionHandler.getConnection(context);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		if (user == null) {
			response.setStatus(401);
			return;
		}
		
		String email = request.getParameter("email");

		if (email == null) {
			response.setStatus(400);
			response.getWriter().println("Email non presente");
			return;
		}
		
		List<Notebook> accounts = null;

		NotebookDAO notebookDAO = new NotebookDAO(connection);
		try {
			accounts = notebookDAO.getNotebookUsersByEmail(email, user.getId());
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			response.getWriter().println("Errore nella ricezione dei dati dal database");
			return;
		}

		response.setStatus(200);
		String json = new Gson().toJson(accounts, List.class);
		response.getWriter().println(json);
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
