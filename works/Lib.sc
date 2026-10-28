Lib {

	classvar midiOuts;

	// a folder ending in "/" means every sound file in it
	*glob {|path|
		path = path.standardizePath;
		^if(path.endsWith("/"), { path ++ "*" }, { path })
	}

	*loadFolderSamples {|path="/Users/bjarni/Works/SND/impulses/*", server, action|
		var bufs;
		server = server ? Server.default;
		bufs = SoundFile.collect(this.glob(path)).collect { |sf| Buffer.read(server, sf.path) };
		if(action.notNil, { fork { server.sync; action.value(bufs) } });
		^bufs
	}

	// buffers keyed by file name without extension, e.g. d[\clicka]
	*loadFolderDict {|path, server, action|
		var dict = IdentityDictionary.new;
		server = server ? Server.default;
		SoundFile.collect(this.glob(path)).do { |sf|
			dict[PathName(sf.path).fileNameWithoutExtension.asSymbol] = Buffer.read(server, sf.path)
		};
		if(action.notNil, { fork { server.sync; action.value(dict) } });
		^dict
	}

	// the first `size` frames of each file as a wavetable buffer (2 * size frames)
	*loadWavetables {|path, size=4096, server, action|
		var bufs;
		server = server ? Server.default;
		bufs = SoundFile.collect(this.glob(path)).collect { |sf|
			var signal = Signal.newClear(size), file = SoundFile.openRead(sf.path);
			file.readData(signal); file.close;
			Buffer.loadCollection(server, signal.asWavetable)
		};
		if(action.notNil, { fork { server.sync; action.value(bufs) } });
		^bufs
	}

	// grain window: fast curved rise, short fall
	*grainWindow {|atk=0.1, rel=0.01, curves=#[8, -4], server|
		^Buffer.sendCollection(server ? Server.default, Env([0, 1, 0], [atk, rel], curves).discretize, 1)
	}

	// pattern values as raw samples, as many as fit in `dur` seconds
	*patternToBuffer {|pattern, dur=1, server, action|
		var n, stream = pattern.asStream, data = List.new, v;
		server = server ? Server.default;
		n = ((server.sampleRate ? 44100) * dur).asInteger;
		while { data.size < n and: { (v = stream.next).notNil } } { data.addAll(v.asArray) };
		^Buffer.loadCollection(server, data.keep(n), 1, action)
	}

	// one MIDIOut per driver and port, created once
	*midiOut {|driver="IAC Driver", port="BG.Virtual"|
		var key = (driver ++ "/" ++ port).asSymbol;
		midiOuts = midiOuts ? IdentityDictionary.new;
		if(midiOuts[key].isNil, {
			if(MIDIClient.initialized.not, { MIDIClient.init; MIDIIn.connectAll });
			midiOuts[key] = MIDIOut.newByName(driver, port);
		});
		^midiOuts[key]
	}

	// map a CC onto anything that responds to set (Ndef, NodeProxy, Synth); returns the MIDIFunc
	*ccMap {|target, param, cc, lo=0, hi=1, warp=\lin, chan, post=false|
		var spec = ControlSpec(lo, hi, warp);
		if(MIDIClient.initialized.not, { MIDIClient.init; MIDIIn.connectAll });
		^MIDIFunc.cc({ |val|
			var v = spec.map(val / 127);
			target.set(param, v);
			if(post, { [param, v].postln });
		}, cc, chan)
	}

	// calls action.(amp) on every onset of source: a Function (graph), a Bus, or a hardware input index
	*onsetFollow {|source, thresh=0.5, action, odftype=\complex, server|
		var cmd = ("/onset" ++ UniqueID.next).asSymbol, synth, osc;
		server = server ? Server.default;
		synth = {
			var in = case
				{ source.isKindOf(Function) } { source.value }
				{ source.isKindOf(Bus) } { In.ar(source.index, source.numChannels) }
				{ SoundIn.ar(source) };
			in = in.asArray.sum;
			SendReply.kr(Onsets.kr(FFT(LocalBuf(512), in), thresh, odftype), cmd, Amplitude.kr(in));
			0
		}.play(server, addAction: \addToTail);
		osc = OSCFunc({ |msg| action.value(msg[3]) }, cmd, server.addr);
		synth.onFree { osc.free };
		^synth
	}

	// timestamped recording of the main outs; stop with s.stopRecording
	*rec {|name="rec", dir="~/Desktop/", numChannels=2, header="wav", sample="int24", duration, server|
		var path = dir.standardizePath +/+ name ++ "_" ++ Date.getDate.stamp ++ "." ++ header;
		server = server ? Server.default;
		server.recHeaderFormat = header;
		server.recSampleFormat = sample;
		server.record(path, 0, numChannels, duration: duration);
		^path
	}

	// one mono file per bus channel, for multichannel pieces and datasets
	*recordChannels {|name="rec", numChannels=2, duration=4, offset=0, dir="~/Desktop/record/", sample="int24", server, action|
		var paths, bufs, synths;
		server = server ? Server.default;
		dir = dir.standardizePath;
		File.mkdir(dir);
		paths = numChannels.collect { |i| dir +/+ name ++ "_" ++ (offset + i) ++ ".wav" };
		^Routine {
			SynthDef(\libDiskOut, { |bufnum, bus| DiskOut.ar(bufnum, In.ar(bus, 1)) }).add;
			bufs = numChannels.collect { Buffer.alloc(server, 262144, 1) };
			server.sync;
			bufs.do { |b, i| b.write(paths[i], "wav", sample, 0, 0, true) };
			server.sync;
			synths = bufs.collect { |b, i| Synth(\libDiskOut, [\bufnum, b, \bus, offset + i], server, \addToTail) };
			"Recording started".postln;
			duration.wait;
			synths.do(_.free);
			server.sync;
			bufs.do { |b| b.close; b.free };
			"Recording stopped".postln;
			action.value(paths);
		}.play
	}

	// runs a CDP program in Terminal, where CDP is on the PATH; returns the command
	*cdp {|program, input, output, params=""|
		var cmd = program + input.standardizePath.shellQuote + output.standardizePath.shellQuote + params;
		cmd.postln;
		cmd.runInTerminal;
		^cmd
	}

	*boot {|outs=16, ins=2, memSize=(8192 * 256), server, action|
		server = server ? Server.default;
		server.options.numOutputBusChannels = outs;
		server.options.numInputBusChannels = ins;
		server.options.memSize = memSize;
		server.waitForBoot(action);
		^server
	}
}
