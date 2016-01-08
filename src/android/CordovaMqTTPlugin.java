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
    CallbackContext syncCB,asyncCB;
    PluginResult resok,reserr;
    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals("connect")){
            this.syncCB = callbackContext;
            if (connection== null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connect(args.getString(0), args.getString(1), args.getInt(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                sendOnceUpdate("already connected");
            }
            return true;
        }
        if(action.equals("publish")){
            this.syncCB = callbackContext;
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            publish(args);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                sendOnceUpdate("already connected");
            }
            return true;
        }
        if(action.equals("subscribe")){
            this.asyncCB = callbackContext;
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            subscribe(args);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return true;
        }
        if(action.equals("disconnect")){
            this.syncCB = callbackContext;
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        disconnect();
                    }
                });
            }else{
                syncCB.success("already disconnected");
            }
            return true;
        }
        if(action.equals("unsubscribe")){
            this.syncCB = callbackContext;
            if (connection!= null){
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            unsubscribe(args);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return true;
        }

        return false;
    }

    private void connect(String url,String cid,int keepalive) {
        if (url != null && url.length() > 0) {
            Log.i("mqttalabs", url);
            mqtt = new MQTT();
            try {
                mqtt.setHost(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            connection = mqtt.callbackConnection();

            connection.connect(new Callback<Void>() {
                public void onFailure(Throwable value) {
                    Log.e("mqttalabs:fail", value.toString());
                    sendOnceUpdate("failure");
                }

                // Once we connect..
                public void onSuccess(Void v) {
                    Log.d("mqttalabs", "connected");
                }
            });
            connection.listener(new Listener() {

                public void onDisconnected() {
                    sendOnceUpdate("disconnected");
                }

                public void onConnected() {
                    sendOnceUpdate("connected");
                }

                public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
                    //Log.d("mqttalabs", "topic: " + topic.toString());
                    //Log.d("mqttalabs", "payload: " + payload.toString());

                    JSONObject subdata = new JSONObject();
                    try {
                        subdata.put("topic", topic.toString());
                        subdata.put("payload", payload.toString());
                        sendUpdate(subdata);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // You can now process a received message from a topic.
                    // Once process execute the ack runnable.
                    ack.run();
                }

                public void onFailure(Throwable value) {
                    Log.d("mqttalabs", "connection failure: " + value.toString());
                    sendUpdate("failure");
                }
            });
            //callbackContext.success("connecting");
        } else {
            sendUpdate("Expected one non-empty string argument.");
        }
    }


    private void publish(JSONArray args) throws JSONException {
        if(args.toString().length()>0){
            if (connection == null) {
                Log.i("mqttalabs", "connection is null");
            }
            connection.publish(args.getString(0), args.getString(1).getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
                public void onSuccess(Void v) {
                    sendOnceUpdate("published");
                    // the pubish operation completed successfully.
                }

                public void onFailure(Throwable value) {
                    sendOnceUpdate("not published");
                    Log.e("mqttalabs", value.toString());
                }
            });

        }else{
            syncCB.error("error publishing");
        }
    }
    private void subscribe(JSONArray args) throws JSONException {
        if(args.toString().length()>0){
            Topic[] topics = {new Topic(args.getString(0), QoS.AT_LEAST_ONCE)};
            connection.subscribe(topics, new Callback<byte[]>() {
                public void onSuccess(byte[] qoses) {
                    Log.d("mqttalabs", "subscribed");

                    sendUpdate("subscribed");
//                    PluginResult result = new PluginResult(PluginResult.Status.OK,qoses.toString());
//                    result.setKeepCallback(true);
//                    cbctx.sendPluginResult(result);
                    // The result of the subcribe request.
                    Log.d("mqttalabs", qoses.toString());
                }

                public void onFailure(Throwable value) {
                    sendUpdate("not subscribed");
                    Log.d("mqttalabs", "not subscribed\n" + value.toString());
                }
            });
            //cbctx.success("subscribing");
        }else{
            asyncCB.error("error subscribing");
        }

    }
    private void disconnect(){
        Log.i("mqttplugin","disconnecting..");
        connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Log.i("mqttplugin","disconnect");
                sendOnceUpdate("disconnect");
                connection = null;
            }

            @Override
            public void onFailure(Throwable value) {
                Log.e("mqtt plugin", value.toString());
                sendOnceUpdate("disconnect failure");
            }
        });
    }
    private void unsubscribe(JSONArray args) throws JSONException {
        if(args.toString().length()>0){
            UTF8Buffer[] topic = new UTF8Buffer[1];
            topic[0] = new UTF8Buffer(args.getString(0));
            connection.unsubscribe(topic, new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    sendOnceUpdate("unsubscribe is success");
                }

                @Override
                public void onFailure(Throwable value) {
                    sendOnceUpdate("unsubscribe is failure :" + value.toString());
                }
            });
        }else{
            syncCB.error("error unsubscribing");
        }

    }
    private void sendOnceUpdate(String message){
        if (syncCB != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK,message);
            result.setKeepCallback(false);
            syncCB.sendPluginResult(result);

            Log.i("mqttalabs","\nfor subscribe the callback id is "+syncCB.getCallbackId());
        }
    }
    private void sendUpdate(String message){
        if (asyncCB != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK,message);
            result.setKeepCallback(true);
            asyncCB.sendPluginResult(result);

            Log.i("mqttalabs","\nfor subscribe the callback id is "+asyncCB.getCallbackId());
        }
    }
    private void sendUpdate(JSONObject message){
        if (asyncCB != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK,message);
            result.setKeepCallback(true);
            asyncCB.sendPluginResult(result);

            Log.i("mqttalabs","\nfor subscribe the callback id is "+asyncCB.getCallbackId());
        }
    }

}
