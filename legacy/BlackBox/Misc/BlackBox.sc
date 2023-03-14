BlackBox {

	classvar basePath = "/Users/bjarni/Works/BlackBox/";

	*boot {
		Server.default.boot;
		MPKmini.initMidi;
	}

	*loadSynths {|interpreter, name=""|
		switch(name.toLower(),
			"chaotic", { ^interpreter.compileFile(basePath ++ "SynthDefs/Chaotic/Chaotic.scd").value },
			"modulation", { ^interpreter.compileFile(basePath ++ "SynthDefs/Modulation/Modulation.scd").value },
			"combined", { ^interpreter.compileFile(basePath ++ "SynthDefs/Subtractive/Combined.scd").value },
			"subtractive", { ^interpreter.compileFile(basePath ++ "SynthDefs/Subtractive/Subtractive.scd").value },
			"samplers", { ^interpreter.compileFile(basePath ++ "SynthDefs/Samplers/Playback.scd").value },
			{ ^interpreter.compileFile(basePath ++ "SynthDefs/" ++ name ++ ".scd").value}
		);
	}

	*loadPatterns {|interpreter, name=""|
		switch(name.toLower(),
			"samplers", { ^interpreter.compileFile(basePath ++ "Patterns/SamplerPatterns.scd").value },
			"synths", { ^interpreter.compileFile(basePath ++ "Patterns/SynthPatterns.scd").value },
			{ ^interpreter.compileFile(basePath ++ "Patterns/" ++ name ++ ".scd").value }
		);
	}

	*loadSamplers {|interpreter, path="/Users/bjarni/Works/PIECES/Charvest/synths/cSamplers.scd"|
		interpreter.compileFile(path).value;
	}

	*load {|interpreter, path="/Users/bjarni/Works/PIECES/Charvest/composition/charvest.scd"|
		^interpreter.compileFile(path).value;
	}

	*loadBufferFolder {|path|
		var sndPath = path ++ "/*";
		var files = SoundFile.collect(sndPath);
		"inside loadBufferFolder".postln;
		path.postln;
		^files.collect { |sf| Buffer.read(Server.local, sf.path)}
	}

	*loadBufferFolderToDictionary {|path|
		var sndPath = ~rootPath ++ path ++ "/*";
		var files = SoundFile.collect(sndPath);
		var dict = Dictionary();
		files.do {|sf|
			var buf = Buffer.read(Server.local, sf.path);
			var split = sf.path.split($/);
			var name = split[split.size- 1].replace(".wav", "");
			dict.put(name, buf);
		};
		dict;
	}

	*loadBuffers {|interpreter, name=""|
		"inside load buffers".postln;
		switch(name.toLower(),
			"textures", { ^this.loadBufferFolder(basePath ++ "Material/Textures").value },
			"complex", { ^this.loadBufferFolder(basePath ++ "Material/Complex").value },
			{ ("No Buffer collection found with this name:" + name).postln }
		);
	}

	*loadBuffersAbsolute {|interpreter, name=""|
		 ^this.loadBufferFolder(name).value
	}

	*loadWavetableBuffers {|size, path, server, callback|
		var files = SoundFile.collect(path);

		Routine({

			callback.(files.collect { |sf|
				var signal, file;

				file = SoundFile.new;
				file.openRead(sf.path);
				server.sync;

				signal = Signal.newClear(size);
				file.readData(signal);
				server.sync;

				Buffer.loadCollection(Server.local, signal.asWavetable);
			});
		}).play;
	}

	*increaseResources {|server|
		Routine({
			server.freeAll;
			server.boot;
			server.sync;
			server.options.numPrivateAudioBusChannels = 1024;
			server.options.memSize = 8192 * 256;
			server.options.numPrivateAudioBusChannels = 128;
			server.options.memSize = 8192 * 256;
			server.sync;
			server.reboot;
		}).play;
	}

	*soundflower {
		var options, server;
		Routine({
			server = Server.default;
			options = server.options;
			options.numOutputBusChannels = 16;
			server.options.device = "Soundflower (64ch)";
			server.freeAll;
			server.reboot;
			server.sync;
		}).play;
	}

	*motu {
		var options, server;
		Routine({
			server = Server.default;
			options = server.options;
			options.numOutputBusChannels = 8;
			server.options.device = "MOTU UltraLite mk3 Hybrid";
			server.freeAll;
			server.reboot;
			server.sync;
		}).play;
	}

	*defaultOutput {
		var options, server;
		Routine({
			server = Server.default;
			server.options.device = "Built-in";
			server.freeAll;
			server.reboot;
			server.sync;
		}).play;
	}

	*specsToArguments {|specs|

		var arguments = Array.newClear(specs.size);

		specs.do{|patternSpecs, index|
			var patternArguments = Dictionary();

			patternSpecs.do{|item|
				patternArguments[item[0]] = item[1].default;
			};

			arguments[index] = patternArguments
		};

		^arguments;
	}

	*nestedSpecsToArguments {|specs|

		var patternArguments = Dictionary();

			specs.do{|item|
				patternArguments[item[0]] = item[1].default;
			};

		^patternArguments;
	}
}