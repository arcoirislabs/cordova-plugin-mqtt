cordova.define("com.arcoirislabs.plugin.mqtt.CordovaMqTTPlugin", function(require, exports, module) {
var exec = require('cordova/exec'), cordova = require('cordova'),
channel = require('cordova/channel'),
    utils = require('cordova/utils');
    
    exports.connect = function(args){
        var cid,ka;
        if(!args.isCleanSession){
            cid = args.clientId;
        }else{
            cid = "undefined";
        };
        if(args.keepAlive==undefined){
            ka = 100;
        }else{
            ka = args.keepAlive;
        }
        exec(function(cd){
            switch(cd){
                case "connected":
                    cordova.fireDocumentEvent("connected");
                    break;
                case "disconnected":
                    cordova.fireDocumentEvent("disconnected");
                    break;
                case "failure":
                    cordova.fireDocumentEvent("failure");
                    break;
            }
        }, function(e){
            console.error(e)
        }, "CordovaMqTTPlugin", "connect", [args.url,args.port,cid,ka])
    }
    exports.subscribe = function(args){
        exec(function(data){
            if(data!="subscribed"){
                var d = JSON.parse(data);
                cordova.fireDocumentEvent(d.topic,d)
            }else{
                cordova.fireDocumentEvent(data);
            }
        }, function(e){
        console.error(e)}, "CordovaMqTTPlugin", "subscribe", [args.topic])
    }
    channel.onCordovaReady.subscribe(function() {
        console.log("subscribed to a chennle");
    });

});