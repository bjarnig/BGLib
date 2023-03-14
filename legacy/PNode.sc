ParticleToken {
    var <>startPos, <>grDur, <>rate, <>speed, <>atk, <>sus, <>rel;

	*new {|startPos, grDur, rate, speed, atk, sus, rel|
		^super.newCopyArgs(startPos, grDur, rate, speed, atk, sus, rel);
	}
}

ParticleNode {

	var <>buf, <>control, <>duration, <>nodes, synth;

	*new {|buf, control, duration|
		^super.new.init(buf, control, duration);
	}

	init {|buf, control, duration|
		this.buf = buf;
		this.control = control;
		this.duration = duration;
		this.nodes = List.new();
	}

	addNode {|node|
		nodes.add(node);
	}

	run {
		if(nodes != nil, {
			var next = nodes.choose;
		    SystemClock.sched(duration, { next.run });
		});

		"Start Node".postln;

		synth = Synth(\gr, [
			\buf, this.buf,
			\start, this.control.startPos,
			\grDur, this.control.grDur,
			\rate, this.control.rate,
			\speed, this.control.speed,
			\atk, this.control.atk,
			\sus, this.control.sus,
			\rel, this.control.rel
		]);
	}
}