TraceRecord {

	var <>interpreter, <>lib, <>buffers;
	var <>basePath = "/Users/bjarni/Works/PIECES/Traces";

	// ## section - init

	*new {|interpret|
		^super.new.init(interpret);
	}

	init {|interpret|
		interpreter = interpret;
		MIDIClient.init; MIDIIn.connectAll;
	}

	loadBuffers {
		buffers = Dictionary();
		buffers[\seed] = Buffer.read(Server.default, basePath ++ "/dust/dolico.wav");
		buffers[\gest1] = Buffer.read(Server.default, basePath ++ "/dust/gemum.wav");
		buffers[\gestfinevolve] = Buffer.read(Server.default, basePath ++ "/dust/gestfinevolve.wav");
		buffers[\adapt1] = Buffer.read(Server.default, basePath ++ "/dust/dustroom.wav");
		buffers[\adapt2] = Buffer.read(Server.default, basePath ++ "/dust/reps.wav");
		buffers[\dust1] = Buffer.read(Server.default, basePath ++ "/dust/dacalorm.wav");
		buffers[\dust2] = Buffer.read(Server.default, basePath ++ "/dust/doplim.wav");
		buffers[\dust3] = Buffer.read(Server.default, basePath ++ "/dust/dolocus.wav");
		buffers[\dust4] = Buffer.read(Server.default, basePath ++ "/dust/dustroom.wav");
	}

	addSynths {

		SynthDef(\detach, {
			|out=0, buf=0, amp=1, rate=1, pos=0, pan=1.0, atk=0.2, dec=0.5, sus=1.0, rel=2, gate=1, filter=1000, bw=6|
			var env, sig, offset;
			env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, amp, doneAction:2);
			rate = BufRateScale.ir(buf) * rate;
			offset = BufFrames.kr(buf) * pos;
			sig = HPF.ar(PlayBuf.ar(1,buf,rate, 1, startPos:offset), [40,20]);
			sig = BBandStop.ar(sig, filter, bw);
			OffsetOut.ar(out, sig * env * amp);
		}).add();

		SynthDef(\tracegb, {
			|out=0, amp=0.3, freq=100, pan=0.0, atk=1.1, dec=0.5, sus=1.0, rel=0.5, gate=1, mod=0.5, lop=18000, loprq=1|
			var env, sig;
			env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, doneAction:2);
			sig = Saw.ar(Lag.ar(GbmanL.ar(freq/4).range(freq/2,freq))).fold2(SinOsc.ar(20));
			sig = BBandStop.ar(sig, LFNoise2.ar(0.5).range(freq*0.5, freq * 2), 8);
			sig = HPF.ar(RLPF.ar(sig, lop, loprq), 800);
			sig = BPeakEQ.ar(sig, 19000, 8, -16, 2);
			sig = Pan2.ar(sig * env, pan) * amp;
			OffsetOut.ar(out, sig);
		}).add();

		SynthDef(\dustrace, {
			|out=0, buf=0, amp=1, mod=9, rate=1, pos=0, pan=1.0, atk=0.01, dec=0.5, sus=1.0, rel=0.5, gate=1,filt=1000|
			var env, sig, offset;
			env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, amp, doneAction:2);
			rate = BufRateScale.ir(buf) * rate;
			offset = BufFrames.kr(buf) * pos;
			sig = HPF.ar(BBandStop.ar( PlayBuf.ar(1,buf,rate,loop:1) < LFSaw.ar([mod, mod*1.3]), [filt,filt*1.3], 12), 40) * 0.5;
			OffsetOut.ar(out, sig * env * amp);
		}).add();
	}

	synthesis {

		Server.default.waitForBoot({
			interpreter.compileFile(basePath ++ "/ControllerRecord.scd").value;

			this.addSynths();
			this.loadBuffers();

			" ".postln;
			"###".postln;
			"###".postln;
			"### - synthesis starts -".postln;
			"###".postln;
			"###".postln;
			" ".postln;
		});
	}

	// ## section - utils

	report {|txt, ndef|
		("###" + txt + ":" + ndef).postln;
	}

	list {
		var nodes = Ndef.all.localhost.activeProxies.asArray.collect({|item|
		if(Ndef(item.asSymbol).monitor.isPlaying, { item }) });

		if(nodes.size > 0, {
			"### active nodes:".postln;
			nodes.do{|item| if(item.notNil, { item.postln }) };
		})
	}

	seed {
		^buffers[\seed];
	}

	mix {
		BGMixer(Server.default)
	}

	// ## section - observe

	listen {
		"### trace.listen".postln;
	}

	observe {
		"### trace.observe".postln;
	}

	adapt {|ndef, type=1, amp=1, freq=10, dur=10, atk=1, rel=3|

		var snd, name = ("adapt_" ++ type ++ "_" ++ ndef).asSymbol; name.postln;
		this.report("adapt", name);

		if(type == 1, { snd = buffers[\adapt1] }, { snd = buffers[\adapt2] });

		Ndef(name, {
			var sig = PlayBuf.ar(1, snd, loop:1);
			var thresh = 0.05;
			var compression = 0.1;
			var attack = 0.1;
			var release = 0.1;
			var comp = Compander.ar(sig, Ndef(ndef).ar, thresh, 1, compression, attack, release);
			var outpt = CombC.ar(comp * SinOsc.ar(freq).range(0.8,1.1), 0.1, 0.1, [1.4,0.8]);
			outpt * EnvGen.ar(Env([0,1,1,0],[atk, dur, rel])) * amp;
		}).play;

		^Ndef(name);
	}

	follow {|ndef|
		var name = ("follow_" ++ ndef).asSymbol; name.postln;
		this.report("follow", name);

		Tdef(name, {
			var amp = 1;
			var pattern = PbindProxy(
				\instrument, \tracegb, 'sus', 0.62, 'rel', 3, 'atk', 1.1,
				'modFreq', 194, 'dec', 0.29, 'curve', 0.9, 'freq', Pbrown(2800, 4200, 12),
				'amp', Pn(Plazy{ Penv([ 0.15, 1.4, 0.3, 0.01 ], [ rrand(4,9), rrand(6,10), rrand(3,6) ], 'sine') }),
				'dur', Pn(Plazy{ Pbrown(0.18, 0.4, rrand(0.002, 0.02), 24) })).play;
			8.wait;
			pattern.stop;
			3.wait;
			pattern.play;
			80.wait;
			pattern.stop;
			}).play;

		^Tdef(name);
	}

	crackle {|ndef, amp=0.7|
		var name = ("" ++ ndef).asSymbol; name.postln;
		this.report("crackle", name);
		Ndef(name, { PlayBuf.ar(2, buffers[\gestfinevolve]) * amp }).play;
		^Ndef(name);
	}

	detach {|ndef,dur=25|
		var local = ("detach_"++ndef).asSymbol;
		this.report("detach", local);

		Pdef(local, Pbind(
			\instrument, \detach,
			\buf, buffers[\gest1].bufnum,
			\amp, Pwhite(0.9, 1.5),
			\dur, Env([8, 1.8, 8], [dur*0.5, dur*0.5]),
			\atk, Env([1, 0.2, 1], [dur*0.5, dur*0.5]),
			\rel, Env([4, 2.9, 2], [dur*0.5, dur*0.5]),
			\rate, Pwhite(0.9, 1.1),
			\filter, Pif(Ptime(inf) < (dur*1.2), Pwhite(50, 5000)),
			\pos, Pseq([0, Pwhite(0.1, 0.6, inf)])
		)).play;

		^Pdef(local);
	}

	enact {|ndef|
		var localEffect = ("enact_"++ndef).asSymbol;
		this.report("enact", localEffect);

		Routine {
			Ndef(localEffect, {
			var sig, osc, thres, freq=1000, la, lb, lc, verb, effect;
			var envLo = 0.8, envHi = 0.99;
			var times = { rrand(0.5, 1.8) } ! 8;
			var mix = EnvGen.ar(Env([0, envLo, envHi, envHi/2, envLo, envLo/2, envHi, envLo, 0], times).circle);
			sig = Ndef(ndef).ar;
			verb = sig;
			8.do{verb=AllpassL.ar(verb,0.3,{0.1.rand+0.1}!2,5)};
			verb = verb.tanh;
			effect = BLowPass4.ar(verb * 0.5, LFNoise2.ar(0.15).range(500, 800), 0.2) + BHiPass4.ar(verb * 0.5, LFNoise2.ar(0.25).range(7000, 12000), 0.2);
			(effect * mix) + (sig * (1-mix)); }).play;
			1.wait;
			Ndef(ndef).stop;
		}.play;

		^Ndef(localEffect);
	}

	diffuse  {|ndef, time=120, amp=0.4|
		var name = ("diffuse_" ++ ndef).asSymbol;
		this.report("diffusing", name);

		Routine {
			Ndef(name, {|amp=0.4|
				var output, signal, verb, effect, del;
				signal = Mix( Ndef(ndef).ar);
				signal = signal + signal.fold2(SinOsc.ar(LFNoise2.ar(3).range(5,15)));
				verb = signal;
				8.do{verb=AllpassL.ar(verb,0.3,{0.2.rand+0.1}!2,5)};
				verb = verb.tanh;
				output = BLowPass4.ar(verb * 0.35, LFNoise2.ar(0.2).range(600,1200), 0.2);
				output = output + BHiPass4.ar(verb * 0.1, LFNoise2.ar(0.1).range(8000,12000), 0.2);
				output * amp;
			}).play(4,2);

			Ndef(name).set(\amp, amp);
			Ndef(name).fadeTime = 2;
			Ndef(name).play;
			(time * 0.5).wait;

			5.do {
				(time / 10).wait;
				Ndef(name).xset(\amp, rrand(amp * 0.8, amp * 1.1));
			};

			Ndef(name).xset(\amp, 0);
			3.wait;
			Ndef(name).stop;
		}.play

		^Ndef(name);
	}

	fold {|input1, input2|
		var effname = ("fold_"++input1++"_"++input2).asSymbol;
		this.report("fold", effname);

		Routine {
			Ndef(input1).stop;
			Ndef(input2).stop;
			Ndef(effname, {|lpf=100, hpf=8000|
				var snda = Ndef(input2).ar.fold2(LPF.ar(Ndef(input1).ar, lpf));
				var sndb = LPF.ar(Ndef(input2).ar.fold2(HPF.ar(Ndef(input1).ar, hpf)), hpf * 0.1) * 0.9;
				HPF.ar(snda + sndb, 30);
			}).play;
		}.play;

		^Ndef(effname)
	}

	lopass {|ndef, from=80, to=200, freq=1|
		var localEffect = ("lopass_"++ndef).asSymbol;
		this.report("lopass", localEffect);

		Routine {
			Ndef(localEffect, { LPF.ar(Ndef(ndef).ar, SinOsc.ar(freq).range(from, to)) * 1.2 }).play;
			1.wait;
			Ndef(ndef).stop;
		}.play;

		^Ndef(localEffect)
	}

	ring {|input1, input2, reject=2000|
		var effname = ("ring_"++input1++"_"++input2).asSymbol;
		this.report("ring", effname);

		Routine {
			Ndef(input1).stop;
			Ndef(input2).stop;
			Ndef(effname, {
				var snda = Ndef(input2).ar.mod(LPF.ar(Ndef(input1).ar, LFNoise2.ar(12).range(reject*0.5, reject*2)));
				var sndb = DelayC.ar(Ndef(input2).ar.mod(LPF.ar(Ndef(input1).ar, 4000)), 0.2, Lag.ar(LFNoise2.ar([13,14]).range(0.08, 0.15)));
				HPF.ar( BBandStop.ar( snda + sndb, reject, 6), 30) * 4;
			}).play;
		}.play;

		^Ndef(effname)
	}

	drift {|ndef|
		var localEffect = ("drift_"++ndef).asSymbol; localEffect.postln;
		this.report("drift", localEffect);
		Ndef(localEffect, {
			var sig, osc, verb, thres, freq=1000;
			var la, lb;
			var envLo = 0.3, envHi = 0.9;
			var times = { rrand(0.5, 1.8) } ! 8;
			var env = Env([0, envLo, envHi, envHi/2, envLo, envLo/2, envHi, envLo, 0], times.postln).circle;
			sig = Ndef(ndef).ar;
			sig * Lag.ar(EnvGen.ar(env.circle, timeScale:LFTri.ar(0.2).range(0.1, 2)));
		}).play;

		Ndef(ndef).stop;
		^Ndef(localEffect)
	}

	feed {|input1, input2|
		var effname = ("feed_"++input1++"_"++input2).asSymbol;
		this.report("feed", effname);

		Routine {
			Ndef(input1).stop;
			Ndef(input2).stop;
			Ndef(effname, {| feedback=0.8, delay=0.12|
				var sig, sigb, effect, loc;
				sigb = Amplitude.kr(Ndef(input1).ar);
				sig = (Ndef(input1).ar + Ndef(input2).ar);
				loc = LocalIn.ar(2);
				loc = BPF.ar(loc * feedback + sig, sigb.linlin(0.0,1.0,60,120), 4);
				loc = loc + (0.3 * HPF.ar(SoftClipAmp8.ar(loc, LFNoise2.ar(0.2).range(8,14)), LFNoise2.ar(0.1).range(100,8000)));
				loc = loc + RLPF.ar(loc, 100, 4);
				loc = DelayC.ar(loc, 1.8, [delay, delay * 1.01]);
				loc = HPF.ar(loc,60);
				loc = BPeakEQ.ar(loc, 78, 1, -12);
				LocalOut.ar(loc*0.98);
				Limiter.ar(loc*1.5)
			}).play;
		}.play;

		^Ndef(effname)
	}

	chain {|input1, input2|
		var effname = ("chain_"++input1++"_"++input2).asSymbol;
		this.report("chain", effname);

		Routine {
			Ndef(input1).stop;
			Ndef(input2).stop;
			Ndef(effname, {
				var thresh = 0.05;
				var compression = 0.1;
				var attack = 0.1;
				var release = 0.1;
				var snd = Compander.ar(Ndef(input1).ar, Ndef(input2).ar, thresh, 1, compression, attack, release);
				snd + (Ndef(input2).ar * LFNoise2.ar(rrand(1,10)).range(-0.8, 0.8))
			}).play;
		}.play;

		^Ndef(effname)
	}

	switcha {|input1, input2, dur=7|
		var effname = ("switcha_"++input1++"_"++input2).asSymbol;
		this.report("switcha", effname);

		Routine {
			Ndef(input1).stop;
			Ndef(input2).stop;
			Ndef(effname, {
			var son = HPF.ar(Ndef(input1).ar, XLine.ar(400,19999,dur));
			son + LPF.ar(Ndef(input2).ar, XLine.ar(100,19999,dur*0.6));
		}).play
		}.play;

		^Ndef(effname)
	}

	switchb {|input1, input2, dur=2, freq=10|
		var effname = ("switchb_"++input1++"_"++input2).asSymbol;
		this.report("switchb", effname);

		Routine {
			Ndef(input1).stop;
			Ndef(input2).stop;
			Ndef(effname, {
				var son = Ndef(input1).ar * SinOsc.ar(Line.ar(freq,0.1,dur)).range(Line.ar(1.0,0.0,dur), 1.0) * Line.kr(1,0,dur*3);
				son + (Ndef(input2).ar * Line.kr(0,1,dur)) * SinOsc.ar(freq).range(XLine.ar(0.01,1.0,dur), 1.0);
			}).play
		}.play;

		^Ndef(effname)
	}

	verb {|ndef|
		var localEffect = ("verb_"++ndef).asSymbol;
		this.report("verb", localEffect);

		Routine {
			Ndef(localEffect, { FreeVerb.ar(Ndef(ndef).ar * LFNoise2.ar(0.1).range(0.3,1.1), 0.4, 0.9, 0.1) * 2 }).play;
			1.wait;
			Ndef(ndef).stop;
		}.play;

		^Ndef(localEffect)
	}

	mod {|ndef, freq=15|
		var localEffect = ("mod_"++ndef).asSymbol;
		this.report("verb", localEffect);

		Routine {
			Ndef(localEffect, { Ndef(ndef).ar * SinOsc.ar([freq, freq*1.1]) }).play;
			1.wait;
			Ndef(ndef).stop;
		}.play;

		^Ndef(localEffect)
	}

	freqshift {|ndef|
		var localEffect = ("freqshift_"++ndef).asSymbol;
		this.report("freqshift", localEffect);

		Routine {
			Ndef(localEffect, {
				var src = Ndef(ndef).ar;
				var envHi = 1.2, envLo = 0.3;
				var dst = (src * SinOsc.ar( LFNoise1.ar(0.2).range(10,50).max(10).min(50)).range(0.1, 0.9)) +
				FreqShift.ar(src, LFNoise1.ar(20).range(60,100).max(60).min(100));
				var env = Env([envLo, envLo * 1.1, envHi, envHi * 0.8, envHi * 0.9, envLo, envLo * 1.1, envHi, envHi * 0.8, envHi * 0.9], { rrand(0.1, 1.2) } ! 9);
				dst * EnvGen.ar(env.circle) }).play;
			1.wait;
			Ndef(ndef).stop;
		}.play;

		^Ndef(localEffect)
	}

	dualdist {|ndef|
		var localEffect = ("dualdist_"++ndef).asSymbol;
		this.report("dualdist", localEffect);

		Routine {
			Ndef(localEffect, {
				var lpf=150, hpf=8000, gaina=1000, gainb=200, f2=2;
				var son = Ndef(ndef).ar;
				var output =
				HPF.ar(SoftClipAmp8.ar(son, pregain:gaina), Lag.ar(LFNoise0.ar(LFTri.ar(0.1).range(2,16)).range(hpf, hpf*2))) * 0.5 +
				(LPF.ar(SoftClipAmp8.ar(son, pregain:gainb), Lag.ar(LFNoise0.ar(f2).range(lpf*0.25, lpf))) * 0.8);
				HPF.ar(output * 0.4, 40)
			}).play;
			1.wait;
			Ndef(ndef).stop;
		}.play;

		^Ndef(localEffect)
	}

	dust {|type=0, time=5, rate=1, amp=0.8, durFrom=0.1, durTo=0.9, modFrom=0.1, modTo=8|
		var buf, name = ("dust_" ++ type).asSymbol; name.postln;
		this.report("dust", name);

		switch (type,
			0, { buf = buffers[\dust1] },
			1, { buf = buffers[\dust2] },
			2, { buf = buffers[\dust3] },
			3, { buf = buffers[\dust4] });

		Pdef(name, Pbind(\instrument, \dustrace, \rate, rate, \buf, buf, \pos, Pif(Ptime(inf) < time,
			Pwhite(0.0,0.5)), \mod, Pwhite(modFrom, modTo),
		\amp, Prand([1.4,1.2,1.6,0,0,0],inf) * amp, \dur, Pwhite(durFrom,durTo), \out, 24)).play;

		^Pdef(name)
	}

	plane {|ndef|
		var localEffect = ("plane_"++ndef).asSymbol;
		this.report("plane", localEffect);

		Ndef(localEffect, {
			var size = 3, from = 1, to = 25, modFreq = 17, modFrom = 15, modTo = 22, sd = SampleDur.ir * 0.25;
			var amp = 1 - Amplitude.ar(Ndef(ndef).ar);
			var snd = SinOsc.ar(50 + SinOsc.ar(10).range(5,8), 0 , 0.3);
			snd = snd + DemandEnvGen.ar(
				Dseq([1, [-1, 0.5]], inf),
				Dseq(Array.rand(size,from,to), inf) * sd
			).mod(SinOsc.ar(LFNoise0.ar(modFreq).range(modFrom, modTo))) * 0.15;
			snd = BBandStop.ar(snd, LFNoise2.kr(0.2).range(100, 4000));
			snd * amp * EnvGen.ar( Env([0,1,0.1,0.8,0.2,1,0], { rrand(3.8, 7.2) } ! 6, [6,-6]).circle )
		}).play;

		^Ndef(localEffect)
	}
}
