ICLC {

	*loadBuffers {|interpreter, path="/Users/bjarni/Works/Pieces/ICLC/scd/lib/buffers.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSynths {|interpreter, path="/Users/bjarni/Works/Pieces/ICLC/scd/lib/synths.scd"|
		interpreter.compileFile(path).value;
	}

	*loadSpatial {|interpreter, path="/Users/bjarni/Works/Pieces/ICLC/scd/lib/spatial.scd"|
		interpreter.compileFile(path).value;
	}

	*load {|interpreter|
		ICLC.loadSynths(interpreter);
		ICLC.loadBuffers(interpreter);
		ICLC.loadSpatial(interpreter);
	}
}
