HP {

	*loadBuffers {|interpreter, path="/Users/bjarni/Works/Adapt/Holding-Pattern/code/HoldingBuffers.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSynths {|interpreter, path="/Users/bjarni/Works/Adapt/Holding-Pattern/code/HoldingSynths.scd"|
		interpreter.compileFile(path).value;
	}

	*load {|interpreter|
		HP.loadSynths(interpreter);
		HP.loadBuffers(interpreter);
	}
}