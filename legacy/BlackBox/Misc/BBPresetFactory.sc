BBPresetFactory {

	*repetitive {|baseDur=1|
		^[
			[\posp, 0.33, \durp, baseDur/6, \ampp, 1],
			[\posp, BPat.move(0.33, 0.28, 8), \durp, BPat.move(1/6, 1/4, 16, Pseq([1/4], inf)), \ampp, 1],
			[\posp, 0.25, \durp, baseDur/5, \ampp, 1],
			[\posp, 0.42, \durp, baseDur/3, \ampp, 1],
			[\posp, 0.1, \durp, baseDur/12, \ampp, Pseq([1,1,0,0.5], inf)],
			[\posp, 0.14, \durp, baseDur/2, \ampp, Pseq([1,1,0,0.5], inf)]
		];
	}

	*brownian {|baseDur=1|
		^[
			[\posp, Pbrown(0.1, 0.3, 0.05), \durp, Pbrown(baseDur/3, baseDur/6, baseDur/8), \ampp, 1],
			[\posp, Pbrown(0.6, 0.9, 0.05), \durp, Pbrown(baseDur/8, baseDur/4, baseDur/10), \ampp, 1]
		];
	} 

	*elseq {	
		^[
			[\ampp,BPat.sin(0,0.9,2),\durp,BPat.prw(0.4, 0.6),\posp,BPat.ior(0.0,0.8),\atk,BPat.iot(0.2, 0.8),\rel,BPat.iot(0.4, 0.7)],
			[\ampp,BPat.sev(0,1.2),\durp,BPat.rhn(1/8,1/4),\posp,BPat.ior(0.0,0.6),\atk,BPat.ior(0.2,0.8),\rel,BPat.ior(0.5, 0.9)],
			[\ampp,BPat.sec(0.3,1.2),\durp,BPat.rhn(1/10, 1),\posp,BPat.iot(0.0,0.15),\atk,BPat.ior(0.1,0.5),\rel,BPat.ior(0.8,1.6)],
			[\ampp,BPat.wht(0.1,1.0),\durp,BPat.iot(1/2, 1/16),\posp,BPat.ior(0.1,0.55),\atk,BPat.ior(0.1,0.3),\rel,BPat.ior(0.1,0.6)],
			[\ampp,BPat.prw(0.1,1.0),\durp,BPat.sin(0.1, 1.2),\posp,BPat.sin(0.0, 0.5),\atk,BPat.prw(0.2, 0.8),\rel,BPat.prw(0.8, 1.4)],
			[\ampp,BPat.sec(0,1.2),\durp,BPat.rhn(1/10,1/2),\posp,BPat.ior(0.3,0.9),\atk,BPat.ior(0.1,0.2),\rel,BPat.iot(0.5, 0.9)],
			[\ampp,BPat.sec(0.0,1.2,8),\durp,BPat.rhn(1/10, 1/5),\posp,BPat.iot(0.3,0.5),\atk,BPat.ior(0.1,0.5),\rel,BPat.ior(0.8,1.6)],
			[\ampp,BPat.wht(0.01,1.0),\durp,BPat.iot(1/2, 4),\posp,BPat.ior(0.0,0.9),\atk,BPat.ior(0.1,0.3),\rel,BPat.ior(0.1,0.6)]
		]
	}

	*brackets {
		^[
			[\ampp, Pn(Penv([0,1,1,0],[1,3,1])).trace],
			[\ampp, Pseq([1,1,0,0.5], inf)],
			[\durp, Pseq([1/2, 1/4], inf)],
			[\ampp, Pn(Penv([0,0.5,1,0.25,0],[5,8,3,2]))],
			[\posp, Pseq([Pseq([0.45], 1), Pseq([0.76], 2), Pseq([0.80], inf)])],
			[\posp, Env([0.7, 0.99], 25)],
			[\ampp, Env([0.5, 0.2], 15)],
			[\durp, Pseq([Pseq([2], 4), Pseq([8], inf)])],
			[\posp, Pseq([Pseries(0.15, 0.0005, 500), Pwhite(0.15, 0.25, inf)])],
			[\durp, Pseq([1, 1/16, 1/32], inf)],
			[\durp, Pseq([1, 1/24, 1/32], inf)],
			[\durp, Pseq([4, 1/24, 1/32], inf)],
			[\durp, Pseq([4, 1/8, 1/32, 8], inf)],
			[\durp, Pseq([1, 1/16, 1/32], inf)],
			[\durp, Pseq([Pseq([16], 1), Pseq([24], 8), Pseq([24], 8),  Pseq([64], inf) ])],
			[\posp, Pseq([Pseq([0.1], 1), Pseq([0.2], 4), Pseq([0.3], 4), Pseq([0.4], 4), Pseq([0.5], inf) ])],
			[\ampp, Pn(Penv([0,1,1,0],[2,0.1,2]))],
			[\ampp, Pseq([Pwhite(0.5, 1.2, 8), 0, 0, 0, 0, 0], inf)],
			[\ampp, Pseq([0.0, 1.0, 0.125, 0.75, 0], inf)]
		];
	}
}

 