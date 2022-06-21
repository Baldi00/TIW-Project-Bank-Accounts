(function() {
	document.getElementById("loginButton").addEventListener('click', (e) => {
		document.getElementById("loginErrorMessage").hidden = true;
		var form = e.target.closest("form");
		if (form.checkValidity()) {
			makeCall("POST", 'Login', form,
				function(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						switch (x.status) {
							case 200:
								sessionStorage.setItem('username', message);
								window.location.href = "home.html";
								break;
							case 500:
								let errorMessage = document.getElementById("loginErrorMessage");
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