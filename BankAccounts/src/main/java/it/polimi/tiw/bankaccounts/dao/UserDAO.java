package it.polimi.tiw.bankaccounts.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.bankaccounts.beans.User;

public class UserDAO {
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public User checkUserCredentials(String email, String password) throws SQLException {
		User user = null;
		String query = "SELECT * FROM bankaccounts.user WHERE email = ? and password = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, email);
			pstatement.setString(2, password);
			result = pstatement.executeQuery();
			while (result.next()) {
				user = new User();
				user.setId(result.getInt("id"));
				user.setName(result.getString("name"));
				user.setSurname(result.getString("surname"));
				user.setEmail(result.getString("email"));
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
		return user;
	}

	public boolean checkIfUserPresent(String email) throws SQLException {
		String query = "SELECT * FROM bankaccounts.user WHERE email = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		boolean alreadyPresent;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, email);
			result = pstatement.executeQuery();
			alreadyPresent = result.next();
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

		return alreadyPresent;
	}

	public User registerUser(String name, String surname, String email, String password) throws SQLException {
		String query = "INSERT INTO bankaccounts.user (name, surname, email, password) VALUES (?, ?, ?, ?);";
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, name);
			pstatement.setString(2, surname);
			pstatement.setString(3, email);
			pstatement.setString(4, password);
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

		return checkUserCredentials(email, password);
	}

	public User getUserByAccount(int accountId) throws SQLException {
		User user = null;
		String query = "SELECT U.* FROM bankaccounts.user U JOIN bankaccounts.bankaccount A ON U.id = A.owner WHERE A.id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, accountId);
			result = pstatement.executeQuery();
			while (result.next()) {
				user = new User();
				user.setId(result.getInt("id"));
				user.setName(result.getString("name"));
				user.setSurname(result.getString("surname"));
				user.setEmail(result.getString("email"));
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
		return user;
	}

}
