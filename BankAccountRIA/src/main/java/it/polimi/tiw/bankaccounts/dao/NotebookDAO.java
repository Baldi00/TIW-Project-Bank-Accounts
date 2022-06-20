package it.polimi.tiw.bankaccounts.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.bankaccounts.beans.Notebook;

public class NotebookDAO {
	private Connection con;

	public NotebookDAO(Connection connection) {
		this.con = connection;
	}

	public List<Notebook> getNotebookUsersByEmail(String email, int userId) throws SQLException {
		List<Notebook> bankAccounts = null;
		String query = "SELECT U.email, U.name, U.surname, A.id "
				+ "FROM bankaccountsria.notebook N JOIN bankaccountsria.bankaccount A JOIN bankaccountsria.user U "
				+ "ON N.savedAccount = A.id AND A.owner = U.id " + "WHERE U.email LIKE ? AND N.user = ?;";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, email + "%");
			pstatement.setInt(2, userId);
			result = pstatement.executeQuery();
			bankAccounts = new ArrayList<>();
			while (result.next()) {
				Notebook notebook = new Notebook();
				notebook.setAccountId(userId);
				notebook.setAccountOwnerEmail(result.getString("email"));
				notebook.setAccountOwnerName(result.getString("surname") + " " + result.getString("name"));
				notebook.setAccountId(result.getInt("id"));
				bankAccounts.add(notebook);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return bankAccounts;
	}

	public void addNotebookAccount(int userId, int accountId) throws SQLException {
		Notebook notebook = getNotebookAccount(userId, accountId);
		if (notebook != null && notebook.getUserId() == userId && notebook.getAccountId() == accountId) {
			return;
		}

		String query = "INSERT INTO bankaccountsria.notebook (user, savedAccount) VALUES (?, ?);";
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, userId);
			pstatement.setInt(2, accountId);
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

	public Notebook getNotebookAccount(Integer user, Integer accountId) throws SQLException {
		Notebook notebook = null;
		String query = "SELECT * FROM bankaccountsria.notebook WHERE user = ? and savedAccount = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, user);
			pstatement.setInt(2, accountId);
			result = pstatement.executeQuery();
			while (result.next()) {
				notebook = new Notebook();
				notebook.setUserId(result.getInt("user"));
				notebook.setAccountId(result.getInt("savedAccount"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return notebook;
	}

}
