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
import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.dao.BankAccountDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;
import it.polimi.tiw.bankaccounts.utils.TemplateEngineHandler;

/**
 * Servlet implementation class HomePage
 */
@WebServlet("/HomePage")
public class HomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public HomePage() {
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

		List<BankAccount> accounts = null;

		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		try {
			accounts = bankAccountDAO.getAccountsByUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(500, "Error in retrieving data from the database");
			return;
		}

		String path = "home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("user", user);
		ctx.setVariable("accounts", accounts);
		templateEngine.process(path, ctx, response.getWriter());
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
