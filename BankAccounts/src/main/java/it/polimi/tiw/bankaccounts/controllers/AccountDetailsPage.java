package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.bankaccounts.beans.BankAccount;
import it.polimi.tiw.bankaccounts.beans.Movement;
import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.BankAccountDAO;
import it.polimi.tiw.bankaccounts.dao.MovementDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;
import it.polimi.tiw.bankaccounts.utils.TemplateEngineHandler;

/**
 * Servlet implementation class AccountDetailsPage
 */
@WebServlet("/AccountDetailsPage")
public class AccountDetailsPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public AccountDetailsPage() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
		connection = ConnectionHandler.getConnection(context);
		templateEngine = TemplateEngineHandler.getTemplateEngine(context);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String templatePath = "accountDetails.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		if (user == null) {
			String path = getServletContext().getContextPath() + "/LoginRegisterPage";
			response.sendRedirect(path);
			return;
		}

		Integer accountId;
		try {
			accountId = Integer.parseInt(request.getParameter("accountId"));
		} catch (NumberFormatException e) {
			accountId = null;
		}

		if (accountId == null) {
			response.sendError(400, "Missing or wrong parameters");
			return;
		}

		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		try {
			BankAccount bankAccount = bankAccountDAO.getAccountDetails(accountId, user);
			if (bankAccount == null) {
				response.sendError(404, "Request bank account does not exist");
				return;
			}
			ctx.setVariable("bankAccount", bankAccount);
		} catch (SQLException e) {
			response.sendError(500, "Database connection error");
			return;
		}

		MovementDAO movementDAO = new MovementDAO(connection);
		try {
			List<Movement> receivedMovements = movementDAO.getReceivedMovementsByAccount(accountId, user);
			List<Movement> sentMovements = movementDAO.getSentMovementsByAccount(accountId, user);
			ctx.setVariable("receivedMovements", receivedMovements);
			ctx.setVariable("sentMovements", sentMovements);
		} catch (SQLException e) {
			response.sendError(500, "Database connection error");
			return;
		}

		templateEngine.process(templatePath, ctx, response.getWriter());
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
