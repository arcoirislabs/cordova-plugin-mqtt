package com.arcoirislabs.plugin.mqtt;

import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaMqTTPlugin extends CordovaPlugin {

    MQTT mqtt = null;
    CallbackConnection connection = null;
    CallbackContext connectionCb;
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

            return true;
        }
        if(action.equals("publish")){
            this.publish(args,callbackContext);
            return true;
        }
        if(action.equals("subscribe")){
            this.subscribe(args,callbackContext);
            return true;
        }
        if(action.equals("disconecct")){
            this.disconnect(callbackContext);
            return true;
        }

        return false;
    }

    private void connect(String url,int port,String cid,int keepalive, final CallbackContext callbackContext) {
        if (url != null && url.length() > 0) {
            Log.i("mqttalabs", url);
            Log.i("mqttalabs", String.valueOf(port));
            mqtt = new MQTT();
            connectionCb = callbackContext;
            try {
                mqtt.setHost("tcp://test.mosquitto.org:1883");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            connection = mqtt.callbackConnection();

            connection.connect(new Callback<Void>() {
                public void onFailure(Throwable value) {
                    Log.e("mqttalabs:fail", value.toString());
                    sendConnectionData("failure");
                }

                // Once we connect..
                public void onSuccess(Void v) {
                    Log.d("mqttalabs", "connected");
                }
            });
            connection.listener(new Listener() {

                public void onDisconnected() {
                    sendConnectionData("disconnected");
                }

                public void onConnected() {
                    sendConnectionData("connected");
                }

                public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
                    Log.d("mqttalabs", "topic: " + topic.toString());
                    Log.d("mqttalabs", "payload: " + payload.toString());

                    JSONObject subdata = new JSONObject();
                    try {
                        subdata.put("topic", topic.toString());
                        subdata.put("payload", payload.toString());
                        sendSubscribedData(subdata.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // You can now process a received message from a topic.
                    // Once process execute the ack runnable.
                    ack.run();
                }

                public void onFailure(Throwable value) {
                    Log.d("mqttalabs", "connection failure: " + value.toString());
                    sendConnectionData("failure");
                }
            });
            //callbackContext.success("connecting");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }


    private void publish(JSONArray args, final CallbackContext cbctx) throws JSONException {
        //cbctx.sendPluginResult();
    }
    private void subscribe(JSONArray args, final CallbackContext cbctx) throws JSONException {
        if(args.toString().length()>0){
            Topic[] topics = {new Topic(args.getString(0), QoS.AT_LEAST_ONCE)};
            connectionCb = cbctx;
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Log.d("mqttalabs", "subscribed");

                    sendSubscribedData("subscribed");
//                    PluginResult result = new PluginResult(PluginResult.Status.OK,qoses.toString());
//                    result.setKeepCallback(true);
//                    cbctx.sendPluginResult(result);
                    // The result of the subcribe request.
                    Log.d("mqttalabs", qoses.toString());
                }

                public void onFailure(Throwable value) {
                    sendSubscribedData("not subscribed");
                    Log.d("mqttalabs", "not subscribed\n" + value.toString());
                }
            });
            //cbctx.success("subscribing");
        }else{
            cbctx.error("error subscribing");
        }

    }
    private void disconnect(CallbackContext cbctx) throws JSONException {

    }
    private void sendSubscribedData(String message){
        if (connectionCb != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK,message);
            result.setKeepCallback(true);
            connectionCb.sendPluginResult(result);
        }
    }
    private void sendConnectionData(String message){
        if (connectionCb != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK,message);
            result.setKeepCallback(true);
            connectionCb.sendPluginResult(result);
        }
    }
}
