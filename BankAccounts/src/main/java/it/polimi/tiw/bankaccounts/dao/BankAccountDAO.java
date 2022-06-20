package it.polimi.tiw.bankaccounts.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.bankaccounts.beans.BankAccount;
import it.polimi.tiw.bankaccounts.beans.User;

public class BankAccountDAO {
	private Connection con;

	public BankAccountDAO(Connection connection) {
		this.con = connection;
	}

	public void createVoidAccount(User user) throws SQLException {
		String query = "INSERT INTO bankaccounts.bankaccount (balance, owner) VALUES (0, ?);";
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, user.getId());
			pstatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
	}

	public List<BankAccount> getAccountsByUser(User user) throws SQLException {
		List<BankAccount> accounts = new ArrayList<>();

		String query = "SELECT A.* FROM bankaccounts.bankaccount A WHERE owner = ?;";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, user.getId());
			result = pstatement.executeQuery();
			while (result.next()) {
				BankAccount account = new BankAccount();
				account.setId(result.getInt("id"));
				account.setBalance(result.getDouble("balance"));
				account.setOwner(result.getInt("owner"));
				accounts.add(account);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}

		return accounts;
	}

	public BankAccount getAccountDetails(int accountId, User user) throws SQLException {

		BankAccount account = null;

		String query = "SELECT A.* FROM bankaccounts.bankaccount A WHERE id = ? AND owner = ?;";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, accountId);
			pstatement.setInt(2, user.getId());
			result = pstatement.executeQuery();
			while (result.next()) {
				account = new BankAccount();
				account.setId(result.getInt("id"));
				account.setBalance(result.getDouble("balance"));
				account.setOwner(result.getInt("owner"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}

		return account;
	}

	public Boolean checkSufficientBalance(int accountId, double amount, User user) throws SQLException {
		BankAccount account = getAccountDetails(accountId, user);

		if (account == null)
			return null;

		return account.getBalance() >= amount;
	}

	public Integer transferMoney(int senderAccountId, int receiverAccountId, double amount, String reason, User user)
			throws SQLException {

		Integer movementId = null;
		boolean sufficientBalance = checkSufficientBalance(senderAccountId, amount, user);
		if (!sufficientBalance)
			return null;

		String query1 = "UPDATE bankaccounts.bankaccount SET balance = balance-? WHERE id = ?;";
		String query2 = "UPDATE bankaccounts.bankaccount SET balance = balance+? WHERE id = ?;";

		MovementDAO movementDAO = new MovementDAO(con);
		PreparedStatement pstatement = null;

		con.setAutoCommit(false);
		try {

			pstatement = con.prepareStatement(query1);
			pstatement.setDouble(1, amount);
			pstatement.setInt(2, senderAccountId);
			pstatement.executeUpdate();
			pstatement.close();

			pstatement = con.prepareStatement(query2);
			pstatement.setDouble(1, amount);
			pstatement.setInt(2, receiverAccountId);
			pstatement.executeUpdate();

			movementId = movementDAO.addMovement(senderAccountId, receiverAccountId, amount, reason);

			con.commit();
		} catch (SQLException e) {
			con.rollback();
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			con.setAutoCommit(true);
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}

		return movementId;
	}

}
