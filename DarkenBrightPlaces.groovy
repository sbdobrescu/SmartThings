 *  Darken Bright Places
 *
 *  Copyright 2015 Bobby Dobrescu
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
    name: "Darken Bright Places",
    namespace: "sbdobrescu",
    author: "Bobby Dobrescu",
    description: "Turn off some switches when door opens while motion is active",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Select Switches and Sensors") {
		input "switches", "capability.switch", title: "Switches", required: true, multiple: true
        input "motion1", "capability.motionSensor", title: "Motion Sensor", required: true, multiple: false		
        input "contact1", "capability.contactSensor", title: "Open/Close Sensor", required: true, multiple: false
	}
}

def installed() {
	log.debug "Installed called with $settings"
    initialize()
}

def updated() {
	log.debug "Updated called with $settings"
    unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motion1, "motion.active", motionHandler)
    subscribe(contact1, "contact.open", contactHandler)
	state.pendingOff = null
}

def motionHandler(evt) {
	log.debug "$evt.value"
    state.pendingOff = "motion" // Motion Is Active
    }
 
def contactHandler(evt) {
	log.debug "$evt.value"
    if (state.pendingOff == "motion"){
        switchesOff()
     }
}        
	
    
def switchesOff() {
    switches.off()
    log.info "Lights turned off because motion was active when the door opened"
    }
  
