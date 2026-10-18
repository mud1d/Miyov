# Miyov 0.3 — PNG mushukcha va ovoz

Bu Android loyiha manbasi, o‘rnatiladigan APK emas.

## O‘zgarishlar
- Oldingi mushukchaga mos 56 yangi PNG kadr yaratildi; panjalar va chetlar to‘liq, fon shaffof.
- Ekrandagi mushukcha oynasi 88 × 80 dp; ko‘rinadigan mushuk bundan ham kichik.
- Yurishda mushuk chap/o‘ng tomonga buriladi va ekran chegarasidan chiqmaydi.
- Bosganda oldingi meow.wav ijro etiladi. Bu oldingi qisqa ovoz, yangi yozilgan haqiqiy miyov emas.
- Asosiy oynada «Ovozni tekshir» tugmasi bor. Media ovozi nol bo‘lsa xabar chiqadi.
- Sudrash bilan bosish ajratildi; uzoq bosish mushukchani yopadi.

## APK olish
1. GitHub’dagi Miyov loyihasining fayllarini shu ZIP ichidagi fayllar bilan yangilang.
   PNG fayllari app/src/main/assets/miyov ichida qolishi kerak.
2. Actions → Build Miyov APK → Run workflow.
3. Yashil belgi chiqqach, Artifacts → Miyov3-APK ni yuklab oling.
4. Ichidagi app-debug.apk ni telefonda oching. Bir xil imzo bilan yig‘ilgan bo‘lsa yangilanish sifatida o‘rnatiladi.
5. Ilovani ochib «Ovozni tekshir», keyin «Mushukchani chiqar» ni bosing.
6. Ruxsat so‘ralsa «Boshqa ilovalar ustida ko‘rsatish» ni yoqing.

Android Studio orqali ham loyihani ochib, Build APK qilish mumkin.

## Tekshiruv holati
Barcha 56 PNG alohida ko‘rib chiqildi. 56 ta farqli kadr, 7 holat, har birida 8 tadan kadr bor.
Har biri RGBA, 256 × 256 piksel; mushukcha rasm chegarasida kesilmaydi.
Yurish/yugurishda oyoq holatlari, qiziqish/erkalashda bosh va ko‘z holatlari o‘zgaradi.
Sakrashda kadrlar orasidagi vertikal siljish saqlandi. Uyqu sekinroq ijro etiladi.
AI bilan yaratilgan kadrlar orasida mayda mo‘yna/proportsiya farqlari qolishi mumkin.
Bu professional qo‘lda chizilgan, mutlaqo bir xil shaklli animatsiya degani emas.
WAV fayli o‘qiladi va bo‘sh emas. Ovoz kodi Android rasmiy MediaPlayer
qoidasiga ko‘ra audio atributlarini prepare() dan oldin o‘rnatadi.
Bu muhitda Android SDK va Gradle yo‘q; APK kompilyatsiyasi hamda
telefonning haqiqiy ovoz/overlay tekshiruvi bajarilmagan.
