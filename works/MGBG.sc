MGBG {

	*load {|interpreter, path="/Users/bjarni/Desktop/_/mgbg-mengi/to-load.scd"|
		var t, i;
		interpreter.compileFile(path).value;
		t = Trace(interpreter);
		i = Intervention();
		t.synthesis();
		^ [t, i];
	}

	*ticks {
		arg count = 10, // how many phonems
		amp = 0.5,
		overlap = 0.1, // smaller number means more overlap
		rate = 1,
		instrument = \itemst; // choose from \item, \am, \pink, \dist

		Pdef(\p1,
			Pbind(
				\instrument, instrument,
				\buf, Prand(~clicks, count),
				\dur, Pfunc{|ev| ev.buf.duration * overlap},
				\amp, Pfunc{amp},
				\rate, Pfunc{rate}
			)
		).play
	}

	*brown {
		arg count = 16,
		overlap = 0.4,
		step = 2,
		start = 0.1, // 0.0 means from beginning and 1.0 is end
		amp = 0.4,
		rate = 1,
		instrument = \dist; // choose from \item, \am, \pink, \dist

		var st = (~impulses.size * start).asInt;

		Pdef(\p3,
			Pbind(
				\instrument, instrument,
				\index, Pbrown(st, ~impulses.size-1, step, count),
				\buf, Pfunc {|ev| ~impulses.wrapAt(ev.index) },
				\durmult, Pbrown(overlap/2, overlap*2, overlap/10),
				\dur, Pfunc{|ev| ev.buf.duration * ev.durmult},
				\amp, Pfunc{amp},
				\rate, Pfunc{rate}
			)
		).play
	}
}
