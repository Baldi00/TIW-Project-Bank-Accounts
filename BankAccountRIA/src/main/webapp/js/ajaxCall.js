function makeCall(method, url, formElement, cback, reset) {
	var req = new XMLHttpRequest();
	req.onreadystatechange = function() {
		cback(req)
	};
	req.open(method, url);
	if (formElement == null) {
		req.send();
	} else {
		let formData = new FormData(formElement);
		req.send(formData);
	}
	if (formElement !== null && reset === true) {
		formElement.reset();
	}
}
