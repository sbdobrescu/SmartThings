/**
 *  3400-X Keypad Manager
 *
 *  Copyright 2015 Mitch Pond
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Keypad Manager",
    namespace: "mitchpond",
    author: "Mitch Pond",
    description: "Service manager for Centralite 3400-X security keypad. Keeps keypad state in sync with Smart Home Monitor",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: false)


preferences {
	page(name: "setupPage")
}

def setupPage() {
	dynamicPage(name: "setupPage",title: "", install: true, uninstall: true) {
        section("Settings") {
			paragraph "NOTE: All PIN codes should be four digits. Shorter PINs will be padded with leading zeroes. (42 becomes 0042)"
			paragraph "Disclaimer: PIN 0000 is generated when ON button is pressed, therefore this code has been restricted in order to use the ON button"
            input(name: "keypad", title: "Keypad", type: "capability.lockCodes", multiple: false, required: true)
            input(name: "pin"	, title: "Security PIN code", type: "number", range: "0001..9999", required: true)
        }
		def routines = location.helloHome?.getPhrases()*.label
        routines?.sort()
        section("Security Routines", hideable: true, hidden: false) {
        	input(name: "armRoutine", title: "Arm/Away routine", type: "enum", options: routines, required: false)
            input(name: "disarmRoutine", title: "Disarm routine", type: "enum", options: routines, required: false)
            input(name: "stayRoutine", title: "Arm/Stay routine", type: "enum", options: routines, required: false)
        }
		section("Other Control") {
            input name: "pin1"	, title: "Light Control PIN code (toggles switches)", type: "number", range: "0000..9999", required: false, submitOnChange: true
			if(pin1) input "sSwitches", "capability.switch", title: "Switches", required: false, multiple: true, submitOnChange: true
			input "pBbutton", "capability.switch", title: "When Panic Button is Pushed, Toggle these Switches", required: false, multiple: true, submitOnChange: true
			input "onButton", "capability.switch", title: "When ON Button is Pushed, Toggle these Switches", required: false, multiple: true, submitOnChange: true
        }     
		section("Name") {            
            label(title: "Assign a name", required: false)
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(location,"alarmSystemStatus",alarmStatusHandler)
    subscribe(keypad,"codeEntered",codeEntryHandler)   
    subscribe(keypad,"button.pushed",buttonNumHandler)  

//initialize keypad to correct state
    def event = [name:"alarmSystemStatus", value: location.currentState("alarmSystemStatus").value, 
    			displayed: true, description: "System Status is ${shmState}"]
    alarmStatusHandler(event)
}

//Returns the PIN padded with zeroes to 4 digits
private String getPIN(type){
if (type == "security") return settings.pin.value.toString().padLeft(4,'0')
else return settings.pin1.value.toString().padLeft(4,'0')
}

// TODO: implement event handlers
def alarmStatusHandler(event) {
	log.debug "Keypad manager caught alarm status change: "+event.value
    if (event.value == "off") keypad?.setDisarmed()
    else if (event.value == "away") keypad?.setArmedAway()
    else if (event.value == "stay") keypad?.setArmedStay()
}

private sendSHMEvent(String shmState){
	def event = [name:"alarmSystemStatus", value: shmState, 
    			displayed: true, description: "System Status is ${shmState}"]
    sendLocationEvent(event)
}

private execRoutine(armMode) {
	if (armMode == 'away') location.helloHome?.execute(settings.armRoutine)
    else if (armMode == 'stay') location.helloHome?.execute(settings.stayRoutine)
    else if (armMode == 'off') location.helloHome?.execute(settings.disarmRoutine)    
}

def codeEntryHandler(evt){
	//do stuff
    log.debug "Caught code entry event! ${evt.value.value}"
    log.warn "keypad data ${evt.value}"
    def codeEntered = evt.value as String
    def correctCode = getPIN("security")
    def data = evt.data as String
    def armMode = ''
    
    if (data == '0') armMode = 'off'
    else if (data == '3') armMode = 'away'
    else if (data == '1') armMode = 'stay'
    else if (data == '2') armMode = 'stay'	//Currently no separate night mode for SHM, set to 'stay'
    else {
    	log.error "${app.label}: Unexpected arm mode sent by keypad!: "+data
        return []
        }
    
    if (codeEntered == correctCode) {
    	log.debug "Correct PIN entered. Change SHM state to ${armMode}"
        keypad.acknowledgeArmRequest(data)
        sendSHMEvent(armMode)
		execRoutine(armMode)
        
    }
    else {
    	log.debug "Checking Control PIN"
		correctCode = getPIN("control")	
		if (codeEntered == correctCode) {
            keypad.sendInvalidKeycodeResponse()
    		log.debug "Correct control PIN entered. Toggle switches"
            if (sSwitches) {
                if (sSwitches?.currentValue('switch').contains('on')) {
                    sSwitches?.off()
                    }
                else if (sSwitches?.currentValue('switch').contains('off')) {
                    sSwitches?.on()
                    }
            }
    	}
        else {
        	if(codeEntered == "0000") {
            keypad.sendInvalidKeycodeResponse()
    		log.debug "On button code entered. Toggle switches"
                if (onButton) {
                    if (onButton?.currentValue('switch').contains('on')) {
                        onButton?.off()
                    }
                    else if (onButton?.currentValue('switch').contains('off')) {
                        onButton?.on()
                    }
                }
         	}
        	else {
            log.debug "No match. PIN entered was $codeEntered. Sending InvalidKeycodeResponse"
            keypad.sendInvalidKeycodeResponse()
            }
        }
    }
}

def buttonNumHandler(evt) {
	def event = evt.data
    def eVal = evt.value
    def eName = evt.name
    def eDev = evt.device

    if(parent) log.info "button event received: event = $event, eVal = $eVal, eName = $eName, eDev = $eDev, eDisplayN = $eDisplayN, eDisplayT = $eDisplayT, eTxt = $eTxt"
		def buttonNumUsed = evt.data.replaceAll("\\D+","")
        buttonNumUsed = buttonNumUsed.toInteger()
       	int butNum = buttonNumUsed 
		log.warn "button num = $butNum, value = $eVal"
        log.debug "Panic button pushed. Toggle switches"
                if (pButton) {
                    if (pButton?.currentValue('switch').contains('on')) {
                        pButton?.off()
                    }
                    else if (pButton?.currentValue('switch').contains('off')) {
                        pButton?.on()
                    }
                }       
}  
