Context {

	var name;
	var basePath = "/Users/bjarni/Works/Context/Environment/";

	var gui, mapGui, transformations, observers, processes, storage;
	var currentRecordingRef;

	*new {|interpreter, name|
		^super.newCopyArgs().init(interpreter, name);
	}

	init {|interpreter, strName="session"|
		name = strName.replace(" ", "");
		gui = interpreter.compileFile(basePath ++ "Gui.scd").value;
		mapGui = interpreter.compileFile(basePath ++ "GuiMap.scd").value;
		transformations = interpreter.compileFile(basePath ++ "lib/Transformations.scd").value;
		observers = interpreter.compileFile(basePath ++ "lib/Observers.scd").value;
		processes = interpreter.compileFile(basePath ++ "lib/Processes.scd").value;
		storage = interpreter.compileFile(basePath ++ "Db.scd").value;
		interpreter.compileFile(basePath ++ "lib/Pipeline.scd").value;
	}


	///// REGION HISTORY

	createRecPath {|extension|
		^basePath ++ "data/record/" ++ name ++ "_" ++ currentRecordingRef ++ extension;
	}

	listen {
		var path;

		"## Context is listening ".postln;

		currentRecordingRef = Date.getDate.rawSeconds.postln.asString.replace(".", "");
		path = this.createRecPath(".wav");

		History.clear.end; History.start;

		if(Server.default.hasBooted, {
			Routine {
				Server.default.prepareForRecord(path, 2);
				Server.default.sync;
				Server.default.record;
			}.play
		})
	}

	normalizeLines {|list|
		var last = -1, tmp = 0;

		list.do{|item, index|

			if(last > -1, {
				tmp = list[index][0];
				list[index][0] = list[index][0] - last;
				last = tmp;
			}, {
				last = list[index][0];
				list[index][0] = 0;
			})
		};

		^list
	}

	collect {|useTemplate=false|
		var file, lines, path = this.createRecPath(".scd");

		History.end;

		if(Server.default.isRecording, {
			Server.default.stopRecording;
		});

		"## Listing is complete.".postln;

		// trim the whitespaces and undesired parantheses
		lines = History.lines.collect{|line| [line[0], line[2].replace(".play", "")]};

		// only select the Ndef ones and sort them
		lines = lines.select({|item| item[1].beginsWith("Ndef") });
		lines.sort({|a,b| a[0] < b[0] });

		file = File(path, "w");
		file.write(lines.asCompileString);
		file.close;

		if(useTemplate, {
			// Nodes, TEMP hardcoded to avoid the recording !!!
			^List[
			[0.0, Ndef(\vesica, { Vesica.ar(20, 0.2)})],
			[2.0, Ndef(\demwhite, { BPeakEQ.ar(DemWhite.ar(1.1), LFNoise2.ar(2).range(60,4000), 3, -6) })],
			[4.0, Ndef(\gravity, { LPF.ar(LeastChange.ar(GravityGrid.ar(0, [20, 30]), LoFM.ar(30)), 100, 0.4)})]
		]
		}, {
			^this.normalizeLines(lines.collect{|line| [line[0], line[1].interpret] });
		})
	}

	attach {|list|
		var output = List();

		list.do{|item|
			output.add([0.0, item]);
		};

		^output
	}

	// filter the history data, now only Ndefs are being recorded
	filterHistoryData {|path|
		var session, lines;

		// read the session
		session = History.new.loadCS(path);

		// trim the whitespaces and undesired parantheses .. triming needed!
		// l = h.lines.collect{|line| [line[0], line[2].trim("(").trim(")").trim(" ").trim("\n")]};
		lines = session.lines.collect{|line| [line[0], line[2]]};

		// only select the Ndef ones
		lines = lines.select({|item| item[1].beginsWith("Ndef") });

		lines.sort({|a,b| a[0] < b[0] });

		^lines;
	}

	// execute the session lines and respects the playing times
	executeSession {|list|

		// sort the list
		list.sort({|a,b| a[0] < b[0] });

		// iterate the list by time and interpret all the Ndefs
		Routine {
			list.do {|action|
				var wait = (action[0]);
				action.postcs;
				action[1].interpret;
				wait.postln;
				wait.wait;
			};
		}.play
	}

	// loads the session and prints the Ndefs to play (but does not play them)
	loadSession {|list|

		// sort the list
		list.sort({|a,b| a[0] < b[0] });

		// iterate the list and add all the Ndefs by replacing .play with an empty string
		list = list.collect {|action|
			if(action[1].contains(".play"), { action[1].postln });
			[action[0], action[1].replace(".play", "")];
		};

		// iterate the list by time and interpret all the Ndefs
		Routine {
			list.do {|action|
				var wait = (action[0]);
				action.postcs;
				action[1].interpret;
			};
		}.play
	}

	import {
		if(name == "Training", {
			var list, file, path="/Users/bjarni/Works/Context/Environment/data/record/Training.scd";
			file = File(path, "r");
			list = file.readAllString.interpret;
			list = list.collect {|item| [item[0], item[1].interpret ]};
			"loading Training".postln;
			list.postcs;
			this.layout(list, true);
			^list;
		})
	}

	///// END REGION HISTORY

	getPipeline {
		^~pipeline;
	}

	simulate {|nodes|
		var net;

		Routine {

			// Open Safari (without any controls)
			"open -a safari http://localhost:3000".unixCmd;
			3.wait;

			// Send the initial data to the simulation
			net = NetAddr("127.0.0.1", 57121);
			net.sendMsg("/numberOfBricks", nodes.size);

			//	m.sendMsg("/numberOfBricks", JSON.stringify([1,2,3]));

			// Listen to actions
			OSCdef(\x, {|msg, time|
				msg.postln;
				msg[1].postln;
				if(msg[1] == \scstart, {"recieving simulation data".postln }, { "message is not scstart".postln });
			}, "\bjarni");

			0.2.wait;
			Context.follow(0);

			0.1.wait;
			this.import();
			1.wait;

			2.wait;
			Context.follow(1);

			4.wait;
			Ndef(\bloded, { |freq=150, modFreq=8, harms=150|
	var son = Splay.ar ( HPF.ar(Blip.ar( Array.series(4, freq, 15), harms).fold2(SinOsc.ar(Array.series(4, modFreq, modFreq/2))), 30)) * 0.6;
	son = BBandStop.ar(son, LFNoise1.ar(0.3).exprange(100, 8000), 4);
	LPF.ar(son, 12000) * LFNoise2.ar(0.1).range(0.1,0.8);
}).play;

			5.wait;
Ndef(\blofm, { SinOsc.ar(LFCub.kr([25, XLine.kr(15, 26, 20)], 0, 50, 400),0, LFTri.ar(0.01).range(0.001, 0.01)) }).play

		}.play(AppClock)
	}

	display {|nodes|
		mapGui.value("Nodes -" + name, nodes);
	}

	// Persistant magic !!!
	stack {|nodes|
		var colorsa = (), colorsb = ();

		colorsa.primary = Color.fromHexString("#1E3231");
		colorsa.secondary = Color.fromHexString("#DD614A");
		colorsa.ternary = Color.fromHexString("#F4A698");

		colorsb.primary = Color.fromHexString("#2C1320");
		colorsb.secondary = Color.fromHexString("#8797AF");
		colorsb.ternary = Color.fromHexString("#A7ADC6");

		gui.value("[ Environment ]", nodes, transformations, observers, processes, colorsa, storage);
		gui.value("[ Environment ]", nodes, transformations, observers, processes, colorsb, storage);

	}

	layout {|nodes, loadProcesses=false|
		gui.value(name, nodes, transformations, observers, processes, nil, storage, loadProcesses);
	}

	*create {|interpreter, path="/Users/bjarni/Works/BlackBox/Development/CONTEXT/CNTX-Program.scd"|
		^interpreter.compileFile(path).value;
	}

	*loadSynths {|interpreter, path="/Users/bjarni/Works/Context/SynthDefs/Synthesis.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSamplers {|interpreter, path="/Users/bjarni/Works/Context/SynthDefs/Samplers.scd"|
		interpreter.compileFile(path).value;
	}

	*lib {|interpreter, path="/Users/bjarni/Works/Context/Environment/Lib.scd"|
		^interpreter.compileFile(path).value;
	}

	*loadWavetables {|path, server, callback|
		var size = 256 * 16;
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

				Buffer.loadCollection(server, signal.asWavetable);
			});
		}).play;
	}

	*load {|interpreter|

	DDWSnippets.put("demwhite", "Ndef(\\demwhite, { BPeakEQ.ar(DemWhite.ar(1.1), LFNoise2.ar(2).range(80,4000), 3, -6) }).play;");

	DDWSnippets.put("gravity", "Ndef(\\gravity, { LPF.ar(LeastChange.ar(GravityGrid.ar(0, [20, 30]), LoFM.ar(30)), 100, 0.4)}).play;");


	~buffers = Dictionary();
	~buffers.put(\blb5a, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/Bl05/blb5a.wav"));
	~buffers.put(\blb3a, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/Bl03/blb3a.wav"));
	~buffers.put(\blb3d, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/Bl03/blb3d.wav"));
	~buffers.put(\blb4d, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/Bl04/blb4d.wav"));
	~buffers.put(\intera, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/interrupt/intera.wav"));
	~buffers.put(\interb, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/interrupt/interb.wav"));
	~buffers.put(\interc, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/interrupt/interc.wav"));
	~buffers.put(\interd, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/interrupt/interd.wav"));
	~buffers.put(\intere, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/interrupt/intere.wav"));
	~buffers.put(\interf, Buffer.read(Server.default,"/Users/bjarni/Works/Context/Material/Buffers/interrupt/interf.wav"));

	~function = interpreter.compileFile("/Users/bjarni/Works/Context/Blocks/lectorate/Blect03.scd").value;

	SynthDef(\iSampleFiltering, {
		|out=0, buf=0, amp=0.3, rate=1, amp2=1.0, note2=60, pos=0, pan=0.0,
		atk=1.1, dec=0.5, sus=1.0, rel=0.5, gate=1, hpf=20,lpf=19999|
		var env, sig, offset, pitch;
		env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, amp, doneAction:2);
		pitch = BufRateScale.ir(buf) * rate * (note2 / 60.0);
		offset = BufFrames.kr(buf) * pos;
		sig = PlayBuf.ar(2, buf, pitch, 1, offset);
		sig = RLPF.ar(sig, lpf);
		sig = RHPF.ar(sig, hpf);
		sig = Pan2.ar(sig * env, pan, amp2);
		OffsetOut.ar(out, sig);
	}).add();

	SynthDef(\iSample, {
		|out=0, buf=0, amp=0.3, rate=1, amp2=1.0, note2=60, pos=0, pan=0.0, atk=1.1, dec=0.5, sus=1.0, rel=0.5, gate=1|
		var env, sig, offset, pitch;
		env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, amp, doneAction:2);
		pitch = BufRateScale.ir(buf) * rate * (note2 / 60.0);
		offset = BufFrames.kr(buf) * pos;
		sig = PlayBuf.ar(2, buf, pitch, 1, offset);
		sig = Pan2.ar(sig * env, pan, amp2);
		OffsetOut.ar(out, sig);
	}).add();

	SynthDef(\gb, {
	|out=0, amp=0.3, freq=100, pan=0.0, atk=1.1, dec=0.5, sus=1.0, rel=0.5, gate=1, mod=0.5,
	lop=18000, loprq=1|
	var env, sig;
	env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, doneAction:2);
	sig = Saw.ar(Lag.ar(GbmanL.ar(freq/4).range(freq/2,freq))).fold2(SinOsc.ar(20));
	sig = BBandStop.ar(sig, LFNoise2.ar(0.5).range(freq*0.5, freq * 2), 8);
	sig = RLPF.ar(sig, lop, loprq);
	sig = Pan2.ar(sig * env, pan) * amp * 0.8;
	OffsetOut.ar(out, sig);
	}).add();

	Context.loadSamplers(interpreter);

	~loadFolder = {|path|
		SoundFile.collect(path).collect { |sf| Buffer.read(Server.local, sf.path)}
	};

	~sbs = ~loadFolder.("/Users/bjarni/Works/Context/Material/Buffers/Bl02/*");
	" ** Context: loading complete".postln;
	}

	*interrupt {|name,freq=10|
	var localDist = (1.0.rand.asString ++ "dist").asSymbol;
	var sound = [~buffers[\intera],~buffers[\interb],~buffers[\interc],~buffers[\interd],~buffers[\intere],~buffers[\interf]].choose;
	var f1 = rrand(0.3, 2.5);
	var f2 = rrand(1.2, 3.5);

	Synth(\iSample, [\atk, 0.0, \buf, sound, \amp, 1]);

	Routine {
		Ndef(localDist, {
			var lpf=150, hpf=8000, gaina=1000, gainb=200, son = \in.ar([0,0]);
			HPF.ar(SoftClipAmp8.ar(son, pregain:gaina) * EnvGen.ar(Env([0.2,0.1,0.0], [6,4,10])), Lag.ar(LFNoise0.ar(f1).range(hpf, hpf*2))) +
			LPF.ar(SoftClipAmp8.ar(son, pregain:gainb) * EnvGen.ar(Env([0.2,0.1,0.0], [5,5,15])), Lag.ar(LFNoise0.ar(f2).range(lpf*0.25, lpf)));
		}).play;

		0.1.wait;

		Ndef(name).stop;
		Ndef(name) <>> Ndef(localDist);
		Ndef(localDist).play;
	}.play;

	localDist
}

*observe {|name,interpreter|
	var task = ~function.value(name);
	task.play;
	("** observing :" + name).postln;
}

*observeinter {|name|
	Context.interrupt(\craks);
	Context.interrupt(\verb);
	~runObserver = false;
}

*follow {|type=1|

	if(type == 1, {

		" ** following : [1]".postln;

		Pdef(\follow1, Pbind(
			\instrument, \iSampleFiltering, \buf, ~buffers[\blb3a], \lpf, Pbrown(100,15000,1000), \hpf, 40, \amp, 3,
			\dur, Pwhite(0.08, 0.16), \legato, Pwhite(0.2, 0.6), \pos, Env([0.0, 0.8, 0.4, 0.01],[30, 5, 2])
		)).play
	}, {
		" ** following : [2]".postln;
		Pdef(\follow2, Pbind(
			\instrument, \iSampleFiltering, \buf, ~buffers[\blb3a], \hpf, 40, \amp, 2,
			\dur, Pwhite(0.08, 0.16), \legato, 0.9, \pos, Env([0.0, 0.8, 0.99, 0.01],[30, 5, 2])
		)).play
	});
}

*unadapt { ~adapt = false; }

*adapt {

	~adapt = true;

	Tdef(\simal, {

		Ndef(\logic, { |freq = 60, index = 5, modMult = 25|
			var mod = Lag.ar(Logistic.ar(LFNoise1.kr(0.001,0.5,3.5), LFNoise1.kr(0.2,500,1000), 0.25, 0.5).range(freq, freq * modMult));
			var signal = Splay.ar(PMOsc.ar(Array.rand(8, freq/3, freq), mod, rrand(index, index * 2) ! 8,0, 0.02), 0.1);
			signal * EnvGen.ar(Env([0,1,0,1,0], [1,8,15,6]).circle) * (Gendy3.ar(freq:XLine.ar(10, 1000, 20)));
		});

		0.1.wait;

		Ndef(\logichpf, { HPF.ar( Ndef(\logic).ar, 2000 ) }).play;

		2.wait;

		" ** adapt to : [current]".postln;

		Ndef(\modyfreq, { LFNoise1.ar(0.001).range(2000, 2200) });
		Ndef(\modyfreq).fadeTime = 0.1;

		1.1.wait;

		Ndef(\dusttrig, { Impulse.ar(20) });
		Ndef(\dust, {
			var sig = Saw.ar(LFNoise2.ar([0.1, 0.15]).range(Ndef(\modyfreq), Ndef(\modyfreq)*1.01));
			sig = RLPF.ar(sig, 800) * 1.4 * EnvGen.ar(Env.perc(0.01, 0.01), Ndef(\dusttrig));
			HPF.ar(sig * EnvGen.ar(Env([0,1,0.2,1,0], [1,2,4,6]).circle), 20) * XLine.ar(0.0001, 1, 5)
		}).play;

		6.wait;

		Ndef(\logic, { |freq = 90, index = 5, modMult = 25|
			var mod = Lag.ar(Logistic.ar(LFNoise1.kr(0.001,0.5,3.5), LFNoise1.kr(0.2,500,1000), 0.25, 0.5).range(freq, freq * modMult));
			var signal = Splay.ar(PMOsc.ar(Array.rand(8, freq/3, freq), mod, rrand(index, index * 2) ! 8,0, 0.02), 0.5);
			signal * EnvGen.ar(Env([0,1,0,1,0], [1,8,15,6]).circle) * (Gendy3.ar(freq:XLine.ar(10, 1000, 20)));
		});

		3.wait;

		// // l o o p // //

		while({~adapt == true}, {
			Ndef(\logichpf, { HPF.ar( Ndef(\logic).ar, LFNoise1.ar(1/3).range(rrand(600,1200), rrand(3000, 6000)) ) });
			rrand(3,7).wait;
			Ndef(\dusttrig, { Impulse.ar(rrand(12,25)) });
			rrand(2,4).wait;

		});

		// e n d ///

		Ndef(\dust, {
			var sig = Saw.ar(LFNoise2.ar([0.1, 0.15]).range(Ndef(\modyfreq), Ndef(\modyfreq)*1.01));
			sig = RLPF.ar(sig, 800) * 1.15 * EnvGen.ar(Env.perc(0.01, 0.01), Impulse.ar(Line.kr(19, 5, 10)));
			HPF.ar(sig * EnvGen.ar(Env([0,1,0.2,1,0], [1,2,6,4]).circle), 20) * EnvGen.ar(Env([1,0.8,0.3,0.0], [4,8,16]))
		});

		3.wait;

		Ndef(\logic, { |freq = 80, index = 1, modMult = 25|
			var mod = Lag.ar(Logistic.ar(LFNoise1.kr(0.001,0.5,3.5), LFNoise1.kr(0.2,500,1000), 0.25, 0.5).range(freq, freq * modMult));
			var signal = Splay.ar(PMOsc.ar(Array.rand(8, freq/4, freq), mod, rrand(index, index * 2) ! 8,0, 0.02), 0.2);
			signal * EnvGen.ar(Env([0,1,0,1,0], [1,2,1,2]).circle) * (Gendy3.ar(freq:10) * 1.5) * EnvGen.ar(Env([1,0.8,0.3,0.0], [2,4,8]))
		});

}).play;

}

*residue {
	Pbind(\instrument, \iSampleFiltering, \lpf, 18000, \rate, rrand(0.8, 1.2), \buf, Prand(~sbs,rrand(3,5)), \amp, 1, \pos, Pwhite(0.0,0.6), \rel, 0.01, \dur, Pseq([0.2,0.4,0.1,0.2] * 8,inf)).play;
}

}
