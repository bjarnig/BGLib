Superpang {

	*loadSynths {|interpreter, path="/Users/bjarni/Works/PIECES/Superpang/code/Superbuffers.scd"|
		interpreter.compileFile(path).value;
	}

	*loadBuffers {|interpreter, path="/Users/bjarni/Works/PIECES/Superpang/code/Supersynths.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSections {|interpreter, path="/Users/bjarni/Works/PIECES/Superpang/code/Supersections.scd"|
		interpreter.compileFile(path).value;
	}

	*load {|interpreter|
		Superpang.loadSynths(interpreter);
		Superpang.loadBuffers(interpreter);
		Superpang.loadSections(interpreter);
	}
}