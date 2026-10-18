# Miyov 0.4

Kichik PNG mushukcha: 72 ta kadr, haqiqiy mushuk yozuvi va bosishga javoblar.

- Yurish: 16 kadr. Yugurish: 16 kadr. Qolgan 5 holat: har birida 8 kadr.
- Harakat ekran yangilanishi bilan sinxron; tezlik vaqtga bog‘liq, oyoq fazasi bosib o‘tilgan masofaga mos.
- 2–7 ketma-ket bosish: uchib chiqadigan yurakchalar.
- 3 bosish: yurakchalar bilan kapalak.
- 8 va undan ortiq ketma-ket bosish: jahl holati va peshona oldida qizil to‘rt burchakli anime belgisi.
- Bosishlar orasida 1 sekunddan ortiq tanaffus bo‘lsa hisob qaytadan boshlanadi.
- Sudrashda joyini o‘zgartiradi; uzoq bosish yopadi.
- Haqiqiy yumshoq miyov yozuvi chalinayotganida uni qayta boshlamaydi; ovoz uzilib g‘alati tovushga aylanmaydi.
- Ovoz «Ovozni tekshir» tugmasi orqali tekshiriladi; telefonning media ovozi ishlatiladi.

## APK
Actions → eng yangi muvaffaqiyatli Build Miyov APK → Artifacts → Miyov-APK.
ZIP ichidagi app-debug.apk faylini telefonga o‘rnating.

## Tekshiruv
CI har safar PetDynamicsTest unit testlarini va APK yig‘ilishini bajaradi.
Testlar turli ekran chastotalarida masofa bir xil chiqishini, ushlashda to‘xtashni,
yurakcha/jahl bosqichlarini va chegaradan qaytishni tekshiradi.
PNGlar shaffof chetlar, to‘liq tana va 72 farqli kadr bo‘yicha tekshirilgan.
AI yaratgan rasmlarda mayda kadrlararo farqlar qolishi mumkin.
Telefondagi yakuniy seziladigan ravonlik va ovoz foydalanuvchi qurilmasida tekshirilishi kerak.
Ovoz manbasi va litsenziyasi: AUDIO_CREDITS.md.
