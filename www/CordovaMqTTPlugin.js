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
        if (args.url!==undefined) {
            exec(function(cd){
                cordova.fireDocumentEvent("connected");
                if(args.success!=undefined){
                    args.success("connected");
                }

            }, function(e){
                switch(e){
                    case "disconnected":
                        cordova.fireDocumentEvent("disconnected");
                        if(args.error!=undefined){
                            args.error("disconnected");
                        }
                        break;
                    case "failure":
                        cordova.fireDocumentEvent("failure");
                        if(args.error!=undefined){
                            args.error("failure");
                        }
                        break;
                }
                args.error(e)
            }, "CordovaMqTTPlugin", "connect", [url,cid,ka])
        } else{
            alert("Please provide the url and the port.")
        };
            
    }
    exports.subscribe = function(args){
        exec(function(data){
            if(data!="subscribed"){
                var d = JSON.parse(data);
                cordova.fireDocumentEvent(d.topic,d)
                if(args.success!=undefined){
                    args.success(d)
                }
            }else{
                cordova.fireDocumentEvent(data);
                if(args.success!=undefined){
                    args.success(data)
                }
            }
        }, function(e){
            if(args.error!=undefined){
                args.error(e)
            }
        }, "CordovaMqTTPlugin", "subscribe", [args.topic])
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
    channel.onCordovaReady.subscribe(function() {
        console.log("subscribed to a chennle");
    });
