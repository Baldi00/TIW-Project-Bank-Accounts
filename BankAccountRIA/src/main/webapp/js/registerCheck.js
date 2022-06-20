(function() {

	document.getElementById("emailError").hidden = true;
	document.getElementById("passwordTooShortError").hidden = true;
	document.getElementById("passwordMismatchError").hidden = true;

	document.getElementById("registerButton").addEventListener('click', (e) => {
		document.getElementById("successfulRegisterMessage").hidden = true;
		document.getElementById("registerErrorMessage").hidden = true;
		
		var form = e.target.closest("form");
		if (form.checkValidity()) {

			const regex = new RegExp("^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$");

			let name = document.getElementById("registerName").value;
			let surname = document.getElementById("registerSurname").value;
			let email = document.getElementById("registerEmail").value;
			let psw = document.getElementById("registerPassword").value;
			let rpsw = document.getElementById("registerRepeatedPassword").value;

			let errors = false;

			if (name.length == 0 || surname.length == 0 || email.length == 0
				|| psw.length == 0 || rpsw.length == 0) {
				document.getElementById("missingParametersError").hidden = false;
				errors = true;
			} else {
				document.getElementById("missingParametersError").hidden = true;
			}

			if (!regex.test(email)) {
				document.getElementById("emailError").hidden = false;
				errors = true;
			} else {
				document.getElementById("emailError").hidden = true;
			}

			if (psw.length < 8) {
				document.getElementById("passwordTooShortError").hidden = false;
				errors = true;
			} else {
				document.getElementById("passwordTooShortError").hidden = true;
			}


			if (psw != rpsw) {
				document.getElementById("passwordMismatchError").hidden = false;
				errors = true;
			} else {
				document.getElementById("passwordMismatchError").hidden = true;
			}

			if (errors) {
				return;
			}

			makeCall("POST", 'Register', e.target.closest("form"),
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						switch (x.status) {
							case 200:
								document.getElementById("successfulRegisterMessage").hidden = false;
								break;
							case 500:
								let errorMessage = document.getElementById("registerErrorMessage");
								errorMessage.innerHTML = "<td colspan=2>"+message+"</td>";
								errorMessage.hidden = false;
								break;
						}
					}
				}
			);
		} else {
			form.reportValidity();
		}
	});
})();