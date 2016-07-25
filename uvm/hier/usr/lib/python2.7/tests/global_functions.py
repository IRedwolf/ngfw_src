import os
import sys
import subprocess
import time
import datetime
import re

import remote_control
import system_properties
import ipaddr
import smtplib
import json

from uvm import Uvm

officeNetworks = ('10.111.0.0/16','10.112.0.0/16');
iperfServers = [('10.111.0.0/16','10.111.56.41'), # Office network
                ('10.112.0.0/16','10.112.56.44')] # ATS VM
iperfServer = ""
smtpServerHost = 'test.untangle.com'
accountFileServer = "10.112.56.44"
accountFile = "/tmp/account_login.json"

uvmContext = Uvm().getUvmContext(timeout=120)
uvmContextLongTimeout = Uvm().getUvmContext(timeout=300)

def getIpAddress(base_URL="test.untangle.com",extra_options="",localcall=False):
    timeout = 4
    result = ""
    while result == "" and timeout > 0:
        timeout -= 1
        if localcall:
            result = subprocess.check_output("wget --timeout=4 " + extra_options + " -q -O - \"$@\" test.untangle.com/cgi-bin/myipaddress.py", shell=True)
        else:
            result = remote_control.runCommand("wget --timeout=4 " + extra_options + " -q -O - \"$@\" " + base_URL + "/cgi-bin/myipaddress.py",stdout=True)
    return result
    
def verifyIperf(wanIP):
    # https://iperf.fr/
    global iperfServer
    # check if there is an iperf server on the same network
    for iperfServerSet in iperfServers:
        if ipaddr.IPv4Address(wanIP) in ipaddr.IPv4Network(iperfServerSet[0]):
            iperfServer = iperfServerSet[1]
            break
    if iperfServer == "":
        print "No iperf server in the same network"
        return False
    # Check to see if iperf endpoint is reachable
    iperfServerReachable = subprocess.call(["ping -c 1 " + iperfServer + " >/dev/null 2>&1"],shell=True,stdout=None,stderr=None)
    if iperfServerReachable != 0:
        print "iperf Server is unreachable."
        return False
    # Check to see if some other test is using iperf for UDP testing
    iperfRunning = remote_control.runCommand("pidof iperf", host=iperfServer)
    if iperfRunning == 0:
        print "iperf is already running on server."
        return False
    # Check that the client has iperf
    clientHasIperf = remote_control.runCommand("test -x /usr/bin/iperf")
    if clientHasIperf != 0:
        print "iperf not installed on client."
        return False
    return True

def getUDPSpeed( receiverIP, senderIP, targetIP=None, targetRate=None ):
    if targetIP == None:
        targetIP = receiverIP
    if targetRate == None:
        targetRate = "50M"

    # Use iperf to get UDP speed.  Returns number the udp speed
    # start iperf receivier on server
    remote_control.runCommand("iperf -s -p 5000 -u >/dev/null 2>&1 &", host=receiverIP)
    # start the UDP generator on the client behind the Untangle.
    iperf_tries = 5
    while iperf_tries > 0:  # try iperf a few times if it fails to send UDP packets correctly.
        report=remote_control.runCommand("iperf -c " + targetIP + " -u -p 5000 -b " + targetRate + " -t 10 -fK", host=senderIP, stdout=True)
        if '%' in report:
            break
        else:
            iperf_tries -= 1
    # kill iperf receiver    
    remote_control.runCommand("pkill iperf", host=receiverIP)

    lines = report.split("\n")
    udp_speed = None
    for line in lines:
        if '%' in line: # results line contains a '%'
            match = re.search(r'([0-9.]+) ([KM])Bytes/sec', line)
            udp_speed =  match.group(1)
            udp_speed = float(udp_speed)
            if match.group(2) == "M":
                udp_speed *= 1000
            break
    return udp_speed

def getDownloadSpeed():
    try:
        # Download file and record the average speed in which the file was download
        result = remote_control.runCommand("wget -t 3 --timeout=60 -O /dev/null -o /dev/stdout http://test.untangle.com/5MB.zip 2>&1 | tail -2", stdout=True)
        match = re.search(r'([0-9.]+) [KM]B\/s', result)
        bandwidth_speed =  match.group(1)
        # cast string to float for comparsion.
        bandwidth_speed = float(bandwidth_speed)
        # adjust value if MB or KB
        if "MB/s" in result:
            bandwidth_speed *= 1000
        # print "bandwidth_speed <%s>" % bandwidth_speed
        return bandwidth_speed
    except Exception,e:
        return None

def get_events( eventEntryCategory, eventEntryTitle, conditions, limit ):
    reports = uvmContextLongTimeout.nodeManager().node("untangle-node-reports")
    if reports == None:
        print "WARNING: reports app not found"
        return None

    reports.flushEvents()

    reportsManager = reports.getReportsManager()

    reportEntry = reportsManager.getReportEntry( eventEntryCategory, eventEntryTitle )
    if reportEntry == None:
        print "WARNING: Event entry not found: %s %s" % (eventEntryCategory, eventEntryTitle)
        return None

    return reportsManager.getEvents( reportEntry, conditions, limit )

def check_events( events, num_events, *args, **kwargs):
    if events == None:
        return False
    if num_events == 0:
        return False
    if len(events) == 0:
        print "No events in list"
        return False
    if kwargs.get('min_date') == None:
        min_date = datetime.datetime.now()-datetime.timedelta(minutes=12)
    else:
        min_date = kwargs.get('min_date')
    if (len(args) % 2) != 0:
        print "Invalid argument length"
        return False
    num_checked = 0
    while num_checked < num_events:
        if len(events) <= num_checked:
            print "failed to find event checked: %i total: %i" % (num_checked, len(events)) 
            break
        event = events[num_checked]
        num_checked += 1

        # if event has a date and its too old - ignore the event
        if event.get('time_stamp') != None:
            ts = datetime.datetime.fromtimestamp((event['time_stamp']['time'])/1000)
            if ts < min_date:
                print "ignoring old event: %s < %s " % (ts.isoformat(),min_date.isoformat())
                continue

        # check each expected value
        # if one doesn't match continue to the next event
        # if all match, return True
        allMatched = True
        for i in range(0, len(args)/2):
            key=args[i*2]
            expectedValue=args[i*2+1]
            actualValue = event.get(key)
            #print "key %s expectedValue %s actualValue %s " % ( key, str(expectedValue), str(actualValue) )
            if str(expectedValue) != str(actualValue):
                print "mismatch event[%s] expectedValue %s != actualValue %s " % ( key, str(expectedValue), str(actualValue) )
                allMatched = False
                break

        if allMatched:
            return True
    return False

def isInOfficeNetwork(wanIP):
    for officeNetworkTest in officeNetworks:
        if ipaddr.IPv4Address(wanIP) in ipaddr.IPv4Network(officeNetworkTest):
            return True
            break
    return False

def sendTestmessage(mailhost=smtpServerHost):
    sender = 'test@example.com'
    receivers = ['qa@example.com']

    message = """From: Test <test@example.com>
    To: Test Group <qa@example.com>
    Subject: SMTP e-mail test

    This is a test e-mail message.
    """

    try:
       smtpObj = smtplib.SMTP(mailhost)
       smtpObj.sendmail(sender, receivers, message)
       print "Successfully sent email through " + mailhost
       return 1
    except smtplib.SMTPException, e:
       print "Error: unable to send email through " + mailhost + " " + str(e)
       return 0

def getStatusValue(node, label):
    metric = node.getMetric(label)
    if metric == None:
        print "Missing metric: %s"%str(label) 
        return 0
    if metric.get('value') == None:
        print "Missing metric value: %s"%str(label) 
        return 0
    return metric.get('value')

def getLiveAccountInfo(accounttype):
    # Tries to file account password file and returns account and password if available
    accountFileServerPing = subprocess.call(["ping","-c","1",accountFileServer],stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    if accountFileServerPing != 0:
        return ("message",accountFileServer + " not available")
    # result_ping = subprocess.check_output("ping -c 1 " + accountFileServer, shell=True)
    # remove old file if it exist
    subprocess.call("wget -q -4 -t 2 --timeout=5 http://" + accountFileServer + "/account_login.json -O " + accountFile, shell=True)
    if not os.path.isfile(accountFile):
        return ("message",accountFile + " file not available")
    with open(accountFile) as data_file:    
        accounts = json.load(data_file)    
    if os.path.isfile(accountFile):
        os.remove(accountFile)
    for account in accounts: #i is each student's name, class, and number
        if account[0] == accounttype:
            return (account[1], account[2])
    return ("message",accounttype + " account not found")

