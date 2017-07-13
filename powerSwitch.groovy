/**
 *  Power Switch
 *
 *  Copyright 2016 SmartThings
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
    name: "Power Switch",
    namespace: "BD",
    author: "BDOBRESCU",
		description: "Turn a switch on and then off (after a period of time) when certain power consumption level is reached.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    singleInstance: fase
)

preferences {
	section {
		input(name: "meter", type: "capability.powerMeter", title: "Select Power Meter", required: true, multiple: false, description: null)
        input(name: "threshold", type: "number", title: "Minimum Wattage Threshold", required: true, description: "in either watts or kw.")
		input(name: "minutes", type: "number", title: "Below Threshold Delay", required: false, description: "in minutes (optional)")
    }
    section {
    	input(name: "switches", type: "capability.switch", title: "Turn ON This Switch", required: true, multiple: false, description: null)    
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
	subscribe(meter, "power", meterHandler)
}


def meterHandler(evt) { 
    def meterValue = evt.value as double
    def thresholdValue = threshold as int
    if (!state.cycleOn && meterValue > thresholdValue ) {
        log.debug "Cycle started, turning ${switches} on."
        switches.on()
        state.cycleOn = true
    }
   else if (state.cycleOn && meterValue <= thresholdValue) {   
        if (minutes) {
        	runIn(minutes * 60, bufferPending)
        	state.meterValueOld = meterValue       
		}	
        	else {
        		switches.off()
        		state.cycleOn = false
        		log.debug "Power: ${meterValue} W, turning ${switches} off"
        }
  	}
}

def bufferPending() {  
    def meterValueNew = meter.currentValue("power") as double //evt.value 
    def thresholdValueNew = threshold as int      
    if (state.cycleOn && meterValueNew <= thresholdValueNew ) {
        switches.off()
        state.cycleOn = false
        log.debug "Power after delay is: ${meterValueNew} W, turning ${switches} off"
        }
}
