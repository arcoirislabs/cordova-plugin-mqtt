/*
Adding new router config.
routerConfig:{
    router:type Object
    publishMethod:type String,
    useDefaultRouter:type Boolean
}*/
var exec = require('cordova/exec'), cordova = require('cordova'),
channel = require('cordova/channel'),
    utils = require('cordova/utils');
    var url,uname,pass,iscls,router,mqtt = null;
    var isDefault,useJS = true;
    exports.connect = function(args){
        //SSL support is coming soon
        //var urgx = //(^(tcp|ssl|local)&?:\/\/[^\@_?!]+[^\s]+[^\.]+\:\d{2,})/g;
        var urgx = /(^(tcp|local)&?:\/\/[^\@_?!]+[^\s]+[^\.]+\:\d{2,})/g;
        if (args.port!==undefined) {
            url = args.url+":"+args.port;
        } else{
            url = args.url;
        }
        if (args.willTopicConfig!==undefined) {
            if(args.willTopicConfig.retain===undefined){
                args.willTopicConfig.retain = true;
            }
        } else {
            args.willTopicConfig = {};
        }
        if (args.isCleanSession===undefined) {
            iscls = true;
        } else {
            iscls = args.isCleanSession;
        }
        if (args.routerConfig!==undefined) {
            if(Object.keys(args.routerConfig).length>0){
                if (!args.routerConfig.useDefaultRouter&&args.routerConfig.useDefaultRouter!==undefined) {
                    if(args.routerConfig.router!==undefined){
                        router = args.routerConfig.router;
                        isDefault = useDefaultRouter;
                    } else{
                        console.error("Please set your topic router object");
                    }
                } else {
                    //using default topic router
                    router = new ME();
                }

            }else{
                //setting mqtt-emitter instance as default router
                router = new ME();
            }
        } else {
            router = new ME();
        }
        if(url.length>0&&urgx.exec(url)!==null){
            if (cordova.platformId === "android") {
                exec(function(cd){
                    //console.log("data",cd);
                    switch(cd.call){
                        case "connected":
                            delete cd.call;
                            if(args.success!==undefined){
                                args.success(cd);
                            }
                            //cordova.fireDocumentEvent("connected",cd);
                            console.warn("Cordova Plugin Warning: The eventListner implmentation to read the published payloads shall be discontinued from the 0.3.0 version. Kindly take a note of this change.");
                            break;
                        case "disconnected":
                            delete cd.call;
                            if(args.error!==undefined){
                                args.error(cd);
                            }
                            //cordova.fireDocumentEvent("disconnected",cd);
                            break;
                        case "failure":
                            delete cd.call;
                            if(args.onConnectionLost!==undefined){
                                args.onConnectionLost(cd);
                            }
                            //cordova.fireDocumentEvent("failure",cd);
                            break;
                        case "onPublish":
                            delete cd.call;
                            if(args.onPublish!==undefined){
                                args.onPublish(cd.topic,cd.payload);
                            }
                            if(router!==null){
                                if (args.routerConfig!==undefined) {
                                    if (args.routerConfig.publishMethod!==undefined) {
                                        router[args.routerConfig.publishMethod](cd.topic,cd.payload);
                                    }

                                } else {
                                    router.emit(cd.topic,cd.payload);
                                }

                            }
                            //cordova.fireDocumentEvent(cd.topic,cd);
                            break;
                        default:
                            console.log(cd);
                            break;
                    }
                }, function(e){
                    console.error(e);
                }, "CordovaMqTTPlugin", "connect", [url,args.clientId,args.keepAlive||60000,iscls,args.connectionTimeout||30,args.username, args.password,args.willTopicConfig.topic,args.willTopicConfig.payload,args.willTopicConfig.qos||0,args.willTopicConfig.retain||true,args.version||"3.1.1"]);
            } else {

                if (args.url.split("tcp://").length > 1) {
                    client = new Paho.MQTT.Client(args.url.split("tcp://")[1], Number(args.wsPort), args.urlPath||"/ws", args.clientId);
                }
                // if (args.url.split("local://").length > 1) {
                //     client = new Paho.MQTT.Client(args.url.split("local://")[1], Number(args.wsPort), args.urlPath||"/ws", args.clientId);
                // }
                client.onConnectionLost = function (cd) {
                    client = null;
                    if(args.error!==undefined){
                        args.error(cd);
                    }
                };
                client.onMessageArrived = function (payload) {
                    if(args.onPublish!==undefined){
                        args.onPublish(payload.destinationName,payload.payloadString);
                    }
                    if(router!==null){
                        if (args.routerConfig!==undefined) {
                            if (args.routerConfig.publishMethod!==undefined) {
                                router[args.routerConfig.publishMethod](payload.destinationName,payload.payloadString);
                            }

                        } else {
                            router.emit(payload.destinationName,payload.payloadString);
                        }

                    }
                };
                var connOpts = {};
                connOpts.userName = args.username||"";
                connOpts.password = args.password||"";
                connOpts.onSuccess = function (cd) {
                    if(args.success!==undefined){
                        args.success(cd);
                    }
                };
                connOpts.onFailure = function (cd) {
                    client = null;
                    if(args.error!==undefined){
                        args.error(cd);
                    }
                };
                connOpts.timeout = args.connectionTimeout||30;
                connOpts.keepAliveInterval = args.keepAlive||60000;
                connOpts.cleanSession = iscls;
                //connOpts.mqttVersion = args.version||"3.1.1";
                //console.log("will",args.willTopicConfig);
                if (args.willTopicConfig.topic !== undefined) {
                    var willMsg = new Paho.MQTT.Message(args.payloadString);
                    willMsg.destinationName = args.willTopicConfig.topic;
                    willMsg.qos = args.willTopicConfig.qos||0;
                    willMsg.retained = args.willTopicConfig.retain||true;
                    connOpts.willMessage = willMsg;
                }
                client.connect(connOpts);
            }

        } else{
            console.error("Please provide the URL to connect. If entered then please check the URL format. We support only tcp:// & local:// protocols");
        }

    };
    exports.publish = function(args){
        (args.retain===undefined) ? (args.retain=false) : (args.retain=true);
        if(args.topic.length>0){
            if (cordova.platformId === "android") {
                exec(function(data){
                    //console.log("data",data);
                    //cordova.fireDocumentEvent(data);
                    switch(data.call){
                        case "success":
                            delete data.call;
                            if(args.success !== undefined){
                                args.success(data);
                            }

                            break;
                        case "failure":
                            delete data.call;
                            if(args.success !== undefined){
                                args.error(data);
                            }
                            break;
                    }
                }, function(e){
                    if(args.error !== undefined){
                        args.error(e);
                    }
                }, "CordovaMqTTPlugin", "publish", [args.topic,args.payload,args.qos||0,args.retain]);
            } else {
                var message = new Paho.MQTT.Message(args.payload);
                message.destinationName = args.topic;
                message.qos = args.qos;
                message.retained = args.retain;
                if (client !== null) {
                    client.send(message);
                }
            }

        } else{
            console.error("Please provide a topic string in topic argument");
        }
    };
    exports.subscribe = function(args){
        (args.retain===undefined) ? args.retain=false : args.retain=true;
        if(args.topic.length>0){
            if (cordova.platformId === "android") {
                exec(function(data){
                    //console.log("data",data);
                    switch(data.call){
                        case "success":
                            delete data.call;
                            if (args.success!==undefined) {
                                args.success(data);
                            }
                            break;
                        case "failure":
                            delete data.call;
                            if (args.error!==undefined) {
                                args.error(data);
                            }

                            break;
                    }
                }, function(e){
                    console.error(e);
                    args.error(e);
                }, "CordovaMqTTPlugin", "subscribe", [args.topic,args.qos||0]);
            } else {
                if (client !== null) {
                    client.subscribe(args.topic,{
                        qos:args.qos||0,
                        onSuccess:function(cd,data){
                            if (args.success!==undefined) {
                                args.success(data);
                            }
                        },
                        onFailure:function (cd,data) {
                            if (args.error!==undefined) {
                                args.error(data);
                            }
                        }
                    });
                }
            }

        } else{
            console.error("Please provide a topic string in topic argument");
        }

    };
    exports.unsubscribe = function(args){
        if(args.topic.length > 0){
            if (cordova.platformId === "android") {
                exec(function(data){
                    console.log("data",data);
                    switch(data.call){
                        case "success":
                            delete data.call;
                            if (args.success!==undefined) {
                                args.success(data);
                            }
                            if (router!==null) {
                                router.removeListener(args.topic);
                            }
                            break;
                        case "failure":
                            delete data.call;
                            if (args.error!==undefined) {
                                args.error(data);
                            }
                            break;
                    }
                }, function(e){
                    console.error(e);
                    args.error(e);
                }, "CordovaMqTTPlugin", "unsubscribe", [args.topic]);
            } else {
                if (client !== null) {
                    client.unsubscribe(args.topic,{
                        timeout:args.timeout||5000,
                        onSuccess:function(cd,data){
                            if (args.success!==undefined) {
                                args.success(data);
                            }
                        },
                        onFailure:function (cd,data) {
                            if (args.error!==undefined) {
                                args.error(data);
                            }
                        }
                    });
                }
            }

        } else{
            console.error("Please provide a topic string in topic argument");
        }
    };
    exports.disconnect = function(args){
        if (cordova.platformId === "android") {
            exec(function(data){
                console.log("data",data);
                switch(data.call){
                    case "success":
                        delete data.call;
                        if (args.success!==undefined) {
                            args.success(data);
                        }
                        break;
                    case "failure":
                        delete data.call;
                        if (args.error!==undefined) {
                            args.error(data);
                        }
                        break;
                }
            }, function(e){
                console.error(e);
                args.error(e);
            }, "CordovaMqTTPlugin", "disconnect", []);
        } else {
            if (client !== null) {
                client.disconnect();
            }
        }
    };
    exports.router = function() {
        if (router!==null) {
            return router;
        } else {
            console.error("Router object seems to be destroyed");
        }
    };
    exports.listen = function (topic,cb) {
        if (router!==null) {
            router.on(topic,cb);
        } else {
            console.error("Router object seems to be destroyed");
        }
    };
