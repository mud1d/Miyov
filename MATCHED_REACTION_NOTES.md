# Matched face reactions v0.8

Accepted assets were generated with built-in image generation. The32-frame guided walk atlas was edited to replace dark-brown far legs with light-orange fur/cream paws. Two further edits of that corrected atlas changed only contented and annoyed expressions, retaining matching poses. A separate16-frame reaction generation was rejected for stylistic/proportion differences and is not shipped.

All three accepted atlases use the same crop boxes derived from the corrected walk alpha components. Each crop is uniformly scaled1.12 and aligned at right edge230, ground224, in256px cells. All atlases are2048x1024 (8x4), quantized192 RGBA colors. No frame duplication. The former separate-limb renderer is not used.

Android always renders the corrected walk bitmap for walking, curious, affection and anger states. On entering a reaction, it retains the current gait index and stops velocity to prevent drift with frozen paws. Affection/anger add only a57x60 facial patch at(180,98), clipped to the oval bounding rectangle(180,98)-(237,158). Source-generated torso/limb differences are never drawn. Ears outside the face oval retain their original silhouette. Red anger mark moved above the current forehead to(225,84). Only the small face crops are decoded into retained bitmaps. Sleep continues to use the prior8 PNGs, outside this tap-reaction fix. Audio unchanged.

A private raster reconstruction verified face composition; the repository preview demonstrates the visible expression changes. Preview is not a device recording. Actual Android playback and audio are not claimed verified. Pose generation still has minor temporal/anatomical approximations.

Walk-color edit prompt:

Use case: precise-object-edit. Edit this existing 8-column x4-row sprite sheet of32 walking orange-white cats. Change ONLY the colors of the far-side legs and paws: the dark brown/orange hind/front legs must become the SAME warm light orange fur as the nearer legs, and ALL FOUR paws must have matching creamy-white toes. Keep only a very subtle 8% natural occlusion shadow on far limbs. No dark brown legs, no brown boots. Everything else MUST remain unchanged: preserve each of the32 leg poses, foot locations, body/head silhouettes, face, fur, tail, grid positions, scale and camera. Preserve ALL32 frames and the8x4 grid. Actual transparent alpha background. No ground/shadows/borders/labels. Production in-place palette correction, not a redesign. Output1536x1024.

Affection face-edit prompt:

Use case: precise-object-edit. Edit the provided existing32-frame cat walk atlas. Change ONLY the eyes/eyelids and a tiny mouth expression on EVERY cat: eyes softly closed contentedly in a relaxed cat smile, ears relaxed. Preserve ALL other pixels/geometry as faithfully as possible: same head size and shape, fur texture, orange tabby pattern, light orange legs and white paws, torso, all32 distinct foot positions, tails, 8columns x4rows layout, cell locations, lighting. No redraw into bold-outline cartoon, no thick outlines, no change in leg length or head/body proportions. This is the SAME cat at the SAME32 walking phases, ONLY with contentedly closed eyes. No hearts or symbols. Same1536x1024 dimensions and transparent alpha background. No backdrop, ground, shadow, text or checkerboard.

Angry face-edit prompt:

Use case: precise-object-edit. Edit this existing32-frame cat walk sprite atlas. Change ONLY face/ear expression on each cat to mildly ANNOYED: narrowed dark eyes with gently lowered eyebrows, small closed-mouth frown, ears tilt slightly backward. Not scary, no snarling or teeth. Keep the EXACT same cute kitten face proportions. Preserve all32 walking phases, all leg/foot poses, head/body silhouettes and size, fur texture, orange/white coat, tail, light-orange legs and white paws, 8columns x4rows layout, exact cell positions and1536x1024 image dimensions. No body crouch/sit changes. Do not redraw into cartoon outlines. Keep body pixels and pose as faithful to reference as possible; ONLY facial acting changes. No anger mark, hearts or other symbols, those will be drawn in the app. Transparent alpha background. No floor, shadows, text, border or checkerboard.

