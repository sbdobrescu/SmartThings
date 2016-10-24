/**
 *  	Denon Network Receiver 
 *    	Based on Denon/Marantz receiver by Kristopher Kubicki
 *    	SmartThings driver to connect your Denon Network Receiver to SmartThings
 *		Tested with AVR-S710W (game1 & game2 inputs are not available), AVR 1912
 */

preferences {
    input("destIp", "text", title: "IP", description: "The device IP")
    input("destPort", "number", title: "Port", description: "The port you wish to connect", defaultValue: 80)
}

metadata {
    definition (name: "Denon AVR", namespace: "SB", 
        author: "Bobby Dobrescu") {
        capability "Actuator"
        capability "Switch" 
        capability "Polling"
        capability "Switch Level"
        capability "Music Player" 
        
        attribute "mute", "string"
        attribute "input", "string"
		attribute "inputChan", "enum"
        
        command "mute"
        command "unmute"
        command "toggleMute"
		command "cbl"
		command "tv"
		command "BD"
		command "MP"
		command "BT"
		command "G1"
		command "G2"    
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
           		attributeState "default", label:'Volume Level: ${name}', action:"setLevel"
            }
            tileAttribute("device.input", key: "SECONDARY_CONTROL") {
            	attributeState ("default", label:'Current Input: ${currentValue}')
        	}
        }        
        standardTile("poll", "device.poll", width: 2, height: 2, decoration: "flat") {
            state "poll", label: "", action: "polling.poll", icon: "st.secondary.refresh", backgroundColor: "#FFFFFF"
        }
        standardTile("mute", "device.mute", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "muted", action:"unmute", backgroundColor: "#ffffff", icon:"st.custom.sonos.muted", nextState:"unmuted"
            state "unmuted", action:"mute", backgroundColor: "#ffffff", icon:"st.custom.sonos.unmuted", nextState:"muted"
        }
        standardTile("CBL", "device.switch", width: 2, height: 2, decoration: "flat"){
        	state "Cable", label: 'Cable', action: "cbl", icon:"st.Electronics.electronics3"
        	}
        standardTile("TV", "device.switch", width: 2, height: 2, decoration: "flat"){
        	state "TV Audio", label: 'TV Audio', action: "tv", icon:"st.Electronics.electronics18"
        	}
        standardTile("BD", "device.switch", width: 2, height: 2, decoration: "flat"){
        	state "Blu-ray", label: 'Blu-ray', action: "BD", icon:"st.Electronics.electronics8"
        	}
        standardTile("MP", "device.switch", width: 2, height: 2, decoration: "flat"){
        	state "Media Player", label: 'Media Player', action: "MP", backgroundColor: "#ffffff", icon:"st.Electronics.electronics6"
			}
        standardTile("BT", "device.switch", width: 2, height: 2, decoration: "flat"){
        	state "BT", label: 'Bluetooth', action: "BT", icon:"st.Electronics.electronics2"
        	}
        standardTile("G1", "device.switch", width: 2, height: 2, decoration: "flat"){
        	state "G1", label: 'Game 1', action: "G1", icon:"st.Electronics.electronics11"
        	}
        standardTile("G2", "device.switch", width: 2, height: 2, decoration: "flat"){
        	state "G2", label: 'Game 2', action: "G1", icon:"st.Electronics.electronics19"
        	}            
        main "switch"
        details(["switch","input","mute","CBL", "TV", "BD", "MP", "BT", "G1", "G2","poll"])
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

def cbl() {
	def cbl = "SAT/CBL"
    log.debug "Setting input to '${cbl}'"
    request("cmd0=PutZone_InputFunction%2FSAT/CBL")
	}

def tv() {
    log.debug "Setting input to TV Audio"
    request("cmd0=PutZone_InputFunction%2FTV")
    }

def BD() {
	log.debug "Setting input to Blu-ray"
    request("cmd0=PutZone_InputFunction%2FBD")
	}

def MP() {
	log.debug "Setting input to Media Player"
    request("cmd0=PutZone_InputFunction%2FMPLAY")

	}

def BT() {
	log.debug "Setting input to Bluetooth"
    request("cmd0=PutZone_InputFunction%2FBT")
}
def G1() {
	log.debug "Setting input to Game 1"
    request("cmd0=PutZone_InputFunction%2FGAME1")
}
def G2() {
	log.debug "Setting input to Game 2"
    request("cmd0=PutZone_InputFunction%2FGAME2")
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
