MIDIMix : AkaiMIDI {

	var knobRow1,knobRow2,knobRow3,buttonRow1,buttonRow2,faderRow1,faderRow2;

	*new{|deviceId|
		^super.new.init(deviceId);
	}

	init {|inDeviceId|

		deviceId = inDeviceId;
		knobRow1 = [16, 20, 24, 28, 46, 50, 54, 58];
		knobRow2 = [17, 21, 25, 29, 47, 51, 55, 59];
		knobRow3 = [18, 22, 26, 30, 48, 52, 56, 60];
		buttonRow1 = [1, 4, 7, 10, 13, 16, 19, 22];
		buttonRow2 = [3, 6, 9, 12, 15, 18, 21, 24];
		faderRow1 = [19, 23, 27, 31, 49, 53, 57, 61];
		faderRow2 = [62];
	}

	test {

		knobRow1.do {|item, index|
			var n = index + 1;
			MIDIFunc.cc({
				(testKnobPrepend + "A" ++ n).postln;
			}, item, nil, deviceId);
		};

		knobRow2.do {|item, index|
			var n = index + 1;
			MIDIFunc.cc({
				(testKnobPrepend + "B" ++ n).postln;
			}, item, nil, deviceId);
		};

		knobRow3.do {|item, index|
			var n = index + 1;
			MIDIFunc.cc({
				(testKnobPrepend + "C" ++ n).postln;
			}, item, nil, deviceId);
		};

		faderRow1.do {|item, index|
			var n = index + 1;
			MIDIFunc.cc({
				(testFaderPrepend + "A" ++ n).postln;
			}, item, nil, deviceId);
		};

		faderRow2.do {|item, index|
			var n = index + 1;
			MIDIFunc.cc({
				(testFaderPrepend + "B" ++ n).postln;
			}, item, nil, deviceId);
		};

		buttonRow1.do {|item, index|
			var n = index + 1;
			MIDIFunc.noteOn({
				(testButtonPrepend + "A" ++ n).postln;
			}, item, nil, deviceId);
		};

		buttonRow2.do {|item, index|
			var n = index + 1;
			MIDIFunc.noteOn({
				(testButtonPrepend + "B" ++ n).postln;
			}, item, nil, deviceId);
		};
	}

	scenes {|sceneA, sceneB, tempo=110|
		var midiFuncs, playingPatterns, clock = TempoClock.new(tempo / 60); 
		#midiFuncs, playingPatterns = this.patterns(sceneA);

		MIDIFunc.noteOn({|vel, value|
			" SET SCENE A ".postln;
			midiFuncs.do{|mc| mc.free};
			playingPatterns.do {|item| if(item.notNil, { item.stop })};
			#midiFuncs, playingPatterns = this.patterns(sceneA);			
		}, 25);

		MIDIFunc.noteOn({|vel, value|
			" SET SCENE B ".postln;
			midiFuncs.do{|mc| mc.free};
			playingPatterns.do {|item| if(item.notNil, { item.stop })};
			#midiFuncs, playingPatterns = this.patterns(sceneB);			
		}, 26);
	} 

	patterns {|items, clock|
        var midiFuncs = List();
		var playingPatterns = Array.newClear(items.size);
		var addSpecMapping = {|item, index, target|
			midiFuncs.add(MIDIFunc.cc({|value|
				var mappedValue = item.specs[index][1].map(value/127);
				("Setting: " + item.specs[index][0] + ":" + mappedValue).postln;
				item.arguments[item.specs[index][0]] = mappedValue;
			}, target, nil, deviceId));
		};

		"items.size: " + items.size.postln;

		items.do{|item, index|
			// ("looping items: " + index + " : " + buttonRow1[index]).postln;

			midiFuncs.add(MIDIFunc.noteOn({|value|
			if(playingPatterns[index].notNil, {
					("Stoping Slot:" + (index + 1)).postln;
					playingPatterns[index].stop;
					playingPatterns[index] = nil;
				}, {
					("Starting Slot:" + (index + 1)).postln;
					item.setPreset(item.currentPreset);
					playingPatterns[index] = item.pattern.play(clock);
				});
			}, buttonRow1[index], nil, deviceId));

		    midiFuncs.add(MIDIFunc.noteOn({|value|
				("Increasing preset, slot:" + (index + 1)).postln;
				item.increasePreset();
			}, buttonRow2[index], nil, deviceId));

		   if(item.specs.notNil, {
			   if(item.specs.size > 0, { addSpecMapping.value(item, 0, faderRow1[index]) });
			   if(item.specs.size > 1, { addSpecMapping.value(item, 1, knobRow3[index]) });
			   if(item.specs.size > 2, { addSpecMapping.value(item, 2, knobRow2[index]) });
			   if(item.specs.size > 3, { addSpecMapping.value(item, 3, knobRow1[index]) });
		    });
		};

		^[midiFuncs, playingPatterns];
	}
}