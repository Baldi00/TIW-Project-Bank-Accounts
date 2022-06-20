package it.polimi.tiw.bankaccounts.beans;

public class Notebook {
	private int userId;
	private int accountId;
	private String accountOwnerEmail;
	private String accountOwnerName;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getAccountOwnerEmail() {
		return accountOwnerEmail;
	}

	public void setAccountOwnerEmail(String accountOwnerEmail) {
		this.accountOwnerEmail = accountOwnerEmail;
	}

	public String getAccountOwnerName() {
		return accountOwnerName;
	}

	public void setAccountOwnerName(String accountOwnerName) {
		this.accountOwnerName = accountOwnerName;
	}

}
