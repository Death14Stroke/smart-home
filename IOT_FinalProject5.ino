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
long startt;
long stopp;

unsigned long startTime[7];
unsigned long stopTime[7]; 
String units[] = {"-1","units1","units2","units3","units4","units5","units6"};
String sts[] = {"-1","d1","d2","d3","d4","d5","d6"};
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

    Serial.println("checking for stream.....");
    
    FirebaseStream.stream(ss4);
    
    
    Serial.print("ss4 = ");
    
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


    delay(2000);
    for(int i=0;i<6;i++)
    {
      String s1 = st[i];
      String s2 = "/";
      String s3 = "status";
      //String s4 = "dname";
      String sFin = s1 + s2 + s3;
     // String sFin2 = s1 + s2 + s4;
      Firebase.setBool(sFin,true);
  
    }

    
}



void loop() {



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

      delay(1000);


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
             
             Serial.println("Temerature and Pressure written to the databse  ....");
             delay(1000);


          /*
           * Looking for  event 
           * 
           */


            delay(1000);

         
            if (FirebaseStream.available())
            {
              


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
              //delay(1000);

              String timme0 = "/";
              String timme1 = user;
              String timme2 = "/";
              String timme3 = "stsp/";
              String timme4 = sts[fin];
              String timme5 = "/start";
              String timmeFin = timme0 + timme1 + timme2 + timme3 + timme4 + timme5;
              Serial.println("Printing the time to take start time ----  ");
              Serial.println(timmeFin);
              String getStartTime = Firebase.getString(timmeFin);
              Serial.println("Printing start time--- -  ");
              Serial.println(getStartTime);
              
              char ch[11];// = getStartTime.c_str();
              getStartTime.toCharArray(ch , 11 );
              Serial.println("Printing character array");
              Serial.println(ch);
              startt = atol(ch);
              
              Serial.print("startt --- ");
              Serial.println(startt);
              
            }

 

  
      else
      {
        digitalWrite(pin[fin],LOW);
        
         Serial.println("Noting down the time when the device is turned off...");

         
             long epoch = startt;
             Serial.print("Epoch value when starting   --- ");
             Serial.println(epoch);
             
             long l1 = ((epoch  % 86400L) / 3600);
             
              l1 = l1 * 3600;
              epoch = epoch - l1;
              
              
              
            long l2 = (epoch  % 3600) / 60;
            l2 = l2*60;
            epoch = epoch - l2;


            long l3 = (epoch%60);
            epoch = epoch - l3;

            Serial.print("epoch--   ");
            Serial.println(epoch);
        

              const int n = snprintf(NULL, 0, "%ld", epoch);
              assert(n > 0);
              char buf[n+1];
              int c = snprintf(buf, n+1, "%ld", epoch);
              assert(buf[n] == '\0');
              assert(c == n);

              
              String timme0 = "/";
              String timme1 = user;
              String timme2 = "/";
              String timme3 = "stsp/";
              String timme4 = sts[fin];
              String timme5 = "/stop";
              String timmeFin = timme0 + timme1 + timme2 + timme3 + timme4 + timme5;
              String getStopTime = Firebase.getString(timmeFin);
              Serial.println("Printing the stop time --- ");
              Serial.println(getStopTime);
              char ch[11];// = getStopTime.c_str();
              getStopTime.toCharArray(ch,11);
                
              stopp = atol(ch);
            
            Serial.print("stopp ---- ");
            Serial.println(stopp);

          
            
             String logs0 = "/";
             String logs1 = user;
             String logs2 = "/logs";
             String logs3 = "/";
             //String logs4 = 
             String logs4(buf);
             String logsFin = logs0 + logs1 + logs2 + logs3 + logs4; 

             Serial.println("Printing logs fin");
            
             Serial.println(logsFin);
             String units0 = "/";
             String units1 = units[fin];
             String unitsFin = logsFin + units0 + units1;

             Serial.println("Printing unitsFin  ");
             Serial.println(unitsFin);
             
             float ft = Firebase.getFloat(unitsFin);
             Serial.print("Reading done from start = ");
             Serial.println(ft);
             
            delay(1000);
            Serial.print("Existing units in the port   ");
            Serial.println(Firebase.getFloat(unitsFin)); 
            //converting the difference into hours

            float diff2 = (float)(stopp - startt);
            Serial.print("difference --- ");
            Serial.println(diff2);
            
            float hours = diff2/3600.0;
         
            
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
