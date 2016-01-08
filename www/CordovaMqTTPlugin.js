
var exec = require('cordova/exec'), cordova = require('cordova'),
channel = require('cordova/channel'),
    utils = require('cordova/utils');
    
    exports.connect = function(args){
         var cid,ka,url;
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
                if (args.port!==undefined) {
                    url = args.url+":"+args.port;
                } else{
                    url = args.url;
                };
        exec(function(cd){
            switch(cd){
                case "connected":
                    args.success(cd)
                    cordova.fireDocumentEvent("connected");
                    break;
                case "disconnected":
                    args.error(cd)
                    cordova.fireDocumentEvent("disconnected");
                    break;
                case "failure":
                    args.error(cd)
                    cordova.fireDocumentEvent("failure");
                    break;
            }
        }, function(e){
            console.error(e)
        }, "CordovaMqTTPlugin", "connect", [url,cid,ka]);
    }
    exports.subscribe = function(args){
                exec(function(data){
                    if(data!="subscribed"){
                        data.payload = JSON.parse(data.payload.replace("ascii:",""));
                        cordova.fireDocumentEvent(data.topic,data);
                        if(args.onPublish!==undefined){
                            args.onPublish(data.topic,data.payload);
                        }
                    }else{
                        cordova.fireDocumentEvent(data);
                    }
                    args.success(data)
        }, function(e){
        console.error(e)}, "CordovaMqTTPlugin", "subscribe", [args.topic]);
         
    }
    exports.publish = function(args){
            exec(function(data){
                cordova.fireDocumentEvent(data);
                if(args.success!=undefined){
                    args.success(data)
                }
            }, function(e){
                if(args.error!=undefined){
                    args.error(e)
                }
            }, "CordovaMqTTPlugin", "publish", [args.topic,args.payload])
        }
        exports.unsubscribe = function(args){
            exec(function(data){
                cordova.fireDocumentEvent(data);
                document.removeEventListener(args.topic)
                if(args.success!=undefined){
                    args.success(data)
                }
            }, function(e){
                if(args.error!=undefined){
                    args.error(e)
                }
            }, "CordovaMqTTPlugin", "unsubscribe", [args.topic])
        }
        exports.disconnect = function(args){
            exec(function(data){
                cordova.fireDocumentEvent(data);
                if(args.success!=undefined){
                    args.success(data)
                }
            }, function(e){
                if(args.error!=undefined){
                    args.error(e)
                }
            }, "CordovaMqTTPlugin", "disconnect", [args.topic])
        }