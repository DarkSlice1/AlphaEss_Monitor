Application for monitoring and reporting in the state of the Alpha Ess Solar System inverter.   
Also reports on the Energy usage of 1 or more TP-110 Smart Plug    
   
All metrics are then published to DataDog   
   
Requires the follow Enviroment variables.   
   
ALPHA_USERNAME= The Registered Usename on your Alpha Ess System   
ALPHA_PASSWORD= The Registered password on your Alpha Ess System   
ALPHA_SYS_SN= The Serial number of your Alpha Ess System ( can be seen on the top of the Alpha App)   
TAPO_USERNAME= The Registered Usename/Email Address on your Tapo TP-Link Account   
TAPO_PASSWORD= The Registered Password Address on your Tapo TP-Link Account   
TAPO_ADDRESSES= Common Delimied list of IP address of you Tapo devices EG. "192.168.1.10,192.168.1.11";   
KAMON_DATADOG_API_KEY= The API key for your DataDog account upon which to pushlish all metrics to   
   
   
Known issues      
Alpha published all watage usage in a Double, DataDog can only accept a Long - All metrics for Alpha are * by 10   
   
   
Usages      
-> Can be used to calculate State of Health in the Solar inverter Battery   
-> To see how wattage the invertor is using - how much actaul wattage are you getting from solar   
-> The breakdown of hosehold usage by what device   
-> The option to remotly turns a device in the house on instead of selling energy back the grid   
