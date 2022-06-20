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

import it.polimi.tiw.bankaccounts.utils.TemplateEngineHandler;

/**
 * Servlet implementation class LoginRegisterPage
 */
@WebServlet("/LoginRegisterPage")
public class LoginRegisterPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	public LoginRegisterPage() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();
		templateEngine = TemplateEngineHandler.getTemplateEngine(context);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		if (session.getAttribute("user") != null) {
			String path = getServletContext().getContextPath() + "/HomePage";
			response.sendRedirect(path);
		}
		String register = request.getParameter("register");
		String login = request.getParameter("login");
		String path = "loginRegister.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		if (register != null)
			ctx.setVariable("register", register);
		if (login != null)
			ctx.setVariable("login", login);
		templateEngine.process(path, ctx, response.getWriter());
	}
}
