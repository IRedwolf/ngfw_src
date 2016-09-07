# -*-ruby-*-

# declare all arch-dep sub packages...
pkgs = ['libmvutil', 'libnetcap', 'libvector',
            'jmvutil', 'jnetcap', 'jvector']

# ... and include them
pkgs.each { |a| require "#{SRC_HOME}/#{a}/package.rb" }

# files and dirs we'll manipulate
file_libuvmcore_so = "#{BuildEnv::SRC.staging}/libuvmcore.so"
dist_dir = BuildEnv::SRC['untangle-libuvmcore'].distDirectory
dest_libuvmcore_dir = "#{dist_dir}/usr/lib/uvm/"
dest_libuvmcore_so = "#{dest_libuvmcore_dir}/libuvmcore.so"

# make sure we properly check if dependencies are up-to-date; for some
# reason this happens automatically when those those Targets are
# registered as dependencies, but not otherwise
pkgs.map { |a| BuildEnv::SRC[a]['archive'].task.invoke }

# make the .so dependent on all packages generated from arch-dep sub
# packages, and describe how to build it
archiveFiles = pkgs.map { |a| BuildEnv::SRC[a]['archive'].filename }
file file_libuvmcore_so => archiveFiles do
  # define compiler
  flags = "-pthread #{CCompilerEnv.defaultDebugFlags}"
  compilerEnv = CCompilerEnv.new( {"flags" => flags} )
  cbuilder = CBuilder.new(BuildEnv::SRC, compilerEnv)

  # shared lib building
  cbuilder.makeSharedLibrary(archiveFiles, file_libuvmcore_so, [],
                             ['netfilter_queue','netfilter_conntrack'],
                             [])
end

# associate a task to the building of that so file
task :libuvmcore_so => file_libuvmcore_so

# installed version of the so file
file dest_libuvmcore_so => file_libuvmcore_so do
  FileUtils.mkdir_p(dest_libuvmcore_dir)
  info "[copy    ] #{file_libuvmcore_so} => #{dest_libuvmcore_dir}"
  FileUtils.cp("#{BuildEnv::SRC.staging}/libuvmcore.so", dest_libuvmcore_dir)
end

# associate a task to the installation of that so file
task :dest_uvmcore_so => dest_libuvmcore_so

# create the build dependency graph
# graphViz('graphviz.dot')
