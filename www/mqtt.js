/*
MqTT-Cordova 
Doc:-
Publish (with credentials)
mqtt.publish({
	url:"",
	port:"",
	topic:"",
	secure:false/true,
	qos:"",
	message:"",
	cleanSession:true/false,
	username:"",
	password:"",
	debug:true/false,
	success:function(data){},
	error:function(data){}
});
*/
var sero = {
	publish : function (data) {
		var dataUrl,cleanSes,quietM;
		if (data.secure) {
			dataUrl = "ssl://"+data.url+data.port;
		} else{
			dataUrl = "tcp://"+data.url+data.port;
		};
		if (data.cleanSession) {
			cleanSes=true;
		} else{
			cleanSes=false;
		};
		if (data.debug) {
			quietM = true;
		} else{
			quietM = false;
		};
		cordova.exec(function(response) {
        	data.success(response)
            },function(error) {data.error(error);},"MqTTPlugin","publish",[dataUrl, data.clientId, quietM,data.username,data.password,cleanSes,data.topic,data.qos,data.message]);
	},
	subscribe : function (data,callback) {
		var dataUrl,cleanSes,quietM;
		if (data.secure) {
			dataUrl = "ssl://"+data.url+data.port;
		} else{
			dataUrl = "tcp://"+data.url+data.port;
		};
		if (data.cleanSession) {
			cleanSes=true;
		} else{
			cleanSes=false;
		};
		if (data.debug) {
			quietM = true;
		} else{
			quietM = false;
		};
		cordova.exec(function(response) {
        	data.success(response)
            },function(error) {data.error(error);},"MqTTPlugin","subscribe",[dataUrl, data.clientId, quietM,data.username,data.password,cleanSes,data.topic,data.qos]);
	}
}
module.exports = sero;
