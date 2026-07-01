# ملاحظات V45 Stage 14.7.1 — Build Fix

تم إصلاح خطأ بناء APK في Stage 14.7.

## سبب الخطأ
كان هناك سطر نصي مكسور داخل MainActivity.java في دالة أزرار الاختصارات الصغيرة:

`b.setText(icon + "\n" + label);`

وكان قد ظهر في السورس كسطرين داخل String مما سبب خطأ:

- unclosed string literal
- Compilation failed

## الإصلاح
تم تحويل النص إلى صيغة Java صحيحة باستخدام `\n` داخل نفس السطر.

## الإصدار
- versionCode: 65
- versionName: 45.0-stage14.7.1-build-fix

لا يوجد تغيير في منطق الرسائل أو المحافظ أو المخزون.
