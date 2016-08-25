/**
*	NYCE Ceiling Motion Sensor
*
*	Copyright 2016 NYCE Sensors Inc.
*
*	File: nyce-ceiling-motion.groovy
*	Version 1.0.3
*	Last Edited: 24 Aug 2016
*	By: RC
*
*/

metadata {
	definition (name: "NYCE Ceiling Motion Sensor", namespace: "NYCE", author: "NYCE") {
		capability "Battery"
		capability "Configuration"
		capability "Motion Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Refresh"

		command "enrollResponse"

		attribute "batteryReportType", "string"

		fingerprint inClusters: "0000,0001,0003,0402,0405,0406,0500,0020", manufacturer: "NYCE", model: "3043"
	}

	simulator {

	}

	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'Occupied', icon:"st.motion.motion.active", backgroundColor:"#ffa81e")
			state("inactive", label:'Vacant', icon:"st.motion.motion.inactive", backgroundColor:"#79b821")
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		valueTile("temperature", "device.temperature") {
			state("temperature", label:'${currentValue}${unit}', unit:'°', icon:"st.Weather.weather2", backgroundColor:"#00adc6")
		}

		valueTile("humidity", "device.humidity") {
			state("humidity", label:'${currentValue}${unit}', unit:'%', icon:"st.Weather.weather12", backgroundColor:"#00adc6")
		}

		main (["motion"])
		details(["motion","battery","refresh","temperature","humidity"])
 	}
}

def parse(String description) {
	Map map = [:]

	List listMap = []
	List listResult = []

	log.debug "parse: Parse message: ${description}"

	if(description?.startsWith("enroll request")) {
		List cmds = enrollResponse()

		log.debug "parse: enrollResponse() ${cmds}"
		listResult = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	else {
		if(description?.startsWith("zone status")) {
			listMap = parseIasMessage(description)
		}
		else if(description?.startsWith("read attr -")) {
			map = parseReportAttributeMessage(description)
		}
		else if(description?.startsWith("catchall:")) {
			map = parseCatchAllMessage(description)
		}
		else if(description?.startsWith("updated")) {
			List cmds = configure()
			listResult = cmds?.collect { new physicalgraph.device.HubAction(it) }
		}
		else if(description?.startsWith("temperature:")) {
			map = parseTemperatureMessage(description)
		}
		else if(description?.startsWith("humidity: ")) {
			map = parseHumidityMessage(description)
		}

		// Create events from map or list of maps, whichever was returned
		if(listMap) {
			for(msg in listMap) {
				listResult << createEvent(msg)
			}
		}
		else if(map) {
			listResult << createEvent(map)
		}
	}

	log.debug "parse: listResult ${listResult}"
	return listResult
}

private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	// 0x07 is bind message
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
							 cluster.command == 0x0B ||
							 cluster.command == 0x07 ||
							 (cluster.data.size() > 0 && cluster.data.first() == 0x3e)

	return !ignoredMessage
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)

    log.info "IN parseCatchAllMessage()"

	if (shouldProcessMessage(cluster)) {
		def msgStatus = cluster.data[2]

		log.debug "parseCatchAllMessage: msgStatus: ${msgStatus}"

		if(msgStatus == 0) {
			switch(cluster.clusterId) {
				case 0x0001:
					log.debug 'Battery'

					if(cluster.attrId == 0x0020) {
						if(state.batteryReportType == "voltage") {
							resultMap.name = 'battery'
							resultMap.value = getBatteryPercentage(cluster.data.last)
							log.debug "Battery Voltage convert to ${resultMap.value}%"
						}
					}
					else if(cluster.attrId == 0x0021) {
						if(state.batteryReportType == "percentage") {
							resultMap.name = 'battery'
							resultMap.value = (cluster.data.last / 2)
							log.debug "Battery Percentage convert to ${resultMap.value}%"
						}
					}
					break
				case 0x0402:    // temperature cluster
					log.debug 'Temperature'

					if(cluster.command == 0x01) {
						if(cluster.data[3] == 0x29) {
							def tempC = Integer.parseInt(cluster.data[-2..-1].reverse().collect{cluster.hex1(it)}.join(), 16) / 100
							resultMap = getTemperatureResult(getConvertedTemperature(tempC))
							log.debug "Temp resultMap: ${resultMap}"
						}
						else {
							log.debug "Temperature cluster Wrong data type"
						}
					}
					else {
						log.debug "Unhandled Temperature cluster command ${cluster.command}"
					}
					break
				case 0x0405:    // humidity cluster
					log.debug 'Humidity'

					if(cluster.command == 0x01) {
						if(cluster.data[3] == 0x21) {
							def hum = Integer.parseInt(cluster.data[-2..-1].reverse().collect{cluster.hex1(it)}.join(), 16) / 100
							resultMap = getHumidityResult(hum)
							log.debug "Hum resultMap: ${resultMap}"
						}
						else {
							log.debug "Humidity cluster wrong data type"
						}
					}
					else {
						log.debug "Unhandled Humidity cluster command ${cluster.command}"
					}
					break
				default:
					break
			}
		}
		else {
			log.debug "Message error code: Error code: ${msgStatus}    ClusterID: ${cluster.clusterId}    Command: ${cluster.command}"
		}
	}

	log.info "OUT parseCatchAllMessage()"
	return resultMap
}

private int getBatteryPercentage(int value) {
	def minVolts = 2.3
	def maxVolts = 3.0
	def volts = value / 10
	def pct = (volts - minVolts) / (maxVolts - minVolts)

	//for battery that may have a higher voltage than 3.0V
	if(pct > 1) {
		pct = 1
	}

	if(pct <= 0) {
		pct = 0.07
	}
	return (int) pct * 100
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) {
		map, param -> def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	Map resultMap = [:]

	log.info "IN parseReportAttributeMessage()"
	log.debug "descMap ${descMap}"

	switch(descMap.cluster) {
		case "0001":
			log.debug "Battery"

			if(descMap.attrId == "0020") {
				if(state.batteryReportType == "voltage") {
					resultMap.name = 'battery'
					resultMap.value = getBatteryPercentage(convertHexToInt(descMap.value))
					log.debug "Battery Voltage convert to ${resultMap.value}%"
				}
			}
			else if(descMap.attrId == "0021") {
				if(descMap.result != "unsupported attr") {
					state.batteryReportType = "percentage"
				}
				else {
					state.batteryReportType = "voltage"
				}

				if(state.batteryReportType == "percentage")	{
					resultMap.name = 'battery'
					resultMap.value = (convertHexToInt(descMap.value) / 2)
					log.debug "Battery Percentage convert to ${resultMap.value}%"
				}
			}
			break
		default:
			log.info descMap.cluster
			log.info "cluster1"
			break
	}

	log.info "OUT parseReportAttributeMessage()"
	return resultMap
}

private Map parseTemperatureMessage(String description) {
	Map resultMap = [:]

	def tempC = Float.parseFloat((description - "temperature: ").trim())

	resultMap = getTemperatureResult(getConvertedTemperature(tempC))

	log.debug "parseTemperatureMessage: Temp resultMap: ${resultMap}"

	return resultMap
}

private Map parseHumidityMessage(String description) {
	Map resultMap = [:]

	def hum = Float.parseFloat((description - "humidity: " - "%").trim()).round()

	resultMap = getHumidityResult(hum)

	log.debug "parseHumidityMessage: Hum resultMap: ${resultMap}"

	return resultMap
}

def getConvertedTemperature(value) {
	if(getTemperatureScale() == "C") {
		return value.toDouble().round()
	}
	else {
		return celsiusToFahrenheit(value).toDouble().round()
	}
}

private Map getTemperatureResult(value) {
	return [
		name: "temperature",
		value: value,
		unit: "°" + getTemperatureScale()
	]
}

private Map getHumidityResult(value) {
	return [
		name: "humidity",
		value: value,
		unit: "%RH"
	]
}

private List parseIasMessage(String description) {
	List parsedMsg = description.split(" ")
	String msgCode = parsedMsg[2]

	List resultListMap = []
	Map resultMap_battery = [:]
	Map resultMap_battery_state = [:]
	Map resultMap_sensor = [:]

	// Relevant bit field definitions from ZigBee spec
	def BATTERY_BIT = ( 1 << 3 )
	def TROUBLE_BIT = ( 1 << 6 )
	def SENSOR_BIT = ( 1 << 1 )		// it's ALARM1 bit from the ZCL spec

	// Convert hex string to integer
	def zoneStatus = Integer.parseInt(msgCode[-4..-1],16)

	log.info "IN parseIasMessage()"
	log.debug "zoneStatus: ${zoneStatus}"

	// Check each relevant bit, create map for it, and add to list
	log.debug "Battery Status ${zoneStatus & BATTERY_BIT}"
	log.debug "Trouble Status ${zoneStatus & TROUBLE_BIT}"
	log.debug "Sensor Status ${zoneStatus & SENSOR_BIT}"

	resultMap_sensor.name = "motion"
	resultMap_sensor.value = (zoneStatus & SENSOR_BIT) ? "active" : "inactive"

	resultListMap << resultMap_battery_state
	resultListMap << resultMap_battery
	resultListMap << resultMap_sensor

	log.info "OUT parseIasMessage()"
	return resultListMap
}

def configure() {
	String zigbeeId = swapEndianHex(device.hub.zigbeeId)

	attrInit()

	def configCmds = [
		//battery reporting and heartbeat
		"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 1 {${device.zigbeeId}} {}", "delay 200",

		//configure battery voltage report
		"zcl global send-me-a-report 1 0x20 0x20 600 3600 {01}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500",

		//configure battery percentage report
		"zcl global send-me-a-report 1 0x21 0x20 600 3600 {01}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500",

		// Writes CIE attribute on end device to direct reports to the hub's EUID
		"zcl global write 0x500 0x10 0xf0 {${zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 500",

		// Set binding for temperature and humidity (use default settings)
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0402 {${device.zigbeeId}} {}",

		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0405 {${device.zigbeeId}} {}"
	]

	log.debug "configure: Write IAS CIE"
	return configCmds + refresh()
}

def attrInit() {
	log.debug "Attr Init"
	state.batteryReportType = "voltage"
}

def enrollResponse() {
	[
		// Enrolling device into the IAS Zone
		"raw 0x500 {01 23 00 00 00}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1"
	]
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;

	while(j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}

	return array
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def refresh()
{
	log.debug "Refreshing Battery"
	[
		"st rattr 0x${device.deviceNetworkId} ${endpointId} 1 0x20",
		"st rattr 0x${device.deviceNetworkId} ${endpointId} 1 0x21"
	]
}
