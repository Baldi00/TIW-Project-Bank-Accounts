package it.polimi.tiw.bankaccounts.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.bankaccounts.beans.BankAccount;
import it.polimi.tiw.bankaccounts.beans.Movement;
import it.polimi.tiw.bankaccounts.beans.User;

public class MovementDAO {
	private Connection con;

	public MovementDAO(Connection connection) {
		this.con = connection;
	}

	public List<Movement> getReceivedMovementsByAccount(int accountId, User user) throws SQLException {
		List<Movement> movements = new ArrayList<>();

		String query = "SELECT M.*, U.name, U.surname FROM bankaccountsria.movement M JOIN bankaccountsria.bankaccount A JOIN bankaccountsria.bankaccount A2 JOIN bankaccountsria.user U "
				+ "ON A2.owner = U.id AND A.id = M.receiver_account AND A2.id = M.sender_account "
				+ "WHERE M.receiver_account = ? AND A.owner = ? ORDER BY M.date DESC;";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, accountId);
			pstatement.setInt(2, user.getId());
			result = pstatement.executeQuery();
			while (result.next()) {
				Movement movement = new Movement();
				movement.setId(result.getInt("id"));
				movement.setDate(result.getDate("date"));
				movement.setAmount(result.getDouble("amount"));
				movement.setReason(result.getString("reason"));
				movement.setSenderAccount(result.getInt("sender_account"));
				movement.setReceiverAccount(result.getInt("receiver_account"));
				movement.setSenderName(result.getString("name") + " " + result.getString("surname"));
				movements.add(movement);
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

		return movements;
	}

	public List<Movement> getSentMovementsByAccount(int accountId, User user) throws SQLException {
		List<Movement> movements = new ArrayList<>();

		String query = "SELECT M.*, U.name, U.surname FROM bankaccountsria.movement M JOIN bankaccountsria.bankaccount A JOIN bankaccountsria.bankaccount A2 JOIN bankaccountsria.user U "
				+ "ON A2.owner = U.id AND A.id = M.sender_account AND A2.id = M.receiver_account "
				+ "WHERE M.sender_account = ? AND A.owner = ? ORDER BY M.date DESC;";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, accountId);
			pstatement.setInt(2, user.getId());
			result = pstatement.executeQuery();
			while (result.next()) {
				Movement movement = new Movement();
				movement.setId(result.getInt("id"));
				movement.setDate(result.getDate("date"));
				movement.setAmount(result.getDouble("amount"));
				movement.setReason(result.getString("reason"));
				movement.setSenderAccount(result.getInt("sender_account"));
				movement.setReceiverAccount(result.getInt("receiver_account"));
				movement.setReceiverName(result.getString("name") + " " + result.getString("surname"));
				movements.add(movement);
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

		return movements;
	}

	public Integer addMovement(int senderAccountId, int receiverAccountId, double amount, String reason)
			throws SQLException {
		Integer movementId = null;
		String query1 = "INSERT INTO bankaccountsria.movement (date, amount, reason, sender_account, receiver_account) VALUES (?, ?, ?, ?, ?);";
		String query2 = "SELECT id FROM bankaccountsria.movement WHERE date = ? AND amount = ? AND reason = ? AND sender_account = ? AND receiver_account = ?;";
		ResultSet result;
		PreparedStatement pstatement = null;

		Date currentDate = new Date(System.currentTimeMillis());

		try {
			pstatement = con.prepareStatement(query1);
			pstatement.setDate(1, currentDate);
			pstatement.setDouble(2, amount);
			pstatement.setString(3, reason);
			pstatement.setInt(4, senderAccountId);
			pstatement.setInt(5, receiverAccountId);
			pstatement.executeUpdate();
			pstatement.close();

			pstatement = con.prepareStatement(query2);
			pstatement.setDate(1, currentDate);
			pstatement.setDouble(2, amount);
			pstatement.setString(3, reason);
			pstatement.setInt(4, senderAccountId);
			pstatement.setInt(5, receiverAccountId);
			result = pstatement.executeQuery();

			while (result.next()) {
				movementId = result.getInt("id");
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

		return movementId;
	}

	public Movement getMovementDetails(int movementId, User user) throws SQLException {
		Movement movement = null;

		String query = "SELECT M.*, U.name AS n1, U.surname AS s1, U2.name AS n2, U2.surname AS s2 "
				+ "FROM bankaccountsria.movement M JOIN bankaccountsria.bankaccount A JOIN bankaccountsria.bankaccount A2 JOIN bankaccountsria.user U JOIN bankaccountsria.user U2 "
				+ "ON A.id = M.sender_account AND A2.id = M.receiver_account AND A.owner = U.id AND A2.owner = U2.id "
				+ "WHERE M.id = ? AND A.owner = ?;";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, movementId);
			pstatement.setInt(2, user.getId());
			result = pstatement.executeQuery();
			while (result.next()) {
				movement = new Movement();
				movement.setId(result.getInt("id"));
				movement.setDate(result.getDate("date"));
				movement.setAmount(result.getDouble("amount"));
				movement.setReason(result.getString("reason"));
				movement.setSenderAccount(result.getInt("sender_account"));
				movement.setReceiverAccount(result.getInt("receiver_account"));
				movement.setSenderName(result.getString("n1") + " " + result.getString("s1"));
				movement.setReceiverName(result.getString("n2") + " " + result.getString("s2"));
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

		return movement;
	}

	public BankAccount[] getAccountsByMovement(int movementId, User user) throws SQLException {
		BankAccount ba1 = null;
		BankAccount ba2 = null;
		BankAccount[] bankAccounts = null;

		String query = "SELECT A.id AS id1, A.balance AS b1, A.owner AS o1, A2.id AS id2, A2.balance AS b2, A2.owner AS o2 "
				+ "FROM bankaccountsria.movement M JOIN bankaccountsria.bankaccount A JOIN bankaccountsria.bankaccount A2 "
				+ "ON A.id = M.sender_account AND A2.id = M.receiver_account "
				+ "WHERE M.id = ? AND (A.owner = ? OR A2.owner = ?);";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, movementId);
			pstatement.setInt(2, user.getId());
			pstatement.setInt(3, user.getId());
			result = pstatement.executeQuery();
			while (result.next()) {
				ba1 = new BankAccount();
				ba2 = new BankAccount();

				ba1.setId(result.getInt("id1"));
				ba1.setBalance(result.getDouble("b1"));
				ba1.setOwner(result.getInt("o1"));
				ba2.setId(result.getInt("id2"));
				ba2.setBalance(result.getDouble("b2"));
				ba2.setOwner(result.getInt("o2"));

				bankAccounts = new BankAccount[] { ba1, ba2 };
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

		return bankAccounts;
	}

}
