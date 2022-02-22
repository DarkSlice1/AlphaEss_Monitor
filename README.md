

##### Table of Contents  
- [Overview](#overview)   
- [Getting Setup](#getting-setup)   
- [Generated Alpha ESS Metrics](#generated-alpha-ess-metrics)   
  - [Solar Metrics](#solar-metrics)   
  - [Battery Metrics](#battery-metrics)   
  - [Energy Cost Tracking](#energy-cost-tracking)   
- [Tapo P110 Energy Tracking](#tapo-p110-energy-tracking)
- [Ember Heat Tracking](#ember-heat-tracking)
- [Zappi Tracking](#zappi-tracking)
- [Forecasting](#forecasting)

# Overview
   
Application for home monitoring of Energy usage / Generation and Heating Use.   
Energy generation is tracked via the AlphaESS API (Solor Panels / Inverter / Battery).   
Engergy usage is tracked via TAPO P110 smart sockets - the API's have been reverse Engineered to get this data.   
Heating is tacked via the Ember Smart Heating system - tracks Temperature / Boost times / how long its been bruning oil      
EV Car Changing is tracked via the MyEnergi API
   
All metrics are then published to DataDog to mapping and data collecting.  
I have this running on a Rasberry Pi 2W without issue



## _Getting Setup_
Setup an account on [DataDog](https://www.datadoghq.com/)   
-> Create a new dashboard   
-> (Optional)Pull down the Repo and run via SBT (simple Build tool)   
-> Set the below enviromental varaibles based on what metrics you wish to gather (Each section has its own variables to select from)  


pull down the assembly jar file form the releases or compile via SBT   
example (linux) script could be

```sh
export ALPHA_ENABLE=true          
export ALPHA_USERNAME=username      
export ALPHA_PASSWORD=passowrd    
export ALPHA_SYS_SN=serial_number  

export KAMON_DATADOG_API_KEY=MyKey

java -jar alphaess_monitor-assembly-1.0.jar 

```




## _Generated Alpha ESS Metrics_  
[AlpahEss](https://alphaess.com/)
```diff
- Please note All metric's are miltiplied by 10 as the kamon library can't push doubles to DataDog, 
- Please divide by 10 on the DataDog side.   
- Example : A grid pull of 5000 watts is actaully 500.0 watts
```

Be sure to set the enviromental variables if you wish to use these metrics   
```sh
export ALPHA_ENABLE=true            # true / false value - do you want to gather alpha metrics
export ALPHA_USERNAME=username      # The Registered Usename on your Alpha Ess System     
export ALPHA_PASSWORD=passowrd      # The Registered password on your Alpha Ess System      
export ALPHA_SYS_SN=serial_number   # The Serial number of your Alpha Ess System ( can be seen on the top of the Alpha App)    
```

All there metrics are gathered by pinging the Alpha API every 10 seconds, the totals metrics are counter values and are only as accurate the data received. 


### _Solar Metrics_
Alpha's API will seperate each array of solar panels into Solar Strings. These 4 Gauge Metrics will show the generated Solar energy in watts.
if you have 2 Solar Stirngs, Each holding an array of panels of different wattage, you could check the health of the string by compare the % output of one Solar String against another
(this metrics is multiplied by 10, to avoid issues with decimal points, please divide by 10 on DataDog) 
```sh
alpha.ess.ppv1                  (Gauge : How much is generated for String 1 right now)
alpha.ess.ppv2                  (Gauge : How much is generated for String 2 right now)
alpha.ess.ppv3                  (Gauge : How much is generated for String 3 right now)
alpha.ess.ppv4                  (Gauge : How much is generated for String 4 right now)
```

To get the total solar Generated you can you this metric
```sh
alpha.ess.totalSolarGeneration  (Counter : Total Solar generator over all Solar Strings)
```
Below is anexample of how DataDog could display these values
[![SolarExample](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/SolarExample.png)]

### _Battery Metrics_
Currently Alpha has a warranty on the SOH (State of Health) of the battery. This warranty states that the battery should hold greater than 80% charge after 10 years. Unfortunatly Alpha has no way of tracking this information. (i asked)
The below matrics will help

```sh
alpha.ess.soc                       (Gauge : State of Charge of the Battery)
alpha.ess.pbatChargeCounter         (Counter : how much total watts have we put into the battery)
alpha.ess.pbatDischargeCounter      (Counter : how much total watts have we taken from the battery)
alpha.ess.pbatChargeGauge           (Gauge : how much watts are charging the battery with now)
alpha.ess.pbatDischargeGauge        (Gauge : how much watts are taking fron the battery with now)
```
[![BatteryExample](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/BatteryExample.png)]

for checking the SOH of the battery we need to first fully charge the battery.
We can then check the 90% of SOC added. (not a good idea to completly drain the battery)
So lets assume we start at 10% and charge up to 100%. We can use the _alpha.ess.pbatChargeCounter_ to check hw much charge we've placed into the battery and compary with the company state battery capactiy. oddly enough my system is 3 x 5.7 kw and should have a usable capacity of 5.1kw x 3 = 15.3. However i am getting 10% more 16.8kw.
The Datadog JSON Monitor is written like this 
```sh
{
    "viz": "query_value",
    "requests": [
        {
            "formulas": [
                {
                    "formula": "((((query1 / 10) / 60 / 6) / (15300 * 0.9)) * 100) - ((((query2 / 10) / 60 / 6) / (15300 * 0.9)) * 100)"
                }
            ],
            "response_format": "scalar",
            "queries": [
                {
                    "query": "sum:alpha.ess.pbatChargeCounter{$Alpha_SystemID}.as_count()",
                    "data_source": "metrics",
                    "name": "query1",
                    "aggregator": "sum"
                },
                {
                    "query": "sum:alpha.ess.pbatDischargeCounter{$Alpha_SystemID}.as_count()",
                    "data_source": "metrics",
                    "name": "query2",
                    "aggregator": "sum"
                }
            ]
        }
    ],
    "autoscale": true,
    "custom_unit": "%",
    "precision": 2
}
```
and will yeild this result
![BatterySOHExample](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/BatterySOHExample.png)]



### _Energy Cost Tracking_
The following metrics are available to you for tracking house hold energy from the grid and pushing to the grid

```sh
alpha.ess.gridPull_l1               (Gauge : real time watts pulled from the Grid)
alpha.ess.gridPush_l1               (Gauge : real time watts pushed to the Grid)
alpha.ess.totalGridConsumption      (Counter : Total watts pull from the Grid)
alpha.ess.totalGridPush             (Counter : Total watts pushed to the Grid)
alpha.ess.houseLoad                 (Gauge : Real time watts load on the house)
```

if you wanted to work out how much your energy companany should be charging you, you have the grid pull counter metrics in 1 hour slot
Very usefull if you want to compare energy providers to see what energy you use when during the day and compare provider plans

```sh
alpha.ess.gridPull_CostHour_00_01
alpha.ess.gridPull_CostHour_01_02
alpha.ess.gridPull_CostHour_03_04
alpha.ess.gridPull_CostHour_04_05
alpha.ess.gridPull_CostHour_06_07
alpha.ess.gridPull_CostHour_08_09
alpha.ess.gridPull_CostHour_09_10
alpha.ess.gridPull_CostHour_10_11
alpha.ess.gridPull_CostHour_11_12
alpha.ess.gridPull_CostHour_12_13
alpha.ess.gridPull_CostHour_13_14
alpha.ess.gridPull_CostHour_15_16
alpha.ess.gridPull_CostHour_16_17
alpha.ess.gridPull_CostHour_17_18
alpha.ess.gridPull_CostHour_19_20
alpha.ess.gridPull_CostHour_20_21
alpha.ess.gridPull_CostHour_21_22
alpha.ess.gridPull_CostHour_22_23
alpha.ess.gridPull_CostHour_23_00
```
So what does this data look like?   
![GridPullAndCosting](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/GridPullAndCosting.png)



 # _Tapo P110 Energy Tracking_
[Tapo P110 Smart Socket](https://www.tapo.com/uk/product/smart-plug/tapo-p110/)
```diff
- Please note All metric's are miltiplied by 10 as the kamon library can't push doubles to DataDog, 
- Please divide by 10 on the DataDog side.   
- Example : A grid pull of 5000 watts is actaully 500.0 watts
``` 
Be sure to set the enviromental variables if you wish to use these metrics   
```sh
export TAPO_ENABLED=true                          # true / false value - do you want to gather tapo metrics
export TAPO_USERNAME=username                     # The Registered Usename/Email Address on your Tapo TP-Link Account
export TAPO_PASSWORD=password                     # The Registered Password Address on your Tapo TP-Link Account    
export TAPO_ADDRESSES=192.168.1.10,192.168.1.11   # Common Delimied list of IP address of you Tapo devices
```

The idea here to tracking large consumers of energy that are plugged into a wall socket. We cant track house hold items that are wired directly to the fuse box (Celiing lights, Ovens, Hot Water Tanks etc)   
    
The application will generate the following metrics, once you add a comma delimited list of local ip address ( I highly recommend that you give a static IP to these devices via your routers Static DHCP mapping) The application will go though the list every 10 seconds and grab the current wattage draw
 
 ```sh
 tapo.EnergyUsageCounter    (Counter: The total wattage over a period of time)
 tapo.tapoEnergyUsageGauge  (Gauge : The Watage right now)
 ```
 
 So what can yo do with this data? you can map what is being drawn from your house at any time, overlapping with the Alpha Ess House load metric you can see what applicance is drawing what wattage and see as a percentage of the total house draw.   
You can also see what applicance is cost your largest cost in your energy bill

![TapoEnergyPerDevice](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/TapoEnergyPerDevice.png)
![TapoHouseLoad](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/TapoHouseLoad.png)
![TapoPercentageUsage](https://github.com/DarkSlice1/AlphaEss_Monitor/blob/master/readmeImages/TapoPercentageUsage.png)


# _Ember Heat Tracking_
 todo   
[Ember Heating](https://emberapp.ephcontrols.com/)

Be sure to set the enviromental variables if you wish to use these metrics   
```sh
export EMBER_ENABLED=true            # true / false value - do you want to gather ember metrics
export EMBER_USERNAME=username       # The Registered Usename on your Ember Heating System.  
export EMBER_PASSWORD=password       # The Registered password on your Ember Heating System.
```
 
 
# _Zappi Tracking_
 todo  
[My Energi - Zappi](https://myenergi.com/product/zappi/)   
Be sure to set the enviromental variables if you wish to use these metrics   
```sh
export MYENERGI_ENABLED=true            # true / false value - do you want to gather myenergi metrics
export MYENERGI_USERNAME=username       # The Hub ID on your MyEnergi System.   
export MYENERGI_PASSWORD=password       # The Registered password on your MyEnergi System.   
```


# _Forecasting_
 todo  
    
Be sure to set the enviromental variables if you wish to use these metrics   
```sh
export FORCAST_ENABLED=true           # true / false value - do you want to gather forecasting metrics
export FORCAST_LAT=0                  # See http://doc.forecast.solar/doku.php?id=api:estimate.  
export FORCAST_LON=-0                 # See http://doc.forecast.solar/doku.php?id=api:estimate.  
export FORCAST_DEC=0                  # See http://doc.forecast.solar/doku.php?id=api:estimate.  
export FORCAST_AZ=0                   # See http://doc.forecast.solar/doku.php?id=api:estimate.  
export FORCAST_KWH=0                  # See http://doc.forecast.solar/doku.php?id=api:estimate.  
```
 
