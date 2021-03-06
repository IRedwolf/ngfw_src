"""spam_blocker ssl tests"""
import subprocess

import unittest
import pytest
from tests.global_functions import uvmContext
import runtests.remote_control as remote_control
import runtests.test_registry as test_registry
import tests.global_functions as global_functions
import tests.ipaddr as ipaddr
from uvm import Uvm

from .test_spam_blocker_base import SpamBlockerBaseTests

#
# Just extends the spam base tests to include SSL Inspector with default settings
#
@pytest.mark.spam_blocker_w_ssl
class SpamBlockerWithSSLTests(SpamBlockerBaseTests):

    @staticmethod
    def module_name():
        # cheap trick to force class variable _app into global namespace as app
        global app
        app = SpamBlockerBaseTests._app
        return "spam-blocker"

    @staticmethod
    def shortName():
        return "spam-blocker"

    @staticmethod
    def displayName():
        return "Spam Blocker"

    # verify daemon is running
    def test_009_IsRunningAndSSL(self):
        appSSL = appSP = uvmContext.appManager().app(self.appNameSSLInspector())
        appSSL.start()
        result = subprocess.call("ps aux | grep spamd | grep -v grep >/dev/null 2>&1", shell=True)
        assert (result == 0)
        result = subprocess.call("ps aux | grep spamcatd | grep -v grep >/dev/null 2>&1", shell=True)
        assert ( result == 0 )

    def test_010_license_valid(self):
        print("MODULE NAME FROM SUBCLASS: %s" % self.module_name())
        assert(uvmContext.licenseManager().isLicenseValid("spam-blocker-lite"))

test_registry.register_module("spam-blocker-w-ssl", SpamBlockerWithSSLTests)
