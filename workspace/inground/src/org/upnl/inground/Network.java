package org.upnl.inground;

import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

public class Network {
	
	private String url;
	private Context context;
	private PersistentCookieStore cookieStore;
	private AsyncHttpClient client = new AsyncHttpClient();
	
	public Network(String url, Context context) {
		this.url = url;
		this.context = context;
		
		cookieStore = new PersistentCookieStore(context);
		client.setCookieStore(cookieStore);
		client.addHeader("Accept", "application/json");
		client.addHeader("Content-type", "application/json");
		client.setTimeout(40 * 1000);
	}
	
	public void post(RequestData requestData, AsyncHttpResponseHandler responseHandler) {
		try {
			StringEntity entity = new StringEntity(new Gson().toJson(requestData));
			client.post(context, url, entity, null, responseHandler);
		}catch(Exception e) {
			// TODO
		}
	}
	
	public String getSessionId() {
		List<Cookie> cookies = cookieStore.getCookies();
		for(int i = 0; i < cookies.size(); i++) {
			Cookie cookie = cookies.get(i);
			if(cookie.getName().equals("INGROUND_SESSION_ID")) {
				return cookie.getValue();
			}
		}
		return "";
	}

}