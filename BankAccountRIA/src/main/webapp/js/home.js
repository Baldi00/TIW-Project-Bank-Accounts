(function() {

	//PAGE INITIALIZATION

	let pageManager;
	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			location.href = "loginRegister.html";
		} else {
			pageManager = new PageManager();
			pageManager.initialize();
			pageManager.showAccountsListFragment();
		}
	}, false);


	class PageManager {

		//Page fragments
		accountsListFragment;
		accountDetailsFragment;
		accountTransferFragment;
		transferSuccessFragment;

		//Page components
		username;
		logoutButton;
		receiverEmail;
		errorMessage;
		transferMoneyButton;
		transferMoneyAmount;
		receiverAccountId;
		reason;
		amountError;
		missingParametersError;
		notebookAccounts;

		//Objects
		accountsList;
		accountDetails;
		transferSuccess;
		transferFailed;

		constructor() {
			this.getPageFragments();
			this.getPageComponents();
		}

		getPageFragments() {
			this.accountsListFragment = document.getElementById("accountsListFragment");
			this.accountDetailsFragment = document.getElementById("accountDetailsFragment");
			this.accountTransferFragment = document.getElementById("accountTransferFragment");
			this.transferSuccessFragment = document.getElementById("transferSuccessFragment");
		}

		getPageComponents() {
			this.username = document.getElementById("username");
			this.logoutButton = document.getElementById("logoutButton");
			this.receiverEmail = document.getElementById("receiverEmail");
			this.errorMessage = document.getElementById("errorMessage");
			this.transferMoneyButton = document.getElementById("transferMoneyButton");
			this.transferMoneyAmount = document.getElementById("transferMoneyAmount");
			this.receiverAccountId = document.getElementById("receiverAccountId");
			this.reason = document.getElementById("reason");
			this.amountError = document.getElementById("amountError");
			this.missingParametersError = document.getElementById("missingParametersError");
			this.notebookAccounts = document.getElementById("notebookAccounts");
		}

		initialize() {
			this.username.innerHTML = sessionStorage.getItem("username");
			this.addEventListenerToLogoutButton();
			this.addEventListenerToTransferMoneyButton();
			this.addEventListenerToReceiverEmailInput();
			this.initializeNotebookHints();
		}

		addEventListenerToLogoutButton() {
			this.logoutButton.addEventListener("click", () => {
				makeCall("GET", 'Logout', null, (x) => {
					if (x.readyState == XMLHttpRequest.DONE && x.status == 200) {
						sessionStorage.removeItem('username');
						location.href = "loginRegister.html";
					}
				});
			}, false);
		}


		addEventListenerToTransferMoneyButton() {
			this.hideTransferSuccessFragments();
			this.transferMoneyButton.addEventListener("click", (e) => {
				this.hideErrorMessage();
				if (this.receiverEmail.value.length == 0 || this.receiverAccountId.value.length == 0
					|| this.transferMoneyAmount.value.length == 0 || this.reason.value.length == 0) {
					this.missingParametersError.hidden = false;
					return;
				} else {
					this.missingParametersError.hidden = true;
				}

				if (isNaN(this.transferMoneyAmount.value) || this.transferMoneyAmount.value < 1) {
					this.amountError.hidden = false;
					return;
				} else {
					this.amountError.hidden = true;
				}

				let form = e.target.closest("form");
				if (form.checkValidity()) {
					makeCall("POST", 'TransferMoney', form, (x) => {
						if (x.readyState == XMLHttpRequest.DONE) {
							var message = x.responseText;
							switch (x.status) {
								case 200:
									pageManager.showTransferSuccessFragment(message);
									pageManager.showAccountsListFragment();
									pageManager.showAccountDetailsFragment();
									break;
								case 400:
									pageManager.showTransferFailedMessage(message);
									break;
								case 401:
									location.href = "loginRegister.html";
									break;
							}
						}
					}, true);
				} else {
					form.reportValidity();
				}
			});
		}


		addEventListenerToReceiverEmailInput() {
			var self = this;
			this.receiverEmail.addEventListener("input", () => {
				makeCall("GET", 'GetNotebookAccountsData?email=' + this.receiverEmail.value, null,
					(x) => {
						if (x.readyState == XMLHttpRequest.DONE) {
							var message = x.responseText;
							switch (x.status) {
								case 200:
									let accounts = JSON.parse(message);
									self.notebookAccounts.innerHTML = "<option value='1, '>Scegli un conto</option>";

									accounts.forEach(function(account) {
										let option = document.createElement("option");
										option.innerHTML = account.accountId + ' - ' + account.accountOwnerName;
										option.setAttribute("value", account.accountId + ", " + account.accountOwnerEmail);
										self.notebookAccounts.appendChild(option);
									});
									self.notebookAccounts.addEventListener("change", (e) => {
										pageManager.receiverAccountId.value = e.target.value.split(", ")[0];
										this.receiverEmail.value = e.target.value.split(", ")[1];
									}, false);
									break;
								case 401:
									location.href = "loginRegister.html";
									break;
								case 400:
								case 500:
									pageManager.showErrorMessage(message);
									break;
							}
						}
					});
			});
		}

		initializeNotebookHints() {
			var e = new Event("input");
			this.receiverEmail.dispatchEvent(e);
		}

		hideTransferSuccessFragments() {
			this.transferSuccessFragment.hidden = true;
		}

		showAccountsListFragment() {
			this.hideErrorMessage();
			this.accountsList = new AccountsList(this);
			this.accountsList.show();
			this.accountsListFragment.hidden = false;
		}

		showAccountDetailsFragment(accountId) {
			this.hideErrorMessage();
			
			if (accountId != null) {
				this.accountDetails = new AccountDetails(accountId);
			}
			
			if (this.accountDetails != null) {
				this.accountDetails.show();
				this.initializeNotebookHints();
				this.accountDetailsFragment.hidden = false;
				this.accountTransferFragment.hidden = false;
			}
		}

		showTransferSuccessFragment(movementId) {
			this.hideErrorMessage();
			this.transferSuccess = new TransferSuccess(movementId);
			this.transferSuccess.show();
			this.transferSuccessFragment.hidden = false;
		}

		showTransferFailedMessage(failReason) {
			this.hideErrorMessage();
			this.transferFailed = new TransferFailed(failReason);
			this.transferFailed.show();
		}

		showErrorMessage(message) {
			this.errorMessage.textContent = message;
			this.errorMessage.hidden = false;
		}

		hideErrorMessage() {
			this.errorMessage.hidden = true;
		}
	}

	class AccountsList {
		accountsListArray;

		show() {
			var self = this;
			makeCall("GET", 'GetAccountsData', null, (x) => {
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;
					switch (x.status) {
						case 200:
							self.accountsListArray = JSON.parse(message);
							if (self.accountsListArray.length != 0) {
								self.print();
							}
							break;
						case 401:
							location.href = "loginRegister.html";
							break;
						case 500:
							pageManager.showErrorMessage(message);
							break;
					}
				}
			});
		}

		print() {
			var container = document.getElementById("accountsList");
			container.innerHTML = "";
			this.accountsListArray.forEach(function(account) {
				let div = document.createElement("div");
				let span = document.createElement("span");
				let a = document.createElement("a");

				div.setAttribute("class", "mb-3");
				span.innerHTML = 'Codice: ' + account.id + ', Saldo: ' + account.balance + ' euro ';
				a.innerHTML = 'Trasferisci denaro/Dettagli';
				a.setAttribute('class', "btn btn-danger");
				a.setAttribute('accountId', account.id);
				a.addEventListener("click", (e) => {
					pageManager.hideTransferSuccessFragments();
					pageManager.showAccountDetailsFragment(e.target.getAttribute("accountId"));
				}, false);
				a.href = "#";

				div.appendChild(span);
				div.appendChild(document.createElement("br"));
				div.appendChild(a);
				container.appendChild(div);
			});
		}
	}

	class AccountDetails {

		accountId;
		accountsDetailsArray;

		constructor(accountId) {
			this.accountId = accountId;
		}

		show() {
			var self = this;
			makeCall("GET", 'GetAccountDetailsData?accountId=' + this.accountId,
				null, (x) => {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						switch (x.status) {
							case 200:
								self.accountsDetailsArray = JSON.parse(message);
								if (self.accountsDetailsArray.length != 0) {
									self.print();
								}
								break;
							case 401:
								location.href = "loginRegister.html";
								break;
							case 400:
							case 500:
								pageManager.showErrorMessage(message);
								break;
						}
					}
				});
		}

		print() {
			let accountDetails = document.getElementById("accountDetails");
			accountDetails.innerHTML = 'Codice: ' + this.accountsDetailsArray.bankAccount.id + ', Saldo: ' + this.accountsDetailsArray.bankAccount.balance + ' euro ';

			let transferMoneySenderAccountId = document.getElementById("transferMoneySenderAccountId");
			transferMoneySenderAccountId.value = this.accountsDetailsArray.bankAccount.id;

			this.printSentMovements();
			this.printReceivedMovements();
		}


		printSentMovements() {
			var accountDetailsSentMovements = document.getElementById("accountDetailsSentMovements");

			accountDetailsSentMovements.innerHTML = "";
			if (this.accountsDetailsArray.sentMovements.length == 0) {
				accountDetailsSentMovements.innerHTML = "Nessun trasferimento effettuato";
			} else {
				let table = document.createElement("table");
				let tbody = document.createElement("tbody");
				table.setAttribute("class", "table table-bordered table-black-border white-opaque-backgroud");
				let tr = document.createElement("tr");
				tr.innerHTML = "<th>Data</th><th>Importo</th><th>Causale</th><th>Conto destinazione</th>";
				tbody.appendChild(tr);

				this.accountsDetailsArray.sentMovements.forEach((movement) => {
					let tr = document.createElement("tr");
					tr.innerHTML = '<td>' + movement.date + '</td><td>' + movement.amount + ' euro</td><td>' + movement.reason + '</td><td>' + movement.receiverAccount + ' di ' + movement.receiverName + '</td>';
					tbody.appendChild(tr);
				});
				table.appendChild(tbody);
				accountDetailsSentMovements.appendChild(table);
			}
		}


		printReceivedMovements() {
			var accountDetailsReceivedMovements = document.getElementById("accountDetailsReceivedMovements");

			accountDetailsReceivedMovements.innerHTML = "";
			if (this.accountsDetailsArray.receivedMovements.length == 0) {
				accountDetailsReceivedMovements.innerHTML = "Nessun trasferimento ricevuto";
			} else {
				let table = document.createElement("table");
				let tbody = document.createElement("tbody");
				table.setAttribute("class", "table table-bordered table-black-border white-opaque-backgroud");
				let tr = document.createElement("tr");
				tr.innerHTML = "<th>Data</th><th>Importo</th><th>Causale</th><th>Conto mittente</th>";
				tbody.appendChild(tr);

				this.accountsDetailsArray.receivedMovements.forEach((movement) => {
					let tr = document.createElement("tr");
					tr.innerHTML = '<td>' + movement.date + '</td><td>' + movement.amount + ' euro</td><td>' + movement.reason + '</td><td>' + movement.senderAccount + ' di ' + movement.senderName + '</td>';
					tbody.appendChild(tr);
				});
				table.appendChild(tbody);
				accountDetailsReceivedMovements.appendChild(table);
			}
		}
	}

	class TransferSuccess {

		movementId;
		movementDetails;

		constructor(movementId) {
			this.movementId = movementId;
		}

		show() {
			var self = this;
			makeCall("GET", 'GetTranferSuccessData?movementId=' + self.movementId,
				null, (y) => {
					if (y.readyState == XMLHttpRequest.DONE) {
						var message = y.responseText;
						switch (y.status) {
							case 200:
								self.movementDetails = JSON.parse(message);
								self.print();
								break;
							case 401:
								location.href = "loginRegister.html";
								break;
							case 400:
							case 500:
								pageManager.showErrorMessage(message);
								break;
						}
					}
				});
		}

		print() {
			let transferSuccessFragment = pageManager.transferSuccessFragment;
			transferSuccessFragment.innerHTML = '<h1 class="text-green">TRASFERIMENTO RIUSCITO</h1>' +
				'<table class="table-center"><tr><td class="align-top">' +
				'<h3>Dettagli trasferimento</h3>' +
				'<table class="table table-bordered table-black-border white-opaque-backgroud"><tbody>' +
				'<tr><th>Importo:</th><td>' + this.movementDetails.movement.amount + ' euro</td></tr>' +
				'<tr><th>Causale:</th><td>' + this.movementDetails.movement.reason + '</td></tr>' +
				'</tbody></table></td><td class="align-top">' +
				'<h3>Conto origine</h3>' +
				'<table class="table table-bordered table-black-border white-opaque-backgroud"><tbody>' +
				'<tr><th>Codice:</th><td>' + this.movementDetails.senderAccount.id + '</td></tr>' +
				'<tr><th>Proprietario:</th><td>' + this.movementDetails.movement.senderName + '</td></tr>' +
				'<tr><th>Saldo prima del trasferimento:</th><td>' + (this.movementDetails.senderAccount.balance + this.movementDetails.movement.amount) + ' euro</td></tr>' +
				'<tr><th>Saldo dopo il trasferimento:</th><td>' + this.movementDetails.senderAccount.balance + ' euro</td></tr>' +
				'</tbody></table></td><td class="align-top">' +
				'<h3>Conto destinazione</h3>' +
				'<table class="table table-bordered table-black-border white-opaque-backgroud"><tbody>' +
				'<tr><th>Codice:</th><td>' + this.movementDetails.receiverAccount.id + '</td></tr>' +
				'<tr><th>Proprietario:</th><td>' + this.movementDetails.movement.receiverName + '</td></tr>' +
				'<tr><th>Saldo prima del trasferimento:</th><td>' + (this.movementDetails.receiverAccount.balance - this.movementDetails.movement.amount) + ' euro</td></tr>' +
				'<tr><th>Saldo dopo il trasferimento:</th><td>' + this.movementDetails.receiverAccount.balance + ' euro</td></tr>' +
				'</tbody></table></td></tr></table>';

			let form = document.createElement("form");
			let button = document.createElement("input");
			let hiddenInput = document.createElement("input");
			let spanOk = document.createElement("span");

			button.setAttribute("type", "button");
			button.setAttribute("class", "btn btn-danger");
			button.setAttribute("value", "Salva dati del conto in rubrica");
			button.addEventListener("click", () => {
				makeCall("POST", 'AddNotebookAccount', form, (x) => {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						switch (x.status) {
							case 200:
								document.getElementById("addNotebookAccountSuccess").hidden = false;
								break;
							case 401:
								window.location.href = "loginRegister.html";
								break;
							case 500:
								pageManager.showError(message);
								break;
						}
					}
				});
			}, false);


			hiddenInput.setAttribute("type", "hidden");
			hiddenInput.setAttribute("name", "accountId");
			hiddenInput.setAttribute("value", this.movementDetails.receiverAccount.id);

			spanOk.setAttribute("id", "addNotebookAccountSuccess");
			spanOk.setAttribute("hidden", true);
			spanOk.setAttribute("class", "text-green");
			spanOk.innerHTML = "Conto salvato in rubrica";

			form.appendChild(button);
			form.appendChild(spanOk);
			form.appendChild(hiddenInput);

			transferSuccessFragment.appendChild(form);
		}
	}

	class TransferFailed {

		failedReason;

		constructor(failedReason) {
			this.failedReason = failedReason;
		}

		show() {
			let alertMessage = "TRASFERIMENTO FALLITO\n";
			switch (this.failedReason) {
				case 'accountDoesNotExist\r\n':
					alertMessage += 'Motivo: Il conto da cui prelevare non esiste';
					break;
				case 'notEnoughMoney\r\n':
					alertMessage += 'Motivo: Il conto da cui prelevare non ha un saldo sufficiente per compiere il trasferimento';
					break;
				case 'wrongAccountOrEmail\r\n':
					alertMessage += 'Motivo: Il codice conto e l\'email del destinatario non coincidono';
					break;
				case 'sameBankAccount\r\n':
					alertMessage += 'Motivo: Tentativo di trasferimento di denaro sullo stesso conto corrente';
					break;
				case 'missingParameters\r\n':
					alertMessage += 'Motivo: Parametri mancanti';
					break;
				case 'genericError\r\n':
					alertMessage += 'Motivo: Errore generico';
					break;
			}
			window.alert(alertMessage);
		}
	}
})();
