# ملاحظات V45 Stage 14.7.6.1 — Build Fix

## سبب الإصلاح
ظهر خطأ بناء في GitHub Actions داخل الملف:

`MainActivity.java`

عند السطر الذي يستخدم:

`HashMap<String, CheckBox>`

والسبب أن الملف لم يكن يحتوي على import للكلاس `HashMap`.

## ما تم إصلاحه
- إضافة:

`import java.util.HashMap;`

- لم يتم تغيير منطق المحافظ.
- لم يتم تغيير قاعدة الرقم أولاً.
- لم يتم تغيير واجهة القيد أو القائمة الرئيسية.

## الإصدار
- versionCode: 71
- versionName: 45.0-stage14.7.6.1-build-fix
