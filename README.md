# cordova-plugin-mqtt

[![Join the chat at https://gitter.im/arcoirislabs/cordova-plugin-mqtt](https://badges.gitter.im/arcoirislabs/cordova-plugin-mqtt.svg)](https://gitter.im/arcoirislabs/cordova-plugin-mqtt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

cordova-plugin-mqtt is plugin for building MQTT client for multiple platforms in Apache Cordova. Currently Android platform is present and next support is planned for iOS & Windows Phone. 

### Cordova platform support
5.x (CLI)
4.x (Cordova Android)

### Version
0.2.7 (Fixed some bugs. Check out the sample app code to build a proper MQTT client.)

### Installation

Install plugins via plugin repository or GitHub

```sh
$ cordova plugin add cordova-plugin-mqtt
```

```sh
$ cordova plugin add https://github.com/arcoirislabs/cordova-plugin-mqtt.git
```

### Changelog
1. New sample application is added.
2. Fixed the subscribe callback issue.
3. Some minor workarounds.

### Documentation

[UPDATE]

We have written a tutorial for this plugin over [here](https://medium.com/@arcoirislabs/using-mqtt-on-apache-cordova-564d4fab526b). Kindly check out before you start developing. Cheers
##### Methods
1. [connect](#connect)
2. [publish](#publish)
3. [subscribe](#subscribe)
4. [unsubscribe](#unsubscribe)
5. [disconnect](#disconnect)

##### Events
Default listeners you can program anywhere for following events
 - connected
 - disconnected
 - failure (connection)
 - subscribed
 - not subscribed
 - published
 - not published

For example you can configure the event in this way

 ```javascript
 document.addEventListener("connected",function(e){
  console.log(e.type)
 },false)
```


##### connect
To connect to a broker. This plugin doesn't supports mqtt:// protocol. Use tcp:// instead.

```javascript
cordova.plugins.CordovaMqTTPlugin.connect({
    url:"tcp://test.mosquitto.org", //a public broker used for testing purposes only. Try using a self hosted broker for production.
    port:1883, 
    clientId:"YOUR_USER_ID_LESS_THAN_24_CHARS",
    connectionTimeout:3000,
    willTopicConfig:{
        qos:0,
        retain:true,
        topic:"<will topic>",
        payload:"<will topic message>"
    },
    username:"uname",
    password:'pass',
    keepAlive:60,
    success:function(s){
        console.log("connect success");
    },
    error:function(e){
        console.log("connect error");
    },
    onConnectionLost:function (){
        console.log("disconnect");
    }
})
```

##### publish
To publish to a channel. You can use this function.

```javascript
cordova.plugins.CordovaMqTTPlugin.publish({
   topic:"sampletopic",
   payload:"hello from the plugin",
   qos:0,
   retain:false
  success:function(s){

  },
  error:function(e){
  
  }
})
```
In order to debug the publish call you can either go for callbacks in the function or events. Once published the function will call the "published" event & the success callback else the function will call both "not published" event & error callback. 

##### subscribe
To subscribe to a channel. You can use this function.

```javascript
cordova.plugins.CordovaMqTTPlugin.subscribe({
   topic:"sampletopic",
   qos:0,
  success:function(s){

  },
  error:function(e){
  
  }
})
```
The success callback can notify you once you are successfully subscribed, so it will be called only once. The onPublish method is deprecated.
If you want to read the payload, you can listen to the event by the name of the topic. For example if you have subscribed to the topic called "sampletopic". You can read the payload in this way.

```javascript
 document.addEventListener("sampletopic",function(e){
  console.log(e.payload)
 },false)
```

##### unsubscribe

To unsubscribe to a channel. You can use this function.

```javascript
cordova.plugins.CordovaMqTTPlugin.unsubscribe({
   topic:"sampletopic",
  success:function(s){

  },
  error:function(e){
  
  }
})
```
This function will also fire the unsubscribe event which yiu can listen to using the document.addEventListener function. Also the event listener for the topic is removed automatically once the client successfully unsubscibes.


##### disconnect

To disconnect yourself from a server, use following function
```javascript
cordova.plugins.CordovaMqTTPlugin.disconnect({
  success:function(s){

  },
  error:function(e){
  
  }
})
```

### Todos

 - Plan support for new platforms (iOS, Windows Phone)
 - Add background service support in Android version

License
----

MIT


