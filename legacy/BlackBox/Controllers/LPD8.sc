LPD8 : AkaiMIDI {

	/* LPD8 - PRESET 2, CC */

	*new{|deviceId|
		^super.new.init(deviceId);
	}

	init {|inDeviceId|
		deviceId = inDeviceId;
		triggers = [35, 36, 42, 39, 37, 38, 46, 44];
		toggles =  [ 1, 2, 3, 4, 5, 6, 8, 9 ];
		knobs = Array.series(8, 10);
	}

	test {

		triggers.do {|item, index|
			var n = index + 1;
			MIDIFunc.noteOn({
				(testTriggerPrepend + n).postln;
				}, item, nil, deviceId);
			};

		toggles.do {|item, index|
			var n = index + 1;
			MIDIFunc.cc({
				(testTogglePrepend + n).postln;
				}, item, nil, deviceId);
			};

		knobs.do {|item, index|
			var n = index + 1;
			MIDIFunc.cc({
				(testKnobPrepend + n).postln;
				}, item, nil, deviceId);
		};
	}

	epoc {|items, presets, akaiDeviceID = -1271054120, release = 1.2|
		var presetNotes = [35, 36, 42, 39, 37, 38, 46, 44];
		var triggerCC = [1, 2, 3, 4, 5, 6, 8, 9];
		var objects = Array.new(items.size), active = Array.newClear(items.size);

		/* Initialize objects */

		items.do {|item|

			if(item.notNil, {
				item.postln;
				item.update;
				item.initObject;
				objects.add(item.object);
				active.add(0);
				// s.sync;
			})
		};


		/* MIDI Start/Stop */

		MIDIFunc.cc({|vel, num|
			var index = 0; //  = num-1;

			triggerCC.do {|n, i|
				if(n == num, {index = i});
			};

			"BG! -> Come on CC. Start/Stop".postln;
			index.postln;

			if(vel == 0,
				{ objects[index].stop(release);  active[index] = 0; "stopping".postln },
				{ objects[index].play; active[index] = 1; "playing".postln }); active[index]

		}, triggerCC, nil, akaiDeviceID);


		/* MIDI Presets */

		MIDIFunc.noteOn({|vel, note|
			var preset = 0, count = 0;

			presetNotes.do {|n|
				if(n == note, {preset = count});
				count = count + 1;
			};

			("Preset index is: " + preset).postln;
			presets[preset].postln;

			objects.do{|obj, index|
				if(active[index] == 1, {
					var temp = objects[index].control.amplitude;
					(" index " + index + " preset " + preset).postln;
					objects[index].control = presets[index][preset];
					objects[index].control.amplitude = temp;
					obj.update;
				})
			};

		}, presetNotes, nil, akaiDeviceID);


		/* Polarities */

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set frequency".postln;
					obj.control.frequency = val/127;
					obj.update
				})
			};
		}, 10, 1, akaiDeviceID);

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set surface".postln;
					obj.control.surface = val/127;
					obj.update
				})
			};
		}, 11, 1, akaiDeviceID);

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set color".postln;
					obj.control.color = val/127;
					obj.update
				})
			};
		}, 12,1, akaiDeviceID);

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set amplitude".postln;
					obj.control.amplitude = val/127;
					obj.update
				})
			};
		}, 13, 1, akaiDeviceID);

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set speed".postln;
					obj.control.speed = val/127;
					obj.update
				})
			};
		}, 14, 1, akaiDeviceID);

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set density".postln;
					obj.control.density = val/127;
					obj.update
				})
			};
		}, 15, 1, akaiDeviceID);

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set entropy".postln;
					obj.control.entropy = val/127;
					obj.update
				})
			};
		}, 16, 1, akaiDeviceID);

		MIDIFunc.cc({|val|
			objects.do{|obj, index|

				if(active[index] == 1, {
					"Set position".postln;
					obj.control.position = val/127;
					obj.update
				})
			};
		}, 17, 1, akaiDeviceID);
	}
}