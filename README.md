# mqtt-cordova (under development)

mqtt-cordova is plugin for building MQTT client for multiple platforms in Apache Cordova. Currently Android platform will be added and next Windows Phone will be looked forward. 

### Cordova platform support
5.x (CLI)
4.x (Cordova Android)

### Version
0.0.1

### Installation

Install plugins via plugin repository or GitHub

```sh
$ cordova plugin add com.arcoirislabs.plugin.mqtt
```

```javascript
$ $ cordova plugin add https://github.com/arcoirislabs/mqtt-cordova.git
```

### Documentation

Default listeners you can program anywhere for following events
 - connected
 - disconnected
 - failure (connection)
 - subscribed
 - not subscribed

For example you can configure the event in this way

 ```javascript
 document.addEventListener(<event name>,function(e){
  console.log(e.type)
 },false)
```

There are following methods currently planned
 - publish

To connect to a broker

```javascript
cordova.plugins.CordovaMqTTPlugin.connect({
  port:1883,
  url:"mqtt://test.mosquitto.org"})
```

To subscribe to a channel. You can use this function.

```javascript
cordova.plugins.CordovaMqTTPlugin.subscribe({
   topic:"sampletopic"
})
```

If you want to read the payload, you can listen to the event by the name of the topic. For example if you have subscribed to the topic called "sampletopic". You can read the payload in this way.

```javascript
 document.addEventListener("sampletopic",function(e){
  console.log(e.payload)
 },false)
```


### Todos

 - Add new method for publish
 - Plan support for new platforms (iOS, Windows Phone)

License
----

MIT



