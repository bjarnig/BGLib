Upics {

	*loadSynths {|interpreter, path="/Users/bjarni/Works/PIECES/Upics/synths/pEffects.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSamplers {|interpreter, path="/Users/bjarni/Works/PIECES/Upics/synths/pSamplers.scd"|
		interpreter.compileFile(path).value;
	}

	*load {|interpreter, path="/Users/bjarni/Works/PIECES/Upics/composition/upics.scd"|
		interpreter.compileFile(path).value;
	}
}