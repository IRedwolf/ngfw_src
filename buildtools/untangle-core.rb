# -*-ruby-*-
# $Id$

## Require all of the sub packages.
## Done manually because order matters.
require "#{SRC_HOME}/libmvutil/package.rb"
require "#{SRC_HOME}/libnetcap/package.rb"
require "#{SRC_HOME}/libvector/package.rb"
require "#{SRC_HOME}/jmvutil/package.rb"
require "#{SRC_HOME}/jnetcap/package.rb"
require "#{SRC_HOME}/jvector/package.rb"
require "#{SRC_HOME}/uvm/package.rb"

require "#{SRC_HOME}/reporting/package.rb"
require "#{SRC_HOME}/ftp-casing/package.rb"
require "#{SRC_HOME}/http-casing/package.rb"
require "#{SRC_HOME}/mail-casing/package.rb"
require "#{SRC_HOME}/spyware/package.rb"
require "#{SRC_HOME}/router/package.rb"
require "#{SRC_HOME}/shield/package.rb"
require "#{SRC_HOME}/firewall/package.rb"
require "#{SRC_HOME}/openvpn/package.rb"
require "#{SRC_HOME}/protofilter/package.rb"
require "#{SRC_HOME}/ips/package.rb"

## Base Nodes
require "#{SRC_HOME}/spam-base/package.rb"
require "#{SRC_HOME}/virus-base/package.rb"
require "#{SRC_HOME}/clam-base/package.rb"
require "#{SRC_HOME}/webfilter-base/package.rb"

## Spam based nodes
require "#{SRC_HOME}/phish/package.rb"
require "#{SRC_HOME}/spamassassin/package.rb"

## Webfilter based nodes
require "#{SRC_HOME}/webfilter/package.rb"

## Ad Blocker node
require "#{SRC_HOME}/adblocker/package.rb"

## Virus based nodes
require "#{SRC_HOME}/clam/package.rb"

wlibs = []

libuvmcore_so = "#{BuildEnv::SRC.staging}/libuvmcore.so"

archives = ['libmvutil', 'libnetcap', 'libvector', 'jmvutil', 'jnetcap', 'jvector']

## Make the .so dependent on each archive
archives.each do |n|
  file libuvmcore_so => BuildEnv::SRC[n]['archive']
end

file libuvmcore_so do
  compilerEnv = CCompilerEnv.new( { "flags" => "-pthread #{CCompilerEnv.defaultDebugFlags}" } )
  archivesFiles = archives.map { |n| BuildEnv::SRC[n]['archive'].filename }

  CBuilder.new(BuildEnv::SRC, compilerEnv).makeSharedLibrary(archivesFiles, libuvmcore_so, [],
                                                             ['xml2', 'sysfs', 'netfilter_queue','netfilter_conntrack'], wlibs)
end

BuildEnv::SRC['untangle-libuvm']['impl'].register_dependency(libuvmcore_so)

BuildEnv::SRC.installTarget.install_files(libuvmcore_so, "#{BuildEnv::SRC['untangle-libuvmcore'].distDirectory}/usr/lib/uvm")

# DO IT!
#graphViz('foo.dot')
