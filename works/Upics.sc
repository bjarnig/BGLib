Upics {

	*path { ^"/Users/bjarni/Works/Adapt/Upic-Observe/" }

	*loadSynths {|interpreter, path="/Users/bjarni/Works/Adapt/Upic-Observe/synths/pEffects.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSamplers {|interpreter, path="/Users/bjarni/Works/Adapt/Upic-Observe/synths/pSamplers.scd"|
		interpreter.compileFile(path).value;
	}

	*load {|interpreter, path="/Users/bjarni/Works/Adapt/Upic-Observe/composition/upics.scd"|
		interpreter.compileFile(path).value;
	}
}


