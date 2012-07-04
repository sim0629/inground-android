package org.upnl.inground;

class RequestData {
	protected String kind;
	RequestData(String kind) {
		this.kind = kind;
	}
}

class LoginRequestData extends RequestData {
	private String account;
	LoginRequestData(String account) {
		super("login");
		this.account = account;
	}
}

class ResponseData {
	String kind;
	ResponseData() {
	}
}

class LoginResponseData extends ResponseData {
	LoginResponseData() {
	}
}

class ErrorResponseData extends ResponseData {
	String message;
	ErrorResponseData() {
	}
}