package it.polimi.tiw.bankaccounts.beans;

import java.sql.Date;

public class Movement {
	private int id;
	private Date date;
	private double amount;
	private String reason;
	private int senderAccount;
	private int receiverAccount;
	private String senderName;
	private String receiverName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getSenderAccount() {
		return senderAccount;
	}

	public void setSenderAccount(int senderAccount) {
		this.senderAccount = senderAccount;
	}

	public int getReceiverAccount() {
		return receiverAccount;
	}

	public void setReceiverAccount(int receiverAccount) {
		this.receiverAccount = receiverAccount;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
}
