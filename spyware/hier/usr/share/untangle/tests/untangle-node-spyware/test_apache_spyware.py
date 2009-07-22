from untangle.ats.apache_setup import ApacheSetup

class TestApacheSpyware(ApacheSetup):
    @classmethod
    def setup_class(cls):
        ApacheSetup.setup_class.im_func(cls)
        ApacheSetup.install_node.im_func( cls, "untangle-node-spyware", delete_existing = False )

        ## Just use the tid for another node, this way you don't need a nonce but can
        ## tell if it is able to access the blockpage servlet.
        tid = cls.node_manager.im_func( cls ).nodeInstances("untangle-node-router")["list"][0]
        cls.router_tid = str( tid["id"] )
    
    def test_root_access_outside_admin_enable( self ):
        self.set_access_settings({ "isOutsideAdministrationEnabled" : True })

        base = "http%s://localhost%s/spyware/blockpage?tid=" + self.router_tid

        ## This should be run as root, so it should access these
        yield self.check_access, base % ( "", "" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "", ":64156" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "s", "" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "s", ":64157" ), 406, "Feature is not installed"

    def test_root_access_outside_admin_disabled( self ):
        self.set_access_settings({ "isOutsideAdministrationEnabled" : False })

        base = "http%s://localhost%s/spyware/blockpage?tid=" + self.router_tid

        ## This should be run as root, so it should access these
        yield self.check_access, base % ( "", "" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "", ":64156" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "s", "" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "s", ":64157" ), 406, "Feature is not installed"

    def test_nonroot_access_outside_admin_enable( self ):
        self.set_access_settings({ "isOutsideAdministrationEnabled" : True })

        base = "http%s://192.0.2.43%s/spyware/blockpage?tid=" + self.router_tid
        
        yield self.check_access, base % ( "", "" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "", ":64156" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "s", "" ), 403, "Off-site administration is disabled"
        yield self.check_access, base % ( "s", ":64157" ), 406, "Feature is not installed"

    def test_nonroot_access_outside_admin_disabled( self ):
        self.set_access_settings({ "isOutsideAdministrationEnabled" : False })

        base = "http%s://192.0.2.43%s/spyware/blockpage?tid=" + self.router_tid

        ## This should be run as root, so it should access these
        yield self.check_access, base % ( "", "" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "", ":64156" ), 406, "Feature is not installed"
        yield self.check_access, base % ( "s", "" ), 403, "Off-site administration is disabled"
        yield self.check_access, base % ( "s", ":64157" ), 406, "Feature is not installed"
