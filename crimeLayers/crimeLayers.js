YAHOO.util.Connect.asyncRequest('GET', "/jetty/", {
	success: function(o) {
		var s = o.responseText;					
		//alert(s);				
		try {		
			var crimeLayersLang = YAHOO.lang.JSON.parse(s);				
			//alert(crimeLayersLang);		
			crimeInit(crimeLayersLang);
		}catch(ex) {
			alert(ex);
		}			
	},
	failure: function() {
		alert("Error calling for module list");
	}
});


/********
 * CODE
 ********/ 
crimeInit = function(crimeLayersLang) {	
	// General layer container
	LayerContainer = function(opts, layer) {
		LayerContainer.superclass.constructor.call(this, opts, layer);
		this.logicInputValues = [];
	};
	YAHOO.lang.extend(LayerContainer, WireIt.FormContainer, {

		/** 
	    * @property xtype
	    * @description String representing this class for exporting as JSON
	    * @default "WireIt.LayerContainer"
	    * @type String
	    */
	   xtype: "LayerContainer",

		setInput: function(bStatus,term) {
			this.logicInputValues[term.name] = bStatus;

			if(this.title == "channel") {
				this.setLogic( this.logicInputValues["_INPUT1"] );
			}
			else if(this.title == "broadcast") {
				this.setLogic( this.logicInputValues["_INPUT1"] );
			}
			else if(this.title == "polite") {
				this.setLogic( this.logicInputValues["_INPUT1"] );
			}
			else if(this.title == "netflood") {
				this.setLogic( this.logicInputValues["_INPUT1"] );
			}
			else if(this.title == "unicast") {
				this.setLogic( this.logicInputValues["_INPUT1"] );
			}
			else if (this.title == "multihop") {
				this.setLogic( this.logicInputValues["_INPUT1"] || this.logicInputValues["_INPUT2"]  );
			}				
			else if (this.title == "route_discovery") {
				this.setLogic( this.logicInputValues["_INPUT1"] || this.logicInputValues["_INPUT2"]  );
			}
			else if (this.title == "mesh") {
				this.setLogic( this.logicInputValues["_INPUT1"] || this.logicInputValues["_INPUT2"]  );
			}
		},

		setLogic: function(bStatus) {
			this.status = bStatus;

			// Set the image
			if(this.imageName) {
				var image = this.imageName+"_"+(bStatus ? "on" : "off")+".png";
				YAHOO.util.Dom.setStyle(this.bodyEl, "background-image", "url(images/"+image+")");
			}

			// trigger the output wires !
			for(var i = 0 ; i < this.terminals.length ; i++) {
				var term = this.terminals[i];
				if(term.name == "_OUTPUT") {
					for(var j = 0 ; j < term.wires.length ; j++) {
						var wire = term.wires[j];
						var otherTerm = wire.getOtherTerminal(term);
						if(otherTerm.container) {
							otherTerm.container.setInput(bStatus, otherTerm);
						}
						wire.color = bStatus ? "rgb(173,216,230)" : "rgb(255,255,255)";
						wire.redraw();
					}
				}
			}
		},
		switchStatus: function() {
			this.setLogic(!this.status);
		}
	});	

	YAHOO.util.Event.onDOMReady( function() { 
		try {		
			crimeLayers = new WireIt.WiringEditor(crimeLayersLang); 		
		}catch(ex) {
			alert(ex);
		}
	});
}

