var exec = require('cordova/exec'), cordova = require('cordova'),
channel = require('cordova/channel'),
    utils = require('cordova/utils');
    
    exports.connect = function(args){
        var url,uname,pass;
        if (args.port!==undefined) {
            url = args.url+":"+args.port;
        } else{
            url = args.url;
        };
        if(args.willTopicConfig.retain===undefined){
            args.willTopicConfig.retain = true;
        }
        exec(function(cd){
            switch(cd.call){
                case "connected":
                    delete cd['call'];
                    if(args.success!==undefined){
                        args.success(cd)
                    };
                    cordova.fireDocumentEvent("connected",cd);
                    break;
                case "disconnected":
                    delete cd['call'];
                    if(args.error!==undefined){
                        args.error(cd);
                    };
                    cordova.fireDocumentEvent("disconnected",cd);
                    break;
                case "failure":
                    delete cd['call'];
                    if(args.onConnectionLost!==undefined){
                        args.onConnectionLost(cd)
                    };
                    cordova.fireDocumentEvent("failure",cd);
                    break;
                case "onPublish":
                    delete cd['call'];
                    cordova.fireDocumentEvent(cd.topic,cd);
                    break;
                default:
                    console.log(cd)
                    break;
            }
        }, function(e){
            console.error(e)
        }, "CordovaMqTTPlugin", "connect", [url,args.clientId,args.keepAlive||60,args.isCleanSession,args.connectionTimeout||30,args.username, args.password,args.willTopicConfig.topic,args.willTopicConfig.payload,args.willTopicConfig.qos||0,args.willTopicConfig.retain,args.version||"3.1.1"]);
    }
    exports.publish = function(args){
        if (args.retain===undefined) {
            args.retain=false;
        } 
        exec(function(data){
            cordova.fireDocumentEvent(data);
            switch(data.call){
                case "success":
                    delete data['call'];
                    if(args.success!=undefined){
                        args.success(data)
                    }

                    break;
                case "failure":
                    delete data['call'];
                    if(args.success!=undefined){
                         args.error(data);
                    }
                    break;
            }
        }, function(e){
            if(args.error!=undefined){
                args.error(e)
            }
        }, "CordovaMqTTPlugin", "publish", [args.topic,args.payload,args.qos||0,args.retain])
    }
    exports.subscribe = function(args){
        if (args.retain===undefined) {
            args.retain=false;
        } 
        exec(function(data){
            switch(data.call){
                case "success":
                    delete data['call'];
                    args.success(data);
                    break;
                case "failure":
                    delete data['call'];
                    args.error(data);
                    break;
            }
        }, function(e){
            console.error(e)
            args.error(e);
        }, "CordovaMqTTPlugin", "subscribe", [args.topic,args.qos||0]);

    }
    exports.unsubscribe = function(args){
        exec(function(data){
            switch(data.call){
                case "success":
                    delete data['call'];
                    args.success(data);
                    break;
                case "failure":
                    delete data['call'];
                    args.error(data);
                    break;
            }
        }, function(e){
            console.error(e)
            args.error(e);
        }, "CordovaMqTTPlugin", "unsubscribe", [args.topic]);
    }
    exports.disconnect = function(args){
        exec(function(data){
            switch(data.call){
                case "success":
                    delete data['call'];
                    args.success(data);
                    break;
                case "failure":
                    delete data['call'];
                    args.error(data);
                    break;
            }
        }, function(e){
            console.error(e)
            args.error(e);
        }, "CordovaMqTTPlugin", "disconnect", []);
    }