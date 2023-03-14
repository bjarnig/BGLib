// BGMixer : NdefMixer {
//
// 	*initClass {
// 		Class.initClassTree(GUI);
//
// 		GUI.skins.put(\jit, (
// 			fontSpecs: 		["Monaco", 9],
// 			fontColor: 		Color.white,
// 			background: 	Color.fromHexString("#0A2239"),
// 			foreground:		Color.fromHexString("#0A2239"),
// 			onColor:		Color.fromHexString("#176087"), // Color(0.5, 1, 0.5),
// 			onColor2:   	Color.fromHexString("#53A2BE"),
// 			offColor:		Color.grey(0.8, 0.5),
// 			hiliteColor:	Color.green(1.0, 0.5),
// 			gap:			4@4,
// 			margin: 		4@4,
// 			buttonHeight:	22,
// 			headHeight: 	28
//
// 			)
// 		);
// 	}
// }

NFMix : NdefMixer {

	*initClass {
		Class.initClassTree(GUI);

		GUI.skins.put(\jit, (
			fontSpecs: 		["Monaco", 9],
			fontColor: 		Color.white,
			textColor: 		Color.white,
			background: 	Color.fromHexString("#4d94c3"),
			foreground:		Color.fromHexString("#161b22"),
			onColor:		Color.fromHexString("#407b52"), // Color(0.5, 1, 0.5),
			onColor2:   	Color.fromHexString("#53A2BE"),
			offColor:		Color.grey(0.8, 0.5),
			hiliteColor:	Color.green(1.0, 0.5),
			gap:			4@4,
			margin: 		4@4,
			buttonHeight:	22,
			headHeight: 	28

		)
		);
	}
}
