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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.bankaccounts.beans.BankAccount;
import it.polimi.tiw.bankaccounts.beans.Movement;
import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.MovementDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;
import it.polimi.tiw.bankaccounts.utils.TemplateEngineHandler;

/**
 * Servlet implementation class TransferSuccessPage
 */
@WebServlet("/TransferSuccessPage")
public class TransferSuccessPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public TransferSuccessPage() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
		connection = ConnectionHandler.getConnection(context);
		templateEngine = TemplateEngineHandler.getTemplateEngine(context);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		if (user == null) {
			String path = getServletContext().getContextPath() + "/LoginRegisterPage";
			response.sendRedirect(path);
			return;
		}

		Integer movementId;

		try {
			movementId = Integer.parseInt(request.getParameter("movementId"));
		} catch (NumberFormatException e) {
			movementId = null;
		}

		if (movementId == null) {
			response.sendError(400, "Missing or wrong parameters");
			return;
		}

		MovementDAO movementDAO = new MovementDAO(connection);
		BankAccount[] bankAccounts;
		Movement movement;
		try {
			bankAccounts = movementDAO.getAccountsByMovement(movementId, user);
			movement = movementDAO.getMovementDetails(movementId, user);
		} catch (SQLException e) {
			response.sendError(500, "Database connection error");
			return;
		}

		String templatePath = "transferSuccessful.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		if (bankAccounts != null && movement != null) {
			ctx.setVariable("senderAccount", bankAccounts[0]);
			ctx.setVariable("receiverAccount", bankAccounts[1]);
			ctx.setVariable("movement", movement);
		} else {
			response.sendError(400, "Requested movement is wrong");
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
