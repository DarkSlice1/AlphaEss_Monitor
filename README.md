Currently Re-doing the Readme

##### Table of Contents  
[Overview](#overview)   
- [Getting Setup](#getting-setup)   
- [Generated Alpha ESS Mestrics](#generated-alpha-ess-mestrics)   
  - [Solar Metrics](#solar-metrics)   
  - [Battery Metrics](#battery-metrics)   
  - [Energy Cost Tracking](#energy-cost-tracking)   


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
-> (Optional)Pull down the Repo and run via SBT (simple Build tool)   
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

pull down the assembly jar file form the releases or compile via SBT   
example (linux) script could be

```sh
export ALPHA_USERNAME=username
export ALPHA_PASSWORD=passowrd
export ALPHA_SYS_SN=serial_number
export EMBER_USERNAME=username
export EMBER_PASSWORD=password
export TAPO_USERNAME=username
export TAPO_PASSWORD=password
export TAPO_ADDRESSES=192.168.1.2,192.168.1.3
export KAMON_DATADOG_API_KEY=key
export FORCAST_LAT=0
export FORCAST_LON=-0
export FORCAST_DEC=0
export FORCAST_AZ=0
export FORCAST_KWH=0

java -jar alphaess_monitor-assembly-1.0.jar 

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
```

if you wanted to work out how much your energy companany should be charging you, you have the grid pull counter metrics in 1 hour slot

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



   
KNOWN ISSUES     
Alpha published all watage usage in a Double, DataDog can only accept a Long - All metrics for Alpha are * by 10     
   
   
Usages       
-> Can be used to calculate State of Health in the Solar inverter Battery    
-> To see how wattage the invertor is using - how much actaul wattage are you getting from solar    
-> The breakdown of hosehold usage by what device     
-> The option to remotly turns a device in the house on instead of selling energy back the grid     



