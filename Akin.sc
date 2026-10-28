Akin {

	classvar <>cmd = "/Users/bjarni/Works/Tools/listen/bin/listen";

	// hits as Events (unit, path, start, end, score); blocks sclang while the query runs (~8 s)
	*like {|paths, n=20, unit=\file, match=\any, perFile=1, ignore|
		var args, out;
		if(paths.isString, { paths = [paths] });
		args = paths.collect { |p| p.standardizePath.shellQuote }.join(" ");
		args = args + "-n" + n + "--unit" + unit + "--match" + match + "--per-file" + perFile + "--tsv";
		if(ignore.notNil, { args = args + "--ignore" + ignore.asArray.join(",") });
		out = (cmd.shellQuote + "like" + args).unixCmdGetStdOut;
		^out.split($\n).reject(_.isEmpty).collect { |line|
			var f = line.split($\t);
			(unit: f[0].asSymbol, path: f[1], start: f[2].asFloat, end: f[3].asFloat, score: f[4].asFloat)
		}
	}

	// query and load in one go: an Array of Buffers, closest first
	*load {|paths, n=20, unit=\file, match=\any, perFile=1, ignore, server|
		^this.buffers(this.like(paths, n, unit, match, perFile, ignore), server)
	}

	// one Buffer per hit; segments load only their span
	*buffers {|hits, server|
		server = server ? Server.default;
		^hits.collect { |h|
			var sf = SoundFile.openRead(h.path), sr;
			if(sf.isNil, {
				("Akin: cannot read" + h.path).warn; nil
			}, {
				sr = sf.sampleRate; sf.close;
				if(h.unit == \segment, {
					Buffer.read(server, h.path, (h.start * sr).asInteger, ((h.end - h.start) * sr).asInteger)
				}, {
					Buffer.read(server, h.path)
				})
			})
		}.reject(_.isNil)
	}
}
