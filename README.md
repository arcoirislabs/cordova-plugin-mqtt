# cordova-plugin-mqtt

[![npm version](https://badge.fury.io/js/cordova-plugin-mqtt.svg)](https://badge.fury.io/js/cordova-plugin-mqtt)
[![Join the chat at https://gitter.im/arcoirislabs/cordova-plugin-mqtt](https://badges.gitter.im/arcoirislabs/cordova-plugin-mqtt.svg)](https://gitter.im/arcoirislabs/cordova-plugin-mqtt?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


cordova-plugin-mqtt is plugin for building MQTT client for multiple platforms in Apache Cordova. Currently Android platform is present and next support is planned for iOS & Windows Phone.

### Cordova platform support
5.x (CLI)
4.x (Cordova Android) & above.

### Note
From v0.3.x, the eventListner implementation shall be deprecated. Kindly take a note of this.

### Installation

Install plugins via plugin repository or GitHub

```sh
$ cordova plugin add cordova-plugin-mqtt
```

```sh
$ cordova plugin add https://github.com/arcoirislabs/cordova-plugin-mqtt.git
```

### Changelog
1. Fixed the disconnect issue.

### Documentation

[UPDATE]

We have written a tutorial for this plugin over [here](https://medium.com/@arcoirislabs/using-mqtt-on-apache-cordova-564d4fab526b). Kindly check out before you start developing. Cheers
##### Methods
1. [connect](#connect)
2. [publish](#publish)
3. [subscribe](#subscribe)
4. [unsubscribe](#unsubscribe)
5. [disconnect](#disconnect)
6. [router](#router)
7. [listen](#listen)

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
Deprecated

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
        qos:0, //default is 0
        retain:true, //default is true
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
    },
    routerConfig:{
        router:routerObject //instantiated router object
        publishMethod:"emit", //refer your custom router documentation to get the emitter/publishing function name. The parameter should be a string and not a function.
        useDefaultRouter:false //Set false to use your own topic router implementation. Set true to use the stock topic router implemented in the plugin.
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
To subscribe to a channel. You can use this function. You can also use wildcard based subscription using following ways

```javascript
//Simple subscribe
cordova.plugins.CordovaMqTTPlugin.subscribe({
   topic:"sampletopic",
   qos:0,
  success:function(s){

  },
  error:function(e){

  }
});

//Single level wildcard subscribe
cordova.plugins.CordovaMqTTPlugin.subscribe({
   topic:"/+/sampletopic",
   qos:0,
  success:function(s){

  },
  error:function(e){

  }
});

//multi level wildcard subscribe
cordova.plugins.CordovaMqTTPlugin.subscribe({
   topic:"/sampletopic/#",
   qos:0,
  success:function(s){

  },
  error:function(e){

  }
});

//Using both kinds of wildcards

cordova.plugins.CordovaMqTTPlugin.subscribe({
   topic:"/+/sampletopic/#",
   qos:0,
  success:function(s){

  },
  error:function(e){

  }
})
```
The success callback can notify you once you are successfully subscribed, so it will be called only once. The onPublish method is deprecated.
If you want to read the payload, you can listen to the event by the name of the topic. For example if you have subscribed to the topic called "sampletopic". You can read the payload in this way.

#####Update:-
We are introducing topic pattern support to listen to certain topics in a way the developer wishes to. This topic pattern helps developer to make a common listener to different topics sharing same levels using single and multi-level wildcards.

```javascript
 //Deprecated
 document.addEventListener("sampletopic",function(e){
  console.log(e.payload)
 },false);

 //New way to listen to topics
 cordova.plugins.CordovaMqTTPlugin.listen("/topic/+singlewc/#multiwc",function(payload,params){
  //Callback:- (If the user has published to /topic/room/hall)
  //payload : contains payload data
  //params : {singlewc:room,multiwc:hall}
})
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

##### router

This function provides you the access to all the topic router functions you have used. If you have used a the stock topic router you can access the payload for a topic by this method.
```javascript
//Declare this function in any scope to access the router function "on" to receive the payload for certain topic
cordova.plugins.CordovaMqTTPlugin.router.on("/topic/+singlewc/#multiwc",function(payload,params){
  //Callback:- (If the user has published to /topic/room/hall)
  //payload : contains payload data
  //params : {singlewc:room,multiwc:hall}
});

//To get a callback on topic subscribe/unsubscribe event, you can listen by this method
cordova.plugins.CordovaMqTTPlugin.router.onadd(function(topic){

});
cordova.plugins.CordovaMqTTPlugin.router.onremove(function(topic){

});
```

##### listen

This function lets you listen to certain topic pattern specifically constructed by topic patters as shown below.
```javascript
//Declare this function in any scope to access the router function "on" to receive the payload for certain topic
cordova.plugins.CordovaMqTTPlugin.listen("/topic/+singlewc/#multiwc",function(payload,params){
  //Callback:- (If the user has published to /topic/room/hall)
  //payload : contains payload data
  //params : {singlewc:room,multiwc:hall}
});


```

### Todos

 - Add a stable iOS support in v0.3.0
 - Plan support for new platform (Windows Phone)
 - Add background service support in Android version to save the payload related from certain topics in a DB when the app is in background.


License
----

MIT
