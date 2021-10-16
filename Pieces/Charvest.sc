Charvest {

	*loadSynths {|interpreter, path="/Users/bjarni/Works/PIECES/Charvest/synths/cSynths.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSamplers {|interpreter, path="/Users/bjarni/Works/PIECES/Charvest/synths/cSamplers.scd"|
		interpreter.compileFile(path).value;
	}

	*load {|interpreter, path="/Users/bjarni/Works/PIECES/Charvest/Charvest/composition/charvest.scd"|
		interpreter.compileFile(path).value;
	}

	*gui {|interpreter, path="/Users/bjarni/Works/PIECES/Charvest/composition/gui.scd"|
		interpreter.compileFile(path).value;
	}
}

