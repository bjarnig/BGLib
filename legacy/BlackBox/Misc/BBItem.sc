
/* wraps a pattern with its specs and arguments */

BBItem {

	var <>pattern, <>specs, <>arguments, <>presets;
	var <>currentPreset = 0;

	*new{|pattern, specs, arguments, presets|
		^super.new.init(pattern, specs, arguments, presets);
	}

	init {|inPattern, inSpecs, inArguments, inPresets|
		pattern = inPattern;
		specs = inSpecs;
		arguments = inArguments;
		presets = inPresets;
	}
 
	setPreset {|index|
		forBy (0, presets[index].size - 1, 2, {|i|
			presets[index][i].postln;
			presets[index][i + 1].postln;
			pattern.set(presets[index][i], presets[index][i + 1]);
		});
	}

	increasePreset {
		if((currentPreset + 1) >= presets.size, 
		{currentPreset = 0}, { currentPreset = currentPreset + 1; });
		("Setting preset: " + currentPreset).postln;
		this.setPreset(currentPreset);
	}

	specsToArguments {
		arguments = Dictionary();

		specs.do{|item|
			arguments[item[0]] = item[1].default;
			// pattern.set(\degree, Pseq([0, 2, 5b, 1b], inf)); // que ?
		};
	}
}