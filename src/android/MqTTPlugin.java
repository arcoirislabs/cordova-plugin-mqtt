package com.arcoirislabs.plugin.mqtt;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
/**
 * This class echoes a string called from JavaScript.
 */

public class MqTTPlugin extends CordovaPlugin {
    public String id;
    public CordovaWebView webView;					// WebView object
    public CordovaInterface cordova;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        assert this.cordova == null;
        this.cordova = cordova;
        this.webView = webView;
    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        String url = args.getString(0);
            String clientId =args.getString(1);
            Boolean quietMode = args.getString(2);
            String username = args.getString(3);
            String password = args.getString(4);
            Boolean cleanSession = args.getString(5);
            String topic = args.getString(6);
            String qos = args.getString(7);
            String message = args.getString(8);
        if (action.equals("publish")) {
            this.publish(url, clientId, quietMode, username, password, cleanSession, topic , qos, message, callbackContext);
            return true;
        }
        if (action.equals("subscribe")) {
            this.subscribe(url, clientId, quietMode, username, password, cleanSession, topic , qos, callbackContext);
            return true;
        }
        return false;
    }

    
    
    private void publish(String url, String clientId, String quietMode, String username, String password, String cleanSession, String topic , String qos, String message, CallbackContext callbackContext); {
        //Mqtt.publish(url, clientId, quietMode, username, password, cleanSession, topic , qos, message);
        if (message != null && message.length() > 0) {
        	String html= "Publish";
            callbackContext.success(html);

        } else {
            callbackContext.error("Check your parameters");
        }
    }
    private void subscribe(String url, String clientId, String quietMode, String username, String password, String cleanSession, String topic , String qos, CallbackContext callbackContext); {
        //Mqtt.publish(url, clientId, quietMode, username, password, cleanSession, topic , qos, message);
        if (url != null && url.length() > 0) {
            String html= "Subscribe";
            callbackContext.success(html);

        } else {
            callbackContext.error("Check your parameters");
        }
    }
}
