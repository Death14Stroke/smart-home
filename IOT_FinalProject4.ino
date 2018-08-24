/*
 * 
 * Revisiting Version
 */



#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#include <Adafruit_BMP085.h> 
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include<Wire.h>
#include<FirebaseObject.h>
#include<assert.h>

#include <SFE_MicroOLED.h>  // Include the SFE_MicroOLED library
#define PIN_RESET 255  //
#define DC_JUMPER 0  // I2C Addres: 0 - 0x3C, 1 - 0x3D
 
MicroOLED oled(PIN_RESET, DC_JUMPER); // Example I2C declaration



unsigned int localPort = 2390;      // local port to listen for UDP packets
String user;


unsigned long startTime[7];
unsigned long stopTime[7]; 
String units[] = {"-1","units1","units2","units3","units4","units5","units6"};
int n = 0;
int totalNotDetected = 0;
int totalDetected = 0;
int a;



IPAddress timeServerIP; // time.nist.gov NTP server address
IPAddress timeServerIP2;// 


const char* ntpServerName =   "0.cn.pool.ntp.org";
const char* ntpServerName2 = "2.asia.pool.ntp.org";



//const char* ntpServerName = "time.nist.gov";
//const char* ntpServerName = "2.asia.pool.ntp.org";

const int NTP_PACKET_SIZE = 48; // NTP time stamp is in the first 48 bytes of the message

byte packetBuffer[ NTP_PACKET_SIZE]; //buffer to hold incoming and outgoing packets
byte packetBuffer2[ NTP_PACKET_SIZE];
// A UDP instance to let us send and receive packets over UDP
WiFiUDP udp;






Adafruit_BMP085 bmp; 


// Set these to run example.
#define FIREBASE_HOST "home-automation-it478.firebaseio.com"
#define FIREBASE_AUTH "GY9fkH3WiYkx1hHPYHTushZD7hmyZyCF7FOwER7L"

String st[] = {"/devices/device1","/devices/device2","/devices/device3","/devices/device4","/devices/device5" ,"/devices/device6" ,"/devices/device7" ,"/devices/device8"};   



FirebaseArduino FirebaseStream;



int pin[] = {-1,D0,D3,D4,D5,D6,D8};




void setup() {
  // put your setup code here, to run once:

  
  Serial.begin(9600);
  
  oled.clear(PAGE);
  oled.begin();
  oled.clear(ALL);  // Clear the display's memory (gets rid of artifacts)
  oled.display();  
  char ssid[50];
  char password[50];
  password[0] = '\0';
  bool moveon = false;

   int i;
  String input;
  String first;
  String answer;
  char ans[50];
  ans[0] = '\0';

  while(!moveon)
  {
    int numberOfNetworks = WiFi.scanNetworks();
    delay(1000);
    input = "";
    first = "";
    answer = "";
    ans[0] = '\0';
    
     for(i =0; i<numberOfNetworks; i++)
    {
      Serial.print(i);
      Serial.print(" ---  ");
      Serial.print("Network name: ");
      Serial.println(WiFi.SSID(i));
      Serial.print("Signal strength: ");
      Serial.println(WiFi.RSSI(i));
      Serial.println("-----------------------");
      
  }

   Serial.println("Enter the number of the network you want to connect to");
   int waitInput = 0;

  

   while(input.length()==0)
   {
   if(Serial.available()>0)
    {
    input = Serial.readString();
    Serial.print("You entered   ");
    Serial.println(input);

    delay(1000);
    
    Serial.print("Enter y if you Would you like to connect to   ");
    Serial.println(WiFi.SSID(input.toInt()));
    Serial.println("Else enter n");
    delay(1000);
    while(ans[0]=='\0')
    {
      if(Serial.available()>0)
      {
        answer = Serial.readString();
        answer.toCharArray(ans,50);
        if(ans[0]=='y' || ans[0]=='Y')
        {
          moveon = true;
        }
      }
    }
   
    
  }

  else
  {
    Serial.println("Waiting for input");
    
    delay(2000);
    waitInput++;

    if(waitInput==6)
    {
      break;
    }

    
  } 

  }
  } 


  WiFi.SSID(input.toInt()).toCharArray(ssid,50);

  Serial.print("Enter the password for ");
  Serial.print(WiFi.SSID(input.toInt()));



  while(password[0] == '\0')
  {
    if(Serial.available()>0)
    {
      first = Serial.readString();
      first.toCharArray(password,50);
      Serial.print("You entered   ");
      Serial.println(password);

    
      WiFi.begin(ssid , password);
      Serial.print("connecting");
      int t = 0;
      while (WiFi.status() != WL_CONNECTED) {
      Serial.print(".");
      delay(500);
      t++;
      if(t==30)
      {
        Serial.println("Wrong password");
        password[0] =  '\0';
        break;
      }
    }

      if(t<30)
      {
        Serial.println();
        Serial.println("connected: ");
        //delay(2000);    
  
        oled.clear(PAGE);
        oled.setFontType(0);
        oled.setCursor(1,3);
        oled.print("Connected to ");
        oled.setCursor(1,12);
        oled.print(WiFi.SSID(input.toInt()));
        oled.display(); 
          
      Serial.println(WiFi.localIP());
      }
      
      
    }
    else
    {
    Serial.println("waiting for password");
    delay(1000);
    }
  }


  bool checkingDone = false;
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  delay(1000);
  String pass;


    while(!checkingDone)
    {
        Serial.println("---------------Enter USER NAME-------------");
      
        while(user.length()==0)
        {
          if(Serial.available()>0)
          {
            user = Serial.readString();
            checkingDone = true;
          }
        }


        Serial.println("---------------Enter  PASSWORD-------------");

        while(pass.length()==0)
        {
          if(Serial.available()>0)
          {
            pass = Serial.readString();
          }
        }

        Serial.println("Authenticating");

        FirebaseObject userGet = Firebase.get(user);
        String userGet1 = "/";
        String userGet2 = user;
        String userGet3 = "/password";
        String userGetFin = userGet1 + userGet2 + userGet3;
        String userPassword = Firebase.getString(userGetFin);
        
        delay(2000);

        if((pass == NULL)  || (!(pass.equals(userPassword))))
        {
            Serial.println("Wrong USERNAME or PASSWORD");
            user = "";
            pass = "";
        }
    
        else
        {
          Serial.println("------------WELCOME TO YOUR SMART HOME-------------");
          checkingDone = true;
        }
              
    }

    FirebaseStream.begin(FIREBASE_HOST, FIREBASE_AUTH);
    String ss0 = "/";
    String ss1 = user;
    String ss2 = "/";
    String ss3 = "devices/";
    String ss4 = ss0 + ss1 + ss2 + ss3;
    FirebaseStream.stream(ss4);
    Serial.print("ss4 = ");
    Serial.println(ss4);

    if (Firebase.success()) {
          Serial.println("streaming ok");
     }
    
    
    bmp.begin();
    delay(1000);
    if (!bmp.begin()) 
    {
        Serial.println("Could not find BMP180 or BMP085 sensor at 0x77");
          while (1) {
          Serial.print(".");
          }
    }
    udp.begin(localPort);



  for(int j=0;j<8;j++)
  {

    
    String c1 = "/";
    String c2 = user;
    String c3 = st[j];
    String resultf = c1 + c2 + c3;
  
    st[j] = resultf;
  }

  
    pinMode(D0,OUTPUT);
    pinMode(D3,OUTPUT);
    pinMode(D4,OUTPUT);
    pinMode(D5,OUTPUT);
    pinMode(D6,OUTPUT);
    pinMode(D8,OUTPUT);
  
    digitalWrite(D0,LOW);
    digitalWrite(D3,LOW);
    digitalWrite(D4,LOW);
    digitalWrite(D5,LOW);
    digitalWrite(D6,LOW);
    digitalWrite(D8,LOW);

    
    
}



void loop() {


for(int i=0;i<8;i++)
{
  
      

     /*
      * Checking for the PIR SENSOR FOR FIRST TIME
      */



      

        Serial.println("Checking if someone is present in room or not");
        a = getPirValue();
        if(a==0)
        {
          totalNotDetected++;  
      
          
          if(totalNotDetected==10)
          {

           Serial.println("No presence detected");
           
            
           String allOff1 = "/";
           String allOff2 = user;
           String allOff3 = "/alloff";
           String allOffFinal = allOff1 + allOff2 + allOff3;
           String alertBool1 = "/alert";
           String alertBoolFinal = allOff1 + allOff2 + alertBool1;
           
            if(Firebase.getBool(allOffFinal)==false)
                 {
                    Firebase.setBool(alertBoolFinal,true);
                    Serial.println("Sending notification....");
                 }
                  
             if (Firebase.failed()) {
            Serial.print("setting /number failed:");
            Serial.println(Firebase.error());  
            return;
            }   
    
           totalNotDetected = 0;
          }
      
        } 


        else
        {
          totalDetected++;
          Serial.println("Presence Detected");
          totalNotDetected = 0;
        }



        






        /*
         * Displaying the Temperature and Pressure on the OLED
         */

          delay(1000);
          
          oled.clear(PAGE); 
          oled.setFontType(0);
          oled.setCursor(1, 3);
          oled.print("Pressure = ");
          oled.setCursor(1, 12);
        
          /*
           * Writing pressure to the serial monitor
           */

          Serial.print("-----*****Pressure of the room*****----- = ");
          float f2 = bmp.readPressure();
          Serial.println(f2); 
          
          oled.print(f2);
          oled.print(" Pa");
         delay(1000);
          oled.setCursor(1, 21);
          oled.print("Temp =");
          oled.setCursor(1, 30);

          /*
           * 
           * Writing temperature to the serial monitor
           */

        
          Serial.print("-----*****Temperature of the room*****----- = ");
          float f = bmp.readTemperature();
          Serial.println(f); 

          
          oled.print(f);
          oled.print(" *C ");
          oled.display();  
          

          /*
           * Writing temperature and Pressure to the Database;
           */

           String temp1 = "/";
           String temp2 = user;
           String temp3 = "/temperature";
           String pressure1 = "/Pressure";

           String tempFin = temp1 + temp2 + temp3;
           String pressureFin = temp1 + temp2 + pressure1;

            Serial.println(tempFin);
            Serial.println(pressureFin);
            Serial.println("Writing temperature and Pressure to the database  .... ");
             Firebase.setFloat(tempFin,f);
             Firebase.setFloat(pressureFin,f2);
             Serial.println();
             Serial.println();
             Serial.println("Temerature and Pressure written to the databse  ....");
             delay(1000);

          
          /*
           * 
           * Start Checking for the Devices
           */






           
          Serial.print("Checking for Device ");
          String ratingFin = st[i] + "/rating";
          String dnameFin = st[i] + "/dname";
          String statusFin = st[i] + "/status";
          String portFin = st[i] + "/port";

        
            if(Firebase.getInt(ratingFin)==-1)
            {
                digitalWrite(pin[1+i],LOW);
                Serial.print("No device connected in relay  ... ");
                Serial.println(i+1);
                delay(1000);
            }
      
          else
          {
            Serial.print("Device is connected in relay  ...");
            Serial.println(i+1);

            Serial.println();
            Serial.println("Following are the details....");


            Serial.print("---***Name***--- = ");
            Serial.println(Firebase.getString(dnameFin));
            delay(500);
            Serial.println();
            
            Serial.print("---***Rating***--- = ");
            Serial.println(Firebase.getInt(ratingFin));
            delay(500);
            Serial.println();
            
            Serial.print("Status = ");    
            bool isport = Firebase.getBool(statusFin);
            Serial.println(isport);
            delay(500);
            
            int port = Firebase.getInt(portFin);

            if(isport==1)
            {
              digitalWrite(pin[port],HIGH);
            }
            else
            {
              digitalWrite(pin[port],LOW);
            }
            
          }


          if (FirebaseStream.failed()) {
            Serial.println("ERROR: streaming error");
            Serial.println(FirebaseStream.error());
          }

          /*
           * Looking for  event 
           * 
           */


            delay(1000);

            
            if (FirebaseStream.available()) {

              Serial.println("***********Some activity detected***********");
            FirebaseObject event = FirebaseStream.readEvent();
            String eventType = event.getString("type");
            Serial.println(eventType);
            Serial.print("event: ");
            Serial.println(event.getString("event"));
            Serial.print("path: ");
            String ptr = event.getString("path");
            Serial.println(ptr);
            Serial.print("data: ");
            bool b = event.getBool("data");
            Serial.println(b);
        
             char ch = ptr[7];
             int fin = ch - '0';
        
            String ptr2 = "status";

            if(strstr(ptr.c_str(),ptr2.c_str()))
            {
            if(b==1)
            {
              digitalWrite(pin[fin],HIGH);
              
              Serial.println("Noting the time as the device is turning on...");
              delay(1000);
              
              WiFi.hostByName(ntpServerName, timeServerIP); 
              
              sendNTPpacket(timeServerIP); // send an NTP packet to a time server
              // wait to see if a reply is available
              delay(1000);
              
              int cb = udp.parsePacket();
        
        
        


        
                 udp.read(packetBuffer, NTP_PACKET_SIZE); // read the packet into the buffer
        
               unsigned long highWord = word(packetBuffer[40], packetBuffer[41]);
              unsigned long lowWord = word(packetBuffer[42], packetBuffer[43]);
               unsigned long secsSince1900 = highWord << 16 | lowWord;
           
              const unsigned long seventyYears = 2208988800UL;
               unsigned long epoch = secsSince1900 - seventyYears;
        
                Serial.print("epoch when the light is put on is ");
                Serial.println(epoch);
              Serial.print("The UTC time when the device is started is ");       // UTC is the time at Greenwich Meridian (GMT)

              Serial.print("Starting epoch  ");
              Serial.println(epoch);
              Serial.print((((epoch  % 86400L) / 3600))); // print the hour (86400 equals secs per day)
              Serial.print(':');
             if ( ((epoch % 3600) / 60) < 10 ) {
              //// In the first 10 minutes of each hour, we'll want a leading '0'
              Serial.print('0');
            }
            Serial.print((epoch  % 3600) / 60); // print the minute (3600 equals secs per minute)
            Serial.print(':');
            if ( (epoch % 60) < 10 ) {
              // In the first 10 seconds of each minute, we'll want a leading '0'
              Serial.print('0');
              }
            Serial.println(epoch % 60); // print the second
              startTime[fin] = epoch;
            }


  
      else
      {
        digitalWrite(pin[fin],LOW);

         Serial.println("Noting down the time when the device is turned off...");
         WiFi.hostByName(ntpServerName2, timeServerIP2); 
        
        sendNTPpacket(timeServerIP2); // send an NTP packet to a time server
        // wait to see if a reply is available
        delay(1000);
        
        int cb = udp.parsePacket();
        
          
            udp.read(packetBuffer, NTP_PACKET_SIZE); // read the packet into the buffer

            unsigned long highWord = word(packetBuffer[40], packetBuffer[41]);
            unsigned long lowWord = word(packetBuffer[42], packetBuffer[43]);
            unsigned long secsSince1900 = highWord << 16 | lowWord;
            const unsigned long seventyYears = 2208988800UL;
            unsigned long epoch = secsSince1900 - seventyYears;
        
            stopTime[fin] = epoch;
            Serial.print("epoch when the light is off is ");
            Serial.println(epoch);
        
            Serial.print("Just to check whether the earlier start[fin] and end[fin] are different ");
            Serial.print("start[fin] ");
            Serial.println(startTime[fin]);
            Serial.print("stop[fin] ");
            Serial.println(stopTime[fin]);
            
            Serial.print("The UTC time when the device is put off is ");  
    
    
            // UTC is the time at Greenwich Meridian (GMT)
            
              Serial.print("Epoch  ");
        
            Serial.println(epoch);
        
            
             Serial.print((((epoch  % 86400L) / 3600))); // print the hour (86400 equals secs per day)
              Serial.print(':');
             if ( ((epoch % 3600) / 60) < 10 ) {
              //// In the first 10 minutes of each hour, we'll want a leading '0'
              Serial.print('0');
             }
              Serial.print((epoch  % 3600) / 60); // print the minute (3600 equals secs per minute)
            Serial.print(':');
            if ( (epoch % 60) < 10 ) {
              // In the first 10 seconds of each minute, we'll want a leading '0'
              Serial.print('0');
            }
            Serial.println(epoch % 60); // print the second
            
             
             unsigned long l1 = ((epoch  % 86400L) / 3600);
             
              l1 = l1 * 3600;
              epoch = epoch - l1;
              
              
              
            unsigned long l2 = (epoch  % 3600) / 60;
            l2 = l2*60;
            epoch = epoch - l2;


            unsigned long l3 = (epoch%60);
            epoch = epoch - l3;
            
           
          // wait ten seconds before asking for the time again
          
          delay(2000);
         
          unsigned long diff = stopTime[fin] - startTime[fin];
              const int n = snprintf(NULL, 0, "%lu", epoch);
              assert(n > 0);
              char buf[n+1];
              int c = snprintf(buf, n+1, "%lu", epoch);
              assert(buf[n] == '\0');
              assert(c == n);

      
              startTime[fin] = 0;
              stopTime[fin] = 0;
            
              
       
    
    
             String logs0 = "/";
             String logs1 = user;
             String logs2 = "/logs";
             String logs3 = "/";
             String logs4(buf);
             String logsFin = logs0 + logs1 + logs2 + logs3 + logs4; 
             Serial.println(logsFin);
             String units0 = "/";
             String units1 = units[fin];
             String unitsFin = logsFin + units0 + units1;
             
             
             float ft = Firebase.getFloat(unitsFin);
             Serial.print("Reading done from start = ");
             Serial.println(ft);
             
            delay(1000);
            Serial.print("Existing units in the port   ");
            Serial.println(Firebase.getFloat(unitsFin)); 
            //converting the difference into hours

            float diff2 = (float)diff;
            float hours = diff2/3600.0;
            //float hours = (float)diff/3600.00;
            //ft = ft + hours;
            
            Serial.print("diff hours  ");
            Serial.println(hours);
          
            String time1 = st[fin-1];
            String time2 = "/rating";
            String timeFin = time1 + time2;
            Serial.print("timeFin = ");
            Serial.println(timeFin);
            
            int rat = Firebase.getInt(timeFin);
            Serial.print("rating = ");
            Serial.println(rat);
            
            float finHours = rat*hours;
            Serial.print("after multiplying = ");
            Serial.println(finHours);
            ft = ft + finHours;
            Serial.println(ft);
           Firebase.setFloat(unitsFin,ft);
  
  
}

    } 

}

}

}


int getPirValue()
{
  //int pirValue = digialRead(D7);
  Serial.println(totalNotDetected);
  if(digitalRead(D7)==LOW)
  {
    Serial.println("----Not Detected----");
    return 0; 
  }

    Serial.println("---Detected---");
  return 1;
}



unsigned long sendNTPpacket(IPAddress& address)
{
  Serial.println("sending NTP packet...");
  // set all bytes in the buffer to 0
  memset(packetBuffer, 0, NTP_PACKET_SIZE);
  // Initialize values needed to form NTP request
  // (see URL above for details on the packets)
  packetBuffer[0] = 0b11100011;   // LI, Version, Mode
  packetBuffer[1] = 0;     // Stratum, or type of clock
  packetBuffer[2] = 6;     // Polling Interval
  packetBuffer[3] = 0xEC;  // Peer Clock Precision
  // 8 bytes of zero for Root Delay & Root Dispersion
  packetBuffer[12]  = 49;
  packetBuffer[13]  = 0x4E;
  packetBuffer[14]  = 49;
  packetBuffer[15]  = 52;

  // all NTP fields have been given values, now
  // you can send a packet requesting a timestamp:
  udp.beginPacket(address, 123); //NTP requests are to port 123
  udp.write(packetBuffer, NTP_PACKET_SIZE);
  udp.endPacket();
}
