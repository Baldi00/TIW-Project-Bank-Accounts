package it.polimi.tiw.bankaccounts.controllers;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.bankaccounts.beans.User;
import it.polimi.tiw.bankaccounts.utils.TemplateEngineHandler;

/**
 * Servlet implementation class TransferFailedPage
 */
@WebServlet("/TransferFailedPage")
public class TransferFailedPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	public TransferFailedPage() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
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

		String failReason = request.getParameter("failReason");
		Integer account;

		try {
			account = Integer.parseInt(request.getParameter("account"));
		} catch (NumberFormatException e) {
			account = null;
		}

		if (account == null || failReason == null
				|| (!failReason.equals("accountDoesNotExist") && !failReason.equals("notEnoughMoney")
						&& !failReason.equals("wrongAccountOrEmail") && !failReason.equals("sameBankAccount")
						&& !failReason.equals("genericError"))) {
			response.sendError(400, "Missing or wrong parameters");
			return;
		}

		String templatePath = "transferFailed.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		ctx.setVariable("failReason", failReason);
		ctx.setVariable("account", account);

		templateEngine.process(templatePath, ctx, response.getWriter());

	}

}
