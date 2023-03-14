MPKmini : AkaiMIDI {

	*new{|deviceId|
		^super.new.init(deviceId);
	}

	init {|inDeviceId|
		deviceId = inDeviceId;

		// TODO: Make accessible in case they change
		knobs = Array.series(8, 1);
		toggles = Array.series(8, 9);
		keys = Array.series(25, 48);
	}

	/* tests the controls by printing */

	test {

		/* MPKmini, Preset 4, red bank red cc. */

		var toggleOffset = 9;
		var keysOffset = 48;

		MPKmini.initMidi();

		// Toggles
		8.do{|index|
			var toggle = toggleOffset + index;
			MIDIFunc.cc({|val| (testTogglePrepend + (index + 1)).postln}, toggle);
		};

		// Knobs
		8.do{|index|
			var knob = index + 1;
			MIDIFunc.cc({|val| (testKnobPrepend +  knob).postln}, knob);
		};

		// Keys
		25.do{|index|
			var key = keysOffset + index;
			MIDIFunc.noteOn({|val| (testKeyPrepend + (index + 1)).postln}, key);
		};
	}

	patterns {|patterns, specs, arguments|

		super.patterns(patterns, specs, arguments);

		// Keys
		keys.do{|key, index|
			MIDIFunc.noteOn({|val| (testKeyPrepend + (index + 1)).postln}, key);
		};
	}

	splitSampler {|names, specs, defaults, buffers, intervals, output=0|
		var items = Array.newClear(names.size), setParamValue;

		"INSIDE splitSampler".postln;

		names.postcs;

		names.do{|name, index|
			var item = ();
			item = ();
			item.name = name;
			item.notes = Array.newClear(128);
			item.active = false;
			item.specs = specs[name];
			item.params = Dictionary();
			item.buffers = buffers[index];
			item.step = (120/item.buffers.size).ceil;
			item.defaults = defaults[index];
			item.interval = intervals[index];

			("Name" + name).postln;

			MIDIFunc.cc({|value|
				"Setting active".postln;
				item.name.postln;
				if(value < 1,
					{ item.active = false },
					{ item.active = true }
				);

			}, toggles[index], nil, deviceId);

			// Specs dynamically added with controls
			if(item.specs.notNil, {
				item.specs.do {|itemSpec, specIndex|
					MIDIFunc.cc({|val|
						var param = itemSpec[0];
						var spec = itemSpec[1];
						var map = spec.map(val/127.0);
						setParamValue.value(item, index, param, map);
						item.params[param] = map;
					}, specIndex + 1);
				};
			});

			items[index] = item;
		};

		setParamValue = {|item, index, param, value|
			if(item.active, {
				("slot:" ++ (index + 1) ++ "," + item.name.asString + ":" + param + ":" + value).postln;
				item.notes.do{|note|
					if (note.notNil, { note.set(param, value) });
				};
			});
		};

		// Start a synth and store in array
		MIDIFunc.noteOn({|vel, num|
			var freq = num.midicps;
		    var amp2 = vel.linlin(0, 127, 0.05, 3.5);

			// Start synths with previous params
			items.do {|item|

				/*"**** Start synths with previous params *****".postln;
				(item.step).postln;
				(item.interval).postln;
				(item.params).postln;
				(item.params.keys.size).postln;*/

				if(item.active && item.notes[num].isNil && (item.interval.isNil == false), {
					var index = (num/item.step).floor;
		    		var base = 60 - (item.interval * (item.step/2).floor);
		    		var note = Array.fill(item.step, {|i| base + (i * item.interval)}).wrapAt(num);
	        		var args = Array.new(item.params.keys.size * 2);
	        		index = index.min(item.buffers.size);

					item.params.keysValuesDo {|key, value|
						args.add(key);
						args.add(value);
					};

					item.name.postln;
					item.notes[num] = Synth(item.name,
						[\buf, item.buffers[index], \amp2, amp2, \note2, note.postln, \out, output] ++ args ++ item.defaults);

				})
			};
		}, nil, nil, deviceId);

		// Stop the synth from arrayed location
		MIDIFunc.noteOff({ |vel, num|
			items.do {|item|
				if (item.notes[num].notNil, {
					item.notes[num].set(\gate, 0);
					item.notes[num] = nil
				})
			}
		}, nil, nil, deviceId);
	}

	/* Simple synth takes the name of a synth and array with specs */

	// TODO: Needs a fix if we want to use preset 4 etc ...

	simpleSynth {|name, specs, defaults, firstFader = 100|
		var amp=0.5, notes=Array.newClear(128), params=Dictionary(), setParamValue;

		setParamValue = {|param, value|
			notes.do{|note|
				if (note.notNil, {note.set(param, value);});
			};
		};

		// First fader always used for amp
		MIDIFunc.cc({|val|
			amp = val / 127.0;
			setParamValue.value(\amp, amp);
		}, firstFader, nil, deviceId);

		// Specs dynamically added with controls
		if(specs[name].notNil, {
			specs[name].do {|item, index|
				MIDIFunc.cc({|val|
					var param = item[0];
					var spec = item[1];
					var map = spec.map(val/127.0);
					setParamValue.value(param, map);
					params[param] = map;
				}, index + 2);
			};
		}, nil, nil, deviceId);

		// Start a synth and store in array
		MIDIFunc.noteOn({|vel, num|
			var freq = num.midicps;
			var args = Array.new(params.keys.size * 2);

			"START".postln;

			// Start synth with previous params
			params.keysValuesDo {|key, value|
				args.add(key);
				args.add(value);
			};

			if (notes[num].isNil,{
				notes[num] = Synth(name, [\freq, freq, \amp, amp] ++ args ++ defaults);
			})
		}, nil, nil, deviceId);

		// Stop the synth from arrayed location
		MIDIFunc.noteOff({ |vel, num|
			if (notes[num].notNil,{
				notes[num].set(\gate, 0);
				notes[num] = nil
			})
		}, nil, nil, deviceId);
	}
}