// Named VBAP speaker layouts, loaded once and shared between pieces
Dome {

	classvar <layouts, <buffers;

	*initClass {
		var ring = { |elev, angles| angles.collect { |a| [a, elev] } };
		var lower = ring.(0, [-45, 45, -90, 90, -135, 135, 0, 180]);
		layouts = IdentityDictionary[
			\coined -> [3, ring.(10, [-22.5, 22.5, -67.5, 67.5, -112.5, 112.5, -157.5, 157.5]) ++ lower],
			\inv -> [3, ring.(10, [-10, 10, -70, 70, -100, 100, -160, 160]) ++ lower],
			\zigzag -> [3, ring.(14.97, [-22.5, 22.5, -67.5, 67.5, -112.5, 112.5, -157.5, 157.5]) ++ lower]
		];
		buffers = IdentityDictionary.new;
	}

	*add {|name, dim, positions| layouts[name] = [dim, positions] }

	*array {|name| ^VBAPSpeakerArray.new(*layouts[name]) }

	// all layouts, or only the given names
	*load {|server, names|
		(names ? layouts.keys).do { |k| buffers[k] = this.array(k).loadToBuffer(server ? Server.default) };
		^buffers
	}

	*at {|name| ^buffers[name] }
}
