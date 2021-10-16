VV {

	*new {|interpreter|
		^super.newCopyArgs().init(interpreter);
	}

	init {|interpreter|

	}

  *load {|interpreter|

	Server.default.waitForBoot({

		{

		Superpang.load(interpreter);

		~myBus = Bus.control; { Out.kr(~myBus, LFNoise2.kr(0.4).exprange(0.9, 1.0)) }.play;
		~myBusDur = Bus.control; { Out.kr(~myBusDur, LFDNoise3.kr(SinOsc.ar(0.01).range(0.01,10)).range(0.05, 0.3)) }.play;

		SynthDef(\smp, {
		|out=0, buf=0, amp=1, rate=1.02, pos=0, pan=1.0, atk=0.2, dec=0.5, sus=1.0, rel=2, gate=1|
			var env, sig, offset;
			env = EnvGen.kr(Env.adsr(atk, dec, sus, rel), gate, amp, doneAction:2);
			rate = BufRateScale.ir(buf) * rate;
			offset = BufFrames.kr(buf) * pos;
			sig = PlayBuf.ar(2,buf,rate,1,offset);
			OffsetOut.ar(out, sig * env * amp);
		}).add;

		~sb = Buffer.alloc(Server.default, 512);
		Server.default.sync;

		SynthDef(\vvi, { |item=0, rate=1, start=0, amp=0.75, dur=0.3, pan=0, bassFreq=50, bassAmp=0.25, bassAtk=0.1, bassRel=0.1, bassFb=0.3|
			var sig, shape, env, pos, onsets, trigger, bleeps, chain;
			shape = Env([0, amp, amp, 0], [dur*0.01, dur*0.89, dur*0.1]);
			env = EnvGen.ar(shape, doneAction: 2);
			pos = start * BufSamples.ir(item);
			sig = PlayBuf.ar(1, item, rate * BufRateScale.ir(item), 1, pos, 0);
			chain = FFT(~sb, sig);
			onsets = Onsets.kr(chain, MouseX.kr(0,1), \complex);
			trigger= SendTrig.kr(onsets);
			bleeps = SinOscFB.ar(bassFreq, bassFb, EnvGen.kr(Env.perc(bassAtk, bassRel, bassAmp), onsets));
			bleeps = HPF.ar(bleeps, 20);
			OffsetOut.ar(4, ((sig * EnvGen.kr(Env.perc(0.01, 0.05, 1.5)))).dup * env);
			OffsetOut.ar(2, HPF.ar(bleeps * 0.8,40).dup * env);
		}).add;

		~m = Buffer.read(Server.default, "/Users/bjarni/Music/momentaries.wav");

		Pdef(\rhythm).fadeTime = 0; Ndef(\sound).fadeTime = 3; NF(\vsync, {|freq=1| freq });

		Server.default.sync;

		"## VV.load complete".postln;

		}.fork

		})
	}

	items {
		^Pseq([~sclicks.["d"].at(3)],inf);
	}

	follow {
		Pdef(\rhythm2, Pbind(
		\instrument, \sritem,
		\buf,  [~sclicks.["f"].at(1)],
		\rate, Pwhite(0.25, 2),
		\dur, Pfunc{|ev| ~myBusDur.getSynchronous * 0.5 },
		\amp, Pseq([ Pbrown(0.01, 0.3, 0.01,10), Pn(0,12) ],inf) * 2.2
		)).play(quant:1)
	}

	*onsets {|ina, inb, inc|

		if(inc[0] == 2, {
			" Yes it XXX".postln;
			~bb0.value;
		});

		if(inc[0] == 3, {
			" Yes it YYY".postln;
			~bc0.value;
		});

	}

}

Relate {
	pulse {|rate|
		^Pfunc{|ev| ~myBusDur.getSynchronous }
	}
	*sync {|pat|
		Pbindef(pat, \sync, Pfunc{|ev| Ndef(\vsync).set(\freq, ev.dur.reciprocal)})
	}
	*variate{
		{
		~bc1.value;
		rrand(4,6).wait;
		~bd1.value;
		rrand(4,6).wait;
		~ba2.value;
			rrand(4,6).wait;
~bb2.value;
			rrand(4,6).wait;
~bc2.value;
			rrand(4,6).wait;
~bd2.value;
			rrand(4,6).wait;
		}.fork

	}
}

Behaviour {
	drift {
		^Pn(Plazy({ Pseq([ Pfin(rrand(8,18), Pfunc{|ev| ~myBus.getSynchronous }), Pseq([0],rrand(14,15)) ]) }));
	}
	*decorate {|pat, type|

		if(type == "texture", {
			"we should hear texture".postln;
			Pbindef(pat, \texture, Pfunc{ ~x.set(\gate, 0); ~x = Synth(\smp, [\buf, ~m, \pos, rrand(0.3,0.32), \amp, 0.5, \out, 12]); })
		});

		if(type == "dust", {

			" Yes it DUST".postln;

			{
			~bd0.value;
			4.wait;
			~ba1.value;
			6.wait;
			~bb1.value;
			}.fork

		});
	}
}

Vblip : UGen {
	*ar {arg freq = 4000, lpf=800, mul=1.0, add=0.0;
		var out = RLPF.ar(Blip.ar( LFNoise2.ar([0.1, 0.15]).range(freq, freq*1.1)), lpf);
		^out.madd(mul, add)
	}
}