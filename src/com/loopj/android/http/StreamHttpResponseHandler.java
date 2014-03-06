package com.loopj.android.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;

import android.os.Message;

public class StreamHttpResponseHandler extends AsyncHttpResponseHandler {
    
	protected static final int PARSED_MESSAGE = 745634;
    protected static final int FAILED_MESSAGE = 786593;

    // Interface to AsyncHttpRequest
    void sendResponseMessage(HttpResponse response) {
        StatusLine status = response.getStatusLine();
        Object responseBody = null;
        try {
            HttpEntity entity = null;
            HttpEntity temp = response.getEntity();
            if(temp != null) {
            	entity = new BufferedHttpEntity(temp);
                InputStream is = entity.getContent();
                responseBody = parseStream(is);
                is.close();
            }
        } catch(IOException e) {
            sendFailureMessage(e, (String) null);
        }

        if(status.getStatusCode() >= 300) {
            sendFailureMessage(new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()), responseBody);
        } else {
            sendSuccessMessage(status.getStatusCode(), responseBody);
        }
    }
    
    protected void sendSuccessMessage(int statusCode, Object responseBody) {
        sendMessage(obtainMessage(PARSED_MESSAGE, new Object[] {Integer.valueOf(statusCode), responseBody}));
    }

    protected void sendFailureMessage(Throwable e, Object responseBody) {
        sendMessage(obtainMessage(FAILED_MESSAGE, new Object[]{e, responseBody}));
    }
    
    protected Object parseStream(InputStream stream) { return null; }
    
    //
    // Callbacks to be overridden, typically anonymously
    //

    public void onSuccess(Object response) {}

    public void onSuccess(int statusCode, Object response) {
        onSuccess(response);
    }

    public void onFailure(Throwable e, Object errorResponse) {}

    @Override
    public void onFailure(Throwable error, String content) {
        onFailure(error, (Object)content);
    }

    protected void handleParsedMessage(int statusCode, Object responseBody) {
        onSuccess(statusCode, responseBody);
    }

    protected void handleFailedMessage(Throwable e, Object responseBody) {
        onFailure(e, responseBody);
    }

    @Override
    protected void handleMessage(Message msg) {
        switch(msg.what){
            case PARSED_MESSAGE:
                Object[] response = (Object[]) msg.obj;
                handleParsedMessage(((Integer) response[0]).intValue(), response[1]);
                break;
            case FAILED_MESSAGE:
                response = (Object[])msg.obj;
                handleFailedMessage((Throwable)response[0], response[1]);
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
