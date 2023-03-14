PatternTest {

	*rapid {|testcase|
		("Test, rapid, playing for" + testcase).postln;

		Pbindef(
		    \unittest,
		    \instrument, testcase,
			\legato, 0.5,
			\atk, Pwhite(0.05, 0.1),
			\dec, 0.02,
			\sus, Pwhite(0.05, 0.1),
			\rel, Pwhite(0.1, 0.3),
			\dur, Pseq([0.01, 0.15], inf),
			\freq, Pseq([Pn(400, 20), Pn(2000, 20), Pn(100, 20), Pn(4000, 20)], inf),
			\amp, 0.6
		).play
	}

	*slow {|testcase|
		("Test, slow, playing for" + testcase).postln;

		Pbindef(
		    \unittest,
		    \instrument, testcase,
			\legato, 0.9,
			\atk, Pwhite(0.4, 1.2),
			\dec, 1,
			\sus, Pwhite(1, 2),
			\rel, Pwhite(0.8, 2.5),
			\dur, Pseq([1.5, 3], inf),
			\freq, Pseq([Pn(400, 10), Pn(2000, 10), Pn(100, 10), Pn(4000, 10)], inf),
			\amp, 0.6
		).play
	}

	*mixed {|testcase|
		("Test, mixed, playing for" + testcase).postln;

		Pbindef(
		    \unittest,
		    \instrument, testcase,
			\legato, 0.9,
			\atk, Pwhite(0.01, 1.2),
			\rel, Pwhite(0.3, 2.5),
			\dur, Pwhite(0.1, 2.5),
			\freq, Pwhite(40, 1000),
			\amp, Pwhite(0.2, 0.8)
		).play
	}

	*risefall {|testcase|
		("Test, risefall, playing for" + testcase).postln;

		Pbindef(
		    \unittest,
		    \instrument, testcase,
			\legato, 0.9,
			\atk, 0.1,
			\dec, 1,
			\sus, 1,
			\rel, Pwhite(0.2, 0.3),
			\dur, 0.2,
			\freq, Pseq([Pseries(100, 100, 40), Pseries(4000, -100, 10)],inf).trace,
			\amp, 0.6
		).play
	}

	*chords {|testcase|
		("Test, chords, playing for" + testcase).postln;

		Pbindef(
		    \unittest,
		    \instrument, testcase,
			\legato, 0.9,
			\atk, 0.1,
			\rel, Pwhite(0.2, 0.3),
			\dur, 1,
			\freq, Pfunc{ Array.rand(3, 100, 1500) },
			\amp, 0.6
		).play
	}
}