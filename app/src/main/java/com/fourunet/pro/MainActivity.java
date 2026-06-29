package com.fourunet.pro;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.biometrics.BiometricPrompt;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.ContactsContract;
import android.os.StrictMode;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Method;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.HashSet;
import java.util.Calendar;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int REQ_FILE = 20;
    private static final int REQ_EXCEL = 21;
    private static final int REQ_PDF = 30;
    private static final int REQ_BACKUP_SAVE = 40;
    private static final int REQ_BACKUP_RESTORE = 41;
    private static final int REQ_ADMIN_PACK = 50;
    private static final int REQ_PICK_CONTACT = 60;
    private static final int REQ_NETWORK_LOGO = 70;

    LinearLayout root;
    LinearLayout content;
    LinearLayout nav;
    String activeTab = "home";
    int selectedAmount = 50;
    int pendingReportAmount = -1;
    boolean pendingReportSummaryOnly = false;
    EditText pendingContactPhone;
    EditText pendingContactName;
    String directSalePhoneDraft = "";
    String directSaleNameDraft = "";
    boolean directSaleRewardsDraft = true;
    String routerStudioTab = "connect";
    boolean routerStudioConnected = false;
    String routerStudioPasswordDraft = "";
    String routerStudioPasswordMode = "username";
    String rewardsPanelTab = "overview";
    boolean pendingRewardImport = false;
    int pendingRewardImportAmount = 100;
    String reviewFocusLogId = "";
    boolean appUnlocked = false;
    final ExecutorService performanceExecutor = Executors.newSingleThreadExecutor();
    private static final int IMPORT_BATCH_SIZE = 500;
    int logsVisibleLimit = AppStore.performanceLogPageSize();
    int cardsVisibleLimit = AppStore.performanceCardPageSize();
    int cardsVisibleAmount = -1;

    // Stage 13.6 STRONG PERFORMANCE ENGINE.
    // عرض البيانات بالدفعات وتشغيل التقارير الثقيلة في الخلفية.

    // Stage 13.5 SMART UI + DIRECT SALE ENHANCEMENT.
    // تحسينات واجهة آمنة + بيع آجل للزبون الموثق + تنبيهات مخزون منخفض.
    final int purple = Color.rgb(155, 92, 255);          // Neon Purple
    final int purpleDark = Color.rgb(8, 10, 24);         // Deep app background
    final int purpleLight = Color.rgb(32, 231, 255);     // Neon Cyan accent
    final int bg = Color.rgb(4, 6, 16);                  // Near black
    final int card = Color.rgb(14, 18, 36);              // Dark card
    final int card2 = Color.rgb(20, 25, 47);             // Dark secondary card
    final int text = Color.rgb(246, 247, 255);           // Clear white text
    final int muted = Color.rgb(170, 176, 199);          // Muted readable text
    final int borderSoft = Color.rgb(43, 55, 92);
    final int green = Color.rgb(0, 230, 118);
    final int orange = Color.rgb(255, 193, 7);
    final int red = Color.rgb(255, 82, 118);
    final int gold = Color.rgb(224, 174, 65);
    final int goldDark = Color.rgb(94, 64, 18);
    final int goldSoft = Color.rgb(51, 37, 16);
    final int neonBlue = Color.rgb(42, 109, 255);
    final int neonCyan = Color.rgb(32, 231, 255);
    final int neonPink = Color.rgb(255, 56, 176);

    private static final String SUPPORT_NAME = "P.P.E.ALI ALSHRABI";
    private static final String SUPPORT_PHONE_LOCAL = "776901570";
    private static final String SUPPORT_PHONE_INTL = "967776901570";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) getWindow().setStatusBarColor(purpleDark);
        AppStore.ensureDefaultCategories(this);
        if (!AppStore.isLicenseUsable(this)) {
            showActivationScreen();
            return;
        }
        if (AppStore.isAppLockRequired(this)) {
            showAppLockScreen();
            return;
        }
        openMainAfterSecurity();
    }

    @Override
    protected void onDestroy() {
        try { performanceExecutor.shutdownNow(); } catch (Exception ignored) {}
        super.onDestroy();
    }

    private void openMainAfterSecurity() {
        appUnlocked = true;
        requestPermissionsIfNeeded();
        buildLayout();
        showHome();
        handleIncomingReviewIntent(getIntent());
        AppStore.performAutoBackupIfDue(this, "فتح التطبيق");
    }

    private void showAppLockScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(bg);
        page.setPadding(dp(22), dp(40), dp(22), dp(28));
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        setContentView(page);

        TextView lockIcon = tv("🔐", 72, purpleLight, false);
        lockIcon.setGravity(Gravity.CENTER);
        page.addView(lockIcon, new LinearLayout.LayoutParams(-1, -2));

        TextView title = tv("حماية الدخول", 27, text, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(16), 0, dp(8));
        page.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView note = small("اختر طريقة الدخول التي قمت بتفعيلها من إعدادات الأمان. إذا كانت البصمة غير متاحة استخدم رقم PIN الاحتياطي.");
        note.setGravity(Gravity.CENTER);
        page.addView(note, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout box = cardBox();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(24), 0, 0);
        box.setLayoutParams(lp);
        String method = AppStore.getAppLockMethod(this);
        box.addView(tv("طريقة الدخول الحالية", 17, text, true));
        box.addView(small(AppStore.appLockMethodLabel(method)));
        box.addView(separator());

        boolean canUsePin = AppStore.hasAppPin(this);
        boolean wantsFingerprint = AppStore.LOCK_METHOD_FINGERPRINT.equals(method) || AppStore.LOCK_METHOD_FINGERPRINT_PIN.equals(method);
        boolean wantsPin = AppStore.LOCK_METHOD_PIN.equals(method) || AppStore.LOCK_METHOD_FINGERPRINT_PIN.equals(method);

        if (wantsFingerprint) {
            box.addView(action("فتح بالبصمة", purple, Color.WHITE, v -> startBiometricUnlock(() -> openMainAfterSecurity(), canUsePin)));
        }
        if (wantsPin || canUsePin) {
            box.addView(action("إدخال PIN", card2, text, v -> showPinUnlockDialog(() -> openMainAfterSecurity(), false)));
        }
        box.addView(action("خروج", Color.rgb(82,30,42), Color.WHITE, v -> finish()));
        page.addView(box);

        if (wantsFingerprint) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> startBiometricUnlock(() -> openMainAfterSecurity(), canUsePin), 450);
        }
    }

    private boolean isBiometricSupportedForPrompt() {
        if (Build.VERSION.SDK_INT < 28) return false;
        try {
            return getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        } catch (Exception e) {
            return false;
        }
    }

    private void startBiometricUnlock(Runnable onSuccess, boolean allowPinFallback) {
        if (!isBiometricSupportedForPrompt()) {
            if (allowPinFallback && AppStore.hasAppPin(this)) {
                toast("البصمة غير متاحة على هذا الجهاز، استخدم PIN");
                showPinUnlockDialog(onSuccess, true);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("البصمة غير متاحة")
                        .setMessage("هذا الجهاز لا يدعم البصمة أو لم يتم إعداد بصمة من إعدادات الهاتف. يمكنك تفعيل PIN كطريقة دخول احتياطية من إعدادات الأمان.")
                        .setPositiveButton("حسنًا", null)
                        .show();
            }
            return;
        }
        try {
            BiometricPrompt.Builder builder = new BiometricPrompt.Builder(this)
                    .setTitle("تأكيد الهوية")
                    .setSubtitle("استخدم بصمة الإصبع للدخول إلى التطبيق");
            if (allowPinFallback && AppStore.hasAppPin(this)) {
                builder.setNegativeButton("استخدام PIN", getMainExecutor(), (dialog, which) -> showPinUnlockDialog(onSuccess, true));
            } else {
                builder.setNegativeButton("إلغاء", getMainExecutor(), (dialog, which) -> { });
            }
            BiometricPrompt prompt = builder.build();
            prompt.authenticate(new CancellationSignal(), getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    toast("تم التحقق بنجاح");
                    onSuccess.run();
                }
                @Override public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (allowPinFallback && AppStore.hasAppPin(MainActivity.this)) {
                        toast("يمكنك الدخول باستخدام PIN");
                    }
                }
                @Override public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    toast("لم يتم التعرف على البصمة");
                }
            });
        } catch (Exception e) {
            if (allowPinFallback && AppStore.hasAppPin(this)) {
                toast("تعذر تشغيل البصمة، استخدم PIN");
                showPinUnlockDialog(onSuccess, true);
            } else {
                toast("تعذر تشغيل البصمة");
            }
        }
    }

    private void showPinUnlockDialog(Runnable onSuccess, boolean allowCancel) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(4), dp(8), dp(4));
        EditText pin = new EditText(this);
        pin.setHint("أدخل رقم PIN");
        pin.setGravity(Gravity.CENTER);
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(pin, new LinearLayout.LayoutParams(-1, dp(58)));
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("الدخول برقم PIN")
                .setView(layout)
                .setPositiveButton("دخول", null)
                .setNegativeButton(allowCancel ? "إلغاء" : "خروج", null)
                .create();
        dlg.setOnShowListener(d -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (AppStore.verifyAppPin(this, pin.getText().toString())) {
                    dlg.dismiss();
                    onSuccess.run();
                } else {
                    toast("PIN غير صحيح");
                }
            });
            dlg.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                dlg.dismiss();
                if (!allowCancel) finish();
            });
        });
        dlg.show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingReviewIntent(intent);
    }

    private void handleIncomingReviewIntent(Intent intent) {
        if (intent == null || content == null) return;
        String id = intent.getStringExtra("open_review_log_id");
        if (id != null && !id.trim().isEmpty()) {
            reviewFocusLogId = id.trim();
            showLogs();
            return;
        }
        if (intent.getBooleanExtra("open_pending_reviews", false)) {
            OperationLog first = AppStore.firstPendingCriticalLog(this);
            reviewFocusLogId = first == null ? "" : first.id;
            showLogs();
        }
    }

    private void showActivationScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(bg);
        setContentView(page);

        TextView top = new TextView(this);
        top.setText("ONLINE");
        top.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        top.setPadding(0, 0, dp(24), 0);
        top.setTextSize(22);
        top.setTypeface(appTypeface(true));
        top.setTextColor(Color.WHITE);
        top.setBackgroundColor(purpleLight);
        page.addView(top, new LinearLayout.LayoutParams(-1, dp(56)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setGravity(Gravity.CENTER_HORIZONTAL);
        body.setPadding(dp(24), dp(30), dp(24), dp(28));
        scroll.addView(body);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView lock = tv("🔐", 76, purpleLight, false);
        lock.setGravity(Gravity.CENTER);
        body.addView(lock, new LinearLayout.LayoutParams(-1, -2));

        TextView title = tv("تفعيل ONLINE", 30, text, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, dp(18), 0, dp(8));
        body.addView(title, titleLp);

        TextView note = tv("أرسل كود الطلب للإدارة، ثم أدخل رمز التفعيل المناسب. يدعم النظام: تجربة شهر، اشتراك سنة، أو مدى الحياة. رموز V30 موقّعة ومربوطة بهذا الجهاز وبتاريخ انتهاء ثابت.", 15, muted, false);
        note.setGravity(Gravity.CENTER);
        body.addView(note);

        LinearLayout info = cardBox();
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(-1, -2);
        infoLp.setMargins(0, dp(24), 0, dp(16));
        info.setLayoutParams(infoLp);
        info.addView(tv("كود طلب التفعيل", 16, text, true));
        TextView req = tv(AppStore.getRequestCode(this), 20, purpleLight, true);
        req.setGravity(Gravity.LEFT);
        req.setPadding(0, dp(8), 0, dp(10));
        info.addView(req);
        info.addView(separator());
        info.addView(tv("الرقم التسلسلي (Serial Number)", 16, text, true));
        TextView serial = tv(AppStore.getSerialNumber(this), 17, text, false);
        serial.setGravity(Gravity.LEFT);
        serial.setPadding(0, dp(8), 0, dp(10));
        info.addView(serial);
        info.addView(separator());
        info.addView(tv("معرف الجهاز (Device ID)", 16, text, true));
        TextView device = tv(AppStore.getDeviceId(this), 13, text, false);
        device.setGravity(Gravity.LEFT);
        device.setPadding(0, dp(8), 0, 0);
        info.addView(device);
        body.addView(info);

        TextView license = small(AppStore.licenseSummary(this));
        license.setGravity(Gravity.CENTER);
        LinearLayout licenseBox = cardBox();
        licenseBox.addView(tv("حالة التفعيل", 17, text, true));
        licenseBox.addView(license);
        body.addView(licenseBox);

        EditText networkName = activationInput("اسم الشبكة مثل: فور يو");
        networkName.setText(AppStore.getNetworkName(this));
        body.addView(networkName, new LinearLayout.LayoutParams(-1, dp(58)));

        Space sp1 = new Space(this);
        body.addView(sp1, new LinearLayout.LayoutParams(-1, dp(12)));

        EditText codeInput = activationInput("الصق رمز التفعيل كاملًا هنا");
        codeInput.setSingleLine(false);
        codeInput.setMinLines(3);
        codeInput.setMaxLines(6);
        codeInput.setGravity(Gravity.LEFT | Gravity.TOP);
        codeInput.setPadding(dp(14), dp(10), dp(14), dp(10));
        codeInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        body.addView(codeInput, new LinearLayout.LayoutParams(-1, dp(130)));

        TextView codeHint = small("رموز V30 تبدأ بـ ONL2. وهي طويلة لأنها موقّعة ومشفرة. الصق الرمز كاملًا كما وصلك من الإدارة.");
        codeHint.setGravity(Gravity.RIGHT);
        codeHint.setPadding(0, dp(8), 0, dp(12));
        body.addView(codeHint);

        Button activate = action("تفعيل الرمز", purpleLight, Color.rgb(56, 48, 78), v -> {
            String name = networkName.getText().toString().trim();
            String code = codeInput.getText().toString().trim();
            if (name.isEmpty()) {
                toast("اكتب اسم الشبكة أولًا");
                return;
            }
            if (code.isEmpty()) {
                toast("أدخل رمز التفعيل أولًا");
                return;
            }
            boolean activationOk = AppStore.activateOffline(this, code);
            if (!activationOk) {
                toast("رمز التفعيل غير صحيح أو لا يخص هذا الجهاز");
                return;
            }
            AppStore.setNetworkName(this, name);
            toast("تم تفعيل التطبيق بنجاح على هذا الجهاز");
            requestPermissionsIfNeeded();
            buildLayout();
            showHome();
        });
        body.addView(activate, new LinearLayout.LayoutParams(-1, dp(58)));

        Button copyText = action("عرض النص الذي ترسله للإدارة", card2, text, v -> showActivationRequestText());
        LinearLayout.LayoutParams copyLp = new LinearLayout.LayoutParams(-1, dp(54));
        copyLp.setMargins(0, dp(12), 0, 0);
        body.addView(copyText, copyLp);

        TextView hint = tv("ملاحظة مهمة: مولد V30 يمنع تكرار الشهر التجريبي لنفس كود الطلب ويحفظ سجل أجهزة محلي. كل ترخيص مربوط بالجهاز وبتاريخ انتهاء ثابت.", 12, muted, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(14), 0, dp(10));
        body.addView(hint);

        body.addView(supportContactBox());
    }

    private LinearLayout supportContactBox() {
        LinearLayout box = cardBox();
        box.setPadding(dp(14), dp(14), dp(14), dp(14));
        box.setBackground(round(Color.rgb(34, 30, 48), dp(20), Color.argb(80, 200,179,255), dp(1)));

        TextView title = tv("تواصل مع الدعم للتفعيل", 18, text, true);
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        TextView name = tv(SUPPORT_NAME, 15, purpleLight, true);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(6), 0, 0);
        box.addView(name);

        TextView phone = tv("رقم الدعم: " + SUPPORT_PHONE_LOCAL, 15, muted, false);
        phone.setGravity(Gravity.CENTER);
        phone.setPadding(0, dp(2), 0, dp(12));
        box.addView(phone);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        Button sms = action("✉ SMS", Color.rgb(52, 46, 70), text, v -> openSupportSms());
        Button call = action("☎ اتصال", Color.rgb(52, 46, 70), text, v -> openSupportCall());
        Button wa = action("☘ واتساب", Color.rgb(37, 211, 102), Color.WHITE, v -> openSupportWhatsApp());

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(0, dp(50), 1);
        btnLp.setMargins(dp(4), 0, dp(4), 0);
        row.addView(sms, btnLp);
        row.addView(call, new LinearLayout.LayoutParams(0, dp(50), 1));
        ((LinearLayout.LayoutParams)call.getLayoutParams()).setMargins(dp(4), 0, dp(4), 0);
        row.addView(wa, new LinearLayout.LayoutParams(0, dp(50), 1));
        ((LinearLayout.LayoutParams)wa.getLayoutParams()).setMargins(dp(4), 0, dp(4), 0);

        box.addView(row);

        TextView help = small("اضغط واتساب أو SMS لإرسال كود الطلب وبيانات الجهاز مباشرة إلى الدعم.");
        help.setGravity(Gravity.CENTER);
        help.setPadding(0, dp(12), 0, 0);
        box.addView(help);
        return box;
    }

    private String supportMessage() {
        return "السلام عليكم " + SUPPORT_NAME + "\n"
                + "طلب تفعيل تطبيق ONLINE Admin\n"
                + "نوع الطلب: شهر تجريبي / سنة / مدى الحياة\n"
                + "اسم الشبكة: " + AppStore.getNetworkName(this) + "\n"
                + "كود الطلب: " + AppStore.getRequestCode(this) + "\n"
                + "Serial: " + AppStore.getSerialNumber(this) + "\n"
                + "Device ID: " + AppStore.getDeviceId(this) + "\n"
                + "الشركة: " + Build.MANUFACTURER + "\n"
                + "الموديل: " + Build.MODEL + "\n"
                + "إصدار Android: " + Build.VERSION.RELEASE + "\n"
                + "SDK: " + Build.VERSION.SDK_INT + "\n"
                + "Package: " + getPackageName() + "\n"
                + "إصدار التطبيق: " + getAppVersionNameSafe() + "\n"
                + "وقت الطلب: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
    }

    private void openSupportWhatsApp() {
        openWhatsAppBusinessMessage(SUPPORT_PHONE_INTL, supportMessage());
    }

    private void openSupportCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SUPPORT_PHONE_LOCAL));
            startActivity(intent);
        } catch (Exception e) {
            toast("تعذر فتح الاتصال");
        }
    }

    private void openSupportSms() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + SUPPORT_PHONE_LOCAL));
            intent.putExtra("sms_body", supportMessage());
            startActivity(intent);
        } catch (Exception e) {
            toast("تعذر فتح الرسائل");
        }
    }

    private String getAppVersionNameSafe() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "غير معروف";
        }
    }

    private void showActivationRequestText() {
        String textToSend = supportMessage();
        new AlertDialog.Builder(this)
                .setTitle("أرسل هذا النص للإدارة")
                .setMessage(textToSend)
                .setNegativeButton("نسخ", (d, w) -> {
                    try {
                        android.content.ClipboardManager cm = (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("ONLINE activation request", textToSend));
                        toast("تم نسخ بيانات التفعيل");
                    } catch (Exception e) {
                        toast("تعذر النسخ");
                    }
                })
                .setPositiveButton("تم", null)
                .show();
    }

    private void performOnlineLicenseCheck(boolean fromActivationScreen) {
        toast("جاري الاتصال بسيرفر التفعيل...");
        new Thread(() -> {
            boolean ok = false;
            String msg = "";
            String status = "inactive";
            String expires = "";
            String network = AppStore.getNetworkName(this);
            try {
                String api = AppStore.getLicenseApiUrl(this);
                URL url = new URL(api);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                String data = "action=" + enc("verify")
                        + "&app=" + enc("ONLINE")
                        + "&request_code=" + enc(AppStore.getRequestCode(this))
                        + "&device_id=" + enc(AppStore.getDeviceId(this))
                        + "&serial=" + enc(AppStore.getSerialNumber(this))
                        + "&network_name=" + enc(network);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();

                InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                JSONObject json = new JSONObject(sb.toString());
                ok = json.optBoolean("ok", false);
                status = json.optString("status", ok ? "active" : "inactive");
                expires = json.optString("expires_at", "");
                msg = json.optString("message", ok ? "تم التحقق من الترخيص" : "لم يتم قبول الترخيص");
                String serverNetwork = json.optString("network_name", "");
                if (!serverNetwork.trim().isEmpty()) network = serverNetwork.trim();
            } catch (Exception e) {
                msg = "تعذر الاتصال بسيرفر التفعيل: " + e.getMessage();
            }

            final boolean finalOk = ok;
            final String finalMsg = msg;
            final String finalStatus = status;
            final String finalExpires = expires;
            final String finalNetwork = network;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalOk && "active".equalsIgnoreCase(finalStatus)) {
                    AppStore.setNetworkName(this, finalNetwork);
                    AppStore.saveOnlineLicense(this, finalStatus, finalExpires, finalMsg, AppStore.getRequestCode(this));
                    toast("تم تفعيل / تحديث الاشتراك بنجاح");
                    requestPermissionsIfNeeded();
                    buildLayout();
                    showHome();
                } else {
                    toast(finalMsg);
                    if (fromActivationScreen) showActivationScreen();
                }
            });
        }).start();
    }

    private String enc(String value) throws Exception {
        return URLEncoder.encode(value == null ? "" : value, "UTF-8");
    }


    private EditText activationInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        input.setTextColor(text);
        input.setHintTextColor(muted);
        input.setTextSize(16);
        input.setTypeface(appTypeface(false));
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setBackground(round(card2, dp(12), borderSoft, dp(1)));
        return input;
    }

    private void requestPermissionsIfNeeded() {
        ArrayList<String> perms = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.RECEIVE_SMS);
        if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.READ_SMS);
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.SEND_SMS);
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.READ_CONTACTS);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.POST_NOTIFICATIONS);
        if (!perms.isEmpty()) requestPermissions(perms.toArray(new String[0]), 10);
    }

    private void buildLayout() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(bg);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

        root.addView(header(), new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(18), dp(16), dp(18));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(Color.rgb(3, 4, 12));
        nav.setPadding(dp(6), dp(8), dp(6), dp(8));
        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));
        rebuildNav();
    }

    private View header() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(14), dp(16), dp(14), dp(10));
        wrap.setBackgroundColor(bg);

        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.VERTICAL);
        h.setPadding(dp(18), dp(20), dp(18), dp(20));
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(25, 219, 230), Color.rgb(35, 74, 255), Color.rgb(143, 55, 255)});
        gd.setCornerRadius(dp(30));
        gd.setStroke(dp(1), Color.argb(120, 255, 255, 255));
        h.setBackground(gd);
        if (Build.VERSION.SDK_INT >= 21) h.setElevation(dp(7));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        View avatar = networkLogoView(54);
        row.addView(avatar, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setGravity(Gravity.RIGHT);
        titleBox.addView(tv("مرحبًا 👋", 14, Color.argb(235, 255,255,255), false));
        TextView title = tv(AppStore.getNetworkName(this), 28, Color.WHITE, true);
        title.setGravity(Gravity.RIGHT);
        titleBox.addView(title);
        String phone = AppStore.getNetworkPhone(this);
        String subText = (phone == null || phone.trim().isEmpty()) ? "إدارة وبيع كروت الإنترنت تلقائيًا" : "رقم التواصل: " + phone.trim();
        TextView sub = tv(subText, 11, Color.argb(215, 255,255,255), false);
        sub.setGravity(Gravity.RIGHT);
        titleBox.addView(sub);

        row.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = tv("4U", 16, Color.rgb(40, 27, 7), true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(gold, dp(24), Color.argb(180, 255, 235, 167), dp(1)));
        row.addView(badge, new LinearLayout.LayoutParams(dp(52), dp(52)));

        h.addView(row);
        wrap.addView(h, new LinearLayout.LayoutParams(-1, -2));
        return wrap;
    }

    private View networkLogoView(int sizeDp) {
        FrameLayout frame = new FrameLayout(this);
        frame.setPadding(dp(3), dp(3), dp(3), dp(3));
        frame.setBackground(round(Color.argb(42, 255, 255, 255), dp(sizeDp / 2), neonCyan, dp(1)));
        if (Build.VERSION.SDK_INT >= 21) frame.setElevation(dp(5));

        String path = AppStore.getNetworkLogoPath(this);
        if (path != null && !path.trim().isEmpty()) {
            try {
                File f = new File(path.trim());
                if (f.exists()) {
                    Bitmap raw = BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (raw != null) {
                        ImageView iv = new ImageView(this);
                        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        iv.setImageBitmap(makeCircularLogoBitmap(raw, dp(sizeDp - 6)));
                        frame.addView(iv, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
                        return frame;
                    }
                }
            } catch (Exception ignored) {}
        }
        TextView fallback = tv("4U", Math.max(14, sizeDp / 3), Color.rgb(40, 27, 7), true);
        fallback.setGravity(Gravity.CENTER);
        fallback.setBackground(round(gold, dp(sizeDp / 2), Color.argb(180, 255, 235, 167), dp(1)));
        frame.addView(fallback, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        return frame;
    }

    private Bitmap makeCircularLogoBitmap(Bitmap src, int outSizePx) {
        int size = Math.max(dp(38), outSizePx);
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.rgb(12, 16, 30));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, p);
        Path clip = new Path();
        clip.addCircle(size / 2f, size / 2f, size / 2f - dp(1), Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        int pad = dp(4);
        float sw = Math.max(1, src.getWidth());
        float sh = Math.max(1, src.getHeight());
        float scale = Math.min((size - pad * 2f) / sw, (size - pad * 2f) / sh);
        float dw = sw * scale;
        float dh = sh * scale;
        RectF dst = new RectF((size - dw) / 2f, (size - dh) / 2f, (size + dw) / 2f, (size + dh) / 2f);
        canvas.drawBitmap(src, null, dst, p);
        canvas.restore();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dp(1));
        p.setColor(Color.argb(160, 255, 255, 255));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - dp(1), p);
        return out;
    }

    private void rebuildNav() {
        nav.removeAllViews();
        nav.addView(navButton("الإعدادات", "settings", "⚙", v -> showSettings()));
        nav.addView(navButton("دفتر", "ledger", "▤", v -> showSmartLedger()));
        nav.addView(navButton("السجل", "logs", "◷", v -> showLogs()));
        nav.addView(navButton("استيراد", "import", "⇧", v -> showImport()));
        nav.addView(navButton("بيع مباشر", "direct", "✉", v -> showDirectSale()));
        nav.addView(navButton("الفئات", "categories", "▦", v -> showCategories()));
        nav.addView(navButton("الرئيسية", "home", "⌂", v -> showHome()));
    }

    private Button navButton(String label, String key, String icon, View.OnClickListener listener) {
        boolean selected = key.equals(activeTab);
        Button b = new Button(this);
        b.setText(icon + "\n" + label);
        b.setTextSize(selected ? 11 : 10);
        b.setTypeface(appTypeface(selected));
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setTextColor(selected ? Color.WHITE : Color.rgb(150, 156, 178));
        int bgColor = selected ? Color.rgb(31, 24, 62) : Color.rgb(8, 10, 22);
        int stroke = selected ? neonCyan : Color.rgb(31, 38, 62);
        b.setBackground(round(bgColor, dp(20), stroke, selected ? dp(2) : dp(1)));
        b.setPadding(dp(2), dp(3), dp(2), dp(3));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(64), 1);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        b.setLayoutParams(lp);
        if (Build.VERSION.SDK_INT >= 21) b.setElevation(selected ? dp(5) : dp(1));
        return b;
    }

    private void setTab(String key) { activeTab = key; rebuildNav(); }
    private void clear() { content.removeAllViews(); content.setPadding(dp(18), dp(18), dp(18), dp(18)); content.setBackgroundColor(bg); }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radius);
        if (strokeWidth > 0) gd.setStroke(strokeWidth, strokeColor);
        return gd;
    }

    private void applyNeonPress(View v) {
        v.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(1.035f).scaleY(1.035f).alpha(0.96f).setDuration(95).start();
                if (Build.VERSION.SDK_INT >= 21) view.setElevation(dp(10));
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(120).start();
                if (Build.VERSION.SDK_INT >= 21) view.setElevation(dp(4));
            }
            return false;
        });
    }

    private Typeface appTypeface(boolean bold) {
        return Typeface.create(bold ? "sans-serif-medium" : "sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL);
    }

    private TextView tv(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT);
        t.setTypeface(appTypeface(bold));
        return t;
    }

    private LinearLayout cardBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        box.setBackground(round(card, dp(24), borderSoft, dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(14));
        box.setLayoutParams(lp);
        applyNeonPress(box);
        if (Build.VERSION.SDK_INT >= 21) box.setElevation(dp(4));
        return box;
    }

    private LinearLayout reviewAlertBox(boolean focused) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(14), dp(14), dp(14));
        int bgColor = focused ? Color.rgb(70, 18, 30) : Color.rgb(48, 24, 34);
        box.setBackground(round(bgColor, dp(18), Color.rgb(255, 64, 80), focused ? dp(3) : dp(2)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(12));
        box.setLayoutParams(lp);
        return box;
    }

    private View separator() {
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(38, 45, 76));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(1));
        lp.setMargins(0, dp(8), 0, dp(8));
        line.setLayoutParams(lp);
        return line;
    }

    private TextView title(String s) {
        TextView t = tv("✦ " + s, 22, text, true);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setPadding(dp(14), dp(11), dp(14), dp(11));
        t.setBackground(round(Color.rgb(10, 14, 30), dp(18), Color.argb(145, 32, 231, 255), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(4), 0, dp(14));
        t.setLayoutParams(lp);
        if (Build.VERSION.SDK_INT >= 21) t.setElevation(dp(3));
        return t;
    }

    private TextView small(String s) {
        TextView t = tv(s, 13, muted, false);
        t.setLineSpacing(2, 1.1f);
        return t;
    }

    private TextView badge(String s, int color) {
        TextView b = tv(s, 12, color, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(10), dp(5), dp(10), dp(5));
        b.setBackground(round(Color.argb(28, Color.red(color), Color.green(color), Color.blue(color)), dp(18), color, dp(1)));
        return b;
    }

    private Button action(String label, int bgColor, int fgColor, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(fgColor);
        b.setTypeface(appTypeface(true));
        b.setAllCaps(false);
        b.setPadding(dp(8), dp(8), dp(8), dp(8));
        int stroke = (bgColor == card || bgColor == card2 || bgColor == Color.TRANSPARENT) ? borderSoft : Color.argb(110, 255,255,255);
        b.setBackground(round(bgColor, dp(18), stroke, dp(1)));
        b.setOnClickListener(listener);
        applyNeonPress(b);
        if (Build.VERSION.SDK_INT >= 21) b.setElevation(dp(2));
        return b;
    }

    private void styleNeonDialog(AlertDialog dlg, int accentColor) {
        if (dlg == null) return;
        dlg.setOnShowListener(d -> {
            try {
                if (dlg.getWindow() != null) {
                    dlg.getWindow().setBackgroundDrawable(round(Color.rgb(28, 31, 48), dp(22), accentColor, dp(1)));
                }
                Button pos = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
                Button neg = dlg.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button neu = dlg.getButton(AlertDialog.BUTTON_NEUTRAL);
                if (pos != null) { pos.setTextColor(accentColor); pos.setTypeface(appTypeface(true)); }
                if (neg != null) { neg.setTextColor(muted); neg.setTypeface(appTypeface(true)); }
                if (neu != null) { neu.setTextColor(orange); neu.setTypeface(appTypeface(true)); }
            } catch (Exception ignored) {}
        });
    }

    private void showNeonAlert(String title, String message, int accentColor, boolean sound) {
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("حسنًا", null)
                .create();
        styleNeonDialog(dlg, accentColor);
        dlg.show();
        if (sound) playAlertSound();
    }

    private void playAlertSound() {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 180);
        } catch (Exception ignored) {}
    }

    private void toneOk() { playAlertSound(); }

    private void toneAlert() { playAlertSound(); }

    private void showLowStockWarningIfNeeded(int amount, int remainingAfterSale) {
        if (remainingAfterSale > 0 && remainingAfterSale < 10) {
            showNeonAlert("تنبيه مخزون منخفض", "تم البيع بنجاح، لكن المتبقي من فئة " + amount + " هو " + remainingAfterSale + " كروت فقط.\n\nإذا أصبح المخزون 10 أو أكثر ستتوقف هذه التنبيهات تلقائيًا.", orange, true);
        }
    }

    private Button goldAnimatedAction(String label, View.OnClickListener listener) {
        Button b = action(label, Color.rgb(168, 113, 24), Color.WHITE, listener);
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.rgb(123, 78, 8), Color.rgb(255, 205, 91), Color.rgb(155, 94, 14)});
        gd.setCornerRadius(dp(18));
        gd.setStroke(dp(1), Color.argb(180, 255, 235, 167));
        b.setBackground(gd);
        b.setTextSize(16);
        b.setPadding(dp(10), dp(10), dp(10), dp(10));
        b.postDelayed(new Runnable() {
            boolean up = true;
            @Override public void run() {
                if (b.getWindowToken() == null) return;
                float s = up ? 1.035f : 1.0f;
                b.animate().scaleX(s).scaleY(s).alpha(up ? 0.94f : 1.0f).setDuration(650).withEndAction(() -> b.postDelayed(this, 450)).start();
                up = !up;
            }
        }, 450);
        return b;
    }

    private void showHome() {
        setTab("home");
        clear();

        content.addView(dashboardWelcomeCard());
        content.addView(dashboardStatsGrid());
        content.addView(title("الخدمات الرئيسية"));
        content.addView(dashboardServicesGrid());
        content.addView(title("آخر العمليات"));
        content.addView(recentTransactionsCard());
    }

    private View dashboardWelcomeCard() {
        LinearLayout box = cardBox();
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(12, 18, 40), Color.rgb(18, 22, 52), Color.rgb(26, 18, 55)});
        gd.setCornerRadius(dp(28));
        gd.setStroke(dp(1), Color.rgb(34, 53, 105));
        box.setBackground(gd);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView state = tv((AppStore.isAutoSendEnabled(this) ? "نشط" : "متوقف") + "  ●", 14, AppStore.isAutoSendEnabled(this) ? green : red, true);
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(14), dp(8), dp(14), dp(8));
        state.setBackground(round(Color.argb(32, Color.red(AppStore.isAutoSendEnabled(this) ? green : red), Color.green(AppStore.isAutoSendEnabled(this) ? green : red), Color.blue(AppStore.isAutoSendEnabled(this) ? green : red)), dp(24), AppStore.isAutoSendEnabled(this) ? green : red, dp(1)));
        top.addView(state);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setGravity(Gravity.RIGHT);
        titleBox.addView(tv("لوحة التحكم", 15, muted, false));
        titleBox.addView(tv(AppStore.getNetworkName(this), 25, text, true));
        top.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));

        box.addView(top);
        box.addView(separator());
        TextView note = small(AppStore.isAutoSendEnabled(this)
                ? "النظام يعمل. تم تحديث الشكل فقط بأسلوب داكن متوهج بدون تغيير منطق السلف أو الرسائل."
                : "الإرسال التلقائي متوقف. يمكنك تشغيله من الإعدادات عند الحاجة.");
        box.addView(note);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(12), 0, 0);
        actions.addView(action("بيع كرت", Color.rgb(24, 197, 220), Color.rgb(2, 12, 22), v -> showDirectSale()), new LinearLayout.LayoutParams(0, dp(52), 1));
        actions.addView(action("الدفتر", Color.rgb(42, 31, 84), Color.WHITE, v -> showSmartLedger()), new LinearLayout.LayoutParams(0, dp(52), 1));
        box.addView(actions);
        return box;
    }

    private View dashboardStatsGrid() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        row1.addView(dashboardStatCard("كروت مباعة", String.valueOf(AppStore.sentOperationsCount(this)), "💳", purple), new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(dashboardStatCard("العمليات", String.valueOf(AppStore.processedOperationsCount(this)), "📊", gold), new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(dashboardStatCard("الزبائن", String.valueOf(AppStore.loadLedgerCustomers(this).size()), "👥", purpleDark), new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(dashboardStatCard("الديون", AppStore.ledgerTotalDebt(this) + " ر.ي", "📒", orange), new LinearLayout.LayoutParams(0, -2, 1));

        grid.addView(row1);
        grid.addView(row2);
        return grid;
    }

    private View dashboardStatCard(String label, String value, String icon, int accent) {
        LinearLayout box = cardBox();
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.setMargins(dp(5), dp(5), dp(5), dp(9));
        box.setLayoutParams(blp);
        box.setGravity(Gravity.CENTER);
        box.setBackground(round(Color.rgb(18, 23, 43), dp(24), Color.argb(145, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(1)));
        TextView iconView = tv(icon, 22, accent, true);
        iconView.setGravity(Gravity.CENTER);
        box.addView(iconView);
        TextView number = tv(value, 22, text, true);
        number.setGravity(Gravity.CENTER);
        box.addView(number);
        TextView caption = tv(label, 12, muted, false);
        caption.setGravity(Gravity.CENTER);
        box.addView(caption);
        return box;
    }

    private View dashboardServicesGrid() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);

        row1.addView(serviceTile("📒", "الدفتر", "حسابات وسلف", v -> showSmartLedger()), new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(serviceTile("💵", "بيع كرت", "بيع سريع", v -> showDirectSale()), new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(serviceTile("▦", "الفئات", "مخزون الكروت", v -> showCategories()), new LinearLayout.LayoutParams(0, -2, 1));

        row2.addView(serviceTile("📄", "التقارير", "PDF وإحصاءات", v -> showLogs()), new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(serviceTile("⇧", "استيراد", "TXT و Excel", v -> showImport()), new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(serviceTile("⚙", "الإعدادات", "تحكم كامل", v -> showSettings()), new LinearLayout.LayoutParams(0, -2, 1));

        grid.addView(row1);
        grid.addView(row2);
        return grid;
    }

    private View serviceTile(String icon, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout box = cardBox();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(5), dp(5), dp(5), dp(10));
        box.setLayoutParams(lp);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(16), dp(8), dp(16));
        int accent = title.contains("بيع") ? neonCyan : (title.contains("تقارير") ? gold : (title.contains("تنبيهات") ? neonPink : purple));
        box.setBackground(round(Color.rgb(14, 17, 31), dp(26), Color.argb(170, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(title.contains("بيع") ? 2 : 1)));
        box.setOnClickListener(listener);
        if (Build.VERSION.SDK_INT >= 21) box.setElevation(title.contains("بيع") ? dp(8) : dp(4));
        TextView i = tv(icon, 25, accent, true);
        i.setGravity(Gravity.CENTER);
        i.setPadding(0, dp(10), 0, dp(10));
        i.setBackground(round(Color.argb(28, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(40), Color.TRANSPARENT, 0));
        box.addView(i, new LinearLayout.LayoutParams(dp(58), dp(58)));
        TextView t = tv(title, 14, text, true);
        t.setGravity(Gravity.CENTER);
        box.addView(t);
        TextView sub = tv(subtitle, 10, muted, false);
        sub.setGravity(Gravity.CENTER);
        box.addView(sub);
        return box;
    }

    private View recentTransactionsCard() {
        LinearLayout box = cardBox();
        ArrayList<OperationLog> logs = AppStore.loadRecentLogs(this, 4);
        if (logs.isEmpty()) {
            box.addView(small("لا توجد عمليات حديثة حتى الآن."));
            return box;
        }
        int max = Math.min(4, logs.size());
        for (int i = 0; i < max; i++) {
            box.addView(recentOperationRow(logs.get(i), i < max - 1));
        }
        box.addView(action("عرض السجل كاملًا", purpleLight, purple, v -> showLogs()), new LinearLayout.LayoutParams(-1, dp(48)));
        return box;
    }

    private View recentOperationRow(OperationLog log, boolean line) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setGravity(Gravity.RIGHT);
        String name = (log.customerName == null || log.customerName.trim().isEmpty()) ? (log.customerPhone == null ? "عملية" : log.customerPhone) : log.customerName;
        info.addView(tv((log.status == null ? "عملية" : log.status) + " - " + name, 14, text, true));
        info.addView(small((log.createdAt == null ? "" : log.createdAt) + " | " + log.amount + " ر.ي"));
        row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        TextView amount = tv(log.amount + "", 14, green, true);
        amount.setGravity(Gravity.CENTER);
        amount.setPadding(dp(8), dp(4), dp(8), dp(4));
        amount.setBackground(round(Color.rgb(232, 245, 233), dp(16), Color.TRANSPARENT, 0));
        row.addView(amount);

        if (!line) return row;
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(row);
        wrap.addView(separator());
        return wrap;
    }

    private View stat(String label, String value) {
        LinearLayout box = cardBox();
        TextView l = tv(label, 12, muted, false); l.setGravity(Gravity.CENTER);
        TextView v = tv(value, 28, text, true); v.setGravity(Gravity.CENTER);
        box.addView(l); box.addView(v);
        return box;
    }

    private View categorySummary(CategoryItem c) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        box.setBackground(round(card, dp(18), Color.argb(55, 255,255,255), dp(1)));

        TextView amount = tv(String.valueOf(c.amount), 26, purpleLight, true);
        amount.setGravity(Gravity.RIGHT);
        box.addView(amount);
        int available = AppStore.availableCount(this, c.amount);
        if (available <= 0) box.addView(badge("نفدت الكمية", red));
        else if (available < 10) box.addView(badge("⚠ مخزون منخفض: " + available, orange));
        box.addView(small(c.amount + " ر.ي"));
        box.addView(separator());
        box.addView(small("إجمالي الكروت: " + AppStore.cardsByAmountCount(this, c.amount)));
        box.addView(small("كروت مباعة: " + AppStore.soldCount(this, c.amount)));
        box.addView(small("الكروت المتبقية: " + available));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(action("🗑", Color.TRANSPARENT, red, v -> confirmDeleteCategory(c)), new LinearLayout.LayoutParams(0, dp(44), 1));
        actions.addView(action("✎", Color.TRANSPARENT, Color.rgb(41, 167, 255), v -> showCategoryDialog(c)), new LinearLayout.LayoutParams(0, dp(44), 1));
        box.addView(actions);
        return box;
    }
    private Button categoryChoiceButton(CategoryItem c) {
        boolean selected = c.amount == selectedAmount;
        int bgColor = selected ? Color.rgb(74, 55, 126) : card2;
        int borderColor = selected ? purpleLight : Color.argb(70, 255, 255, 255);
        int fgColor = selected ? Color.WHITE : text;
        Button b = new Button(this);
        b.setText(c.amount + " ريال");
        b.setTextColor(fgColor);
        b.setTextSize(16);
        b.setTypeface(appTypeface(true));
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackground(round(bgColor, dp(16), borderColor, selected ? dp(2) : dp(1)));
        b.setOnClickListener(v -> {
            selectedAmount = c.amount;
            showImport();
        });
        return b;
    }


    private void showCategories() {
        setTab("categories");
        clear();
        content.addView(title("إدارة الفئات"));

        LinearLayout add = cardBox();
        add.addView(tv("إضافة فئة جديدة", 17, text, true));
        add.addView(small("أي فئة تضيفها ستظهر مع الفئات الأساسية وتصبح قابلة للإدارة."));
        add.addView(action("إضافة فئة", purple, Color.WHITE, v -> showCategoryDialog(null)));
        content.addView(add);

        LinearLayout cleanup = cardBox();
        cleanup.addView(tv("تنظيف الكروت المباعة القديمة", 17, text, true));
        cleanup.addView(small("يحذف أرقام الكروت المباعة فقط، ولا يحذف سجلات البيع المالية. يمكنك التنظيف التلقائي بعد 10 أيام أو اختيار تاريخ محدد بنفسك."));
        cleanup.addView(action("تنظيف المباعة +10 أيام", Color.rgb(82,30,42), Color.WHITE, v -> confirmCleanupOldSoldCards()));
        cleanup.addView(action("حذف المباعة حسب تاريخ أختاره", Color.rgb(120,60,35), Color.WHITE, v -> showCleanupSoldCardsByDateDialog()));
        content.addView(cleanup);

        for (CategoryItem c : AppStore.loadCategories(this)) {
            LinearLayout box = cardBox();
            int available = AppStore.availableCount(this, c.amount);
            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            TextView nameTitle = tv(c.name + " - " + c.amount + " ريال", 18, text, true);
            head.addView(nameTitle, new LinearLayout.LayoutParams(0, -2, 1));
            if (available <= 0) head.addView(badge("نفدت", red));
            else if (available < 10) head.addView(badge("⚠ منخفض", orange));
            box.addView(head);
            box.addView(small("الحالة: " + (c.active ? "مفعلة" : "موقفة") + "\nالمتاح: " + available + " | المباع: " + AppStore.soldCount(this, c.amount)));

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.addView(action("عرض الكروت", card2, text, v -> showCardsForCategory(c)), new LinearLayout.LayoutParams(0, -2, 1));
            actions.addView(action("تعديل", purple, Color.WHITE, v -> showCategoryDialog(c)), new LinearLayout.LayoutParams(0, -2, 1));
            box.addView(actions);

            Button del = action("حذف الفئة", Color.rgb(82,30,42), Color.WHITE, v -> confirmDeleteCategory(c));
            box.addView(del);
            content.addView(box);
        }
    }

    private void showCategoryDialog(CategoryItem category) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(14), dp(12), dp(14), dp(6));
        layout.setBackgroundColor(Color.rgb(28, 31, 48));

        EditText amount = neonInput("قيمة الفئة مثل 100", InputType.TYPE_CLASS_NUMBER);
        amount.setText(category == null ? "" : String.valueOf(category.amount));

        EditText name = neonInput("اسم الفئة", InputType.TYPE_CLASS_TEXT);
        name.setText(category == null ? "" : category.name);

        CheckBox active = channelCheckBox("الفئة مفعلة", category == null || category.active, neonCyan);

        layout.addView(amount);
        layout.addView(name);
        layout.addView(active);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(category == null ? "إضافة فئة" : "تعديل فئة")
                .setView(layout)
                .setPositiveButton("حفظ", (d, w) -> {
                    int a = 0;
                    try { a = Integer.parseInt(amount.getText().toString().trim()); } catch (Exception ignored) {}
                    if (a <= 0) { toast("أدخل فئة صحيحة"); return; }
                    String n = name.getText().toString().trim();
                    if (n.isEmpty()) n = "فئة " + a;
                    if (category == null) {
                        boolean ok = AppStore.addCategory(this, a);
                        if (!ok) toast("الفئة موجودة مسبقًا");
                    } else {
                        AppStore.updateCategory(this, category, a, n, active.isChecked());
                    }
                    showCategories();
                })
                .setNegativeButton("إلغاء", null)
                .create();
        styleNeonDialog(dlg, neonCyan);
        dlg.show();
    }

    private EditText neonInput(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(inputType);
        e.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        e.setTextColor(text);
        e.setHintTextColor(muted);
        e.setTextSize(16);
        e.setTypeface(appTypeface(false));
        e.setSingleLine(true);
        e.setPadding(dp(12), 0, dp(12), 0);
        e.setBackground(round(Color.rgb(12, 16, 31), dp(14), Color.rgb(61, 73, 112), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(58));
        lp.setMargins(0, dp(6), 0, dp(8));
        e.setLayoutParams(lp);
        return e;
    }

    private void confirmDeleteCategory(CategoryItem c) {
        int total = AppStore.cardsByAmountCount(this, c.amount);
        new AlertDialog.Builder(this)
                .setTitle("حذف الفئة")
                .setMessage("اختر طريقة الحذف لفئة " + c.amount + " ريال.\n\nعدد الكروت داخل الفئة: " + total + " كرت.")
                .setPositiveButton("حذف الفئة فقط", (d,w) -> { AppStore.deleteCategory(this, c); showCategories(); })
                .setNeutralButton("حذف الفئة والكروت", (d,w) -> {
                    int removed = AppStore.deleteCardsByAmount(this, c.amount);
                    AppStore.deleteCategory(this, c);
                    toast("تم حذف الفئة و" + removed + " كرت");
                    showCategories();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showCardsForCategory(CategoryItem c) {
        setTab("categories");
        clear();
        content.addView(title("كروت " + c.name));

        LinearLayout tools = cardBox();
        tools.addView(tv("إدارة كروت الفئة", 17, text, true));
        tools.addView(small("تم تنظيف هذه النسخة: إضافة الكروت الآن من داخل التطبيق فقط عبر الإدخال اليدوي أو ملفات TXT أو Excel/CSV."));
        tools.addView(action("➕ إضافة يدوية", purple, Color.WHITE, v -> { selectedAmount = c.amount; showManualAddDialog(); }));
        tools.addView(action("🔎 بحث عن كرت", card2, text, v -> showCardSearchDialog(c.amount)));
        tools.addView(action("📄 استيراد TXT لهذه الفئة", card2, text, v -> { selectedAmount = c.amount; openTxtFile(); }));
        tools.addView(action("📊 استيراد Excel / CSV لهذه الفئة", card2, text, v -> { selectedAmount = c.amount; openExcelFile(); }));
        tools.addView(action("🗑 حذف كل كروت هذه الفئة", Color.rgb(82,30,42), Color.WHITE, v -> confirmDeleteAllCardsForCategory(c)));
        content.addView(tools);

        if (cardsVisibleAmount != c.amount) {
            cardsVisibleAmount = c.amount;
            cardsVisibleLimit = AppStore.performanceCardPageSize();
        }
        int totalCards = AppStore.cardsByAmountCount(this, c.amount);
        ArrayList<CardItem> list = AppStore.cardsByAmountLimited(this, c.amount, 0, cardsVisibleLimit);
        if (list.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(small("لا توجد كروت في هذه الفئة."));
            content.addView(empty);
            return;
        }

        LinearLayout perfInfo = cardBox();
        perfInfo.addView(tv("عرض سريع", 16, text, true));
        perfInfo.addView(small("المعروض الآن: " + list.size() + " من " + totalCards + " كرت. لا يتم تحميل كل الكروت دفعة واحدة حتى يبقى التطبيق سريعًا."));
        if (totalCards > list.size()) {
            perfInfo.addView(action("تحميل " + AppStore.performanceCardPageSize() + " كرت أخرى", card2, text, v -> {
                cardsVisibleLimit += AppStore.performanceCardPageSize();
                showCardsForCategory(c);
            }));
        }
        content.addView(perfInfo);

        for (CardItem item : list) {
            LinearLayout box = cardBox();
            box.addView(badge(item.sold ? "مباع" : "متاح", item.sold ? orange : green));
            box.addView(tv(displayCardCode(item.code), 17, text, true));
            TextView addedAt = small("تاريخ الإضافة: " + ((item.createdAt == null || item.createdAt.trim().isEmpty()) ? "غير مسجل" : item.createdAt));
            addedAt.setTextSize(10);
            addedAt.setTextColor(muted);
            box.addView(addedAt);
            if (item.sold) box.addView(small("أرسل إلى: " + item.buyerPhone + "\nالوقت: " + item.soldAt));
            LinearLayout actions = new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.addView(action("تعديل", purple, Color.WHITE, v -> showCardEditDialog(item)), new LinearLayout.LayoutParams(0, -2, 1));
            actions.addView(action("حذف", Color.rgb(82,30,42), Color.WHITE, v -> confirmDeleteCard(item, c)), new LinearLayout.LayoutParams(0, -2, 1));
            box.addView(actions);
            content.addView(box);
        }
    }

    private void confirmDeleteCard(CardItem item, CategoryItem c) {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد حذف الكرت")
                .setMessage("هل تريد حذف هذا الكرت؟\n\n" + item.code + "\n\nلن يمكن التراجع عن الحذف إلا إذا كانت لديك نسخة احتياطية.")
                .setPositiveButton("موافق، حذف", (d,w) -> { AppStore.deleteCard(this, item.id); showCardsForCategory(c); })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmDeleteAllCardsForCategory(CategoryItem c) {
        int total = AppStore.cardsByAmountCount(this, c.amount);
        new AlertDialog.Builder(this)
                .setTitle("حذف كل كروت الفئة")
                .setMessage("سيتم حذف كل الكروت العادية داخل فئة " + c.amount + " ريال.\n\nالعدد الحالي: " + total + " كرت.\n\nلن يمكن التراجع إلا من نسخة احتياطية.")
                .setPositiveButton("موافق، حذف الكل", (d,w) -> {
                    int removed = AppStore.deleteCardsByAmount(this, c.amount);
                    toast("تم حذف " + removed + " كرت من الفئة");
                    showCardsForCategory(c);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmCleanupOldSoldCards() {
        int count = AppStore.countSoldCardsOlderThanDays(this, 10);
        new AlertDialog.Builder(this)
                .setTitle("تنظيف الكروت المباعة القديمة")
                .setMessage("سيتم حذف الكروت المباعة التي مر على بيعها أكثر من 10 أيام فقط.\n\nالعدد المؤهل الآن: " + count + " كرت.\n\nلن يتم حذف سجلات البيع المالية، وسيتم تسجيل عملية التنظيف في السجل.\n\nهل تريد المتابعة؟")
                .setPositiveButton("نعم، تنظيف", (d,w) -> {
                    int removed = AppStore.cleanupSoldCardsOlderThanDays(this, 10);
                    toast("تم تنظيف " + removed + " كرت مباع قديم");
                    showCategories();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showCleanupSoldCardsByDateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView info = small("اختر التاريخ النهائي. سيتم حذف أرقام الكروت المباعة حتى هذا التاريخ فقط، مع إبقاء سجلات البيع المالية والتقارير. الصيغ المقبولة: 2026-06-01 أو 2026/06/01.");
        EditText date = new EditText(this);
        date.setHint("مثال: 2026-06-01");
        date.setGravity(Gravity.RIGHT);
        date.setInputType(InputType.TYPE_CLASS_TEXT);
        date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        layout.addView(info);
        layout.addView(date);
        new AlertDialog.Builder(this)
                .setTitle("حذف المباعة حسب التاريخ")
                .setView(layout)
                .setPositiveButton("متابعة", (d,w) -> {
                    String chosenDate = date.getText().toString().trim();
                    int count = AppStore.countSoldCardsUntilDate(this, chosenDate);
                    if (count < 0) { toast("صيغة التاريخ غير صحيحة"); return; }
                    new AlertDialog.Builder(this)
                            .setTitle("تأكيد نهائي")
                            .setMessage("سيتم حذف أرقام الكروت المباعة حتى تاريخ: " + chosenDate
                                    + "\n\nالعدد المؤهل الآن: " + count + " كرت."
                                    + "\n\nلن يتم حذف سجلات البيع المالية، وسيتم تسجيل العملية في السجل. لا يمكن التراجع إلا من نسخة احتياطية."
                                    + "\n\nهل تريد المتابعة؟")
                            .setPositiveButton("نعم، حذف", (dd,ww) -> {
                                int removed = AppStore.cleanupSoldCardsUntilDate(this, chosenDate);
                                if (removed < 0) { toast("صيغة التاريخ غير صحيحة"); return; }
                                toast("تم حذف " + removed + " كرت مباع حسب التاريخ");
                                showCategories();
                            })
                            .setNegativeButton("إلغاء", null)
                            .show();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showCardSearchDialog(int amountFilter) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        EditText query = new EditText(this);
        query.setHint("اكتب رقم الكرت أو جزء منه");
        query.setGravity(Gravity.RIGHT);
        query.setInputType(InputType.TYPE_CLASS_TEXT);
        TextView result = small("سيظهر البحث هنا.");
        result.setTextSize(12);
        layout.addView(query);
        layout.addView(result);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(amountFilter > 0 ? "بحث داخل فئة " + amountFilter : "بحث عن كرت في النظام")
                .setView(layout)
                .setPositiveButton("إغلاق", null)
                .create();
        query.addTextChangedListener(simpleWatcher(value -> {
            ArrayList<CardItem> found = AppStore.searchCards(this, value);
            StringBuilder sb = new StringBuilder();
            int shown = 0;
            for (CardItem item : found) {
                if (amountFilter > 0 && item.amount != amountFilter) continue;
                shown++;
                sb.append(shown).append(") ").append(displayCardCode(item.code))
                        .append(" | فئة: ").append(item.amount)
                        .append(" | ").append(item.sold ? "مباع" : "متاح")
                        .append(" | إضافة: ").append(item.createdAt == null || item.createdAt.trim().isEmpty() ? "غير مسجل" : item.createdAt);
                if (item.sold) sb.append(" | إلى: ").append(item.buyerPhone);
                sb.append("\n");
                if (shown >= 30) { sb.append("... تم عرض أول 30 نتيجة فقط"); break; }
            }
            result.setText(sb.length() == 0 ? "لا توجد نتيجة مطابقة." : sb.toString());
        }));
        dialog.show();
    }

    private void showCardEditDialog(CardItem card) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText amount = new EditText(this); amount.setInputType(InputType.TYPE_CLASS_NUMBER); amount.setText(String.valueOf(card.amount));
        EditText code = new EditText(this); code.setInputType(InputType.TYPE_CLASS_TEXT); code.setText(card.code);
        CheckBox sold = new CheckBox(this); sold.setText("مباع"); sold.setChecked(card.sold);
        layout.addView(amount); layout.addView(code); layout.addView(sold);

        new AlertDialog.Builder(this)
                .setTitle("تعديل كرت")
                .setView(layout)
                .setPositiveButton("حفظ", (d,w) -> {
                    int a = 0;
                    try { a = Integer.parseInt(amount.getText().toString().trim()); } catch(Exception ignored) {}
                    if (a <= 0 || code.getText().toString().trim().isEmpty()) { toast("بيانات غير صحيحة"); return; }
                    boolean ok = AppStore.updateCard(this, card, a, code.getText().toString().trim(), sold.isChecked());
                    if (!ok) { toast("هذا الكرت موجود مسبقًا في فئة أخرى أو في نفس الفئة"); return; }
                    showCategories();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showImport() {
        setTab("import");
        clear();
        content.addView(title("استيراد الكروت"));

        LinearLayout picker = cardBox();
        picker.addView(tv("اختر الفئة", 17, text, true));
        picker.addView(small("اختر الفئة التي تريد إضافة الكروت إليها. الفئة المختارة تظهر بإطار بنفسجي واضح."));
        ArrayList<CategoryItem> cats = AppStore.loadCategories(this);
        if (!cats.isEmpty()) {
            boolean foundSelected = false;
            for (CategoryItem cat : cats) if (cat.amount == selectedAmount) foundSelected = true;
            if (!foundSelected) selectedAmount = cats.get(0).amount;
        }
        LinearLayout row = null;
        int col = 0;
        for (CategoryItem cat : cats) {
            if (col == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                picker.addView(row);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(58), 1);
            lp.setMargins(dp(4), dp(6), dp(4), dp(6));
            row.addView(categoryChoiceButton(cat), lp);
            col++;
            if (col == 2) col = 0;
        }
        TextView chosen = tv("الفئة المختارة: " + selectedAmount + " ريال", 15, purpleLight, true);
        chosen.setGravity(Gravity.CENTER);
        chosen.setPadding(0, dp(8), 0, 0);
        picker.addView(chosen);
        content.addView(picker);

        LinearLayout ops = cardBox();
        ops.addView(tv("إضافة الكروت", 17, text, true));
        ops.addView(small("نسخة منظمة وآمنة: تم إلغاء إضافات الراوتر ونقاط البيع والتحديثات الداخلية مؤقتًا. إضافة الكروت متاحة فقط يدويًا أو من TXT أو Excel/CSV."));
        ops.addView(action("➕ إضافة يدوية", purple, Color.WHITE, v -> showManualAddDialog()));
        ops.addView(action("🔎 بحث عن كرت في النظام", card2, text, v -> showCardSearchDialog(-1)));
        ops.addView(action("📄 استيراد من ملف TXT", card2, text, v -> openTxtFile()));
        ops.addView(action("📊 استيراد من ملف Excel / CSV", card2, text, v -> openExcelFile()));
        ops.addView(small("TXT: كل سطر كرت واحد. Excel/CSV: يتم قراءة الخلايا النصية والرقمية ككروت، مع تجاهل الفراغات والمكرر."));
        content.addView(ops);
    }


    private void showDirectSale() {
        setTab("direct");
        clear();
        content.addView(title("بيع كرت مباشر"));

        LinearLayout info = cardBox();
        info.addView(tv("بيع مباشر منسق", 18, text, true));
        info.addView(small("اختر الفئة، ثم رقم الزبون، ثم طريقة الإرسال. خيار الآجل يظهر فقط إذا كان الزبون موجودًا في الدفتر ووضعه يسمح بالسلفة."));
        content.addView(info);

        LinearLayout picker = cardBox();
        picker.addView(tv("اختر الفئة", 18, text, true));
        ArrayList<CategoryItem> cats = AppStore.loadCategories(this);
        if (!cats.isEmpty()) {
            boolean foundSelected = false;
            for (CategoryItem cat : cats) if (cat.amount == selectedAmount) foundSelected = true;
            if (!foundSelected) selectedAmount = cats.get(0).amount;
        }
        LinearLayout row = null;
        int col = 0;
        for (CategoryItem cat : cats) {
            if (col == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                picker.addView(row);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(76), 1);
            lp.setMargins(dp(4), dp(6), dp(4), dp(6));
            row.addView(directSaleCategoryButton(cat), lp);
            col++;
            if (col == 3) col = 0;
        }
        int selectedAvailable = AppStore.availableCount(this, selectedAmount);
        TextView chosen = tv("الفئة المختارة: " + selectedAmount + " ريال | المتبقي: " + selectedAvailable, 15, selectedAvailable > 0 && selectedAvailable < 10 ? orange : purpleLight, true);
        chosen.setGravity(Gravity.CENTER);
        chosen.setPadding(0, dp(8), 0, 0);
        picker.addView(chosen);
        content.addView(picker);

        LinearLayout form = cardBox();
        form.addView(tv("بيانات الزبون", 18, text, true));

        LinearLayout phoneRow = new LinearLayout(this);
        phoneRow.setOrientation(LinearLayout.HORIZONTAL);
        phoneRow.setGravity(Gravity.CENTER_VERTICAL);

        Button contactBtn = action("بحث", card2, text, v -> openContactPicker(pendingContactPhone, pendingContactName));
        LinearLayout.LayoutParams contactLp = new LinearLayout.LayoutParams(dp(92), dp(58));
        contactLp.setMargins(0, 0, dp(8), dp(6));
        phoneRow.addView(contactBtn, contactLp);

        EditText phone = neonInput("رقم الزبون الذي سيصله الكرت", InputType.TYPE_CLASS_PHONE);
        phone.setText(directSalePhoneDraft);
        LinearLayout.LayoutParams phoneLp = new LinearLayout.LayoutParams(0, dp(58), 1);
        phoneLp.setMargins(0, 0, 0, dp(6));
        phoneRow.addView(phone, phoneLp);
        form.addView(phoneRow);

        EditText name = neonInput("اسم الزبون اختياري", InputType.TYPE_CLASS_TEXT);
        name.setText(directSaleNameDraft);
        form.addView(name);

        TextView customerHint = small("إذا كان الزبون موجودًا في الدفتر ومسموح له بالسلفة سيظهر خيار آجل تلقائيًا.");
        form.addView(customerHint);

        pendingContactPhone = phone;
        pendingContactName = name;
        phone.addTextChangedListener(simpleWatcher(value -> directSalePhoneDraft = value));
        name.addTextChangedListener(simpleWatcher(value -> directSaleNameDraft = value));

        TextView payTitle = tv("طريقة البيع", 18, text, true);
        payTitle.setPadding(0, dp(12), 0, dp(4));
        form.addView(payTitle);
        RadioGroup payGroup = new RadioGroup(this);
        payGroup.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton cash = payRadio("نقدي", true);
        RadioButton credit = payRadio("آجل", false);
        credit.setVisibility(View.GONE);
        payGroup.addView(cash, new RadioGroup.LayoutParams(0, dp(50), 1));
        payGroup.addView(credit, new RadioGroup.LayoutParams(0, dp(50), 1));
        form.addView(payGroup);

        TextView methodTitle = tv("اختر طريقة الإرسال", 18, text, true);
        methodTitle.setPadding(0, dp(12), 0, dp(4));
        form.addView(methodTitle);
        CheckBox sendSms = channelCheckBox("رسائل SMS", true, purpleLight);
        CheckBox sendWhats = channelCheckBox("واتساب", true, Color.rgb(37, 211, 102));
        form.addView(sendSms);
        form.addView(sendWhats);

        CheckBox saleRewards = channelCheckBox("احتساب المكافأة لهذه العملية", directSaleRewardsDraft && AppStore.isRewardsEnabled(this), goldSoft);
        saleRewards.setEnabled(AppStore.isRewardsEnabled(this));
        saleRewards.setOnCheckedChangeListener((buttonView, isChecked) -> directSaleRewardsDraft = isChecked);
        form.addView(saleRewards);
        if (!AppStore.isRewardsEnabled(this)) {
            form.addView(small("نظام المكافآت متوقف من إعدادات المكافآت العامة، لذلك لن تُحسب مكافأة لهذه العملية."));
        }

        Runnable updateCreditUi = () -> {
            LedgerCustomer lc = findDirectSaleLedgerCustomer(phone.getText().toString(), name.getText().toString());
            if (lc != null && lc.canAutoLoan()) {
                credit.setVisibility(View.VISIBLE);
                customerHint.setText("الزبون موثق: " + lc.name + " | المتاح للسلفة: " + lc.availableForRequest() + " ر.ي");
                customerHint.setTextColor(green);
            } else if (lc != null) {
                credit.setVisibility(View.GONE);
                cash.setChecked(true);
                customerHint.setText("الزبون موجود لكن الآجل غير متاح: " + AppStore.ledgerModeLabel(lc.effectiveMode()));
                customerHint.setTextColor(orange);
            } else {
                credit.setVisibility(View.GONE);
                cash.setChecked(true);
                customerHint.setText("إذا كان الزبون موجودًا في الدفتر ومسموح له بالسلفة سيظهر خيار آجل تلقائيًا.");
                customerHint.setTextColor(muted);
            }
        };
        phone.addTextChangedListener(simpleWatcher(value -> { directSalePhoneDraft = value; updateCreditUi.run(); }));
        name.addTextChangedListener(simpleWatcher(value -> { directSaleNameDraft = value; updateCreditUi.run(); }));
        updateCreditUi.run();

        Button primary = action("بيع وإرسال حسب الاختيار", purple, Color.WHITE, v ->
                performDirectSaleCombined(
                        phone.getText().toString(),
                        name.getText().toString(),
                        sendSms.isChecked(),
                        sendWhats.isChecked(),
                        saleRewards.isChecked(),
                        credit.getVisibility() == View.VISIBLE && credit.isChecked()
                ));
        LinearLayout.LayoutParams primaryLp = new LinearLayout.LayoutParams(-1, dp(58));
        primaryLp.setMargins(0, dp(10), 0, dp(6));
        form.addView(primary, primaryLp);

        content.addView(form);
    }

    private RadioButton payRadio(String label, boolean checked) {
        RadioButton r = new RadioButton(this);
        r.setText(label);
        r.setTextColor(text);
        r.setTextSize(15);
        r.setTypeface(appTypeface(true));
        r.setGravity(Gravity.CENTER);
        r.setChecked(checked);
        r.setPadding(dp(8), dp(6), dp(8), dp(6));
        r.setBackground(round(Color.rgb(12, 16, 31), dp(16), Color.rgb(61, 73, 112), dp(1)));
        if (Build.VERSION.SDK_INT >= 21) r.setButtonTintList(android.content.res.ColorStateList.valueOf(neonCyan));
        return r;
    }

    private LedgerCustomer findDirectSaleLedgerCustomer(String phone, String name) {
        LedgerCustomer byPhone = AppStore.findLedgerCustomerByPhone(this, phone);
        if (byPhone != null) return byPhone;
        return AppStore.findLedgerCustomerByName(this, name);
    }

    private interface TextChangeCallback { void onChange(String value); }

    private TextWatcher simpleWatcher(TextChangeCallback cb) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { cb.onChange(s == null ? "" : s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private CheckBox channelCheckBox(String label, boolean checked, int accentColor) {
        CheckBox cb = new CheckBox(this);
        cb.setText(label);
        cb.setTextColor(text);
        cb.setTextSize(15);
        cb.setTypeface(appTypeface(true));
        cb.setChecked(checked);
        cb.setPadding(dp(10), dp(7), dp(10), dp(7));
        cb.setBackground(round(Color.rgb(12, 16, 31), dp(16), Color.argb(150, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
        lp.setMargins(0, dp(4), 0, dp(4));
        cb.setLayoutParams(lp);
        if (Build.VERSION.SDK_INT >= 21) cb.setButtonTintList(android.content.res.ColorStateList.valueOf(accentColor));
        return cb;
    }

    private LinearLayout.LayoutParams quickLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(52), 1);
        lp.setMargins(dp(3), dp(5), dp(3), dp(3));
        return lp;
    }

    private Button directSaleCategoryButton(CategoryItem c) {
        boolean selected = c.amount == selectedAmount;
        int bgColor = selected ? Color.rgb(74, 55, 126) : card2;
        int borderColor = selected ? purpleLight : Color.argb(70, 255, 255, 255);
        Button b = new Button(this);
        b.setText(c.amount + " ريال\nمتاح: " + AppStore.availableCount(this, c.amount));
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(appTypeface(true));
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackground(round(bgColor, dp(16), borderColor, selected ? dp(2) : dp(1)));
        b.setOnClickListener(v -> { selectedAmount = c.amount; showDirectSale(); });
        return b;
    }

    private boolean isPendingAlertStatus(OperationLog log) {
        return AppStore.isPendingCriticalLog(log);
    }

    private void showLogs() {
        setTab("logs");
        clear();
        content.addView(title("السجلات والتقارير"));

        LinearLayout report = cardBox();
        report.addView(tv("تقرير PDF", 17, text, true));
        report.addView(small("يمكن تنزيل تقرير إجمالي مفصل مقسم إلى مربعات لكل فئة، أو تقرير مختصر، أو تقرير لفئة واحدة فقط."));
        report.addView(action("تقرير إجمالي مفصل حسب الفئات", purple, Color.WHITE, v -> startPdfDownload(-1, false)));
        report.addView(action("تقرير إجمالي مختصر", card2, text, v -> startPdfDownload(-1, true)));
        report.addView(action("تقرير PDF لفئة محددة", card2, text, v -> showPdfCategoryDialog()));
        report.addView(separator());
        report.addView(tv("تقارير المراجعة والمتابعة", 16, text, true));
        report.addView(small("تقارير PDF منظمة يمكن مشاركتها عبر واتساب: يومي، أسبوعي، شهري، أو فترة مخصصة."));
        LinearLayout reportQuick = new LinearLayout(this);
        reportQuick.setOrientation(LinearLayout.HORIZONTAL);
        reportQuick.addView(action("يومي PDF", gold, Color.rgb(35,24,8), v -> sharePeriodReportPdf("تقرير يومي", periodStartDaysAgo(0), todayDate())), new LinearLayout.LayoutParams(0, dp(50), 1));
        reportQuick.addView(action("أسبوعي PDF", card2, text, v -> sharePeriodReportPdf("تقرير أسبوعي", periodStartDaysAgo(6), todayDate())), new LinearLayout.LayoutParams(0, dp(50), 1));
        reportQuick.addView(action("شهري PDF", card2, text, v -> sharePeriodReportPdf("تقرير شهري", firstDayOfThisMonth(), todayDate())), new LinearLayout.LayoutParams(0, dp(50), 1));
        report.addView(reportQuick);
        report.addView(action("تقرير PDF حسب تاريخ مخصص", purple, Color.WHITE, v -> showCustomPeriodReportDialog()));
        report.addView(action("حذف كل إشعارات السداد", Color.rgb(82,30,42), Color.WHITE, v -> confirmClearLogs()));
        content.addView(report);

        int totalLogs = AppStore.logsCount(this);
        ArrayList<OperationLog> logs = AppStore.loadRecentLogs(this, logsVisibleLimit);
        if (logs.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(small("لا توجد عمليات حتى الآن."));
            content.addView(empty);
            return;
        }

        if (reviewFocusLogId != null && !reviewFocusLogId.trim().isEmpty()) {
            ArrayList<OperationLog> ordered = new ArrayList<>();
            for (OperationLog log : logs) if (reviewFocusLogId.equals(log.id)) ordered.add(log);
            for (OperationLog log : logs) if (!reviewFocusLogId.equals(log.id)) ordered.add(log);
            logs = ordered;
        }

        LinearLayout perf = cardBox();
        perf.addView(tv("محرك الأداء", 16, text, true));
        perf.addView(small("المعروض الآن: " + logs.size() + " من " + totalLogs + " عملية. يتم تحميل السجل بالدفعات بدل فتح كل العمليات مرة واحدة."));
        if (totalLogs > logs.size()) {
            perf.addView(action("تحميل " + AppStore.performanceLogPageSize() + " عملية أخرى", card2, text, v -> {
                logsVisibleLimit += AppStore.performanceLogPageSize();
                showLogs();
            }));
        }
        content.addView(perf);

        for (OperationLog log : logs) {
            boolean focused = reviewFocusLogId != null && reviewFocusLogId.equals(log.id);
            boolean pendingAlert = isPendingAlertStatus(log);
            LinearLayout box = focused || pendingAlert ? reviewAlertBox(focused) : cardBox();
            String statusText = log.status == null ? "" : log.status;
            int color = statusText.contains("تمت المراجعة") ? green : (pendingAlert ? Color.rgb(255, 64, 80) : (statusText.contains("تم") ? green : red));
            box.addView(badge(statusText.isEmpty() ? "غير محدد" : statusText, color));
            if (pendingAlert) box.addView(badge(focused ? "🚨 هذه العملية من الإشعار وتحتاج مراجعة" : "🚨 يحتاج مراجعة", Color.rgb(255, 64, 80)));
            box.addView(tv(log.amount + " ريال", 18, text, true));
            String phoneText = log.customerPhone == null || log.customerPhone.isEmpty() ? "-" : log.customerPhone;
            String nameText = log.customerName == null || log.customerName.isEmpty() ? "-" : log.customerName;
            box.addView(small("الدفع: " + log.provider + "\nالرقم: " + phoneText + "\nالاسم: " + nameText + "\nالكرت: " + displayCardCode(log.cardCode) + "\nالوقت: " + log.createdAt + "\nملاحظة: " + log.message));
            if (pendingAlert) {
                box.addView(action("✅ تمت المراجعة", green, Color.rgb(12, 30, 18), v -> confirmMarkReviewed(log)));
            }
            if (canAddTrustedNameFromLog(log)) {
                box.addView(action("➕ إضافة الاسم للمحافظ الموثوقة", gold, Color.rgb(35, 24, 8), v -> showApproveTrustedNameDialog(log)));
            }
            if (log.customerPhone != null && !log.customerPhone.isEmpty()) {
                LinearLayout tools = new LinearLayout(this);
                tools.setOrientation(LinearLayout.HORIZONTAL);
                tools.setGravity(Gravity.CENTER);
                Button resend = action("💬 إعادة SMS", card2, text, v -> resendLogSms(log));
                Button wa = action("🟢 واتساب", Color.rgb(37, 211, 102), Color.WHITE, v -> sendLogWhatsApp(log));
                LinearLayout.LayoutParams toolLp = new LinearLayout.LayoutParams(0, dp(50), 1);
                toolLp.setMargins(dp(3), dp(8), dp(3), dp(4));
                tools.addView(resend, toolLp);
                tools.addView(wa, toolLp);
                box.addView(tools);
            }
            box.addView(action("📄 تقرير العملية PDF / واتساب", purple, Color.WHITE, v -> shareSingleOperationPdf(log)));
            box.addView(action("حذف إشعار السداد", Color.rgb(82,30,42), Color.WHITE, v -> confirmDeleteLog(log)));
            content.addView(box);
        }
    }


    private void confirmMarkReviewed(OperationLog log) {
        if (log == null) return;
        new AlertDialog.Builder(this)
                .setTitle("تأكيد المراجعة")
                .setMessage("هل تمت مراجعة هذه العملية؟\n\nبعد التأكيد سيتوقف إشعارها ولن تظهر باللون الأحمر.")
                .setPositiveButton("نعم، تمت المراجعة", (d, w) -> {
                    AppStore.markLogReviewed(this, log.id);
                    NotifyHelper.cancelReviewNotification(this, log.id);
                    if (!AppStore.hasPendingCriticalLogs(this)) NotifyHelper.cancelPendingReviewAlert(this);
                    toast("تم إنهاء إشعار هذه العملية");
                    reviewFocusLogId = "";
                    showLogs();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private boolean canAddTrustedNameFromLog(OperationLog log) {
        if (log == null) return false;
        String st = log.status == null ? "" : log.status;
        String phone = log.customerPhone == null ? "" : log.customerPhone.trim();
        String name = log.customerName == null ? "" : log.customerName.trim();
        return (st.contains("معلق") || st.contains("إيداع صريح") || st.contains("يحتاج مراجعة")) && phone.isEmpty() && !name.isEmpty() && log.amount > 0;
    }

    private void showApproveTrustedNameDialog(OperationLog log) {
        if (log == null) return;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));

        String provider = log.provider == null || log.provider.trim().isEmpty() ? "محفظة" : log.provider.trim();
        String sender = log.sender == null ? "" : log.sender.trim();
        String nameValue = log.customerName == null ? "" : log.customerName.trim();

        TextView help = small("تم التعرف على إيداع صحيح، لكن الرسالة لا تحتوي رقم زبون صحيح 9 أرقام يبدأ بـ 7. راجع الاسم، اكتب رقم الزبون، ثم احفظه داخل نفس المحفظة التي وصل منها الإيداع فقط. الإشعار سيبقى قائمًا حتى تضغط زر تمت المراجعة.");
        EditText wallet = new EditText(this); wallet.setHint("اسم المحفظة"); wallet.setGravity(Gravity.RIGHT); wallet.setInputType(InputType.TYPE_CLASS_TEXT); wallet.setText(provider);
        EditText keywords = new EditText(this); keywords.setHint("كلمات التعرف"); keywords.setGravity(Gravity.RIGHT); keywords.setInputType(InputType.TYPE_CLASS_TEXT); keywords.setText(defaultKeywordsForProvider(provider, sender));
        EditText name = new EditText(this); name.setHint("الاسم كما ظهر في رسالة المحفظة"); name.setGravity(Gravity.RIGHT); name.setInputType(InputType.TYPE_CLASS_TEXT); name.setText(nameValue);
        EditText phone = new EditText(this); phone.setHint("رقم الزبون: 7xxxxxxxx"); phone.setGravity(Gravity.RIGHT); phone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(help);
        layout.addView(wallet);
        layout.addView(keywords);
        layout.addView(name);
        layout.addView(phone);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("اعتماد الاسم الموثوق")
                .setView(layout)
                .setPositiveButton("حفظ وإرسال كرت", null)
                .setNeutralButton("حفظ فقط", null)
                .setNegativeButton("إلغاء", null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> approveTrustedNameFromLog(dialog, log, wallet, keywords, name, phone, true));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> approveTrustedNameFromLog(dialog, log, wallet, keywords, name, phone, false));
        });
        dialog.show();
    }

    private String defaultKeywordsForProvider(String provider, String sender) {
        String p = provider == null ? "" : provider.trim();
        String s = sender == null ? "" : sender.trim();
        String lower = p.toLowerCase(Locale.US) + " " + s.toLowerCase(Locale.US);
        if (lower.contains("one") || p.contains("ون")) return "one cash, onecash, ون كاش";
        if (lower.contains("jaib") || p.contains("جيب")) return "jaib, جيب";
        if (lower.contains("jawali") || p.contains("جوالي")) return "jawali, جوالي";
        if (!s.isEmpty()) return p + ", " + s;
        return p;
    }

    private void approveTrustedNameFromLog(AlertDialog dialog, OperationLog log, EditText wallet, EditText keywords, EditText name, EditText phone, boolean sendNow) {
        String walletName = wallet.getText().toString().trim();
        String keywordText = keywords.getText().toString().trim();
        String fullName = name.getText().toString().trim();
        String cleanPhone = SmsProcessor.cleanPhone(phone.getText().toString());
        if (walletName.isEmpty() || fullName.isEmpty()) {
            toast("اسم المحفظة والاسم الظاهر مطلوبة");
            return;
        }
        if (!SmsProcessor.isValidLocalMobile(cleanPhone)) {
            toast("رقم الزبون يجب أن يكون 9 أرقام ويبدأ بـ 7");
            return;
        }

        AppStore.addWalletContact(this, walletName, keywordText, fullName, cleanPhone);
        if (!sendNow) {
            AppStore.updateLogDetails(this, log.id, fullName, "", "معلق: تمت إضافة الاسم بانتظار المراجعة", "تمت إضافة الاسم والرقم إلى نفس المحفظة فقط: " + walletName + " برقم: " + cleanPhone + ". لم يتم حجز كرت ولم يتم إرسال رسالة. اضغط تمت المراجعة بعد إكمال المعالجة لإيقاف الإشعار.", "");
            NotifyHelper.notifyExplicitDepositReview(this, log.id, log.amount, walletName, fullName, "تمت إضافة الاسم بانتظار المراجعة");
            toast("تم حفظ الاسم والرقم في نفس المحفظة، والإشعار باقٍ حتى المراجعة");
            dialog.dismiss();
            showLogs();
            return;
        }

        CardItem cardItem = AppStore.takeAvailableCard(this, log.amount, cleanPhone);
        if (cardItem == null) {
            AppStore.updateLogDetails(this, log.id, fullName, cleanPhone, "إيداع صريح غير منفذ: نفدت الكمية", "تمت إضافة الاسم إلى نفس المحفظة فقط، لكن لا توجد كروت متاحة لفئة " + log.amount + " ريال. لم يتم إرسال رسالة كرت.", "");
            NotifyHelper.notifyExplicitDepositReview(this, log.id, log.amount, walletName, fullName, "نفدت فئة " + log.amount);
            toast("تم حفظ الاسم، لكن الكمية نافدة");
            dialog.dismiss();
            showLogs();
            return;
        }

        String msg = AppStore.buildSuccessMessage(this, log.amount, cardItem.code);
        RewardResult reward = AppStore.applyRewardsForPaidSale(this, cleanPhone, fullName, log.amount, cardItem.code);
        String rewardNote = "";
        if (reward != null) {
            if (reward.customerMessagePart != null && !reward.customerMessagePart.isEmpty()) msg += reward.customerMessagePart;
            if (reward.internalNote != null && !reward.internalNote.isEmpty()) rewardNote = "\n" + reward.internalNote;
        }
        AppStore.updateLogDetails(this, log.id, fullName, cleanPhone, "جاري إرسال SMS", "تمت إضافة الاسم إلى المحافظ الموثوقة ثم حجز كرت وإرسال الرسالة بعد اعتمادك من السجل." + rewardNote, cardItem.code);
        SmsProcessor.sendSmsWithTracking(this, cleanPhone, msg, log.id, log.amount, cardItem.code, false,
                "تم إرسال الكرت بعد اعتماد الاسم من السجل",
                "فشل إرسال الكرت بعد اعتماد الاسم؛ استخدم إعادة SMS أو واتساب");
        AppStore.performAutoBackupIfDue(this, "اعتماد اسم محفظة من السجل");
        toast("تم حفظ الاسم وتجهيز إرسال الكرت");
        dialog.dismiss();
        new Handler(Looper.getMainLooper()).postDelayed(this::showLogs, 900);
    }

    private void resendLogSms(OperationLog log) {
        new AlertDialog.Builder(this)
                .setTitle("إعادة إرسال SMS")
                .setMessage("سيتم إعادة إرسال الرسالة إلى الرقم:\n" + log.customerPhone)
                .setPositiveButton("إرسال", (d,w) -> {
                    SmsProcessor.resendFromLog(this, log);
                    toast("تمت محاولة إعادة الإرسال؛ راجع حالة العملية بعد لحظات");
                    new Handler(Looper.getMainLooper()).postDelayed(this::showLogs, 900);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void sendLogWhatsApp(OperationLog log) {
        try {
            String phone = SmsProcessor.cleanPhone(log.customerPhone);
            if (phone.isEmpty()) { toast("لا يوجد رقم للزبون"); return; }
            if (!phone.startsWith("967")) phone = "967" + phone;
            boolean noStock = log.cardCode == null || log.cardCode.trim().isEmpty();
            boolean directSale = "manual".equalsIgnoreCase(log.sender) || (log.provider != null && log.provider.contains("بيع مباشر"));
            if (noStock && directSale) { toast("لا توجد رسالة للبيع المباشر لأن البيع لم يكتمل"); return; }
            String msg = noStock
                    ? AppStore.buildNoStockMessage(this, log.amount)
                    : (directSale ? AppStore.buildDirectSaleMessage(this, log.amount, log.cardCode) : AppStore.buildSuccessMessage(this, log.amount, log.cardCode));
            openWhatsAppBusinessMessage(phone, msg);
        } catch (Exception e) {
            toast("تعذر فتح واتساب");
        }
    }


    private void performDirectSaleCombined(String rawPhone, String rawName, boolean viaSms, boolean viaWhatsApp, boolean rewardsForThisSale) {
        performDirectSaleCombined(rawPhone, rawName, viaSms, viaWhatsApp, rewardsForThisSale, false);
    }

    private void performDirectSaleCombined(String rawPhone, String rawName, boolean viaSms, boolean viaWhatsApp, boolean rewardsForThisSale, boolean saleOnCredit) {
        String clean = SmsProcessor.cleanPhone(rawPhone);
        String name = rawName == null ? "" : rawName.trim();
        if (!SmsProcessor.isValidLocalMobile(clean)) { toast("اكتب رقم زبون صحيح: 9 أرقام ويبدأ بـ 7"); return; }
        if (!viaSms && !viaWhatsApp) { toast("اختر SMS أو واتساب على الأقل"); return; }

        String channels = selectedChannelsLabel(viaSms, viaWhatsApp);
        boolean effectiveRewards = rewardsForThisSale && AppStore.isRewardsEnabled(this);
        LedgerCustomer creditCustomer = saleOnCredit ? findDirectSaleLedgerCustomer(clean, name) : null;
        if (saleOnCredit) {
            if (creditCustomer == null || !creditCustomer.canAutoLoan()) {
                showNeonAlert("الآجل غير متاح", "هذا الزبون غير موثق أو السلفة غير مفعلة لحسابه.", orange, true);
                return;
            }
            if (!AppStore.canLedgerCustomerTakeLoan(creditCustomer, selectedAmount)) {
                showNeonAlert("تجاوز السقف", "لا يمكن البيع بالآجل لأن العملية ستتجاوز سقف العميل.\n\nالمتاح: " + creditCustomer.availableForRequest() + " ر.ي", red, true);
                return;
            }
        }

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("تأكيد البيع والإرسال")
                .setMessage("سيتم حجز كرت واحد فقط من فئة " + selectedAmount + " ريال للرقم:\n" + clean
                        + "\n\nطريقة البيع: " + (saleOnCredit ? "آجل" : "نقدي")
                        + "\nطرق الإرسال المختارة:\n" + channels
                        + "\n\nالمكافأة لهذه العملية: " + (effectiveRewards ? "مفعلة" : "معطلة")
                        + "\n\nتنبيه: واتساب يفتح الرسالة جاهزة، وقد تحتاج الضغط على زر الإرسال داخل واتساب.")
                .setPositiveButton("موافق، بيع وإرسال", (d,w) -> {
                    CardItem reserved = AppStore.takeAvailableCard(this, selectedAmount, clean);
                    if (reserved == null) {
                        String id = java.util.UUID.randomUUID().toString();
                        AppStore.addLog(this, new OperationLog(id, "بيع مباشر", "manual", name, clean, selectedAmount,
                                "فشل البيع: نفدت الكمية",
                                "بيع مباشر: لا توجد كروت متاحة من الفئة المختارة. لم يتم إرسال أي رسالة لأن البيع من داخل التطبيق وليس تحويلًا تلقائيًا.",
                                "", AppStore.now()));
                        AppStore.performAutoBackupIfDue(this, "محاولة بيع مباشر بدون كمية");
                        showNeonAlert("انتهت كمية هذه الفئة", "لا توجد كروت متاحة لفئة " + selectedAmount + " ريال.\n\nلم يتم إرسال أي رسالة للزبون. أضف كروتًا جديدة أو اختر فئة أخرى.", red, true);
                        new Handler(Looper.getMainLooper()).postDelayed(this::showLogs, 900);
                        return;
                    }
                    String code = reserved.code;
                    LedgerCustomer afterCredit = null;
                    if (saleOnCredit) {
                        afterCredit = AppStore.applyLedgerLoanWithCredit(this, clean, selectedAmount, code);
                        if (afterCredit == null) {
                            showNeonAlert("تعذر تسجيل الآجل", "تم منع العملية لأن حساب الزبون لا يسمح بالآجل أو السقف غير كافٍ.", red, true);
                            return;
                        }
                    }
                    String msg = AppStore.buildDirectSaleMessage(this, selectedAmount, code);
                    if (saleOnCredit && afterCredit != null) {
                        msg = msg + "\nعليك:" + afterCredit.debt + "ر.ي";
                    }
                    String id = java.util.UUID.randomUUID().toString();
                    String status = viaSms ? "جاري إرسال SMS" : "بانتظار إرسال خارجي";
                    String note = "بيع مباشر: تم حجز كرت واحد وتم تجهيز الإرسال عبر: " + channels + (saleOnCredit ? " | بيع آجل" : " | بيع نقدي");
                    if (effectiveRewards && !saleOnCredit) {
                        RewardResult reward = AppStore.applyRewardsForPaidSale(this, clean, name, selectedAmount, code);
                        if (reward != null) {
                            if (reward.customerMessagePart != null && !reward.customerMessagePart.isEmpty()) msg += reward.customerMessagePart;
                            if (reward.internalNote != null && !reward.internalNote.isEmpty()) note += "\n" + reward.internalNote;
                        }
                    } else if (saleOnCredit) {
                        note += "\nلم يتم احتساب مكافأة للآجل حتى يتم السداد.";
                    } else {
                        note += "\nالمكافأة معطلة لهذه العملية؛ لم يتم احتساب نقاط جديدة.";
                    }
                    AppStore.addLog(this, new OperationLog(id, "بيع مباشر", "manual", name, clean, selectedAmount, status, note, code, AppStore.now()));

                    if (viaSms) {
                        SmsProcessor.sendSmsWithTracking(this, clean, msg, id, selectedAmount, code, false,
                                "تم إرسال رسالة البيع المباشر عبر SMS",
                                "فشل إرسال SMS؛ استخدم واتساب من السجل");
                    }

                    if (viaWhatsApp) {
                        openWhatsAppMessage(clean, msg);
                    }

                    AppStore.performAutoBackupIfDue(this, "بيع مباشر متعدد القنوات");
                    int remainingAfterSale = AppStore.availableCount(this, selectedAmount);
                    if (!(remainingAfterSale > 0 && remainingAfterSale < 10)) {
                        toast("تم تجهيز البيع عبر: " + channels);
                    }
                    showLowStockWarningIfNeeded(selectedAmount, remainingAfterSale);
                    new Handler(Looper.getMainLooper()).postDelayed(this::showLogs, 1200);
                })
                .setNegativeButton("إلغاء", null)
                .create();
        styleNeonDialog(dlg, saleOnCredit ? gold : purpleLight);
        dlg.show();
    }

    private String selectedChannelsLabel(boolean sms, boolean whatsapp) {
        ArrayList<String> list = new ArrayList<>();
        if (sms) list.add("SMS");
        if (whatsapp) list.add("واتساب");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) out.append(" + ");
            out.append(list.get(i));
        }
        return out.toString();
    }

    private void openWhatsAppMessage(String phone, String message) {
        openWhatsAppBusinessMessage(phone, message);
    }

    private void openWhatsAppBusinessMessage(String phone, String message) {
        String clean = SmsProcessor.cleanPhone(phone);
        if (clean.isEmpty()) { toast("لا يوجد رقم واتساب صحيح"); return; }
        if (!clean.startsWith("967")) clean = "967" + clean;
        String encoded;
        try {
            encoded = URLEncoder.encode(message == null ? "" : message, "UTF-8");
        } catch (Exception e) {
            encoded = "";
        }
        String url = "https://wa.me/" + clean + "?text=" + encoded;

        Intent businessIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        businessIntent.setPackage("com.whatsapp.w4b"); // WhatsApp Business
        try {
            startActivity(businessIntent);
            return;
        } catch (Exception ignored) {
            // إذا لم يكن واتساب أعمال مثبتًا، نفتح الرابط بالطريقة العامة حتى لا يفشل الإرسال.
        }

        try {
            Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(fallback);
            toast("واتساب أعمال غير مثبت أو غير متاح؛ تم فتح واتساب المتاح في الجهاز");
        } catch (Exception e) {
            toast("تعذر فتح واتساب أعمال");
        }
    }

    private String displayCardCode(String code) {
        if (code == null || code.trim().isEmpty()) return "-";
        return code.trim();
    }

    private void manualAutoBackupNow() {
        try {
            String path = AppStore.writeAutoBackupNow(this, "نسخة يدوية من زر النسخ التلقائي");
            toast("تم حفظ نسخة تلقائية: " + path);
            showSettings();
        } catch (Exception e) {
            toast("فشل النسخ التلقائي: " + e.getMessage());
        }
    }

    private void showAutoBackupSettings() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        CheckBox enabled = new CheckBox(this);
        enabled.setText("تفعيل النسخ الاحتياطي التلقائي");
        enabled.setChecked(AppStore.isAutoBackupEnabled(this));
        layout.addView(enabled);
        final String[] labels = new String[]{"كل 6 ساعات", "كل 12 ساعة", "يوميًا / كل 24 ساعة", "كل 48 ساعة"};
        final int[] values = new int[]{6, 12, 24, 48};
        int current = AppStore.getAutoBackupIntervalHours(this);
        int checked = 1;
        for (int i = 0; i < values.length; i++) if (values[i] == current) checked = i;
        final int[] selected = new int[]{checked};
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        for (int i = 0; i < labels.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(labels[i]);
            rb.setId(1000 + i);
            group.addView(rb);
        }
        group.check(1000 + checked);
        group.setOnCheckedChangeListener((g, id) -> selected[0] = id - 1000);
        layout.addView(group);
        TextView note = small("يتم حفظ النسخ داخل مجلد التطبيق ONLINE_auto_backups عند فتح التطبيق أو تنفيذ عمليات مهمة بعد مرور الفترة المحددة.");
        layout.addView(note);
        new AlertDialog.Builder(this)
                .setTitle("إعدادات النسخ التلقائي")
                .setView(layout)
                .setPositiveButton("حفظ", (d,w) -> {
                    AppStore.setAutoBackupEnabled(this, enabled.isChecked());
                    int idx = selected[0];
                    if (idx < 0 || idx >= values.length) idx = 1;
                    AppStore.setAutoBackupIntervalHours(this, values[idx]);
                    toast("تم حفظ إعدادات النسخ التلقائي");
                    showSettings();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }


    private void showBackupRestoreDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(4), dp(4), dp(4), dp(4));

        layout.addView(tv("النسخ الاحتياطي", 16, text, true));
        layout.addView(small("اختر مكان حفظ النسخة. النسخ إلى الهاتف يحفظ داخل مجلد التطبيق، أما Drive فيفتح نافذة اختيار مكان الحفظ حتى تختار Google Drive أو أي مزود ملفات."));
        layout.addView(action("نسخ إلى الهاتف", purple, Color.WHITE, v -> createPhoneBackupNow()));
        layout.addView(action("نسخ إلى Drive", Color.rgb(168, 113, 24), Color.WHITE, v -> openBackupSaveToDrive()));
        layout.addView(separator());

        layout.addView(tv("استرجاع النسخ", 16, text, true));
        layout.addView(small("اختر مصدر النسخة. الاسترجاع يستبدل البيانات الحالية داخل التطبيق، لذلك احفظ نسخة حالية قبل المتابعة إذا كانت البيانات مهمة."));
        layout.addView(action("من الهاتف", purple, Color.WHITE, v -> showLocalBackupsForRestore()));
        layout.addView(action("من Drive", Color.rgb(168, 113, 24), Color.WHITE, v -> openBackupRestoreFromDrive()));
        layout.addView(separator());
        layout.addView(action("سجل الاسترجاع", card2, text, v -> showRestoreHistoryDialog()));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        new AlertDialog.Builder(this)
                .setTitle("النسخ والاسترجاع")
                .setView(scroll)
                .setPositiveButton("إغلاق", null)
                .show();
    }

    private void createPhoneBackupNow() {
        try {
            String path = AppStore.writeAutoBackupNow(this, "نسخة يدوية إلى الهاتف");
            toast("تم حفظ النسخة في الهاتف: " + path);
        } catch (Exception e) {
            toast("فشل النسخ إلى الهاتف: " + e.getMessage());
        }
    }

    private void openBackupSaveToDrive() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, AppStore.buildBackupFileName(this));
            startActivityForResult(Intent.createChooser(intent, "اختر Drive أو مكان حفظ النسخة"), REQ_BACKUP_SAVE);
        } catch (Exception e) {
            toast("تعذر فتح نافذة حفظ الملفات. ثبّت تطبيق Files أو Google Drive ثم حاول مرة أخرى");
        }
    }

    private void openBackupRestoreFromDrive() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(Intent.createChooser(intent, "اختر ملف النسخة من Drive أو الملفات"), REQ_BACKUP_RESTORE);
        } catch (Exception e) {
            toast("تعذر فتح نافذة اختيار الملفات. ثبّت تطبيق Files أو Google Drive ثم حاول مرة أخرى");
        }
    }

    private void showLocalBackupsForRestore() {
        try {
            File dir = AppStore.getAutoBackupDir(this);
            File[] arr = dir.listFiles();
            ArrayList<File> backups = new ArrayList<>();
            if (arr != null) {
                for (File f : arr) {
                    if (f != null && f.isFile() && f.getName().toLowerCase(Locale.US).endsWith(".json")) backups.add(f);
                }
            }
            if (backups.isEmpty()) {
                toast("لا توجد نسخ محفوظة في الهاتف");
                return;
            }
            java.util.Collections.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            String[] labels = new String[backups.size()];
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            for (int i = 0; i < backups.size(); i++) {
                File f = backups.get(i);
                long kb = Math.max(1L, f.length() / 1024L);
                labels[i] = f.getName() + "\n" + fmt.format(new Date(f.lastModified())) + " | " + kb + " KB";
            }
            new AlertDialog.Builder(this)
                    .setTitle("استرجاع من الهاتف")
                    .setItems(labels, (d, which) -> restoreBackupFromFile(backups.get(which)))
                    .setNegativeButton("إلغاء", null)
                    .show();
        } catch (Exception e) {
            toast("تعذر عرض نسخ الهاتف: " + e.getMessage());
        }
    }

    private void restoreBackupFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            reader.close();
            confirmAndRestoreBackup(sb.toString(), "الهاتف: " + file.getName());
        } catch (Exception e) {
            toast("فشل قراءة نسخة الهاتف: " + e.getMessage());
        }
    }

    private void confirmAndRestoreBackup(String backupText, String sourceLabel) {
        new AlertDialog.Builder(this)
                .setTitle("استعادة نسخة احتياطية")
                .setMessage("المصدر: " + (sourceLabel == null ? "ملف نسخة" : sourceLabel) + "\n\nهل تريد استعادة هذه النسخة الاحتياطية؟\n\nسيتم استبدال البيانات الحالية داخل التطبيق ببيانات ملف النسخة: الكروت، الفئات، السجلات، المحافظ، الرسائل، اسم الشبكة والإعدادات. يفضل حفظ نسخة من البيانات الحالية قبل المتابعة.")
                .setPositiveButton("موافق، استعادة", (d,w) -> {
                    try {
                        AppStore.restoreBackupJson(this, backupText);
                        try { AppStore.writeAutoBackupNow(this, "بعد الاستعادة"); } catch(Exception ignored) {}
                        toast("تمت استعادة النسخة الاحتياطية بنجاح وتسجيل وقت الاستعادة");
                        buildLayout();
                        showHome();
                    } catch (Exception e) {
                        toast("فشل الاستعادة: " + e.getMessage());
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showRestoreHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("سجل استعادة النسخ")
                .setMessage(AppStore.restoreHistoryText(this))
                .setPositiveButton("موافق", null)
                .show();
    }


    private void showPdfCategoryDialog() {
        ArrayList<CategoryItem> cats = AppStore.loadCategories(this);
        if (cats.isEmpty()) { toast("لا توجد فئات"); return; }
        String[] labels = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) labels[i] = cats.get(i).amount + " ريال";
        new AlertDialog.Builder(this)
                .setTitle("اختر الفئة للتقرير")
                .setItems(labels, (d, which) -> showPdfTypeDialog(cats.get(which).amount))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showPdfTypeDialog(int amount) {
        String[] items = new String[]{"تقرير مفصل لهذه الفئة", "تقرير مختصر لهذه الفئة"};
        new AlertDialog.Builder(this)
                .setTitle("نوع التقرير")
                .setItems(items, (d, which) -> startPdfDownload(amount, which == 1))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void startPdfDownload(int amount, boolean summaryOnly) {
        pendingReportAmount = amount;
        pendingReportSummaryOnly = summaryOnly;
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date());
        String mode = summaryOnly ? "summary" : "details";
        String name = amount < 0 ? "4U_NET_report_all_" + mode + "_" + stamp + ".pdf" : "4U_NET_report_" + amount + "_" + mode + "_" + stamp + ".pdf";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, name);
        startActivityForResult(intent, REQ_PDF);
    }

    private static final String REPORT_TABLE_HEADER = "الوقت والتاريخ | الفئة | الحالة | طريقة السداد | العدد | ملاحظة";

    private String reportTableRow(String dateTime, String category, String status, String paymentMethod, String count, String note) {
        return safe(dateTime) + " | "
                + safe(category) + " | "
                + safe(status) + " | "
                + safe(paymentMethod) + " | "
                + safe(count) + " | "
                + shortText(note == null ? "" : note, 220);
    }

    private String paymentMethodForReport(OperationLog log) {
        if (log == null) return "-";
        String provider = log.provider == null ? "" : log.provider.trim();
        String msg = log.message == null ? "" : log.message.trim();
        String sender = log.sender == null ? "" : log.sender.trim();
        String hay = (provider + " " + msg + " " + sender).toLowerCase(Locale.ROOT);
        if (hay.contains("one cash") || hay.contains("onecash") || hay.contains("ون كاش")) return "ون كاش";
        if (hay.contains("jawali") || hay.contains("جوالي")) return "جوالي";
        if (hay.contains("jaib") || hay.contains("جيب")) return "جيب";
        if (hay.contains("كريمي") || hay.contains("alkuraimi") || hay.contains("kuraimi")) return "كريمي";
        if (hay.contains("فلوسك") || hay.contains("floosk") || hay.contains("flousak")) return "فلوسك";
        if (hay.contains("عن طريق المحل") || hay.contains("المحل") || hay.contains("نقد") || provider.contains("بيع مباشر")) return "عن طريق المحل";
        if (hay.contains("سلفة") || hay.contains("تسليف") || hay.contains("دفتر")) return "كرت سلف";
        if (provider.contains("تعبئة رصيد")) return "تعبئة من الإدارة";
        if (provider.contains("طلب رصيد")) return "طلب رصيد";
        if (provider.contains("نقطة") || provider.contains("موثوق") || msg.contains("بصمة")) return "نقطة بيع";
        if (provider.toLowerCase(Locale.US).contains("sms") || msg.toLowerCase(Locale.US).contains("sms")) return "SMS";
        if ("admin".equalsIgnoreCase(sender) || provider.contains("إدارة") || provider.contains("ادارة")) return "إدارة";
        return provider.isEmpty() ? "-" : provider;
    }

    private void confirmDeleteLog(OperationLog log) {
        new AlertDialog.Builder(this)
                .setTitle("حذف إشعار السداد")
                .setMessage("سيتم حذف الإشعار من السجل فقط، ولن يتم إرجاع الكرت إلى المتاح.")
                .setPositiveButton("موافق، حذف", (d,w) -> { AppStore.deleteLog(this, log.id); showLogs(); })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmClearLogs() {
        new AlertDialog.Builder(this)
                .setTitle("حذف كل إشعارات السداد")
                .setMessage("سيتم حذف سجلات العمليات فقط. الكروت المباعة ستبقى مباعة حتى لا يحدث تكرار بيع.")
                .setPositiveButton("موافق، حذف", (d,w) -> { AppStore.clearLogs(this); showLogs(); })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void writePdfReport(Uri uri, int amountFilter) {
        writePdfReport(uri, amountFilter, pendingReportSummaryOnly);
    }

    private void writePdfReport(Uri uri, int amountFilter, boolean summaryOnly) {
        runPerformanceTask("جاري حفظ تقرير PDF في الخلفية...", () -> {
            String title = amountFilter < 0 ? "تقرير مبيعات كل الفئات" : "تقرير مبيعات فئة " + amountFilter + " ريال";
            String report = buildCardsSalesPdfReportText(title, amountFilter, summaryOnly);
            writeStyledPdfToUri(uri, title, report);
        });
    }

    private String buildCardsSalesPdfReportText(String title, int amountFilter, boolean summaryOnly) {
        ArrayList<OperationLog> all = AppStore.loadLogs(this);
        ArrayList<OperationLog> logs = new ArrayList<>();
        for (OperationLog log : all) if (amountFilter < 0 || log.amount == amountFilter) logs.add(log);

        int soldCards = 0, availableCards = 0, totalCards = 0, totalAmount = 0, sentOps = 0, failedOps = 0, reviewOps = 0;
        if (amountFilter < 0) {
            soldCards = AppStore.totalSold(this);
            availableCards = AppStore.totalAvailable(this);
            totalCards = AppStore.totalCardsCount(this);
        } else {
            soldCards = AppStore.soldCount(this, amountFilter);
            availableCards = AppStore.availableCount(this, amountFilter);
            totalCards = AppStore.cardsByAmountCount(this, amountFilter);
        }
        for (OperationLog log : logs) {
            String st = log.status == null ? "" : log.status;
            if ("تم الإرسال".equals(st) || st.contains("نجاح") || st.contains("تم")) {
                sentOps++;
                totalAmount += log.amount;
            } else if (st.contains("فشل") || st.contains("رفض")) {
                failedOps++;
            } else {
                reviewOps++;
            }
        }

        String range = amountFilter < 0 ? "إجمالي كل الفئات" : "فئة " + amountFilter + " ريال";
        StringBuilder out = new StringBuilder();
        out.append(title == null ? "تقرير مبيعات" : title).append("\n");
        out.append("==============================\n");
        out.append("النطاق: ").append(range).append("\n");
        out.append("نوع التقرير: ").append(summaryOnly ? "مختصر" : "مفصل").append("\n");
        out.append("تاريخ إنشاء التقرير: ").append(AppStore.now()).append("\n");
        out.append("طريقة العرض: PDF احترافي منظم بترتيب الأعمدة المطلوب\n");
        out.append("------------------------------\n");
        out.append("الملخص\n");
        out.append("إجمالي الكروت: ").append(totalCards).append("\n");
        out.append("الكروت المباعة: ").append(soldCards).append("\n");
        out.append("الكروت المتبقية: ").append(availableCards).append("\n");
        out.append("عمليات الإرسال الناجحة: ").append(sentOps).append("\n");
        out.append("عمليات تحتاج مراجعة: ").append(reviewOps).append("\n");
        out.append("عمليات فاشلة/مرفوضة: ").append(failedOps).append("\n");
        out.append("إجمالي مبالغ العمليات الناجحة: ").append(totalAmount).append(" ريال\n");
        out.append("عدد إشعارات السداد في التقرير: ").append(logs.size()).append("\n");
        out.append("==============================\n");
        out.append(amountFilter < 0 ? "ملخص الفئات\n" : "ملخص الفئة\n");
        out.append("------------------------------\n");

        ArrayList<CategoryItem> catsForReport = AppStore.loadCategories(this);
        for (CategoryItem cat : catsForReport) {
            if (amountFilter >= 0 && cat.amount != amountFilter) continue;
            int catTotal = AppStore.cardsByAmountCount(this, cat.amount);
            int catSold = AppStore.soldCount(this, cat.amount);
            int catAvailable = AppStore.availableCount(this, cat.amount);
            int catOps = 0;
            int catIncome = 0;
            for (OperationLog lg : logs) {
                if (lg.amount == cat.amount && "تم الإرسال".equals(lg.status)) {
                    catOps++;
                    catIncome += lg.amount;
                }
            }
            out.append("فئة: ").append(cat.amount).append(" ريال\n")
                    .append("إجمالي الكروت: ").append(catTotal).append("\n")
                    .append("المباعة: ").append(catSold).append("\n")
                    .append("المتبقية: ").append(catAvailable).append("\n")
                    .append("عمليات الإرسال الناجحة: ").append(catOps).append("\n")
                    .append("إجمالي مبلغ الفئة: ").append(catIncome).append(" ريال\n")
                    .append("------------------------------\n");
        }

        if (summaryOnly) {
            out.append("ملاحظة: اختر التقرير المفصل لعرض بيانات العمليات والكروت والرسائل.\n");
            out.append(AppStore.getNetworkName(this)).append("\n");
            return out.toString();
        }

        out.append("تفاصيل العمليات\n");
        out.append("==============================\n");
        out.append(REPORT_TABLE_HEADER).append("\n");
        out.append("------------------------------\n");
        if (logs.isEmpty()) out.append("لا توجد عمليات ضمن هذا النطاق.\n");
        for (OperationLog log : logs) {
            String note = "العميل: " + safe(log.customerName)
                    + "، الرقم: " + safe(log.customerPhone)
                    + "، الشراء: " + purchaseMethodForLog(log)
                    + "، الكرت: " + displayCardCode(log.cardCode)
                    + "، الرسالة: " + shortText(log.message, 180);
            out.append(reportTableRow(
                    safe(log.createdAt),
                    log.amount + " ريال",
                    safe(log.status),
                    paymentMethodForReport(log),
                    "1",
                    note
            )).append("\n");
        }
        out.append(AppStore.getNetworkName(this)).append("\n");
        return out.toString();
    }

    private void writeStyledPdfToUri(Uri uri, String title, String report) {
        PdfRenderState st = new PdfRenderState();
        st.pdf = new PdfDocument();
        st.pageNo = 1;
        try {
            startStyledPdfPage(st, title);
            drawHtmlStyleProfessionalReport(st, title, report == null ? "" : report);
            finishStyledPdfPage(st);
            OutputStream out = getContentResolver().openOutputStream(uri);
            st.pdf.writeTo(out);
            if (out != null) out.close();
            runOnUiThread(() -> toast("تم حفظ تقرير PDF احترافي بنجاح"));
        } catch (Exception e) {
            runOnUiThread(() -> toast("فشل حفظ التقرير: " + e.getMessage()));
            try { if (st.page != null) st.pdf.finishPage(st.page); } catch (Exception ignored) {}
        } finally {
            try { st.pdf.close(); } catch (Exception ignored) {}
        }
    }


    private String safe(String v) {
        return v == null || v.trim().isEmpty() ? "-" : v.trim();
    }



    private int getCurrentVersionCode() {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                return (int) getPackageManager().getPackageInfo(getPackageName(), 0).getLongVersionCode();
            }
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCurrentVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "-";
        }
    }

    private void showUpdateCenter() {
        setTab("settings");
        clear();
        content.addView(title("مركز التحديثات"));

        LinearLayout info = cardBox();
        info.addView(tv("تحديث التطبيق بدون حذف البيانات", 17, text, true));
        info.addView(small("الإصدار الحالي: " + getCurrentVersionName() + "\nVersion Code: " + getCurrentVersionCode()));
        info.addView(separator());
        info.addView(small("رابط ملف التحديث الحالي:\n" + AppStore.getUpdateManifestUrl(this)));
        info.addView(action("🔄 فحص التحديثات الآن", purple, Color.WHITE, v -> checkForUpdateNow()));
        info.addView(action("تعديل رابط ملف التحديث", card2, text, v -> showUpdateManifestUrlDialog()));
        info.addView(action("رجوع للإعدادات", card2, text, v -> showSettings()));
        content.addView(info);

        LinearLayout note = cardBox();
        note.addView(tv("طريقة العمل", 17, text, true));
        note.addView(small("أنت ترفع APK الجديد وتحدّث ملف JSON. المستخدم يفتح مركز التحديثات، يضغط فحص، ثم تحميل التحديث. إذا كان APK موقّعًا بنفس المفتاح وبنفس اسم الحزمة، سيتم التحديث فوق النسخة السابقة بدون حذف البيانات."));
        note.addView(small("مهم: أول انتقال من نسخة Debug قد يحتاج حذف مرة واحدة فقط. بعد تثبيت هذه النسخة الموقعة، التحديثات القادمة تثبت فوقها مباشرة."));
        content.addView(note);
    }

    private void showUpdateManifestUrlDialog() {
        EditText e = new EditText(this);
        e.setSingleLine(false);
        e.setMinLines(2);
        e.setText(AppStore.getUpdateManifestUrl(this));
        e.setTextColor(Color.BLACK);
        e.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        int pad = dp(18);
        e.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(this)
                .setTitle("رابط ملف التحديث JSON")
                .setMessage("ضع رابط ملف JSON الذي يحتوي على رقم الإصدار ورابط APK.")
                .setView(e)
                .setPositiveButton("حفظ", (d,w) -> { AppStore.setUpdateManifestUrl(this, e.getText().toString()); showUpdateCenter(); })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void checkForUpdateNow() {
        final String manifestUrl = AppStore.getUpdateManifestUrl(this);
        if (manifestUrl == null || manifestUrl.trim().isEmpty() || manifestUrl.contains("example.com")) {
            new AlertDialog.Builder(this)
                    .setTitle("رابط التحديث غير مضبوط")
                    .setMessage("ضع رابط ملف التحديث JSON أولًا من زر: تعديل رابط ملف التحديث.")
                    .setPositiveButton("حسنًا", null)
                    .show();
            return;
        }
        toast("جاري فحص التحديث...");
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(manifestUrl).openConnection();
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(12000);
                conn.setRequestMethod("GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                runOnUiThread(() -> handleUpdateManifest(sb.toString()));
            } catch (Exception ex) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("تعذر فحص التحديث")
                        .setMessage("تأكد من الإنترنت ورابط ملف التحديث.\n\n" + ex.getMessage())
                        .setPositiveButton("حسنًا", null)
                        .show());
            }
        }).start();
    }

    private void handleUpdateManifest(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            int latestCode = obj.optInt("versionCode", 0);
            String latestName = obj.optString("versionName", "-");
            String apkUrl = obj.optString("apkUrl", "");
            String notes = obj.optString("notes", "");
            String minSupported = obj.optString("minSupportedVersion", "");
            int currentCode = getCurrentVersionCode();
            if (latestCode <= currentCode) {
                new AlertDialog.Builder(this)
                        .setTitle("لا يوجد تحديث جديد")
                        .setMessage("أنت تستخدم آخر إصدار.\n\nالإصدار الحالي: " + getCurrentVersionName())
                        .setPositiveButton("حسنًا", null)
                        .show();
                return;
            }
            String msg = "يوجد تحديث جديد.\n\nالإصدار الحالي: " + getCurrentVersionName()
                    + "\nالإصدار الجديد: " + latestName
                    + (minSupported.isEmpty() ? "" : "\nأقل إصدار مدعوم: " + minSupported)
                    + (notes.isEmpty() ? "" : "\n\nما الجديد:\n" + notes)
                    + "\n\nقبل التحديث يفضّل حفظ نسخة احتياطية من الإعدادات.";
            AlertDialog.Builder b = new AlertDialog.Builder(this)
                    .setTitle("تحديث متاح")
                    .setMessage(msg)
                    .setNegativeButton("لاحقًا", null);
            if (apkUrl != null && !apkUrl.trim().isEmpty()) {
                b.setPositiveButton("تحميل التحديث", (d,w) -> openExternalUrl(apkUrl));
            } else {
                b.setPositiveButton("حسنًا", null);
            }
            b.show();
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setTitle("ملف التحديث غير صحيح")
                    .setMessage("تأكد أن ملف JSON مكتوب بالشكل الصحيح.\n\n" + e.getMessage())
                    .setPositiveButton("حسنًا", null)
                    .show();
        }
    }

    private void openExternalUrl(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            toast("تعذر فتح رابط التحديث");
        }
    }

    private void showSettings() {
        setTab("settings");
        clear();
        content.addView(title("الإعدادات"));

        LinearLayout license = cardBox();
        license.addView(tv("حالة التفعيل", 17, text, true));
        license.addView(small(AppStore.licenseSummary(this)));
        license.addView(separator());
        license.addView(small("كود الطلب: " + AppStore.getRequestCode(this) + "\nSerial: " + AppStore.getSerialNumber(this)));
        license.addView(action("عرض بيانات التفعيل", card2, text, v -> showActivationRequestText()));
        content.addView(license);

        LinearLayout appName = cardBox();
        appName.addView(tv("بيانات الشبكة", 17, text, true));
        appName.addView(small("اسم الشبكة: " + AppStore.getNetworkName(this)
                + "\nرقم الجوال: " + (AppStore.getNetworkPhone(this).trim().isEmpty() ? "غير محدد" : AppStore.getNetworkPhone(this))
                + "\nالشعار: " + (AppStore.getNetworkLogoPath(this).trim().isEmpty() ? "الشعار الافتراضي" : "شعار مخصص")
                + "\nتوقيع الرسائل: " + AppStore.getMessageSignature(this).replace("\n", " / ")));
        LinearLayout idActions = new LinearLayout(this);
        idActions.setOrientation(LinearLayout.HORIZONTAL);
        idActions.addView(action("تعديل البيانات", purple, Color.WHITE, v -> showNetworkIdentityDialog()), new LinearLayout.LayoutParams(0, dp(52), 1));
        idActions.addView(action("اختيار شعار", Color.rgb(31, 91, 122), Color.WHITE, v -> pickNetworkLogo()), new LinearLayout.LayoutParams(0, dp(52), 1));
        appName.addView(idActions);
        appName.addView(action("حذف الشعار", card2, text, v -> { AppStore.clearNetworkLogo(this); toast("تم حذف الشعار"); buildLayout(); showSettings(); }));
        content.addView(appName);

        LinearLayout backupRestore = cardBox();
        backupRestore.addView(tv("النسخ والاسترجاع", 17, text, true));
        backupRestore.addView(small("نافذة واحدة للنسخ الاحتياطي والاسترجاع: حفظ نسخة في الهاتف، حفظ نسخة إلى Drive عبر اختيار مكان الحفظ، واستعادة نسخة من الهاتف أو من Drive."));
        backupRestore.addView(action("فتح النسخ والاسترجاع", purple, Color.WHITE, v -> showBackupRestoreDialog()));
        content.addView(backupRestore);

        LinearLayout security = cardBox();
        security.addView(tv("الأمان والدخول", 17, text, true));
        security.addView(small("الحالة: " + (AppStore.isAppLockRequired(this) ? "مفعّل" : "غير مفعّل")
                + "\nطريقة الدخول: " + AppStore.appLockMethodLabel(AppStore.getAppLockMethod(this))
                + "\nPIN احتياطي: " + (AppStore.hasAppPin(this) ? "موجود" : "غير محدد")));
        security.addView(action("فتح إعدادات الأمان", purple, Color.WHITE, v -> showSecuritySettings()));
        content.addView(security);

        LinearLayout auto = cardBox();
        Switch sw = new Switch(this);
        sw.setText("تشغيل الإرسال التلقائي");
        sw.setTextColor(text);
        sw.setTextSize(17);
        sw.setTypeface(appTypeface(true));
        sw.setChecked(AppStore.isAutoSendEnabled(this));
        sw.setOnCheckedChangeListener((b, enabled) -> AppStore.setAutoSendEnabled(this, enabled));
        auto.addView(sw);
        auto.addView(small("الرسائل الموثوقة: Jawali / Jaib / ONE Cash + أي محفظة تضيفها من إدارة المحافظ والأسماء الموثوقة."));
        content.addView(auto);

        LinearLayout ledger = cardBox();
        ledger.addView(tv("الدفتر المحاسبي الذكي", 17, text, true));
        ledger.addView(small("نافذة عريضة لإدارة حسابات الزبائن، السلف، القيود اليومية، وكشوفات الحساب. يمكن للزبون الموثوق إرسال: سلفني 100، كما يقوم السداد الآلي بخصم الدين أولاً ثم صرف كرت بالباقي عند وجود مبلغ مطابق."));
        ledger.addView(separator());
        ledger.addView(small("الحالة: " + (AppStore.isSmartLedgerEnabled(this) ? "مفعّل" : "متوقف")
                + "\nإجمالي الديون: " + AppStore.ledgerTotalDebt(this) + " ريال"
                + "\nالزبائن الموثوقون: " + AppStore.ledgerTrustedCount(this)));
        ledger.addView(goldAnimatedAction("▤ فتح الدفتر المحاسبي الذكي", v -> showSmartLedger()), new LinearLayout.LayoutParams(-1, dp(58)));
        content.addView(ledger);

        LinearLayout messages = cardBox();
        messages.addView(tv("مركز رسائل الزبائن", 17, text, true));
        messages.addView(small("من هنا تعدّل رسالة الكرت ورسالة نفاد الفئة بشكل واضح، وتضيف العروض أو الملاحظات قبل الإرسال."));
        messages.addView(separator());
        messages.addView(small("معاينة رسالة الكرت الحالية:"));
        messages.addView(messagePreviewText(AppStore.buildSuccessMessage(this, 100, "1547013174")));
        messages.addView(action("فتح مركز تعديل الرسائل", purple, Color.WHITE, v -> showMessageTemplates()));
        content.addView(messages);

        LinearLayout rewards = cardBox();
        rewards.addView(tv("نظام النقاط والمكافآت", 17, text, true));
        rewards.addView(small("النسبة الافتراضية الآن 8% من قيمة كل كرت مرسل. يمكنك تشغيل/إيقاف النقاط لكل فئة وتعديل النسبة لكل فئة."));
        rewards.addView(separator());
        rewards.addView(small("الحالة: " + (AppStore.isRewardsEnabled(this) ? "مفعّل" : "متوقف")
                + "\nحد المكافأة: " + AppStore.getRewardThreshold(this) + " نقطة"
                + "\nفئة كرت المكافأة: " + AppStore.getRewardCardAmount(this) + " ريال"
                + "\nمخزون المكافآت المتاح: " + AppStore.rewardAvailableCount(this, AppStore.getRewardCardAmount(this))));
        rewards.addView(goldAnimatedAction("🏆 فتح إدارة النقاط والمكافآت", v -> showRewardsSettings()), new LinearLayout.LayoutParams(-1, dp(56)));
        content.addView(rewards);

        LinearLayout updates = cardBox();
        updates.addView(tv("مركز التحديثات", 17, text, true));
        updates.addView(small("افحص وجود إصدار جديد واعرض المميزات التي ترفعها أنت في ملف التحديث JSON."));
        updates.addView(action("🔄 بحث عن تحديث", purple, Color.WHITE, v -> showUpdateCenter()));
        content.addView(updates);

        LinearLayout trusted = cardBox();
        trusted.addView(tv("إدارة المحافظ والأسماء الموثوقة", 17, text, true));
        trusted.addView(small("أضف أي محفظة بنفس نظام ون كاش مثل floosk أو كاش: اسم المحفظة + كلمات التعرف + الاسم الظاهر في رسالة المحفظة + رقم إرسال الكرت."));
        trusted.addView(action("فتح إدارة المحافظ", purple, Color.WHITE, v -> showTrustedContacts()));
        content.addView(trusted);

        LinearLayout creditAgents = cardBox();
        creditAgents.addView(tv("الأرقام الموثوقة بالسقف", 17, text, true));
        creditAgents.addView(small("أضف رقم نقطة بيع موثوق. يرسل لك رسالة مثل: تم اضافة 100 من-733938509، فيرسل التطبيق الكرت إلى الرقم داخل الرسالة، مع سقف رصيد قابل للتعديل لكل رقم."));
        creditAgents.addView(action("فتح الأرقام الموثوقة", purple, Color.WHITE, v -> showTrustedCreditAgents()));
        content.addView(creditAgents);

        LinearLayout danger = cardBox();
        danger.addView(tv("حذف البيانات", 17, text, true));
        danger.addView(action("حذف كل البيانات", Color.rgb(82,30,42), Color.WHITE, v -> new AlertDialog.Builder(this)
                .setTitle("تأكيد")
                .setMessage("هل تريد حذف كل البيانات؟\n\nسيتم حذف البيانات التشغيلية داخل التطبيق. ملاحظة: التفعيل الأوفلاين يبقى محفوظًا على نفس الجهاز.")
                .setPositiveButton("موافق، حذف", (d,w) -> { AppStore.clearAll(this); AppStore.ensureDefaultCategories(this); buildLayout(); showHome(); })
                .setNegativeButton("إلغاء", null)
                .show()));
        content.addView(danger);
    }



    private void showSmartLedger() {
        setTab("ledger");
        clear();
        content.addView(title("الدفتر المحاسبي الذكي"));

        LinearLayout head = cardBox();
        head.addView(tv("نظام الدفتر الآلي", 18, text, true));
        head.addView(small("يقيد الإيداعات والسلف والسداد في حسابات الزبائن. التحديث الجديد: أي إيداع من عميل موجود في الدفتر يُرحّل كسداد/رصيد فقط، وصرف الكرت يتم فقط إذا أرسل العميل رقم الفئة مثل 100."));
        head.addView(separator());
        head.addView(small("الحالة: " + (AppStore.isSmartLedgerEnabled(this) ? "مفعّل" : "متوقف")
                + "\nالسلفة: " + (AppStore.isLedgerLoanEnabled(this) ? "مفعلة" : "متوقفة")
                + "\nالسداد الآلي: " + (AppStore.isLedgerAutoSettleEnabled(this) ? "مفعل" : "متوقف")
                + "\nإجمالي الديون: " + AppStore.ledgerTotalDebt(this) + " ريال"
                + "\nإجمالي أرصدة العملاء: " + AppStore.ledgerTotalCreditBalance(this) + " ريال"));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(action("إعدادات", purple, Color.WHITE, v -> showSmartLedgerSettings()), new LinearLayout.LayoutParams(0, dp(50), 1));
        actions.addView(action("زبون جديد", gold, Color.rgb(35,24,8), v -> showLedgerCustomerDialog(null)), new LinearLayout.LayoutParams(0, dp(50), 1));
        head.addView(actions);
        LinearLayout actions2 = new LinearLayout(this);
        actions2.setOrientation(LinearLayout.HORIZONTAL);
        actions2.addView(action("قيد يومي", card2, text, v -> showLedgerEntryDialog()), new LinearLayout.LayoutParams(0, dp(50), 1));
        actions2.addView(action("رجوع للضبط", card2, text, v -> showSettings()), new LinearLayout.LayoutParams(0, dp(50), 1));
        head.addView(actions2);
        content.addView(head);

        LinearLayout help = cardBox();
        help.addView(tv("طريقة الرسائل", 17, text, true));
        help.addView(small("طلب السلفة من العميل على وضع آلي كامل:\n100 أو 150 أو 500\n\nالإيداعات:\nأي إيداع من ون كاش/جوالي/جيب/كريمي/فلوسك/المحل يخصم من الدين، والزائد يتحفظ كرصيد للعميل. لا يتم صرف كرت بسبب الإيداع."));
        content.addView(help);

        LinearLayout customers = cardBox();
        customers.addView(tv("حسابات الزبائن", 17, text, true));
        ArrayList<LedgerCustomer> list = AppStore.loadLedgerCustomers(this);
        if (list.isEmpty()) {
            customers.addView(small("لا توجد حسابات زبائن بعد. أضف زبوناً موثوقاً لتفعيل سلفني والسداد الآلي."));
        } else {
            for (LedgerCustomer c : list) customers.addView(ledgerCustomerRow(c));
        }
        content.addView(customers);

        LinearLayout entries = cardBox();
        entries.addView(tv("آخر القيود", 17, text, true));
        ArrayList<LedgerEntry> es = AppStore.loadLedgerEntries(this);
        if (es.isEmpty()) entries.addView(small("لا توجد قيود محاسبية حتى الآن."));
        int max = Math.min(8, es.size());
        for (int i = 0; i < max; i++) entries.addView(ledgerEntryRow(es.get(i)));
        content.addView(entries);
    }

    private View ledgerCustomerRow(LedgerCustomer c) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackground(round(Color.rgb(17, 22, 40), dp(20), c.debt > 0 ? orange : (c.creditBalance > 0 ? gold : neonCyan), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(8), 0, 0);
        box.setLayoutParams(lp);
        box.addView(tv((c.name == null || c.name.isEmpty() ? c.phone : c.name) + "  —  " + c.phone, 15, text, true));
        box.addView(small("الدين الحالي: " + c.debt + " ريال | الرصيد: " + c.creditBalance + " ريال | حد السلفة: " + c.loanLimit
                + "\nالمتاح من السقف: " + c.remainingLoan() + " ريال | وضع التعامل: " + AppStore.ledgerModeLabel(c.effectiveMode())));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(action("كشف", purple, Color.WHITE, v -> showLedgerStatementDialog(c)), new LinearLayout.LayoutParams(0, dp(46), 1));
        row.addView(action("تعديل", card, text, v -> showLedgerCustomerDialog(c)), new LinearLayout.LayoutParams(0, dp(46), 1));
        row.addView(action("حذف", Color.rgb(82,30,42), Color.WHITE, v -> confirmDeleteLedgerCustomer(c)), new LinearLayout.LayoutParams(0, dp(46), 1));
        box.addView(row);
        return box;
    }

    private View ledgerEntryRow(LedgerEntry e) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(8), dp(10), dp(8));
        box.setBackground(round(Color.rgb(17, 22, 40), dp(18), Color.rgb(39, 50, 84), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(7), 0, 0);
        box.setLayoutParams(lp);
        box.addView(tv((e.status == null ? LedgerEntry.inferStatus(e.type) : e.status) + " — " + e.customerName, 14, text, true));
        box.addView(small(e.createdAt
                + "\nالفئة/المبلغ: " + AppStore.ledgerEntryAmount(e) + " | الطريقة: " + (e.paymentMethod == null ? LedgerEntry.inferPaymentMethod(e.type, e.reference) : e.paymentMethod)
                + "\n" + e.description
                + "\nمدين: " + e.debit + " | دائن: " + e.credit + " | المتبقي عليه: " + e.balanceAfter));
        return box;
    }

    private void showSmartLedgerSettings() {
        setTab("ledger");
        clear();
        content.addView(title("ضبط الدفتر المحاسبي الذكي"));
        LinearLayout box = cardBox();
        box.addView(tv("نافذة الضبط العريضة", 18, text, true));
        box.addView(small("هذه الإعدادات تضبط الدفتر فقط. الإيداعات من عملاء الدفتر تُحسب كسداد/رصيد، وطلبات الكروت تتم برسالة رقم الفئة من العميل."));
        Switch enabled = new Switch(this);
        enabled.setText("تشغيل الدفتر المحاسبي الذكي");
        enabled.setTextColor(text); enabled.setTextSize(16); enabled.setTypeface(appTypeface(true));
        enabled.setChecked(AppStore.isSmartLedgerEnabled(this));
        box.addView(enabled);
        Switch loans = new Switch(this);
        loans.setText("تشغيل ميزة سلفني للزبائن الموثوقين");
        loans.setTextColor(text); loans.setTextSize(16); loans.setTypeface(appTypeface(true));
        loans.setChecked(AppStore.isLedgerLoanEnabled(this));
        box.addView(loans);
        Switch settle = new Switch(this);
        settle.setText("تشغيل ترحيل الإيداعات كسداد/رصيد لعملاء الدفتر");
        settle.setTextColor(text); settle.setTextSize(16); settle.setTypeface(appTypeface(true));
        settle.setChecked(AppStore.isLedgerAutoSettleEnabled(this));
        box.addView(settle);
        box.addView(action("حفظ إعدادات الدفتر", gold, Color.rgb(35,24,8), v -> {
            AppStore.saveLedgerSettings(this, enabled.isChecked(), loans.isChecked(), settle.isChecked());
            toast("تم حفظ إعدادات الدفتر المحاسبي");
            showSmartLedger();
        }), new LinearLayout.LayoutParams(-1, dp(54)));
        box.addView(action("رجوع للدفتر", card2, text, v -> showSmartLedger()), new LinearLayout.LayoutParams(-1, dp(50)));
        content.addView(box);
    }

    private EditText ledgerEdit(String hint, String value, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value == null ? "" : value);
        e.setGravity(Gravity.RIGHT);
        e.setInputType(inputType);
        e.setTextColor(text);
        e.setHintTextColor(muted);
        e.setTextSize(16);
        e.setTypeface(appTypeface(false));
        e.setSingleLine(false);
        e.setPadding(dp(14), 0, dp(14), 0);
        e.setBackground(round(Color.rgb(10, 14, 30), dp(16), Color.rgb(48, 62, 102), dp(1)));
        return e;
    }

    private void showLedgerCustomerDialog(LedgerCustomer old) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(12), dp(16), dp(8));
        layout.setBackgroundColor(bg);
        EditText name = ledgerEdit("اسم الزبون", old == null ? "" : old.name, InputType.TYPE_CLASS_TEXT);
        EditText phone = ledgerEdit("رقم الزبون 7xxxxxxxx", old == null ? "" : old.phone, InputType.TYPE_CLASS_PHONE);
        EditText limit = ledgerEdit("سقف السلفة", old == null ? "500" : String.valueOf(old.loanLimit), InputType.TYPE_CLASS_NUMBER);
        EditText debt = ledgerEdit("المتبقي عليه / المديونية", old == null ? "0" : String.valueOf(old.debt), InputType.TYPE_CLASS_NUMBER);
        EditText credit = ledgerEdit("رصيد العميل", old == null ? "0" : String.valueOf(old.creditBalance), InputType.TYPE_CLASS_NUMBER);
        EditText notes = ledgerEdit("ملاحظات", old == null ? "" : old.notes, InputType.TYPE_CLASS_TEXT);

        TextView modeTitle = tv("طريقة التعامل مع العميل", 16, text, true);
        modeTitle.setPadding(0, dp(8), 0, dp(2));
        RadioGroup modes = new RadioGroup(this);
        modes.setOrientation(RadioGroup.VERTICAL);
        RadioButton autoFull = new RadioButton(this);
        autoFull.setId(1001);
        autoFull.setText("آلي كامل: يطلب كرت + يسدد + الزائد رصيد");
        autoFull.setTextColor(text);
        RadioButton settleOnly = new RadioButton(this);
        settleOnly.setId(1002);
        settleOnly.setText("إيقاف السلف / سداد فقط");
        settleOnly.setTextColor(text);
        RadioButton stopped = new RadioButton(this);
        stopped.setId(1003);
        stopped.setText("موقوف: لا صرف ولا سداد تلقائي");
        stopped.setTextColor(text);
        if (Build.VERSION.SDK_INT >= 21) {
            autoFull.setButtonTintList(android.content.res.ColorStateList.valueOf(neonCyan));
            settleOnly.setButtonTintList(android.content.res.ColorStateList.valueOf(neonCyan));
            stopped.setButtonTintList(android.content.res.ColorStateList.valueOf(neonCyan));
        }
        modes.addView(autoFull);
        modes.addView(settleOnly);
        modes.addView(stopped);
        String oldMode = old == null ? LedgerCustomer.MODE_AUTO_FULL : old.effectiveMode();
        if (LedgerCustomer.MODE_AUTO_FULL.equals(oldMode)) modes.check(1001);
        else if (LedgerCustomer.MODE_SETTLE_ONLY.equals(oldMode)) modes.check(1002);
        else modes.check(1003);

        layout.addView(name);
        layout.addView(phone);
        layout.addView(limit);
        layout.addView(debt);
        layout.addView(credit);
        layout.addView(notes);
        layout.addView(modeTitle);
        layout.addView(modes);
        new AlertDialog.Builder(this)
                .setTitle(old == null ? "إضافة زبون للدفتر" : "تعديل زبون")
                .setView(layout)
                .setPositiveButton("حفظ", (d,w) -> {
                    int lim = safeInt(limit.getText().toString());
                    int deb = safeInt(debt.getText().toString());
                    int cr = safeInt(credit.getText().toString());
                    String mode = LedgerCustomer.MODE_AUTO_FULL;
                    if (modes.getCheckedRadioButtonId() == 1002) mode = LedgerCustomer.MODE_SETTLE_ONLY;
                    else if (modes.getCheckedRadioButtonId() == 1003) mode = LedgerCustomer.MODE_STOPPED;
                    AppStore.addOrUpdateLedgerCustomer(this, old == null ? "" : old.id, name.getText().toString(), phone.getText().toString(), lim, deb, cr, mode, notes.getText().toString());
                    toast("تم حفظ حساب الزبون");
                    showSmartLedger();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private int safeInt(String value) {
        try { return Math.max(0, Integer.parseInt(value == null ? "0" : value.trim())); } catch(Exception e) { return 0; }
    }

    private void showLedgerEntryDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(12), dp(16), dp(8));
        layout.setBackgroundColor(bg);
        EditText phone = ledgerEdit("رقم الزبون", "", InputType.TYPE_CLASS_PHONE);
        EditText desc = ledgerEdit("الملاحظة / البيان", "قيد يدوي", InputType.TYPE_CLASS_TEXT);
        EditText debit = ledgerEdit("مدين: يزيد الدين على الزبون", "0", InputType.TYPE_CLASS_NUMBER);
        EditText credit = ledgerEdit("دائن: سداد/ينقص الدين", "0", InputType.TYPE_CLASS_NUMBER);
        Spinner method = new Spinner(this);
        String[] methods = new String[]{"قيد يدوي", "ون كاش", "جوالي", "جيب", "كريمي", "فلوسك", "عن طريق المحل", "إدارة"};
        method.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, methods));
        method.setBackground(round(Color.rgb(10, 14, 30), dp(16), Color.rgb(48, 62, 102), dp(1)));
        layout.addView(phone);
        layout.addView(desc);
        layout.addView(debit);
        layout.addView(credit);
        layout.addView(method);
        new AlertDialog.Builder(this)
                .setTitle("إضافة قيد يومي")
                .setView(layout)
                .setPositiveButton("حفظ القيد", (d,w) -> {
                    String ph = phone.getText().toString();
                    if (!AppStore.isValidLocalMobile(ph)) { toast("رقم الزبون غير صحيح"); return; }
                    int deb = safeInt(debit.getText().toString());
                    int cr = safeInt(credit.getText().toString());
                    String pm = method.getSelectedItem() == null ? "قيد يدوي" : method.getSelectedItem().toString();
                    LedgerCustomer c = AppStore.changeLedgerDebtByPhone(this, ph, deb, cr, deb > 0 ? "قيد مدين" : "سداد", desc.getText().toString(), pm, pm, deb > 0 ? "سلفة" : "سداد");
                    toast("تم حفظ القيد. المتبقي عليه: " + c.debt + " | رصيده: " + c.creditBalance);
                    showSmartLedger();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showLedgerStatementDialog(LedgerCustomer c) {
        String statement = AppStore.buildLedgerCustomerStatement(this, c);
        String pdfStatement = AppStore.buildLedgerCustomerPdfStatement(this, c);
        TextView view = messagePreviewText(statement);
        ScrollView scroll = new ScrollView(this);
        scroll.setPadding(dp(6), dp(6), dp(6), dp(6));
        scroll.addView(view);
        new AlertDialog.Builder(this)
                .setTitle("كشف حساب")
                .setView(scroll)
                .setPositiveButton("واتساب", (d,w) -> openWhatsAppBusinessMessage(c.phone, statement))
                .setNeutralButton("PDF", (d,w) -> {
                    File file = createTextPdfFile("تقرير إجراءات العميل", pdfStatement, "ledger_customer_statement");
                    if (file != null) sharePdfFile(file, "كشف حساب: " + (c.name == null ? c.phone : c.name));
                })
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private void confirmDeleteLedgerCustomer(LedgerCustomer c) {
        new AlertDialog.Builder(this)
                .setTitle("حذف حساب زبون")
                .setMessage("هل تريد حذف حساب: " + c.name + "؟\nلن تُحذف القيود القديمة من السجل المحاسبي.")
                .setPositiveButton("حذف", (d,w) -> { AppStore.deleteLedgerCustomer(this, c.id); showSmartLedger(); })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showSecuritySettings() {
        setTab("settings");
        clear();
        content.addView(title("الأمان والدخول"));

        LinearLayout current = cardBox();
        current.addView(tv("حماية فتح التطبيق", 18, text, true));
        current.addView(small("اختر طريقة الدخول التي تناسبك. الأفضل استخدام: بصمة + PIN احتياطي حتى لا يتم قفل التطبيق عليك إذا تعطلت البصمة."));
        current.addView(separator());
        current.addView(small("الحالة الحالية: " + (AppStore.isAppLockRequired(this) ? "الحماية مفعلة" : "بدون حماية")
                + "\nطريقة الدخول: " + AppStore.appLockMethodLabel(AppStore.getAppLockMethod(this))
                + "\nPIN احتياطي: " + (AppStore.hasAppPin(this) ? "موجود" : "غير محدد")
                + "\nدعم البصمة على هذا الجهاز: " + (isBiometricSupportedForPrompt() ? "متاح" : "غير متاح أو غير مفعّل")));
        content.addView(current);

        LinearLayout pinBox = cardBox();
        pinBox.addView(tv("رقم PIN الاحتياطي", 17, text, true));
        pinBox.addView(small("استخدم PIN من 4 إلى 8 أرقام. هذا الرقم ضروري كخطة بديلة عند فشل البصمة."));
        pinBox.addView(action(AppStore.hasAppPin(this) ? "تغيير PIN" : "تعيين PIN", goldDark, goldSoft, v -> showSetPinDialog(() -> showSecuritySettings())));
        content.addView(pinBox);

        LinearLayout methods = cardBox();
        methods.addView(tv("اختيار طريقة الدخول", 17, text, true));
        methods.addView(action("بدون حماية", card2, text, v -> confirmDisableAppLock()));
        methods.addView(action("PIN فقط", purple, Color.WHITE, v -> configureAppLockMethod(AppStore.LOCK_METHOD_PIN)));
        methods.addView(action("بصمة فقط", purple, Color.WHITE, v -> configureAppLockMethod(AppStore.LOCK_METHOD_FINGERPRINT)));
        methods.addView(action("بصمة + PIN احتياطي", Color.rgb(50, 90, 72), Color.WHITE, v -> configureAppLockMethod(AppStore.LOCK_METHOD_FINGERPRINT_PIN)));
        content.addView(methods);

        LinearLayout test = cardBox();
        test.addView(tv("اختبار الحماية", 17, text, true));
        test.addView(small("يمكنك اختبار البصمة أو PIN قبل إغلاق التطبيق."));
        test.addView(action("اختبار البصمة", card2, text, v -> startBiometricUnlock(() -> toast("اختبار البصمة ناجح"), AppStore.hasAppPin(this))));
        test.addView(action("اختبار PIN", card2, text, v -> showPinUnlockDialog(() -> toast("اختبار PIN ناجح"), true)));
        content.addView(test);

        LinearLayout back = cardBox();
        back.addView(action("رجوع للإعدادات", card2, text, v -> showSettings()));
        content.addView(back);
    }

    private void configureAppLockMethod(String method) {
        boolean needsPin = AppStore.LOCK_METHOD_PIN.equals(method) || AppStore.LOCK_METHOD_FINGERPRINT_PIN.equals(method);
        boolean needsFingerprint = AppStore.LOCK_METHOD_FINGERPRINT.equals(method) || AppStore.LOCK_METHOD_FINGERPRINT_PIN.equals(method);
        if (needsFingerprint && !isBiometricSupportedForPrompt()) {
            new AlertDialog.Builder(this)
                    .setTitle("البصمة غير متاحة")
                    .setMessage("هذا الجهاز لا يدعم البصمة أو لم يتم إعداد بصمة من إعدادات الهاتف. يمكنك استخدام PIN فقط، أو إعداد البصمة من إعدادات الجهاز ثم العودة للتطبيق.")
                    .setPositiveButton("استخدام PIN فقط", (d,w) -> configureAppLockMethod(AppStore.LOCK_METHOD_PIN))
                    .setNegativeButton("إلغاء", null)
                    .show();
            return;
        }
        if (needsPin && !AppStore.hasAppPin(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("تعيين PIN أولاً")
                    .setMessage("هذه الطريقة تحتاج رقم PIN احتياطي. قم بتعيينه الآن ثم سيتم تفعيل طريقة الدخول المطلوبة.")
                    .setPositiveButton("تعيين PIN", (d,w) -> showSetPinDialog(() -> {
                        AppStore.setAppLockMethod(this, method);
                        toast("تم تفعيل: " + AppStore.appLockMethodLabel(method));
                        showSecuritySettings();
                    }))
                    .setNegativeButton("إلغاء", null)
                    .show();
            return;
        }
        AppStore.setAppLockMethod(this, method);
        toast("تم تفعيل: " + AppStore.appLockMethodLabel(method));
        showSecuritySettings();
    }

    private void confirmDisableAppLock() {
        new AlertDialog.Builder(this)
                .setTitle("تعطيل حماية الدخول")
                .setMessage("هل تريد إلغاء حماية الدخول؟ سيتم فتح التطبيق بدون بصمة أو PIN.")
                .setPositiveButton("نعم، إلغاء الحماية", (d,w) -> {
                    AppStore.setAppLockMethod(this, AppStore.LOCK_METHOD_NONE);
                    toast("تم تعطيل حماية الدخول");
                    showSecuritySettings();
                })
                .setNegativeButton("لا", null)
                .show();
    }

    private void showSetPinDialog(Runnable afterSave) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(4), dp(8), dp(4));
        EditText pin = new EditText(this);
        pin.setHint("PIN من 4 إلى 8 أرقام");
        pin.setGravity(Gravity.CENTER);
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        EditText confirm = new EditText(this);
        confirm.setHint("تأكيد PIN");
        confirm.setGravity(Gravity.CENTER);
        confirm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(pin, new LinearLayout.LayoutParams(-1, dp(58)));
        layout.addView(confirm, new LinearLayout.LayoutParams(-1, dp(58)));
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("تعيين PIN")
                .setView(layout)
                .setPositiveButton("حفظ", null)
                .setNegativeButton("إلغاء", null)
                .create();
        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String p1 = pin.getText().toString().trim();
            String p2 = confirm.getText().toString().trim();
            if (!AppStore.isValidAppPin(p1)) { toast("PIN يجب أن يكون من 4 إلى 8 أرقام"); return; }
            if (!p1.equals(p2)) { toast("تأكيد PIN غير مطابق"); return; }
            AppStore.setAppPin(this, p1);
            dlg.dismiss();
            toast("تم حفظ PIN");
            if (afterSave != null) afterSave.run();
        }));
        dlg.show();
    }


    private void showPosOutlets() {
        setTab("settings");
        clear();
        content.addView(title("نقاط البيع"));

        LinearLayout intro = cardBox();
        intro.addView(tv("نقاط البيع والدفعات المحمية", 17, text, true));
        intro.addView(small("سجّل نقطة البيع مرة واحدة، ثم عند طلب دفعة مثل 500 كرت فئة 100 ريال اضغط تجهيز ملف كروت. سيتم خصم الكروت من مخزون الإدارة وإنشاء ملف مشفر جاهز للإرسال."));
        intro.addView(action("➕ إضافة نقطة بيع جديدة", purple, Color.WHITE, v -> showPosOutletDialog(null)));
        content.addView(intro);

        ArrayList<PosOutlet> outlets = AppStore.loadPosOutlets(this);
        if (outlets.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(small("لا توجد نقاط بيع مسجلة حتى الآن."));
            content.addView(empty);
        }
        for (PosOutlet pos : outlets) {
            LinearLayout box = cardBox();
            box.addView(tv(pos.name.isEmpty() ? "نقطة بيع بدون اسم" : pos.name, 18, text, true));
            box.addView(small("الرقم: " + (pos.phone.isEmpty() ? "-" : pos.phone)
                    + "\nكود الطلب: " + (pos.requestCode.isEmpty() ? "-" : pos.requestCode)
                    + "\nالحالة: " + (pos.active ? "مفعلة" : "موقوفة")
                    + "\nالمكافآت: " + (pos.rewardsEnabled ? "مفعلة" : "بدون مكافآت")
                    + (pos.notes.isEmpty() ? "" : "\nملاحظات: " + pos.notes)));
            box.addView(action("📦 إنشاء دفعة محمية وإرسالها", Color.rgb(50, 90, 72), Color.WHITE, v -> showCreatePosPackDialog(pos)));
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(action("تعديل", card2, text, v -> showPosOutletDialog(pos)), new LinearLayout.LayoutParams(0, dp(50), 1));
            row.addView(action("حذف", Color.rgb(82,30,42), Color.WHITE, v -> confirmDeletePosOutlet(pos)), new LinearLayout.LayoutParams(0, dp(50), 1));
            box.addView(row);
            content.addView(box);
        }

        LinearLayout back = cardBox();
        back.addView(action("رجوع للإعدادات", card2, text, v -> showSettings()));
        content.addView(back);
    }

    private void showPosOutletDialog(PosOutlet existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(4), dp(8), dp(4));

        EditText name = new EditText(this); name.setHint("اسم نقطة البيع"); name.setText(existing == null ? "" : existing.name); name.setGravity(Gravity.RIGHT);
        EditText phone = new EditText(this); phone.setHint("رقم نقطة البيع"); phone.setText(existing == null ? "" : existing.phone); phone.setGravity(Gravity.RIGHT); phone.setInputType(InputType.TYPE_CLASS_PHONE);
        EditText req = new EditText(this); req.setHint("كود الطلب من تطبيق ONLINE POS"); req.setText(existing == null ? "" : existing.requestCode); req.setGravity(Gravity.RIGHT);
        EditText notes = new EditText(this); notes.setHint("ملاحظات اختيارية"); notes.setText(existing == null ? "" : existing.notes); notes.setGravity(Gravity.RIGHT);
        CheckBox active = new CheckBox(this); active.setText("نقطة البيع مفعلة"); active.setChecked(existing == null || existing.active);
        CheckBox rewards = new CheckBox(this); rewards.setText("مع تفعيل المكافآت لهذه النقطة"); rewards.setChecked(existing == null || existing.rewardsEnabled);
        layout.addView(name); layout.addView(phone); layout.addView(req); layout.addView(notes); layout.addView(active); layout.addView(rewards);

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "إضافة نقطة بيع" : "تعديل نقطة بيع")
                .setView(layout)
                .setPositiveButton("حفظ", (d,w) -> {
                    if (name.getText().toString().trim().isEmpty()) { toast("اكتب اسم نقطة البيع"); return; }
                    if (req.getText().toString().trim().isEmpty()) { toast("اكتب كود الطلب الخاص بنقطة البيع"); return; }
                    AppStore.addOrUpdatePosOutlet(this, existing == null ? "" : existing.id, name.getText().toString(), phone.getText().toString(), req.getText().toString(), notes.getText().toString(), active.isChecked(), rewards.isChecked());
                    toast("تم حفظ نقطة البيع");
                    showPosOutlets();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmDeletePosOutlet(PosOutlet pos) {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد الحذف")
                .setMessage("هل تريد حذف نقطة البيع؟\n" + pos.name + "\n\nلن تُحذف الدفعات السابقة من السجل.")
                .setPositiveButton("موافق، حذف", (d,w) -> { AppStore.deletePosOutlet(this, pos.id); showPosOutlets(); })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showCreatePosPackDialog(PosOutlet pos) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(4), dp(8), dp(4));
        TextView hint = small("هذه دفعة محمية لنقطة البيع. سيتم أخذ العدد المطلوب من مخزون الإدارة ووضعه داخل ملف .onlpack مشفر وموقّع. بعد إنشاء الملف تُخصم الكروت من مخزون الإدارة حتى لا تُباع مرتين.");
        EditText amount = new EditText(this); amount.setHint("الفئة مثل 100"); amount.setInputType(InputType.TYPE_CLASS_NUMBER); amount.setGravity(Gravity.RIGHT); amount.setText(String.valueOf(selectedAmount));
        EditText qty = new EditText(this); qty.setHint("العدد مثل 500"); qty.setInputType(InputType.TYPE_CLASS_NUMBER); qty.setGravity(Gravity.RIGHT);
        layout.addView(hint); layout.addView(amount); layout.addView(qty);

        new AlertDialog.Builder(this)
                .setTitle("إنشاء دفعة محمية لـ " + pos.name)
                .setView(layout)
                .setPositiveButton("إنشاء وإرسال", (d,w) -> {
                    try {
                        int a = Integer.parseInt(amount.getText().toString().trim());
                        int q = Integer.parseInt(qty.getText().toString().trim());
                        File file = AppStore.createPosCardPackFile(this, pos, a, q);
                        showPosPackCreatedDialog(file, pos, a, q);
                        showPosOutlets();
                    } catch (Exception e) {
                        toast("فشل إنشاء الملف: " + e.getMessage());
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }


    private void showPosPackCreatedDialog(File file, PosOutlet pos, int amount, int quantity) {
        String filePath = file == null ? "" : file.getAbsolutePath();
        String fileName = file == null ? "" : file.getName();
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(10), dp(6), dp(10), dp(6));

        TextView info = small("تم إنشاء ملف الدفعة بنجاح.\n\n"
                + "نقطة البيع: " + (pos == null ? "" : pos.name) + "\n"
                + "الفئة: " + amount + " ريال\n"
                + "العدد: " + quantity + " كرت\n"
                + "اسم الملف: " + fileName + "\n\n"
                + "مكان الحفظ:\n" + filePath);
        layout.addView(info);

        TextView note = small("ملاحظة: هذا هو ملف .onlpack الذي ترسله لنقطة البيع. لا يتم استيراده إلا من تطبيق ONLINE POS.");
        note.setPadding(0, dp(8), 0, dp(8));
        layout.addView(note);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.addView(action("📎 مشاركة الملف", purple, Color.WHITE, v -> sharePosPackFile(file, pos)), new LinearLayout.LayoutParams(0, dp(52), 1));
        layout.addView(row);

        new AlertDialog.Builder(this)
                .setTitle("ملف نقطة البيع جاهز")
                .setView(layout)
                .setPositiveButton("مشاركة", (d, w) -> sharePosPackFile(file, pos))
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private void sharePosPackFile(File file, PosOutlet pos) {
        try {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception ignored) {}
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.putExtra(Intent.EXTRA_TEXT, "ملف كروت ONLINE POS لنقطة البيع: " + pos.name + "\nيتم استيراده من داخل تطبيق نقطة البيع فقط.");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "إرسال ملف نقطة البيع"));
        } catch (Exception e) {
            toast("تم إنشاء الملف لكن تعذر فتح المشاركة. المسار: " + file.getAbsolutePath());
        }
    }

    private void openContactPicker(EditText phone, EditText name) {
        try {
            pendingContactPhone = phone;
            pendingContactName = name;
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, REQ_PICK_CONTACT);
        } catch (Exception e) {
            toast("تعذر فتح جهات الاتصال");
        }
    }

    private void fillContactFromUri(Uri uri) {
        if (uri == null) return;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String number = numIdx >= 0 ? cursor.getString(numIdx) : "";
                String display = nameIdx >= 0 ? cursor.getString(nameIdx) : "";
                String cleanNumber = SmsProcessor.cleanPhone(number);
                if (pendingContactPhone != null) pendingContactPhone.setText(cleanNumber);
                if (pendingContactName != null && display != null) pendingContactName.setText(display);
                directSalePhoneDraft = cleanNumber;
                directSaleNameDraft = display == null ? "" : display;
                toast("تم اختيار جهة الاتصال");
            }
        } catch (Exception e) {
            toast("تعذر قراءة جهة الاتصال");
        } finally {
            try { if (cursor != null) cursor.close(); } catch(Exception ignored) {}
        }
    }

    private void showMessageTemplates() {
        setTab("settings");
        clear();
        content.addView(title("مركز تعديل رسائل الزبائن"));

        LinearLayout intro = cardBox();
        intro.addView(tv("طريقة التعديل", 17, text, true));
        intro.addView(small("اكتب الرسالة كما تريد أن تصل للزبون. يمكن إضافة عرض أو تنبيه أو ملاحظة في أي سطر. المتغيرات تتبدل تلقائيًا وقت الإرسال."));
        intro.addView(separator());
        intro.addView(badge("{amount} = مبلغ الفئة", purpleLight));
        intro.addView(badge("{card} = رقم الكرت", green));
        intro.addView(badge("{network} = اسم الشبكة", Color.rgb(41, 167, 255)));
        intro.addView(badge("{adminPhone} = رقم إدارة الشبكة", orange));
        content.addView(intro);

        LinearLayout info = cardBox();
        info.addView(tv("بيانات ثابتة داخل الرسائل", 17, text, true));
        info.addView(small("اسم الشبكة الحالي: " + AppStore.getNetworkName(this)));
        info.addView(small("رقم إدارة الشبكة الحالي: " + AppStore.getAdminPhone(this)));
        LinearLayout infoActions = new LinearLayout(this);
        infoActions.setOrientation(LinearLayout.HORIZONTAL);
        infoActions.addView(action("تعديل اسم الشبكة", purple, Color.WHITE, v -> showNetworkNameDialog()), new LinearLayout.LayoutParams(0, -2, 1));
        infoActions.addView(action("تعديل رقم الإدارة", card2, text, v -> showAdminPhoneDialog()), new LinearLayout.LayoutParams(0, -2, 1));
        info.addView(infoActions);
        content.addView(info);

        content.addView(templateBox(true));
        content.addView(directSaleTemplateBox());
        content.addView(posMessageTemplatesBox());
        content.addView(rewardMessageTemplatesBox());
        content.addView(templateBox(false));

        LinearLayout back = cardBox();
        back.addView(action("رجوع للإعدادات", card2, text, v -> showSettings()));
        content.addView(back);
    }

    private TextView messagePreviewText(String value) {
        TextView preview = tv(value, 14, text, false);
        preview.setGravity(Gravity.RIGHT);
        preview.setLineSpacing(4, 1.1f);
        preview.setPadding(dp(12), dp(10), dp(12), dp(10));
        preview.setBackground(round(card2, dp(12), Color.argb(35, 255,255,255), dp(1)));
        return preview;
    }

    private LinearLayout templateBox(boolean successMessage) {
        LinearLayout box = cardBox();
        String titleText = successMessage ? "رسالة إرسال الكرت التلقائية" : "رسالة نفاد الفئة التلقائية";
        String desc = successMessage
                ? "هذه الرسالة تُستخدم عند البيع التلقائي من إشعارات السداد."
                : "هذه الرسالة تُستخدم في المعالجة التلقائية فقط عندما يتم استلام تحويل ولا توجد كروت من نفس الفئة.";
        String sample = successMessage
                ? AppStore.buildSuccessMessage(this, 100, "1547013174")
                : AppStore.buildNoStockMessage(this, 100);

        box.addView(tv(titleText, 18, text, true));
        box.addView(small(desc));
        box.addView(separator());
        box.addView(small("معاينة على فئة 100 ريال:"));
        box.addView(messagePreviewText(sample));

        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.addView(action("تعديل النص", purple, Color.WHITE, v -> showMessageTemplateDialog(successMessage)), new LinearLayout.LayoutParams(0, -2, 1));
        actionsRow.addView(action("استعادة الافتراضي", Color.rgb(82,30,42), Color.WHITE, v -> confirmResetTemplate(successMessage)), new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(actionsRow);
        return box;
    }

    private LinearLayout directSaleTemplateBox() {
        LinearLayout box = cardBox();
        box.addView(tv("رسالة البيع المباشر", 18, text, true));
        box.addView(small("هذه الرسالة مستقلة عن رسالة البيع التلقائي. تُستخدم عندما تبيع أنت من داخل تطبيق الإدارة، ولا تُرسل أي رسالة إذا انتهت الكمية."));
        box.addView(separator());
        box.addView(small("معاينة على فئة 100 ريال:"));
        box.addView(messagePreviewText(AppStore.buildDirectSaleMessage(this, 100, "1547013174")));
        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.addView(action("تعديل النص", purple, Color.WHITE, v -> showDirectSaleMessageTemplateDialog()), new LinearLayout.LayoutParams(0, -2, 1));
        actionsRow.addView(action("استعادة الافتراضي", Color.rgb(82,30,42), Color.WHITE, v -> confirmResetDirectSaleTemplate()), new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(actionsRow);
        return box;
    }

    private LinearLayout posMessageTemplatesBox() {
        LinearLayout box = cardBox();
        box.addView(tv("رسائل نقاط البيع والأرقام الموثوقة", 18, text, true));
        box.addView(small("هذه الرسائل تصل إلى صاحب الرقم الموثوق عند نجاح الطلب أو رفضه بسبب التكرار أو السقف أو الرقم الخاطئ أو نقص الكروت."));
        box.addView(separator());
        box.addView(small("معاينة رسالة النجاح:"));
        box.addView(messagePreviewText(AppStore.applyPosTemplate(this, AppStore.getPosSuccessTemplate(this), demoTrustedAgent(), demoTrustedRequest(), 700, "")));
        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.addView(action("تعديل رسائل نقاط البيع", purple, Color.WHITE, v -> showPosMessageTemplatesDialog()), new LinearLayout.LayoutParams(0, -2, 1));
        actionsRow.addView(action("استعادة الافتراضي", Color.rgb(82,30,42), Color.WHITE, v -> {
            AppStore.resetPosMessageTemplates(this);
            toast("تمت استعادة رسائل نقاط البيع");
            showMessageTemplates();
        }), new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(actionsRow);
        return box;
    }

    private TrustedCreditAgent demoTrustedAgent() {
        return new TrustedCreditAgent("demo", "نقطة بيع تجريبية", "777000111", "تجريبي", 1000, 300, true, true, true, System.currentTimeMillis());
    }

    private ParsedCreditRequest demoTrustedRequest() {
        return new ParsedCreditRequest(new int[]{100, 200}, "735794758", "", "demo");
    }

    private EditText templateEdit(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setMinLines(3);
        input.setMaxLines(7);
        input.setGravity(Gravity.TOP | Gravity.RIGHT);
        input.setSingleLine(false);
        input.setVerticalScrollBarEnabled(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(value);
        return input;
    }

    private ScrollView scrollableDialogView(LinearLayout layout) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setPadding(0, 0, 0, dp(8));
        layout.setPadding(dp(8), dp(8), dp(8), dp(96));
        scroll.addView(layout, new ScrollView.LayoutParams(-1, -2));
        return scroll;
    }

    private void makeDialogKeyboardFriendly(AlertDialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void showPosMessageTemplatesDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView help = small("المتغيرات: {trusted_name} {trusted_phone} {customer_phone} {categories} {cards_count} {total_amount} {remaining_limit} {credit_limit} {used_amount} {reason} {window_minutes} {network} {adminPhone}");
        EditText success = templateEdit("رسالة نجاح الطلب", AppStore.getPosSuccessTemplate(this));
        EditText duplicate = templateEdit("رسالة الطلب المكرر", AppStore.getPosDuplicateTemplate(this));
        EditText invalidPhone = templateEdit("رسالة رقم العميل الخاطئ", AppStore.getPosInvalidPhoneTemplate(this));
        EditText limit = templateEdit("رسالة تجاوز السقف", AppStore.getPosLimitTemplate(this));
        EditText noStock = templateEdit("رسالة نقص الكروت", AppStore.getPosNoStockTemplate(this));
        EditText invalidCategory = templateEdit("رسالة فئة غير معتمدة", AppStore.getPosInvalidCategoryTemplate(this));
        EditText ambiguous = templateEdit("رسالة الطلب الملتبس", AppStore.getPosAmbiguousTemplate(this));
        TextView preview = messagePreviewText(AppStore.applyPosTemplate(this, success.getText().toString(), demoTrustedAgent(), demoTrustedRequest(), 700, "مثال سبب الرفض"));
        Button previewBtn = action("تحديث معاينة النجاح", card2, text, v -> preview.setText(AppStore.applyPosTemplate(this, success.getText().toString(), demoTrustedAgent(), demoTrustedRequest(), 700, "مثال سبب الرفض")));
        layout.addView(help);
        layout.addView(small("1) نجاح الطلب")); layout.addView(success);
        layout.addView(small("2) تكرار الطلب خلال 5 دقائق")); layout.addView(duplicate);
        layout.addView(small("3) رقم العميل غير صحيح")); layout.addView(invalidPhone);
        layout.addView(small("4) تجاوز سقف نقطة البيع")); layout.addView(limit);
        layout.addView(small("5) نقص الكروت")); layout.addView(noStock);
        layout.addView(small("6) فئة غير معتمدة")); layout.addView(invalidCategory);
        layout.addView(small("7) طلب ملتبس يحتاج مراجعة")); layout.addView(ambiguous);
        layout.addView(previewBtn);
        layout.addView(small("معاينة رسالة النجاح:"));
        layout.addView(preview);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("قوالب رسائل نقاط البيع")
                .setView(scrollableDialogView(layout))
                .setPositiveButton("حفظ", (d,w) -> {
                    AppStore.setPosMessageTemplates(this,
                            success.getText().toString(), duplicate.getText().toString(), invalidPhone.getText().toString(),
                            limit.getText().toString(), noStock.getText().toString(), invalidCategory.getText().toString(), ambiguous.getText().toString());
                    toast("تم حفظ رسائل نقاط البيع");
                    showMessageTemplates();
                })
                .setNegativeButton("إلغاء", null)
                .show();
        makeDialogKeyboardFriendly(dialog);
    }

    private LinearLayout rewardMessageTemplatesBox() {
        LinearLayout box = cardBox();
        box.addView(tv("رسائل النقاط والمكافآت", 18, text, true));
        box.addView(small("نصوص قصيرة لا تزيد عن 60 حرفًا قبل الاستبدال، حتى تبقى رسالة الكرت مختصرة."));
        box.addView(separator());
        String preview = AppStore.buildRewardTemplate(this, AppStore.getRewardPointsTemplate(this), 8, 40, 60, 100, 100, "");
        preview += "\n" + AppStore.buildRewardTemplate(this, AppStore.getRewardSentTemplate(this), 8, 0, 0, 100, 100, "987654321");
        box.addView(messagePreviewText(preview));
        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.addView(action("تعديل نصوص النقاط", purple, Color.WHITE, v -> showRewardMessageTemplatesDialog()), new LinearLayout.LayoutParams(0, -2, 1));
        actionsRow.addView(action("استعادة الافتراضي", Color.rgb(82,30,42), Color.WHITE, v -> {
            AppStore.setRewardMessageTemplates(this, AppStore.DEFAULT_REWARD_POINTS_TEMPLATE, AppStore.DEFAULT_REWARD_SENT_TEMPLATE, AppStore.DEFAULT_REWARD_PENDING_TEMPLATE);
            toast("تمت استعادة نصوص النقاط");
            showMessageTemplates();
        }), new LinearLayout.LayoutParams(0, -2, 1));
        box.addView(actionsRow);
        return box;
    }

    private void showRewardMessageTemplatesDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView help = small("المتغيرات: {points} {balance} {remain} {threshold} {rewardAmount} {rewardCard}. كل نص حدّه 60 حرفًا.");
        EditText points = new EditText(this);
        points.setHint("رسالة النقاط");
        points.setSingleLine(true);
        points.setGravity(Gravity.RIGHT);
        points.setText(AppStore.getRewardPointsTemplate(this));
        EditText sent = new EditText(this);
        sent.setHint("رسالة المكافأة المرسلة");
        sent.setSingleLine(true);
        sent.setGravity(Gravity.RIGHT);
        sent.setText(AppStore.getRewardSentTemplate(this));
        EditText pending = new EditText(this);
        pending.setHint("رسالة المكافأة المعلقة");
        pending.setSingleLine(true);
        pending.setGravity(Gravity.RIGHT);
        pending.setText(AppStore.getRewardPendingTemplate(this));
        layout.addView(help);
        layout.addView(points);
        layout.addView(sent);
        layout.addView(pending);
        layout.addView(small("مثال مختصر: نقاط:+8 رصيد:40 باقي:60"));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("تعديل رسائل النقاط")
                .setView(scrollableDialogView(layout))
                .setPositiveButton("حفظ", (d,w) -> {
                    String a = points.getText().toString().trim();
                    String b = sent.getText().toString().trim();
                    String c = pending.getText().toString().trim();
                    if (a.length() > 60 || b.length() > 60 || c.length() > 60) {
                        toast("كل نص يجب ألا يزيد عن 60 حرفًا");
                        return;
                    }
                    AppStore.setRewardMessageTemplates(this, a, b, c);
                    toast("تم حفظ نصوص النقاط");
                    showMessageTemplates();
                })
                .setNegativeButton("إلغاء", null)
                .show();
        makeDialogKeyboardFriendly(dialog);
    }

    private void confirmResetDirectSaleTemplate() {
        new AlertDialog.Builder(this)
                .setTitle("استعادة نص البيع المباشر")
                .setMessage("سيتم استبدال نص البيع المباشر بالنص الافتراضي.")
                .setPositiveButton("استعادة", (d,w) -> {
                    AppStore.setDirectSaleTemplate(this, AppStore.DEFAULT_DIRECT_SALE_TEMPLATE);
                    toast("تمت استعادة نص البيع المباشر");
                    showMessageTemplates();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showDirectSaleMessageTemplateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView help = small("عدّل رسالة البيع المباشر فقط. هذه لا تؤثر على رسائل التحويل التلقائي.");
        TextView vars = small("المتغيرات المتاحة: {amount}  {card}  {network}  {adminPhone}");
        EditText input = new EditText(this);
        input.setMinLines(4);
        input.setMaxLines(8);
        input.setGravity(Gravity.TOP | Gravity.RIGHT);
        input.setSingleLine(false);
        input.setVerticalScrollBarEnabled(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(AppStore.getDirectSaleTemplate(this));

        TextView preview = messagePreviewText(AppStore.applyTemplate(this, input.getText().toString(), 100, "1547013174"));
        Button previewBtn = action("تحديث المعاينة", card2, text, v ->
                preview.setText(AppStore.applyTemplate(this, input.getText().toString(), 100, "1547013174")));
        Button offerBtn = action("إضافة سطر عرض", Color.rgb(50, 70, 55), Color.WHITE, v -> {
            input.append("\n\nعرض خاص: ");
            preview.setText(AppStore.applyTemplate(this, input.getText().toString(), 100, "1547013174"));
        });

        layout.addView(help);
        layout.addView(vars);
        layout.addView(input);
        layout.addView(previewBtn);
        layout.addView(offerBtn);
        layout.addView(small("المعاينة:"));
        layout.addView(preview);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("تعديل رسالة البيع المباشر")
                .setView(scrollableDialogView(layout))
                .setPositiveButton("حفظ", (d,w) -> {
                    AppStore.setDirectSaleTemplate(this, input.getText().toString());
                    toast("تم حفظ نص البيع المباشر");
                    showMessageTemplates();
                })
                .setNegativeButton("إلغاء", null)
                .show();
        makeDialogKeyboardFriendly(dialog);
    }

    private void confirmResetTemplate(boolean successMessage) {
        new AlertDialog.Builder(this)
                .setTitle("استعادة النص الافتراضي")
                .setMessage("سيتم استبدال النص الحالي بالنص الافتراضي.")
                .setPositiveButton("استعادة", (d,w) -> {
                    if (successMessage) AppStore.setSuccessTemplate(this, AppStore.DEFAULT_SUCCESS_TEMPLATE);
                    else AppStore.setNoStockTemplate(this, AppStore.DEFAULT_NO_STOCK_TEMPLATE);
                    toast("تمت استعادة النص الافتراضي");
                    showMessageTemplates();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showNetworkIdentityDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(4), dp(4), dp(4), dp(4));

        EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setGravity(Gravity.RIGHT);
        name.setHint("اسم الشبكة");
        name.setText(AppStore.getNetworkName(this));
        layout.addView(name);

        EditText phone = new EditText(this);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        phone.setGravity(Gravity.RIGHT);
        phone.setHint("رقم جوال الشبكة");
        phone.setText(AppStore.getNetworkPhone(this));
        layout.addView(phone);

        CheckBox showPhone = new CheckBox(this);
        showPhone.setText("إظهار رقم الجوال في توقيع الرسائل");
        showPhone.setChecked(AppStore.isShowPhoneInSignature(this));
        layout.addView(showPhone);

        layout.addView(small("سيظهر اسم الشبكة ورقم الجوال في الواجهة، الرسائل، واتساب، ورأس تقارير PDF. ويمكن اختيار الشعار من زر اختيار شعار في صفحة الإعدادات."));

        new AlertDialog.Builder(this)
                .setTitle("بيانات الشبكة")
                .setView(layout)
                .setPositiveButton("حفظ", (d,w) -> {
                    AppStore.setNetworkName(this, name.getText().toString());
                    AppStore.setNetworkPhone(this, phone.getText().toString());
                    AppStore.setShowPhoneInSignature(this, showPhone.isChecked());
                    toast("تم حفظ بيانات الشبكة");
                    buildLayout();
                    showSettings();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void pickNetworkLogo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "اختر شعار الشبكة"), REQ_NETWORK_LOGO);
    }

    private void saveNetworkLogoFromUri(Uri uri) {
        if (uri == null) { toast("لم يتم اختيار شعار"); return; }
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            if (in == null) { toast("تعذر قراءة الشعار"); return; }
            File outFile = new File(getFilesDir(), "network_logo.png");
            FileOutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.flush();
            out.close();
            in.close();
            AppStore.setNetworkLogoPath(this, outFile.getAbsolutePath());
            toast("تم حفظ شعار الشبكة");
            buildLayout();
            showSettings();
        } catch (Exception e) {
            toast("فشل حفظ الشعار: " + e.getMessage());
        }
    }

    private void showNetworkNameDialog() {
        showNetworkIdentityDialog();
    }


    private void showApiUrlDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setGravity(Gravity.LEFT);
        input.setText(AppStore.getLicenseApiUrl(this));
        new AlertDialog.Builder(this)
                .setTitle("رابط سيرفر التفعيل (اختياري وغير مستخدم في هذه النسخة)")
                .setMessage("ضع رابط ملف api.php بعد رفع موقع الإدارة. مثال: https://domain.com/online_license/api.php")
                .setView(input)
                .setPositiveButton("حفظ", (d,w) -> {
                    AppStore.setLicenseApiUrl(this, input.getText().toString());
                    toast("تم حفظ رابط السيرفر");
                    showSettings();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showAdminPhoneDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setGravity(Gravity.RIGHT);
        input.setText(AppStore.getAdminPhone(this));
        new AlertDialog.Builder(this)
                .setTitle("تعديل رقم إدارة الشبكة")
                .setView(input)
                .setPositiveButton("حفظ", (d,w) -> {
                    AppStore.setAdminPhone(this, input.getText().toString());
                    toast("تم حفظ رقم الإدارة");
                    showMessageTemplates();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showMessageTemplateDialog(boolean successMessage) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView help = small(successMessage
                ? "عدّل رسالة الكرت كما تريد. يمكنك إضافة عرض في آخر الرسالة مثل: عرض خاص: عند شراء كرتين تحصل على خصم."
                : "عدّل رسالة نفاد الفئة. استخدم {adminPhone} حتى يظهر رقم إدارة الشبكة تلقائيًا.");

        TextView vars = small("المتغيرات المتاحة: {amount}  {card}  {network}  {adminPhone}");

        EditText input = new EditText(this);
        input.setMinLines(4);
        input.setMaxLines(8);
        input.setGravity(Gravity.TOP | Gravity.RIGHT);
        input.setSingleLine(false);
        input.setVerticalScrollBarEnabled(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(successMessage ? AppStore.getSuccessTemplate(this) : AppStore.getNoStockTemplate(this));

        TextView previewTitle = small("المعاينة:");
        TextView preview = messagePreviewText(successMessage
                ? AppStore.applyTemplate(this, input.getText().toString(), 100, "1547013174")
                : AppStore.applyTemplate(this, input.getText().toString(), 100, ""));

        Button previewBtn = action("تحديث المعاينة", card2, text, v -> {
            preview.setText(successMessage
                    ? AppStore.applyTemplate(this, input.getText().toString(), 100, "1547013174")
                    : AppStore.applyTemplate(this, input.getText().toString(), 100, ""));
        });

        Button offerBtn = action("إضافة سطر عرض", Color.rgb(50, 70, 55), Color.WHITE, v -> {
            input.append("\n\nعرض خاص: ");
            preview.setText(successMessage
                    ? AppStore.applyTemplate(this, input.getText().toString(), 100, "1547013174")
                    : AppStore.applyTemplate(this, input.getText().toString(), 100, ""));
        });

        layout.addView(help);
        layout.addView(vars);
        layout.addView(input);
        layout.addView(previewBtn);
        if (successMessage) layout.addView(offerBtn);
        layout.addView(previewTitle);
        layout.addView(preview);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(successMessage ? "تعديل رسالة إرسال الكرت" : "تعديل رسالة نفاد الفئة")
                .setView(scrollableDialogView(layout))
                .setPositiveButton("حفظ", (d,w) -> {
                    String value = input.getText().toString();
                    if (successMessage) AppStore.setSuccessTemplate(this, value);
                    else AppStore.setNoStockTemplate(this, value);
                    toast("تم حفظ نص الرسالة");
                    showMessageTemplates();
                })
                .setNegativeButton("إلغاء", null)
                .show();
        makeDialogKeyboardFriendly(dialog);
    }


    private void showRewardsSettings() {
        setTab("settings");
        clear();
        content.addView(title("مكافآت العملاء"));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(14), dp(14), dp(14), dp(14));
        top.setBackground(round(Color.rgb(77, 52, 15), dp(20), goldSoft, dp(1)));
        LinearLayout.LayoutParams topLp = new LinearLayout.LayoutParams(-1, -2);
        topLp.setMargins(0, 0, 0, dp(12));
        top.setLayoutParams(topLp);
        top.addView(tv("🏆 نظام المكافآت الذهبي", 19, goldSoft, true));
        top.addView(small("نسبة افتراضية 8%، مع إمكانية تشغيل/إيقاف النقاط وتعديل النسبة لكل فئة. عند وصول العميل إلى الحد المحدد يتم إرسال كرت المكافأة آليًا عند توفره."));
        content.addView(top);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        shell.setBaselineAligned(false);
        LinearLayout.LayoutParams shellLp = new LinearLayout.LayoutParams(-1, -2);
        shellLp.setMargins(0, 0, 0, dp(12));
        shell.setLayoutParams(shellLp);

        LinearLayout side = rewardsSidebar();
        LinearLayout pane = rewardsPane();

        shell.addView(side, new LinearLayout.LayoutParams(dp(118), -2));
        LinearLayout.LayoutParams paneLp = new LinearLayout.LayoutParams(0, -2, 1);
        paneLp.setMargins(dp(8), 0, 0, 0);
        shell.addView(pane, paneLp);
        content.addView(shell);

        if ("settings".equals(rewardsPanelTab)) renderRewardsGeneral(pane);
        else if ("categories".equals(rewardsPanelTab)) renderRewardsCategories(pane);
        else if ("customers".equals(rewardsPanelTab)) renderRewardsCustomersPanel(pane);
        else if ("gift".equals(rewardsPanelTab)) renderRewardsGiftPanel(pane);
        else if ("expiry".equals(rewardsPanelTab)) renderRewardsExpiryPanel(pane);
        else renderRewardsOverview(pane);
    }

    private LinearLayout rewardsSidebar() {
        LinearLayout side = new LinearLayout(this);
        side.setOrientation(LinearLayout.VERTICAL);
        side.setPadding(dp(7), dp(10), dp(7), dp(10));
        side.setBackground(round(goldDark, dp(22), goldSoft, dp(1)));
        side.addView(rewardsSideTitle("القائمة"));
        side.addView(rewardsSideButton("overview", "الرئيسية"));
        side.addView(rewardsSideButton("settings", "الإعدادات"));
        side.addView(rewardsSideButton("categories", "الفئات"));
        side.addView(rewardsSideButton("customers", "العملاء"));
        side.addView(rewardsSideButton("gift", "المكافأة"));
        side.addView(rewardsSideButton("expiry", "الصلاحية"));
        Space sp = new Space(this);
        side.addView(sp, new LinearLayout.LayoutParams(-1, dp(8)));
        Button back = action("رجوع", Color.rgb(48, 38, 55), Color.WHITE, v -> showSettings());
        side.addView(back, new LinearLayout.LayoutParams(-1, dp(48)));
        return side;
    }

    private TextView rewardsSideTitle(String value) {
        TextView t = tv(value, 15, goldSoft, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, dp(8));
        return t;
    }

    private Button rewardsSideButton(String key, String label) {
        boolean selected = key.equals(rewardsPanelTab);
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(12);
        b.setTypeface(appTypeface(true));
        b.setTextColor(selected ? Color.rgb(35, 25, 10) : Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setBackground(round(selected ? goldSoft : Color.rgb(111, 78, 22), dp(16), selected ? Color.WHITE : Color.argb(110,255,229,151), selected ? dp(1) : dp(1)));
        b.setOnClickListener(v -> { rewardsPanelTab = key; showRewardsSettings(); });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(46));
        lp.setMargins(0, 0, 0, dp(7));
        b.setLayoutParams(lp);
        return b;
    }

    private LinearLayout rewardsPane() {
        LinearLayout pane = new LinearLayout(this);
        pane.setOrientation(LinearLayout.VERTICAL);
        pane.setPadding(dp(12), dp(12), dp(12), dp(12));
        pane.setBackground(round(card, dp(22), Color.argb(160, 218,170,72), dp(1)));
        return pane;
    }

    private LinearLayout rewardsGoldCard() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(12), dp(12), dp(12));
        box.setBackground(round(Color.rgb(43, 34, 49), dp(18), Color.argb(130,218,170,72), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        box.setLayoutParams(lp);
        return box;
    }

    private TextView rewardsSectionTitle(String value) {
        TextView t = tv(value, 17, goldSoft, true);
        t.setPadding(0, 0, 0, dp(8));
        return t;
    }

    private View rewardsMiniStat(String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(10), dp(10), dp(10));
        box.setGravity(Gravity.CENTER);
        box.setBackground(round(Color.rgb(55, 43, 31), dp(16), Color.argb(120,255,217,126), dp(1)));
        TextView v = tv(value, 18, goldSoft, true);
        v.setGravity(Gravity.CENTER);
        TextView l = tv(label, 11, muted, false);
        l.setGravity(Gravity.CENTER);
        box.addView(v);
        box.addView(l);
        return box;
    }

    private void addRewardsStatRow(LinearLayout parent, View a, View b) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(a, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams lpB = new LinearLayout.LayoutParams(0, -2, 1);
        lpB.setMargins(dp(8), 0, 0, 0);
        row.addView(b, lpB);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
        rowLp.setMargins(0, 0, 0, dp(8));
        parent.addView(row, rowLp);
    }

    private void renderRewardsOverview(LinearLayout pane) {
        pane.addView(rewardsSectionTitle("الرئيسية"));
        ArrayList<CustomerReward> customers = AppStore.loadCustomerRewards(this);
        int totalPoints = 0, totalRewards = 0, totalPaid = 0;
        for (CustomerReward cr : customers) {
            totalPoints += cr.points;
            totalRewards += cr.rewardCards;
            totalPaid += cr.totalPaid;
        }
        int activeCats = 0;
        for (CategoryItem cat : AppStore.loadCategories(this)) if (AppStore.isRewardEnabledForAmount(this, cat.amount)) activeCats++;
        addRewardsStatRow(pane, rewardsMiniStat("الحالة", AppStore.isRewardsEnabled(this) ? "مفعّل" : "متوقف"), rewardsMiniStat("العملاء", String.valueOf(customers.size())));
        addRewardsStatRow(pane, rewardsMiniStat("الفئات المفعلة", String.valueOf(activeCats)), rewardsMiniStat("النقاط الحالية", String.valueOf(totalPoints)));
        addRewardsStatRow(pane, rewardsMiniStat("المكافآت", String.valueOf(totalRewards)), rewardsMiniStat("مشتريات محسوبة", totalPaid + " ر"));

        LinearLayout info = rewardsGoldCard();
        info.addView(tv("ملخص النظام", 16, text, true));
        info.addView(small("النسبة الافتراضية 8%، وحد المكافأة الحالي " + AppStore.getRewardThreshold(this) + " نقطة، وفئة كرت المكافأة " + AppStore.getRewardCardAmount(this) + " ريال.\nصلاحية النقاط: " + (AppStore.isRewardExpiryEnabled(this) ? (AppStore.getRewardExpiryDays(this) + " أيام") : "متوقفة")));
        pane.addView(info);
        pane.addView(action("تعديل الإعدادات", goldDark, goldSoft, v -> { rewardsPanelTab = "settings"; showRewardsSettings(); }), new LinearLayout.LayoutParams(-1, dp(50)));
    }

    private void renderRewardsGeneral(LinearLayout pane) {
        pane.addView(rewardsSectionTitle("الإعدادات العامة"));
        LinearLayout main = rewardsGoldCard();
        Switch enabled = new Switch(this);
        enabled.setText("تشغيل نظام النقاط");
        enabled.setTextColor(text);
        enabled.setTextSize(16);
        enabled.setTypeface(appTypeface(true));
        enabled.setChecked(AppStore.isRewardsEnabled(this));
        main.addView(enabled);
        EditText threshold = new EditText(this);
        threshold.setHint("حد المكافأة بالنقاط مثل 100");
        threshold.setInputType(InputType.TYPE_CLASS_NUMBER);
        threshold.setGravity(Gravity.RIGHT);
        threshold.setText(String.valueOf(AppStore.getRewardThreshold(this)));
        threshold.setTextColor(text);
        threshold.setHintTextColor(muted);
        main.addView(threshold);
        EditText rewardAmount = new EditText(this);
        rewardAmount.setHint("فئة كرت المكافأة مثل 100");
        rewardAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        rewardAmount.setGravity(Gravity.RIGHT);
        rewardAmount.setText(String.valueOf(AppStore.getRewardCardAmount(this)));
        rewardAmount.setTextColor(text);
        rewardAmount.setHintTextColor(muted);
        main.addView(rewardAmount);
        main.addView(action("حفظ الإعدادات", goldDark, goldSoft, v -> {
            int th = safeInt(threshold.getText().toString(), 100);
            int ra = safeInt(rewardAmount.getText().toString(), 100);
            AppStore.setRewardsMainSettings(this, enabled.isChecked(), th, ra);
            toast("تم حفظ إعدادات المكافآت");
            showRewardsSettings();
        }));
        main.addView(small("كرت المكافأة لا يضيف نقاطًا. إذا لم تتوفر كروت فئة المكافأة، يسجل النظام العملية كمكافأة معلقة في السجل."));
        pane.addView(main);
    }

    private void renderRewardsCategories(LinearLayout pane) {
        pane.addView(rewardsSectionTitle("الفئات والنِسَب"));
        LinearLayout hint = rewardsGoldCard();
        hint.addView(small("شغّل أو أوقف النقاط لكل فئة، وعدّل النسبة حسب سياسة البيع. الافتراضي الحالي 8%."));
        pane.addView(hint);
        for (CategoryItem cat : AppStore.loadCategories(this)) {
            LinearLayout box = rewardsGoldCard();
            boolean catEnabled = AppStore.isRewardEnabledForAmount(this, cat.amount);
            int percent = AppStore.getRewardPercentForAmount(this, cat.amount);
            int samplePoints = (int)Math.floor((cat.amount * percent) / 100.0d);
            box.addView(tv(cat.name + " - " + cat.amount + " ريال", 16, text, true));
            box.addView(small("الحالة: " + (catEnabled ? "مفعلة" : "متوقفة") + " | النسبة: " + percent + "% | نقاط الكرت: " + samplePoints));
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(action(catEnabled ? "إيقاف" : "تشغيل", catEnabled ? Color.rgb(91, 41, 43) : Color.rgb(45, 88, 62), Color.WHITE, v -> {
                AppStore.setRewardCategorySetting(this, cat.amount, !AppStore.isRewardEnabledForAmount(this, cat.amount), AppStore.getRewardPercentForAmount(this, cat.amount));
                showRewardsSettings();
            }), new LinearLayout.LayoutParams(0, dp(48), 1));
            LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(0, dp(48), 1);
            editLp.setMargins(dp(8), 0, 0, 0);
            row.addView(action("تعديل", goldDark, goldSoft, v -> showRewardCategoryDialog(cat)), editLp);
            box.addView(row);
            pane.addView(box);
        }
    }

    private void renderRewardsCustomersPanel(LinearLayout pane) {
        pane.addView(rewardsSectionTitle("عملاء النقاط"));
        ArrayList<CustomerReward> list = AppStore.loadCustomerRewards(this);
        if (list.isEmpty()) {
            LinearLayout empty = rewardsGoldCard();
            empty.addView(small("لا يوجد عملاء نقاط حتى الآن. عند بيع كرت لرقم زبون سيبدأ النظام بتسجيل نقاطه تلقائيًا."));
            pane.addView(empty);
            return;
        }
        for (CustomerReward cr : list) {
            LinearLayout box = rewardsGoldCard();
            box.addView(tv(cr.phone, 17, goldSoft, true));
            box.addView(small((cr.name.isEmpty() ? "" : "الاسم: " + cr.name + "\n")
                    + "النقاط الحالية: " + cr.points
                    + "\nإجمالي مشتريات محسوبة: " + cr.totalPaid + " ريال"
                    + "\nإجمالي النقاط المكتسبة: " + cr.totalEarnedPoints
                    + "\nالنقاط المستخدمة: " + cr.totalUsedPoints
                    + "\nكروت المكافأة: " + cr.rewardCards
                    + "\nآخر شراء: " + (cr.lastPurchaseAt.isEmpty() ? "-" : cr.lastPurchaseAt)));
            pane.addView(box);
        }
    }


    private void renderRewardsExpiryPanel(LinearLayout pane) {
        pane.addView(rewardsSectionTitle("صلاحية النقاط والكشوفات"));
        int expiredNow = AppStore.applyRewardPointExpiry(this);

        LinearLayout settings = rewardsGoldCard();
        settings.addView(tv("إعدادات صلاحية النقاط", 16, text, true));
        Switch enabled = new Switch(this);
        enabled.setText("تصفير النقاط عند عدم التعبئة");
        enabled.setTextColor(text);
        enabled.setTextSize(15);
        enabled.setTypeface(appTypeface(true));
        enabled.setChecked(AppStore.isRewardExpiryEnabled(this));
        settings.addView(enabled);
        EditText days = new EditText(this);
        days.setHint("مدة عدم النشاط بالأيام - مثال 10");
        days.setInputType(InputType.TYPE_CLASS_NUMBER);
        days.setGravity(Gravity.RIGHT);
        days.setText(String.valueOf(AppStore.getRewardExpiryDays(this)));
        days.setTextColor(text);
        days.setHintTextColor(muted);
        settings.addView(days);
        settings.addView(small("عند تفعيل هذا الخيار: إذا لم يقم العميل بأي تعبئة جديدة خلال المدة المحددة، يتم تصفير نقاطه وتسجيلها في سجل النقاط المصادرة. العملاء الذين بقي لهم يومان أو أقل يظهرون في كشف التحذير."));
        settings.addView(action("حفظ صلاحية النقاط", goldDark, goldSoft, v -> {
            int d = safeInt(days.getText().toString(), 10);
            AppStore.setRewardExpirySettings(this, enabled.isChecked(), d);
            toast("تم حفظ إعدادات صلاحية النقاط");
            rewardsPanelTab = "expiry";
            showRewardsSettings();
        }));
        if (expiredNow > 0) settings.addView(small("تمت مصادرة نقاط " + expiredNow + " عميل بعد انتهاء مدة عدم النشاط."));
        pane.addView(settings);

        LinearLayout warnHint = rewardsGoldCard();
        warnHint.addView(tv("عملاء قرب انتهاء النقاط", 16, goldSoft, true));
        warnHint.addView(small("يعرض العملاء الذين بقي على مصادرة نقاطهم يومان أو أقل. تستطيع تجهيز كشف مرتب وإرساله عبر WhatsApp Business بعد المراجعة."));
        pane.addView(warnHint);

        ArrayList<RewardExpiryInfo> soon = AppStore.loadRewardExpiringSoon(this, AppStore.DEFAULT_REWARD_EXPIRY_WARNING_DAYS);
        if (soon.isEmpty()) {
            LinearLayout empty = rewardsGoldCard();
            empty.addView(small("لا يوجد عملاء نقاطهم قاربت على الانتهاء حالياً."));
            pane.addView(empty);
        } else {
            for (RewardExpiryInfo info : soon) {
                LinearLayout box = rewardsGoldCard();
                box.addView(tv(info.phone, 17, goldSoft, true));
                box.addView(small((info.name.isEmpty() ? "" : "الاسم: " + info.name + "\n")
                        + "النقاط المعرضة للمصادرة: " + info.points
                        + "\nآخر تعبئة: " + (info.lastPurchaseAt.isEmpty() ? "-" : info.lastPurchaseAt)
                        + "\nتاريخ انتهاء النقاط: " + info.expiryAt
                        + "\nالمتبقي: " + info.daysLeft + " يوم"));
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.addView(action("عرض الكشف", card2, text, v -> showRewardStatementDialog("كشف نقاط معرضة للمصادرة", info.phone, AppStore.buildRewardExpiryWarningStatement(this, info))), new LinearLayout.LayoutParams(0, dp(48), 1));
                LinearLayout.LayoutParams waLp = new LinearLayout.LayoutParams(0, dp(48), 1);
                waLp.setMargins(dp(8), 0, 0, 0);
                row.addView(action("واتساب", Color.rgb(37, 211, 102), Color.WHITE, v -> openWhatsAppBusinessMessage(info.phone, AppStore.buildRewardExpiryWarningStatement(this, info))), waLp);
                box.addView(row);
                pane.addView(box);
            }
        }

        pane.addView(rewardsSectionTitle("سجل النقاط المصادرة"));
        ArrayList<OperationLog> expired = AppStore.loadRewardExpiryLogs(this, 50);
        if (expired.isEmpty()) {
            LinearLayout empty = rewardsGoldCard();
            empty.addView(small("لا توجد نقاط مصادرة حتى الآن."));
            pane.addView(empty);
        } else {
            for (OperationLog log : expired) {
                LinearLayout box = rewardsGoldCard();
                box.addView(tv(log.customerPhone == null || log.customerPhone.isEmpty() ? "عميل بدون رقم" : log.customerPhone, 17, red, true));
                box.addView(small(((log.customerName == null || log.customerName.trim().isEmpty()) ? "" : "الاسم: " + log.customerName + "\n")
                        + "النقاط المصادرة: " + log.amount
                        + "\nتاريخ المصادرة: " + log.createdAt
                        + "\nالتفاصيل: " + log.message));
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.addView(action("عرض كشف المصادرة", card2, text, v -> showRewardStatementDialog("كشف مصادرة النقاط", log.customerPhone, AppStore.buildRewardExpiryLogStatement(this, log))), new LinearLayout.LayoutParams(0, dp(48), 1));
                LinearLayout.LayoutParams waLp = new LinearLayout.LayoutParams(0, dp(48), 1);
                waLp.setMargins(dp(8), 0, 0, 0);
                row.addView(action("واتساب", Color.rgb(37, 211, 102), Color.WHITE, v -> openWhatsAppBusinessMessage(log.customerPhone, AppStore.buildRewardExpiryLogStatement(this, log))), waLp);
                box.addView(row);
                pane.addView(box);
            }
        }
    }

    private void showRewardStatementDialog(String titleText, String phone, String message) {
        TextView msg = messagePreviewText(message == null ? "" : message);
        msg.setPadding(dp(10), dp(10), dp(10), dp(10));
        new AlertDialog.Builder(this)
                .setTitle(titleText)
                .setView(msg)
                .setPositiveButton("إرسال واتساب", (d,w) -> openWhatsAppBusinessMessage(phone, message))
                .setNegativeButton("إغلاق", null)
                .show();
    }

    private void renderRewardsGiftPanel(LinearLayout pane) {
        pane.addView(rewardsSectionTitle("مخزون كروت المكافآت"));
        int rewardAmount = AppStore.getRewardCardAmount(this);
        int threshold = AppStore.getRewardThreshold(this);
        int rewardTotal = AppStore.rewardTotalCount(this, rewardAmount);
        int rewardAvailable = AppStore.rewardAvailableCount(this, rewardAmount);
        int rewardSent = AppStore.rewardSentCount(this, rewardAmount);
        LinearLayout box = rewardsGoldCard();
        box.addView(tv("الإعداد الحالي", 16, text, true));
        box.addView(small("حد المكافأة: " + threshold + " نقطة\nفئة كرت المكافأة: " + rewardAmount + " ريال\nمخزون المكافآت: " + rewardTotal + " | المتاح: " + rewardAvailable + " | المرسل: " + rewardSent));
        box.addView(separator());
        box.addView(small("كروت المكافآت مخزون مستقل. لا تُحسب ضمن كروت البيع ولا تضيف نقاطًا جديدة عند إرسالها."));
        pane.addView(box);

        LinearLayout tools = rewardsGoldCard();
        tools.addView(tv("تغذية مخزون المكافآت", 16, text, true));
        tools.addView(small("أضف كروت فئة المكافأة هنا فقط. النظام يسحب منها عند وصول العميل إلى حد النقاط."));
        tools.addView(action("➕ إضافة مكافآت يدويًا", goldDark, Color.WHITE, v -> showRewardManualAddDialog(rewardAmount)));
        tools.addView(action("📄 استيراد مكافآت TXT", Color.rgb(78, 61, 35), goldSoft, v -> openRewardTxtFile(rewardAmount)));
        tools.addView(action("📊 استيراد مكافآت Excel / CSV", Color.rgb(78, 61, 35), goldSoft, v -> openRewardExcelFile(rewardAmount)));
        pane.addView(tools);

        pane.addView(action("تعديل فئة وحد المكافأة", goldDark, goldSoft, v -> { rewardsPanelTab = "settings"; showRewardsSettings(); }), new LinearLayout.LayoutParams(-1, dp(50)));
    }

    private int safeInt(String value, int fallback) {
        try { return Integer.parseInt(value == null ? "" : value.trim()); }
        catch (Exception e) { return fallback; }
    }

    private void showRewardCategoryDialog(CategoryItem cat) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(4), dp(8), dp(4));
        CheckBox enabled = new CheckBox(this);
        enabled.setText("النقاط مفعلة لهذه الفئة");
        enabled.setChecked(AppStore.isRewardEnabledForAmount(this, cat.amount));
        EditText percent = new EditText(this);
        percent.setHint("النسبة مثل 8");
        percent.setInputType(InputType.TYPE_CLASS_NUMBER);
        percent.setGravity(Gravity.RIGHT);
        percent.setText(String.valueOf(AppStore.getRewardPercentForAmount(this, cat.amount)));
        layout.addView(enabled);
        layout.addView(percent);
        new AlertDialog.Builder(this)
                .setTitle("نقاط فئة " + cat.amount + " ريال")
                .setView(layout)
                .setPositiveButton("حفظ", (d,w) -> {
                    int p = safeInt(percent.getText().toString(), 8);
                    AppStore.setRewardCategorySetting(this, cat.amount, enabled.isChecked(), p);
                    toast("تم حفظ نسبة الفئة");
                    showRewardsSettings();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showRewardsCustomers() {
        rewardsPanelTab = "customers";
        showRewardsSettings();
    }

    private void showTrustedContacts() {
        setTab("settings");
        clear();
        content.addView(title("المحافظ والأسماء الموثوقة"));

        LinearLayout add = cardBox();
        add.addView(tv("إضافة محفظة أو اسم موثوق", 17, text, true));
        add.addView(small("هذه الصفحة تعرّف التطبيق على المحافظ التي لا تعطي رقم العميل داخل الرسالة. اكتب اسم المحفظة، وكلمات تظهر في المرسل أو نص الرسالة، والاسم كما يظهر في المحفظة، ورقم الزبون الذي سيستلم الكرت."));
        add.addView(action("إضافة محفظة / اسم", purple, Color.WHITE, v -> showTrustedDialog()));
        content.addView(add);

        ArrayList<TrustedContact> list = AppStore.loadTrustedContacts(this);
        if (list.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(small("لا توجد أسماء موثوقة بعد. أضف اسمًا لمحافظ مثل ONE Cash أو floosk أو كاش."));
            content.addView(empty);
        }
        for (TrustedContact contact : list) {
            LinearLayout box = cardBox();
            box.addView(tv(contact.walletName + " - " + contact.fullName, 18, text, true));
            box.addView(small("كلمات التعرف: " + (contact.senderKeywords == null || contact.senderKeywords.trim().isEmpty() ? contact.walletName : contact.senderKeywords)
                    + "\nالاسم الثلاثي: " + contact.tripleName
                    + "\nرقم استلام الكرت: " + contact.phone
                    + "\nالحالة: " + (contact.active ? "مفعّل" : "معطّل")));
            LinearLayout tools = new LinearLayout(this);
            tools.setOrientation(LinearLayout.HORIZONTAL);
            tools.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(50), 1);
            lp.setMargins(dp(3), dp(8), dp(3), dp(4));
            tools.addView(action("تعديل", purple, Color.WHITE, v -> showTrustedDialog(contact)), lp);
            tools.addView(action("حذف", Color.rgb(82,30,42), Color.WHITE, v -> confirmDeleteTrustedContact(contact)), lp);
            box.addView(tools);
            content.addView(box);
        }

        LinearLayout back = cardBox();
        back.addView(action("رجوع للإعدادات", card2, text, v -> showSettings()));
        content.addView(back);
    }

    private void confirmDeleteTrustedContact(TrustedContact contact) {
        String name = (contact.walletName == null ? "" : contact.walletName) + " - " + (contact.fullName == null ? "" : contact.fullName);
        new AlertDialog.Builder(this)
                .setTitle("تأكيد حذف المحفظة / الاسم")
                .setMessage("هل تريد حذف هذا التعريف؟\n\n" + name.trim() + "\n\nبعد الحذف لن يستخدمه التطبيق للتعرف على هذه المحفظة أو هذا الاسم.")
                .setPositiveButton("موافق، حذف", (d,w) -> { AppStore.deleteTrustedContact(this, contact.id); showTrustedContacts(); })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showTrustedDialog() {
        showTrustedDialog(null);
    }

    private void showTrustedDialog(TrustedContact old) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));

        TextView help = small("مثال: اسم المحفظة floosk، كلمات التعرف: floosk, فلوسك، الاسم الظاهر: محمد أحمد علي، رقم استلام الكرت: 77xxxxxxx. يمكن إضافة أكثر من عميل لنفس المحفظة.");
        EditText wallet = new EditText(this); wallet.setHint("اسم المحفظة مثل floosk أو ONE Cash أو كاش"); wallet.setGravity(Gravity.RIGHT); wallet.setInputType(InputType.TYPE_CLASS_TEXT);
        EditText keywords = new EditText(this); keywords.setHint("كلمات التعرف بالرسالة مفصولة بفاصلة مثل floosk, فلوسك, cash"); keywords.setGravity(Gravity.RIGHT); keywords.setInputType(InputType.TYPE_CLASS_TEXT);
        EditText name = new EditText(this); name.setHint("الاسم كما يظهر في رسالة المحفظة"); name.setGravity(Gravity.RIGHT); name.setInputType(InputType.TYPE_CLASS_TEXT);
        EditText phone = new EditText(this); phone.setHint("رقم استلام الكرت"); phone.setGravity(Gravity.RIGHT); phone.setInputType(InputType.TYPE_CLASS_PHONE);
        CheckBox addAllWallets = new CheckBox(this);
        addAllWallets.setText("إضافة نفس الاسم والرقم إلى جميع المحافظ الموجودة والافتراضية");
        addAllWallets.setTextColor(text);
        addAllWallets.setChecked(false);

        if (old != null) {
            wallet.setText(old.walletName);
            keywords.setText(old.senderKeywords);
            name.setText(old.fullName);
            phone.setText(old.phone);
        } else {
            wallet.setText("ONE Cash");
            keywords.setText("one cash, onecash, ون كاش");
        }

        layout.addView(help);
        layout.addView(wallet);
        layout.addView(keywords);
        layout.addView(name);
        layout.addView(phone);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(old == null ? "إضافة محفظة / اسم موثوق" : "تعديل محفظة / اسم موثوق")
                .setView(layout)
                .setPositiveButton("حفظ", null)
                .setNegativeButton("إلغاء", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String walletText = wallet.getText().toString().trim();
            String keywordText = keywords.getText().toString().trim();
            String nameText = name.getText().toString().trim();
            String phoneText = phone.getText().toString().trim();
            String err = AppStore.walletContactValidationMessage(walletText, nameText, phoneText);
            if (!err.isEmpty()) {
                toast(err);
                return;
            }
            boolean ok;
            if (old != null) {
                ok = AppStore.updateTrustedContact(this, old.id, walletText, keywordText, nameText, phoneText, true);
            } else {
                ok = addAllWallets.isChecked()
                        ? AppStore.addWalletContactToAllWallets(this, walletText, keywordText, nameText, phoneText)
                        : AppStore.addWalletContact(this, walletText, keywordText, nameText, phoneText);
            }
            if (!ok) {
                toast("لم يتم الحفظ. تأكد من الاسم الثلاثي ورقم يبدأ بـ 7 ومكون من 9 أرقام");
                return;
            }
            toast(old == null ? "تم حفظ المحفظة / الاسم الموثوق" : "تم تعديل المحفظة / الاسم الموثوق");
            dialog.dismiss();
            showTrustedContacts();
        });
    }


    private void showRouterStudio(String tab) {
        routerStudioTab = (tab == null || tab.trim().isEmpty()) ? "connect" : tab;
        setTab("router");
        clear();
        content.setPadding(dp(0), dp(0), dp(0), dp(0));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setPadding(dp(18), dp(18), dp(18), dp(18));
        head.setBackground(round(Color.rgb(17, 30, 47), dp(0), Color.TRANSPARENT, 0));
        TextView sub = tv("استوديو الكروت", 16, Color.rgb(192, 198, 208), true);
        sub.setGravity(Gravity.RIGHT);
        head.addView(sub);
        TextView big = tv("الراوتر", 34, Color.WHITE, true);
        big.setGravity(Gravity.RIGHT);
        head.addView(big);
        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        badges.setGravity(Gravity.RIGHT);
        badges.addView(routerBadge(routerStudioConnected ? "متصل" : "غير متصل", routerStudioConnected ? green : red));
        badges.addView(routerBadge("User Manager", Color.rgb(145, 151, 162)));
        head.addView(badges);
        content.addView(head, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);
        tabs.setPadding(dp(4), dp(6), dp(4), dp(6));
        tabs.setBackgroundColor(Color.WHITE);
        tabs.addView(routerTabButton("تنفيذ", "execute"), new LinearLayout.LayoutParams(0, dp(54), 1));
        tabs.addView(routerTabButton("القالب", "template"), new LinearLayout.LayoutParams(0, dp(54), 1));
        tabs.addView(routerTabButton("الكروت", "cards"), new LinearLayout.LayoutParams(0, dp(54), 1));
        tabs.addView(routerTabButton("الاتصال", "connect"), new LinearLayout.LayoutParams(0, dp(54), 1));
        content.addView(tabs);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(14), dp(14), dp(18));
        body.setBackgroundColor(Color.rgb(247, 248, 252));
        content.addView(body, new LinearLayout.LayoutParams(-1, -2));

        if ("connect".equals(routerStudioTab)) showRouterConnectBody(body);
        else if ("cards".equals(routerStudioTab)) showRouterCardsBody(body);
        else if ("template".equals(routerStudioTab)) showRouterTemplateBody(body);
        else showRouterExecuteBody(body);
    }

    private TextView routerBadge(String label, int color) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(appTypeface(true));
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(7), dp(14), dp(7));
        b.setBackground(round(color, dp(22), Color.argb(50, 255,255,255), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(dp(6), dp(12), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    private Button routerTabButton(String label, String key) {
        boolean selected = key.equals(routerStudioTab);
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTypeface(appTypeface(selected));
        b.setTextSize(15);
        b.setTextColor(selected ? purple : Color.rgb(80, 82, 92));
        b.setBackground(round(Color.TRANSPARENT, dp(1), selected ? purple : Color.TRANSPARENT, selected ? dp(2) : 0));
        b.setOnClickListener(v -> showRouterStudio(key));
        return b;
    }

    private LinearLayout routerWhiteCard() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        box.setBackground(round(Color.WHITE, dp(18), Color.rgb(230, 232, 238), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(14));
        box.setLayoutParams(lp);
        return box;
    }

    private TextView routerText(String value, int size, int color, boolean bold) {
        TextView t = tv(value, size, color, bold);
        t.setGravity(Gravity.RIGHT);
        return t;
    }

    private EditText routerInput(String hint, String value, int inputType) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value == null ? "" : value);
        e.setSingleLine(true);
        e.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        e.setTextSize(16);
        e.setTextColor(Color.rgb(18, 22, 30));
        e.setHintTextColor(Color.rgb(120, 126, 138));
        e.setTypeface(appTypeface(false));
        e.setInputType(inputType);
        e.setPadding(dp(14), 0, dp(14), 0);
        e.setBackground(round(Color.rgb(250, 251, 254), dp(10), Color.rgb(217, 220, 230), dp(1)));
        return e;
    }

    private void showRouterConnectBody(LinearLayout body) {
        LinearLayout box = routerWhiteCard();
        box.addView(routerText("اتصال راوتر", 23, Color.rgb(25, 29, 38), true));
        box.addView(routerText("أدخل بيانات RouterOS API، ثم اتصل بالراوتر قبل توليد الكروت.", 13, Color.rgb(105, 110, 122), false));

        EditText host = routerInput("عنوان IP", AppStore.getMikroTikHost(this), InputType.TYPE_CLASS_TEXT);
        EditText port = routerInput("منفذ API", String.valueOf(AppStore.getMikroTikPort(this)), InputType.TYPE_CLASS_NUMBER);
        EditText user = routerInput("المستخدم", AppStore.getMikroTikUser(this), InputType.TYPE_CLASS_TEXT);
        EditText pass = routerInput("كلمة السر", routerStudioPasswordDraft, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(58));
        lp.setMargins(0, dp(10), 0, 0);
        box.addView(host, lp);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, dp(58), 1);
        half.setMargins(dp(4), dp(10), dp(4), 0);
        row.addView(user, half);
        row.addView(port, new LinearLayout.LayoutParams(0, dp(58), 1));
        ((LinearLayout.LayoutParams)port.getLayoutParams()).setMargins(dp(4), dp(10), dp(4), 0);
        box.addView(row);
        LinearLayout.LayoutParams passLp = new LinearLayout.LayoutParams(-1, dp(58));
        passLp.setMargins(0, dp(10), 0, 0);
        box.addView(pass, passLp);

        CheckBox save = new CheckBox(this);
        save.setText("حفظ بيانات الاتصال");
        save.setTextColor(Color.rgb(25, 29, 38));
        save.setTextSize(15);
        save.setGravity(Gravity.RIGHT);
        save.setChecked(true);
        box.addView(save);

        Button connect = action("اتصال بالراوتر", red, Color.WHITE, v -> {
            int p = parseIntSafe(port.getText().toString(), 8728);
            routerStudioPasswordDraft = pass.getText().toString();
            if (save.isChecked()) AppStore.saveMikroTikSettings(this, host.getText().toString().trim(), p, user.getText().toString().trim(), AppStore.getMikroTikProfileFilter(this), AppStore.getMikroTikLastLimit(this));
            testRouterStudioConnection(host.getText().toString().trim(), p, user.getText().toString().trim(), routerStudioPasswordDraft);
        });
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, dp(58));
        clp.setMargins(0, dp(12), 0, 0);
        box.addView(connect, clp);
        body.addView(box);

        LinearLayout typeBox = routerWhiteCard();
        typeBox.addView(routerText("نوع الإضافة", 22, Color.rgb(25, 29, 38), true));
        typeBox.addView(routerText("النظام الحالي يعمل على User Manager فقط، وليس Hotspot Users.", 14, Color.rgb(105, 110, 122), false));
        typeBox.addView(routerBadge("User Manager", Color.rgb(145, 151, 162)));
        body.addView(typeBox);
    }

    private void testRouterStudioConnection(String host, int port, String user, String pass) {
        if (host.isEmpty() || user.isEmpty()) { toast("أدخل IP الراوتر والمستخدم"); return; }
        toast("جاري الاتصال بالراوتر...");
        new Thread(() -> {
            MikroTikApiClient client = new MikroTikApiClient();
            boolean ok = false; String msg;
            try {
                client.connect(host, port <= 0 ? 8728 : port, 12000);
                client.login(user, pass == null ? "" : pass);
                ArrayList<String> profiles = client.getUserManagerProfiles();
                ok = true;
                msg = "تم الاتصال بنجاح. عدد بروفايلات User Manager المقروءة: " + profiles.size();
            } catch (Exception e) { msg = e.getMessage(); }
            finally { client.close(); }
            final boolean finalOk = ok; final String finalMsg = msg;
            new Handler(Looper.getMainLooper()).post(() -> {
                routerStudioConnected = finalOk;
                new AlertDialog.Builder(this).setTitle(finalOk ? "متصل" : "فشل الاتصال").setMessage(finalMsg).setPositiveButton("موافق", (d,w) -> showRouterStudio(finalOk ? "cards" : "connect")).show();
            });
        }).start();
    }

    private Button routerCategoryButton(CategoryItem cat) {
        boolean sel = cat.amount == selectedAmount;
        Button b = new Button(this);
        b.setText(cat.name + "\n" + cat.amount + " ريال");
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTypeface(appTypeface(sel));
        b.setGravity(Gravity.CENTER);
        b.setTextColor(sel ? Color.WHITE : Color.rgb(40, 43, 54));
        b.setBackground(round(sel ? purple : Color.rgb(250,251,254), dp(14), sel ? purpleLight : Color.rgb(218,221,230), dp(1)));
        b.setOnClickListener(v -> { selectedAmount = cat.amount; showRouterStudio("cards"); });
        return b;
    }

    private void showRouterCardsBody(LinearLayout body) {
        LinearLayout profileBox = routerWhiteCard();
        profileBox.addView(routerText("Profile", 16, purple, true));
        EditText profile = routerInput("بروفايل User Manager مثل free أو 100", AppStore.getMikroTikProfileFilter(this), InputType.TYPE_CLASS_TEXT);
        LinearLayout rowp = new LinearLayout(this); rowp.setOrientation(LinearLayout.HORIZONTAL);
        Button refresh = action("⟳", Color.WHITE, purple, v -> showMikroTikProfilesToEdit(profile));
        rowp.addView(refresh, new LinearLayout.LayoutParams(dp(74), dp(64)));
        LinearLayout.LayoutParams proLp = new LinearLayout.LayoutParams(0, dp(64), 1);
        proLp.setMargins(dp(8), 0, 0, 0);
        rowp.addView(profile, proLp);
        profileBox.addView(rowp);
        body.addView(profileBox);

        LinearLayout cats = routerWhiteCard();
        cats.addView(routerText("الفئة داخل التطبيق", 20, Color.rgb(25, 29, 38), true));
        ArrayList<CategoryItem> allCats = AppStore.loadCategories(this);
        LinearLayout r = null; int col = 0;
        for (CategoryItem c : allCats) {
            if (col == 0) { r = new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL); cats.addView(r); }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(58), 1); lp.setMargins(dp(4), dp(8), dp(4), 0);
            r.addView(routerCategoryButton(c), lp);
            col++; if (col == 2) col = 0;
        }
        cats.addView(routerText("المختار: " + selectedAmount + " ريال", 14, purple, true));
        body.addView(cats);

        LinearLayout gen = routerWhiteCard();
        gen.addView(routerText("توليد الكروت", 23, Color.rgb(25, 29, 38), true));
        EditText count = routerInput("العدد", AppStore.getMikroTikLastLimit(this) <= 0 ? "5" : String.valueOf(AppStore.getMikroTikLastLimit(this)), InputType.TYPE_CLASS_NUMBER);
        EditText len = routerInput("طول الرمز بعد البادئة", "9", InputType.TYPE_CLASS_NUMBER);
        EditText prefix = routerInput("البادئة - أرقام فقط، تضاف قبل الرمز", "5", InputType.TYPE_CLASS_NUMBER);
        LinearLayout first = new LinearLayout(this); first.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0, dp(58), 1); fp.setMargins(dp(4), dp(10), dp(4), 0);
        first.addView(len, fp); first.addView(count, new LinearLayout.LayoutParams(0, dp(58), 1)); ((LinearLayout.LayoutParams)count.getLayoutParams()).setMargins(dp(4), dp(10), dp(4), 0);
        gen.addView(first);
        LinearLayout.LayoutParams preLp = new LinearLayout.LayoutParams(-1, dp(58)); preLp.setMargins(0, dp(10), 0, 0); gen.addView(prefix, preLp);
        gen.addView(routerText("نوع كلمة المرور", 15, Color.rgb(80, 84, 96), true));
        RadioGroup passMode = new RadioGroup(this); passMode.setOrientation(RadioGroup.HORIZONTAL); passMode.setGravity(Gravity.CENTER);
        RadioButton same = new RadioButton(this); same.setText("متساوية"); same.setTextColor(Color.rgb(30,34,45)); same.setId(1);
        RadioButton empty = new RadioButton(this); empty.setText("فارغة"); empty.setTextColor(Color.rgb(30,34,45)); empty.setId(2);
        RadioButton userOnly = new RadioButton(this); userOnly.setText("اسم مستخدم فقط"); userOnly.setTextColor(Color.rgb(30,34,45)); userOnly.setId(3);
        passMode.addView(userOnly); passMode.addView(empty); passMode.addView(same);
        passMode.check("empty".equals(routerStudioPasswordMode) ? 2 : ("useronly".equals(routerStudioPasswordMode) ? 3 : 1));
        gen.addView(passMode);
        Button make = action("توليد الكروت", purple, Color.WHITE, v -> {
            int checked = passMode.getCheckedRadioButtonId();
            routerStudioPasswordMode = checked == 2 ? "empty" : (checked == 3 ? "useronly" : "username");
            createCardsInMikroTikWithPasswordMode(
                    AppStore.getMikroTikHost(this), AppStore.getMikroTikPort(this), AppStore.getMikroTikUser(this), routerStudioPasswordDraft,
                    profile.getText().toString().trim(), "admin", parseIntSafe(count.getText().toString(), 0), parseIntSafe(len.getText().toString(), 9), prefix.getText().toString().trim(), routerStudioPasswordMode);
        });
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(-1, dp(58)); mlp.setMargins(0, dp(12), 0, 0); gen.addView(make, mlp);
        body.addView(gen);
    }

    private void showMikroTikProfilesToEdit(EditText profileField) {
        String host = AppStore.getMikroTikHost(this); int port = AppStore.getMikroTikPort(this); String user = AppStore.getMikroTikUser(this);
        if (routerStudioPasswordDraft == null) routerStudioPasswordDraft = "";
        toast("جاري قراءة بروفايلات User Manager...");
        new Thread(() -> {
            MikroTikApiClient client = new MikroTikApiClient();
            ArrayList<String> profiles = new ArrayList<>(); String error = null;
            try { client.connect(host, port <= 0 ? 8728 : port, 10000); client.login(user, routerStudioPasswordDraft); profiles = client.getUserManagerProfiles(); }
            catch(Exception e) { error = e.getMessage(); }
            finally { client.close(); }
            final ArrayList<String> finalProfiles = profiles; final String finalError = error;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalError != null) { toast("فشل قراءة البروفايلات: " + finalError); return; }
                if (finalProfiles.isEmpty()) { toast("لا توجد بروفايلات"); return; }
                String[] arr = finalProfiles.toArray(new String[0]);
                new AlertDialog.Builder(this).setTitle("اختر بروفايل").setItems(arr, (d, which) -> profileField.setText(arr[which])).show();
            });
        }).start();
    }

    private void showRouterTemplateBody(LinearLayout body) {
        LinearLayout box = routerWhiteCard();
        box.addView(routerText("القالب", 23, Color.rgb(25, 29, 38), true));
        box.addView(routerText("هذه الصفحة لتجهيز شكل طباعة الكروت. حاليًا يتم إنشاء الكرت كرقم/اسم مستخدم فقط، ثم تستطيع الطباعة من تقارير PDF أو صفحة الفئة.", 14, Color.rgb(105,110,122), false));
        box.addView(action("فتح تقارير PDF", purple, Color.WHITE, v -> showLogs()));
        body.addView(box);
    }

    private void showRouterExecuteBody(LinearLayout body) {
        LinearLayout box = routerWhiteCard();
        box.addView(routerText("تنفيذ", 23, Color.rgb(25, 29, 38), true));
        box.addView(routerText("التنفيذ الفعلي يتم من تبويب الكروت بعد اختيار Profile، الفئة، العدد، والبادئة. كل دفعة تُضاف إلى User Manager وإلى التطبيق مباشرة وتُسجل في السجل.", 14, Color.rgb(105,110,122), false));
        box.addView(action("اذهب إلى الكروت", purple, Color.WHITE, v -> showRouterStudio("cards")));
        body.addView(box);
    }

    private void createCardsInMikroTikWithPasswordMode(String host, int port, String user, String pass, String profileName, String customerName, int quantity, int randomLength, String prefix, String passwordMode) {
        if (host.isEmpty() || user.isEmpty()) { toast("أدخل بيانات الاتصال من تبويب الاتصال أولًا"); return; }
        if (profileName == null || profileName.trim().isEmpty()) { toast("اختر أو اكتب بروفايل User Manager"); return; }
        if (quantity <= 0) { toast("أدخل عدد الكروت المطلوب"); return; }
        if (quantity > 1000) { toast("للحماية اجعل العدد 1000 أو أقل في الدفعة الواحدة"); return; }
        final int length = Math.max(4, Math.min(16, randomLength));
        final int amount = selectedAmount;
        final String cleanProfile = profileName.trim();
        final String cleanCustomer = (customerName == null || customerName.trim().isEmpty()) ? "admin" : customerName.trim();
        final String cleanPrefix = prefix == null ? "" : prefix.trim().replaceAll("[^0-9]", "");
        final String cleanMode = passwordMode == null ? "username" : passwordMode;
        AppStore.saveMikroTikSettings(this, host, port, user, cleanProfile, quantity);
        toast("جاري إنشاء الكروت في User Manager...");
        new Thread(() -> {
            MikroTikApiClient client = new MikroTikApiClient();
            ArrayList<String> appCodes = new ArrayList<>();
            HashSet<String> existingNames = new HashSet<>();
            String error = null; int created = 0; int added = 0;
            String batchId = "UM-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
            try {
                client.connect(host, port <= 0 ? 8728 : port, 15000);
                client.login(user, pass == null ? "" : pass);
                ArrayList<MikroTikApiClient.UserManagerUser> oldUsers = client.getUserManagerUsers();
                for (MikroTikApiClient.UserManagerUser old : oldUsers) if (old.username != null && !old.username.trim().isEmpty()) existingNames.add(old.username.trim());
                Random rnd = new Random(System.currentTimeMillis()); int attempts = 0;
                while (created < quantity && attempts < quantity * 40) {
                    attempts++;
                    String name = generateNumericCardCode(cleanPrefix, length, rnd);
                    if (existingNames.contains(name)) continue;
                    String userPass = "empty".equals(cleanMode) || "useronly".equals(cleanMode) ? "" : name;
                    String comment = "ONLINE_APP amount=" + amount + " batch=" + batchId + " mode=" + cleanMode;
                    client.addUserManagerNumericUser(name, userPass, cleanProfile, cleanCustomer, comment);
                    existingNames.add(name); created++; appCodes.add(name);
                }
                if (created < quantity) throw new Exception("تم إنشاء " + created + " فقط من أصل " + quantity + ". غيّر البادئة أو زد طول الرمز.");
                added = AppStore.importCards(this, amount, appCodes, "usermanager_studio:" + host + ":" + cleanProfile + ":" + batchId);
                AppStore.addLog(this, new OperationLog(java.util.UUID.randomUUID().toString(), "User Manager", host, "", "", amount,
                        "استوديو الراوتر", "تم إنشاء " + created + " كرت داخل User Manager وإضافة " + added + " للتطبيق | Profile: " + cleanProfile + " | Batch: " + batchId, "", AppStore.now()));
            } catch(Exception e) { error = e.getMessage(); }
            finally { client.close(); }
            final String finalError = error; final int finalCreated = created; final int finalAdded = added; final String finalBatch = batchId;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalError != null) new AlertDialog.Builder(this).setTitle("فشل التنفيذ").setMessage(finalError).setPositiveButton("موافق", null).show();
                else new AlertDialog.Builder(this).setTitle("تم إنشاء الكروت").setMessage("الفئة: " + amount + " ريال\nProfile: " + cleanProfile + "\nالعدد في User Manager: " + finalCreated + "\nالمضاف للتطبيق: " + finalAdded + "\nBatch: " + finalBatch + "\n\nالكروت محفوظة كأرقام فقط واسم مستخدم فقط داخل التطبيق.").setPositiveButton("موافق", (d,w) -> showRouterStudio("cards")).show();
            });
        }).start();
    }

    private void showMikroTikImportDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(4), dp(8), dp(4));

        TextView help = small("يسحب التطبيق مستخدمي Hotspot من MikroTik عبر RouterOS API ويضيفهم ككروت في الفئة المختارة. فعّل خدمة API في الراوتر واستخدم يوزر بصلاحية read,api فقط.");
        EditText host = new EditText(this); host.setHint("IP الراوتر مثل 192.168.88.1"); host.setGravity(Gravity.RIGHT); host.setInputType(InputType.TYPE_CLASS_TEXT); host.setText(AppStore.getMikroTikHost(this));
        EditText port = new EditText(this); port.setHint("منفذ API غالبًا 8728"); port.setGravity(Gravity.RIGHT); port.setInputType(InputType.TYPE_CLASS_NUMBER); port.setText(String.valueOf(AppStore.getMikroTikPort(this)));
        EditText user = new EditText(this); user.setHint("اسم مستخدم MikroTik API"); user.setGravity(Gravity.RIGHT); user.setInputType(InputType.TYPE_CLASS_TEXT); user.setText(AppStore.getMikroTikUser(this));
        EditText pass = new EditText(this); pass.setHint("كلمة مرور MikroTik API"); pass.setGravity(Gravity.RIGHT); pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText profile = new EditText(this); profile.setHint("فلتر Profile/Comment اختياري مثل 100 أو 1H"); profile.setGravity(Gravity.RIGHT); profile.setInputType(InputType.TYPE_CLASS_TEXT); profile.setText(AppStore.getMikroTikProfileFilter(this));
        EditText limit = new EditText(this); limit.setHint("العدد المطلوب: 0 = الكل"); limit.setGravity(Gravity.RIGHT); limit.setInputType(InputType.TYPE_CLASS_NUMBER); limit.setText(String.valueOf(AppStore.getMikroTikLastLimit(this)));

        layout.addView(help);
        layout.addView(host);
        layout.addView(port);
        layout.addView(user);
        layout.addView(pass);
        layout.addView(profile);
        layout.addView(limit);

        new AlertDialog.Builder(this)
                .setTitle("سحب كروت من MikroTik - فئة " + selectedAmount)
                .setView(layout)
                .setPositiveButton("سحب وإضافة", (d,w) -> {
                    int p = parseIntSafe(port.getText().toString(), 8728);
                    int l = parseIntSafe(limit.getText().toString(), 0);
                    importCardsFromMikroTik(
                            host.getText().toString().trim(),
                            p,
                            user.getText().toString().trim(),
                            pass.getText().toString(),
                            profile.getText().toString().trim(),
                            l
                    );
                })
                .setNeutralButton("اختبار الاتصال", (d,w) -> {
                    int p = parseIntSafe(port.getText().toString(), 8728);
                    testMikroTikConnection(host.getText().toString().trim(), p, user.getText().toString().trim(), pass.getText().toString());
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private int parseIntSafe(String value, int fallback) {
        try { return Integer.parseInt(value == null ? "" : value.trim()); } catch (Exception e) { return fallback; }
    }

    private void testMikroTikConnection(String host, int port, String user, String pass) {
        if (host.isEmpty() || user.isEmpty()) { toast("أدخل IP الراوتر واسم المستخدم"); return; }
        toast("جاري اختبار الاتصال بـ MikroTik...");
        new Thread(() -> {
            String result;
            MikroTikApiClient client = new MikroTikApiClient();
            try {
                client.connect(host, port <= 0 ? 8728 : port, 8000);
                client.login(user, pass == null ? "" : pass);
                result = "تم الاتصال وتسجيل الدخول بنجاح";
            } catch (Exception e) {
                result = "فشل الاتصال: " + e.getMessage();
            } finally {
                client.close();
            }
            final String msg = result;
            new Handler(Looper.getMainLooper()).post(() -> new AlertDialog.Builder(this)
                    .setTitle("نتيجة اختبار MikroTik")
                    .setMessage(msg)
                    .setPositiveButton("موافق", null)
                    .show());
        }).start();
    }

    private void importCardsFromMikroTik(String host, int port, String user, String pass, String profileFilter, int limit) {
        if (host.isEmpty() || user.isEmpty()) { toast("أدخل IP الراوتر واسم المستخدم"); return; }
        AppStore.saveMikroTikSettings(this, host, port, user, profileFilter, limit);
        toast("جاري سحب الكروت من MikroTik...");
        final int amount = selectedAmount;
        new Thread(() -> {
            MikroTikApiClient client = new MikroTikApiClient();
            ArrayList<String> codes = new ArrayList<>();
            String error = null;
            int found = 0;
            int added = 0;
            try {
                client.connect(host, port <= 0 ? 8728 : port, 12000);
                client.login(user, pass == null ? "" : pass);
                ArrayList<MikroTikApiClient.HotspotUser> users = client.getHotspotUsers(profileFilter, Math.max(0, limit));
                found = users.size();
                for (MikroTikApiClient.HotspotUser u : users) {
                    String name = u.name == null ? "" : u.name.trim();
                    String password = u.password == null ? "" : u.password.trim();
                    if (name.isEmpty()) continue;
                    String code;
                    if (password.isEmpty() || password.equals(name)) {
                        code = name;
                    } else {
                        code = "اسم المستخدم: " + name + "\nكلمة المرور: " + password;
                    }
                    codes.add(code);
                }
                added = AppStore.importCards(this, amount, codes, "mikrotik:" + host);
                AppStore.addLog(this, new OperationLog(java.util.UUID.randomUUID().toString(), "MikroTik", host, "", "", amount,
                        "استيراد كروت", "تم سحب " + found + " مستخدم وإضافة " + added + " كرت من MikroTik", "", AppStore.now()));
            } catch (Exception e) {
                error = e.getMessage();
            } finally {
                client.close();
            }
            final String finalError = error;
            final int finalFound = found;
            final int finalAdded = added;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalError != null) {
                    new AlertDialog.Builder(this)
                            .setTitle("فشل سحب الكروت من MikroTik")
                            .setMessage(finalError + "\n\nتأكد من تفعيل خدمة API والمنفذ 8728 وأن الهاتف على نفس شبكة الراوتر.")
                            .setPositiveButton("موافق", null)
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("تم السحب من MikroTik")
                            .setMessage("الفئة: " + amount + " ريال\nالمستخدمون المطابقون: " + finalFound + "\nالكروت الجديدة المضافة: " + finalAdded + "\n\nتم تجاهل الكروت المكررة تلقائيًا.")
                            .setPositiveButton("موافق", (d,w) -> showImport())
                            .show();
                }
            });
        }).start();
    }


    private void showMikroTikCreateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(4), dp(8), dp(4));

        TextView help = small("هذه العملية تنشئ كروت رقمية جديدة داخل MikroTik User Manager وليس Hotspot Users، ثم تضيف نفس الأرقام إلى البرنامج تحت الفئة المختارة. الكرت في التطبيق والطباعة يكون رقم/اسم مستخدم فقط. داخل User Manager يتم ضبط كلمة المرور مساوية لنفس الرقم حتى تعمل مع صفحات الهوتسبوت التي تستخدم username=password.");
        EditText host = new EditText(this); host.setHint("IP الراوتر مثل 192.168.88.1"); host.setGravity(Gravity.RIGHT); host.setInputType(InputType.TYPE_CLASS_TEXT); host.setText(AppStore.getMikroTikHost(this));
        EditText port = new EditText(this); port.setHint("منفذ API غالبًا 8728"); port.setGravity(Gravity.RIGHT); port.setInputType(InputType.TYPE_CLASS_NUMBER); port.setText(String.valueOf(AppStore.getMikroTikPort(this)));
        EditText user = new EditText(this); user.setHint("اسم مستخدم MikroTik API"); user.setGravity(Gravity.RIGHT); user.setInputType(InputType.TYPE_CLASS_TEXT); user.setText(AppStore.getMikroTikUser(this));
        EditText pass = new EditText(this); pass.setHint("كلمة مرور MikroTik API"); pass.setGravity(Gravity.RIGHT); pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText profile = new EditText(this); profile.setHint("بروفايل User Manager مثل 1H أو 100M"); profile.setGravity(Gravity.RIGHT); profile.setInputType(InputType.TYPE_CLASS_TEXT); profile.setText(AppStore.getMikroTikProfileFilter(this));
        EditText customer = new EditText(this); customer.setHint("Customer/Owner اختياري - غالبًا admin"); customer.setGravity(Gravity.RIGHT); customer.setInputType(InputType.TYPE_CLASS_TEXT); customer.setText("admin");
        EditText count = new EditText(this); count.setHint("عدد الكروت المطلوب إنشاؤها"); count.setGravity(Gravity.RIGHT); count.setInputType(InputType.TYPE_CLASS_NUMBER); count.setText("10");
        EditText codeLength = new EditText(this); codeLength.setHint("عدد الأرقام بعد البادئة، مثل 8"); codeLength.setGravity(Gravity.RIGHT); codeLength.setInputType(InputType.TYPE_CLASS_NUMBER); codeLength.setText("8");
        EditText prefix = new EditText(this); prefix.setHint("بادئة الأرقام اختيارية، أرقام فقط مثل 77 أو 100"); prefix.setGravity(Gravity.RIGHT); prefix.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(help);
        layout.addView(host);
        layout.addView(port);
        layout.addView(user);
        layout.addView(pass);
        layout.addView(profile);
        layout.addView(customer);
        layout.addView(count);
        layout.addView(codeLength);
        layout.addView(prefix);

        new AlertDialog.Builder(this)
                .setTitle("إنشاء كروت User Manager - فئة " + selectedAmount)
                .setView(layout)
                .setPositiveButton("إنشاء وإضافة", (d,w) -> {
                    int p = parseIntSafe(port.getText().toString(), 8728);
                    int q = parseIntSafe(count.getText().toString(), 0);
                    int len = parseIntSafe(codeLength.getText().toString(), 8);
                    createCardsInMikroTik(
                            host.getText().toString().trim(),
                            p,
                            user.getText().toString().trim(),
                            pass.getText().toString(),
                            profile.getText().toString().trim(),
                            customer.getText().toString().trim(),
                            q,
                            len,
                            prefix.getText().toString().trim()
                    );
                })
                .setNeutralButton("عرض بروفايلات User Manager", (d,w) -> {
                    int p = parseIntSafe(port.getText().toString(), 8728);
                    showMikroTikProfiles(host.getText().toString().trim(), p, user.getText().toString().trim(), pass.getText().toString());
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showMikroTikProfiles(String host, int port, String user, String pass) {
        if (host.isEmpty() || user.isEmpty()) { toast("أدخل IP الراوتر واسم المستخدم أولًا"); return; }
        toast("جاري قراءة بروفايلات User Manager...");
        new Thread(() -> {
            MikroTikApiClient client = new MikroTikApiClient();
            String msg;
            try {
                client.connect(host, port <= 0 ? 8728 : port, 10000);
                client.login(user, pass == null ? "" : pass);
                ArrayList<String> profiles = client.getUserManagerProfiles();
                if (profiles.isEmpty()) {
                    msg = "لم يتم العثور على Profiles. تأكد من وجود بروفايلات داخل MikroTik User Manager.";
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (String profile : profiles) sb.append("• ").append(profile).append("\n");
                    msg = sb.toString().trim();
                }
            } catch (Exception e) {
                msg = "فشل قراءة بروفايلات User Manager: " + e.getMessage();
            } finally {
                client.close();
            }
            final String finalMsg = msg;
            new Handler(Looper.getMainLooper()).post(() -> new AlertDialog.Builder(this)
                    .setTitle("بروفايلات User Manager")
                    .setMessage(finalMsg)
                    .setPositiveButton("موافق", null)
                    .show());
        }).start();
    }

    private void createCardsInMikroTik(String host, int port, String user, String pass, String profileName, String customerName, int quantity, int randomLength, String prefix) {
        if (host.isEmpty() || user.isEmpty()) { toast("أدخل IP الراوتر واسم المستخدم"); return; }
        if (profileName == null || profileName.trim().isEmpty()) { toast("اكتب اسم بروفايل User Manager"); return; }
        if (quantity <= 0) { toast("أدخل عدد الكروت المطلوب"); return; }
        if (quantity > 1000) { toast("للحماية اجعل العدد 1000 أو أقل في الدفعة الواحدة"); return; }
        final int length = Math.max(4, Math.min(16, randomLength));
        final int amount = selectedAmount;
        final String cleanProfile = profileName.trim();
        final String cleanCustomer = (customerName == null || customerName.trim().isEmpty()) ? "admin" : customerName.trim();
        final String cleanPrefix = prefix == null ? "" : prefix.trim().replaceAll("[^0-9]", "");
        AppStore.saveMikroTikSettings(this, host, port, user, cleanProfile, quantity);
        toast("جاري إنشاء الكروت في User Manager...");
        new Thread(() -> {
            MikroTikApiClient client = new MikroTikApiClient();
            ArrayList<String> appCodes = new ArrayList<>();
            HashSet<String> existingNames = new HashSet<>();
            String error = null;
            int created = 0;
            int added = 0;
            String batchId = "UM-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
            try {
                client.connect(host, port <= 0 ? 8728 : port, 15000);
                client.login(user, pass == null ? "" : pass);
                ArrayList<MikroTikApiClient.UserManagerUser> oldUsers = client.getUserManagerUsers();
                for (MikroTikApiClient.UserManagerUser old : oldUsers) {
                    if (old.username != null && !old.username.trim().isEmpty()) existingNames.add(old.username.trim());
                }
                Random rnd = new Random(System.currentTimeMillis());
                int attempts = 0;
                while (created < quantity && attempts < quantity * 40) {
                    attempts++;
                    String name = generateNumericCardCode(cleanPrefix, length, rnd);
                    if (existingNames.contains(name)) continue;
                    String comment = "ONLINE_APP amount=" + amount + " batch=" + batchId;
                    client.addUserManagerNumericUser(name, cleanProfile, cleanCustomer, comment);
                    existingNames.add(name);
                    created++;
                    appCodes.add(name);
                }
                if (created < quantity) throw new Exception("تم إنشاء " + created + " فقط من أصل " + quantity + ". حاول تغيير بادئة الأرقام أو زيادة عدد الأرقام.");
                added = AppStore.importCards(this, amount, appCodes, "usermanager_create:" + host + ":" + cleanProfile + ":" + batchId);
                AppStore.addLog(this, new OperationLog(java.util.UUID.randomUUID().toString(), "User Manager", host, "", "", amount,
                        "إنشاء كروت", "تم إنشاء " + created + " كرت رقمي داخل User Manager وإضافة " + added + " كرت إلى التطبيق | Profile: " + cleanProfile + " | Batch: " + batchId, "", AppStore.now()));
            } catch (Exception e) {
                error = e.getMessage();
            } finally {
                client.close();
            }
            final String finalError = error;
            final int finalCreated = created;
            final int finalAdded = added;
            final String finalBatch = batchId;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalError != null) {
                    new AlertDialog.Builder(this)
                            .setTitle("فشل إنشاء كروت User Manager")
                            .setMessage(finalError + "\n\nتأكد أن بروفايل User Manager مكتوب بنفس الاسم، وأن مستخدم API لديه صلاحية write,read,api، وأن User Manager مثبت ومفعل.")
                            .setPositiveButton("موافق", null)
                            .show();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("تم إنشاء الكروت")
                            .setMessage("الفئة داخل التطبيق: " + amount + " ريال\nبروفايل User Manager: " + cleanProfile + "\nCustomer/Owner: " + cleanCustomer + "\nعدد الكروت المنشأة في User Manager: " + finalCreated + "\nعدد الكروت المضافة للتطبيق: " + finalAdded + "\nرقم الدفعة: " + finalBatch + "\n\nالكروت أرقام فقط واسم مستخدم فقط داخل التطبيق والطباعة.")
                            .setPositiveButton("موافق", (d,w) -> showImport())
                            .show();
                }
            });
        }).start();
    }

    private String generateNumericCardCode(String prefix, int randomLength, Random rnd) {
        String cleanPrefix = prefix == null ? "" : prefix.trim().replaceAll("[^0-9]", "");
        StringBuilder sb = new StringBuilder();
        if (!cleanPrefix.isEmpty()) sb.append(cleanPrefix);
        for (int i = 0; i < randomLength; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    private String generateCardCode(String prefix, int randomLength, Random rnd) {
        String alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        if (prefix != null && !prefix.trim().isEmpty()) sb.append(prefix.trim().toUpperCase(Locale.US).replaceAll("[^A-Z0-9]", ""));
        for (int i = 0; i < randomLength; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }


    private void showRewardManualAddDialog(int amount) {
        EditText input = new EditText(this);
        input.setMinLines(7);
        input.setGravity(Gravity.TOP | Gravity.RIGHT);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("كل سطر = كرت مكافأة واحد");
        new AlertDialog.Builder(this)
                .setTitle("إضافة كروت مكافأة فئة " + amount)
                .setView(input)
                .setPositiveButton("حفظ", (d,w) -> {
                    ArrayList<String> lines = new ArrayList<>();
                    for (String line : input.getText().toString().split("\\r?\\n")) lines.add(line);
                    int added = AppStore.importRewardCards(this, amount, lines, "manual_reward");
                    toast("تمت إضافة " + added + " كرت إلى مخزون المكافآت");
                    rewardsPanelTab = "gift";
                    showRewardsSettings();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void openRewardTxtFile(int amount) {
        pendingRewardImport = true;
        pendingRewardImportAmount = amount;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_FILE);
    }

    private void openRewardExcelFile(int amount) {
        pendingRewardImport = true;
        pendingRewardImportAmount = amount;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_EXCEL);
    }

    private void showManualAddDialog() {
        EditText input = new EditText(this);
        input.setMinLines(7);
        input.setGravity(Gravity.TOP | Gravity.RIGHT);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("كل سطر = كرت واحد");
        new AlertDialog.Builder(this)
                .setTitle("إضافة كروت فئة " + selectedAmount)
                .setView(input)
                .setPositiveButton("حفظ", (d,w) -> {
                    ArrayList<String> lines = new ArrayList<>();
                    for (String line : input.getText().toString().split("\r?\n")) collectCardsFromTextLine(lines, line);
                    final AlertDialog progressDialog = showImportProgressDialog("إضافة يدوية", "إدخال يدوي");
                    final ImportStats stats = new ImportStats();
                    stats.scanned = lines.size();
                    performanceExecutor.execute(() -> {
                        try {
                            ArrayList<String> batch = new ArrayList<>();
                            for (String code : lines) {
                                batch.add(code);
                                if (batch.size() >= IMPORT_BATCH_SIZE) flushImportBatch(selectedAmount, batch, "manual", stats, false, 0, progressDialog);
                            }
                            flushImportBatch(selectedAmount, batch, "manual", stats, false, 0, progressDialog);
                            runOnUiThread(() -> {
                                TextView status = findImportStatusText(progressDialog);
                                if (status != null) status.setText("اكتملت الإضافة اليدوية
تمت القراءة: " + stats.scanned + "
تمت الإضافة: " + stats.added + "
المكرر/الموجود مسبقًا: " + stats.duplicates);
                                toneOk();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    try { progressDialog.dismiss(); } catch(Exception ignored) {}
                                    toast("تمت إضافة " + stats.added + " كرت");
                                    showHome();
                                }, 800);
                            });
                        } catch (Exception ex) {
                            runOnUiThread(() -> {
                                try { progressDialog.dismiss(); } catch(Exception ignored) {}
                                toneAlert();
                                toast("فشل الإدخال: " + ex.getMessage());
                            });
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }


    private void openBackupSave() {
        openBackupSaveToDrive();
    }

    private void openBackupRestore() {
        openBackupRestoreFromDrive();
    }

    private void writeBackupToUri(Uri uri) {
        try {
            OutputStream out = getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(AppStore.exportBackupJson(this).toString(2));
            writer.flush();
            writer.close();
            if (out != null) out.close();
            toast("تم حفظ النسخة الاحتياطية بنجاح");
        } catch (Exception e) {
            toast("فشل حفظ النسخة الاحتياطية: " + e.getMessage());
        }
    }

    private void restoreBackupFromUri(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            reader.close();
            final String backupText = sb.toString();
            confirmAndRestoreBackup(backupText, "ملف خارجي / Drive");
        } catch (Exception e) {
            toast("فشل قراءة ملف النسخة الاحتياطية: " + e.getMessage());
        }
    }


    private void openAdminPackFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_ADMIN_PACK);
    }

    private void importAdminPackFromUri(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            reader.close();
            int added = AppStore.importAdminCardPack(this, sb.toString());
            toast("تم استيراد " + added + " كرت من ملف الإدارة");
            showHome();
        } catch (Exception e) {
            toast("فشل استيراد ملف الإدارة: " + e.getMessage());
        }
    }

    private void openExcelFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_EXCEL);
    }

    private void openTxtFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE && resultCode == RESULT_OK && data != null) {
            importFromUri(data.getData());
        }
        if (requestCode == REQ_EXCEL && resultCode == RESULT_OK && data != null) {
            importFromExcelUri(data.getData());
        }
        if (requestCode == REQ_PDF && resultCode == RESULT_OK && data != null) {
            writePdfReport(data.getData(), pendingReportAmount, pendingReportSummaryOnly);
        }
        if (requestCode == REQ_BACKUP_SAVE && resultCode == RESULT_OK && data != null) {
            writeBackupToUri(data.getData());
        }
        if (requestCode == REQ_BACKUP_RESTORE && resultCode == RESULT_OK && data != null) {
            restoreBackupFromUri(data.getData());
        }
        if (requestCode == REQ_ADMIN_PACK && resultCode == RESULT_OK && data != null) {
            importAdminPackFromUri(data.getData());
        }
        if (requestCode == REQ_PICK_CONTACT && resultCode == RESULT_OK && data != null) {
            fillContactFromUri(data.getData());
        }
        if (requestCode == REQ_NETWORK_LOGO && resultCode == RESULT_OK && data != null) {
            saveNetworkLogoFromUri(data.getData());
        }
    }

    private void collectCardsFromTextLine(ArrayList<String> out, String line) {
        if (out == null || line == null) return;
        String cleaned = line.replace('\uFEFF', ' ').trim();
        if (cleaned.isEmpty()) return;
        String[] parts = cleaned.split("[\\s,;؛،|]+");
        for (String part : parts) {
            String v = AppStore.normalizeCardCode(cleanExcelCell(part));
            if (!v.isEmpty()) out.add(v);
        }
    }

    private static class ImportStats {
        int scanned = 0;
        int added = 0;
        int duplicates = 0;
        int failed = 0;
    }

    private AlertDialog showImportProgressDialog(String title, String fileName) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(18), dp(18), dp(14));
        layout.setBackground(round(Color.rgb(8, 11, 25), dp(22), neonCyan, dp(1)));

        TextView head = tv(title, 20, text, true);
        head.setGravity(Gravity.RIGHT);
        layout.addView(head);

        TextView file = small("الملف: " + (fileName == null || fileName.trim().isEmpty() ? "ملف مختار" : fileName));
        file.setTextColor(muted);
        layout.addView(file);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, dp(14));
        pp.setMargins(0, dp(16), 0, dp(12));
        layout.addView(progress, pp);

        TextView status = tv("جاري القراءة والاستيراد...", 16, text, true);
        status.setGravity(Gravity.RIGHT);
        status.setTag("import_status_text");
        layout.addView(status);

        TextView hint = small("لا تغلق التطبيق أثناء الاستيراد. يتم الإدخال على دفعات حتى لا يتشنج النظام.");
        hint.setTextColor(orange);
        layout.addView(hint);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(layout).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        });
        dialog.show();
        return dialog;
    }

    private TextView findImportStatusText(AlertDialog dialog) {
        if (dialog == null) return null;
        View root = dialog.getWindow() == null ? null : dialog.getWindow().getDecorView();
        if (root == null) return null;
        View found = findViewByTagRecursive(root, "import_status_text");
        return found instanceof TextView ? (TextView) found : null;
    }

    private View findViewByTagRecursive(View v, Object tag) {
        if (v == null) return null;
        if (tag != null && tag.equals(v.getTag())) return v;
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View found = findViewByTagRecursive(g.getChildAt(i), tag);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void updateImportDialog(AlertDialog dialog, ImportStats stats, String phase) {
        runOnUiThread(() -> {
            TextView status = findImportStatusText(dialog);
            if (status != null) {
                status.setText((phase == null ? "جاري الاستيراد..." : phase)
                        + "
تمت القراءة: " + stats.scanned
                        + "
تمت الإضافة: " + stats.added
                        + "
المكرر/الموجود مسبقًا: " + stats.duplicates
                        + (stats.failed > 0 ? "
فشل: " + stats.failed : ""));
            }
        });
    }

    private int flushImportBatch(int amount, ArrayList<String> batch, String sourceName, ImportStats stats, boolean rewardMode, int rewardAmount, AlertDialog dialog) {
        if (batch == null || batch.isEmpty()) return 0;
        int batchSize = batch.size();
        int added;
        if (rewardMode) {
            added = AppStore.importRewardCards(this, rewardAmount, batch, sourceName);
        } else {
            added = AppStore.importCardsWithProgress(this, amount, batch, sourceName, null);
        }
        stats.added += Math.max(0, added);
        stats.duplicates += Math.max(0, batchSize - added);
        batch.clear();
        updateImportDialog(dialog, stats, "جاري إدخال الدفعات...");
        try { Thread.sleep(15); } catch (Exception ignored) {}
        return added;
    }

    private void importFromUri(Uri uri) {
        final boolean rewardMode = pendingRewardImport;
        final int rewardAmount = pendingRewardImportAmount;
        final int amount = selectedAmount;
        final String sourceName = getFileName(uri);
        final AlertDialog progressDialog = showImportProgressDialog("استيراد الكروت", sourceName);
        final ImportStats stats = new ImportStats();
        performanceExecutor.execute(() -> {
            ArrayList<String> batch = new ArrayList<>();
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    int before = batch.size();
                    collectCardsFromTextLine(batch, line);
                    stats.scanned += Math.max(0, batch.size() - before);
                    if (stats.scanned % 200 == 0) updateImportDialog(progressDialog, stats, "جاري قراءة الملف...");
                    if (batch.size() >= IMPORT_BATCH_SIZE) flushImportBatch(amount, batch, sourceName, stats, rewardMode, rewardAmount, progressDialog);
                }
                reader.close();
                flushImportBatch(amount, batch, sourceName, stats, rewardMode, rewardAmount, progressDialog);
                runOnUiThread(() -> {
                    pendingRewardImport = false;
                    TextView status = findImportStatusText(progressDialog);
                    if (status != null) status.setText("اكتمل الاستيراد بنجاح
تمت القراءة: " + stats.scanned + "
تمت الإضافة: " + stats.added + "
المكرر/الموجود مسبقًا: " + stats.duplicates);
                    toneOk();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try { progressDialog.dismiss(); } catch (Exception ignored) {}
                        toast("تم الاستيراد: " + stats.added + " كرت" + (stats.duplicates > 0 ? " | مكرر: " + stats.duplicates : ""));
                        if (rewardMode) { rewardsPanelTab = "gift"; showRewardsSettings(); } else showHome();
                    }, 850);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pendingRewardImport = false;
                    try { progressDialog.dismiss(); } catch (Exception ignored) {}
                    toneAlert();
                    toast("فشل قراءة الملف: " + e.getMessage());
                });
            }
        });
    }

    private void importFromExcelUri(Uri uri) {
        final boolean rewardMode = pendingRewardImport;
        final int rewardAmount = pendingRewardImportAmount;
        final int amount = selectedAmount;
        final String sourceName = getFileName(uri);
        final AlertDialog progressDialog = showImportProgressDialog("استيراد Excel / CSV", sourceName);
        final ImportStats stats = new ImportStats();
        performanceExecutor.execute(() -> {
            ArrayList<String> batch = new ArrayList<>();
            String name = sourceName.toLowerCase(Locale.US);
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                if (name.endsWith(".xlsx")) {
                    ArrayList<String> cards = readXlsxCells(in);
                    for (String card : cards) {
                        if (card == null || card.trim().isEmpty()) continue;
                        batch.add(card);
                        stats.scanned++;
                        if (stats.scanned % 200 == 0) updateImportDialog(progressDialog, stats, "جاري قراءة خلايا Excel...");
                        if (batch.size() >= IMPORT_BATCH_SIZE) flushImportBatch(amount, batch, sourceName, stats, rewardMode, rewardAmount, progressDialog);
                    }
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int before = batch.size();
                        collectCardsFromTextLine(batch, line);
                        stats.scanned += Math.max(0, batch.size() - before);
                        if (stats.scanned % 200 == 0) updateImportDialog(progressDialog, stats, "جاري قراءة CSV...");
                        if (batch.size() >= IMPORT_BATCH_SIZE) flushImportBatch(amount, batch, sourceName, stats, rewardMode, rewardAmount, progressDialog);
                    }
                    reader.close();
                }
                flushImportBatch(amount, batch, sourceName, stats, rewardMode, rewardAmount, progressDialog);
                runOnUiThread(() -> {
                    pendingRewardImport = false;
                    TextView status = findImportStatusText(progressDialog);
                    if (status != null) status.setText("اكتمل الاستيراد بنجاح
تمت القراءة: " + stats.scanned + "
تمت الإضافة: " + stats.added + "
المكرر/الموجود مسبقًا: " + stats.duplicates);
                    toneOk();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try { progressDialog.dismiss(); } catch (Exception ignored) {}
                        toast("تم الاستيراد: " + stats.added + " كرت" + (stats.duplicates > 0 ? " | مكرر: " + stats.duplicates : ""));
                        if (rewardMode) { rewardsPanelTab = "gift"; showRewardsSettings(); } else showHome();
                    }, 850);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pendingRewardImport = false;
                    try { progressDialog.dismiss(); } catch (Exception ignored) {}
                    toneAlert();
                    toast("فشل قراءة ملف Excel/CSV: " + e.getMessage());
                });
            }
        });
    }

    private ArrayList<String> readXlsxCells(InputStream input) throws Exception {
        ArrayList<String> shared = new ArrayList<>();
        ArrayList<String> cells = new ArrayList<>();
        java.util.HashMap<String, String> entries = new java.util.HashMap<>();
        ZipInputStream zis = new ZipInputStream(input);
        ZipEntry entry;
        byte[] buf = new byte[4096];
        while ((entry = zis.getNextEntry()) != null) {
            String n = entry.getName();
            if (n.equals("xl/sharedStrings.xml") || n.startsWith("xl/worksheets/sheet")) {
                StringBuilder sb = new StringBuilder();
                int len;
                while ((len = zis.read(buf)) > 0) sb.append(new String(buf, 0, len, "UTF-8"));
                entries.put(n, sb.toString());
            }
            zis.closeEntry();
        }
        zis.close();
        String sharedXml = entries.get("xl/sharedStrings.xml");
        if (sharedXml != null) {
            int pos = 0;
            while (true) {
                int a = sharedXml.indexOf("<t", pos);
                if (a < 0) break;
                int b = sharedXml.indexOf('>', a);
                int c = sharedXml.indexOf("</t>", b);
                if (b < 0 || c < 0) break;
                shared.add(xmlDecode(sharedXml.substring(b + 1, c)));
                pos = c + 4;
            }
        }
        for (String key : entries.keySet()) {
            if (!key.startsWith("xl/worksheets/sheet")) continue;
            String xml = entries.get(key);
            int pos = 0;
            while (true) {
                int vStart = xml.indexOf("<v>", pos);
                if (vStart < 0) break;
                int vEnd = xml.indexOf("</v>", vStart);
                if (vEnd < 0) break;
                String raw = xml.substring(vStart + 3, vEnd).trim();
                int cellStart = xml.lastIndexOf("<c", vStart);
                int cellTagEnd = xml.indexOf('>', cellStart);
                String tag = (cellStart >= 0 && cellTagEnd > cellStart) ? xml.substring(cellStart, cellTagEnd) : "";
                String value = raw;
                if (tag.contains("t=\"s\"") || tag.contains("t='s'")) {
                    try {
                        int idx = Integer.parseInt(raw);
                        if (idx >= 0 && idx < shared.size()) value = shared.get(idx);
                    } catch(Exception ignored) {}
                }
                value = cleanExcelCell(value);
                if (!value.isEmpty()) cells.add(value);
                pos = vEnd + 4;
            }

            int inlinePos = 0;
            while (true) {
                int cellStart = xml.indexOf("<c", inlinePos);
                if (cellStart < 0) break;
                int cellTagEnd = xml.indexOf('>', cellStart);
                int cellEnd = xml.indexOf("</c>", cellTagEnd);
                if (cellTagEnd < 0 || cellEnd < 0) break;
                String tag = xml.substring(cellStart, cellTagEnd);
                if (tag.contains("t=\"inlineStr\"") || tag.contains("t='inlineStr'")) {
                    String cellXml = xml.substring(cellTagEnd + 1, cellEnd);
                    StringBuilder inlineValue = new StringBuilder();
                    int tPos = 0;
                    while (true) {
                        int tStart = cellXml.indexOf("<t", tPos);
                        if (tStart < 0) break;
                        int tOpenEnd = cellXml.indexOf('>', tStart);
                        int tEnd = cellXml.indexOf("</t>", tOpenEnd);
                        if (tOpenEnd < 0 || tEnd < 0) break;
                        inlineValue.append(xmlDecode(cellXml.substring(tOpenEnd + 1, tEnd)));
                        tPos = tEnd + 4;
                    }
                    String value = cleanExcelCell(inlineValue.toString());
                    if (!value.isEmpty()) cells.add(value);
                }
                inlinePos = cellEnd + 4;
            }
        }
        return cells;
    }

    private String cleanExcelCell(String s) {
        if (s == null) return "";
        s = xmlDecode(s).trim();
        if (s.endsWith(".0") && s.matches("[0-9]+\\.0")) s = s.substring(0, s.length() - 2);
        if (s.matches("[0-9]+(\\.[0-9]+)?[eE][+\\-]?[0-9]+")) {
            try { s = new java.math.BigDecimal(s).toPlainString(); } catch (Exception ignored) {}
            if (s.endsWith(".0") && s.matches("[0-9]+\\.0")) s = s.substring(0, s.length() - 2);
        }
        return AppStore.normalizeCardCode(s);
    }

    private String xmlDecode(String s) {
        if (s == null) return "";
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'");
    }



    private void showTrustedCreditAgents() {
        showTrustedCreditAgents("");
    }

    private void showTrustedCreditAgents(String filterText) {
        setTab("settings");
        clear();
        content.addView(title("الأرقام الموثوقة / نقاط البيع"));

        LinearLayout intro = cardBox();
        intro.addView(tv("إضافة رصيد عبر رقم موثوق", 17, text, true));
        intro.addView(small("الأرقام الموثوقة هي نقاط البيع أو أي شخص تثق فيه. يمكن تسجيل رقم أساسي ورقم إضافي لنفس نقطة البيع، لكن لا تُقبل الطلبات إلا بالبصمة المسجلة."));
        intro.addView(action("➕ إضافة رقم موثوق", purple, Color.WHITE, v -> showTrustedCreditAgentDialog(null)));
        content.addView(intro);

        LinearLayout searchBox = cardBox();
        searchBox.addView(tv("🔍 بحث واضح عن نقطة بيع", 18, text, true));
        searchBox.addView(small("ابحث بالاسم، الرقم الأساسي، الرقم الإضافي، أو البصمة."));
        EditText search = new EditText(this);
        search.setHint("مثال: الشرعبي أو 77xxxxxxx أو البصمة");
        search.setGravity(Gravity.RIGHT);
        search.setTextSize(18);
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        search.setText(filterText == null ? "" : filterText);
        searchBox.addView(search, new LinearLayout.LayoutParams(-1, dp(56)));
        LinearLayout searchRow = new LinearLayout(this); searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.addView(action("بحث", purple, Color.WHITE, v -> showTrustedCreditAgents(search.getText().toString().trim())), new LinearLayout.LayoutParams(0, -2, 1));
        searchRow.addView(action("عرض الكل", card2, text, v -> showTrustedCreditAgents("")), new LinearLayout.LayoutParams(0, -2, 1));
        searchBox.addView(searchRow);
        content.addView(searchBox);

        ArrayList<TrustedCreditAgent> list = AppStore.loadTrustedCreditAgents(this);
        String q = filterText == null ? "" : filterText.trim().toLowerCase(Locale.US);
        ArrayList<TrustedCreditAgent> shown = new ArrayList<>();
        for (TrustedCreditAgent item : list) {
            if (item == null) continue;
            String hay = ((item.name == null ? "" : item.name) + " "
                    + (item.senderPhone == null ? "" : item.senderPhone) + " "
                    + (item.secondaryPhone == null ? "" : item.secondaryPhone) + " "
                    + (item.posSignature == null ? "" : item.posSignature) + " "
                    + (item.active ? "مفعل active" : "موقوف inactive") + " "
                    + (item.signatureRequired ? "بصمة" : "بدون بصمة")).toLowerCase(Locale.US);
            if (q.isEmpty() || hay.contains(q)) shown.add(item);
        }
        if (list.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(small("لا توجد أرقام موثوقة بالسقف بعد. أضف رقم نقطة بيع مع سقف مثل 5000 ريال."));
            content.addView(empty);
        } else if (shown.isEmpty()) {
            LinearLayout empty = cardBox();
            empty.addView(small("لا توجد نتائج مطابقة للبحث الحالي."));
            content.addView(empty);
        }
        for (TrustedCreditAgent a : shown) {
            LinearLayout box = cardBox();
            int remain = AppStore.remainingTrustedCredit(a);
            box.addView(tv((a.active ? "✅ " : "⛔ ") + a.name, 18, text, true));
            box.addView(small("الرقم الأساسي: " + a.senderPhone
                    + "\nالرقم الإضافي: " + ((a.secondaryPhone == null || a.secondaryPhone.trim().isEmpty()) ? "غير مسجل" : a.secondaryPhone)
                    + "\nالسقف: " + a.creditLimit + " ريال"
                    + "\nالمستخدم: " + a.usedAmount + " ريال"
                    + "\nالمتبقي: " + remain + " ريال"
                    + "\nبصمة نقطة البيع: " + ((a.posSignature == null || a.posSignature.trim().isEmpty()) ? "غير مسجلة - لن تقبل الطلبات" : a.posSignature)
                    + "\nطلب البصمة: " + (a.signatureRequired ? "إجباري" : "غير مفعل")
                    + "\nالمكافآت: " + (a.rewardsEnabled ? "مفعلة لهذا الرقم" : "معطلة لهذا الرقم")));
            LinearLayout row1 = new LinearLayout(this); row1.setOrientation(LinearLayout.HORIZONTAL);
            row1.addView(action("تعديل", purple, Color.WHITE, v -> showTrustedCreditAgentDialog(a)), new LinearLayout.LayoutParams(0, -2, 1));
            row1.addView(action("تعبئة رصيد", Color.rgb(160, 120, 25), Color.WHITE, v -> showTrustedCreditTopUpDialog(a)), new LinearLayout.LayoutParams(0, -2, 1));
            row1.addView(action("تصفير السقف", Color.rgb(51, 90, 64), Color.WHITE, v -> confirmResetTrustedCreditAgent(a)), new LinearLayout.LayoutParams(0, -2, 1));
            box.addView(row1);
            box.addView(action("📅 تقرير نقطة البيع حسب التاريخ", card2, text, v -> showTrustedCreditAgentDateReportDialog(a)));
            box.addView(action("حذف الرقم", Color.rgb(82,30,42), Color.WHITE, v -> new AlertDialog.Builder(this)
                    .setTitle("حذف رقم موثوق")
                    .setMessage("هل تريد حذف " + a.name + "؟")
                    .setPositiveButton("حذف", (d,w) -> { AppStore.deleteTrustedCreditAgent(this, a.id); showTrustedCreditAgents(); })
                    .setNegativeButton("إلغاء", null)
                    .show()));
            content.addView(box);
        }
        LinearLayout back = cardBox();
        back.addView(action("رجوع للإعدادات", card2, text, v -> showSettings()));
        content.addView(back);
    }



    private String todayDate() {
        return new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date());
    }

    private String periodStartDaysAgo(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -Math.max(0, daysAgo));
        return new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(cal.getTime());
    }

    private String firstDayOfThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(cal.getTime());
    }

    private void runPerformanceTask(String waitMessage, Runnable task) {
        toast(waitMessage == null ? "جاري المعالجة في الخلفية..." : waitMessage);
        performanceExecutor.execute(() -> {
            try {
                task.run();
            } catch (Exception ex) {
                runOnUiThread(() -> toast("فشل التنفيذ: " + ex.getMessage()));
            }
        });
    }

    private void showCustomPeriodReportDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        layout.addView(small("اكتب الفترة بصيغة 2026/06/01 أو 2026-06-01. سيُنشأ تقرير PDF منظم قابل للمشاركة عبر واتساب."));
        EditText from = new EditText(this); from.setHint("من تاريخ"); from.setGravity(Gravity.RIGHT); from.setSingleLine(true); from.setInputType(InputType.TYPE_CLASS_TEXT);
        EditText to = new EditText(this); to.setHint("إلى تاريخ"); to.setGravity(Gravity.RIGHT); to.setSingleLine(true); to.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(from);
        layout.addView(to);
        new AlertDialog.Builder(this)
                .setTitle("تقرير فترة مخصصة")
                .setView(layout)
                .setPositiveButton("إنشاء ومشاركة PDF", (d,w) -> sharePeriodReportPdf("تقرير فترة مخصصة", from.getText().toString().trim(), to.getText().toString().trim()))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void shareSingleOperationPdf(OperationLog log) {
        if (log == null) return;
        runPerformanceTask("جاري إنشاء تقرير العملية في الخلفية...", () -> {
            String report = buildSingleOperationReport(log);
            File file = createTextPdfFile("تقرير عملية", report, "operation_report");
            if (file != null) runOnUiThread(() -> sharePdfFile(file, "تقرير عملية"));
        });
    }

    private String buildSingleOperationReport(OperationLog log) {
        if (log == null) return "لا توجد عملية محددة.";
        StringBuilder out = new StringBuilder();
        out.append("تقرير عملية\n");
        out.append("==============================\n");
        out.append("رقم العملية: ").append(safe(log.id)).append("\n");
        out.append("التاريخ والوقت: ").append(safe(log.createdAt)).append("\n");
        out.append("نوع العملية: ").append(safe(log.provider)).append("\n");
        out.append("طريقة الشراء: ").append(purchaseMethodForLog(log)).append("\n");
        out.append("الحالة: ").append(safe(log.status)).append("\n");
        out.append("المصدر: ").append(safe(log.sender)).append("\n");
        out.append("الاسم/نقطة البيع: ").append(safe(log.customerName)).append("\n");
        out.append("رقم العميل: ").append(safe(log.customerPhone)).append("\n");
        out.append("المبلغ: ").append(log.amount).append(" ريال\n");
        out.append("الكرت: ").append(displayCardCode(log.cardCode)).append("\n");
        out.append("------------------------------\n");
        out.append("ملاحظة النظام:\n").append(safe(log.message)).append("\n");
        out.append("==============================\n");
        out.append("تم إنشاء التقرير: ").append(AppStore.now()).append("\n");
        out.append(AppStore.getNetworkName(this)).append("\n");
        return out.toString();
    }

    private void shareTrustedAgentReportPdf(TrustedCreditAgent a, String from, String to) {
        if (a == null) return;
        runPerformanceTask("جاري إنشاء تقرير نقطة البيع في الخلفية...", () -> {
            String report = buildTrustedCreditAgentDateReport(a, from, to);
            File file = createTextPdfFile("تقرير نقطة البيع", report, "pos_report");
            if (file != null) runOnUiThread(() -> sharePdfFile(file, "تقرير نقطة البيع: " + a.name));
        });
    }

    private void sharePeriodReportPdf(String title, String from, String to) {
        runPerformanceTask("جاري إنشاء تقرير PDF في الخلفية...", () -> {
            String report = buildPeriodReport(title, from, to);
            File file = createTextPdfFile(title, report, "period_report");
            if (file != null) runOnUiThread(() -> sharePdfFile(file, title));
        });
    }

    private String buildPeriodReport(String title, String from, String to) {
        String f = normalizeReportDate(from);
        String t = normalizeReportDate(to);
        ArrayList<OperationLog> logs = AppStore.loadLogs(this);
        ArrayList<TrustedCreditAgent> agents = AppStore.loadTrustedCreditAgents(this);
        int count = 0, success = 0, pending = 0, rejected = 0, totalSales = 0, topups = 0, balanceRequests = 0;
        StringBuilder details = new StringBuilder();
        for (OperationLog log : logs) {
            if (!isLogInReportDateRange(log, f, t)) continue;
            count++;
            String st = log.status == null ? "" : log.status;
            String provider = log.provider == null ? "" : log.provider;
            boolean isTopup = provider.contains("تعبئة رصيد");
            boolean isBalanceReq = provider.contains("طلب رصيد");
            if (isTopup) topups += Math.max(0, log.amount);
            if (isBalanceReq) balanceRequests++;
            if (AppStore.isPendingCriticalLog(log) || st.contains("معلق") || st.contains("مراجعة")) pending++;
            else if (st.contains("رفض") || st.contains("مرفوض") || st.contains("تجاوز") || st.contains("فشل")) rejected++;
            else if (st.contains("تم") || st.contains("جاري إرسال")) { success++; if (!isTopup && !isBalanceReq) totalSales += Math.max(0, log.amount); }
            String note = "الاسم/النقطة: " + safe(log.customerName)
                    + "، الرقم/العميل: " + safe(log.customerPhone)
                    + "، طريقة الشراء: " + purchaseMethodForLog(log)
                    + "، الرسالة: " + shortText(log.message, 180);
            details.append(reportTableRow(
                    safe(log.createdAt),
                    log.amount + " ريال",
                    safe(st),
                    paymentMethodForReport(log),
                    "1",
                    note
            )).append("\n");
        }
        int remainingAll = 0;
        for (TrustedCreditAgent a : agents) remainingAll += AppStore.remainingTrustedCredit(a);
        StringBuilder out = new StringBuilder();
        out.append(title == null ? "تقرير" : title).append("\n");
        out.append("==============================\n");
        out.append("الفترة: ").append(f.isEmpty() ? "من البداية" : f).append(" إلى ").append(t.isEmpty() ? "آخر سجل" : t).append("\n");
        out.append("تاريخ إنشاء التقرير: ").append(AppStore.now()).append("\n");
        out.append("------------------------------\n");
        out.append("عدد العمليات: ").append(count).append("\n");
        out.append("عمليات ناجحة/قيد الإرسال: ").append(success).append("\n");
        out.append("عمليات تحتاج مراجعة: ").append(pending).append("\n");
        out.append("عمليات مرفوضة/فاشلة: ").append(rejected).append("\n");
        out.append("طلبات رصيد من نقاط البيع: ").append(balanceRequests).append("\n");
        out.append("إجمالي المبيعات: ").append(totalSales).append(" ريال\n");
        out.append("إجمالي تعبئة أرصدة نقاط البيع: ").append(topups).append(" ريال\n");
        out.append("إجمالي المتبقي الحالي لنقاط البيع: ").append(remainingAll).append(" ريال\n");
        out.append("==============================\n");
        out.append("ملخص نقاط البيع\n");
        out.append("------------------------------\n");
        if (agents.isEmpty()) out.append("لا توجد نقاط بيع مسجلة.\n");
        for (TrustedCreditAgent a : agents) {
            out.append("نقطة البيع: ").append(a.name).append("\n")
                    .append("الأساسي: ").append(a.senderPhone).append(" | الإضافي: ").append((a.secondaryPhone == null || a.secondaryPhone.trim().isEmpty()) ? "غير مسجل" : a.secondaryPhone).append("\n")
                    .append("السقف: ").append(a.creditLimit).append(" | المستخدم: ").append(a.usedAmount).append(" | المتبقي: ").append(AppStore.remainingTrustedCredit(a)).append("\n")
                    .append("------------------------------\n");
        }
        out.append("تفاصيل العمليات\n");
        out.append("==============================\n");
        out.append(REPORT_TABLE_HEADER).append("\n");
        out.append("------------------------------\n");
        if (count == 0) out.append("لا توجد عمليات ضمن الفترة المحددة.\n");
        else out.append(details);
        out.append(AppStore.getNetworkName(this)).append("\n");
        return out.toString();
    }

    private String shortText(String value, int max) {
        String v = value == null ? "" : value.replace('\r', ' ').trim();
        if (v.length() <= max) return v;
        return v.substring(0, Math.max(0, max)) + "...";
    }

    private String purchaseMethodForLog(OperationLog log) {
        if (log == null) return "غير محدد";
        String provider = log.provider == null ? "" : log.provider;
        String msg = log.message == null ? "" : log.message;
        String sender = log.sender == null ? "" : log.sender;
        if (provider.contains("تعبئة رصيد")) return "تعبئة رصيد نقطة بيع من الإدارة";
        if (provider.contains("طلب رصيد")) return "طلب رصيد من نقطة بيع ببصمة معتمدة";
        if (provider.contains("نقطة") || provider.contains("موثوق") || msg.contains("بصمة") || msg.contains("نقطة البيع")) return "شراء عبر نقطة بيع ببصمة معتمدة";
        if (provider.toLowerCase(Locale.US).contains("sms") || msg.toLowerCase(Locale.US).contains("sms")) return "شراء عبر رسالة SMS";
        if ("admin".equalsIgnoreCase(sender)) return "عملية إدارية داخل التطبيق";
        if (provider.contains("مباشر")) return "بيع مباشر من الإدارة";
        return "شراء من مخزون كروت " + AppStore.getNetworkName(this);
    }

    private static class PdfRenderState {
        PdfDocument pdf;
        PdfDocument.Page page;
        Canvas canvas;
        int pageNo;
        int y;
        final int w = 842;
        final int h = 595;
        final int margin = 36;
    }

    private static class PdfReportData {
        ArrayList<String[]> info = new ArrayList<>();
        ArrayList<String[]> summary = new ArrayList<>();
        ArrayList<String[]> rows = new ArrayList<>();
        ArrayList<String> notes = new ArrayList<>();
    }

    private File createTextPdfFile(String title, String report, String prefix) {
        PdfRenderState st = new PdfRenderState();
        st.pdf = new PdfDocument();
        st.pageNo = 1;
        try {
            startStyledPdfPage(st, title);
            drawHtmlStyleProfessionalReport(st, title, report == null ? "" : report);
            finishStyledPdfPage(st);

            File dir = new File(getCacheDir(), "reports_pdf");
            if (!dir.exists()) dir.mkdirs();
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String safePrefix = prefix == null || prefix.trim().isEmpty() ? "fouru_report" : prefix.trim().replaceAll("[^A-Za-z0-9_\\-]", "_");
            File file = new File(dir, safePrefix + "_professional_" + stamp + ".pdf");
            OutputStream out = new java.io.FileOutputStream(file);
            st.pdf.writeTo(out);
            out.close();
            runOnUiThread(() -> toast("تم إنشاء تقرير PDF احترافي"));
            return file;
        } catch (Exception e) {
            runOnUiThread(() -> toast("فشل إنشاء PDF: " + e.getMessage()));
            try { if (st.page != null) st.pdf.finishPage(st.page); } catch (Exception ignored) {}
            return null;
        } finally {
            try { st.pdf.close(); } catch (Exception ignored) {}
        }
    }

    private void startStyledPdfPage(PdfRenderState st, String title) {
        st.page = st.pdf.startPage(new PdfDocument.PageInfo.Builder(st.w, st.h, st.pageNo).create());
        st.canvas = st.page.getCanvas();
        st.y = 36;
        drawPdfHeader(st, title);
    }

    private void finishStyledPdfPage(PdfRenderState st) {
        drawPdfFooter(st);
        st.pdf.finishPage(st.page);
        st.page = null;
    }

    private void ensurePdfSpace(PdfRenderState st, int needed, String title) {
        if (st.y + needed <= st.h - 56) return;
        finishStyledPdfPage(st);
        st.pageNo++;
        startStyledPdfPage(st, title);
    }

    private Paint pdfPaint(int color, float size, boolean bold, Paint.Align align) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(size);
        p.setTextAlign(align);
        p.setTypeface(Typeface.create(Typeface.SANS_SERIF, bold ? Typeface.BOLD : Typeface.NORMAL));
        return p;
    }

    private void drawPdfHeader(PdfRenderState st, String title) {
        Canvas c = st.canvas;
        Paint p = pdfPaint(Color.WHITE, 11, false, Paint.Align.RIGHT);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.rgb(80, 55, 132));
        c.drawRoundRect(new RectF(st.margin, st.y, st.w - st.margin, st.y + 82), 18, 18, bgPaint);

        boolean logoDrawn = false;
        String logoPath = AppStore.getNetworkLogoPath(this);
        if (logoPath != null && !logoPath.trim().isEmpty()) {
            try {
                Bitmap bmp = BitmapFactory.decodeFile(logoPath.trim());
                if (bmp != null) {
                    RectF logoRect = new RectF(st.w - st.margin - 63, st.y + 16, st.w - st.margin - 13, st.y + 66);
                    c.drawBitmap(bmp, null, logoRect, new Paint(Paint.ANTI_ALIAS_FLAG));
                    logoDrawn = true;
                }
            } catch (Exception ignored) {}
        }
        if (!logoDrawn) {
            Paint goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            goldPaint.setColor(Color.rgb(218, 170, 72));
            c.drawCircle(st.w - st.margin - 38, st.y + 41, 25, goldPaint);
            Paint logoText = pdfPaint(Color.WHITE, 19, true, Paint.Align.CENTER);
            c.drawText("4U", st.w - st.margin - 38, st.y + 48, logoText);
        }

        Paint titlePaint = pdfPaint(Color.WHITE, 20, true, Paint.Align.RIGHT);
        c.drawText(cleanPdfText(AppStore.getNetworkName(this)), st.w - st.margin - 76, st.y + 31, titlePaint);
        Paint subPaint = pdfPaint(Color.rgb(238, 230, 255), 10, false, Paint.Align.RIGHT);
        c.drawText(cleanPdfText(AppStore.getPdfHeaderSubtitle(this)), st.w - st.margin - 76, st.y + 50, subPaint);
        c.drawText(title == null || title.trim().isEmpty() ? "تقرير" : cleanPdfText(title), st.w - st.margin - 76, st.y + 68, subPaint);

        Paint datePaint = pdfPaint(Color.WHITE, 9, false, Paint.Align.LEFT);
        c.drawText("تاريخ الإنشاء: " + AppStore.now(), st.margin + 14, st.y + 32, datePaint);
        c.drawText("رقم التقرير: " + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()), st.margin + 14, st.y + 52, datePaint);
        st.y += 104;
    }

    private void drawPdfFooter(PdfRenderState st) {
        Canvas c = st.canvas;
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.rgb(221, 216, 232));
        linePaint.setStrokeWidth(1f);
        c.drawLine(st.margin, st.h - 42, st.w - st.margin, st.h - 42, linePaint);
        Paint p = pdfPaint(Color.rgb(90, 88, 96), 9, false, Paint.Align.RIGHT);
        c.drawText(cleanPdfText(AppStore.getNetworkName(this)) + " - تقرير صادر من النظام", st.w - st.margin, st.h - 24, p);
        Paint pagePaint = pdfPaint(Color.rgb(90, 88, 96), 9, false, Paint.Align.LEFT);
        c.drawText("صفحة " + st.pageNo, st.margin, st.h - 24, pagePaint);
    }


    private String cleanPdfText(String value) {
        if (value == null) return "-";
        String v = value.replace("📊", "").replace("🕒", "").replace("📂", "")
                .replace("🚦", "").replace("💳", "").replace("🔢", "").replace("📝", "")
                .replace("✔", "تم").replace("✖", "رفض").replace("⏳", "معلق").replace("🔄", "قيد")
                .replace("🎫", "").replace("💰", "").replace("✅", "").replace("👋", "")
                .replace("🏦", "").replace("💵", "").replace("📱", "").replace("🟢", "")
                .replace("🟡", "").replace("🔴", "").replace("🔵", "").replace("🏆", "")
                .replace("📌", "").replace("🛍️", "").replace("🛒", "").replace("🧾", "")
                .replace("👨‍💼", "").replace("🚫", "");
        v = v.replace("\uFE0F", "").replaceAll("[\\uD800-\\uDFFF]", "");
        return v.trim().isEmpty() ? "-" : v.trim();
    }

    private String[] normalizedReportCells(String line) {
        String[] raw = line == null ? new String[0] : line.split("\\|", -1);
        String[] cells = new String[]{"", "", "", "", "", ""};
        for (int i = 0; i < Math.min(6, raw.length); i++) cells[i] = cleanPdfText(raw[i]);
        if (raw.length > 6) {
            StringBuilder note = new StringBuilder(cells[5]);
            for (int i = 6; i < raw.length; i++) note.append(" | ").append(cleanPdfText(raw[i]));
            cells[5] = note.toString();
        }
        return cells;
    }

    private PdfReportData parseProfessionalReportData(String report) {
        PdfReportData data = new PdfReportData();
        String[] lines = (report == null ? "" : report.replace("\r", "")).split("\n");
        int section = 0; // 0 info, 1 summary, 2 table, 3 notes
        for (String raw : lines) {
            String line = cleanPdfText(raw == null ? "" : raw.trim());
            if (line.isEmpty() || "-".equals(line) || isPdfDividerLine(line)) continue;
            if (line.equals("شبكة فور يو") || line.equals("شبكة لان فور يو") || line.equals("فور يو") || line.equals("لان فور يو")) continue;
            if (line.contains("تفاصيل العمليات")) { section = 2; continue; }
            if (line.contains("ملخص") || line.equals("الإجمالي") || line.equals("الخلاصة")) { section = 1; continue; }
            if (line.contains("بيانات العميل") || line.contains("بيانات نقطة البيع")) { section = 0; continue; }
            if (isReportTableLine(line)) {
                String[] cells = normalizedReportCells(line);
                boolean header = cells[0].contains("الوقت") || cells[1].contains("الفئة") || cells[2].contains("الحالة");
                if (!header) data.rows.add(cells);
                section = 2;
                continue;
            }
            int sep = line.indexOf(':');
            if (sep > 0 && sep < 40) {
                String key = cleanPdfText(line.substring(0, sep));
                String val = cleanPdfText(line.substring(sep + 1));
                if (section == 1 || isSummaryKey(key)) data.summary.add(new String[]{key, val});
                else if (section == 2) data.notes.add(key + ": " + val);
                else data.info.add(new String[]{key, val});
            } else {
                if (!line.contains("لا توجد عمليات")) data.notes.add(line);
            }
        }
        if (data.summary.isEmpty()) data.summary.add(new String[]{"عدد العمليات", String.valueOf(data.rows.size())});
        return data;
    }

    private void drawHtmlStyleProfessionalReport(PdfRenderState st, String title, String report) {
        PdfReportData data = parseProfessionalReportData(report);
        drawHtmlStyleMetaCard(st, title);
        drawHtmlLegend(st);
        if (!data.info.isEmpty()) drawHtmlKeyValueGrid(st, "بيانات التقرير", data.info);
        drawHtmlSummaryBox(st, data.summary, data.rows.size());
        drawPdfSectionTitle(st, "تفاصيل العمليات");
        if (data.rows.isEmpty()) {
            drawEmptyOperationsBox(st);
        } else {
            drawHtmlReportTableHeader(st);
            for (String[] row : data.rows) drawHtmlReportTableRow(st, row, false);
        }
        if (!data.notes.isEmpty()) {
            drawPdfSectionTitle(st, "ملاحظات");
            int max = Math.min(6, data.notes.size());
            for (int i = 0; i < max; i++) drawPdfParagraph(st, data.notes.get(i), "ملاحظات");
        }
    }

    private void drawHtmlStyleMetaCard(PdfRenderState st, String title) {
        ensurePdfSpace(st, 92, title);
        int x = st.margin;
        int w = st.w - st.margin * 2;
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(238, 242, 247));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + 72), 14, 14, fill);
        Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
        accent.setColor(Color.rgb(52, 152, 219));
        st.canvas.drawRoundRect(new RectF(x + w - 6, st.y, x + w, st.y + 72), 8, 8, accent);
        Paint label = pdfPaint(Color.rgb(30, 42, 74), 15, true, Paint.Align.RIGHT);
        Paint val = pdfPaint(Color.rgb(45, 53, 68), 10, false, Paint.Align.RIGHT);
        st.canvas.drawText(cleanPdfText(title == null || title.trim().isEmpty() ? "تقرير المعاملات" : title), x + w - 18, st.y + 25, label);
        st.canvas.drawText(cleanPdfText(AppStore.getNetworkName(this)) + " - تقرير منظم بالأعمدة المعتمدة", x + w - 18, st.y + 44, val);
        st.canvas.drawText("التاريخ: " + AppStore.now(), x + w - 18, st.y + 61, val);
        st.y += 88;
    }

    private void drawHtmlLegend(PdfRenderState st) {
        ensurePdfSpace(st, 50, "دليل التقرير");
        int x = st.margin;
        int w = st.w - st.margin * 2;
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(238, 242, 247));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + 36), 10, 10, fill);
        drawLegendItem(st, x + w - 18, "مكتمل", Color.rgb(40, 167, 69));
        drawLegendItem(st, x + w - 110, "معلق", Color.rgb(255, 193, 7));
        drawLegendItem(st, x + w - 200, "مرفوض", Color.rgb(220, 53, 69));
        drawLegendItem(st, x + w - 300, "قيد التنفيذ", Color.rgb(0, 123, 255));
        Paint p = pdfPaint(Color.rgb(45, 53, 68), 9.5f, false, Paint.Align.LEFT);
        st.canvas.drawText("طرق السداد: ون كاش | جوالي | جيب | كريمي | فلوسك | عن طريق المحل | كرت سلف | رصيد العميل", x + 14, st.y + 23, p);
        st.y += 50;
    }

    private void drawLegendItem(PdfRenderState st, int right, String label, int colorValue) {
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setColor(colorValue);
        st.canvas.drawCircle(right - 7, st.y + 18, 5, dot);
        Paint p = pdfPaint(Color.rgb(45, 53, 68), 9.5f, false, Paint.Align.RIGHT);
        st.canvas.drawText(label, right - 18, st.y + 22, p);
    }

    private void drawHtmlKeyValueGrid(PdfRenderState st, String title, ArrayList<String[]> pairs) {
        if (pairs == null || pairs.isEmpty()) return;
        drawPdfSectionTitle(st, title);
        int x = st.margin;
        int gap = 8;
        int cols = 3;
        int totalW = st.w - st.margin * 2;
        int cellW = (totalW - gap * (cols - 1)) / cols;
        int cellH = 42;
        for (int i = 0; i < pairs.size(); i++) {
            if (i % cols == 0) ensurePdfSpace(st, cellH + 8, title);
            int col = i % cols;
            int left = x + col * (cellW + gap);
            int top = st.y;
            Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
            fill.setColor(Color.WHITE);
            st.canvas.drawRoundRect(new RectF(left, top, left + cellW, top + cellH), 8, 8, fill);
            Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
            border.setStyle(Paint.Style.STROKE);
            border.setStrokeWidth(0.8f);
            border.setColor(Color.rgb(221, 225, 232));
            st.canvas.drawRoundRect(new RectF(left, top, left + cellW, top + cellH), 8, 8, border);
            String key = pairs.get(i)[0];
            String val = pairs.get(i)[1];
            drawPdfTextBlock(st.canvas, key, left + 8, top + 8, cellW - 16, 14, Color.rgb(90, 88, 110), 8.5f, true, Layout.Alignment.ALIGN_OPPOSITE);
            drawPdfTextBlock(st.canvas, val, left + 8, top + 23, cellW - 16, 16, Color.rgb(30, 36, 48), 9.5f, false, Layout.Alignment.ALIGN_OPPOSITE);
            if (col == cols - 1 || i == pairs.size() - 1) st.y += cellH + 8;
        }
    }

    private void drawHtmlSummaryBox(PdfRenderState st, ArrayList<String[]> summary, int rowCount) {
        ensurePdfSpace(st, 72, "الخلاصة");
        int x = st.margin;
        int w = st.w - st.margin * 2;
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(232, 240, 254));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + 56), 10, 10, fill);
        Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
        accent.setColor(Color.rgb(52, 152, 219));
        st.canvas.drawRoundRect(new RectF(x + w - 6, st.y, x + w, st.y + 56), 7, 7, accent);
        Paint titlePaint = pdfPaint(Color.rgb(30, 42, 74), 11, true, Paint.Align.RIGHT);
        st.canvas.drawText("الخلاصة", x + w - 18, st.y + 20, titlePaint);
        StringBuilder line = new StringBuilder();
        line.append("إجمالي العمليات: ").append(rowCount);
        if (summary != null) {
            int added = 0;
            for (String[] p : summary) {
                if (p == null || p.length < 2) continue;
                String k = cleanPdfText(p[0]);
                String v = cleanPdfText(p[1]);
                if (k.contains("عدد العمليات")) continue;
                if (added < 5) {
                    line.append(" | ").append(k).append(": ").append(v);
                    added++;
                }
            }
        }
        drawPdfTextBlock(st.canvas, line.toString(), x + 16, st.y + 30, w - 34, 20, Color.rgb(45, 53, 68), 9.5f, false, Layout.Alignment.ALIGN_OPPOSITE);
        st.y += 72;
    }

    private void drawEmptyOperationsBox(PdfRenderState st) {
        ensurePdfSpace(st, 50, "تفاصيل العمليات");
        int x = st.margin;
        int w = st.w - st.margin * 2;
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(253, 252, 255));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + 36), 8, 8, fill);
        drawPdfTextBlock(st.canvas, "لا توجد عمليات لهذا التقرير حتى الآن.", x + 12, st.y + 11, w - 24, 16, Color.rgb(90, 88, 96), 10, false, Layout.Alignment.ALIGN_CENTER);
        st.y += 48;
    }

    private void drawHtmlReportTableHeader(PdfRenderState st) {
        drawHtmlReportTableRow(st, new String[]{"الوقت والتاريخ", "الفئة", "الحالة", "طريقة السداد", "العدد", "ملاحظة"}, true);
    }

    private int htmlStatusBg(String status) {
        String v = status == null ? "" : status;
        if (v.contains("رفض") || v.contains("فشل") || v.contains("ملغي") || v.contains("مرفوض")) return Color.rgb(248, 215, 218);
        if (v.contains("معلق") || v.contains("مراجعة") || v.contains("نفدت")) return Color.rgb(255, 243, 205);
        if (v.contains("قيد") || v.contains("جاري")) return Color.rgb(204, 229, 255);
        return Color.rgb(212, 237, 218);
    }

    private int htmlStatusText(String status) {
        String v = status == null ? "" : status;
        if (v.contains("رفض") || v.contains("فشل") || v.contains("ملغي") || v.contains("مرفوض")) return Color.rgb(114, 28, 36);
        if (v.contains("معلق") || v.contains("مراجعة") || v.contains("نفدت")) return Color.rgb(133, 100, 4);
        if (v.contains("قيد") || v.contains("جاري")) return Color.rgb(0, 64, 133);
        return Color.rgb(21, 87, 36);
    }

    private void drawHtmlReportTableRow(PdfRenderState st, String[] cells, boolean header) {
        if (cells == null) cells = new String[]{"", "", "", "", "", ""};
        while (cells.length < 6) cells = new String[]{safe(cells.length>0?cells[0]:""), safe(cells.length>1?cells[1]:""), safe(cells.length>2?cells[2]:""), safe(cells.length>3?cells[3]:""), safe(cells.length>4?cells[4]:""), ""};
        int[] widths = new int[]{125, 70, 90, 110, 45, 330};
        int[] chars = new int[]{19, 10, 13, 16, 5, 58};
        ArrayList<ArrayList<String>> wrapped = new ArrayList<>();
        int maxLines = 1;
        for (int i = 0; i < 6; i++) {
            ArrayList<String> w = wrapPdfLine(cleanPdfText(cells[i]), chars[i]);
            if (!header && i == 5 && w.size() > 3) {
                ArrayList<String> cut = new ArrayList<>();
                cut.add(w.get(0));
                cut.add(w.get(1));
                cut.add(w.get(2) + "...");
                w = cut;
            }
            wrapped.add(w);
            maxLines = Math.max(maxLines, w.size());
        }
        int rowH = header ? 34 : Math.max(38, 18 + maxLines * 13);
        if (st.y + rowH + 4 > st.h - 56) {
            finishStyledPdfPage(st);
            st.pageNo++;
            startStyledPdfPage(st, "تفاصيل العمليات");
            if (!header) drawHtmlReportTableHeader(st);
        }
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(header ? Color.rgb(30, 42, 74) : Color.WHITE);
        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(0.8f);
        border.setColor(Color.rgb(221, 225, 232));
        int right = st.w - st.margin;
        for (int i = 0; i < 6; i++) {
            int left = right - widths[i];
            RectF rect = new RectF(left, st.y, right, st.y + rowH);
            st.canvas.drawRect(rect, fill);
            st.canvas.drawRect(rect, border);
            if (!header && i == 2) {
                Paint pill = new Paint(Paint.ANTI_ALIAS_FLAG);
                pill.setColor(htmlStatusBg(cells[i]));
                float pillLeft = left + 8;
                float pillRight = right - 8;
                float pillTop = st.y + 8;
                float pillBottom = Math.min(st.y + rowH - 8, pillTop + 22);
                st.canvas.drawRoundRect(new RectF(pillLeft, pillTop, pillRight, pillBottom), 12, 12, pill);
                drawPdfTextBlock(st.canvas, cleanPdfText(cells[i]), pillLeft + 4, pillTop + 5, pillRight - pillLeft - 8, 13,
                        htmlStatusText(cells[i]), 8.2f, true, Layout.Alignment.ALIGN_CENTER);
            } else {
                StringBuilder cellText = new StringBuilder();
                for (String txt : wrapped.get(i)) {
                    if (cellText.length() > 0) cellText.append("\n");
                    cellText.append(txt == null || txt.trim().isEmpty() ? "-" : txt);
                }
                drawPdfTextBlock(st.canvas, cellText.toString(), left + 4, st.y + (header ? 9 : 8), Math.max(8, widths[i] - 8), rowH - 8,
                        header ? Color.WHITE : Color.rgb(40, 38, 48), header ? 9.0f : 8.2f, header, Layout.Alignment.ALIGN_CENTER);
            }
            right = left;
        }
        st.y += rowH + 3;
    }

    private void drawReportMetaCard(PdfRenderState st, String title, String report) {
        ensurePdfSpace(st, 84, title);
        int x = st.margin;
        int w = st.w - (st.margin * 2);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(249, 247, 253));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + 70), 14, 14, fill);
        Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
        accent.setColor(Color.rgb(218, 170, 72));
        st.canvas.drawRoundRect(new RectF(x + w - 7, st.y, x + w, st.y + 70), 8, 8, accent);
        Paint label = pdfPaint(Color.rgb(80, 55, 132), 12, true, Paint.Align.RIGHT);
        Paint val = pdfPaint(Color.rgb(35, 35, 42), 10, false, Paint.Align.RIGHT);
        st.canvas.drawText("ملخص التقرير", x + w - 18, st.y + 24, label);
        st.canvas.drawText("نوع التقرير: " + (title == null ? "تقرير" : title), x + w - 18, st.y + 43, val);
        st.canvas.drawText("طريقة العرض: PDF منظم ببطاقات وجداول بدون فواصل نصية", x + w - 18, st.y + 59, val);
        st.y += 88;
    }

    private void drawParsedProfessionalReport(PdfRenderState st, String report) {
        String currentTitle = "تقرير";
        String[] lines = report.replace("\r", "").split("\n");
        boolean inDetails = false;
        for (String rawLine : lines) {
            String line = rawLine == null ? "" : rawLine.trim();
            if (line.isEmpty()) { st.y += 4; continue; }
            if (isPdfDividerLine(line)) { st.y += 6; continue; }
            if (line.equals("شبكة فور يو") || line.equals("شبكة لان فور يو") || line.equals("فور يو") || line.equals("لان فور يو")) continue;
            if (isReportTableLine(line)) {
                boolean headerLine = line.contains("الوقت والتاريخ") || line.contains("طريقة السداد") || line.contains("طريقة الدفع");
                if (headerLine && st.y + 95 > st.h - 56) {
                    finishStyledPdfPage(st);
                    st.pageNo++;
                    startStyledPdfPage(st, "تفاصيل العمليات");
                }
                drawPdfReportTableRow(st, line);
                inDetails = true;
                continue;
            }

            if (line.equals("الملخص") || line.equals("ملخص الفئات") || line.equals("ملخص الفئة") || line.equals("ملخص نقاط البيع") || line.equals("تفاصيل العمليات") || line.equals("بيانات نقطة البيع") || line.equals("طريقة الشراء") || line.contains("طلبات الرصيد")) {
                currentTitle = line;
                inDetails = line.equals("تفاصيل العمليات");
                if (inDetails) ensurePdfSpace(st, 150, line);
                drawPdfSectionTitle(st, line);
                continue;
            }

            if (line.matches("^[0-9]+\\).*")) {
                drawPdfOperationHeader(st, line);
                inDetails = true;
                continue;
            }

            int sep = line.indexOf(':');
            if (sep > 0 && sep < 34) {
                String key = line.substring(0, sep).trim();
                String value = line.substring(sep + 1).trim();
                if (isSummaryKey(key) && !inDetails) drawPdfSummaryChip(st, key, value);
                else drawPdfInfoRow(st, key, value, key.contains("الحالة") ? colorForStatus(value) : Color.rgb(45, 43, 52));
            } else {
                drawPdfParagraph(st, line, currentTitle);
            }
        }
        drawPdfSectionTitle(st, "التوقيع");
        drawPdfInfoRow(st, "توقيع النظام", AppStore.getNetworkName(this), Color.rgb(80, 55, 132));
    }

    private boolean isPdfDividerLine(String line) {
        if (line == null) return false;
        String v = line.trim();
        if (v.length() < 3) return false;
        for (int i = 0; i < v.length(); i++) {
            char ch = v.charAt(i);
            if (ch != '-' && ch != '=' && ch != '_' && ch != '—') return false;
        }
        return true;
    }

    private boolean isSummaryKey(String key) {
        if (key == null) return false;
        return key.contains("عدد") || key.contains("إجمالي") || key.contains("عمليات") || key.contains("المتبقي") || key.contains("الفترة") || key.contains("تاريخ إنشاء");
    }

    private int colorForStatus(String value) {
        String v = value == null ? "" : value;
        if (v.contains("رفض") || v.contains("فشل") || v.contains("مرفوض")) return Color.rgb(169, 42, 66);
        if (v.contains("مراجعة") || v.contains("معلق") || v.contains("قيد")) return Color.rgb(188, 128, 22);
        if (v.contains("تم") || v.contains("نجاح") || v.contains("إرسال")) return Color.rgb(38, 124, 82);
        return Color.rgb(45, 43, 52);
    }

    private boolean isReportTableLine(String line) {
        if (line == null || !line.contains("|")) return false;
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) return false;
        return line.contains("الوقت والتاريخ") || line.contains("طريقة السداد") || line.contains("طريقة الدفع") || parts.length >= 6;
    }

    private void drawPdfTextBlock(Canvas canvas, String textValue, float left, float top, float width, float height, int colorValue, float size, boolean bold, Layout.Alignment alignment) {
        if (canvas == null) return;
        TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tp.setColor(colorValue);
        tp.setTextSize(size);
        tp.setTypeface(Typeface.create(Typeface.SANS_SERIF, bold ? Typeface.BOLD : Typeface.NORMAL));
        String value = cleanPdfText(textValue == null || textValue.trim().isEmpty() ? "-" : textValue.trim());
        int save = canvas.save();
        canvas.translate(left, top);
        try {
            StaticLayout layout = StaticLayout.Builder.obtain(value, 0, value.length(), tp, Math.max(1, (int) width))
                    .setAlignment(alignment == null ? Layout.Alignment.ALIGN_OPPOSITE : alignment)
                    .setIncludePad(false)
                    .setLineSpacing(0, 1.0f)
                    .build();
            layout.draw(canvas);
        } catch (Exception e) {
            Paint fallback = pdfPaint(colorValue, size, bold, Paint.Align.RIGHT);
            canvas.drawText(value, width, Math.min(height, size + 2), fallback);
        } finally {
            canvas.restoreToCount(save);
        }
    }

    private void drawPdfReportTableRow(PdfRenderState st, String line) {
        String[] raw = line.split("\\|", -1);
        ArrayList<String> cells = new ArrayList<>();
        for (String part : raw) cells.add(part == null ? "" : part.trim());
        while (cells.size() < 6) cells.add("");
        if (cells.size() > 6) {
            StringBuilder note = new StringBuilder(cells.get(5));
            for (int i = 6; i < cells.size(); i++) note.append(" | ").append(cells.get(i));
            while (cells.size() > 6) cells.remove(cells.size() - 1);
            cells.set(5, note.toString());
        }
        boolean header = cells.get(0).contains("الوقت") || cells.get(1).contains("الفئة") || cells.get(2).contains("الحالة");
        int[] widths = new int[]{95, 52, 66, 78, 38, 194};
        int[] chars = new int[]{18, 8, 12, 14, 5, 38};
        ArrayList<ArrayList<String>> wrapped = new ArrayList<>();
        int maxLines = 1;
        for (int i = 0; i < 6; i++) {
            ArrayList<String> w = wrapPdfLine(cells.get(i), chars[i]);
            if (!header && w.size() > 3) {
                ArrayList<String> cut = new ArrayList<>();
                cut.add(w.get(0));
                cut.add(w.get(1));
                cut.add(w.get(2) + "...");
                w = cut;
            }
            wrapped.add(w);
            maxLines = Math.max(maxLines, w.size());
        }
        int rowH = header ? 30 : Math.max(38, 20 + maxLines * 15);
        if (st.y + rowH + 4 > st.h - 56) {
            finishStyledPdfPage(st);
            st.pageNo++;
            startStyledPdfPage(st, "تفاصيل العمليات");
            if (!header) {
                drawPdfReportTableRow(st, REPORT_TABLE_HEADER);
            }
        }
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(header ? Color.rgb(80, 55, 132) : Color.rgb(253, 252, 255));
        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(0.8f);
        border.setColor(Color.rgb(222, 216, 234));
        Paint textPaint = pdfPaint(header ? Color.WHITE : Color.rgb(40, 38, 48), header ? 9.0f : 8.6f, header, Paint.Align.RIGHT);
        int right = st.w - st.margin;
        for (int i = 0; i < 6; i++) {
            int left = right - widths[i];
            RectF rect = new RectF(left, st.y, right, st.y + rowH);
            st.canvas.drawRect(rect, fill);
            st.canvas.drawRect(rect, border);
            StringBuilder cellText = new StringBuilder();
            for (String txt : wrapped.get(i)) {
                if (cellText.length() > 0) cellText.append("\n");
                cellText.append(txt == null || txt.isEmpty() ? "-" : txt);
            }
            drawPdfTextBlock(st.canvas, cellText.toString(), left + 4, st.y + (header ? 7 : 8), Math.max(8, widths[i] - 8), rowH - 8,
                    header ? Color.WHITE : Color.rgb(40, 38, 48), header ? 9.0f : 8.6f, header, Layout.Alignment.ALIGN_OPPOSITE);
            right = left;
        }
        st.y += rowH + 3;
    }

    private void drawPdfSectionTitle(PdfRenderState st, String title) {
        ensurePdfSpace(st, 44, title);
        Paint p = pdfPaint(Color.rgb(80, 55, 132), 13, true, Paint.Align.RIGHT);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.rgb(218, 170, 72));
        line.setStrokeWidth(3f);
        st.canvas.drawLine(st.margin, st.y + 10, st.w - st.margin - 150, st.y + 10, line);
        st.canvas.drawText(title == null ? "قسم" : title, st.w - st.margin, st.y + 15, p);
        st.y += 34;
    }

    private void drawPdfSummaryChip(PdfRenderState st, String key, String value) {
        ensurePdfSpace(st, 44, key);
        int x = st.margin;
        int w = st.w - st.margin * 2;
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(245, 242, 251));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + 34), 12, 12, fill);
        drawPdfTextBlock(st.canvas, key, x + w - 185, st.y + 10, 170, 20, Color.rgb(80, 55, 132), 10, true, Layout.Alignment.ALIGN_OPPOSITE);
        drawPdfTextBlock(st.canvas, value == null || value.isEmpty() ? "-" : value, x + 16, st.y + 10, w - 215, 20, Color.rgb(35, 35, 42), 10, false, Layout.Alignment.ALIGN_OPPOSITE);
        st.y += 40;
    }

    private void drawPdfOperationHeader(PdfRenderState st, String title) {
        ensurePdfSpace(st, 54, title);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(80, 55, 132));
        st.canvas.drawRoundRect(new RectF(st.margin, st.y, st.w - st.margin, st.y + 34), 12, 12, fill);
        Paint p = pdfPaint(Color.WHITE, 11, true, Paint.Align.RIGHT);
        st.canvas.drawText("عملية " + title, st.w - st.margin - 14, st.y + 22, p);
        st.y += 44;
    }

    private void drawPdfInfoRow(PdfRenderState st, String key, String value, int valueColor) {
        ArrayList<String> valueLines = wrapPdfLine(value == null || value.isEmpty() ? "-" : value, 54);
        int rowH = Math.max(30, 18 + valueLines.size() * 15);
        ensurePdfSpace(st, rowH + 6, key);
        int x = st.margin;
        int w = st.w - st.margin * 2;
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.rgb(253, 252, 255));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + rowH), 8, 8, fill);
        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(0.8f);
        border.setColor(Color.rgb(229, 225, 238));
        st.canvas.drawRoundRect(new RectF(x, st.y, x + w, st.y + rowH), 8, 8, border);
        drawPdfTextBlock(st.canvas, key == null ? "" : key, x + w - 140, st.y + 10, 128, rowH - 12, Color.rgb(88, 75, 112), 9.5f, true, Layout.Alignment.ALIGN_OPPOSITE);
        StringBuilder valueText = new StringBuilder();
        for (String line : valueLines) {
            if (valueText.length() > 0) valueText.append("\n");
            valueText.append(line);
        }
        drawPdfTextBlock(st.canvas, valueText.toString(), x + 12, st.y + 10, w - 165, rowH - 12, valueColor, 9.5f, false, Layout.Alignment.ALIGN_OPPOSITE);
        st.y += rowH + 6;
    }

    private void drawPdfParagraph(PdfRenderState st, String line, String title) {
        ArrayList<String> lines = wrapPdfLine(line, 74);
        int needed = Math.max(24, lines.size() * 15 + 12);
        ensurePdfSpace(st, needed, title);
        Paint p = pdfPaint(Color.rgb(55, 53, 65), 9.5f, false, Paint.Align.RIGHT);
        for (String v : lines) {
            st.canvas.drawText(v, st.w - st.margin, st.y, p);
            st.y += 15;
        }
        st.y += 4;
    }

    private ArrayList<String> wrapPdfLine(String line, int maxChars) {
        ArrayList<String> out = new ArrayList<>();
        if (line == null) { out.add(""); return out; }
        String s = line.trim();
        if (s.length() <= maxChars) { out.add(s); return out; }
        while (s.length() > maxChars) {
            int cut = s.lastIndexOf(' ', maxChars);
            if (cut < 20) cut = maxChars;
            out.add(s.substring(0, cut).trim());
            s = s.substring(cut).trim();
        }
        if (!s.isEmpty()) out.add(s);
        return out;
    }

    private void sharePdfFile(File file, String title) {
        try {
            if (file == null || !file.exists()) { toast("ملف PDF غير موجود"); return; }
            Uri uri;
            try {
                uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            } catch (Exception fpError) {
                try {
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                } catch (Exception ignored) {}
                uri = Uri.fromFile(file);
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, title == null ? "تقرير PDF - " + AppStore.getNetworkName(this) : title);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "إرسال التقرير PDF عبر واتساب أو أي تطبيق"));
        } catch (Exception e) {
            toast("تم إنشاء PDF لكن تعذرت المشاركة. المسار: " + (file == null ? "" : file.getAbsolutePath()));
        }
    }

    private void showTrustedCreditAgentDateReportDialog(TrustedCreditAgent a) {
        if (a == null) return;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        layout.addView(small("اختر الفترة لعرض تقرير مرتب خاص بهذه النقطة. اترك التاريخ فارغاً لعرض كل السجل. الصيغة المقبولة: 2026/06/23 أو 2026-06-23."));
        EditText from = new EditText(this);
        from.setHint("من تاريخ اختياري: 2026/06/01");
        from.setGravity(Gravity.RIGHT);
        from.setSingleLine(true);
        from.setInputType(InputType.TYPE_CLASS_TEXT);
        EditText to = new EditText(this);
        to.setHint("إلى تاريخ اختياري: 2026/06/23");
        to.setGravity(Gravity.RIGHT);
        to.setSingleLine(true);
        to.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(from);
        layout.addView(to);
        new AlertDialog.Builder(this)
                .setTitle("تقرير نقطة البيع: " + a.name)
                .setView(layout)
                .setPositiveButton("عرض التقرير", (d,w) -> showTrustedCreditAgentReportText(a, from.getText().toString().trim(), to.getText().toString().trim()))
                .setNeutralButton("مشاركة PDF", (d,w) -> shareTrustedAgentReportPdf(a, from.getText().toString().trim(), to.getText().toString().trim()))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showTrustedCreditAgentReportText(TrustedCreditAgent a, String from, String to) {
        String report = buildTrustedCreditAgentDateReport(a, from, to);
        TextView tvReport = tv(report, 14, Color.rgb(25, 25, 30), false);
        tvReport.setGravity(Gravity.RIGHT);
        tvReport.setLineSpacing(4, 1.1f);
        ScrollView scroll = new ScrollView(this);
        scroll.setPadding(dp(10), dp(10), dp(10), dp(10));
        scroll.setBackgroundColor(Color.WHITE);
        scroll.addView(tvReport);
        new AlertDialog.Builder(this)
                .setTitle("تقرير المراجعة: " + (a == null ? "" : a.name))
                .setView(scroll)
                .setPositiveButton("إغلاق", null)
                .setNeutralButton("مشاركة PDF", (d,w) -> shareTrustedAgentReportPdf(a, from, to))
                .show();
    }

    private String normalizeReportDate(String value) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) return "";
        v = v.replace('-', '/');
        try {
            String[] parts = v.split("/");
            if (parts.length >= 3) {
                int y = Integer.parseInt(parts[0].trim());
                int m = Integer.parseInt(parts[1].trim());
                int d = Integer.parseInt(parts[2].trim());
                return String.format(Locale.US, "%04d/%02d/%02d", y, m, d);
            }
        } catch (Exception ignored) {}
        if (v.length() >= 10) return v.substring(0, 10);
        return v;
    }

    private boolean isLogInReportDateRange(OperationLog log, String from, String to) {
        String date = log == null || log.createdAt == null ? "" : log.createdAt.trim();
        if (date.length() >= 10) date = date.substring(0, 10).replace('-', '/');
        String f = normalizeReportDate(from);
        String t = normalizeReportDate(to);
        if (!f.isEmpty() && date.compareTo(f) < 0) return false;
        if (!t.isEmpty() && date.compareTo(t) > 0) return false;
        return true;
    }

    private boolean isLogForTrustedCreditAgent(OperationLog log, TrustedCreditAgent a) {
        if (log == null || a == null) return false;
        String sender = SmsProcessor.cleanPhone(log.sender == null ? "" : log.sender);
        String main = SmsProcessor.cleanPhone(a.senderPhone == null ? "" : a.senderPhone);
        String extra = SmsProcessor.cleanPhone(a.secondaryPhone == null ? "" : a.secondaryPhone);
        if (!sender.isEmpty() && (sender.equals(main) || (!extra.isEmpty() && sender.equals(extra)))) return true;
        String name = a.name == null ? "" : a.name.trim();
        if (!name.isEmpty() && log.customerName != null && log.customerName.trim().equals(name)) return true;
        String msg = log.message == null ? "" : log.message;
        return !name.isEmpty() && msg.contains(name);
    }

    private String sourceLabelForTrustedLog(OperationLog log, TrustedCreditAgent a) {
        String sender = SmsProcessor.cleanPhone(log == null || log.sender == null ? "" : log.sender);
        String main = SmsProcessor.cleanPhone(a == null || a.senderPhone == null ? "" : a.senderPhone);
        String extra = SmsProcessor.cleanPhone(a == null || a.secondaryPhone == null ? "" : a.secondaryPhone);
        if (!sender.isEmpty() && sender.equals(main)) return "الرقم الأساسي";
        if (!sender.isEmpty() && !extra.isEmpty() && sender.equals(extra)) return "الرقم الإضافي";
        if ("admin".equalsIgnoreCase(log == null ? "" : log.sender)) return "الإدارة";
        return sender.isEmpty() ? "غير محدد" : sender;
    }

    private String buildTrustedCreditAgentDateReport(TrustedCreditAgent a, String from, String to) {
        if (a == null) return "لا توجد نقطة بيع محددة.";
        ArrayList<OperationLog> logs = AppStore.loadLogs(this);
        StringBuilder out = new StringBuilder();
        String f = normalizeReportDate(from);
        String t = normalizeReportDate(to);
        out.append("نقطة البيع: ").append(a.name).append("\n");
        out.append("الرقم الأساسي: ").append(a.senderPhone).append("\n");
        out.append("الرقم الإضافي: ").append((a.secondaryPhone == null || a.secondaryPhone.trim().isEmpty()) ? "غير مسجل" : a.secondaryPhone).append("\n");
        out.append("الفترة: ").append(f.isEmpty() ? "من البداية" : f).append(" إلى ").append(t.isEmpty() ? "آخر سجل" : t).append("\n");
        out.append("--------------------------------\n");
        int count = 0, success = 0, pending = 0, rejected = 0, total = 0, topups = 0;
        StringBuilder rows = new StringBuilder();
        for (OperationLog log : logs) {
            if (!isLogForTrustedCreditAgent(log, a)) continue;
            if (!isLogInReportDateRange(log, from, to)) continue;
            count++;
            String st = log.status == null ? "" : log.status;
            String provider = log.provider == null ? "" : log.provider;
            boolean isTopup = provider.contains("تعبئة رصيد");
            if (isTopup) topups += Math.max(0, log.amount);
            if (AppStore.isPendingCriticalLog(log) || st.contains("معلق") || st.contains("مراجعة")) pending++;
            else if (st.contains("رفض") || st.contains("مرفوض") || st.contains("تجاوز") || st.contains("فشل")) rejected++;
            else if (st.contains("تم") || st.contains("جاري إرسال")) { success++; if (!isTopup) total += Math.max(0, log.amount); }
            String note = "المصدر: " + sourceLabelForTrustedLog(log, a)
                    + "، العميل: " + ((log.customerPhone == null || log.customerPhone.isEmpty()) ? "-" : log.customerPhone)
                    + "، الكرت: " + displayCardCode(log.cardCode)
                    + "، الرسالة: " + shortText(log.message, 160);
            rows.append(reportTableRow(
                    safe(log.createdAt),
                    log.amount + " ريال",
                    st.isEmpty() ? "غير محدد" : st,
                    paymentMethodForReport(log),
                    "1",
                    note
            )).append("\n");
        }
        out.append("عدد العمليات: ").append(count).append("\n");
        out.append("عمليات ناجحة/قيد الإرسال: ").append(success).append("\n");
        out.append("عمليات تحتاج مراجعة: ").append(pending).append("\n");
        out.append("عمليات مرفوضة/فاشلة: ").append(rejected).append("\n");
        out.append("إجمالي مبيعات الفترة: ").append(total).append(" ريال\n");
        out.append("إجمالي تعبئة الرصيد بالفترة: ").append(topups).append(" ريال\n");
        out.append("المتبقي الحالي: ").append(AppStore.remainingTrustedCredit(a)).append(" ريال\n");
        out.append("================================\n");
        out.append("تفاصيل العمليات\n");
        out.append("--------------------------------\n");
        out.append(REPORT_TABLE_HEADER).append("\n");
        out.append("--------------------------------\n");
        if (count == 0) out.append("لا توجد عمليات مطابقة لهذه النقطة ضمن التاريخ المحدد.");
        else out.append(rows);
        out.append(AppStore.getNetworkName(this)).append("\n");
        return out.toString();
    }

    private void showTrustedCreditTopUpDialog(TrustedCreditAgent a) {
        if (a == null) return;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        layout.addView(small("سيتم إضافة المبلغ فوق السقف/الرصيد السابق لنقطة البيع، ولن يتم تصفير المستخدم السابق. ستصل رسالة إلى نقطة البيع بتوقيع " + AppStore.getNetworkName(this) + "."));
        EditText amount = new EditText(this);
        amount.setHint("مبلغ التعبئة مثل 5000");
        amount.setGravity(Gravity.RIGHT);
        amount.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText note = new EditText(this);
        note.setHint("ملاحظة اختيارية");
        note.setGravity(Gravity.RIGHT);
        note.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(amount);
        layout.addView(note);
        new AlertDialog.Builder(this)
                .setTitle("تعبئة رصيد: " + a.name)
                .setView(layout)
                .setPositiveButton("تعبئة وإرسال", (d, w) -> {
                    int val = 0;
                    try { val = Integer.parseInt(amount.getText().toString().trim()); } catch (Exception ignored) {}
                    if (val <= 0) { toast("اكتب مبلغ تعبئة صحيح"); return; }
                    String logId = AppStore.addTrustedCreditTopUp(this, a.id, val, note.getText().toString().trim());
                    TrustedCreditAgent updated = AppStore.findTrustedCreditAgentById(this, a.id);
                    if (updated != null) {
                        String msg = AppStore.buildTrustedCreditTopUpMessage(this, updated, val);
                        SmsProcessor.sendSmsWithTracking(this, updated.senderPhone, msg, logId, val, "", false,
                                "تم إرسال رسالة تعبئة الرصيد لنقطة البيع",
                                "تمت التعبئة لكن فشل إرسال SMS لنقطة البيع");
                    }
                    toast("تمت تعبئة رصيد نقطة البيع");
                    showTrustedCreditAgents();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmResetTrustedCreditAgent(TrustedCreditAgent a) {
        if (a == null) return;
        String msg = "هل تريد تصفير المستخدم من سقف نقطة البيع؟\n\n"
                + "الاسم: " + a.name + "\n"
                + "الرقم: " + a.senderPhone + "\n"
                + "المستخدم الحالي: " + a.usedAmount + " ريال\n"
                + "المتبقي الحالي: " + AppStore.remainingTrustedCredit(a) + " ريال\n\n"
                + "اختر نعم للتصفير أو لا للإلغاء.";
        new AlertDialog.Builder(this)
                .setTitle("تأكيد تصفير السقف")
                .setMessage(msg)
                .setPositiveButton("نعم، صفّر", (d, w) -> {
                    AppStore.resetTrustedCreditAgentUsage(this, a.id);
                    toast("تم تصفير المستخدم لهذا الرقم");
                    showTrustedCreditAgents();
                })
                .setNegativeButton("لا", null)
                .show();
    }

    private void showTrustedCreditAgentDialog(TrustedCreditAgent old) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(8), dp(8), dp(8), dp(8));
        TextView help = small("الرقم الموثوق هو رقم نقطة البيع أو أي شخص تثق فيه ويرسل لك SMS. مثال رسالته: تم اضافة 100 من-733938509. تستطيع تفعيل أو تعطيل المكافآت لكل رقم بشكل مستقل.");
        EditText name = new EditText(this); name.setHint("اسم نقطة البيع / الشخص الموثوق"); name.setGravity(Gravity.RIGHT); name.setInputType(InputType.TYPE_CLASS_TEXT);
        EditText phone = new EditText(this); phone.setHint("الرقم الأساسي لنقطة البيع"); phone.setGravity(Gravity.RIGHT); phone.setInputType(InputType.TYPE_CLASS_PHONE);
        EditText secondaryPhone = new EditText(this); secondaryPhone.setHint("رقم إضافي اختياري لنفس نقطة البيع"); secondaryPhone.setGravity(Gravity.RIGHT); secondaryPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        EditText signature = new EditText(this); signature.setHint("بصمة نقطة البيع مثل: الشرعبي"); signature.setGravity(Gravity.RIGHT); signature.setInputType(InputType.TYPE_CLASS_TEXT);
        EditText limit = new EditText(this); limit.setHint("السقف بالريال مثل 5000"); limit.setGravity(Gravity.RIGHT); limit.setInputType(InputType.TYPE_CLASS_NUMBER);
        CheckBox active = new CheckBox(this); active.setText("مفعل"); active.setTextColor(text); active.setGravity(Gravity.RIGHT); active.setChecked(true);
        CheckBox requireSignature = new CheckBox(this); requireSignature.setText("طلب البصمة إجباري"); requireSignature.setTextColor(text); requireSignature.setGravity(Gravity.RIGHT); requireSignature.setChecked(true);
        CheckBox rewards = new CheckBox(this); rewards.setText("تفعيل المكافآت لهذا الرقم"); rewards.setTextColor(text); rewards.setGravity(Gravity.RIGHT); rewards.setChecked(true);
        if (old != null) {
            name.setText(old.name);
            phone.setText(old.senderPhone);
            secondaryPhone.setText(old.secondaryPhone == null ? "" : old.secondaryPhone);
            signature.setText(old.posSignature);
            limit.setText(String.valueOf(old.creditLimit));
            active.setChecked(old.active);
            requireSignature.setChecked(old.signatureRequired);
            rewards.setChecked(old.rewardsEnabled);
        } else {
            limit.setText("5000");
        }
        layout.addView(help);
        layout.addView(name);
        layout.addView(phone);
        layout.addView(secondaryPhone);
        layout.addView(signature);
        layout.addView(limit);
        layout.addView(active);
        layout.addView(requireSignature);
        layout.addView(rewards);
        if (!AppStore.isRewardsEnabled(this)) {
            layout.addView(small("ملاحظة: نظام المكافآت العام متوقف حالياً. هذا الخيار سيُحفظ للرقم، لكنه لن يعمل فعلياً حتى يتم تشغيل المكافآت من إعدادات المكافآت العامة."));
        }
        new AlertDialog.Builder(this)
                .setTitle(old == null ? "إضافة رقم موثوق" : "تعديل رقم موثوق")
                .setView(layout)
                .setPositiveButton("حفظ", (d,w) -> {
                    String ph = phone.getText().toString().trim();
                    if (ph.isEmpty()) { toast("رقم نقطة البيع مطلوب"); return; }
                    String extraPh = secondaryPhone.getText().toString().trim();
                    String cleanMain = SmsProcessor.cleanPhone(ph);
                    String cleanExtra = SmsProcessor.cleanPhone(extraPh);
                    if (!cleanExtra.isEmpty() && cleanExtra.equals(cleanMain)) { toast("الرقم الإضافي يجب أن يختلف عن الرقم الأساسي"); return; }
                    String sig = signature.getText().toString().trim();
                    if (requireSignature.isChecked() && AppStore.isBadPosSignature(sig)) { toast("البصمة مطلوبة ويجب ألا تكون رقماً فقط أو أقل من 3 أحرف أو فور يو"); return; }
                    int lim = 5000;
                    try { lim = Integer.parseInt(limit.getText().toString().trim()); } catch(Exception ignored) {}
                    AppStore.addOrUpdateTrustedCreditAgent(this, name.getText().toString().trim(), ph, extraPh, sig, lim, active.isChecked(), rewards.isChecked(), requireSignature.isChecked());
                    toast("تم حفظ الرقم الموثوق" + (requireSignature.isChecked() ? " مع بصمة إجبارية" : " بدون بصمة إجبارية"));
                    showTrustedCreditAgents();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private String getFileName(Uri uri) {
        String result = "txt";
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
                cursor.close();
            }
        } catch(Exception ignored) {}
        return result;
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
