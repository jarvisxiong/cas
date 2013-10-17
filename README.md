cas
===

Channel Ad Server.

pre-git doc
http://twiki.corp.inmobi.com/Engineering/CAS

git clone git@github.corp.inmobi.com:channel-adserve/cas.git

mvn clean install

java -jar target/Server...jar

config location
/opt/mkhoj/conf/cas/channel-server.properties

source
git@github.corp.inmobi.com:channel-adserve/misc.git

VMs
10.14.118.184
10.14.118.185
10.14.118.119
10.14.118.66

Requests

Ad request
http://localhost:8800/backfill?args={"site-type":"FAMILY_SAFE","u-id-params-log":{"O1":"e7083b68019bb1d4641065c44977dd0900840113","u-id-s":"O1"},"handset":[46806,"lg_e612_ver1_subuag"],"rqMkAdSlot":"15","tp":"c_gwhirl","new-category":[20,8],"site-floor":0,"os-id":3,"raw-uid":{"O1":"e7083b68019bb1d4641065c44977dd0900840113"},"carrier":[521,102,"MX",28967,31286],"site-url":"https://play.google.com/store/apps/details?id=com.magmamobile.game.Burger","tid":"c5190fe1-0141-1cb3-d320-180373ed49e6","sel-seg-id":"0","site":[118274,57],"rqHUserAgent":"Mozilla/5.0 (Linux; U; Android 4.0.3; es-mx; LG-E612g Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30","rqMkSiteid":"4028cbff379738bf01379cff92c60229","w-s-carrier":"189.203.217.132","loc-src":"no-targeting","rqIpFileVer":"5","slot-served":"15","uparams":{"u-rt":"0","u-appdnm":"Hamburguesa","u-appver":"1.0.9","u-key-ver":"1","u-appbid":"com.magmamobile.game.Burger"},"r-format":"xhtml","site-allowBanner":true,"rqMkAdcount":"1","category":[1,10,13,249,532],"source":"ANDROID","rich-media":false,"adcode":"NON-JS","model-id":46425,"sdk-version":"a361","pub-id":"4028cb9028ba521501291690dd91095c"}

update config
http://localhost:8800/configChange?update={"adapter.ifc.status":"off"}

Ad request
http://localhost:8800/trace?args={"site":[34093,60],"site-type":"PERFORMANCE","w-s-carrier":"3.54.96.0","rqHUserAgent":"Mozilla/5.0 (Linux; U; Android 4.2.1; en-us; Nexus 7 Build/JOP40D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30","handset":["46769","google_nexus7_ver1_suban42aosp"],"carrier":[406,94,"US",12328,31118],"new-category":[70,71],"uparams":{"u-id":"202cb962ac59075b964b07152d234b70","u-age":"12","u-postalcode":"123","u-gender":"male"},"rqMkSiteid":"00e3a40cd8e74922b22d52ef6712b43b","rqMkAdSlot":"15","tid":"b3e6fe89-4fcd-4553-a05a-753d02a777ea","os-id":3,"r-format":"xhtml","source":"APP","slot-served":"2","model-id":46188,"sel-seg-id":0,"u-id-params":{"O1":"9e74e1b452e07afded40fd15b45aa9e6","u-id-s":"O1"}}

