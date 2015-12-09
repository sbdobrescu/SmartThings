definition(
    name: "Notify with Blinking Hue!",
    singleInstance: true,
    namespace: "sbdobrescu",
    author: "Bobby Dobrescu",
    description: "Parent app for Notify with Hue Settings",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@3x.png"
)

preferences {
    page(name: "mainPage", title: "Notification Settings", install: true, uninstall: true,submitOnChange: true) {
            section {
                    app(name: "childRules", appName: "Notify with Hue Settings", namespace: "sbdobrescu", title: "Settings...", multiple: true)
            }
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
    childApps.each {child ->
            log.info "Installed Rules: ${child.label}"
    }
}
