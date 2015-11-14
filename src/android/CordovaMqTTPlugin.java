package com.arcoirislabs.plugin.mqtt;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaMqTTPlugin extends CordovaPlugin {

    MQTT mqtt = null;
    CallbackConnection connection = null;
    PluginResult resok,reserr;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals("connect")){
            String url, cid;
            int port,ka;
            url = args.getString(0);
            port = args.getInt(1);
            cid = args.getString(2);
            ka = args.getInt(3);
            this.connect(url,port,cid,ka, callbackContext);
        }
        if(action=="publish"){
            this.publish(args,callbackContext);
        }
        if(action=="subscribe"){
            this.subscribe(args,callbackContext);
        }
        if(action=="disconecct"){
            this.disconnect(callbackContext);
        }
        if(action.equals("coolMethod")){
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
        }

        return false;
    }


    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    private void connect(String url,int port,String cid,int keepalive, final CallbackContext callbackContext) {
        if (url != null && url.length() > 0) {
            Log.i("mqttalabs", url);
            Log.i("mqttalabs", String.valueOf(port));
            mqtt = new MQTT();
            try {
                mqtt.setHost("tcp://test.mosquitto.org:1883");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            connection = mqtt.callbackConnection();

            connection.connect(new Callback<Void>() {
                public void onFailure(Throwable value) {
                    Log.e("mqttalabs", "failure");

                }

                // Once we connect..
                public void onSuccess(Void v) {
                    Log.d("mqttalabs", "connected");
                }
            });
            callbackContext.success("connecting");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }


    private void publish(JSONArray args, final CallbackContext cbctx) throws JSONException {
        //cbctx.sendPluginResult();
    }
    private void subscribe(JSONArray args, CallbackContext cbctx) throws JSONException {

    }
    private void disconnect(CallbackContext cbctx) throws JSONException {

    }
}
