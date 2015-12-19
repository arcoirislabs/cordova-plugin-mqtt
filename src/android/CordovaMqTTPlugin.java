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
    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if(action.equals("connect")){
            if (connection== null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connect(args.getString(0), args.getString(1), args.getInt(2), callbackContext);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                callbackContext.success("Connection is already established.");
            }
            return true;
        }
        if(action.equals("publish")){
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            publish(args, callbackContext);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                callbackContext.error("No connection is established.");
            }

            return true;
        }
        if(action.equals("subscribe")){
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            subscribe(args, callbackContext);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                callbackContext.error("No connection is established.");
            }
            return true;
        }
        if(action.equals("unsubscribe")){
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            unsubscribe(args, callbackContext);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                callbackContext.error("No connection is established.");
            }

            return true;
        }
        if(action.equals("disconnect")){
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            disconnect(callbackContext);
                            connection = null;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                callbackContext.error("No connection is established.");
            }
            return true;
        }

        return false;
    }


    private void connect(String url,String cid,int keepalive, final CallbackContext callbackContext) {
        if (url != null && url.length() > 0) {
            Log.i("mqtt plugin",url);
            mqtt = new MQTT();
            connectionCb = callbackContext;
            try {
                mqtt.setHost(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            connection = mqtt.callbackConnection();

            connection.connect(new Callback<Void>() {
                public void onFailure(Throwable value) {
                    Log.e("mqttalabs:fail", value.toString());
                    //sendConnectionData("failure",false);
                }

                // Once we connect..
                public void onSuccess(Void v) {
                    Log.d("mqttalabs", "connected");
                }
            });
            connection.listener(new Listener() {

                public void onDisconnected() {
                    sendConnectionData("disconnected", true);
                }

                public void onConnected() {
                    sendConnectionData("connected", true);
                }

                public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
                    Log.d("mqttalabs", "topic: " + topic.toString());
                    Log.d("mqttalabs", "payload: " + payload.toString());

                    JSONObject subdata = new JSONObject();
                    try {
                        subdata.put("topic", topic.toString());
                        subdata.put("payload", payload.toString());
                        sendSubscribedData(subdata.toString(),true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // You can now process a received message from a topic.
                    // Once process execute the ack runnable.
                    ack.run();
                }

                public void onFailure(Throwable value) {
                    Log.d("mqttalabs", "connection failure: " + value.toString());
                    sendConnectionData("failure",false);
                }
            });
            //callbackContext.success("connecting");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }


    private void publish(JSONArray args, final CallbackContext cbctx) throws JSONException {
        if(args.toString().length()>0){
            connectionCb = cbctx;
            connection.publish(args.getString(0), args.getString(1).getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
                public void onSuccess(Void v) {
                    sendPublishedData("published", true);
                    // the pubish operation completed successfully.
                }

                public void onFailure(Throwable value) {
                    sendPublishedData("not published", false);
                }
            });

        }else{
            cbctx.error("error publishing");
        }
    }
    private void subscribe(JSONArray args, final CallbackContext cbctx) throws JSONException {
        if(args.toString().length()>0){
            Topic[] topics = {new Topic(args.getString(0), QoS.AT_LEAST_ONCE)};
            connectionCb = cbctx;
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Log.d("mqttalabs", "subscribed");

                    sendSubscribedData("subscribed", true);
//                    PluginResult result = new PluginResult(PluginResult.Status.OK,qoses.toString());
//                    result.setKeepCallback(true);
//                    cbctx.sendPluginResult(result);
                    // The result of the subcribe request.
                    Log.d("mqttalabs", qoses.toString());
                }

                public void onFailure(Throwable value) {
                    sendSubscribedData("not subscribed", false);
                    Log.d("mqttalabs", "not subscribed\n" + value.toString());
                }
            });
            //cbctx.success("subscribing");
        }else{
            cbctx.error("error subscribing");
        }

    }
    private void disconnect(final CallbackContext cbctx) throws JSONException {
        connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                cbctx.success("disconnect");
            }

            @Override
            public void onFailure(Throwable value) {
                Log.e("mqtt plugin", value.toString());
                cbctx.error("failure");
            }
        });
    }
    private void unsubscribe(JSONArray args, final CallbackContext cbctx) throws JSONException {
        if(args.toString().length()>0){
            UTF8Buffer[] topic = new UTF8Buffer[1];
            topic[0] = new UTF8Buffer(args.getString(0));
            connectionCb = cbctx;
            connection.unsubscribe(topic, new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    sendUnsubscribedData(value.toString(),true);
                }

                @Override
                public void onFailure(Throwable value) {
                    sendUnsubscribedData(value.toString(),false);
                }
            });
        }else{
            cbctx.error("error unsubscribing");
        }

    }
    private void sendSubscribedData(String message, boolean success){
        PluginResult result;
        if (connectionCb != null) {
            if(success){
                result = new PluginResult(PluginResult.Status.OK,message);
            }else{
                result = new PluginResult(PluginResult.Status.ERROR,message);
            }
            result.setKeepCallback(true);
            connectionCb.sendPluginResult(result);
        }
    }
    private void sendUnsubscribedData(String message, boolean success){
        PluginResult result;
        if (connectionCb != null) {
            if(success){
                result = new PluginResult(PluginResult.Status.OK,message);
            }else{
                result = new PluginResult(PluginResult.Status.ERROR,message);
            }
            result.setKeepCallback(true);
            connectionCb.sendPluginResult(result);
        }
    }
    private void sendPublishedData(String message, boolean success){
        PluginResult result;
        if (connectionCb != null) {
            if (success){
                result = new PluginResult(PluginResult.Status.OK,message);
            }else{
                result = new PluginResult(PluginResult.Status.ERROR,message);
            }
            result.setKeepCallback(true);
            connectionCb.sendPluginResult(result);
        }
    }
    private void sendConnectionData(String message,boolean success){
        PluginResult result;
        if (connectionCb != null) {
            if(success){
                result = new PluginResult(PluginResult.Status.OK,message);
            }else{
                result = new PluginResult(PluginResult.Status.ERROR,message);
            }

            result.setKeepCallback(true);
            connectionCb.sendPluginResult(result);
        }
    }
}