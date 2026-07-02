# ملاحظات V45 Stage 14.7.3 — Source 8 Build Fix

تم تنفيذ إصلاح بناء عاجل فوق Stage 14.7.2.

## سبب الفشل
ظهر خطأ في GitHub Actions:

`text blocks are not supported in -source 8`

وكان السبب استخدام تسلسل `\s` بصيغة غير متوافقة مع Java source 8 داخل `PaymentParser.java`.

## ما تم إصلاحه
- تعديل تعبير استخراج رقم الحساب في `PaymentParser.java`.
- استبدال `\s` غير المتوافق داخل النص بـ `\\s` الصحيح لتعبيرات Regex في Java 8.
- لم يتم تغيير منطق المحافظ أو قواعد الأمان.

## الإصدار
- versionCode: 67
- versionName: 45.0-stage14.7.3-source8-build-fix
