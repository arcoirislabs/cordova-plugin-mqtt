!function(e){if("object"==typeof exports&&"undefined"!=typeof module)module.exports=e();else if("function"==typeof define&&define.amd)define([],e);else{var f;"undefined"!=typeof window?f=window:"undefined"!=typeof global?f=global:"undefined"!=typeof self&&(f=self),f.MQTTEmitter=e()}}(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";
/*
 mqtt-emitter by RangerMauve. Version 0.0.2
*/

var mqtt_regex = require("mqtt-regex");
var MQTTStore = require("mqtt-store");

module.exports = MQTTEmitter;

/**
 * Creates a new MQTTEmitter instance
 */
function MQTTEmitter() {
	this._listeners = new MQTTStore();
	this._regexes = [];
}

// Inherit the PatternEmitter methods and stuff
MQTTEmitter.prototype = Object.create({
	addListener: addListener,
	on: addListener,
	once: once,
	removeListener: removeListener,
	removeAllListeners: removeAllListeners,
	listeners: listeners,
	emit: emit,
	onadd: onadd,
	onremove: onremove,
});

/**
 * Listen for MQTT messages that match a given pattern.
 * @see {@link https://github.com/RangerMauve/mqtt-regex|mqtt-regex}
 * @param  {String}      topic   MQTT topic pattern with optional placeholders
 * @param  {Function}    handler Callback which takes the MQTT payload and topic params
 * @return {MQTTEmitter}         Returns self for use in chaining
 */
function addListener(topic, handler) {
	var matcher = mqtt_regex(topic);
	var topic_string = matcher.topic;

	var listeners = this._listeners.get(topic_string);
	if (!listeners) {
		listeners = this._listeners.set(topic_string, []);
	}

	var is_new = (listeners.length === 0);

	listeners.push({
		fn: handler,
		params: matcher.exec,
		pattern: topic
	});

	if (is_new) this.onadd(topic_string);

	return this;
}

/**
 * Adds a one time listener for the event
 * @param  {String}      topic   Topic pattern to listen on
 * @param  {Function}    handler Function to call the next time this topic appears
 * @return {MQTTEmitter}         Returns self for use in chaining
 */
function once(topic, handler) {
	var self = this;
	once_handler.handler = handler;
	this.on(topic, once_handler);

	return this;

	function once_handler(data, params) {
		handler.call(self, data, params);
		self.removeListener(topic, once_handler);
	}
}

/**
 * Removes an existing listener
 * @param  {String}      topic   Topic pattern to unsubscribe from
 * @param  {Function}    handler Handler that was used originally
 * @return {MQTTEmitter}         Returns self for use in chaining
 */
function removeListener(topic, handler) {
	var matcher = mqtt_regex(topic);
	var topic_string = matcher.topic;
	var listeners = this._listeners.get(topic_string);

	if (!listeners || !listeners.length) return this;

	var has_filtered = false;
	var filtered_listeners = listeners.filter(function (listener) {
		if (has_filtered) return true;

		var matches = (listener.fn === handler);
		if (!matches) return true;

		has_filtered = true;
		return false;
	});

	if (!filtered_listeners.length) this.onremove(topic_string);

	if (has_filtered)
		this._listeners.set(topic_string, filtered_listeners);

	return this;
}

/**
 * Removes all listeners for this type of topic.
 * @param  {String}      topic Topic pattern to unsubscribe from
 * @return {MQTTEmitter}       Returns self for use in chaining
 */
function removeAllListeners(topic) {
	if (topic) {
		var matcher = mqtt_regex(topic);
		var topic_string = matcher.topic;
		var listeners = this._listeners.get(topic_string);

		if (!listeners.length) return this;

		this._listeners.set(topic_string, []);
		this.onremove(topic_string);

	} else {
		this._listeners = new MQTTStore();
	}

	return this;
}

/**
 * Returns an array of listeners that match this topic
 * @param  {String} topic The topic pattern to get listeners for
 * @return {Array}        Array of handler functions
 */
function listeners(topic) {
	var matcher = mqtt_regex(topic);
	var topic_string = matcher.topic;

	return (this._listeners.get(topic_string) || []).map(function (listener) {
		return listener.fn;
	});
}

/**
 * Process a new MQTT event and dispatch it to relevant listeners
 * @param  {String}  topic   The raw MQTT topic string recieved from a connection
 * @param  {Any}     payload This is the payload from the MQTT topic event
 * @return {Boolean}         Returns true if there were any listeners called for this topic
 */
function emit(topic, payload) {
	var matcher = mqtt_regex(topic);
	var topic_string = matcher.topic;
	var matching = this._listeners.match(topic_string);
	if (!matching.length) return false;

	matching.forEach(function (listeners) {
		listeners.forEach(function (listener) {
			var params = listener.params(topic);
			listener.fn(payload, params, topic, listener.pattern);
		});
	});

	return true;
}

/**
 * Hook for reacting to new MQTT topics
 * @param {String} topic MQTT topic that is being subscribed to
 */
function onadd(topic) {
	// Detect when new topics are added, maybe
	// Can be useful for auto-subscribing on actual MQTT connection
}

/**
 * Hook for reacting to removed MQTT topics
 * @param  {String} topic MQTT topiuc that is being unsubscribed from
 */
function onremove(topic) {
	// Detect when topics are no longer listened to here
	// Can be useful for auto-unsubscribing on an actual MQTT connection
}

},{"mqtt-regex":2,"mqtt-store":4}],2:[function(require,module,exports){
/*
	The MIT License (MIT)

	Copyright (c) 2014 RangerMauve

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/

var escapeRegex = require('escape-string-regexp');

module.exports = parse;

/**
 * Parses topic string with parameters
 * @param topic Topic string with optional params
 * @returns {Object} Compiles a regex for matching topics, getParams for getting params, and exec for doing both
 */
function parse(topic) {
	var tokens = tokenize(topic).map(process_token);
	var result = {
		regex: make_regex(tokens),
		getParams: make_pram_getter(tokens),
		topic: make_clean_topic(tokens)
	};
	result.exec = exec.bind(result);
	return result;
};

/**
 * Matches regex against topic, returns params if successful
 * @param topic Topic to match
 */
function exec(topic) {
	var regex = this.regex;
	var getParams = this.getParams;
	var match = regex.exec(topic);
	if (match) return getParams(match);
}

// Split the topic into consumable tokens
function tokenize(topic) {
	return topic.split("/");
}

// Processes token and determines if it's a `single`, `multi` or `raw` token
// Each token contains the type, an optional parameter name, and a piece of the regex
// The piece can have a different syntax for when it is last
function process_token(token, index, tokens) {
	var last = (index === (tokens.length - 1));
	if (token[0] === "+") return process_single(token, last);
	else if (token[0] === "#") return process_multi(token, last);
	else return process_raw(token, last);
}

// Processes a token for single paths (prefixed with a +)
function process_single(token) {
	var name = token.slice(1);
	return {
		type: "single",
		name: name,
		piece: "([^/#+]+/)",
		last: "([^/#+]+/?)"
	};
}

// Processes a token for multiple paths (prefixed with a #)
function process_multi(token, last) {
	if (!last) throw new Error("# wildcard must be at the end of the pattern");
	var name = token.slice(1);
	return {
		type: "multi",
		name: name,
		piece: "((?:[^/#+]+/)*)",
		last: "((?:[^/#+]+/?)*)"
	}
}

// Processes a raw string for the path, no special logic is expected
function process_raw(token) {
	var token = escapeRegex(token);
	return {
		type: "raw",
		piece: token + "/",
		last: token + "/?"
	};
}

// Turn a topic pattern into a regular MQTT topic
function make_clean_topic(tokens) {
	return tokens.map(function (token) {
		if (token.type === "raw") return token.piece.slice(0, -1);
		else if (token.type === "single") return "+";
		else if (token.type === "multi") return "#";
		else return ""; // Wat
	}).join("/");
}

// Generates the RegExp object from the tokens
function make_regex(tokens) {
	var str = tokens.reduce(function (res, token, index) {
			var is_last = (index == (tokens.length - 1));
			var before_multi = (index === (tokens.length - 2)) && (last(tokens).type == "multi");
			return res + ((is_last || before_multi) ? token.last : token.piece);
		},
		"");
	return new RegExp("^" + str + "$");
}

// Generates the function for getting the params object from the regex results
function make_pram_getter(tokens) {
	return function (results) {
		// Get only the capturing tokens
		var capture_tokens = remove_raw(tokens);
		var res = {};

		// If the regex didn't actually match, just return an empty object
		if (!results) return res;

		// Remove the first item and iterate through the capture groups
		results.slice(1).forEach(function (capture, index) {
			// Retreive the token description for the capture group
			var token = capture_tokens[index];
			var param = capture;
			// If the token doesn't have a name, continue to next group
			if (!token.name) return;

			// If the token is `multi`, split the capture along `/`, remove empty items
			if (token.type === "multi") {
				param = capture.split("/");
				if (!last(param))
					param = remove_last(param);
				// Otherwise, remove any trailing `/`
			} else if (last(capture) === "/")
				param = remove_last(capture);
			// Set the param on the result object
			res[token.name] = param;
		});
		return res;
	}
}

// Removes any tokens of type `raw`
function remove_raw(tokens) {
	return tokens.filter(function (token) {
		return (token.type !== "raw");
	})
}

// Gets the last item or character
function last(items) {
	return items[items.length - 1];
}

// Returns everything but the last item or character
function remove_last(items) {
	return items.slice(0, items.length - 1);
}

},{"escape-string-regexp":3}],3:[function(require,module,exports){
'use strict';

var matchOperatorsRe = /[|\\{}()[\]^$+*?.]/g;

module.exports = function (str) {
	if (typeof str !== 'string') {
		throw new TypeError('Expected a string');
	}

	return str.replace(matchOperatorsRe, '\\$&');
};

},{}],4:[function(require,module,exports){
module.exports = MQTTStore;

function MQTTStore() {
	if (!(this instanceof MQTTStore))
		return new MQTTStore();
	this.tree = {
		children: {}
	};
}

MQTTStore.prototype = {
	tree: null,
	get: get,
	query: query,
	match: match,
	set: set
};

function get(key) {
	return get_one(split(key), this.tree);
}

function query(key) {
	return get_all(split(key), this.tree);
}

function match(key) {
	return get_matching(split(key), this.tree);
}

function set(key, value) {
	set_one(split(key), this.tree, value);
	return value;
}

function remove_one(path, tree) {
	if (!path.length) return;
	var next = path[0];
	if (!(next in tree.children)) return;
	return remove_one(path.slice(1), tree);
}

function set_one(path, tree, value) {
	if (!path.length) return tree.value = value;
	var next = path[0];
	var children = tree.children;
	var next_tree = children[next];
	if (!next_tree)
		next_tree = children[next] = {
			children: {}
		};
	set_one(path.slice(1), tree.children[next], value);
}

function get_one(path, tree) {
	if (!path.length) return tree.value;
	var next = path[0];
	var next_tree = tree.children[next];
	if (!next_tree) return undefined;
	return get_one(path.slice(1), next_tree);
}

function get_all(path, tree) {
	if (!path.length) {
		var value = tree.value;
		if (value !== undefined) return [value];
		return [];
	}

	var next = path[0];

	if (next === "+") return values(tree.children)
		.map(get_all.bind(null, path.slice(1)))
		.reduce(flatten, []);

	if (next === "#") return all_values(tree);

	var next_tree = tree.children[next];
	if (!next_tree) return [];
	return get_all(path.slice(1), next_tree);
}

function get_matching(path, tree) {
	if (!path.length) {
		var results = [];
		var value = tree.value;
		if (value !== undefined) results.push(value);
		var multi = tree.children["#"];
		if (multi && multi.value)
			results.push(multi.value);
		return results;
	}

	var multi = tree.children["#"];
	var single = tree.children["+"];

	var next = path[0];
	var next_tree = tree.children[next];

	var rest = path.slice(1);

	var results = [];

	if (multi && multi.value)
		results.push(multi.value);

	return results.concat([single, next_tree]
		.reduce(function (all, subtree) {
			if (subtree) return all.concat(get_matching(rest, subtree));
			return all;
		}, []));
}

function all_values(tree) {
	return values(tree.children).map(function (subtree) {
		var all = all_values(subtree);
		var current = subtree.value;
		if (current !== undefined)
			all.push(current);
		return all;
	}).reduce(flatten, []);
}

function keys(object) {
	return Object.keys(object);
}

function values(object) {
	return keys(object).map(function (key) {
		return object[key];
	});
}

function flatten(prev, current) {
	return prev.concat(current);
}

function split(path) {
	return path.split("/");
}

},{}]},{},[1])(1)
});