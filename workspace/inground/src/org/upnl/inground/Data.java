package org.upnl.inground;

class RequestData {
	String kind;
	RequestData(String kind) {
		this.kind = kind;
	}
}

class LoginRequestData extends RequestData {
	String account;
	LoginRequestData(String account) {
		super("login");
		this.account = account;
	}
}

class MapRequestData extends RequestData {
	MapRequestData() {
		super("map");
	}
}

class StartRequestData extends RequestData {
	double[] location;
	StartRequestData(double lat, double lng) {
		super("start");
		this.location = new double[] { lat, lng };
	}
}

class GrabRequestData extends RequestData {
	double[] location;
	GrabRequestData(double lat, double lng) {
		super("grab");
		this.location = new double[] { lat, lng };
	}
}

class ThrowRequestData extends RequestData {
	double[] velocity;
	ThrowRequestData(double y, double x) {
		super("throw");
		this.velocity = new double[] { y, x };
	}
}

class PollRequestData extends RequestData {
	PollRequestData() {
		super("poll");
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

class MapResponseData extends ResponseData {
	double[][] map;
	MapResponseData() {
	}
}

class StartResponseData extends ResponseData {
	boolean success;
	StartResponseData() {
	}
}

class GrabResponseData extends ResponseData {
	boolean success;
	GrabResponseData() {
	}
}

class ThrowResponseData extends ResponseData {
	boolean success;
	double[] location;
	ThrowResponseData() {
	}
}

class GroundResponseData extends ResponseData {
	String account;
	int[] ground;
	GroundResponseData() {
	}
}

class FinishResponseData extends ResponseData {
	FinishResultData[] result;
	FinishResponseData() {
	}
	class FinishResultData {
		String account;
		int nofcells;
		FinishResultData() {
		}
	}
}

class ErrorResponseData extends ResponseData {
	String message;
	ErrorResponseData() {
	}
}