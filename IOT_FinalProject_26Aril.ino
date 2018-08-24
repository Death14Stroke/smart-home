/*
 * 
 * Revisiting Version 2
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


String passwordFin;
int noAttempt = 0;

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




Adafruit_BMP085 bmp; 


// Set these to run example.
#define FIREBASE_HOST "home-automation-it478.firebaseio.com"
#define FIREBASE_AUTH "GY9fkH3WiYkx1hHPYHTushZD7hmyZyCF7FOwER7L"

String st[] = {"/devices/device1","/devices/device2","/devices/device3","/devices/device4","/devices/device5" ,"/devices/device6" ,"/devices/device7" ,"/devices/device8"};   



FirebaseArduino FirebaseStream;
FirebaseArduino FirebaseStream2;


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
        
      
        oled.print("WiFi Connected to ");
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
          oled.clear(PAGE);
          oled.setFontType(0);
          oled.setCursor(1,3);
          
          
          oled.print("WELCOME TO Your"); 
          oled.display(); 
          oled.setCursor(1,12); 
          oled.print("SMART HOME");
          oled.display(); 
          
          checkingDone = true;
        }
              
    }


    FirebaseStream2.begin(FIREBASE_HOST, FIREBASE_AUTH);
    
    String ss0 = "/";
    String ss1 = user;
    String ss2 = "/";
    String ss3 = "devices/";
    String ss4 = ss0 + ss1 + ss2 + ss3;

    Serial.println("checking for stream.....");

    String ss5 = ss0 + ss1 + ss2;
    FirebaseStream2.stream(ss5);
    
    if (Firebase.success()) {
          Serial.println("streaming 2 ok");
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
     if(Firebase.getBool(sFin)==true)
     {
        digitalWrite(pin[i+1],HIGH);
     }
     // Firebase.setBool(sFin,true);
      delay(500);
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
     
            if(FirebaseStream2.available())
            {

              Serial.println("Some activity detected in temperature ");
              
                FirebaseObject event = FirebaseStream2.readEvent();
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
        
             char ch = ptr[15];
                int fin = ch - '0';
            String ptr2 = "status";
            String ptr22 = "tempreq";
            String ptr222 = "pressreq";
            
            if(strstr(ptr.c_str(),ptr22.c_str()))
            {
              
              if(b==1)
              {
                Serial.println("temerature request detected");
                setTemperature();
                delay(500);

                String bb1 = "/";
                String bb2 = user;
                String bb3 = "/tempreq";
                String bb4 = bb1 + bb2 + bb3;
                Serial.println(bb4);
                Firebase.setBool(bb4 , false);
              }
            }

           if(strstr(ptr.c_str(),ptr222.c_str()))
           {
            if(b==1)
            {
              Serial.println("pressure request detected");
                setPressure();
                delay(500);

                String bb1 = "/";
                String bb2 = user;
                String bb3 = "/pressreq";
                String bb4 = bb1 + bb2 + bb3;
                Serial.println(bb4);
                Firebase.setBool(bb4 , false);
            }
           }
            
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
            int rat = Firebase.getFloat(timeFin);
           // int rat = Firebase.getInt(timeFin);
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

void setTemperature()
{

               String temp1 = "/";
               String temp2 = user;
               String temp3 = "/temperature";
               //String pressure1 = "/Pressure";
    
               String tempFin = temp1 + temp2 + temp3;
               //String pressureFin = temp1 + temp2 + pressure1;

              Serial.println(tempFin);
              //Serial.println(pressureFin);
              Serial.println("Writing temperature and Pressure to the database  .... ");
              
              float f = bmp.readTemperature();
              Serial.println(f); 
              Firebase.setFloat(tempFin,f);
              //Firebase.setFloat(pressureFin,f2);
              oled.clear(PAGE); 
              oled.setFontType(0);
              oled.setCursor(1, 3);
              oled.print("Temp =  ");
               
               oled.setCursor(1,12);
              oled.print(f);
              oled.print(" *C ");
              oled.display();  
              
             Serial.println("Temerature written to the databse  ....");
             delay(1000);
  
}

void setPressure()
{
              String temp1 = "/";
               String temp2 = user;
               //String temp3 = "/temperature";
               String pressure1 = "/Pressure";
    
               //String tempFin = temp1 + temp2 + temp3;
               String pressureFin = temp1 + temp2 + pressure1;

              //Serial.println(tempFin);
              Serial.println(pressureFin);
              Serial.println("Writing Pressure to the database  .... ");
             // Firebase.setFloat(tempFin,f);
             
             float f2 = bmp.readPressure();
             Serial.println(f2); 
               
              
              Firebase.setFloat(pressureFin,f2);
              oled.clear(PAGE); 
              oled.setFontType(0);
              oled.setCursor(1, 3);
              oled.print("Pressure = ");
              
              oled.setCursor(1, 12);
              oled.print(f2);
              oled.print(" Pa");
              oled.display();
               Serial.println("Temerature and Pressure written to the databse  ....");
               delay(1000);
               
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


