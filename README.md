mqtt-cordova
============

# MqTT Plugin for Cordova

This is a plugin for MqTT protocol of messaging. Currently Android is supported. Forthcoming support for Windows Phone.

## Features

This plugin can publish and subscribe for any MqTT server. Currently it works only for compulsory authentication.
to add the plugin 

## Usage

To publish a message you can use this function

```
mqtt.publish({
	url:"m2m.eclipse.org",
    topic:"topic",
    secure:false,
    qos:"2",
    clientId:"SampleJavaV3_",
    portNo:"1883",
    message:"Howzaaa",
    cleanSession:true,
    username:null,
    password:null,
    debug:false,
    success:function(data){
        alert(data);
    },
    error:function(data){
         alert(data);
    }
});
```

To subscribe you can use this function

```
mqtt.subscribe({
	url:"",
	port:"",
	topic:"",
	secure:false/true,
	qos:"",
	cleanSession:true/false,
	username:"",
	password:"",
	debug:true/false,
	success:function(data){},
	error:function(data){}
});
```

Created and maintained by Arcoiris Labs
Have fun!
