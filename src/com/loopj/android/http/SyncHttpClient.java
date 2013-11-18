package com.loopj.android.http;

import java.io.InputStream;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.os.Message;

public abstract class SyncHttpClient extends AsyncHttpClient {
	private int responseCode;
	/*
	 * as this is a synchronous request this is just a helping mechanism to pass
	 * the result back to this method. Therefore the result object has to be a
	 * field to be accessible
	 */
	private Object result;
	AsyncHttpResponseHandler responseHandler = new StreamHttpResponseHandler() {

		void sendResponseMessage(org.apache.http.HttpResponse response) {
			responseCode = response.getStatusLine().getStatusCode();
			super.sendResponseMessage(response);
		};
		
		protected Object parseStream(InputStream stream) {
			return parseResponseStream(stream);
		};

		@Override
		protected void sendMessage(Message msg) {
			/*
			 * Dont use the handler and send it directly to the analysis
			 * (because its all the same thread)
			 */
			handleMessage(msg);
		}

		@Override
		public void onSuccess(Object content) {
			result = content;
		}

		@Override
		public void onFailure(Throwable error, Object content) {
			result = onRequestFailed(error, content);
		}
	};
	
	protected abstract Object parseResponseStream(InputStream stream);

	/**
	 * @return the response code for the last request, might be usefull
	 *         sometimes
	 */
	public int getResponseCode() {
		return responseCode;
	}

	// Private stuff
	protected void sendRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, AsyncHttpResponseHandler responseHandler,
			Context context) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		/*
		 * will execute the request directly
		 */
		new AsyncHttpRequest(client, httpContext, uriRequest, responseHandler)
				.run();
	}

	public abstract Object onRequestFailed(Throwable error, Object content);

	public void delete(String url, RequestParams queryParams,
			AsyncHttpResponseHandler responseHandler) {
		// TODO what about query params??
		delete(url, responseHandler);
	}

	public Object get(String url, RequestParams params) {
		this.get(url, params, responseHandler);
		/*
		 * the response handler will have set the result when this line is
		 * reached
		 */
		return result;
	}

	public Object get(String url) {
		this.get(url, null, responseHandler);
		return result;
	}

	public Object put(String url, RequestParams params) {
		this.put(url, params, responseHandler);
		return result;
	}

	public Object put(String url) {
		this.put(url, null, responseHandler);
		return result;
	}

	public Object post(String url, RequestParams params) {
		this.post(url, params, responseHandler);
		return result;
	}

	public Object post(String url) {
		this.post(url, null, responseHandler);
		return result;
	}

	public Object delete(String url, RequestParams params) {
		this.delete(url, params, responseHandler);
		return result;
	}

	public Object delete(String url) {
		this.delete(url, null, responseHandler);
		return result;
	}

}
