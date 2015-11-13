/**
 *  Honeywell Thermostat Director
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

// Automatically generated. Make future change here.

definition(
	name: "Honeywell Thermostat Director",
	namespace: "bdobrescu",
	author: "Bobby Dobrescu",
	description: "Adjust your Honeywell thermostat based on the temperature range of a specific temperature sensor",
	category: "Green Living",
	iconUrl: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",
	iconX2Url: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png"
)

preferences {
    page name:"pageSetup"
    page name:"TemperatureSettings"
    page name:"ThermostatandDoors"
    page name:"ThermostatBoost"
    page name:"ThermostatAway"
    page name:"Settings"

}

// Show setup page
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

	return dynamicPage(pageProperties) {
        section("Setup Menu") {
            href "TemperatureSettings", title: "Temperatures", description: "", state:greyedOut()
            href "ThermostatandDoors", title: "Thermostat & Sensors", description: "", state: greyedOutTherm()
            href "ThermostatBoost", title: "Thermostat Boost", description: "", state: greyedOutTherm1()
            href "ThermostatAway", title: "Thermostat Away Mode", description: "", state: greyedOutTherm2()
			href "Settings", title: "General Settings", description: "", state: greyedOutSettings()
            }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Page - Temperature Settings	
def TemperatureSettings() {

    def sensor = [
        name:       "sensor",
        type:       "capability.temperatureMeasurement",
        title:      "Which Sensor?",
        multiple:   false,
        required:   true
    ]
    def setLow = [
        name:       "setLow",
        type:       "number",
        title:      "Low temp?",
        required:   true
    ]
    
    def cold = [
        name:       "cold",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]

    def SetHeatingLow = [
        name:       "SetHeatingLow",
        type:       "number",
        title:		"Heating Temperature (degrees)",
        required:   false
    ]
    
     def SetCoolingLow = [
        name:       "SetCoolingLow",
        type:       "number",
        title:		"Cooling Temperature (degrees)",
        required:   false
    ]   
    
    def setHigh = [
        name:       "setHigh",
        type:       "number",
        title:      "High temp?",
        required:   true
    ]
    
    def hot = [
        name:       "hot",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]
    
    def SetHeatingHigh = [
        name:       "SetHeatingHigh",
        type:       "number",
        title:		"Heating Temperature (degrees)",
        required:   false
    ]
    
     def SetCoolingHigh = [
        name:       "SetCoolingHigh",
        type:       "number",
        title:		"Cooling Temperature (degrees)",
        required:   false
    ] 
    
    def neutral = [
        name:       "neutral",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]],
        required:   false
    ]

    def fan = [
        name:       "fan",
        type:       "enum",
        title:		"Fan mode?",
        metadata:   [values:["fanAuto", "fanOn", "fanCirculate"]],
        required:   false
    ]

    def pageName = "Temperature Settings"
    
    def pageProperties = [
        name:       "TemperatureSettings",
        title:      "Temperature Settings",
        nextPage:   "ThermostatandDoors"
    ]
    
    return dynamicPage(pageProperties) {

		section("Select the temperature sensor that will control your thermostat"){
			input sensor
		}
		section("When the temperature falls below this temperature..."){
			input setLow
		}
        section("Change the following settings:"){
			input cold
            input SetHeatingLow
            input SetCoolingLow
		}
        section("When the temperature raises above this temperature..."){
			input setHigh
		}
        section("Change the following settings:"){
			input hot
            input SetHeatingHigh
            input SetCoolingHigh
		}
        section("When temperature is between the previous temperatures, change thermostat to..."){
			input neutral
            input fan
		}        
    }
    
}

// Page - Thermostat and Sensors
def ThermostatandDoors() {

    def thermostat = [
        name:       "thermostat",
        type:       "capability.thermostat",
        title:      "Which Thermostat?",
        multiple:   false,
        required:   true
    ]
    
    def doors = [
        name:       "doors",
        type:       "capability.contactSensor",
        title:      "Which Sensor?",
        multiple:	true,
        required:   true
    ]
    
    def turnOffDelay = [
        name:       "turnOffDelay",
        type:       "decimal",
        title:		"Number of minutes",
        required:	false
    ]
    
    def pageName = "Thermostat and Doors"
    
    def pageProperties = [
        name:       "ThermostatandDoors",
        title:      "Thermostat and Sensors",
        nextPage:   "ThermostatBoost"
    ]

    return dynamicPage(pageProperties) {

		section(""){
        	paragraph "Optional: select one or more sensors to turn off the thermostat"
        }
        section("") {
			input thermostat
		}
        section("If these sensors are open, turn off thermostat regardless of temperature settings") {
			input doors
		}
		section("Wait this long before turning the thermostat off (defaults to 1 minute)") {
			input turnOffDelay
		}
    }
    
}

// Page - Thermostat Boost
def ThermostatBoost() {

    def turnOnTherm = [
        name: 		"turnOnTherm", 
        type:		"enum", 
        title:      "Which mode?",
 		metadata: 	[values: ["cool", "heat", "emergencyHeat"]], 
        required: 	false
    ]
    
    def modes1 = [
        name:		"modes1", 
        type:		"mode", 
        title: 		"Put thermostat into boost mode when Home Mode is...", 
        multiple: 	true, 
        required: 	false
    ]
    
    def coolingTemp = [
        name:       "coolingTemp",
        type:       "decimal",
        title:		"Cooling Temp?",
        required:	false
    ]
    
    def heatingTemp = [
        name:       "heatingTemp",
        type:       "number",
        title:		"Heating Temp?",
        required:	false
    ]
    
    def turnOffDelay2 = [
        name:       "turnOffDelay2",
        type:       "number",
        title:		"Number of minutes",
        required:	false
    ]
    
    def pageName = "Thermostat Boost"
    
    def pageProperties = [
        name:       "ThermostatBoost",
        title:      "Thermostat Boost",
        nextPage:   "ThermostatAway"
    ]

    return dynamicPage(pageProperties) {

		section(""){
        	paragraph "If your home is too hot or too cold, you can temporarily 'boost' your heating or cooling" +
            " needs for a predefined amount of time, before returning to regular schedule."
        }
        
        section("Select 'boost' mode...") {
    		input turnOnTherm
  		}
        
        section("Set the thermostat to the following temps") {
    		input heatingTemp
            input coolingTemp

  		}
  		section("For this many minutes...") {
    		input turnOffDelay2
  		}
        section("In addtion to 'app touch' the following modes will also boost the thermostat") {
   			input modes1
        }
    }
} 
// Page - Thermostat Away
def ThermostatAway() {

    def modes2 = [
        name:		"modes2", 
        type:		"mode", 
        title: 		"Put thermostat into Away Mode when Home Mode changes to...", 
        multiple: 	true, 
        required: 	false
    ]
    
    def away = [
        name:       "away",
        type:       "enum",
        title:		"Which Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]], 
        required: 	false
    ]

    def setAwayLow = [
        name:       "setAwayLow",
        type:       "decimal",
        title:      "Low temp?",
        required:   false
    ]
    
    def AwayCold = [
        name:       "AwayCold",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]
    
    def setAwayHigh = [
        name:       "setAwayHigh",
        type:       "decimal",
        title:      "High temp?",
        required:   false
    ]
    
    def AwayHot = [
        name:       "AwayHot",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]
    
    def SetHeatingAway = [
        name:       "SetHeatingAway",
        type:       "number",
        title:		"Heating Temperature (degrees)",
        required: 	false
    ]   
    
    def SetCoolingAway = [
        name:       "SetCoolingAway",
        type:       "number",
        title:		"Cooling Temperature (degrees)",
        required: 	false
    ]     
    
    def fanAway = [
        name:       "fanAway",
        type:       "enum",
        title:		"Fan mode?",
        metadata:   [values:["fanAuto", "fanOn", "fanCirculate"]],
        required: 	false
    ]

    def pageName = "Thermostat Away"
    
    def pageProperties = [
        name:       "ThermostatAway",
        title:      "Thermostat Away",
        nextPage:   "Settings"
    ]

    return dynamicPage(pageProperties) {

		section(""){
        	paragraph "Adjust the settings of your thermostat when the Home Mode changes to 'Away'."
        }
		
        section("Select Home Away Mode(s)...") {
   			input modes2
        }
           
        section("Change the thermostat to Away Mode...") {
    		input away
  		}
        
        section("Also change Away Fan to...") {
    		input fanAway
  		}
        
        section("Set the thermostat to the following temperatures") {
    		input SetHeatingAway
    		input SetCoolingAway
  		}
   		
        section("If the temperature falls below this temperature..."){
			input setAwayLow
		}
        
        section("Change thermostat to..."){
			input AwayCold
		}
        
        section("If the temperature raises above this temperature..."){
			input setAwayHigh
    	}
        section("Change thermostat to..."){
			input AwayHot
    	} 
	 }
}

// Show "Setup" page
def Settings() {

    def sendPushMessage = [
        name: 		"sendPushMessage",
        type: 		"enum", 
        title: 		"Send a push notification?", 
        metadata:	[values:["Yes","No"]], 
        required:	true, 
        defaultValue: "No"
    ]
    
    def phoneNumber = [
        name: 		"phoneNumber", 
        type:		"phone", 
        title: 		"Send SMS notifications to?", 
        required: 	false
    ]
    
    def days = [
        name:       "days",
        type:       "enum",
        title:      "Only on certain days of the week",
        multiple:   true,
        required:   false,
        options: 	["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    ]
    
    def modes = [
        name:		"modes", 
        type:		"mode", 
        title: 		"Only when mode is", 
        multiple: 	true, 
        required: 	false
    ]
    
    def pageName = "Settings"
    
    def pageProperties = [
        name:       "Settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {


		section( "Notifications" ) {
			input sendPushMessage
			input phoneNumber
		}
		section(title: "More options", hideable: true) {
			href "timeIntervalInput", title: "Only during a certain time", description: getTimeLabel(starting, ending), state: greyedOutTime(starting, ending), refreshAfterSelection:true
			input days
			input modes
		}    
    }
    
}

def installed(){
	log.debug "Installed called with $settings"
	init()
}

def updated(){
	log.debug "Updated called with $settings"
	unsubscribe()
	init()
}

def init(){
	state.lastStatus = null
	subscribe(app, appTouch)
    runIn(60, "temperatureHandler")
    subscribe(sensor, "temperature", temperatureHandler)
    if(modes1){
    	subscribe(location, modeBoostChange)
    }
    if(modes2){
    	subscribe(location, modeAwayChange)
    }
    if(doors){
            subscribe(doors, "contact.open", temperatureHandler)
            subscribe(doors, "contact.closed", doorCheck)
	}
}

def temperatureHandler(evt) {
	if(modeOk && daysOk && timeOk)  {
       	if (!modes2.contains(location.mode)){
            if(setLow > setHigh){
                def temp = setLow
                setLow = setHigh
                setHigh = temp
            }
            if (doorsOk) {
                def currentTemp = sensor.latestValue("temperature")
                if (currentTemp < setLow) {
                    if (state.lastStatus == "two" || state.lastStatus == "three" || state.lastStatus == null){
                        //log.info "Setting thermostat mode to ${cold}"
                        def msg = "I changed your ${thermostat} mode to ${cold} because temperature is below ${setLow}"
                        thermostat?."${cold}"()
                        thermostat?.setHeatingSetpoint(SetHeatingLow)
                        //thermostat?.setCoolingSetpoint(SetCoolingLow)
                        thermostat?.poll()
                        sendMessage(msg)
                        }
                    state.lastStatus = "one"
                }
                if (currentTemp > setHigh) {
                    if (state.lastStatus == "one" || state.lastStatus == "three" || state.lastStatus == null){
                        //log.info "Setting thermostat mode to ${hot}"
                        def msg = "I changed your ${thermostat} mode to ${hot} because temperature is above ${setHigh}"
                        thermostat?."${hot}"()
                        //thermostat?.setHeatingSetpoint(SetHeatingHigh)
                        thermostat?.setCoolingSetpoint(SetCoolingHigh)
                        thermostat?.poll()
                        sendMessage(msg)
                    }
                    state.lastStatus = "two"
                }
                if (currentTemp > setLow && currentTemp < setHigh) {
                    if (state.lastStatus == "two" || state.lastStatus == "one" || state.lastStatus == null){
                        //log.info "Setting thermostat mode to ${neutral}"
                        def msg = "I changed your ${thermostat} mode to ${neutral} because temperature is neutral"
                        thermostat?."${neutral}"()
                        thermostat?.setThermostatFanMode(fan)
                        sendMessage(msg)
                    }
                    state.lastStatus = "three"
                }
            }
            else{
                def delay = (turnOffDelay != null && turnOffDelay != "") ? turnOffDelay * 60 : 60
                log.debug("Detected open doors.  Checking door states again")
                runIn(delay, "doorCheck")
            }
        }
	}
}
def appTouch(evt) {
	if(modeOk && daysOk && timeOk) {
            if(turnOnTherm){
                state.lastStatus = "disabled"
                def currentCoolSetpoint = thermostat.latestValue("coolingSetpoint") as String
                def currentHeatSetpoint = thermostat.latestValue("heatingSetpoint") as String
                def currentMode = thermostat.latestValue("thermostatMode") as String
                def mode = turnOnTherm
                state.currentCoolSetpoint1 = currentCoolSetpoint
                state.currentHeatSetpoint1 = currentHeatSetpoint
                state.currentMode1 = currentMode

                    thermostat."${mode}"()
                    thermostat.setCoolingSetpoint(coolingTemp)
                    thermostat.setHeatingSetpoint(heatingTemp)

            thermoShutOffTrigger()
  
				}    
		}
}

def modeBoostChange(evt) {
	if(modeOk && daysOk && timeOk){
        if(thermostat && modes1.contains(location.mode)){
            state.lastStatus = "disabled"
            def currentCoolSetpoint = thermostat.latestValue("coolingSetpoint") as String
            def currentHeatSetpoint = thermostat.latestValue("heatingSetpoint") as String
            def currentMode = thermostat.latestValue("thermostatMode") as String
            def mode = turnOnTherm
            state.currentCoolSetpoint1 = currentCoolSetpoint
            state.currentHeatSetpoint1 = currentHeatSetpoint
            state.currentMode1 = currentMode

                thermostat."${mode}"()
                thermostat.setCoolingSetpoint(coolingTemp)
                thermostat.setHeatingSetpoint(heatingTemp)   
    }
	else{
		thermoShutOff()
    	}    
	}
}

def modeAwayChange(evt){ 
	if(modeOk && daysOk && timeOk){
    	if (modes2){
            if(modes2.contains(location.mode)){
                    state.lastStatus = "away"
                    thermostat."${away}"()             
                    thermostat.setHeatingSetpoint(SetHeatingAway)
                    thermostat.setCoolingSetpoint(SetCoolingAway)
                    thermostat.setThermostatFanMode(fanAway)
                    def msg = "I changed your ${thermostat} mode to ${away} because Home Mode is set to Away"   
                    sendMessage(msg) 
                    log.debug "Running AwayChange because mode is ${away} and last staus is ${lastStatus}"
            }
            else  {
        			log.debug "Away change did not run because mode is ${away} and last staus is ${lastStatus}"
			}
     	}
	}
}

def modeAwayTempHandler(evt) {

		if(lastStatus == "away"){
        //if(modes2.contains(location.mode)){
           if (currentTemp < setAwayLow) {
					thermostat?."${Awaycold}"()
                    thermostat?.poll()
                    def msg = "I changed your ${thermostat} mode to ${Awaycold} because temperature is below ${setAwayLow}"
                    sendMessage(msg)
  			}
				if (currentTemp > setHigh) {
					thermostat?."${Awayhot}"()
                    thermostat?.poll()
					def msg = "I changed your ${thermostat} mode to ${Awayhot} because temperature is above ${setAwayHigh}"
                    sendMessage(msg)
  				}
		Else {
        	log.debug "Away temps did not change because mode is ${away} and last staus is ${lastStatus}"
        	}
	}
}

def thermoShutOffTrigger() {
    log.info("Starting timer to turn off thermostat")
    def delay = (turnOffDelay2 != null && turnOffDelay2 != "") ? turnOffDelay2 * 60 : 60 
    state.turnOffTime = now()
	log.debug ("Turn off delay is ${delay}")
    runIn(delay, "thermoShutOff")
  }

def thermoShutOff(){
	if(state.lastStatus == "disabled"){
		def coolSetpoint = state.currentCoolSetpoint1
    	def heatSetpoint = state.currentHeatSetpoint1
		def mode = state.currentMode1
    	def coolSetpoint1 = coolSetpoint.replaceAll("\\]", "").replaceAll("\\[", "")
    	def heatSetpoint1 = heatSetpoint.replaceAll("\\]", "").replaceAll("\\[", "")
    	def mode1 = mode.replaceAll("\\]", "").replaceAll("\\[", "")
    
		state.lastStatus = null
		//log.info("Returning thermostat back to normal")
		thermostat.setCoolingSetpoint("${coolSetpoint1}")
    	thermostat.setHeatingSetpoint("${heatSetpoint1}")
    	thermostat."${mode1}"()
    	temperatureHandler()
    }
}

def doorCheck(evt){
	if (!doorsOk){
		log.debug("doors still open turning off ${thermostat}")
		def msg = "I changed your ${thermostat} mode to off because some doors are open"
		
        if (state.lastStatus != "off"){
        	thermostat?.off()
			sendMessage(msg)
		}
		state.lastStatus = "off"
	}

	else{
    	if (state.lastStatus == "off"){
			state.lastStatus = null
        }
        temperatureHandler()
	}
}

private sendMessage(msg){
	if (sendPushMessage == "Yes") {
		sendPush(msg)
	}
	if (phoneNumber != null) {
		sendSms(phoneNumber, msg)
	}
}

private getAllOk() {
	modeOk && daysOk && timeOk && doorsOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDoorsOk() {
	def result = !doors || !doors.latestValue("contact").contains("open")
	log.trace "doorsOk = $result"
	result
}


private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
    
    else if (starting){
    	result = currTime >= start
    }
    else if (ending){
    	result = currTime <= stop
    }
    
	log.trace "timeOk = $result"
	result
}

def getTimeLabel(starting, ending){

	def timeLabel = "Tap to set"
	
    if(starting && ending){
    	timeLabel = "Between" + " " + hhmm(starting) + " "  + "and" + " " +  hhmm(ending)
    }
    else if (starting) {
		timeLabel = "Start at" + " " + hhmm(starting)
    }
    else if(ending){
    timeLabel = "End at" + hhmm(ending)
    }
	timeLabel
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}
def greyedOut(){
	def result = ""
    if (sensor) {
    	result = "complete"	
    }
    result
}

def greyedOutTherm(){
	def result = ""
    if (thermostat) {
    	result = "complete"	
    }
    result
}

def greyedOutTherm1(){
	def result = ""
    if (turnOnTherm) {
    	result = "complete"	
    }
    result
}

def greyedOutTherm2(){
	def result = ""
    if (modes2) {
    	result = "complete"	
    }
    result
}

def greyedOutSettings(){
	def result = ""
    if (starting || ending || days || modes || sendPushMessage) {
    	result = "complete"	
    }
    result
}

def greyedOutTime(starting, ending){
	def result = ""
    if (starting || ending) {
    	result = "complete"	
    }
    result
}

private anyoneIsHome() {
  def result = false

  if(people.findAll { it?.currentPresence == "present" }) {
    result = true
  }

  log.debug("anyoneIsHome: ${result}")

  return result
}

page(name: "timeIntervalInput", title: "Only during a certain time", refreshAfterSelection:true) {
		section {
			input "starting", "time", title: "Starting (both are required)", required: false 
			input "ending", "time", title: "Ending (both are required)", required: false 
		}
        }