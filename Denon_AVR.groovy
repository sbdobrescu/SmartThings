/**
 *  Marantz Network Receiver
 *    Works on Marantz M-CR610
 *    Based on Denon receiver by Kristopher Kubicki
 *    SmartThings driver to connect your Denon Network Receiver to SmartThings
 *
 */

preferences {
    input("destIp", "text", title: "IP", description: "The device IP")
    input("destPort", "number", title: "Port", description: "The port you wish to connect", defaultValue: 80)
}

metadata {
    definition (name: "Denon AVR", namespace: "SB", 
        author: "Kristopher Kubicki") {
        capability "Actuator"
        capability "Switch" 
        capability "Polling"
        capability "Switch Level"
        attribute "mute", "string"
        attribute "input", "string"
        
        command "mute"
        command "unmute"
        command "toggleMute"
    
        }

    simulator {
        // TODO-: define status and reply messages here
    }

    //tiles {
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4) {
           tileAttribute("device.switch", key: "PRIMARY_CONTROL") { 	            
                attributeState "on", label: '${name}', action:"switch.off", backgroundColor: "#79b821", icon:"st.Electronics.electronics16"
            	attributeState "off", label: '${name}', action:"switch.on", backgroundColor: "#ffffff", icon:"st.Electronics.electronics16"
        	}             
            tileAttribute ("level", key: "SLIDER_CONTROL") {
           		attributeState "level", label:'${name}', action:"setLevel"
            }
        }        
        standardTile("poll", "device.poll", width: 2, height: 2, decoration: "flat") {
            state "poll", label: "", action: "polling.poll", icon: "st.secondary.refresh", backgroundColor: "#FFFFFF"
        }
        standardTile("input", "device.input", width: 6, height: 2, decoration: "flat") {
            state "input", label:'Current Input: ${currentValue}', backgroundColor: "#FFFFFF"
        }
        standardTile("mute", "device.mute", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "muted", label: '${name}', action:"unmute", backgroundColor: "#ffffff", icon:"st.custom.sonos.muted", nextState:"unmuted"
            state "unmuted", label: '${name}', action:"mute", backgroundColor: "#ffffff", icon:"st.custom.sonos.unmuted", nextState:"muted"
        }
        
        main "switch"
        details(["switch","input","mute","poll"])
    }
}




def parse(String description) {
	log.debug "Parsing '${description}'"
    
 	def map = stringToMap(description)

    
    if(!map.body || map.body == "DQo=") { return }
        log.debug "${map.body} "
	def body = new String(map.body.decodeBase64())
    
	def statusrsp = new XmlSlurper().parseText(body)
	def power = statusrsp.Power.value.text()
    if(power == "ON") { 
    	sendEvent(name: "switch", value: 'on')
    }
    if(power != "" && power != "ON") { 
    	sendEvent(name: "switch", value: 'off')
    }
    

    def muteLevel = statusrsp.Mute.value.text()
    if(muteLevel == "on") { 
    	sendEvent(name: "mute", value: 'muted')
	}
    if(muteLevel != "" && muteLevel != "on") {
	    sendEvent(name: "mute", value: 'unmuted')
    }
    
    def inputCanonical = statusrsp.InputFuncSelect.value.text()
    def netCanonical = statusrsp.NetFuncSelect.value.text()
     
    // If NetFuncSelect exists, we're parsing formMainZone_MainZoneXml
    // If InputFunc is "NET", use the value in NetFunc instead.
    if(netCanonical != "") {
        if(inputCanonical == "NET") {
            sendEvent(name: "input", value: netCanonical)           
        } else {
            sendEvent(name: "input", value: inputCanonical)
        }
    }
    
    if(statusrsp.MasterVolume.value.text()) { 
    	def int volLevel = (int) statusrsp.MasterVolume.value.toFloat() ?: -40.0
        volLevel = (volLevel + 80) * 0.9
        
   		def int curLevel = 36
        try {
        	curLevel = device.currentValue("level")
        } catch(NumberFormatException nfe) { 
        	curLevel = 36
        }
	
        if(curLevel != volLevel) {
    		sendEvent(name: "level", value: volLevel)
        }
    } 
}


def setLevel(val) {
	sendEvent(name: "mute", value: "unmuted")     
    sendEvent(name: "level", value: val)
	def int scaledVal = val * 0.9 - 80
	request("cmd0=PutMasterVolumeSet%2F$scaledVal")
}

def on() {
	sendEvent(name: "switch", value: 'on')
	request('cmd0=PutZone_OnOff%2FON')
}

def off() { 
	sendEvent(name: "switch", value: 'off')
	request('cmd0=PutZone_OnOff%2FOFF')
}

def mute() { 
	sendEvent(name: "mute", value: "muted")
	request('cmd0=PutVolumeMute%2FON')
}

def unmute() { 
	sendEvent(name: "mute", value: "unmuted")
	request('cmd0=PutVolumeMute%2FOFF')
}

def toggleMute(){
    if(device.currentValue("mute") == "muted") { unmute() }
	else { mute() }
}

def poll() { 
	refresh()
}

def refresh() {

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

    def hubAction = new physicalgraph.device.HubAction(
   	 		'method': 'GET',
    		'path': "/goform/formMainZone_MainZoneXml.xml",
            'headers': [ HOST: "$destIp:$destPort" ] 
		)   
     
   
    hubAction
}

def request(body) { 

    def hosthex = convertIPtoHex(destIp)
    def porthex = convertPortToHex(destPort)
    device.deviceNetworkId = "$hosthex:$porthex" 

    def hubAction = new physicalgraph.device.HubAction(
   	 		'method': 'POST',
    		'path': "/MainZone/index.put.asp",
        	'body': body,
        	'headers': [ HOST: "$destIp:$destPort" ]
		) 
              
    hubAction
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}
