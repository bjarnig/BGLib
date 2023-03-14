BPat {

	*index {|index, patterns, length=inf|
		^Pindex(patterns, index, length);
	}
 
	*move {|from, to, dur, then|
		if(then == nil, {
			^Pseq([Penv([from, to], [dur])], 1);
		}, {
			^Pseq([Penv([from, to], [dur]), then]);
		}) 
	}

	// triangle shape 	
	*tri {|from, middle, to, dur, then|
		if(then == nil, {
			^Penv([from, middle, to], [dur, dur]);
		}, {
			^Pseq([Penv([from, middle, to], [dur, dur]), then]);
		}) 
	}

	// line in/out/in
	*sin {|min=0,max=1,dur=1,length=inf|
		^Pseq([Pn(Penv([min,max,max,min],[dur,dur*2,dur]), 2), Pn(Penv([min,max,min,max,min],[dur,dur*4,dur*2,dur]),2)],length)
	}

	// sequence of brownain(s)
	*prw {|from,to,length=inf|
		^Pseq([Pbrown(from,to,from/8,16), Pbrown(from/2,to,from/4,12), Pbrown(from,to*4,from/4,4), Pbrown(from/2,to/2,from/8,16)],length)
	}

	// sequence of white(s)
	*wht {|from,to,length=inf|
		^Pseq([Pwhite(from, to, 8), Pwhite(from/2, to/2, 16), Pwhite(from, to*2, 4), Pwhite(from/2, to*2, 8)], length)
	}

	// move from to and back again
	*iot {|from,to,dur=4,length=inf|
		^Pseq([Pseq([from], dur), Penv([from, to], [dur*2]), Pseq([to], dur), Penv([to, from], [dur*2])], length)
	}

	// move from to and back again (with randomness)
	*ior {|from,to,dur=4,length=inf|
		^Pseq([Penv([from, to], [dur]), Pbrown(from, to, from/2, dur*2), Penv([to, from], [dur])], length)
	}

	// sequence, variation of min and max
	*sec {|min,max,dur=4,length=inf|
		^Pseq([Pseq([max, min, max], dur), Pseq([min], dur), Pseq([max], dur), Pseq([min], dur) ], length)
	}

	// sequence, variation of min and max
	*sev {|min,max,dur=4,length=inf|
		^Pseq([Pseq([max, min, max, min], dur*2), Pseq([min], dur*2), Pseq([max], dur), Prand([min, max], dur)], length)
	}

	// rhytmic divisions
	*rhn {|from,to,dur=4,length=inf|
		^Pseq([ Pseq([from/2, from, to, to/2], dur), Pseq([from, to],dur/2), Pseq([from/2, from, from*2], dur), Pseq([from/2, from, from*2], dur)], length)
	}
}