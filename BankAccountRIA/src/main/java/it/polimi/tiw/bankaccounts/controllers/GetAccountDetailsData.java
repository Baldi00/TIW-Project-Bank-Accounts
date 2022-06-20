package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.bankaccounts.beans.BankAccount;
import it.polimi.tiw.bankaccounts.beans.Movement;
import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.BankAccountDAO;
import it.polimi.tiw.bankaccounts.dao.MovementDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;

/**
 * Servlet implementation class GetAccountDetailsData
 */
@WebServlet("/GetAccountDetailsData")
@MultipartConfig
public class GetAccountDetailsData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetAccountDetailsData() {
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

		Integer accountId;
		try {
			accountId = Integer.parseInt(request.getParameter("accountId"));
		} catch (NumberFormatException e) {
			accountId = null;
		}

		if (accountId == null) {
			response.setStatus(400);
			response.getWriter().println("Parametri mancanti o errati");
			return;
		}

		Map<String, Object> result = new HashMap<>();

		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		try {
			BankAccount bankAccount = bankAccountDAO.getAccountDetails(accountId, user);
			if (bankAccount == null) {
				response.setStatus(400);
				response.getWriter().println("Il conto richiesto non esiste");
				return;
			}
			result.put("bankAccount", bankAccount);
		} catch (SQLException e) {
			response.setStatus(500);
			response.getWriter().println("Errore di connessione al database");
			return;
		}

		MovementDAO movementDAO = new MovementDAO(connection);
		try {
			List<Movement> receivedMovements = movementDAO.getReceivedMovementsByAccount(accountId, user);
			List<Movement> sentMovements = movementDAO.getSentMovementsByAccount(accountId, user);
			result.put("receivedMovements", receivedMovements);
			result.put("sentMovements", sentMovements);
		} catch (SQLException e) {
			response.setStatus(500);
			response.getWriter().println("Errore di connessione al database");
			return;
		}

		response.setStatus(200);
		String json = new Gson().toJson(result, Map.class);
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
