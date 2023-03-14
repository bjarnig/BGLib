BBItemFactory {

	*sampleProxy {|item, instrument, buffer|

		^PbindProxy(
			\instrument, instrument,
			\buf, buffer,
			\ampp, 1,
			\durp, 1,
			\posp, 0,
			\atk, 0.1,
			\rel, 0.2,
			\rate, Pfunc { item.arguments[\rate] },
			\posc, Pfunc { item.arguments[\pos] },
			\durc, Pfunc { item.arguments[\dur] },
			\ampc, Pfunc { item.arguments[\amp] },
			\dur, Pkey(\durp) * Pkey(\durc),
			\amp, Pkey(\ampc) * Pkey(\ampp),
			\pos, Pfunc {|ev| (ev.posp * ev.posc).min(0.95) },
			\sus, Pfunc {|ev| ev.dur * 0.8},
			\bus, 0
		);
	}

	*sampleProxySpecs {
		^[[\amp, ControlSpec(0.001, 4.0, \exp, 0.001, 1)],
		[\rate, ControlSpec(0.1, 4, \lin, 0.01, 1)],
		[\pos, ControlSpec(0.0, 4.0, \lin, 0.01, 1)],
		[\dur, ControlSpec(0.1, 8, \lin, 0.1, 1)]];
	}

	*synthProxy {|item, instrument|

		^PbindProxy(
			\instrument, instrument,
			\atk, 0.1,
			\rel, 0.2,
			\ampp, 1,
			\durp, 1,
			\freqp, 500,
			\rate, Pfunc { item.arguments[\rate] },
			\posc, Pfunc { item.arguments[\pos] },
			\durc, Pfunc { item.arguments[\dur] },
			\ampc, Pfunc { item.arguments[\amp] },
			\freq, Pkey(\freqp) * Pkey(\rate),
			\dur, Pkey(\durp) * Pkey(\durc),
			\amp, Pkey(\ampc) * Pkey(\ampp),
			\sus, Pfunc {|ev| ev.dur * 0.8},
			\bus, 0
		);
	}

	*synthProxySpecs {
		^[
			[\amp, ControlSpec(0.001, 4.0, \exp, 0.001, 1)],
			[\rate, ControlSpec(0.1, 4, \lin, 0.01, 1)],
			[\dur, ControlSpec(0.1, 4, \lin, 0.1, 1)],
			[\mod, ControlSpec(0.0, 0.9, \lin, 0.001, 0.0)]
		];
	}

	*create {|type, instrument, buffer|
		var item = BBItem();

		if(type == \default, {
			item.specs = BBItemFactory.sampleProxySpecs();
			item.specsToArguments();
			item.pattern = BBItemFactory.sampleProxy(item, instrument, buffer);
		});

		if(type == \synthesis, {
			item.specs = BBItemFactory.synthProxySpecs();
			item.specsToArguments();
			item.pattern = BBItemFactory.synthProxy(item, instrument, buffer);
		});

		^item;
	}

}