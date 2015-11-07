/**
 *  Copyright 2015 SmartThings
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
 *  Brighten My Path
 *
 *  Author: SmartThings
 */
definition(
    name: "Movie Night is Over",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights on when motion is detected and the TV is off.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png"
)

preferences {
	section("When there's movement...") {
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("And this switch is OFF...") {
		input "disable", "capability.switch", title: "Which?", multiple: false
	}	
    
    section("Turn on light(s)...") {
		input "switch1", "capability.switch", multiple: true
	}
	section("You can also:"){
		input "turnOff", "bool", title: "Turn off when motion stops", required: false
        input "minutes", "number", title: "Wait this many minutes to turn off after motion stops (optional)", required: false
		}
}
def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motion1, "motion.active", motionActiveHandler)
    subscribe(disable, "switch.off", motionActiveHandler)
	if(turnOff) {
		subscribe(motions, "motion.inactive", motionActiveHandler)
	}
}

def motionActiveHandler(evt) {
	log.debug "$evt.value"
	if (evt.value == "active") {
    		switch1.on()
            log.debug("${switch1.label ?: switch1.name} turning on.")
    } else if (evt.value == "inactive") {
            if(minutes) runIn(minutes*60, switchoff) else switchoff()
        }
}

def switchoff() {
    switch1.off()
	log.debug("${switch1.label ?: switch1.name} turning off.")
}
