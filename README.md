
OVERVIEW   
Application for home monitoring of Energer usage / Generation and Heating Use.   
Energy generation is tracked via the AlphaESS API (Solor Panels / Inverter / Battery).   
Engergy usage is tracked via TAPO P110 smart sockets - the API's have been reverse Engineered to get this data.   
Heating is tacked via the Ember Smart Heating system - tracks Temperature / Boost times / how long its been bruning oil      
   
All metrics are then published to DataDog to mapping and data collecting.  
   
I have this running on a Rasberry Pi 2W.   


EXPERIMENTAL.  
I'm working with a forcasting model to predict how accurate a solar forcast can be such that i have an idea on Solar Generation for the day.    


NOTES.   
All of the above are pinged every 10 seconds.    
The API from each of these application gives data in a Double.      
Given that our intergation with DataDog can only handle Intagers, each value is multiplied by 10      


COMING SOON.   
Intergration with Siwtch-Bot Temperature.   
Ability to only use some or all part of the intergration in this application.    
Will yodate Readme with Metrics producted and a description of each.    
Will add screen shots of what can be producted on DataDog.    
   
   
ENVIROMENTAL VARIABLES.    
ALPHA_USERNAME= The Registered Usename on your Alpha Ess System     
ALPHA_PASSWORD= The Registered password on your Alpha Ess System      
ALPHA_SYS_SN= The Serial number of your Alpha Ess System ( can be seen on the top of the Alpha App)     

EMBER_USERNAME= The Registered Usename on your Ember Heating System.   
EMBER_PASSWORD= The Registered password on your Ember Heating System.   

TAPO_USERNAME= The Registered Usename/Email Address on your Tapo TP-Link Account      
TAPO_PASSWORD= The Registered Password Address on your Tapo TP-Link Account     
TAPO_ADDRESSES= Common Delimied list of IP address of you Tapo devices EG. "192.168.1.10,192.168.1.11";      

FORCAST_LAT= See http://doc.forecast.solar/doku.php?id=api:estimate.   
FORCAST_LON= See http://doc.forecast.solar/doku.php?id=api:estimate.    
FORCAST_DEC= See http://doc.forecast.solar/doku.php?id=api:estimate.   
FORCAST_AZ= See http://doc.forecast.solar/doku.php?id=api:estimate.   
FORCAST_KWH= See http://doc.forecast.solar/doku.php?id=api:estimate.   

KAMON_DATADOG_API_KEY= The API key for your DataDog account upon which to pushlish all metrics to     

   
KNOWN ISSUES     
Alpha published all watage usage in a Double, DataDog can only accept a Long - All metrics for Alpha are * by 10     
   
   
Usages       
-> Can be used to calculate State of Health in the Solar inverter Battery    
-> To see how wattage the invertor is using - how much actaul wattage are you getting from solar    
-> The breakdown of hosehold usage by what device     
-> The option to remotly turns a device in the house on instead of selling energy back the grid     


[![SolarExample](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/SolarExample.png)]
