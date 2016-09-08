import socket
import unittest2
import os
import subprocess
import sys
import re
import urllib2
import time
import copy
reload(sys)
sys.setdefaultencoding("utf-8")
import re
import subprocess
import ipaddr
import time
import ssl

from jsonrpc import ServiceProxy
from jsonrpc import JSONRPCException
from global_functions import uvmContext
from uvm import Manager
from uvm import Uvm
import test_registry
import remote_control
import system_properties

node = None
nodeFW = None

defaultRackId = 1
origMailsettings = None
test_untangle_com_ip = socket.gethostbyname("test.untangle.com")

def getLatestMailPkg():
    remote_control.runCommand("rm -f mailpkg.tar*") # remove all previous mail packages
    results = remote_control.runCommand("wget -q -t 1 --timeout=3 http://test.untangle.com/test/mailpkg.tar")
    # print "Results from getting mailpkg.tar <%s>" % results
    results = remote_control.runCommand("tar -xvf mailpkg.tar")
    # print "Results from untaring mailpkg.tar <%s>" % results

class UvmTests(unittest2.TestCase):

    @staticmethod
    def nodeName():
        return "uvm"

    @staticmethod
    def vendorName():
        return "Untangle"

    @staticmethod
    def nodeNameSpamCase():
        return "untangle-casing-smtp"

    @staticmethod
    def initialSetUp(self):
        pass

    def setUp(self):
        pass

    def test_010_clientIsOnline(self):
        result = remote_control.isOnline()
        assert (result == 0)

    def test_011_helpLinks(self):
        output, error = subprocess.Popen(['find',
                                          '%s/usr/share/untangle/web/webui/script/' % system_properties.getPrefix(),
                                          '-name',
                                          '*.js',
                                          '-type',
                                          'f'], stdout=subprocess.PIPE).communicate()
        assert(output)
        for line in output.splitlines():
            print "Checking file %s..." % line
            assert (line)
            if line == "":
                continue

            ctx = ssl.create_default_context()
            ctx.check_hostname = False
            ctx.verify_mode = ssl.CERT_NONE

            webUiFile = open( line )
            assert( webUiFile )
            pat  = re.compile(r'''^.*helpSource:\s*['"]+([a-zA-Z_]*)['"]+[\s,]*$''')
            pat2 = re.compile(r'''.*URL=http://wiki.*.untangle.com/(.*)">.*$''')
            for line in webUiFile.readlines():
                match = pat.match(line)
                if match != None:
                    helpSource = match.group(1)
                    assert(helpSource)

                    url = "http://wiki.untangle.com/get.php?source=" + helpSource + "&uid=0000-0000-0000-0000&version=11.0.0&webui=true&lang=en"
                    print "Checking %s = %s " % (helpSource, url)
                    ret = urllib2.urlopen( url, context=ctx )
                    time.sleep(.1) # dont flood wiki
                    assert(ret)
                    result = ret.read()
                    assert(result)
                    match2 = pat2.match( result )
                    assert(match2)
                    # Check that it redirects somewhere other than /
                    print "Result: \"%s\"" % match2.group(1)
                    assert(match2.group(1))

        assert(True)

    def test_020_aboutInfo(self):
        uid =  uvmContext.getServerUID()
        match = re.search(r'\w{4}-\w{4}-\w{4}.\w{4}', uid)
        assert( match )

        version = uvmContext.adminManager().getFullVersionAndRevision()
        match = re.search(r'\d{1,2}\.\d\.\d\~vcs\d{8}r\d{5}\w{4,9}(\.\d)?-\w{5,8}',version)
        assert(match)

        kernel = uvmContext.adminManager().getKernelVersion()
        match = re.search(r'\d.*', kernel)
        assert(match)

        reboot_count = uvmContext.adminManager().getRebootCount()
        match = re.search(r'\d{1,2}', reboot_count)
        assert(match)

        num_hosts = str(uvmContext.hostTable().getCurrentActiveSize())
        match = re.search(r'\d{1,2}', num_hosts)
        assert(match)

        max_num_hosts = str(uvmContext.hostTable().getMaxActiveSize())
        match = re.search(r'\d{1,2}', max_num_hosts)
        assert(match)

    def test_030_testSMTPSettings(self):
        if remote_control.quickTestsOnly:
            raise unittest2.SkipTest('Skipping a time consuming test')
        # Test mail setting in config -> email -> outgoing server
        if (uvmContext.nodeManager().isInstantiated(self.nodeNameSpamCase())):
            print "smtp case present"
        else:
            print "smtp not present"
            uvmContext.nodeManager().instantiate(self.nodeNameSpamCase(), 1)
        nodeSP = uvmContext.nodeManager().node(self.nodeNameSpamCase())
        origNodeDataSP = nodeSP.getSmtpNodeSettings()
        origMailsettings = uvmContext.mailSender().getSettings()
        # print nodeDataSP
        getLatestMailPkg();
        # remove previous smtp log file
        remote_control.runCommand("rm -f /tmp/test_030_testSMTPSettings.log /tmp/test@example.com.1")
        # Start mail sink
        remote_control.runCommand("python fakemail.py --host=" + remote_control.clientIP +" --log=/tmp/test_030_testSMTPSettings.log --port 6800 --background --path=/tmp/", stdout=False, nowait=True)
        newMailsettings = copy.deepcopy(origMailsettings)
        newMailsettings['smtpHost'] = remote_control.clientIP
        newMailsettings['smtpPort'] = "6800"
        newMailsettings['sendMethod'] = 'CUSTOM'

        uvmContext.mailSender().setSettings(newMailsettings)
        time.sleep(10) # give it time for exim to restart

        nodeDataSP = nodeSP.getSmtpNodeSettings()
        nodeSP.setSmtpNodeSettingsWithoutSafelists(nodeDataSP)
        uvmContext.mailSender().sendTestMessage("test@example.com")
        time.sleep(2)
        # force exim to flush queue
        subprocess.call(["exim -qff >/dev/null 2>&1"],shell=True,stdout=None,stderr=None)
        time.sleep(10)

        # Kill mail sink
        remote_control.runCommand("pkill -INT python")
        uvmContext.mailSender().setSettings(origMailsettings)
        nodeSP.setSmtpNodeSettingsWithoutSafelists(origNodeDataSP)
        result = remote_control.runCommand("grep -q 'Untangle Server Test Message' /tmp/test@example.com.1")
        assert(result==0)

test_registry.registerNode("uvm", UvmTests)
