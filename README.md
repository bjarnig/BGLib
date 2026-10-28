# BGLib

My SuperCollider library: pseudo-UGens, classes for my pieces, and language-side utilities.

## Install

Clone into the Extensions folder and recompile the class library. Some UGens and `Dome` need sc3-plugins.

```
cd ~/Library/Application\ Support/SuperCollider/Extensions
git clone git@github.com:bjarnig/BGLib.git
```

## Utilities

```supercollider
Lib.boot(16, 2);                                     // set the usual options and boot

b = Lib.loadFolderSamples("~/Works/SND/impulses/");  // Array of Buffers, a folder or a glob
d = Lib.loadFolderDict("~/Works/SND/impulses/");     // d[\clicka]
w = Lib.loadWavetables("~/Works/SND/waves/", 4096);  // wavetable buffers for Osc / VOsc
g = Lib.grainWindow(0.1, 0.01);                      // Env([0,1,0], [atk, rel], [8,-4]) as a Buffer
p = Lib.patternToBuffer(Pbrown(-0.8, 0.8, 0.1), 2);  // pattern values as raw samples

m = Lib.midiOut;                                     // IAC "BG.Virtual", created once
Lib.ccMap(Ndef(\x), \freq, 21, 20, 2000, \exp);      // CC 21 onto a parameter
Lib.onsetFollow({ SoundIn.ar(0) }, 0.5, { |amp| amp.postln });

Lib.rec("take");                                     // timestamped file on the Desktop
Lib.recordChannels("piece", 16, 60);                 // one mono file per channel
Lib.cdp("blur blur", "~/in.wav", "~/out.wav", "50"); // CDP command, run in Terminal

Dome.load;                                           // VBAP layouts \coined, \inv, \zigzag
VBAP.ar(16, sig, Dome.at(\coined), azi, ele);

Akin.load("~/Works/Archive/textures/dust/dustrain.wav", 10);  // similar sounds from the archive
```

Most methods take an optional `server` (default `Server.default`) and an `action` called once the buffers are on the server.
