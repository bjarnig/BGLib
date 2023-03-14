Intervention {

	var <>interpreter, <>clicks;
	var <>basePath = "/Users/bjarni/Works/PIECES/Traces/dust/interrupt/";

	// ## section - init

	*new {
		^super.new.init();
	}

	init {
		var list = ["intera", "interb","interc", "interd","intere", "interf"];
		clicks = Dictionary();

		Routine {
			Server.default.waitForBoot({
				list.do {|item|
					var path = basePath ++ item ++".wav";
					var buf = Buffer.read(Server.default, path);
					Server.default.sync;
					clicks[item.asSymbol] = buf;
				}
			});
		}.play;

		SynthDef(\intervene, {
			|out=0, buf=0, amp=0.3, rate=1, amp2=1.0, note2=60, pos=0, pan=0.0, atk=1.1, sus=1.0, rel=0.5, gate=1, modFreq=1|
			var env, sig, offset, pitch;
			env = EnvGen.kr(Env([0,1,1,0],[atk, sus, rel]), gate, amp, doneAction:2);
			pitch = BufRateScale.ir(buf) * rate * (note2 / 60.0);
			offset = BufFrames.kr(buf) * pos;
			sig = HPF.ar(PlayBuf.ar(2, buf, pitch, startPos:offset).amclip(LFNoise1.ar([modFreq, modFreq*2])),40);
			sig = Pan2.ar(sig * env, pan, amp2);
			OffsetOut.ar(out, sig);
		}).add();
	}

	report {|txt, ndef|
		("###" + txt + ":" + ndef).postln;
	}

	clear {
		Synth(\intervene, [\atk, 1.0, \buf, clicks.asList().choose, \amp, 1, \modFreq, rrand(1,2000), \note, rrand(55,65)]);
		Ndef.clear;
	}

	// ## section - interventions

	interrupt {|name, freq=10|
		var localDist = ("interrupt_" ++ name).asSymbol;
		var f1 = rrand(2.3, 7.5), f2 = rrand(3.2, 8.5);
		var click = Synth(\intervene, [\atk, 0.0, \buf, clicks.asList().choose, \amp, 1, \modFreq, rrand(1,2000), \note, rrand(55,65)]);
		this.report("interrupt", localDist);

		Routine {
			Ndef(localDist, {
				var lpf=150, hpf=8000, gaina=1000, gainb=200, son = \in.ar([0,0]);
				HPF.ar(SoftClipAmp8.ar(son, pregain:gaina) * EnvGen.ar(Env([0.2,0.1,0.0], [3,4,15])), Lag.ar(LFNoise0.ar(f1).range(hpf, hpf*2))) +
				BBandStop.ar(LPF.ar(SoftClipAmp8.ar(son, pregain:gainb) * EnvGen.ar(Env([0.2,0.1,0.0], [2,5,15])), Lag.ar(LFNoise0.ar(f2).range(lpf*0.25, lpf))),80, 0.1);
			}).play;

			0.1.wait;
			Ndef(name).stop;
			Ndef(name) <>> Ndef(localDist);
			Ndef(localDist).play;

			25.wait;
			Ndef(localDist).stop;
			click.free;
		}.play;

		^Ndef(localDist)
	}

	drop {|name|
		var local = ("drop_" ++ name).asSymbol, dur = rrand(12, 14);
		Synth(\intervene, [\atk, 0.0, \buf, clicks.asList().choose, \amp, 1, \modFreq, rrand(1,2000), \note, rrand(55,65)]);
		this.report("drop", local);

		Routine {
			Ndef(local, {
				var son = \in.ar([0,0]);
				var modTo = EnvGen.ar(Env(Array.geom(4, rrand(6,10), rrand(2,4)), [dur/2,dur/4,dur/6]));
				var filter = EnvGen.ar(Env(Array.series(4, rrand(1000,5000), rrand(1500,4500)), [dur/2,dur/4,dur/6]));
				var hpfSound = HPF.ar(son.tanh.distort, LFPar.ar( LFNoise1.ar(rrand(1,10)).range(modTo/4,modTo)).range(filter/4,filter));
				CombC.ar(hpfSound, 0.8, 0.2, 0.4) * EnvGen.ar(Env([1,0.75,0.5,0.0], [dur/6,dur/4,dur/2]));
			}).play;

			0.1.wait;
			Ndef(name) <>> Ndef(local);
			Ndef(local).play;
			Ndef(name).stop;

			dur.wait;
			Ndef(local).stop;
		}.play;

		^Ndef(local)
	}

	disturb {|name|
		var local = ("disturb_" ++ name).asSymbol;
		var dur = rrand(11, 18);
		this.report("disturb", local);
		Synth(\intervene, [\atk, 0.0, \buf, clicks.asList().choose, \amp, 0.8, \modFreq, rrand(1,2000), \note, rrand(55,65)]);

		Routine {
			Ndef(local, {
				var son = \in.ar([0,0]);
				var feedback = 0.9;
				var freq = LFNoise1.kr(XLine.kr(3,40,dur)).range(1000,8000);
				var q =  XLine.kr(4,1,dur);
				var sig = Fb({|fbSig| BPF.ar(fbSig * feedback + son, freq, q).tanh; }, 0.2);
				sig = LPF.ar(son, 80) + sig;
				sig = sig * EnvGen.ar(Env([1,0.8,0.5,0.0], [dur/2,dur/3,dur/4]));
				sig = XFade2.ar(sig, sig * LFNoise0.ar(28).range(0.2,1.2), XLine.kr(-1, 1, dur/3));
				sig * EnvGen.ar(Env([1,0.75,0.5,0.0], [dur/2,dur/2,dur/2])) * 0.4;
			}).play;

			0.1.wait;
			Ndef(name) <>> Ndef(local);
			Ndef(local).play;
			Ndef(name).stop;

			(dur*2).wait;
			Ndef(local).stop;
		}.play;

		^Ndef(local)
	}

	halt {|name|
		var local = ("halt_" ++ name).asSymbol;
		var dur = rrand(7, 9);
		this.report("halt", local);
		Synth(\intervene, [\atk, 0.0, \buf, clicks.asList().choose, \amp, 1, \modFreq, rrand(1,2000), \note, rrand(55,65)]);

		Routine {
			Ndef(local, {
				var son = \in.ar([0,0]);
				var inFreq = Tartini.kr(son)[0];
				var signal = LeastChange.ar(son, BBandStop.ar(Gendy1.ar(minfreq:inFreq/2,maxfreq:inFreq/3), 2000, 0.01));
				HPF.ar(signal * 0.1, XLine.ar(40, 2000, dur)) * EnvGen.ar(Env([1,0.75,0.5,0.0], [dur/2,dur/2,dur/4]));
			}).play;

			0.1.wait;
			Ndef(name) <>> Ndef(local);
			Ndef(local).play;
			Ndef(name).stop;

			(dur * 1.5).wait;
			Ndef(local).stop;
		}.play;

		^Ndef(local)
	}

	block {|name, inFreq = 100|
		var local = ("block_" ++ name).asSymbol;
		var dur = rrand(7, 11);
		this.report("block", local);
		Synth(\intervene, [\atk, 0.0, \buf, clicks.asList().choose, \amp, 1, \modFreq, rrand(1,2000), \note, rrand(55,65)]);

		Routine {
			Ndef(local, {
				var son = \in.ar([0,0]);
				var side = Limiter.ar((son.squared + (50*son))/(son.squared + ((50-1)*son) + 1), 0.7);
				var side2 = MoogFF.ar(side, LFNoise2.ar(inFreq/Line.kr(1, 40, dur)).range(inFreq,inFreq*4));
				var signal = Compander.ar(son, side2, 0.1, 1, 0.1, 0.01, 0.1);
				var hpfSound = HPF.ar(signal.fold2(SinOsc.ar(inFreq/rrand(20,40))) + side2, XLine.ar(40, 8000, dur));
				hpfSound * EnvGen.ar(Env([1,0.75,0.5,0.0], [dur/2,dur/2,dur/4]));
			}).play;

			0.1.wait;
			Ndef(name) <>> Ndef(local);
			Ndef(local).play;
			Ndef(name).stop;

			(dur * 1.5).wait;
			Ndef(local).stop;
		}.play;

		^Ndef(local)
	}
}
