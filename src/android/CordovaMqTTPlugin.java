package com.arcoirislabs.plugin.mqtt;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaMqTTPlugin extends CordovaPlugin {
    CallbackContext syncCB,asyncCB;
    MqttAsyncClient client;
    boolean connected;
    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals("connect")){
            this.asyncCB = callbackContext;
//            if (connection== null){
//
//            }else{
//                sendOnceUpdate("already connected");
//            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect(args.getString(0), args.getString(1), args.getInt(2), args.getBoolean(3), args.getInt(4), args.getString(5), args.getString(6), args.getString(7), args.getString(8), args.getInt(9), args.getBoolean(10), args.getString(11));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        if(action.equals("publish")){
            this.syncCB = callbackContext;
//            if (connection!= null){
//
//            }else{
//                sendOnceUpdate("already connected");
//            }
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
            return true;
        }
        if(action.equals("subscribe")){

            this.syncCB = callbackContext;
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
            return true;
        }
        if(action.equals("disconnect")){
            this.syncCB = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            });
            return true;
        }
        if(action.equals("unsubscribe")){
            this.syncCB = callbackContext;
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
            return true;
        }

        return false;
    }

    private void connect(String url,String cid,int ka,boolean cleanSess,int connTimeOut,String uname, String pass,String willTopic,String willPayload,int willQos,boolean willRetain,String version) {
        MemoryPersistence persistence = new MemoryPersistence();
        final MqttConnectOptions connOpts = new MqttConnectOptions();
        connected = false;
        try {
            //Log.i("mqttalabs", "client id is " + cid);
            if (cid==null){
                cid = client.generateClientId();
            }
            connOpts.setCleanSession(cleanSess);
            connOpts.setKeepAliveInterval(ka);
            Log.i("mqttalabs", "username " + uname +" . Password is " + pass);

            client = new MqttAsyncClient(url, cid, persistence);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    connected = false;
                    Log.i("mqttalabs", cause.toString());
                    JSONObject dis = new JSONObject();
                    try {
                        dis.put("type", "connectionLost");
                        dis.put("message", cause.toString());
                        dis.put("call", "disconnected");
                        dis.put("connectionStatus", client.isConnected());
                        sendUpdate(dis);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //Log.i("mqttalabs", "topic is " + topic + ". payload is " + message.toString());
                    JSONObject dis = new JSONObject();
                    try {
                        dis.put("type", "messageArrived");
                        dis.put("topic", topic);
                        dis.put("payload", message);
                        dis.put("call", "onPublish");
                        dis.put("connectionStatus", client.isConnected());
                        dis.put("qos",message.getQos());
                        dis.put("isRetained",message.isRetained());
                        dis.put("isDuplicate",message.isDuplicate());
                        sendUpdate(dis);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        token.waitForCompletion();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (willTopic!=null&&willPayload!=null&&willQos>-1){

                connOpts.setWill(willTopic,willPayload.getBytes(),willQos,willRetain);
            }

            if(uname.toString()=="null"&&pass.toString()=="null"){
                Log.i("mqttalabs","not applying creds");

            }else{
                Log.i("mqttalabs","applying creds");
                connOpts.setUserName(uname);
                connOpts.setPassword(pass.toCharArray());
            }
            //connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            connOpts.setConnectionTimeout(connTimeOut);
            client.connect(connOpts, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connected = true;
                    JSONObject dis = new JSONObject();
                    try {
                        dis.put("type", "connected");
                        dis.put("call", "connected");
                        dis.put("response", "connected");
                        dis.put("connectionStatus", client.isConnected());
                        sendUpdate(dis);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    connected = false;
                    JSONObject dis = new JSONObject();
                    try {
                        dis.put("type", "failure");
                        dis.put("call", "failure");
                        dis.put("response", "fail to connect");
                        dis.put("message", exception.toString());
                        dis.put("connectionStatus", client.isConnected());
                        sendUpdate(dis);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void publish(JSONArray args) throws JSONException {
        final MqttMessage payload = new MqttMessage();
        payload.setPayload(args.getString(1).getBytes());
        payload.setQos(args.getInt(2));
        payload.setRetained(args.getBoolean(3));
        Log.i("mqttalabs", "Topic is " + args.getString(0) + ". Payload is " + args.getString(1));
        try {
            if (client!=null){
                client.publish(args.getString(0), payload, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        JSONObject dis = new JSONObject();
                        try {
                            dis.put("type", "publish");
                            dis.put("call", "success");
                            dis.put("response", "published");
                            dis.put("isPayloadDuplicate", payload.isDuplicate());
                            dis.put("qos", payload.getQos());
                            dis.put("connectionStatus", client.isConnected());
                            sendOnceUpdate(dis);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        JSONObject dis = new JSONObject();
                        try {
                            dis.put("type", "publish");
                            dis.put("call", "failure");
                            dis.put("response", "not published");
                            dis.put("isPayloadDuplicate", payload.isDuplicate());
                            dis.put("qos", payload.getQos());
                            dis.put("connectionStatus", client.isConnected());
                            sendOnceUpdate(dis);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                Log.e("mqttalabs","client var is null");
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    private void subscribe(final JSONArray args) throws JSONException {
        try {
            client.subscribe(args.getString(0), args.getInt(1), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    JSONObject dis = new JSONObject();
                    try {
                        dis.put("type", "subscribe");
                        dis.put("call", "success");
                        dis.put("response", "subscribed to " + args.getString(0));
                        dis.put("connectionStatus", client.isConnected());
                        sendOnceUpdate(dis);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    JSONObject dis = new JSONObject();
                    try {
                        dis.put("type", "subscribe");
                        dis.put("call", "failure");
                        dis.put("response", "subscribed to " + args.getString(0));
                        dis.put("message", exception.getMessage());
                        dis.put("connectionStatus", client.isConnected());
                        sendOnceUpdate(dis);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void disconnect(){
        try {

            client.disconnect(4, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        JSONObject res = new JSONObject();
                        try {
                            res.put("type","disconnect");
                            res.put("call","success");
                            res.put("connectionStatus",client.isConnected());
                            sendOnceUpdate(res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        asyncActionToken.waitForCompletion();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Log.i("mqttalabs","disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("mqttalabs"," cannot disconnect --> "+ exception.getMessage());
                    try {
                        JSONObject res = new JSONObject();
                        try {
                            res.put("type","disconnect");
                            res.put("call","failure");
                            res.put("connectionStatus",client.isConnected());
                            res.put("message",exception.toString());
                            sendOnceUpdate(res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        asyncActionToken.waitForCompletion();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void unsubscribe(final JSONArray args) throws JSONException {
        try {
            client.unsubscribe(args.getString(0), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        JSONObject res = new JSONObject();
                        try {
                            res.put("type","unsubscribe");
                            res.put("call","success");
                            res.put("connectionStatus",client.isConnected());
                            res.put("unsubscribedTopic",args.getString(0));
                            sendOnceUpdate(res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        asyncActionToken.waitForCompletion();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    try {
                        JSONObject res = new JSONObject();
                        try {
                            res.put("type","unsubscribe");
                            res.put("call","failure");
                            res.put("connectionStatus",client.isConnected());
                            res.put("unsubscribedTopic",args.getString(0));
                            res.put("message",exception.toString());
                            sendOnceUpdate(res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        asyncActionToken.waitForCompletion();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void sendOnceUpdate(JSONObject message){
        if (syncCB != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK,message);
            result.setKeepCallback(false);
            syncCB.sendPluginResult(result);

            Log.i("mqttalabs","\nfor subscribe the callback id is "+syncCB.getCallbackId());
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
 