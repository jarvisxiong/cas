import requests
server_failed = []
server_sucess = []

def check_health (server, endpoint, data = "none"):
    global server_failed
    global server_sucess
    server_url = "http://"+ server + endpoint
    try:
        #print server_url
        r = requests.get(server_url)
        #print type(r.status_code)
        if r.status_code != 200:
            server_failed.append(server)
        else:
            print 
            server_sucess.append(server)
        r.close()

    except Exception, e:
        #r.close()
        server_failed.append(server)
        #raise e

services_healthcheck = [['cas_uh1', 'cas_healthcheck'], ['ifc_uh1','ifc_healthcheck']]
#cas servers 
#use the below configuration to generatethe host names
#echo cas10{00..13}.ads.uh1.inmobi.com | sed "s/ /\',\' /g" | sed "s/^/'/" | sed "s/$/'/"
cas_uh1 = ['cas1000.ads.uh1.inmobi.com','cas1001.ads.uh1.inmobi.com','cas1002.ads.uh1.inmobi.com','cas1003.ads.uh1.inmobi.com','cas1004.ads.uh1.inmobi.com','cas1005.ads.uh1.inmobi.com','cas1006.ads.uh1.inmobi.com','cas1007.ads.uh1.inmobi.com','cas1008.ads.uh1.inmobi.com','cas1009.ads.uh1.inmobi.com','cas1010.ads.uh1.inmobi.com','cas1011.ads.uh1.inmobi.com','cas1012.ads.uh1.inmobi.com','cas1013.ads.uh1.inmobi.com']
cas_healthcheck = ':8800/stat'

#IFC box details
ifc_uh1 = ['ifc2000.ads.uh1.inmobi.com',' ifc2001.ads.uh1.inmobi.com',' ifc2002.ads.uh1.inmobi.com',' ifc2003.ads.uh1.inmobi.com',' ifc2004.ads.uh1.inmobi.com',' ifc2005.ads.uh1.inmobi.com',' ifc2006.ads.uh1.inmobi.com',' ifc2007.ads.uh1.inmobi.com',' ifc2008.ads.uh1.inmobi.com',' ifc2012.ads.uh1.inmobi.com',' ifc2013.ads.uh1.inmobi.com',' ifc2014.ads.uh1.inmobi.com',' ifc2015.ads.uh1.inmobi.com',' ifc2016.ads.uh1.inmobi.com',' ifc2017.ads.uh1.inmobi.com',' ifc1001.ads.uh1.inmobi.com',' ifc1002.ads.uh1.inmobi.com',' ifc1003.ads.uh1.inmobi.com',' ifc1004.ads.uh1.inmobi.com',' ifc1005.ads.uh1.inmobi.com',' ifc1006.ads.uh1.inmobi.com',' ifc1007.ads.uh1.inmobi.com',' ifc1008.ads.uh1.inmobi.com',' ifc1011.ads.uh1.inmobi.com',' ifc1012.ads.uh1.inmobi.com',' ifc1013.ads.uh1.inmobi.com',' ifc1014.ads.uh1.inmobi.com',' ifc1015.ads.uh1.inmobi.com',' ifc1016.ads.uh1.inmobi.com',' ifc1017.ads.uh1.inmobi.com',' ifc1018.ads.uh1.inmobi.com',' ifc1019.ads.uh1.inmobi.com',' ifc1020.ads.uh1.inmobi.com']
ifc_healthcheck = ':8080/IFCPlatform/admin/v1/metrics'

for service in services_healthcheck:
    for host in service:
        print host
        check_health(host, cas_healthcheck, "test")

# for host in cas_uh1:
#     check_health(host, cas_healthcheck, "test")

# print "Server Failed", server_failed, len(server_failed)
# print "Server succedded", server_sucess, len(server_sucess)