AkaiMIDI {

	var testTriggerPrepend = "This is trigger:";
	var testKnobPrepend = "This is knob:";
	var testButtonPrepend = "This is button:";
	var testFaderPrepend = "This is fader:";
	var testTogglePrepend = "This is toggle:";
	var testKeyPrepend = "This is key:";
	var <>deviceId;
	var <>toggles, <>knobs, <>triggers, <>keys;

	/* inits midi connections if needed */
	*initMidi {
		MIDIClient.init;
		MIDIIn.connectAll;
	}

	*listen {
		MIDIFunc.cc({arg ...args; ("MIDIFunc.cc: " + args).postln});
		MIDIFunc.noteOn({arg ...args; ("MIDIFunc.noteOn: " + args).postln});
		MIDIFunc.noteOff({arg ...args; ("MIDIFunc.noteOff: " + args).postln});
	}

	/* Maps the 8x8 knobs and triggers common to some AKAI models */
	patterns {|items|
		var playingPatterns = Array.newClear(items.size);

		items.do{|item, index|

			MIDIFunc.cc({|value|
				if(value < 1,
					{ playingPatterns[index].stop },
					{ playingPatterns[index] = item.pattern.play }
				);

			}, toggles[index], nil, deviceId);

			item.specs.do {|spec, index|
				MIDIFunc.cc({|value|
					item.arguments[spec[0]] = spec[1].map(value/127);
				}, knobs[index], nil, deviceId);
			};
		};
	}
}