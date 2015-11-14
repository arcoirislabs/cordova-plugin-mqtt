cordova.define("com.arcoirislabs.plugin.mqtt.CordovaMqTTPlugin", function(require, exports, module) { var exec = require('cordova/exec');

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "CordovaMqTTPlugin", "coolMethod", [arg0]);
};
exports.connect = function(args,success,error){
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
    exec(success, error, "CordovaMqTTPlugin", "connect", [args.url,args.port,cid,ka])
}
});
