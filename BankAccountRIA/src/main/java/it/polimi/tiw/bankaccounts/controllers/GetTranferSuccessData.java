package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
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
import it.polimi.tiw.bankaccounts.dao.MovementDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;

/**
 * Servlet implementation class GetTranferSuccessData
 */
@WebServlet("/GetTranferSuccessData")
@MultipartConfig
public class GetTranferSuccessData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetTranferSuccessData() {
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

		Integer movementId;

		try {
			movementId = Integer.parseInt(request.getParameter("movementId"));
		} catch (NumberFormatException e) {
			movementId = null;
		}

		if (movementId == null) {
			response.setStatus(400);
			response.getWriter().println("Parametri errati o mancanti");
			return;
		}

		MovementDAO movementDAO = new MovementDAO(connection);
		BankAccount[] bankAccounts;
		Movement movement;
		try {
			bankAccounts = movementDAO.getAccountsByMovement(movementId, user);
			movement = movementDAO.getMovementDetails(movementId, user);
		} catch (SQLException e) {
			response.setStatus(500);
			response.getWriter().println("Errore di connessione al database");
			return;
		}

		Map<String, Object> result = new HashMap<>();
		if (bankAccounts != null && movement != null) {
			result.put("senderAccount", bankAccounts[0]);
			result.put("receiverAccount", bankAccounts[1]);
			result.put("movement", movement);
		} else {
			response.setStatus(400);
			response.getWriter().println("Il trasferimento richiesto è errato");
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
