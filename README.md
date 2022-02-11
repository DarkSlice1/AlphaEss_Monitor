Currently Re-doing the Readme


# Overview
   
Application for home monitoring of Energer usage / Generation and Heating Use.   
Energy generation is tracked via the AlphaESS API (Solor Panels / Inverter / Battery).   
Engergy usage is tracked via TAPO P110 smart sockets - the API's have been reverse Engineered to get this data.   
Heating is tacked via the Ember Smart Heating system - tracks Temperature / Boost times / how long its been bruning oil      
EV Car Changing is tracked via the MYenergi API
   
All metrics are then published to DataDog to mapping and data collecting.  
I have this running on a Rasberry Pi 2W without issue

## _Getting Setup_
Setup an account on [DataDog](https://www.datadoghq.com/)
-> Create a new dashboard
-> Pull down the Repo and run via SBT (simple Build tool)
-> Set the below enviromental varaibles
   
- ALPHA_USERNAME= The Registered Usename on your Alpha Ess System     
- ALPHA_PASSWORD= The Registered password on your Alpha Ess System      
- ALPHA_SYS_SN= The Serial number of your Alpha Ess System ( can be seen on the top of the Alpha App)     
- EMBER_USERNAME= The Registered Usename on your Ember Heating System.   
- EMBER_PASSWORD= The Registered password on your Ember Heating System.   
- TAPO_USERNAME= The Registered Usename/Email Address on your Tapo TP-Link Account
- TAPO_PASSWORD= The Registered Password Address on your Tapo TP-Link Account     
- TAPO_ADDRESSES= Common Delimied list of IP address of you Tapo devices EG. "192.168.1.10,192.168.1.11";      
- FORCAST_LAT= See http://doc.forecast.solar/doku.php?id=api:estimate.   
- FORCAST_LON= See http://doc.forecast.solar/doku.php?id=api:estimate.    
- FORCAST_DEC= See http://doc.forecast.solar/doku.php?id=api:estimate.   
- FORCAST_AZ= See http://doc.forecast.solar/doku.php?id=api:estimate.   
- FORCAST_KWH= See http://doc.forecast.solar/doku.php?id=api:estimate.   
- KAMON_DATADOG_API_KEY= The API key for your DataDog account upon which to pushlish all metrics to 

Then run via

```sh
sbt run
```



## _Generated Alpha ESS Mestrics_
All there metrics are gathered by pinging the Alpha API every 10 seconds, the totals metrics are counter values and are only as accurate the data received. 

### _Solar Metrics_


Alpha's API will seperate each array of solar panels into Solar Strings. These 4 Gauge Metrics will show the generated Solar energy in watts.
if you have 2 Solar Stirngs, Each holding an array of panels of different wattage, you could check the health of the string by compare the % output of one Solar String against another
(this metrics is multiplied by 10, to avoid issues with decimal points, please divide by 10 on DataDog) 
```sh
alpha.ess.ppv1
alpha.ess.ppv2
alpha.ess.ppv3
alpha.ess.ppv4
```

To get the total solar Generated you can you this metric
```sh
alpha.ess.totalSolarGeneration
```
Below is anexample of how DataDog could display these values
![SolarExample](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/SolarExample.png)

### _Battery Metrics_
Currently Alpha has a warranty on the SOH (State of Health) of the battery. This warranty states that the battery should hold greater than 80% charge after 10 years. Unfortunatly Alpha has no way of tracking this information. (i asked)
The below matrics will help

```sh
alpha.ess.soc (State of Charge of the Battery)
alpha.ess.pbatChargeCounter (how much total watts have we put into the battery)
alpha.ess.pbatDischargeCounter (how much total watts have we taken from the battery)
alpha.ess.pbatChargeGauge (how much watts are charging the battery with now)
alpha.ess.pbatDischargeGauge (how much watts are taking fron the battery with now)
```
![BatteryExample](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/BatteryExample.png)

   
KNOWN ISSUES     
Alpha published all watage usage in a Double, DataDog can only accept a Long - All metrics for Alpha are * by 10     
   
   
Usages       
-> Can be used to calculate State of Health in the Solar inverter Battery    
-> To see how wattage the invertor is using - how much actaul wattage are you getting from solar    
-> The breakdown of hosehold usage by what device     
-> The option to remotly turns a device in the house on instead of selling energy back the grid     



