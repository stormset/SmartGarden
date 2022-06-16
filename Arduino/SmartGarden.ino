#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <EEPROM.h>
#include <TimeLib.h>
#include <WiFiManager.h>
#include <ESP8266WebServer.h>

#define EEPROM_SIZE 256

/* PINOUT */
int relays[] =  { 16, 5, 4, 2 };     // relay pins
int buttons[] = { 14, 12, 13, 15 };  // button pins - are mapped to relay pins by index
const bool buttonsEnabled = false;   // are buttons enabled
const int relayCount = (sizeof(relays)/sizeof(*relays));

/* PARAMETERS - should be configurable through the UI */
const String CODE = "0000";        // the authentification code (should be included in every GET request)
const bool onState = 1;            // relay is Normally-Open (NO) or Normally-Closed (NC)
const uint16_t interval_us = 1000; // timer tick interval in ms

/* Webserver */
ESP8266WebServer server(80);

/* Global variables */
int setTimers[relayCount] = { 0 };       // the time in seconds the relays are set to
int timerRemaining[relayCount] = { 0 };  // the time in seconds the timers have remaining
int runtimes[relayCount] = { 0 };        // the time in seconds the relays have been running


// The handler for web-requests.
void handleRequests() {
  String content = "";
  
  // Note: client app uses start indexing from 1
  if (server.hasArg("deviceAuth")) {
    if (server.arg("deviceAuth") == CODE) {
      if (server.hasArg("switchRequest")) {
        // switch loads with relay-only state response
        toggleRelay((server.arg("switchRequest")).toInt() - 1);
        content += relayStateString();
      }
      if (server.hasArg("switchStateRequest")) {
        // switch loads with detailed state response
        toggleRelay((server.arg("switchStateRequest")).toInt() - 1);
        content += relayStateString() + "|" + timerRemainingString() + "|" + runtimeString() + "|" + getTemperature();
      } else if (server.hasArg("saveRequest")) {
        // save a new config
        deleteSavedInstance();
        writeToEEPROM(server.arg("saveRequest"));
        content += "ok";
      } else if (server.hasArg("checkSavedInstance")) {
        // return whether there is an already stored config
        String tmp = readFromEEPROM();
        content += char(EEPROM.read(0)) != '\0';
        // TODO: validate configs (length, etc.)
      } else if (server.hasArg("deleteSavedInstance")) {
        // delete config
        deleteSavedInstance();
        content += "ok";
      } else if (server.hasArg("getSavedInstance")) {
        // retrieve config
        content += readFromEEPROM();
      } else if (server.hasArg("stateRequest")) {
        // get detailed state info
        content += relayStateString() + "|" + timerRemainingString() + "|" + runtimeString() + "|" + getTemperature();
      } else if (server.hasArg("setTimer") && server.hasArg("setTimerTime")) {
        // set timer
        startTimer(server.arg("setTimer").toInt() - 1, server.arg("setTimerTime").toInt());
        content += timerRemainingString();
      } else {
        content += server.arg("deviceAuth");
      }
    }
  }

  server.send(200, "text/plain; charset=UTF-8", content);
}

// The handler for not found web-request.
void handleNotFound() {
  server.send(404, "text/plain", "Not found");
}

// Writes the settings to the EEPROM but only if it fits (determined by the SIZE macro)
void writeToEEPROM(String str) {
  size_t len = str.length();
  if (len > EEPROM_SIZE)
    return;

  for (int i = 0; i < len; i++)
    EEPROM.write(i, str[i]);

  EEPROM.commit();
}

// Reads the settings from the EEPROM
String readFromEEPROM() {
  String retVal = "";
  
  for (int i = 0; i < EEPROM_SIZE; i++){
    char chr = EEPROM.read(i);
    if (chr == '\0')
      break;
      
    retVal += chr;
  }

  return retVal;
}

// Deletes the settings from the EEPROM
void deleteSavedInstance() {
  for (int i = 0; i < EEPROM_SIZE; i++)
    EEPROM.write(i, '\0');
  
   EEPROM.commit();
}

// Handler for the timer tick.
void inline timerHandler(void) {
  for (int i = 0; i < relayCount; i++)
  {
    // count runtime (seconds)
    if (readRelay(i) == 1)
      runtimes[i]++;
    else
      runtimes[i] = 0;

    // handle timer states
    if (timerRemaining[i] > 0) {
      timerRemaining[i]--;
      if (timerRemaining[i] == 0)
        setRelay(i, 0);
    }
  }

  timer0_write(ESP.getCycleCount() + interval_us * 80000); // 160 when running at 160mhz
}

// Starts the timer for a relay.
// @param ind:  the index of the relay in the relays array to start the timer for
void startTimer(int ind, int duration) {
  if (duration < 0)
    return;

  timerRemaining[ind] = duration;
}

// Sets the state with respect to the relay type (NO/NC) and also clears it's timer
// @param ind:  the index of the relay in the relays array
// @param state:  the state to write to the relay
void setRelay(int ind, int state) {
  timerRemaining[ind] = 0;
  digitalWrite(relays[ind], (onState == 1 ? state : !state));
}

// Toggles the state of the relay
// @param ind:  the index of the relay in the relays array
void toggleRelay(int ind) {
  timerRemaining[ind] = 0;
  digitalWrite(relays[ind], !digitalRead(relays[ind]));
}

// Returns the relay state attributing its type (NC/NO)
// @param ind:  the index of the relay in the relays array
bool readRelay(int ind) {
  return onState ? digitalRead(relays[ind]) : !digitalRead(relays[ind]);
}

// Constructs a string of relay states.
String relayStateString() {
  String str = "";
  for (int i = 0; i < relayCount; i++) {
    str += ((String) readRelay(i)) + ";";
  }

  return str;
}

// Constructs a string of remaining times.
String timerRemainingString() {
  String str = "";
  for (int i = 0; i < relayCount; i++)
  {
    str += ((String) timerRemaining[i]) + ";";
  }

  return str;
}

// Constructs a string of runtimes.
String runtimeString() {
  String str = "";
  for (int i = 0; i < relayCount; i++)
  {
    str += ((String) runtimes[i]) + ";";
  }

  return str;
}

String getTemperature() {
  // TODO
  return String(random(0, 35));
}

void configModeCallback(WiFiManager * myWiFiManager) {
  // might do things when wifi config poral is opened
}

void setup(void) {
  Serial.begin(115200);
  EEPROM.begin(EEPROM_SIZE); // required size

   // configure IO
  for (int i = 0; i < relayCount; i++)
  {
    // relays
    pinMode(relays[i], OUTPUT);
    setRelay(i, 0);

    // buttons
    pinMode(buttons[i], INPUT);
  }
  
  // init timer
  noInterrupts();
  timer0_isr_init();
  timer0_attachInterrupt(timerHandler);
  timer0_write(ESP.getCycleCount() + interval_us * 80000); // 160 when running at 160mhz
  interrupts();

  // init wifi configurator
  WiFiManager wifiManager;
  wifiManager.setConnectTimeout(5);
  wifiManager.setAPCallback(configModeCallback);
  wifiManager.setAPStaticIPConfig(IPAddress(192, 168, 0, 100), IPAddress(192, 168, 0, 100), IPAddress(255, 255, 255, 0));
  wifiManager.setTimeout(180);
  wifiManager.setDebugOutput(false);
  wifiManager.autoConnect("Garden");

  // init webserver
  server.on("/", handleRequests);
  server.onNotFound(handleNotFound);
  const char * headerkeys[] = {
    "User-Agent",
    "Cookie"
  };
  size_t headerkeyssize = sizeof(headerkeys) / sizeof(char * );
  server.collectHeaders(headerkeys, headerkeyssize);

  // start server
  server.begin();
}

void loop(void) {
  // handle requests
  server.handleClient();

  // Handle button press.
  for (int i = 0; buttonsEnabled && (i < relayCount); i++)
  {
    // TODO: rebouncing
    if (digitalRead(buttons[i]) == onState) {
      digitalWrite(relays[i], !digitalRead(relays[i]));
    }
  }
}
