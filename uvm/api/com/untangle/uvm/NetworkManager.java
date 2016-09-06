/**
 * $Id$
 */
package com.untangle.uvm;

import java.util.List;
import java.net.InetAddress;

import com.untangle.uvm.node.IPMaskedAddress;
import com.untangle.uvm.network.InterfaceStatus;
import com.untangle.uvm.network.DeviceStatus;
import com.untangle.uvm.network.InterfaceSettings;
import com.untangle.uvm.network.NetworkSettings;
import com.untangle.uvm.network.NetworkSettingsListener;

/**
 * NetworkManager interface
 * documentation in NetworkManagerImpl
 */
public interface NetworkManager
{
    NetworkSettings getNetworkSettings();

    void setNetworkSettings( NetworkSettings newSettings );

    void registerListener( NetworkSettingsListener networkListener );

    void unregisterListener( NetworkSettingsListener networkListener );

    void renewDhcpLease( int interfaceId );
    
    /* convenience methods */

    List<InterfaceSettings> getEnabledInterfaces();
    
    InetAddress getFirstWanAddress();

    InetAddress getInterfaceHttpAddress( int clientIntf );

    InterfaceSettings findInterfaceId( int interfaceId );

    InterfaceSettings findInterfaceSystemDev( String systemDev );

    InterfaceSettings findInterfaceFirstWan( );

    InterfaceStatus getInterfaceStatus( int interfaceId );

    List<InterfaceStatus> getInterfaceStatus( );
    
    List<DeviceStatus> getDeviceStatus( );

    List<IPMaskedAddress> getCurrentlyUsedNetworks( boolean includeDynamic, boolean includeL2tp, boolean includeOpenvpn );

    boolean isVrrpMaster( int interfaceId );

    List<Integer> getWirelessChannels( String systemDev );

    String getUpnpManager(String command, String arguments);
}
