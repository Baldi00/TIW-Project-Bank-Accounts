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
import it.polimi.tiw.bankaccounts.dao.BankAccountDAO;
import it.polimi.tiw.bankaccounts.dao.UserDAO;
import it.polimi.tiw.bankaccounts.utils.ConnectionHandler;

/**
 * Servlet implementation class TransferMoney
 */
@WebServlet("/TransferMoney")
public class TransferMoney extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public TransferMoney() {
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
			String path = getServletContext().getContextPath() + "/LoginRegisterPage";
			response.sendRedirect(path);
			return;
		}

		String receiverEmail = request.getParameter("receiver_email");
		String reason = request.getParameter("reason");

		Double amount;
		Integer receiverAccountId;
		Integer senderAccountId;
		try {
			amount = Double.parseDouble(request.getParameter("amount"));
			receiverAccountId = Integer.parseInt(request.getParameter("receiver_account_id"));
			senderAccountId = Integer.parseInt(request.getParameter("sender_account_id"));
		} catch (NumberFormatException e) {
			amount = null;
			receiverAccountId = null;
			senderAccountId = null;
		}

		if (receiverEmail == null || reason == null || amount == null || receiverAccountId == null
				|| senderAccountId == null || receiverEmail.equals("") || reason.equals("") || amount < 1) {
			response.sendError(400, "Missing or wrong parameters");
			return;
		}

		if (receiverAccountId == senderAccountId) {
			String path = getServletContext().getContextPath()
					+ "/TransferFailedPage?failReason=sameBankAccount&account=" + senderAccountId;
			response.sendRedirect(path);
			return;
		}
		
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		UserDAO userDAO = new UserDAO(connection);
		try {
			User receiverAccountOwner = userDAO.getUserByAccount(receiverAccountId);
			if (receiverAccountOwner == null || !receiverAccountOwner.getEmail().equals(receiverEmail)) {
				String path = getServletContext().getContextPath()
						+ "/TransferFailedPage?failReason=wrongAccountOrEmail&account=" + senderAccountId;
				response.sendRedirect(path);
				return;
			}

			Boolean sufficientBalance = bankAccountDAO.checkSufficientBalance(senderAccountId, amount, user);
			if (sufficientBalance == null) {
				String path = getServletContext().getContextPath()
						+ "/TransferFailedPage?failReason=accountDoesNotExist&account=" + senderAccountId;
				response.sendRedirect(path);
				return;
			} else if (!sufficientBalance) {
				String path = getServletContext().getContextPath()
						+ "/TransferFailedPage?failReason=notEnoughMoney&account=" + senderAccountId;
				response.sendRedirect(path);
				return;
			}

			Integer movementId = bankAccountDAO.transferMoney(senderAccountId, receiverAccountId, amount, reason, user);

			if (movementId != null) {
				String path = getServletContext().getContextPath() + "/TransferSuccessPage?movementId=" + movementId;
				response.sendRedirect(path);
			} else {
				String path = getServletContext().getContextPath()
						+ "/TransferFailedPage?failReason=genericError&account=" + senderAccountId;
				response.sendRedirect(path);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(500, "Database connection error");
			return;
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
