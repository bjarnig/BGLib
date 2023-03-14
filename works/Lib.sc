Lib {

	*loadFolderSamples {|path="/Users/bjarni/Works/SND/impulses/*"|
		^SoundFile.collect(path).collect { |sf| Buffer.read(Server.local, sf.path)};
	}
}
