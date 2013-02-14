/**
 * JsonRpc Adapter (using ajax)
 * @class WireIt.WiringEditor.adapters.JsonRpc
 * @static 
 */
WireIt.WiringEditor.adapters.JsonRpc = {
	
	config: {
		url: 'http://localhost/jetty/'
	},
	
	init: function() {
		YAHOO.util.Connect.setDefaultPostHeader('application/json');		
	},
	
	saveWiring: function(val, callbacks) {
		
		// Make a copy of the object
		var wiring = {};
		YAHOO.lang.augmentObject(wiring, val);
		
		// Encode the working field as a JSON string
		wiring.working = YAHOO.lang.JSON.stringify(wiring.working);
		
		this._sendJsonRpcRequest("saveWiring", wiring, callbacks);
	},				
	
	listWirings: function(val, callbacks) {
		this._sendJsonRpcRequest("listWirings", val, callbacks);
	},
	
	// private method to send a json-rpc request using ajax
	_sendJsonRpcRequest: function(method, value, callbacks) {
		var postData = YAHOO.lang.JSON.stringify({"id":(this._requestId++),"method":method,"params":value,"version":"json-rpc-2.0"});				
		//alert(postData);

		YAHOO.util.Connect.asyncRequest('POST', this.config.url, {
			success: function(o) {
			try {		
				var s = o.responseText;				
				if (s.indexOf("Error") >= 0) {alert(s);
				} else {
					r = YAHOO.lang.JSON.parse(s);									
					var wirings = r.result;				
					for(var wire in wirings) {
						wire.working = YAHOO.lang.JSON.parse(wirings[i].working);
					}

					//callbacks.success.call(callbacks.scope, r.result);
			 	}
			 }catch(ex) {
				alert(ex);
			}	
			},
			failure: function() {
				callbacks.failure.call(callbacks.scope, r);
			}
		},postData);
	},
	_requestId: 1
};
