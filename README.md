# mqtt-cordova (Working)

mqtt-cordova is plugin for building MQTT client for multiple platforms in Apache Cordova. Currently Android platform will be added and next Windows Phone will be looked forward. 

### Cordova platform support
5.x (CLI)
4.x (Cordova Android)

### Version
0.2.0

### Installation

Install plugins via plugin repository or GitHub

```sh
$ cordova plugin add cordova-plugin-mqtt
```

```sh
$ cordova plugin add https://github.com/arcoirislabs/cordova-plugin-mqtt.git
```

### Changelog
1. No support for authentication yet.
2. Added a better control over multiple subscriptions along with other methods' execution.
3. Fixed multiple subscriptions issue. Now you can subscribe to multiple topics (the topic parameter doesn't accepts an array. Still needs a string. So subscribe to the events separately).
4. Now every function is threaded. So no more blocking of UI due to previous implementation

### Documentation

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
  port:1883,
  url:"tcp://test.mosquitto.org",
  success:function(s){

  },
  error:function(e){

  }
})
```

##### publish
To publish to a channel. You can use this function.

```javascript
cordova.plugins.CordovaMqTTPlugin.publish({
   topic:"sampletopic",
   payload:"hello from the plugin",
  success:function(s){

  },
  error:function(e){
  
  }
})
```
In order to debug the publish call you can either go for callbacks in the function or events. Once published the function will call the "published" event & the success callback else the function will call both "not published" event & error callback. The success callback can notify you once you are successfully subscribed, so it will be called only once. To access the topic and payload objects, you need to use the onPublish method.

##### subscribe
To subscribe to a channel. You can use this function.

```javascript
cordova.plugins.CordovaMqTTPlugin.subscribe({
   topic:"sampletopic",
  success:function(s){

  },
  error:function(e){
  
  },
  onPublish:function(topic,payload){

  }
})
```

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

License
----

MIT


