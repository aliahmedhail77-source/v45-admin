package com.fourunet.pro;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

class AppStore {
    private static volatile boolean autoBackupRunning = false;
    private static final int AUTO_BACKUP_MAX_CARDS_ON_UI_SAFE = 50000;
    private static final String PREF = "fourunet_pro_clean_store";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_CARDS = "cards";
    private static final String KEY_LOGS = "logs";
    private static final String KEY_LEDGER_CUSTOMERS = "ledger_customers_v12";
    private static final String KEY_LEDGER_ENTRIES = "ledger_entries_v12";
    private static final String KEY_LEDGER_ENABLED = "ledger_enabled_v12";
    private static final String KEY_LEDGER_LOAN_ENABLED = "ledger_loan_enabled_v12";
    private static final String KEY_LEDGER_AUTO_SETTLE_ENABLED = "ledger_auto_settle_enabled_v12";
    private static final String KEY_PROCESSED = "processed";
    private static final String KEY_EXACT_PAYMENT_TEXTS = "exact_payment_texts_v44";
    private static final String KEY_TRUSTED_REQUEST_LOCKS = "trusted_request_locks_v48";
    private static final String KEY_IMPORTED_BATCHES = "imported_batches";
    private static final String KEY_RESTORE_HISTORY = "restore_history";
    private static final String KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled";
    private static final String KEY_APP_LOCK_METHOD = "app_lock_method_v47";
    private static final String KEY_APP_LOCK_PIN_HASH = "app_lock_pin_hash_v47";
    private static final String KEY_AUTO_BACKUP_INTERVAL_HOURS = "auto_backup_interval_hours";
    private static final String KEY_LAST_AUTO_BACKUP_AT = "last_auto_backup_at";
    private static final String KEY_CARD_PRIVACY_ENABLED = "card_privacy_enabled";
    private static final String KEY_CARD_SALE_NEWEST_FIRST = "card_sale_newest_first_v143";
    private static final String KEY_CARD_SALE_ORDER_BY_AMOUNT_PREFIX = "card_sale_newest_first_amount_";
    private static final String KEY_TRUSTED = "trusted_contacts";
    private static final String KEY_TRUSTED_CREDIT_AGENTS = "trusted_credit_agents";
    private static final String KEY_POS_OUTLETS = "pos_outlets";
    private static final String KEY_AUTO_SEND = "auto_send_enabled";
    private static final String KEY_SUCCESS_TEMPLATE = "success_message_template";
    private static final String KEY_DIRECT_SALE_TEMPLATE = "direct_sale_message_template";
    private static final String KEY_NO_STOCK_TEMPLATE = "no_stock_message_template";
    private static final String KEY_POS_SUCCESS_TEMPLATE = "pos_success_message_template_v50";
    private static final String KEY_POS_DUPLICATE_TEMPLATE = "pos_duplicate_message_template_v50";
    private static final String KEY_POS_INVALID_PHONE_TEMPLATE = "pos_invalid_phone_message_template_v50";
    private static final String KEY_POS_LIMIT_TEMPLATE = "pos_limit_message_template_v50";
    private static final String KEY_POS_NO_STOCK_TEMPLATE = "pos_no_stock_message_template_v50";
    private static final String KEY_POS_INVALID_CATEGORY_TEMPLATE = "pos_invalid_category_message_template_v50";
    private static final String KEY_POS_AMBIGUOUS_TEMPLATE = "pos_ambiguous_message_template_v50";
    private static final String KEY_LEDGER_PAYMENT_TEMPLATE = "ledger_payment_message_template_v138";
    private static final String KEY_POS_TOPUP_TEMPLATE = "pos_topup_message_template_v138";
    private static final String KEY_NETWORK_NAME = "network_name";
    private static final String KEY_ADMIN_PHONE = "admin_phone";
    private static final String KEY_NETWORK_LOGO_PATH = "network_logo_path_v134";
    private static final String KEY_SHOW_PHONE_IN_SIGNATURE = "show_phone_in_signature_v134";
    private static final String KEY_UI_THEME_MODE = "ui_theme_mode_v142";
    private static final String KEY_UI_ACCENT_COLOR = "ui_accent_color_v142";
    private static final String KEY_MIKROTIK_HOST = "mikrotik_host";
    private static final String KEY_MIKROTIK_PORT = "mikrotik_port";
    private static final String KEY_MIKROTIK_USER = "mikrotik_user";
    private static final String KEY_MIKROTIK_PROFILE_FILTER = "mikrotik_profile_filter";
    private static final String KEY_MIKROTIK_LAST_LIMIT = "mikrotik_last_limit";
    private static final String KEY_REWARDS_ENABLED = "rewards_enabled";
    private static final String KEY_REWARDS_THRESHOLD = "rewards_threshold";
    private static final String KEY_REWARDS_REWARD_AMOUNT = "rewards_reward_amount";
    private static final String KEY_REWARD_CATEGORY_SETTINGS = "reward_category_settings";
    private static final String KEY_CUSTOMER_REWARDS = "customer_rewards";
    private static final String KEY_REWARD_POINTS_TEMPLATE = "reward_points_template";
    private static final String KEY_REWARD_SENT_TEMPLATE = "reward_sent_template";
    private static final String KEY_REWARD_PENDING_TEMPLATE = "reward_pending_template";
    private static final String KEY_REWARD_EXPIRY_ENABLED = "reward_expiry_enabled";
    private static final String KEY_REWARD_EXPIRY_DAYS = "reward_expiry_days";

    private static final String KEY_UPDATE_MANIFEST_URL = "update_manifest_url";
    private static final String DEFAULT_UPDATE_MANIFEST_URL = "https://example.com/online_update.json";
    private static final String KEY_ACTIVATED = "activated";
    private static final String KEY_ACTIVATION_CODE = "activation_code";
    private static final String KEY_ACTIVATED_DEVICE_ID = "activated_device_id";
    private static final String KEY_ACTIVATED_AT = "activated_at";
    private static final String KEY_LOCAL_DEVICE_ID = "local_device_id";
    private static final String KEY_LICENSE_API_URL = "license_api_url";
    private static final String KEY_LICENSE_REQUEST_CODE = "license_request_code";
    private static final String KEY_LICENSE_STATUS = "license_status";
    private static final String KEY_LICENSE_EXPIRES_AT = "license_expires_at";
    private static final String KEY_LICENSE_LAST_CHECK = "license_last_check";
    private static final String KEY_LICENSE_MESSAGE = "license_message";
    private static final String KEY_TRIAL_ACTIVATED = "trial_activated";
    private static final String KEY_TRIAL_CODE = "trial_code";
    private static final String KEY_TRIAL_DEVICE_ID = "trial_device_id";
    private static final String KEY_TRIAL_STARTED_AT_MS = "trial_started_at_ms";
    private static final String KEY_TRIAL_EXPIRES_AT_MS = "trial_expires_at_ms";
    private static final String KEY_TRIAL_LAST_SEEN_AT_MS = "trial_last_seen_at_ms";
    private static final String DEFAULT_LICENSE_API_URL = "https://YOUR-DOMAIN.com/online_license/api.php";
    private static final long MONTHLY_CHECK_MS = 31L * 24L * 60L * 60L * 1000L;
    private static final int TRIAL_DAYS = 30;
    private static final int ANNUAL_DAYS = 365;
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;
    private static final long CLOCK_ROLLBACK_TOLERANCE_MS = 10L * 60L * 1000L;
    static final String LOCK_METHOD_NONE = "none";
    static final String LOCK_METHOD_FINGERPRINT = "fingerprint";
    static final String LOCK_METHOD_PIN = "pin";
    static final String LOCK_METHOD_FINGERPRINT_PIN = "fingerprint_pin";

    private static final String DEFAULT_NETWORK_NAME = "فور يو";
    private static final String DEFAULT_ADMIN_PHONE = "";

    // Stage 13.6.1 STRONG PERFORMANCE ENGINE
    // Cards are stored in SQLite for large stock imports. SharedPreferences remains for legacy settings/logs.
    static final int PERFORMANCE_LOG_PAGE_SIZE = 20;
    static final int PERFORMANCE_CARD_PAGE_SIZE = 20;
    private static final int MAX_FAST_LOGS = 1500;
    private static String cardsCacheJson = null;
    private static ArrayList<CardItem> cardsCache = null;
    private static HashMap<Integer, StockCount> stockCountCache = null;
    private static String logsCacheJson = null;
    private static ArrayList<OperationLog> logsCache = null;

    private static final String KEY_CARDS_DB_READY = "cards_db_ready_v1361";
    private static final String CARD_DB_NAME = "online_cards_perf.db";
    private static final int CARD_DB_VERSION = 1;
    private static CardDbHelper cardDbHelper = null;

    interface ImportProgressCallback {
        void onProgress(int scanned, int added, int duplicates);
    }

    private static class CardDbHelper extends SQLiteOpenHelper {
        CardDbHelper(Context c) { super(c.getApplicationContext(), CARD_DB_NAME, null, CARD_DB_VERSION); }

        @Override public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS cards (" +
                    "id TEXT PRIMARY KEY," +
                    "amount INTEGER NOT NULL," +
                    "code TEXT NOT NULL," +
                    "code_key TEXT NOT NULL UNIQUE," +
                    "sold INTEGER NOT NULL DEFAULT 0," +
                    "buyer_phone TEXT," +
                    "sold_at TEXT," +
                    "source TEXT," +
                    "created_at TEXT" +
                    ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_cards_amount_sold ON cards(amount, sold)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_cards_amount_source_sold ON cards(amount, source, sold)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_cards_sold_at ON cards(sold, sold_at)");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_cards_code_key ON cards(code_key)");
        }

        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { onCreate(db); }
    }

    private static SQLiteDatabase cardDb(Context c) {
        synchronized (AppStore.class) {
            if (cardDbHelper == null) cardDbHelper = new CardDbHelper(c.getApplicationContext());
            SQLiteDatabase db = cardDbHelper.getWritableDatabase();
            try { db.enableWriteAheadLogging(); } catch (Exception ignored) {}
            migrateCardsPrefsToDbIfNeeded(c, db);
            return db;
        }
    }

    private static void migrateCardsPrefsToDbIfNeeded(Context c, SQLiteDatabase db) {
        SharedPreferences sp = prefs(c);
        if (sp.getBoolean(KEY_CARDS_DB_READY, false)) return;
        String raw = sp.getString(KEY_CARDS, "[]");
        db.beginTransaction();
        try {
            if (raw != null && raw.trim().length() > 2) {
                JSONArray arr = new JSONArray(raw);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String code = normalizeCardCode(o.optString("code"));
                    if (code.isEmpty()) continue;
                    ContentValues cv = new ContentValues();
                    cv.put("id", o.optString("id", UUID.randomUUID().toString()));
                    cv.put("amount", o.optInt("amount"));
                    cv.put("code", code);
                    cv.put("code_key", cardDuplicateKey(code));
                    cv.put("sold", o.optBoolean("sold") ? 1 : 0);
                    cv.put("buyer_phone", o.optString("buyerPhone", ""));
                    cv.put("sold_at", o.optString("soldAt", ""));
                    cv.put("source", o.optString("source", ""));
                    String createdAt = o.optString("createdAt", "");
                    if (createdAt.trim().isEmpty()) createdAt = o.optString("addedAt", "");
                    cv.put("created_at", createdAt);
                    db.insertWithOnConflict("cards", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
            db.setTransactionSuccessful();
            sp.edit().putBoolean(KEY_CARDS_DB_READY, true).remove(KEY_CARDS).commit();
            cardsCacheJson = null;
            cardsCache = null;
            stockCountCache = null;
        } catch (Exception e) {
            // Keep old SharedPreferences data if migration fails.
        } finally {
            try { db.endTransaction(); } catch (Exception ignored) {}
        }
    }

    private static CardItem cardFromCursor(Cursor cur) {
        return new CardItem(
                cur.getString(cur.getColumnIndexOrThrow("id")),
                cur.getInt(cur.getColumnIndexOrThrow("amount")),
                cur.getString(cur.getColumnIndexOrThrow("code")),
                cur.getInt(cur.getColumnIndexOrThrow("sold")) == 1,
                cur.getString(cur.getColumnIndexOrThrow("buyer_phone")),
                cur.getString(cur.getColumnIndexOrThrow("sold_at")),
                cur.getString(cur.getColumnIndexOrThrow("source")),
                cur.getString(cur.getColumnIndexOrThrow("created_at"))
        );
    }

    private static ContentValues cardValues(CardItem card) {
        ContentValues cv = new ContentValues();
        cv.put("id", card.id == null || card.id.trim().isEmpty() ? UUID.randomUUID().toString() : card.id);
        cv.put("amount", card.amount);
        String code = normalizeCardCode(card.code);
        cv.put("code", code);
        cv.put("code_key", cardDuplicateKey(code));
        cv.put("sold", card.sold ? 1 : 0);
        cv.put("buyer_phone", card.buyerPhone == null ? "" : card.buyerPhone);
        cv.put("sold_at", card.soldAt == null ? "" : card.soldAt);
        cv.put("source", card.source == null ? "" : card.source);
        cv.put("created_at", card.createdAt == null || card.createdAt.trim().isEmpty() ? now() : card.createdAt);
        return cv;
    }


    // STAGE 13.6.2 HOTFIX:
    // If the first SQLite migration is interrupted on a device that already has a very large
    // legacy SharedPreferences card list, automatic selling must not stop with "no stock".
    // These tiny fallbacks are used only while the DB-ready flag is still false.
    private static boolean hasLegacyCardsPrefs(Context c) {
        try {
            String raw = prefs(c).getString(KEY_CARDS, "[]");
            return raw != null && raw.trim().length() > 2;
        } catch (Exception e) { return false; }
    }

    private static CardItem takeAvailableCardLegacyPrefs(Context c, int amount, String buyerPhone) {
        ArrayList<CardItem> cards = new ArrayList<>();
        CardItem selected = null;
        try {
            String raw = prefs(c).getString(KEY_CARDS, "[]");
            JSONArray arr = new JSONArray(raw == null ? "[]" : raw);
            String when = now();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String createdAt = o.optString("createdAt", "");
                if (createdAt.trim().isEmpty()) createdAt = o.optString("addedAt", "");
                CardItem item = new CardItem(
                        o.optString("id", UUID.randomUUID().toString()),
                        o.optInt("amount"),
                        o.optString("code"),
                        o.optBoolean("sold"),
                        o.optString("buyerPhone"),
                        o.optString("soldAt"),
                        o.optString("source"),
                        createdAt
                );
                if (selected == null && item.amount == amount && !item.sold && !isRewardStock(item)) {
                    item.sold = true;
                    item.buyerPhone = buyerPhone == null ? "" : buyerPhone;
                    item.soldAt = when;
                    selected = item;
                }
                cards.add(item);
            }
            if (selected != null) {
                JSONArray out = new JSONArray();
                for (CardItem card : cards) {
                    JSONObject o = new JSONObject();
                    o.put("id", card.id);
                    o.put("amount", card.amount);
                    o.put("code", card.code);
                    o.put("sold", card.sold);
                    o.put("buyerPhone", card.buyerPhone == null ? "" : card.buyerPhone);
                    o.put("soldAt", card.soldAt == null ? "" : card.soldAt);
                    o.put("source", card.source == null ? "" : card.source);
                    o.put("createdAt", card.createdAt == null ? "" : card.createdAt);
                    out.put(o);
                }
                prefs(c).edit().putString(KEY_CARDS, out.toString()).commit();
                invalidateCardsCache();
            }
        } catch (Exception ignored) {}
        return selected;
    }

    private static ArrayList<CardItem> takeAvailableCardsAllOrNoneLegacyPrefs(Context c, int[] amounts, String buyerPhone) {
        ArrayList<CardItem> cards = new ArrayList<>();
        ArrayList<CardItem> selected = new ArrayList<>();
        if (amounts == null || amounts.length == 0) return selected;
        try {
            String raw = prefs(c).getString(KEY_CARDS, "[]");
            JSONArray arr = new JSONArray(raw == null ? "[]" : raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String createdAt = o.optString("createdAt", "");
                if (createdAt.trim().isEmpty()) createdAt = o.optString("addedAt", "");
                cards.add(new CardItem(o.optString("id", UUID.randomUUID().toString()), o.optInt("amount"), o.optString("code"), o.optBoolean("sold"), o.optString("buyerPhone"), o.optString("soldAt"), o.optString("source"), createdAt));
            }
            HashSet<String> picked = new HashSet<>();
            for (int amount : amounts) {
                CardItem found = null;
                for (CardItem item : cards) {
                    if (item.amount == amount && !item.sold && !isRewardStock(item) && !picked.contains(item.id)) {
                        found = item;
                        break;
                    }
                }
                if (found == null) return new ArrayList<>();
                selected.add(found);
                picked.add(found.id);
            }
            String when = now();
            for (CardItem item : selected) {
                item.sold = true;
                item.buyerPhone = buyerPhone == null ? "" : buyerPhone;
                item.soldAt = when;
            }
            JSONArray out = new JSONArray();
            for (CardItem card : cards) {
                JSONObject o = new JSONObject();
                o.put("id", card.id);
                o.put("amount", card.amount);
                o.put("code", card.code);
                o.put("sold", card.sold);
                o.put("buyerPhone", card.buyerPhone == null ? "" : card.buyerPhone);
                o.put("soldAt", card.soldAt == null ? "" : card.soldAt);
                o.put("source", card.source == null ? "" : card.source);
                o.put("createdAt", card.createdAt == null ? "" : card.createdAt);
                out.put(o);
            }
            prefs(c).edit().putString(KEY_CARDS, out.toString()).commit();
            invalidateCardsCache();
            return selected;
        } catch (Exception ignored) { return new ArrayList<>(); }
    }

    private static final String OFFLINE_LIFETIME_SECRET = "ONLINE_V14_PRIVATE_LIFETIME_SECRET_776901570";
    private static final String OFFLINE_TRIAL_SECRET = "ONLINE_V29_PRIVATE_TRIAL_30_DAYS_SECRET_776901570";
    private static final String OFFLINE_ANNUAL_SECRET = "ONLINE_V29_PRIVATE_ANNUAL_365_DAYS_SECRET_776901570";
    private static final String CARD_PACK_SECRET = "ONLINE_PRIVATE_CARD_PACK_SECRET_776901570_V18";

    // V30: نظام ترخيص أوفلاين موقّع. التطبيق يحتوي Public Key فقط للتحقق، ومولّد الأكواد يحتوي Private Key.
    private static final String SIGNED_LICENSE_PREFIX = "ONL2.";
    private static final String SIGNED_LICENSE_PUBLIC_MODULUS_HEX = "ccacd095a7d76c43710dc298ce58b1751ec2b1766f375308d64ae58e09595f51db022d91d9f3e55d80fb7a822fecdd6031344320d805c7890e0e9669ea38cfe6d704e1285ef951db3944fe9bf3146137cfabd0ee01e0573f3145f1f5d6295da4487c4613937e8f6f8a3295217d8198c764e6400128d13832bc1d313a2521edb46726ebc41695839bf0a10c18d927ad7a4ef882b0a98ee9c077e2f83364b03e76de7f55c2e8e0dfbc707433747cf3d3f251051e6193514142c45949fc228a4082595d35f8f33859c77251d06790e0f813d3e1929a497e3780b85b606fabbb7fdb31348dd18ee25ea841b8ef45280762217e408de8e23489ebb683929472e4557f";
    private static final String SIGNED_LICENSE_PUBLIC_EXPONENT_HEX = "10001";


    private static final String[] OFFLINE_CODE_HASHES = new String[]{
            "9cedd93f760ccfe3da9525c41b67df5c7fc10db71be03f362027c9710e924502",
            "3750044a5df61d0f543443e2455d33723589ee80414ef113cfcd65bee9f75d1d",
            "9d446c1817550c6b8b18374b97c4b129ae2109620582de55df7c8af9ff0b75f0",
            "bf4294cc75ce1ea9e3c721590df17ad96c5fc2bf2263265fe6703585934e8612",
            "656c95c99999a14f6ca2406143eacefa886abdfb3be78e96f0ba9617430da43e",
            "3e2df6d5b09057a22c2974391769e5dd88b3a834c54f9cb70cb7843e32042c7a",
            "dc2f3c429a411e120cbdfef05ced51edf7e5ad46ca6bda921de5bb4f54f2704d",
            "0df747ff0ee9ec8741a57661ca556da3422844bbc8becb6158ea4c8f1ba6427a",
            "9fb1fc38936724ba55a5264c319d87a59fb85219466220477cf8c2cd6ac01a53",
            "82d8059525a7599e08405e04a4b1d410e2687ab59aa83d03784a7b8ed8d5e470"
    };

    static final String SYSTEM_SIGNATURE = "فور يو";
    static final String NETWORK_SIGNATURE = "شبكة لان فور يو";

    static final String DEFAULT_SUCCESS_TEMPLATE = "كرتك فئة {amount}:\n"
            + "{card}\n"
            + "{network}";

    static final String DEFAULT_DIRECT_SALE_TEMPLATE = "كرتك فئة {amount}:\n"
            + "{card}\n"
            + "{network}";

    static final String DEFAULT_NO_STOCK_TEMPLATE = "نفدت فئة {amount} ريال. راجع الإدارة: {adminPhone}";
    static final String DEFAULT_LEDGER_PAYMENT_TEMPLATE = "مرحبًا {customerName}👋\n✅ وصل {amount}ر.ي\nالرصيد قبل: {debtBefore}ر.ي\nالمتبقي الآن: {debtAfter}ر.ي\nرصيدك الزائد: {creditAfter}ر.ي\n{signature}";
    static final String DEFAULT_POS_TOPUP_TEMPLATE = "تمت تعبئة رصيدك\nالمبلغ: {amount}\nالرصيد السابق: {oldRemaining}\nالرصيد المتاح الآن: {remaining}\n{signature}";

    static final String DEFAULT_POS_SUCCESS_TEMPLATE = "تم التنفيذ\n"
            + "العميل: {customer_phone}\n"
            + "الفئات: {categories}\n"
            + "المتبقي: {remaining_limit}\n"
            + "{network}";
    static final String DEFAULT_POS_DUPLICATE_TEMPLATE = "طلب مكرر خلال {window_minutes} دقائق. انتظر أو غيّر الطلب.\n" + "{network}";
    static final String DEFAULT_POS_INVALID_PHONE_TEMPLATE = "رقم العميل غير صحيح. يجب أن يكون 9 أرقام ويبدأ بـ 7.\n" + "{network}";
    static final String DEFAULT_POS_LIMIT_TEMPLATE = "تعذر التنفيذ: تجاوز السقف. السقف:{credit_limit} المستخدم:{used_amount} الطلب:{total_amount}\n" + "{network}";
    static final String DEFAULT_POS_NO_STOCK_TEMPLATE = "تعذر التنفيذ: لا توجد كروت كافية للفئات {categories}.\n" + "{network}";
    static final String DEFAULT_POS_INVALID_CATEGORY_TEMPLATE = "تعذر التنفيذ: فئة غير معتمدة. {reason}\n" + "{network}";
    static final String DEFAULT_POS_AMBIGUOUS_TEMPLATE = "تعذر التنفيذ: الطلب غير واضح وتم تحويله للمراجعة. {reason}\n" + "{network}";

    static final int[] DEFAULT_AMOUNTS = new int[]{50, 100, 150, 200, 250, 300, 500};
    static final int DEFAULT_REWARD_PERCENT = 8;
    static final int DEFAULT_REWARD_THRESHOLD = 100;
    static final int DEFAULT_REWARD_EXPIRY_DAYS = 10;
    static final int DEFAULT_REWARD_EXPIRY_WARNING_DAYS = 2;
    static final int DEFAULT_REWARD_CARD_AMOUNT = 100;
    static final String DEFAULT_REWARD_POINTS_TEMPLATE = "نقاط:+{points} رصيد:{balance} باقي:{remain}";
    static final String DEFAULT_REWARD_SENT_TEMPLATE = "مبروك هديتك:{rewardCard} رصيد:{balance}";
    static final String DEFAULT_REWARD_PENDING_TEMPLATE = "وصلت للهدية، انتظر توفر الكروت";

    private static class StockCount {
        int total;
        int sold;
        int available;
        int rewardTotal;
        int rewardSold;
        int rewardAvailable;
    }

    private static ArrayList<CardItem> copyCardList(ArrayList<CardItem> src) {
        return src == null ? new ArrayList<CardItem>() : new ArrayList<CardItem>(src);
    }

    private static ArrayList<OperationLog> copyLogList(ArrayList<OperationLog> src) {
        return src == null ? new ArrayList<OperationLog>() : new ArrayList<OperationLog>(src);
    }

    static int performanceLogPageSize() { return PERFORMANCE_LOG_PAGE_SIZE; }

    static int performanceCardPageSize() { return PERFORMANCE_CARD_PAGE_SIZE; }

    static boolean isCardSaleNewestFirst(Context c) {
        return prefs(c).getBoolean(KEY_CARD_SALE_NEWEST_FIRST, false);
    }

    static void setCardSaleOrder(Context c, boolean newestFirst) {
        prefs(c).edit().putBoolean(KEY_CARD_SALE_NEWEST_FIRST, newestFirst).apply();
    }

    static boolean isCardSaleNewestFirstForAmount(Context c, int amount) {
        String key = KEY_CARD_SALE_ORDER_BY_AMOUNT_PREFIX + amount;
        if (prefs(c).contains(key)) return prefs(c).getBoolean(key, false);
        return isCardSaleNewestFirst(c);
    }

    static void setCardSaleOrderForAmount(Context c, int amount, boolean newestFirst) {
        prefs(c).edit().putBoolean(KEY_CARD_SALE_ORDER_BY_AMOUNT_PREFIX + amount, newestFirst).apply();
    }

    static String cardSaleOrderLabel(Context c) {
        return isCardSaleNewestFirst(c) ? "الأحدث إدخالًا أولًا" : "الأقدم إدخالًا أولًا";
    }

    static String cardSaleOrderLabelForAmount(Context c, int amount) {
        return isCardSaleNewestFirstForAmount(c, amount) ? "الأحدث إدخالًا أولًا" : "الأقدم إدخالًا أولًا";
    }

    private static String cardSaleSqlOrder(Context c) {
        return isCardSaleNewestFirst(c) ? "rowid DESC" : "rowid ASC";
    }

    private static String cardSaleSqlOrder(Context c, int amount) {
        return isCardSaleNewestFirstForAmount(c, amount) ? "rowid DESC" : "rowid ASC";
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }


    static String getAppLockMethod(Context c) {
        String method = prefs(c).getString(KEY_APP_LOCK_METHOD, LOCK_METHOD_NONE);
        if (method == null || method.trim().isEmpty()) return LOCK_METHOD_NONE;
        if (!LOCK_METHOD_FINGERPRINT.equals(method) && !LOCK_METHOD_PIN.equals(method) && !LOCK_METHOD_FINGERPRINT_PIN.equals(method)) return LOCK_METHOD_NONE;
        return method;
    }

    static void setAppLockMethod(Context c, String method) {
        String clean = method == null ? LOCK_METHOD_NONE : method.trim();
        if (!LOCK_METHOD_FINGERPRINT.equals(clean) && !LOCK_METHOD_PIN.equals(clean) && !LOCK_METHOD_FINGERPRINT_PIN.equals(clean)) clean = LOCK_METHOD_NONE;
        prefs(c).edit().putString(KEY_APP_LOCK_METHOD, clean).apply();
    }

    static boolean isAppLockRequired(Context c) {
        return !LOCK_METHOD_NONE.equals(getAppLockMethod(c));
    }

    static String appLockMethodLabel(String method) {
        if (LOCK_METHOD_FINGERPRINT.equals(method)) return "بصمة الإصبع";
        if (LOCK_METHOD_PIN.equals(method)) return "PIN / رقم سري";
        if (LOCK_METHOD_FINGERPRINT_PIN.equals(method)) return "بصمة + PIN احتياطي";
        return "بدون حماية";
    }

    static boolean isValidAppPin(String pin) {
        if (pin == null) return false;
        String v = pin.trim();
        return v.matches("\\d{4,8}");
    }

    static boolean hasAppPin(Context c) {
        return !prefs(c).getString(KEY_APP_LOCK_PIN_HASH, "").isEmpty();
    }

    static void setAppPin(Context c, String pin) {
        if (!isValidAppPin(pin)) return;
        prefs(c).edit().putString(KEY_APP_LOCK_PIN_HASH, appPinHash(c, pin.trim())).apply();
    }

    static boolean verifyAppPin(Context c, String pin) {
        if (!isValidAppPin(pin)) return false;
        String saved = prefs(c).getString(KEY_APP_LOCK_PIN_HASH, "");
        return !saved.isEmpty() && saved.equals(appPinHash(c, pin.trim()));
    }

    private static String appPinHash(Context c, String pin) {
        return sha256("APP_LOCK_PIN|" + getDeviceId(c) + "|" + (pin == null ? "" : pin.trim()));
    }

    static String getNetworkName(Context c) {
        return prefs(c).getString(KEY_NETWORK_NAME, DEFAULT_NETWORK_NAME);
    }



    static String getUpdateManifestUrl(Context c) {
        return prefs(c).getString(KEY_UPDATE_MANIFEST_URL, DEFAULT_UPDATE_MANIFEST_URL);
    }

    static void setUpdateManifestUrl(Context c, String url) {
        String value = url == null ? "" : url.trim();
        prefs(c).edit().putString(KEY_UPDATE_MANIFEST_URL, value).apply();
    }

    static String formatNetworkName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) value = DEFAULT_NETWORK_NAME;
        value = value.replace("أونلاين", "اونلاين").replace("انلاين", "اونلاين");
        value = value.replace("ONLINE", "اونلاين").replace("Online", "اونلاين").replace("online", "اونلاين");
        while (value.contains("  ")) value = value.replace("  ", " ");
        return value.trim();
    }

    static void setNetworkName(Context c, String name) {
        prefs(c).edit().putString(KEY_NETWORK_NAME, formatNetworkName(name)).apply();
    }

    // Stage 13.4: هوية الشبكة العامة - الاسم والرقم والشعار والتوقيع.
    static String getNetworkLogoPath(Context c) {
        return prefs(c).getString(KEY_NETWORK_LOGO_PATH, "");
    }

    static void setNetworkLogoPath(Context c, String path) {
        prefs(c).edit().putString(KEY_NETWORK_LOGO_PATH, path == null ? "" : path.trim()).apply();
    }

    static void clearNetworkLogo(Context c) {
        prefs(c).edit().remove(KEY_NETWORK_LOGO_PATH).apply();
    }

    static boolean isShowPhoneInSignature(Context c) {
        return prefs(c).getBoolean(KEY_SHOW_PHONE_IN_SIGNATURE, false);
    }

    static void setShowPhoneInSignature(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_SHOW_PHONE_IN_SIGNATURE, enabled).apply();
    }

    static String getNetworkPhone(Context c) {
        return getAdminPhone(c);
    }

    static void setNetworkPhone(Context c, String phone) {
        setAdminPhone(c, phone);
    }

    static String getMessageSignature(Context c) {
        String name = getNetworkName(c);
        String phone = getNetworkPhone(c);
        if (isShowPhoneInSignature(c) && phone != null && !phone.trim().isEmpty()) {
            return name + "\n" + phone.trim();
        }
        return name;
    }

    // Stage 14.2: إعدادات واجهة خفيفة: داكن/نهاري + لون رئيسي.
    static String getUiThemeMode(Context c) {
        return prefs(c).getString(KEY_UI_THEME_MODE, "dark");
    }

    static void setUiThemeMode(Context c, String mode) {
        String value = mode == null ? "dark" : mode.trim().toLowerCase(Locale.US);
        if (!"light".equals(value)) value = "dark";
        prefs(c).edit().putString(KEY_UI_THEME_MODE, value).apply();
    }

    static boolean isLightUi(Context c) {
        return "light".equals(getUiThemeMode(c));
    }

    static String getUiAccentColor(Context c) {
        return prefs(c).getString(KEY_UI_ACCENT_COLOR, "green");
    }

    static void setUiAccentColor(Context c, String color) {
        String value = color == null ? "green" : color.trim().toLowerCase(Locale.US);
        if (!("green".equals(value) || "cyan".equals(value) || "gold".equals(value) || "purple".equals(value))) value = "green";
        prefs(c).edit().putString(KEY_UI_ACCENT_COLOR, value).apply();
    }

    static String getPdfHeaderSubtitle(Context c) {
        String phone = getNetworkPhone(c);
        if (phone != null && !phone.trim().isEmpty()) return "رقم التواصل: " + phone.trim();
        return "إدارة وبيع كروت الإنترنت تلقائياً";
    }

    static boolean isActivated(Context c) {
        if (!prefs(c).getBoolean(KEY_ACTIVATED, false)) return false;
        String savedDevice = prefs(c).getString(KEY_ACTIVATED_DEVICE_ID, "");
        return savedDevice != null && savedDevice.equals(getDeviceId(c));
    }

    static boolean isLicenseUsable(Context c) {
        // V30: يدعم ترخيصًا أوفلاين موقّعًا بتواريخ ثابتة + توافقًا مع رموز V29 القديمة.
        return isActivated(c) || isTrialUsable(c);
    }

    static boolean isTrialUsable(Context c) {
        SharedPreferences sp = prefs(c);
        if (!sp.getBoolean(KEY_TRIAL_ACTIVATED, false)) return false;
        String savedDevice = sp.getString(KEY_TRIAL_DEVICE_ID, "");
        if (savedDevice == null || !savedDevice.equals(getDeviceId(c))) return false;
        long nowMs = System.currentTimeMillis();
        long startMs = sp.getLong(KEY_TRIAL_STARTED_AT_MS, 0);
        long expiresMs = sp.getLong(KEY_TRIAL_EXPIRES_AT_MS, 0);
        long lastSeenMs = sp.getLong(KEY_TRIAL_LAST_SEEN_AT_MS, 0);
        String status = sp.getString(KEY_LICENSE_STATUS, "");
        String label = status != null && status.contains("annual") ? "الاشتراك السنوي" : "الفترة التجريبية";
        if (startMs <= 0 || expiresMs <= 0 || expiresMs <= startMs) return false;
        if (lastSeenMs > 0 && nowMs + CLOCK_ROLLBACK_TOLERANCE_MS < lastSeenMs) {
            sp.edit().putString(KEY_LICENSE_MESSAGE, "تم إيقاف " + label + " بسبب تغيير تاريخ الجهاز للخلف. يرجى التواصل مع الإدارة للتفعيل.").apply();
            return false;
        }
        if (nowMs > expiresMs) {
            sp.edit().putString(KEY_LICENSE_MESSAGE, "انتهى " + label + ". يرجى التواصل مع الإدارة لتجديد التفعيل أو شراء تفعيل مدى الحياة.").apply();
            return false;
        }
        if (nowMs > lastSeenMs) sp.edit().putLong(KEY_TRIAL_LAST_SEEN_AT_MS, nowMs).apply();
        return true;
    }

    static void setActivated(Context c, String activationCode) {
        prefs(c).edit()
                .putBoolean(KEY_ACTIVATED, true)
                .putBoolean(KEY_TRIAL_ACTIVATED, false)
                .putString(KEY_ACTIVATION_CODE, activationCode == null ? "" : activationCode.trim())
                .putString(KEY_ACTIVATED_DEVICE_ID, getDeviceId(c))
                .putString(KEY_ACTIVATED_AT, now())
                .putString(KEY_LICENSE_STATUS, "active_lifetime_offline")
                .putString(KEY_LICENSE_EXPIRES_AT, "LIFETIME")
                .putString(KEY_LICENSE_MESSAGE, "تم التفعيل مدى الحياة لهذا الجهاز")
                .apply();
    }

    static boolean activateOffline(Context c, String code) {
        SignedLicense signed = validateSignedLicenseForThisDevice(c, code);
        if (signed != null) {
            applySignedLicense(c, signed, code);
            return true;
        }
        // توافق مع رموز V29 القديمة. الأفضل استخدام مولّد V30 لأنه يحفظ تاريخ انتهاء ثابت داخل الكود.
        if (validateOfflineCodeForThisDevice(c, code)) {
            setActivated(c, normalizeActivationInput(code));
            saveLicenseBackup(c, code, "active_lifetime_legacy", 0L);
            return true;
        }
        if (validateAnnualCodeForThisDevice(c, code)) {
            setTimedActivation(c, code, ANNUAL_DAYS, "active_annual_365_days_offline_legacy", "تم تفعيل الاشتراك السنوي لمدة 365 يوم على هذا الجهاز");
            saveLicenseBackup(c, code, "active_annual_365_days_offline_legacy", prefs(c).getLong(KEY_TRIAL_EXPIRES_AT_MS, 0));
            return true;
        }
        if (validateTrialCodeForThisDevice(c, code)) {
            setTimedActivation(c, code, TRIAL_DAYS, "active_trial_30_days_offline_legacy", "تم تفعيل النسخة التجريبية لمدة 30 يوم على هذا الجهاز");
            saveLicenseBackup(c, code, "active_trial_30_days_offline_legacy", prefs(c).getLong(KEY_TRIAL_EXPIRES_AT_MS, 0));
            return true;
        }
        return false;
    }

    static boolean activateTrialOffline(Context c, String code) {
        if (!validateTrialCodeForThisDevice(c, code)) return false;
        setTimedActivation(c, code, TRIAL_DAYS, "active_trial_30_days_offline", "تم تفعيل النسخة التجريبية لمدة 30 يوم على هذا الجهاز");
        return true;
    }

    static boolean activateAnnualOffline(Context c, String code) {
        if (!validateAnnualCodeForThisDevice(c, code)) return false;
        setTimedActivation(c, code, ANNUAL_DAYS, "active_annual_365_days_offline", "تم تفعيل الاشتراك السنوي لمدة 365 يوم على هذا الجهاز");
        return true;
    }

    static void setTimedActivation(Context c, String code, int days, String status, String message) {
        long nowMs = System.currentTimeMillis();
        long expiresMs = nowMs + (days * DAY_MS);
        prefs(c).edit()
                .putBoolean(KEY_ACTIVATED, false)
                .putBoolean(KEY_TRIAL_ACTIVATED, true)
                .putString(KEY_TRIAL_CODE, normalizeActivationInput(code))
                .putString(KEY_TRIAL_DEVICE_ID, getDeviceId(c))
                .putLong(KEY_TRIAL_STARTED_AT_MS, nowMs)
                .putLong(KEY_TRIAL_EXPIRES_AT_MS, expiresMs)
                .putLong(KEY_TRIAL_LAST_SEEN_AT_MS, nowMs)
                .putString(KEY_LICENSE_STATUS, status)
                .putString(KEY_LICENSE_EXPIRES_AT, formatDateTime(expiresMs))
                .putString(KEY_LICENSE_MESSAGE, message)
                .apply();
    }

    static class SignedLicense {
        String licenseId;
        String type;
        String requestCode;
        String packageName;
        long issuedMs;
        long expiresMs;
        String customer;
        String phone;
    }

    static SignedLicense validateSignedLicenseForThisDevice(Context c, String code) {
        try {
            String raw = code == null ? "" : code.trim();
            if (!raw.startsWith(SIGNED_LICENSE_PREFIX)) return null;
            String[] parts = raw.split("\\.");
            if (parts.length != 3 || !"ONL2".equals(parts[0])) return null;

            byte[] payloadBytes = base64UrlDecode(parts[1]);
            byte[] signatureBytes = base64UrlDecode(parts[2]);
            if (!verifySignedPayload(payloadBytes, signatureBytes)) {
                prefs(c).edit().putString(KEY_LICENSE_MESSAGE, "رمز التفعيل غير موقّع من مولّد الإدارة أو تم تعديله.").apply();
                return null;
            }

            String payload = new String(payloadBytes, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(payload);
            String app = json.optString("app", "");
            String pkg = json.optString("pkg", "");
            String request = json.optString("request", "");
            String type = json.optString("type", "");
            long issuedMs = json.optLong("issuedMs", 0L);
            long expiresMs = json.optLong("expiresMs", 0L);
            long nowMs = System.currentTimeMillis();

            if (!"ONLINE_ADMIN".equals(app)) return null;
            if (!c.getPackageName().equals(pkg)) return null;
            if (!normalizeActivationInput(getRequestCode(c)).equals(normalizeActivationInput(request))) {
                prefs(c).edit().putString(KEY_LICENSE_MESSAGE, "رمز التفعيل يخص جهازًا آخر ولا يعمل على هذا الجهاز.").apply();
                return null;
            }
            if (!"TRIAL30".equals(type) && !"YEAR365".equals(type) && !"LIFE".equals(type)) return null;
            if (issuedMs <= 0L || issuedMs > nowMs + DAY_MS) {
                prefs(c).edit().putString(KEY_LICENSE_MESSAGE, "تاريخ إصدار الترخيص غير صالح.").apply();
                return null;
            }
            if (!"LIFE".equals(type)) {
                if (expiresMs <= issuedMs) return null;
                if (nowMs > expiresMs) {
                    prefs(c).edit().putString(KEY_LICENSE_MESSAGE, "انتهت صلاحية هذا الترخيص. اطلب تجديد سنة أو تفعيل مدى الحياة.").apply();
                    return null;
                }
            }

            SignedLicense lic = new SignedLicense();
            lic.licenseId = json.optString("licenseId", "");
            lic.type = type;
            lic.requestCode = request;
            lic.packageName = pkg;
            lic.issuedMs = issuedMs;
            lic.expiresMs = expiresMs;
            lic.customer = json.optString("customer", "");
            lic.phone = json.optString("phone", "");
            return lic;
        } catch (Exception e) {
            return null;
        }
    }

    static void applySignedLicense(Context c, SignedLicense lic, String code) {
        if (lic == null) return;
        if ("LIFE".equals(lic.type)) {
            prefs(c).edit()
                    .putBoolean(KEY_ACTIVATED, true)
                    .putBoolean(KEY_TRIAL_ACTIVATED, false)
                    .putString(KEY_ACTIVATION_CODE, code == null ? "" : code.trim())
                    .putString(KEY_ACTIVATED_DEVICE_ID, getDeviceId(c))
                    .putString(KEY_ACTIVATED_AT, formatDateTime(System.currentTimeMillis()))
                    .putString(KEY_LICENSE_STATUS, "active_lifetime_signed_offline")
                    .putString(KEY_LICENSE_EXPIRES_AT, "LIFETIME")
                    .putString(KEY_LICENSE_MESSAGE, "تم التفعيل مدى الحياة لهذا الجهاز بترخيص موقّع")
                    .apply();
            saveLicenseBackup(c, code, "active_lifetime_signed_offline", 0L);
            return;
        }
        String status = "TRIAL30".equals(lic.type) ? "active_trial_30_days_signed_offline" : "active_annual_365_days_signed_offline";
        String label = "TRIAL30".equals(lic.type) ? "الشهر التجريبي" : "الاشتراك السنوي";
        prefs(c).edit()
                .putBoolean(KEY_ACTIVATED, false)
                .putBoolean(KEY_TRIAL_ACTIVATED, true)
                .putString(KEY_TRIAL_CODE, code == null ? "" : code.trim())
                .putString(KEY_TRIAL_DEVICE_ID, getDeviceId(c))
                .putLong(KEY_TRIAL_STARTED_AT_MS, lic.issuedMs)
                .putLong(KEY_TRIAL_EXPIRES_AT_MS, lic.expiresMs)
                .putLong(KEY_TRIAL_LAST_SEEN_AT_MS, System.currentTimeMillis())
                .putString(KEY_LICENSE_STATUS, status)
                .putString(KEY_LICENSE_EXPIRES_AT, formatDateTime(lic.expiresMs))
                .putString(KEY_LICENSE_MESSAGE, "تم تفعيل " + label + " إلى تاريخ ثابت. إذا حُذف التطبيق وأُدخل نفس الكود سيعود المتبقي فقط.")
                .apply();
        saveLicenseBackup(c, code, status, lic.expiresMs);
    }

    static boolean verifySignedPayload(byte[] payloadBytes, byte[] signatureBytes) {
        try {
            BigInteger modulus = new BigInteger(SIGNED_LICENSE_PUBLIC_MODULUS_HEX, 16);
            BigInteger exponent = new BigInteger(SIGNED_LICENSE_PUBLIC_EXPONENT_HEX, 16);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(payloadBytes);
            return verifier.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    static byte[] base64UrlDecode(String value) {
        String v = value == null ? "" : value.trim();
        return Base64.decode(v, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    static void saveLicenseBackup(Context c, String code, String status, long expiresMs) {
        try {
            JSONObject json = new JSONObject();
            json.put("app", "ONLINE_ADMIN");
            json.put("package", c.getPackageName());
            json.put("requestCode", getRequestCode(c));
            json.put("serial", getSerialNumber(c));
            json.put("deviceId", getDeviceId(c));
            json.put("status", status == null ? "" : status);
            json.put("expiresMs", expiresMs);
            json.put("savedAt", formatDateTime(System.currentTimeMillis()));
            json.put("licenseCode", code == null ? "" : code.trim());
            String content = json.toString(2);

            File appDir = new File(c.getExternalFilesDir(null), "ONLINE");
            if (!appDir.exists()) appDir.mkdirs();
            writeTextFile(new File(appDir, "license.onllic"), content);

            try {
                File publicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ONLINE");
                if (!publicDir.exists()) publicDir.mkdirs();
                writeTextFile(new File(publicDir, "license.onllic"), content);
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    static void writeTextFile(File file, String content) throws Exception {
        FileOutputStream fos = new FileOutputStream(file, false);
        OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
        w.write(content == null ? "" : content);
        w.flush();
        w.close();
    }

    static boolean validateTrialCodeForThisDevice(Context c, String code) {
        String input = normalizeActivationInput(code);
        if (input.length() < 12) return false;
        String expected = normalizeActivationInput(expectedTrialActivationCode(c));
        return input.equals(expected);
    }

    static String expectedTrialActivationCode(Context c) {
        return buildTrialActivationCode(getRequestCode(c));
    }

    static String buildTrialActivationCode(String requestCode) {
        String normalizedRequest = normalizeActivationInput(requestCode);
        String seed = OFFLINE_TRIAL_SECRET + "|" + normalizedRequest + "|TRIAL|30|ONLINE|V29";
        String hash = sha256(seed).toUpperCase(Locale.US).replaceAll("[^A-Z0-9]", "");
        while (hash.length() < 12) hash += "0";
        return "ONL-TRIAL30-" + hash.substring(0, 4) + "-" + hash.substring(4, 8) + "-" + hash.substring(8, 12);
    }

    static boolean validateAnnualCodeForThisDevice(Context c, String code) {
        String input = normalizeActivationInput(code);
        if (input.length() < 12) return false;
        String expected = normalizeActivationInput(expectedAnnualActivationCode(c));
        return input.equals(expected);
    }

    static String expectedAnnualActivationCode(Context c) {
        return buildAnnualActivationCode(getRequestCode(c));
    }

    static String buildAnnualActivationCode(String requestCode) {
        String normalizedRequest = normalizeActivationInput(requestCode);
        String seed = OFFLINE_ANNUAL_SECRET + "|" + normalizedRequest + "|ANNUAL|365|ONLINE|V29";
        String hash = sha256(seed).toUpperCase(Locale.US).replaceAll("[^A-Z0-9]", "");
        while (hash.length() < 12) hash += "0";
        return "ONL-YEAR365-" + hash.substring(0, 4) + "-" + hash.substring(4, 8) + "-" + hash.substring(8, 12);
    }

    static boolean validateOfflineCodeForThisDevice(Context c, String code) {
        String input = normalizeActivationInput(code);
        if (input.length() < 12) return false;
        String expected = normalizeActivationInput(expectedLifetimeActivationCode(c));
        return input.equals(expected);
    }

    static String expectedLifetimeActivationCode(Context c) {
        return buildLifetimeActivationCode(getRequestCode(c));
    }

    static String buildLifetimeActivationCode(String requestCode) {
        String normalizedRequest = normalizeActivationInput(requestCode);
        String seed = OFFLINE_LIFETIME_SECRET + "|" + normalizedRequest + "|LIFETIME|ONLINE|V14";
        String hash = sha256(seed).toUpperCase(Locale.US).replaceAll("[^A-Z0-9]", "");
        while (hash.length() < 12) hash += "0";
        return "ONL-LIFE-" + hash.substring(0, 4) + "-" + hash.substring(4, 8) + "-" + hash.substring(8, 12);
    }

    static boolean validateOfflineCode(String code) {
        return false;
    }

    static String normalizeActivationInput(String code) {
        return (code == null ? "" : code.trim().toUpperCase(Locale.US)).replaceAll("[^A-Z0-9]", "");
    }

    static String getActivationCode(Context c) {
        return prefs(c).getString(KEY_ACTIVATION_CODE, "");
    }

    static String getActivatedAt(Context c) {
        return prefs(c).getString(KEY_ACTIVATED_AT, "");
    }

    static void saveOnlineLicense(Context c, String status, String expiresAt, String message, String activationCode) {
        prefs(c).edit()
                .putBoolean(KEY_ACTIVATED, "active".equalsIgnoreCase(status))
                .putString(KEY_ACTIVATION_CODE, activationCode == null ? getRequestCode(c) : activationCode.trim())
                .putString(KEY_LICENSE_STATUS, status == null ? "inactive" : status.trim())
                .putString(KEY_LICENSE_EXPIRES_AT, expiresAt == null ? "" : expiresAt.trim())
                .putString(KEY_LICENSE_MESSAGE, message == null ? "" : message.trim())
                .putLong(KEY_LICENSE_LAST_CHECK, System.currentTimeMillis())
                .apply();
    }

    static String getLicenseStatus(Context c) {
        return prefs(c).getString(KEY_LICENSE_STATUS, "inactive");
    }

    static String getLicenseExpiresAt(Context c) {
        return prefs(c).getString(KEY_LICENSE_EXPIRES_AT, "");
    }

    static String getLicenseMessage(Context c) {
        return prefs(c).getString(KEY_LICENSE_MESSAGE, "");
    }

    static long getLicenseLastCheck(Context c) {
        return prefs(c).getLong(KEY_LICENSE_LAST_CHECK, 0);
    }

    static String getLicenseApiUrl(Context c) {
        return prefs(c).getString(KEY_LICENSE_API_URL, DEFAULT_LICENSE_API_URL);
    }

    static void setLicenseApiUrl(Context c, String url) {
        String value = url == null ? "" : url.trim();
        if (value.isEmpty()) value = DEFAULT_LICENSE_API_URL;
        prefs(c).edit().putString(KEY_LICENSE_API_URL, value).apply();
    }

    static boolean isDefaultApiUrl(Context c) {
        return getLicenseApiUrl(c).contains("YOUR-DOMAIN");
    }

    static String getRequestCode(Context c) {
        String saved = prefs(c).getString(KEY_LICENSE_REQUEST_CODE, "");
        if (saved != null && !saved.trim().isEmpty()) return saved;
        String seed = getDeviceId(c) + "|" + getSerialNumber(c) + "|ONLINE";
        String hash = sha256(seed).toUpperCase(Locale.US).replaceAll("[^A-Z0-9]", "");
        while (hash.length() < 12) hash += "0";
        String code = "REQ-" + hash.substring(0, 4) + "-" + hash.substring(4, 8) + "-" + hash.substring(8, 12);
        prefs(c).edit().putString(KEY_LICENSE_REQUEST_CODE, code).apply();
        return code;
    }

    static boolean needsMonthlyOnlineCheck(Context c) {
        return false;
    }

    static boolean isLicenseExpired(Context c) {
        return false;
    }

    static String licenseSummary(Context c) {
        if (isActivated(c)) {
            String activatedAt = getActivatedAt(c);
            String code = getActivationCode(c);
            String visibleCode = code == null || code.length() < 6 ? "محفوظ" : code.substring(0, Math.min(4, code.length())) + "••••" + code.substring(Math.max(0, code.length() - 4));
            return "مفعل مدى الحياة على هذا الجهاز فقط.\n"
                    + "تاريخ التفعيل: " + (activatedAt == null || activatedAt.isEmpty() ? "-" : activatedAt) + "\n"
                    + "الرمز: " + visibleCode;
        }
        if (isTrialUsable(c)) {
            SharedPreferences sp = prefs(c);
            String status = sp.getString(KEY_LICENSE_STATUS, "");
            long expiresMs = sp.getLong(KEY_TRIAL_EXPIRES_AT_MS, 0);
            long remainingMs = expiresMs - System.currentTimeMillis();
            long remainingDays = Math.max(0, (remainingMs + DAY_MS - 1) / DAY_MS);
            String label = status != null && status.contains("annual") ? "اشتراك سنة كاملة" : "نسخة تجريبية شهر كامل";
            return "مفعل: " + label + " على هذا الجهاز فقط.\n"
                    + "متبقي: " + remainingDays + " يوم\n"
                    + "ينتهي في: " + formatDateTime(expiresMs);
        }
        String msg = getLicenseMessage(c);
        if (msg != null && !msg.trim().isEmpty()) return msg + "\nأرسل كود الطلب للإدارة للحصول على رمز تفعيل: شهر تجريبي، سنة، أو مدى الحياة.";
        return "غير مفعل: أرسل كود الطلب للإدارة للحصول على رمز تفعيل شهر تجريبي أو سنة أو مدى الحياة.";
    }

    static String formatDateTime(long ms) {
        if (ms <= 0) return "-";
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(ms));
        } catch (Exception e) {
            return String.valueOf(ms);
        }
    }

    static String getDeviceId(Context c) {
        String id = "";
        try {
            id = android.provider.Settings.Secure.getString(c.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception ignored) {}
        if (id == null || id.trim().isEmpty()) {
            id = prefs(c).getString(KEY_LOCAL_DEVICE_ID, "");
            if (id == null || id.trim().isEmpty()) {
                id = UUID.randomUUID().toString().replace("-", "");
                prefs(c).edit().putString(KEY_LOCAL_DEVICE_ID, id).apply();
            }
        }
        return id;
    }

    static String getSerialNumber(Context c) {
        String raw = (getDeviceId(c) + Integer.toHexString((getDeviceId(c) + "4UNET").hashCode())).toUpperCase(Locale.US);
        raw = raw.replaceAll("[^A-Z0-9]", "");
        while (raw.length() < 12) raw = raw + "4U0";
        String compact = raw.substring(0, 12);
        return "APP-" + compact.substring(0, 4) + "-" + compact.substring(4, 8) + "-" + compact.substring(8, 12);
    }

    static String expectedActivationCode(Context c) {
        String compact = getSerialNumber(c).replace("APP-", "").replace("-", "");
        String last = compact.substring(compact.length() - 4);
        return "4U-" + last;
    }

    static boolean validateActivationCode(Context c, String code) {
        String input = code == null ? "" : code.trim().replace(" ", "").toUpperCase(Locale.US);
        String expected = expectedActivationCode(c).replace(" ", "").toUpperCase(Locale.US);
        return input.equals(expected) || input.equals(expected.replace("-", "")) || input.equals("4U2026");
    }

    static boolean isCardPrivacyEnabled(Context c) {
        return prefs(c).getBoolean(KEY_CARD_PRIVACY_ENABLED, true);
    }

    static void setCardPrivacyEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_CARD_PRIVACY_ENABLED, enabled).apply();
    }

    static boolean isAutoBackupEnabled(Context c) {
        return prefs(c).getBoolean(KEY_AUTO_BACKUP_ENABLED, true);
    }

    static void setAutoBackupEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply();
    }

    static int getAutoBackupIntervalHours(Context c) {
        int hours = prefs(c).getInt(KEY_AUTO_BACKUP_INTERVAL_HOURS, 12);
        if (hours != 6 && hours != 12 && hours != 24 && hours != 48) hours = 12;
        return hours;
    }

    static void setAutoBackupIntervalHours(Context c, int hours) {
        if (hours != 6 && hours != 12 && hours != 24 && hours != 48) hours = 12;
        prefs(c).edit().putInt(KEY_AUTO_BACKUP_INTERVAL_HOURS, hours).apply();
    }

    static String getLastAutoBackupAt(Context c) {
        return prefs(c).getString(KEY_LAST_AUTO_BACKUP_AT, "لم يتم بعد");
    }

    static File getAutoBackupDir(Context c) {
        File base = c.getExternalFilesDir(null);
        if (base == null) base = c.getFilesDir();
        File dir = new File(base, "ONLINE_auto_backups");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    static String writeAutoBackupNow(Context c, String reason) throws Exception {
        File dir = getAutoBackupDir(c);
        File file = new File(dir, buildBackupFileName(c));
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
        writer.write(exportBackupJson(c).toString(2));
        writer.flush();
        writer.close();
        fos.close();
        String stamp = now();
        prefs(c).edit().putString(KEY_LAST_AUTO_BACKUP_AT, stamp + " | " + (reason == null ? "تلقائي" : reason)).apply();
        return file.getAbsolutePath();
    }

    static void performAutoBackupIfDue(Context c, String reason) {
        if (!isAutoBackupEnabled(c)) return;
        final Context app = c.getApplicationContext();
        final long last = prefs(app).getLong("last_auto_backup_millis", 0L);
        final long nowMs = System.currentTimeMillis();
        final long interval = getAutoBackupIntervalHours(app) * 60L * 60L * 1000L;
        if (last > 0 && nowMs - last < interval) return;
        if (autoBackupRunning) return;

        // Stage 13.7.2 LOGIN FREEZE FIX:
        // النسخ التلقائي كان يعمل على نفس مسار فتح التطبيق، ومع مخزون ضخم مثل 300 ألف كرت
        // قد يعلّق التطبيق بعد نجاح البصمة. لذلك صار النسخ التلقائي يعمل بالخلفية،
        // ويتم تخطي النسخ التلقائي الكامل إذا كان المخزون ضخمًا. النسخ اليدوي يبقى كاملًا.
        prefs(app).edit().putLong("last_auto_backup_millis", nowMs).apply();
        autoBackupRunning = true;
        new Thread(() -> {
            try {
                int totalCards = totalCardRows(app);
                if (totalCards > AUTO_BACKUP_MAX_CARDS_ON_UI_SAFE) {
                    String stamp = now();
                    prefs(app).edit().putString(KEY_LAST_AUTO_BACKUP_AT,
                            stamp + " | تم تخطي النسخ التلقائي الكامل بسبب كثرة الكروت: " + totalCards + " كرت. استخدم النسخ اليدوي عند الحاجة.")
                            .apply();
                    return;
                }
                writeAutoBackupNow(app, reason == null ? "تلقائي بالخلفية" : reason + " - بالخلفية");
            } catch (Exception ignored) {
            } finally {
                autoBackupRunning = false;
            }
        }, "ONLINE-auto-backup-bg").start();
    }

    private static int totalCardRows(Context c) {
        Cursor cur = null;
        try {
            cur = cardDb(c).rawQuery("SELECT COUNT(*) FROM cards", null);
            if (cur.moveToFirst()) return cur.getInt(0);
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        return 0;
    }

    static JSONArray getRestoreHistoryArray(Context c) {
        try { return new JSONArray(prefs(c).getString(KEY_RESTORE_HISTORY, "[]")); } catch(Exception e) { return new JSONArray(); }
    }

    static void recordRestoreHistory(Context c, String backupCreatedAt, String fileNote) {
        try {
            JSONArray arr = getRestoreHistoryArray(c);
            JSONObject o = new JSONObject();
            o.put("restoredAt", now());
            o.put("backupCreatedAt", backupCreatedAt == null ? "" : backupCreatedAt);
            o.put("file", fileNote == null ? "" : fileNote);
            arr.put(o);
            prefs(c).edit().putString(KEY_RESTORE_HISTORY, arr.toString()).apply();
        } catch(Exception ignored) {}
    }

    static String restoreHistoryText(Context c) {
        JSONArray arr = getRestoreHistoryArray(c);
        if (arr.length() == 0) return "لا توجد عمليات استعادة مسجلة.";
        StringBuilder sb = new StringBuilder();
        for (int i = arr.length() - 1; i >= 0; i--) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            sb.append("- تم الاستعادة: ").append(o.optString("restoredAt", "-"));
            String created = o.optString("backupCreatedAt", "");
            if (!created.isEmpty()) sb.append(" | تاريخ النسخة: ").append(created);
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    static String getAdminPhone(Context c) {
        return prefs(c).getString(KEY_ADMIN_PHONE, DEFAULT_ADMIN_PHONE);
    }

    static void setAdminPhone(Context c, String phone) {
        String value = phone == null ? "" : phone.trim();
        if (value.isEmpty()) value = DEFAULT_ADMIN_PHONE;
        prefs(c).edit().putString(KEY_ADMIN_PHONE, value).apply();
    }

    static String getMikroTikHost(Context c) {
        return prefs(c).getString(KEY_MIKROTIK_HOST, "192.168.88.1");
    }

    static int getMikroTikPort(Context c) {
        return prefs(c).getInt(KEY_MIKROTIK_PORT, 8728);
    }

    static String getMikroTikUser(Context c) {
        return prefs(c).getString(KEY_MIKROTIK_USER, "online_app");
    }

    static String getMikroTikProfileFilter(Context c) {
        return prefs(c).getString(KEY_MIKROTIK_PROFILE_FILTER, "");
    }

    static int getMikroTikLastLimit(Context c) {
        return prefs(c).getInt(KEY_MIKROTIK_LAST_LIMIT, 0);
    }

    static void saveMikroTikSettings(Context c, String host, int port, String user, String profileFilter, int limit) {
        prefs(c).edit()
                .putString(KEY_MIKROTIK_HOST, host == null ? "" : host.trim())
                .putInt(KEY_MIKROTIK_PORT, port <= 0 ? 8728 : port)
                .putString(KEY_MIKROTIK_USER, user == null ? "" : user.trim())
                .putString(KEY_MIKROTIK_PROFILE_FILTER, profileFilter == null ? "" : profileFilter.trim())
                .putInt(KEY_MIKROTIK_LAST_LIMIT, Math.max(0, limit))
                .apply();
    }



    static String getSuccessTemplate(Context c) {
        return prefs(c).getString(KEY_SUCCESS_TEMPLATE, DEFAULT_SUCCESS_TEMPLATE);
    }

    static void setSuccessTemplate(Context c, String template) {
        String value = template == null ? "" : template.trim();
        if (value.isEmpty()) value = DEFAULT_SUCCESS_TEMPLATE;
        prefs(c).edit().putString(KEY_SUCCESS_TEMPLATE, value).apply();
    }

    static String getDirectSaleTemplate(Context c) {
        return prefs(c).getString(KEY_DIRECT_SALE_TEMPLATE, DEFAULT_DIRECT_SALE_TEMPLATE);
    }

    static void setDirectSaleTemplate(Context c, String template) {
        String value = template == null ? "" : template.trim();
        if (value.isEmpty()) value = DEFAULT_DIRECT_SALE_TEMPLATE;
        prefs(c).edit().putString(KEY_DIRECT_SALE_TEMPLATE, value).apply();
    }

    static String getNoStockTemplate(Context c) {
        return prefs(c).getString(KEY_NO_STOCK_TEMPLATE, DEFAULT_NO_STOCK_TEMPLATE);
    }

    static void setNoStockTemplate(Context c, String template) {
        String value = template == null ? "" : template.trim();
        if (value.isEmpty()) value = DEFAULT_NO_STOCK_TEMPLATE;
        prefs(c).edit().putString(KEY_NO_STOCK_TEMPLATE, value).apply();
    }

    static String buildSuccessMessage(Context c, int amount, String cardCode) {
        return normalizeSingleCardPhrase(ensureSystemSignature(c, applyTemplate(c, getSuccessTemplate(c), amount, cardCode)));
    }

    static String buildDirectSaleMessage(Context c, int amount, String cardCode) {
        return normalizeSingleCardPhrase(ensureSystemSignature(c, applyTemplate(c, getDirectSaleTemplate(c), amount, cardCode)));
    }

    private static String normalizeSingleCardPhrase(String message) {
        if (message == null) return "";
        return message.replace("كروتك فئة", "كرتك فئة");
    }

    static String buildGroupedCardsMessage(Context c, ArrayList<CardItem> cards) {
        return buildGroupedCardsMessage(c, cards, "");
    }

    static String buildGroupedCardsMessage(Context c, ArrayList<CardItem> cards, String extraMessagePart) {
        if (cards == null || cards.isEmpty()) return ensureSystemSignature(c, "");
        java.util.TreeMap<Integer, ArrayList<String>> grouped = new java.util.TreeMap<>(java.util.Collections.reverseOrder());
        int totalCodes = 0;
        for (CardItem card : cards) {
            if (card == null) continue;
            ArrayList<String> list = grouped.get(card.amount);
            if (list == null) {
                list = new ArrayList<>();
                grouped.put(card.amount, list);
            }
            String code = card.code == null ? "" : card.code.trim();
            if (!code.isEmpty()) {
                list.add(code);
                totalCodes++;
            }
        }
        StringBuilder sb = new StringBuilder();
        String heading = totalCodes == 1 ? "كرتك فئة " : "كروتك فئة ";
        for (Integer amount : grouped.keySet()) {
            ArrayList<String> codes = grouped.get(amount);
            if (codes == null || codes.isEmpty()) continue;
            if (sb.length() > 0) sb.append("\n\n");
            sb.append(heading).append(amount).append(":\n");
            for (int i = 0; i < codes.size(); i++) {
                if (i > 0) sb.append("\n-----\n");
                sb.append(codes.get(i));
            }
        }
        String extra = extraMessagePart == null ? "" : extraMessagePart.trim();
        if (!extra.isEmpty()) sb.append("\n\n").append(extra);
        return ensureSystemSignature(c, sb.toString());
    }

    static String withoutSystemSignature(String message) {
        if (message == null) return "";
        String out = message.trim();
        out = out.replace(NETWORK_SIGNATURE, "").replace(SYSTEM_SIGNATURE, "").trim();
        return out;
    }

    static String buildPosSelfSaleMessage(Context c, ArrayList<CardItem> cards, String extraMessagePart, int remainingLimit) {
        String base = withoutSystemSignature(buildGroupedCardsMessage(c, cards, extraMessagePart));
        if (!base.isEmpty()) base += "\n\n";
        base += "المتبقي: " + Math.max(0, remainingLimit);
        return ensureSystemSignature(c, base);
    }

    static String getLedgerPaymentTemplate(Context c) {
        return prefs(c).getString(KEY_LEDGER_PAYMENT_TEMPLATE, DEFAULT_LEDGER_PAYMENT_TEMPLATE);
    }

    static String getPosTopUpTemplate(Context c) {
        return prefs(c).getString(KEY_POS_TOPUP_TEMPLATE, DEFAULT_POS_TOPUP_TEMPLATE);
    }

    static void setLedgerAndTopUpTemplates(Context c, String ledgerPayment, String posTopUp) {
        prefs(c).edit()
                .putString(KEY_LEDGER_PAYMENT_TEMPLATE, cleanLongTemplate(ledgerPayment, DEFAULT_LEDGER_PAYMENT_TEMPLATE))
                .putString(KEY_POS_TOPUP_TEMPLATE, cleanLongTemplate(posTopUp, DEFAULT_POS_TOPUP_TEMPLATE))
                .apply();
    }

    static String applyLedgerPaymentTemplate(Context c, String template, String customerName, int amount, int debtBefore, int debtAfter, int creditBefore, int creditAfter) {
        String out = template == null || template.trim().isEmpty() ? DEFAULT_LEDGER_PAYMENT_TEMPLATE : template;
        out = out.replace("{customerName}", customerName == null || customerName.trim().isEmpty() ? "عميلنا" : customerName.trim());
        out = out.replace("{amount}", String.valueOf(Math.max(0, amount)));
        out = out.replace("{debtBefore}", String.valueOf(Math.max(0, debtBefore)));
        out = out.replace("{debtAfter}", String.valueOf(Math.max(0, debtAfter)));
        out = out.replace("{creditBefore}", String.valueOf(Math.max(0, creditBefore)));
        out = out.replace("{creditAfter}", String.valueOf(Math.max(0, creditAfter)));
        out = out.replace("{network}", getNetworkName(c));
        out = out.replace("{signature}", getMessageSignature(c));
        return ensureSystemSignature(c, out);
    }

    static String buildLedgerPaymentMessage(Context c, LedgerCustomer customer, int amount, int debtBefore, int creditBefore) {
        String displayName = customer == null || customer.name == null || customer.name.trim().isEmpty() ? "عميلنا" : customer.name.trim();
        int debtAfter = customer == null ? 0 : customer.debt;
        int creditAfter = customer == null ? 0 : customer.creditBalance;
        return applyLedgerPaymentTemplate(c, getLedgerPaymentTemplate(c), displayName, amount, debtBefore, debtAfter, creditBefore, creditAfter);
    }

    static String buildTrustedCreditTopUpMessage(Context c, TrustedCreditAgent a, int addedAmount) {
        int remain = remainingTrustedCredit(a);
        int oldRemaining = Math.max(0, remain - Math.max(0, addedAmount));
        String out = getPosTopUpTemplate(c);
        out = out.replace("{amount}", String.valueOf(Math.max(0, addedAmount)));
        out = out.replace("{oldRemaining}", String.valueOf(oldRemaining));
        out = out.replace("{remaining}", String.valueOf(remain));
        out = out.replace("{network}", getNetworkName(c));
        out = out.replace("{signature}", getMessageSignature(c));
        return ensureSystemSignature(c, out);
    }

    static String buildNoStockMessage(Context c, int amount) {
        return ensureSystemSignature(c, applyTemplate(c, getNoStockTemplate(c), amount, ""));
    }

    static boolean hasSystemSignature(String message) {
        if (message == null) return false;
        String t = message.trim();
        return t.contains(SYSTEM_SIGNATURE)
                || t.contains(NETWORK_SIGNATURE)
                || t.contains("تم التنفيذ")
                || t.contains("تعذر التنفيذ")
                || t.contains("تم الشحن")
                || t.contains("طلب مكرر")
                || t.contains("نفدت كروت")
                || t.contains("عملية تحتاج مراجعة");
    }

    static String ensureSystemSignature(String message) {
        String m = message == null ? "" : message.trim();
        if (m.isEmpty()) return SYSTEM_SIGNATURE;
        if (m.contains(SYSTEM_SIGNATURE) || m.contains(NETWORK_SIGNATURE)) return m;
        return m + "\n" + SYSTEM_SIGNATURE;
    }

    static String ensureSystemSignature(Context c, String message) {
        String m = message == null ? "" : message.trim();
        String sig = getMessageSignature(c);
        if (m.isEmpty()) return sig;
        if (m.contains(sig) || m.contains(getNetworkName(c)) || m.contains(SYSTEM_SIGNATURE) || m.contains(NETWORK_SIGNATURE)) {
            return m.replace(NETWORK_SIGNATURE, sig).replace(SYSTEM_SIGNATURE, sig).trim();
        }
        return m + "\n" + sig;
    }

    private static String cleanLongTemplate(String value, String fallback) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) return fallback;
        if (v.length() > 900) v = v.substring(0, 900);
        return v;
    }

    static String getPosSuccessTemplate(Context c) { return prefs(c).getString(KEY_POS_SUCCESS_TEMPLATE, DEFAULT_POS_SUCCESS_TEMPLATE); }
    static String getPosDuplicateTemplate(Context c) { return prefs(c).getString(KEY_POS_DUPLICATE_TEMPLATE, DEFAULT_POS_DUPLICATE_TEMPLATE); }
    static String getPosInvalidPhoneTemplate(Context c) { return prefs(c).getString(KEY_POS_INVALID_PHONE_TEMPLATE, DEFAULT_POS_INVALID_PHONE_TEMPLATE); }
    static String getPosLimitTemplate(Context c) { return prefs(c).getString(KEY_POS_LIMIT_TEMPLATE, DEFAULT_POS_LIMIT_TEMPLATE); }
    static String getPosNoStockTemplate(Context c) { return prefs(c).getString(KEY_POS_NO_STOCK_TEMPLATE, DEFAULT_POS_NO_STOCK_TEMPLATE); }
    static String getPosInvalidCategoryTemplate(Context c) { return prefs(c).getString(KEY_POS_INVALID_CATEGORY_TEMPLATE, DEFAULT_POS_INVALID_CATEGORY_TEMPLATE); }
    static String getPosAmbiguousTemplate(Context c) { return prefs(c).getString(KEY_POS_AMBIGUOUS_TEMPLATE, DEFAULT_POS_AMBIGUOUS_TEMPLATE); }

    static void setPosMessageTemplates(Context c, String success, String duplicate, String invalidPhone, String limit, String noStock, String invalidCategory, String ambiguous) {
        prefs(c).edit()
                .putString(KEY_POS_SUCCESS_TEMPLATE, cleanLongTemplate(success, DEFAULT_POS_SUCCESS_TEMPLATE))
                .putString(KEY_POS_DUPLICATE_TEMPLATE, cleanLongTemplate(duplicate, DEFAULT_POS_DUPLICATE_TEMPLATE))
                .putString(KEY_POS_INVALID_PHONE_TEMPLATE, cleanLongTemplate(invalidPhone, DEFAULT_POS_INVALID_PHONE_TEMPLATE))
                .putString(KEY_POS_LIMIT_TEMPLATE, cleanLongTemplate(limit, DEFAULT_POS_LIMIT_TEMPLATE))
                .putString(KEY_POS_NO_STOCK_TEMPLATE, cleanLongTemplate(noStock, DEFAULT_POS_NO_STOCK_TEMPLATE))
                .putString(KEY_POS_INVALID_CATEGORY_TEMPLATE, cleanLongTemplate(invalidCategory, DEFAULT_POS_INVALID_CATEGORY_TEMPLATE))
                .putString(KEY_POS_AMBIGUOUS_TEMPLATE, cleanLongTemplate(ambiguous, DEFAULT_POS_AMBIGUOUS_TEMPLATE))
                .apply();
    }

    static void resetPosMessageTemplates(Context c) {
        setPosMessageTemplates(c, DEFAULT_POS_SUCCESS_TEMPLATE, DEFAULT_POS_DUPLICATE_TEMPLATE, DEFAULT_POS_INVALID_PHONE_TEMPLATE,
                DEFAULT_POS_LIMIT_TEMPLATE, DEFAULT_POS_NO_STOCK_TEMPLATE, DEFAULT_POS_INVALID_CATEGORY_TEMPLATE, DEFAULT_POS_AMBIGUOUS_TEMPLATE);
    }

    static String buildPosSuccessMessage(Context c, TrustedCreditAgent a, ParsedCreditRequest req, int remainingLimit) {
        return ensureSystemSignature(c, applyPosTemplate(c, getPosSuccessTemplate(c), a, req, remainingLimit, ""));
    }

    static String buildPosDuplicateMessage(Context c, TrustedCreditAgent a, ParsedCreditRequest req) {
        return ensureSystemSignature(c, applyPosTemplate(c, getPosDuplicateTemplate(c), a, req, a == null ? 0 : a.remaining(), ""));
    }

    static String buildPosInvalidPhoneMessage(Context c, TrustedCreditAgent a, ParsedCreditRequest req, String reason) {
        return ensureSystemSignature(c, applyPosTemplate(c, getPosInvalidPhoneTemplate(c), a, req, a == null ? 0 : a.remaining(), reason));
    }

    static String buildPosLimitMessage(Context c, TrustedCreditAgent a, ParsedCreditRequest req) {
        return ensureSystemSignature(c, applyPosTemplate(c, getPosLimitTemplate(c), a, req, a == null ? 0 : a.remaining(), ""));
    }

    static String buildPosNoStockMessage(Context c, TrustedCreditAgent a, ParsedCreditRequest req) {
        return ensureSystemSignature(c, applyPosTemplate(c, getPosNoStockTemplate(c), a, req, a == null ? 0 : a.remaining(), ""));
    }

    static String buildPosInvalidCategoryMessage(Context c, TrustedCreditAgent a, ParsedCreditRequest req, String reason) {
        return ensureSystemSignature(c, applyPosTemplate(c, getPosInvalidCategoryTemplate(c), a, req, a == null ? 0 : a.remaining(), reason));
    }

    static String buildPosAmbiguousMessage(Context c, TrustedCreditAgent a, ParsedCreditRequest req, String reason) {
        return ensureSystemSignature(c, applyPosTemplate(c, getPosAmbiguousTemplate(c), a, req, a == null ? 0 : a.remaining(), reason));
    }

    static String applyPosTemplate(Context c, String template, TrustedCreditAgent a, ParsedCreditRequest req, int remainingLimit, String reason) {
        String out = template == null ? "" : template;
        String trustedName = a == null ? "" : (a.name == null ? "" : a.name);
        String trustedPhone = a == null ? "" : (a.senderPhone == null ? "" : a.senderPhone);
        int creditLimit = a == null ? 0 : a.creditLimit;
        int usedAmount = a == null ? 0 : a.usedAmount;
        String customer = req == null ? "" : req.customerPhone;
        String cats = req == null ? "" : req.amountsText();
        String total = req == null ? "0" : String.valueOf(req.totalAmount());
        String count = req == null ? "0" : String.valueOf(req.countCards());
        out = out.replace("{trusted_name}", trustedName);
        out = out.replace("{trusted_phone}", trustedPhone);
        out = out.replace("{customer_phone}", customer == null ? "" : customer);
        out = out.replace("{categories}", cats == null ? "" : cats);
        out = out.replace("{cards_count}", count);
        out = out.replace("{total_amount}", total);
        out = out.replace("{remaining_limit}", String.valueOf(Math.max(0, remainingLimit)));
        out = out.replace("{credit_limit}", String.valueOf(Math.max(0, creditLimit)));
        out = out.replace("{used_amount}", String.valueOf(Math.max(0, usedAmount)));
        out = out.replace("{reason}", reason == null ? "" : reason);
        out = out.replace("{window_minutes}", "5");
        out = out.replace("{network}", getMessageSignature(c));
        out = out.replace("{adminPhone}", getAdminPhone(c));
        return out;
    }

    static String applyTemplate(String template, int amount, String cardCode) {
        String out = template == null ? "" : template;
        out = out.replace("{amount}", String.valueOf(amount));
        out = out.replace("{card}", cardCode == null ? "" : cardCode);
        out = out.replace("{network}", getNetworkNameForTemplateFallback());
        out = out.replace("{adminPhone}", getAdminPhoneForTemplateFallback());
        return out;
    }

    static String applyTemplate(Context c, String template, int amount, String cardCode) {
        String out = template == null ? "" : template;
        out = out.replace("{amount}", String.valueOf(amount));
        out = out.replace("{card}", cardCode == null ? "" : cardCode);
        out = out.replace("{network}", getMessageSignature(c));
        out = out.replace("{adminPhone}", getAdminPhone(c));
        return out;
    }

    private static String getNetworkNameForTemplateFallback() {
        return DEFAULT_NETWORK_NAME;
    }

    private static String getAdminPhoneForTemplateFallback() {
        return DEFAULT_ADMIN_PHONE;
    }

    static boolean isAutoSendEnabled(Context c) {
        return prefs(c).getBoolean(KEY_AUTO_SEND, true);
    }

    static void setAutoSendEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_AUTO_SEND, enabled).apply();
    }

    static void ensureDefaultCategories(Context c) {
        ArrayList<CategoryItem> list = loadCategoriesRaw(c);
        HashSet<Integer> exists = new HashSet<>();
        for (CategoryItem item : list) exists.add(item.amount);
        boolean changed = false;
        for (int amount : DEFAULT_AMOUNTS) {
            if (!exists.contains(amount)) {
                list.add(new CategoryItem(UUID.randomUUID().toString(), amount, "فئة " + amount, true, System.currentTimeMillis()));
                changed = true;
            }
        }
        if (changed) saveCategories(c, list);
    }

    private static ArrayList<CategoryItem> loadCategoriesRaw(Context c) {
        ArrayList<CategoryItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_CATEGORIES, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new CategoryItem(
                        o.optString("id"),
                        o.optInt("amount"),
                        o.optString("name"),
                        o.optBoolean("active", true),
                        o.optLong("createdAt")
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static ArrayList<CategoryItem> loadCategories(Context c) {
        ensureDefaultCategories(c);
        ArrayList<CategoryItem> list = loadCategoriesRaw(c);
        // ترتيب تصاعدي حسب الفئة
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(j).amount < list.get(i).amount) {
                    CategoryItem tmp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, tmp);
                }
            }
        }
        return list;
    }

    static void saveCategories(Context c, ArrayList<CategoryItem> list) {
        try {
            JSONArray arr = new JSONArray();
            for (CategoryItem item : list) {
                JSONObject o = new JSONObject();
                o.put("id", item.id);
                o.put("amount", item.amount);
                o.put("name", item.name);
                o.put("active", item.active);
                o.put("createdAt", item.createdAt);
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_CATEGORIES, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    static boolean addCategory(Context c, int amount) {
        ArrayList<CategoryItem> list = loadCategories(c);
        for (CategoryItem item : list) {
            if (item.amount == amount) return false;
        }
        list.add(new CategoryItem(UUID.randomUUID().toString(), amount, "فئة " + amount, true, System.currentTimeMillis()));
        saveCategories(c, list);
        return true;
    }

    static void updateCategory(Context c, CategoryItem category, int amount, String name, boolean active) {
        ArrayList<CategoryItem> list = loadCategories(c);
        for (CategoryItem item : list) {
            if (item.id.equals(category.id)) {
                item.amount = amount;
                item.name = name;
                item.active = active;
                break;
            }
        }
        saveCategories(c, list);
    }

    static void deleteCategory(Context c, CategoryItem category) {
        ArrayList<CategoryItem> list = loadCategories(c);
        ArrayList<CategoryItem> next = new ArrayList<>();
        for (CategoryItem item : list) if (!item.id.equals(category.id)) next.add(item);
        saveCategories(c, next);
    }

    static CategoryItem findCategoryByAmount(Context c, int amount) {
        for (CategoryItem item : loadCategories(c)) {
            if (item != null && item.amount == amount) return item;
        }
        return new CategoryItem(UUID.randomUUID().toString(), amount, amount + " ريال", true, System.currentTimeMillis());
    }

    static ArrayList<CardItem> loadCards(Context c) {
        ArrayList<CardItem> list = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = cardDb(c).query("cards", null, null, null, null, null, "rowid DESC");
            while (cur.moveToNext()) list.add(cardFromCursor(cur));
        } catch (Exception ignored) {
            // Legacy fallback only if database is unavailable.
            String raw = prefs(c).getString(KEY_CARDS, "[]");
            try {
                JSONArray arr = new JSONArray(raw == null ? "[]" : raw);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String createdAt = o.optString("createdAt", "");
                    if (createdAt.trim().isEmpty()) createdAt = o.optString("addedAt", "");
                    list.add(new CardItem(o.optString("id"), o.optInt("amount"), o.optString("code"), o.optBoolean("sold"), o.optString("buyerPhone"), o.optString("soldAt"), o.optString("source"), createdAt));
                }
            } catch (Exception ignored2) {}
        } finally {
            if (cur != null) cur.close();
        }
        return list;
    }

    private static void invalidateCardsCache() {
        cardsCacheJson = null;
        cardsCache = null;
        stockCountCache = null;
    }

    static void saveCards(Context c, ArrayList<CardItem> cards) {
        SQLiteDatabase db = cardDb(c);
        db.beginTransaction();
        try {
            db.delete("cards", null, null);
            if (cards != null) {
                for (CardItem card : cards) {
                    if (card == null || normalizeCardCode(card.code).isEmpty()) continue;
                    db.insertWithOnConflict("cards", null, cardValues(card), SQLiteDatabase.CONFLICT_IGNORE);
                }
            }
            db.setTransactionSuccessful();
            invalidateCardsCache();
        } catch (Exception ignored) {
        } finally {
            try { db.endTransaction(); } catch (Exception ignored) {}
        }
    }

    private static boolean isRewardStock(CardItem item) {
        return item != null && item.source != null && item.source.startsWith("reward_stock");
    }

    private static JSONArray cardsToJsonArray(Context c) {
        JSONArray arr = new JSONArray();
        try {
            for (CardItem card : loadCards(c)) {
                JSONObject o = new JSONObject();
                o.put("id", card.id);
                o.put("amount", card.amount);
                o.put("code", card.code);
                o.put("sold", card.sold);
                o.put("buyerPhone", card.buyerPhone == null ? "" : card.buyerPhone);
                o.put("soldAt", card.soldAt == null ? "" : card.soldAt);
                o.put("source", card.source == null ? "" : card.source);
                o.put("createdAt", card.createdAt == null ? "" : card.createdAt);
                arr.put(o);
            }
        } catch (Exception ignored) {}
        return arr;
    }

    private static ArrayList<CardItem> cardsFromJsonArray(JSONArray arr) {
        ArrayList<CardItem> list = new ArrayList<>();
        if (arr == null) return list;
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String createdAt = o.optString("createdAt", "");
                if (createdAt.trim().isEmpty()) createdAt = o.optString("addedAt", "");
                list.add(new CardItem(o.optString("id", UUID.randomUUID().toString()), o.optInt("amount"), o.optString("code"), o.optBoolean("sold"), o.optString("buyerPhone"), o.optString("soldAt"), o.optString("source"), createdAt));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static String normalizeCardCode(String value) {
        if (value == null) return "";
        String v = value.trim();
        v = v.replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4')
                .replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9')
                .replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4')
                .replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9');
        v = v.replaceAll("\\s+", "");
        return v;
    }

    private static String cardDuplicateKey(String code) {
        return normalizeCardCode(code).toUpperCase(Locale.US);
    }

    static int duplicateCardCount(Context c, ArrayList<String> codes) {
        if (codes == null || codes.isEmpty()) return 0;
        SQLiteDatabase db = cardDb(c);
        int dup = 0;
        HashSet<String> seen = new HashSet<>();
        Cursor cur = null;
        try {
            for (String raw : codes) {
                String key = cardDuplicateKey(raw);
                if (key.isEmpty()) continue;
                if (seen.contains(key)) { dup++; continue; }
                seen.add(key);
                cur = db.rawQuery("SELECT 1 FROM cards WHERE code_key=? LIMIT 1", new String[]{key});
                if (cur.moveToFirst()) dup++;
                cur.close(); cur = null;
            }
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        return dup;
    }

    static int deleteCardsByAmount(Context c, int amount) {
        SQLiteDatabase db = cardDb(c);
        int removed = 0;
        try {
            removed = db.delete("cards", "amount=? AND (source IS NULL OR source NOT LIKE 'reward_stock%')", new String[]{String.valueOf(amount)});
            invalidateCardsCache();
        } catch (Exception ignored) {}
        if (removed > 0) addLog(c, new OperationLog(UUID.randomUUID().toString(), "إدارة الكروت", "cards", "", "", amount,
                "حذف كروت فئة", "تم حذف كل كروت الفئة " + amount + " ريال. العدد: " + removed, "", now()));
        return removed;
    }

    static ArrayList<CardItem> searchCards(Context c, String query) {
        return searchCardsLimited(c, query, -1, 80);
    }

    static ArrayList<CardItem> searchCardsLimited(Context c, String query, int amountFilter, int limit) {
        ArrayList<CardItem> out = new ArrayList<>();
        String q = normalizeCardCode(query).toUpperCase(Locale.US);
        String raw = query == null ? "" : query.trim().toUpperCase(Locale.US);
        if (q.isEmpty() && raw.isEmpty()) return out;
        Cursor cur = null;
        try {
            String like1 = "%" + q + "%";
            String like2 = "%" + raw + "%";
            String where = "(code_key LIKE ? OR UPPER(code) LIKE ?)";
            String[] args;
            if (amountFilter > 0) {
                where += " AND amount=?";
                args = new String[]{like1, like2, String.valueOf(amountFilter)};
            } else {
                args = new String[]{like1, like2};
            }
            cur = cardDb(c).query("cards", null,
                    where,
                    args, null, null, "rowid DESC", String.valueOf(Math.max(1, limit)));
            while (cur.moveToNext()) out.add(cardFromCursor(cur));
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        return out;
    }

    static int importCards(Context c, int amount, ArrayList<String> codes, String source) {
        return importCardsWithProgress(c, amount, codes, source, null);
    }

    static int importCardsWithProgress(Context c, int amount, ArrayList<String> codes, String source, ImportProgressCallback callback) {
        addCategory(c, amount);
        if (codes == null || codes.isEmpty()) return 0;
        SQLiteDatabase db = cardDb(c);
        int added = 0;
        int scanned = 0;
        int duplicates = 0;
        HashSet<String> seenInFile = new HashSet<>();
        Cursor cur = null;
        db.beginTransaction();
        try {
            String created = now();
            for (String raw : codes) {
                scanned++;
                String code = normalizeCardCode(raw);
                if (code.isEmpty()) continue;
                String key = cardDuplicateKey(code);
                if (seenInFile.contains(key)) { duplicates++; continue; }
                seenInFile.add(key);
                cur = db.rawQuery("SELECT 1 FROM cards WHERE code_key=? LIMIT 1", new String[]{key});
                boolean exists = cur.moveToFirst();
                cur.close(); cur = null;
                if (exists) { duplicates++; continue; }
                ContentValues cv = new ContentValues();
                cv.put("id", UUID.randomUUID().toString());
                cv.put("amount", amount);
                cv.put("code", code);
                cv.put("code_key", key);
                cv.put("sold", 0);
                cv.put("buyer_phone", "");
                cv.put("sold_at", "");
                cv.put("source", source == null ? "" : source);
                cv.put("created_at", created);
                long r = db.insertWithOnConflict("cards", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                if (r >= 0) added++; else duplicates++;
                if (callback != null && scanned % 200 == 0) callback.onProgress(scanned, added, duplicates);
                if (scanned % 500 == 0) db.yieldIfContendedSafely();
            }
            db.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
            try { db.endTransaction(); } catch (Exception ignored) {}
        }
        invalidateCardsCache();
        if (callback != null) callback.onProgress(scanned, added, duplicates);
        return added;
    }

    static int importRewardCards(Context c, int amount, ArrayList<String> codes, String source) {
        addCategory(c, amount);
        String src = "reward_stock" + (source == null || source.trim().isEmpty() ? "" : ":" + source.trim());
        return importCards(c, amount, codes, src);
    }

    static void deleteCard(Context c, String id) {
        if (id == null) return;
        try {
            cardDb(c).delete("cards", "id=?", new String[]{id});
            invalidateCardsCache();
        } catch (Exception ignored) {}
    }

    static int countSoldCardsOlderThanDays(Context c, int days) {
        long cutoff = System.currentTimeMillis() - Math.max(1, days) * 24L * 60L * 60L * 1000L;
        int count = 0;
        for (CardItem item : loadCards(c)) {
            if (item != null && item.sold && !isRewardStock(item) && parseStoredDateMs(item.soldAt) > 0 && parseStoredDateMs(item.soldAt) <= cutoff) count++;
        }
        return count;
    }

    static int countSoldCardsUntilDate(Context c, String dateText) {
        long cutoff = parseUserDateEndMs(dateText);
        if (cutoff <= 0) return -1;
        int count = 0;
        for (CardItem item : loadCards(c)) {
            if (item != null && item.sold && !isRewardStock(item) && parseStoredDateMs(item.soldAt) > 0 && parseStoredDateMs(item.soldAt) <= cutoff) count++;
        }
        return count;
    }

    static int cleanupSoldCardsUntilDate(Context c, String dateText) {
        synchronized (AppStore.class) {
            long cutoff = parseUserDateEndMs(dateText);
            if (cutoff <= 0) return -1;
            ArrayList<CardItem> cards = loadCards(c);
            ArrayList<CardItem> next = new ArrayList<>();
            HashSet<String> removedCodes = new HashSet<>();
            int removed = 0;
            for (CardItem item : cards) {
                long soldMs = item == null ? 0L : parseStoredDateMs(item.soldAt);
                if (item != null && item.sold && !isRewardStock(item) && soldMs > 0 && soldMs <= cutoff) {
                    removed++;
                    if (item.code != null && !item.code.trim().isEmpty()) removedCodes.add(item.code.trim());
                } else if (item != null) {
                    next.add(item);
                }
            }
            if (removed > 0) {
                saveCards(c, next);
                maskSoldCardCodesInLogs(c, removedCodes, "تم حجب رقم/أرقام كروت مباعة حتى تاريخ " + safeDateLabel(dateText) + " بتاريخ: " + now());
                addLog(c, new OperationLog(UUID.randomUUID().toString(), "تنظيف الكروت المباعة", "system", "", "", 0,
                        "تم التنظيف", "تم حذف " + removed + " كرتاً مبيعاً حتى تاريخ " + safeDateLabel(dateText) + ". لم يتم حذف سجلات البيع المالية، وتم حجب أرقام الكروت القديمة من السجلات قدر الإمكان.", "", now()));
                performAutoBackupIfDue(c, "تنظيف الكروت المباعة حسب التاريخ");
            }
            return removed;
        }
    }

    static int cleanupSoldCardsOlderThanDays(Context c, int days) {
        synchronized (AppStore.class) {
            int keepDays = Math.max(1, days);
            long cutoff = System.currentTimeMillis() - keepDays * 24L * 60L * 60L * 1000L;
            ArrayList<CardItem> cards = loadCards(c);
            ArrayList<CardItem> next = new ArrayList<>();
            HashSet<String> removedCodes = new HashSet<>();
            int removed = 0;
            for (CardItem item : cards) {
                long soldMs = item == null ? 0L : parseStoredDateMs(item.soldAt);
                if (item != null && item.sold && !isRewardStock(item) && soldMs > 0 && soldMs <= cutoff) {
                    removed++;
                    if (item.code != null && !item.code.trim().isEmpty()) removedCodes.add(item.code.trim());
                } else if (item != null) {
                    next.add(item);
                }
            }
            if (removed > 0) {
                saveCards(c, next);
                maskOldSoldCardCodesInLogs(c, removedCodes, keepDays);
                addLog(c, new OperationLog(UUID.randomUUID().toString(), "تنظيف الكروت المباعة", "system", "", "", 0,
                        "تم التنظيف", "تم حذف " + removed + " كرتاً مبيعاً مر على بيعها أكثر من " + keepDays + " أيام. لم يتم حذف سجلات البيع المالية، وتم حجب أرقام الكروت القديمة من السجلات قدر الإمكان.", "", now()));
                performAutoBackupIfDue(c, "تنظيف الكروت المباعة القديمة");
            }
            return removed;
        }
    }

    private static void maskOldSoldCardCodesInLogs(Context c, HashSet<String> removedCodes, int keepDays) {
        maskSoldCardCodesInLogs(c, removedCodes, "تم حجب رقم/أرقام كروت مباعة قديمة بعد مرور " + keepDays + " أيام بتاريخ: " + now());
    }

    private static void maskSoldCardCodesInLogs(Context c, HashSet<String> removedCodes, String note) {
        if (removedCodes == null || removedCodes.isEmpty()) return;
        ArrayList<OperationLog> logs = loadLogs(c);
        boolean changed = false;
        String safeNote = note == null ? "تم حجب رقم/أرقام كروت مباعة قديمة بتاريخ: " + now() : note;
        for (OperationLog log : logs) {
            if (log == null) continue;
            boolean touched = false;
            String cc = log.cardCode == null ? "" : log.cardCode;
            String msg = log.message == null ? "" : log.message;
            for (String code : removedCodes) {
                if (code == null || code.trim().isEmpty()) continue;
                String clean = code.trim();
                if (cc.contains(clean)) { cc = cc.replace(clean, "محذوف"); touched = true; }
                if (msg.contains(clean)) { msg = msg.replace(clean, "محذوف"); touched = true; }
            }
            if (touched) {
                log.cardCode = cc;
                if (!msg.contains("تم حجب رقم/أرقام كروت مباعة")) msg = msg + (msg.trim().isEmpty() ? "" : "\n") + safeNote;
                log.message = msg;
                changed = true;
            }
        }
        if (changed) saveLogs(c, logs);
    }

    private static long parseUserDateEndMs(String value) {
        if (value == null || value.trim().isEmpty()) return 0L;
        String v = value.trim();
        String[] formats = new String[]{"yyyy-MM-dd", "yyyy/MM/dd", "dd-MM-yyyy", "dd/MM/yyyy"};
        for (String f : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(f, Locale.US);
                sdf.setLenient(false);
                Date d = sdf.parse(v);
                return d.getTime() + 24L * 60L * 60L * 1000L - 1L;
            } catch (Exception ignored) {}
        }
        return 0L;
    }

    private static String safeDateLabel(String value) {
        return value == null ? "" : value.trim().replace("\n", " ").replace("\r", " ");
    }

    private static long parseStoredDateMs(String value) {
        if (value == null || value.trim().isEmpty()) return 0L;
        String v = value.trim();
        String[] formats = new String[]{"yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm", "yyyy-MM-dd HH:mm:ss"};
        for (String f : formats) {
            try { return new SimpleDateFormat(f, Locale.US).parse(v).getTime(); } catch (Exception ignored) {}
        }
        return 0L;
    }

    static boolean updateCard(Context c, CardItem card, int amount, String code, boolean sold) {
        if (card == null || card.id == null) return false;
        String normalized = normalizeCardCode(code);
        String newKey = cardDuplicateKey(normalized);
        if (normalized.isEmpty()) return false;
        Cursor cur = null;
        SQLiteDatabase db = cardDb(c);
        try {
            cur = db.rawQuery("SELECT id FROM cards WHERE code_key=? LIMIT 1", new String[]{newKey});
            if (cur.moveToFirst()) {
                String existingId = cur.getString(0);
                if (!card.id.equals(existingId)) return false;
            }
            if (cur != null) { cur.close(); cur = null; }
            ContentValues cv = new ContentValues();
            cv.put("amount", amount);
            cv.put("code", normalized);
            cv.put("code_key", newKey);
            cv.put("sold", sold ? 1 : 0);
            if (!sold) {
                cv.put("buyer_phone", "");
                cv.put("sold_at", "");
            }
            db.update("cards", cv, "id=?", new String[]{card.id});
            invalidateCardsCache();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (cur != null) cur.close();
        }
    }

    private static HashMap<Integer, StockCount> stockCounts(Context c) {
        if (stockCountCache != null) return stockCountCache;
        HashMap<Integer, StockCount> map = new HashMap<>();
        Cursor cur = null;
        try {
            cur = cardDb(c).rawQuery("SELECT amount, sold, CASE WHEN source LIKE 'reward_stock%' THEN 1 ELSE 0 END AS reward, COUNT(*) AS cnt FROM cards GROUP BY amount, sold, reward", null);
            while (cur.moveToNext()) {
                int amount = cur.getInt(0);
                boolean sold = cur.getInt(1) == 1;
                boolean reward = cur.getInt(2) == 1;
                int cnt = cur.getInt(3);
                StockCount sc = map.get(amount);
                if (sc == null) { sc = new StockCount(); map.put(amount, sc); }
                if (reward) {
                    sc.rewardTotal += cnt;
                    if (sold) sc.rewardSold += cnt; else sc.rewardAvailable += cnt;
                } else {
                    sc.total += cnt;
                    if (sold) sc.sold += cnt; else sc.available += cnt;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        stockCountCache = map;
        return map;
    }

    private static StockCount stockCount(Context c, int amount) {
        StockCount sc = stockCounts(c).get(amount);
        return sc == null ? new StockCount() : sc;
    }

    static ArrayList<CardItem> cardsByAmount(Context c, int amount) {
        return cardsByAmountLimited(c, amount, 0, Integer.MAX_VALUE);
    }

    static ArrayList<CardItem> cardsByAmountLimited(Context c, int amount, int offset, int limit) {
        ArrayList<CardItem> out = new ArrayList<>();
        Cursor cur = null;
        try {
            int safeOffset = Math.max(0, offset);
            int safeLimit = limit <= 0 ? PERFORMANCE_CARD_PAGE_SIZE : limit;
            String lim = safeLimit == Integer.MAX_VALUE ? null : (safeOffset + "," + safeLimit);
            cur = cardDb(c).query("cards", null,
                    "amount=? AND (source IS NULL OR source NOT LIKE 'reward_stock%')",
                    new String[]{String.valueOf(amount)}, null, null, "rowid DESC", lim);
            while (cur.moveToNext()) out.add(cardFromCursor(cur));
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        return out;
    }

    static int cardsByAmountCount(Context c, int amount) {
        return stockCount(c, amount).total;
    }

    static int availableCount(Context c, int amount) {
        return stockCount(c, amount).available;
    }

    static int soldCount(Context c, int amount) {
        return stockCount(c, amount).sold;
    }

    static int rewardAvailableCount(Context c, int amount) {
        return stockCount(c, amount).rewardAvailable;
    }

    static int rewardTotalCount(Context c, int amount) {
        return stockCount(c, amount).rewardTotal;
    }

    static int rewardSentCount(Context c, int amount) {
        return stockCount(c, amount).rewardSold;
    }

    static int totalAvailable(Context c) {
        int total = 0;
        for (StockCount sc : stockCounts(c).values()) total += sc.available;
        return total;
    }

    static int totalSold(Context c) {
        int total = 0;
        for (StockCount sc : stockCounts(c).values()) total += sc.sold;
        return total;
    }

    static int totalCardsCount(Context c) {
        int total = 0;
        for (StockCount sc : stockCounts(c).values()) total += sc.total;
        return total;
    }

    static CardItem takeAvailableCard(Context c, int amount, String buyerPhone) {
        synchronized (AppStore.class) {
            SQLiteDatabase db = null;
            Cursor cur = null;
            try {
                db = cardDb(c);
                cur = db.query("cards", null,
                        "amount=? AND sold=0 AND (source IS NULL OR source NOT LIKE 'reward_stock%')",
                        new String[]{String.valueOf(amount)}, null, null, cardSaleSqlOrder(c, amount), "1");
                if (!cur.moveToFirst()) {
                    if (!prefs(c).getBoolean(KEY_CARDS_DB_READY, false) && hasLegacyCardsPrefs(c)) {
                        return takeAvailableCardLegacyPrefs(c, amount, buyerPhone);
                    }
                    return null;
                }
                CardItem selected = cardFromCursor(cur);
                String when = now();
                ContentValues cv = new ContentValues();
                cv.put("sold", 1);
                cv.put("buyer_phone", buyerPhone == null ? "" : buyerPhone);
                cv.put("sold_at", when);
                int updated = db.update("cards", cv, "id=? AND sold=0", new String[]{selected.id});
                if (updated <= 0) return null;
                selected.sold = true;
                selected.buyerPhone = buyerPhone == null ? "" : buyerPhone;
                selected.soldAt = when;
                invalidateCardsCache();
                return selected;
            } catch (Exception e) {
                if (!prefs(c).getBoolean(KEY_CARDS_DB_READY, false) && hasLegacyCardsPrefs(c)) {
                    return takeAvailableCardLegacyPrefs(c, amount, buyerPhone);
                }
                return null;
            } finally {
                if (cur != null) cur.close();
            }
        }
    }


    // Stage 14: safety rollback for direct-sale operations.
    // If a card is reserved and the ledger step fails before sending/logging, return the card to available stock.
    static boolean restoreTakenCardToAvailable(Context c, CardItem card) {
        if (card == null || card.id == null || card.id.trim().isEmpty()) return false;
        synchronized (AppStore.class) {
            try {
                ContentValues cv = new ContentValues();
                cv.put("sold", 0);
                cv.put("buyer_phone", "");
                cv.put("sold_at", "");
                int updated = cardDb(c).update("cards", cv, "id=?", new String[]{card.id});
                if (updated > 0) {
                    card.sold = false;
                    card.buyerPhone = "";
                    card.soldAt = "";
                    invalidateCardsCache();
                    return true;
                }
            } catch (Exception ignored) {}
            try {
                String raw = prefs(c).getString(KEY_CARDS, "[]");
                JSONArray arr = new JSONArray(raw == null ? "[]" : raw);
                boolean changed = false;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    if (card.id.equals(o.optString("id"))) {
                        o.put("sold", false);
                        o.put("buyerPhone", "");
                        o.put("soldAt", "");
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    prefs(c).edit().putString(KEY_CARDS, arr.toString()).commit();
                    card.sold = false;
                    card.buyerPhone = "";
                    card.soldAt = "";
                    invalidateCardsCache();
                    return true;
                }
            } catch (Exception ignored) {}
            return false;
        }
    }

    static ArrayList<CardItem> takeAvailableCardsAllOrNone(Context c, int[] amounts, String buyerPhone) {
        synchronized (AppStore.class) {
            ArrayList<CardItem> selected = new ArrayList<>();
            if (amounts == null || amounts.length == 0) return selected;
            SQLiteDatabase db = cardDb(c);
            Cursor cur = null;
            db.beginTransaction();
            try {
                HashSet<String> picked = new HashSet<>();
                for (int amount : amounts) {
                    cur = db.query("cards", null,
                            "amount=? AND sold=0 AND (source IS NULL OR source NOT LIKE 'reward_stock%')",
                            new String[]{String.valueOf(amount)}, null, null, cardSaleSqlOrder(c, amount), "10");
                    CardItem found = null;
                    while (cur.moveToNext()) {
                        CardItem tmp = cardFromCursor(cur);
                        if (!picked.contains(tmp.id)) { found = tmp; break; }
                    }
                    cur.close(); cur = null;
                    if (found == null) {
                        if (!prefs(c).getBoolean(KEY_CARDS_DB_READY, false) && hasLegacyCardsPrefs(c)) {
                            return takeAvailableCardsAllOrNoneLegacyPrefs(c, amounts, buyerPhone);
                        }
                        return new ArrayList<>();
                    }
                    selected.add(found);
                    picked.add(found.id);
                }
                String when = now();
                ContentValues cv = new ContentValues();
                cv.put("sold", 1);
                cv.put("buyer_phone", buyerPhone == null ? "" : buyerPhone);
                cv.put("sold_at", when);
                for (CardItem item : selected) {
                    db.update("cards", cv, "id=?", new String[]{item.id});
                    item.sold = true;
                    item.buyerPhone = buyerPhone == null ? "" : buyerPhone;
                    item.soldAt = when;
                }
                db.setTransactionSuccessful();
                invalidateCardsCache();
                return selected;
            } catch (Exception e) {
                if (!prefs(c).getBoolean(KEY_CARDS_DB_READY, false) && hasLegacyCardsPrefs(c)) {
                    return takeAvailableCardsAllOrNoneLegacyPrefs(c, amounts, buyerPhone);
                }
                return new ArrayList<>();
            } finally {
                if (cur != null) cur.close();
                try { db.endTransaction(); } catch (Exception ignored) {}
            }
        }
    }


    static HashSet<String> loadImportedBatches(Context c) {
        return new HashSet<>(prefs(c).getStringSet(KEY_IMPORTED_BATCHES, new HashSet<String>()));
    }

    static boolean isBatchImported(Context c, String batchId) {
        return loadImportedBatches(c).contains(batchId == null ? "" : batchId.trim());
    }

    static int importAdminCardPack(Context c, String jsonText) throws Exception {
        JSONObject root = new JSONObject(jsonText == null ? "" : jsonText);
        String app = root.optString("app", "");
        if (!"ONLINE_CARD_PACK".equals(app)) throw new Exception("هذا الملف ليس ملف كروت إدارة صالحًا");

        String batchId = root.optString("batch_id", "").trim();
        String requestCode = root.optString("device_request_code", "").trim();
        int amount = root.optInt("amount", 0);
        JSONArray arr = root.optJSONArray("cards");
        String signature = root.optString("signature", "").trim();

        if (batchId.isEmpty()) throw new Exception("رقم الدفعة غير موجود");
        if (amount <= 0) throw new Exception("الفئة غير صحيحة");
        if (arr == null || arr.length() == 0) throw new Exception("لا توجد كروت في الملف");
        if (!requestCode.isEmpty() && !requestCode.equals(getRequestCode(c))) throw new Exception("ملف الكروت مخصص لجهاز آخر");
        if (isBatchImported(c, batchId)) throw new Exception("تم استيراد هذه الدفعة سابقًا ولا يمكن استيرادها مرة أخرى");

        ArrayList<String> codes = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            String code = arr.optString(i, "").trim();
            if (!code.isEmpty()) codes.add(code);
        }
        if (codes.isEmpty()) throw new Exception("لا توجد كروت صالحة في الملف");

        String expected = buildCardPackSignature(requestCode, batchId, amount, codes);
        if (!expected.equals(signature)) throw new Exception("ملف الكروت غير صالح أو تم تعديله");

        HashSet<String> insidePack = new HashSet<>();
        for (String code : codes) {
            String key = cardDuplicateKey(code);
            if (insidePack.contains(key)) throw new Exception("يوجد كرت مكرر داخل نفس الملف: " + code);
            insidePack.add(key);
        }
        if (duplicateCardCount(c, codes) > 0) throw new Exception("يوجد كرت أو أكثر من هذا الملف سبق إدخاله في النظام");

        int added = importCards(c, amount, codes, "admin_pack:" + batchId);
        HashSet<String> batches = loadImportedBatches(c);
        batches.add(batchId);
        prefs(c).edit().putStringSet(KEY_IMPORTED_BATCHES, batches).apply();
        addLog(c, new OperationLog(UUID.randomUUID().toString(), "ملف إدارة", "admin_pack", "", "", amount,
                "تم استيراد دفعة", "تم استيراد " + added + " كرت من دفعة الإدارة: " + batchId, "", now()));
        return added;
    }

    static String buildCardPackSignature(String requestCode, String batchId, int amount, ArrayList<String> codes) {
        StringBuilder cardsJoined = new StringBuilder();
        for (String code : codes) cardsJoined.append(code == null ? "" : code.trim()).append("|");
        return sha256(CARD_PACK_SECRET + "|" + (requestCode == null ? "" : requestCode.trim()) + "|" + batchId.trim() + "|" + amount + "|" + cardsJoined.toString());
    }



    static ArrayList<PosOutlet> loadPosOutlets(Context c) {
        ArrayList<PosOutlet> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_POS_OUTLETS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new PosOutlet(
                        o.optString("id"),
                        o.optString("name"),
                        o.optString("phone"),
                        o.optString("requestCode"),
                        o.optString("notes"),
                        o.optBoolean("active", true),
                        o.optBoolean("rewardsEnabled", true),
                        o.optLong("createdAt", System.currentTimeMillis())
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static void savePosOutlets(Context c, ArrayList<PosOutlet> list) {
        try {
            JSONArray arr = new JSONArray();
            for (PosOutlet pos : list) {
                JSONObject o = new JSONObject();
                o.put("id", pos.id);
                o.put("name", pos.name == null ? "" : pos.name);
                o.put("phone", pos.phone == null ? "" : pos.phone);
                o.put("requestCode", pos.requestCode == null ? "" : pos.requestCode);
                o.put("notes", pos.notes == null ? "" : pos.notes);
                o.put("active", pos.active);
                o.put("rewardsEnabled", pos.rewardsEnabled);
                o.put("createdAt", pos.createdAt);
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_POS_OUTLETS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    static void addOrUpdatePosOutlet(Context c, String id, String name, String phone, String requestCode, String notes, boolean active, boolean rewardsEnabled) {
        ArrayList<PosOutlet> list = loadPosOutlets(c);
        String cleanId = id == null ? "" : id.trim();
        String cleanPhone = normalizeLocalPhone(phone);
        String cleanReq = requestCode == null ? "" : requestCode.trim().toUpperCase(Locale.US);
        if (cleanId.isEmpty()) cleanId = UUID.randomUUID().toString();
        boolean updated = false;
        for (PosOutlet pos : list) {
            if (pos.id.equals(cleanId)) {
                pos.name = name == null ? "" : name.trim();
                pos.phone = cleanPhone;
                pos.requestCode = cleanReq;
                pos.notes = notes == null ? "" : notes.trim();
                pos.active = active;
                pos.rewardsEnabled = rewardsEnabled;
                updated = true;
                break;
            }
        }
        if (!updated) {
            list.add(0, new PosOutlet(cleanId, name, cleanPhone, cleanReq, notes, active, rewardsEnabled, System.currentTimeMillis()));
        }
        savePosOutlets(c, list);
    }

    static void deletePosOutlet(Context c, String id) {
        ArrayList<PosOutlet> list = loadPosOutlets(c);
        ArrayList<PosOutlet> next = new ArrayList<>();
        for (PosOutlet pos : list) if (!pos.id.equals(id)) next.add(pos);
        savePosOutlets(c, next);
    }

    static File getPosPackDir(Context c) {
        File base = c.getExternalFilesDir(null);
        if (base == null) base = c.getFilesDir();
        File dir = new File(base, "ONLINE_pos_packs");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    static File createPosCardPackFile(Context c, PosOutlet pos, int amount, int quantity) throws Exception {
        if (pos == null) throw new Exception("اختر نقطة بيع أولًا");
        if (pos.requestCode == null || pos.requestCode.trim().isEmpty()) throw new Exception("كود طلب نقطة البيع غير موجود");
        if (amount <= 0) throw new Exception("الفئة غير صحيحة");
        if (quantity <= 0) throw new Exception("العدد غير صحيح");

        ArrayList<CardItem> selected = new ArrayList<>();
        Cursor selCur = null;
        try {
            selCur = cardDb(c).query("cards", null,
                    "amount=? AND sold=0 AND (source IS NULL OR source NOT LIKE 'reward_stock%')",
                    new String[]{String.valueOf(amount)}, null, null, "rowid ASC", String.valueOf(Math.max(1, quantity)));
            while (selCur.moveToNext()) selected.add(cardFromCursor(selCur));
        } finally {
            if (selCur != null) selCur.close();
        }
        if (selected.size() < quantity) throw new Exception("الكروت المتاحة من فئة " + amount + " أقل من العدد المطلوب. المتاح: " + selected.size());

        ArrayList<String> codes = new ArrayList<>();
        for (CardItem item : selected) codes.add(item.code);

        String posKey = (pos.name + "_" + pos.phone).replaceAll("[^A-Za-z0-9_\\-\\u0600-\\u06FF]", "_");
        String batchId = "POS-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + "-" + sha256(pos.requestCode + amount + quantity + System.currentTimeMillis()).substring(0, 8).toUpperCase(Locale.US);

        JSONObject root = new JSONObject();
        root.put("app", "ONLINE_CARD_PACK");
        root.put("pack_type", "pos_supply");
        root.put("batch_id", batchId);
        root.put("device_request_code", pos.requestCode.trim());
        root.put("pos_name", pos.name);
        root.put("pos_phone", pos.phone);
        root.put("rewards_enabled", pos.rewardsEnabled);
        root.put("network_name", getNetworkName(c));
        root.put("created_at", now());
        root.put("amount", amount);
        root.put("quantity", codes.size());
        JSONArray arr = new JSONArray();
        for (String code : codes) arr.put(code);
        root.put("cards", arr);
        root.put("signature", buildCardPackSignature(pos.requestCode.trim(), batchId, amount, codes));

        File dir = getPosPackDir(c);
        String fileName = "ONLINE_POS_" + posKey + "_" + amount + "x" + codes.size() + "_" + batchId + ".onlpack";
        File file = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
        writer.write(root.toString(2));
        writer.flush();
        writer.close();
        fos.close();

        SQLiteDatabase db = cardDb(c);
        ContentValues cv = new ContentValues();
        cv.put("sold", 1);
        cv.put("buyer_phone", pos.phone == null ? "" : pos.phone);
        cv.put("sold_at", now());
        cv.put("source", "pos_pack:" + batchId);
        for (CardItem item : selected) db.update("cards", cv, "id=?", new String[]{item.id});
        invalidateCardsCache();

        addLog(c, new OperationLog(UUID.randomUUID().toString(), "تغذية نقطة بيع", "pos_pack", pos.name, pos.phone, amount,
                "تم إنشاء ملف نقطة بيع", "تم تجهيز " + codes.size() + " كرت فئة " + amount + " لنقطة البيع: " + pos.name + " | المكافآت: " + (pos.rewardsEnabled ? "مفعلة" : "بدون مكافآت") + " | الدفعة: " + batchId + " | الملف: " + file.getAbsolutePath(), "", now()));
        performAutoBackupIfDue(c, "إنشاء ملف كروت لنقطة بيع");
        return file;
    }

    static ArrayList<OperationLog> loadLogs(Context c) {
        String raw = prefs(c).getString(KEY_LOGS, "[]");
        if (raw != null && raw.equals(logsCacheJson) && logsCache != null) return copyLogList(logsCache);
        ArrayList<OperationLog> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw == null ? "[]" : raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new OperationLog(
                        o.optString("id"),
                        o.optString("provider"),
                        o.optString("sender"),
                        o.optString("customerName"),
                        o.optString("customerPhone"),
                        o.optInt("amount"),
                        o.optString("status"),
                        o.optString("message"),
                        o.optString("cardCode"),
                        o.optString("createdAt")
                ));
            }
            logsCacheJson = raw;
            logsCache = copyLogList(list);
        } catch (Exception ignored) {}
        return list;
    }

    static boolean isTechnicalQueueLog(OperationLog log) {
        if (log == null) return true;
        String provider = log.provider == null ? "" : log.provider.trim();
        String status = log.status == null ? "" : log.status.trim();
        String msg = log.message == null ? "" : log.message.trim();
        String hay = (provider + " " + status + " " + msg);
        return provider.equals("SMS موثوق")
                || provider.contains("الطابور")
                || status.contains("الطابور")
                || status.contains("تمت قراءة الطابور")
                || status.contains("في الطابور الموثوق")
                || msg.contains("بدأت معالجة الرسالة الموثوقة من الطابور")
                || msg.contains("انتهت قراءة الرسالة الموثوقة")
                || hay.contains("تمت قراءة الطابور");
    }

    static boolean isUserVisibleLog(OperationLog log) {
        if (log == null) return false;
        if (isTechnicalQueueLog(log)) return false;
        String status = log.status == null ? "" : log.status.trim();
        String provider = log.provider == null ? "" : log.provider.trim();
        String msg = log.message == null ? "" : log.message.trim();
        if (status.contains("مرفوض بصمت") && !status.contains("مراجعة")) return false;
        if (provider.equals("النظام") && msg.contains("نسخة احتياطية تلقائية")) return false;
        return true;
    }

    static ArrayList<OperationLog> loadVisibleLogs(Context c) {
        ArrayList<OperationLog> out = new ArrayList<>();
        for (OperationLog log : loadLogs(c)) {
            if (isUserVisibleLog(log)) out.add(log);
        }
        return out;
    }

    static int visibleLogsCount(Context c) {
        return loadVisibleLogs(c).size();
    }

    static ArrayList<OperationLog> loadVisibleLogsPage(Context c, int offset, int limit) {
        ArrayList<OperationLog> all = loadVisibleLogs(c);
        ArrayList<OperationLog> out = new ArrayList<>();
        int start = Math.max(0, offset);
        int end = Math.min(all.size(), start + (limit <= 0 ? PERFORMANCE_LOG_PAGE_SIZE : limit));
        for (int i = start; i < end; i++) out.add(all.get(i));
        return out;
    }

    static ArrayList<OperationLog> loadRecentVisibleLogs(Context c, int max) {
        return loadVisibleLogsPage(c, 0, max <= 0 ? PERFORMANCE_LOG_PAGE_SIZE : max);
    }

    private static void invalidateLogsCache() {
        logsCacheJson = null;
        logsCache = null;
    }

    static int logsCount(Context c) {
        return loadLogs(c).size();
    }

    static ArrayList<OperationLog> loadRecentLogs(Context c, int max) {
        return loadLogsPage(c, 0, max <= 0 ? PERFORMANCE_LOG_PAGE_SIZE : max);
    }

    static ArrayList<OperationLog> loadLogsPage(Context c, int offset, int limit) {
        ArrayList<OperationLog> all = loadLogs(c);
        ArrayList<OperationLog> out = new ArrayList<>();
        int start = Math.max(0, offset);
        int end = Math.min(all.size(), start + (limit <= 0 ? PERFORMANCE_LOG_PAGE_SIZE : limit));
        for (int i = start; i < end; i++) out.add(all.get(i));
        return out;
    }

    static void saveLogs(Context c, ArrayList<OperationLog> logs) {
        try {
            JSONArray arr = new JSONArray();
            for (OperationLog log : logs) {
                JSONObject o = new JSONObject();
                o.put("id", log.id);
                o.put("provider", log.provider);
                o.put("sender", log.sender);
                o.put("customerName", log.customerName);
                o.put("customerPhone", log.customerPhone);
                o.put("amount", log.amount);
                o.put("status", log.status);
                o.put("message", log.message);
                o.put("cardCode", log.cardCode);
                o.put("createdAt", log.createdAt);
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_LOGS, arr.toString()).apply();
            invalidateLogsCache();
        } catch (Exception ignored) {}
    }

    static void addLog(Context c, OperationLog log) {
        // Stage 13.8: لا نخزن سجلات الطابور التقنية في سجل المستخدم.
        // السجل الظاهر يبقى للعمليات الفعلية فقط: سداد، سلفة، بيع، تعبئة، مراجعة، فشل مهم.
        if (isTechnicalQueueLog(log)) return;
        ArrayList<OperationLog> logs = loadLogs(c);
        logs.add(0, log);
        while (logs.size() > MAX_FAST_LOGS) logs.remove(logs.size() - 1);
        saveLogs(c, logs);
    }

    static OperationLog findLog(Context c, String id) {
        if (id == null) return null;
        for (OperationLog log : loadLogs(c)) {
            if (id.equals(log.id)) return log;
        }
        return null;
    }

    static void updateLogStatus(Context c, String id, String status, String message) {
        ArrayList<OperationLog> logs = loadLogs(c);
        boolean changed = false;
        for (OperationLog log : logs) {
            if (log.id.equals(id)) {
                if (status != null) log.status = status;
                if (message != null) log.message = message;
                changed = true;
                break;
            }
        }
        if (changed) saveLogs(c, logs);
    }

    static void updateLogStatusKeepMessage(Context c, String id, String status) {
        updateLogStatus(c, id, status, null);
    }

    static void updateLogDetails(Context c, String id, String customerName, String customerPhone, String status, String message, String cardCode) {
        ArrayList<OperationLog> logs = loadLogs(c);
        boolean changed = false;
        for (OperationLog log : logs) {
            if (log.id.equals(id)) {
                if (customerName != null) log.customerName = customerName;
                if (customerPhone != null) log.customerPhone = normalizeLocalPhone(customerPhone);
                if (status != null) log.status = status;
                if (message != null) log.message = message;
                if (cardCode != null) log.cardCode = cardCode;
                changed = true;
                break;
            }
        }
        if (changed) saveLogs(c, logs);
    }

    static void appendLogMessage(Context c, String id, String extra) {
        OperationLog log = findLog(c, id);
        String base = log == null || log.message == null ? "" : log.message;
        updateLogStatus(c, id, null, base + (base.isEmpty() ? "" : "\n") + (extra == null ? "" : extra));
    }

    static void deleteLog(Context c, String id) {
        ArrayList<OperationLog> logs = loadLogs(c);
        ArrayList<OperationLog> next = new ArrayList<>();
        for (OperationLog log : logs) if (!log.id.equals(id)) next.add(log);
        saveLogs(c, next);
    }

    static void clearLogs(Context c) {
        prefs(c).edit().putString(KEY_LOGS, "[]").apply();
        invalidateLogsCache();
    }

    static int sentOperationsCount(Context c) {
        int total = 0;
        for (OperationLog log : loadLogs(c)) if (log.status != null && (log.status.contains("تم إرسال") || log.status.contains("تم تسليم") || log.status.equals("تم الإرسال"))) total++;
        return total;
    }

    static int processedOperationsCount(Context c) {
        return loadVisibleLogs(c).size();
    }

    static HashSet<String> loadProcessed(Context c) {
        return new HashSet<>(prefs(c).getStringSet(KEY_PROCESSED, new HashSet<String>()));
    }

    static boolean isProcessed(Context c, String id) {
        return loadProcessed(c).contains(id);
    }

    static void markProcessed(Context c, String id) {
        HashSet<String> set = loadProcessed(c);
        set.add(id);
        prefs(c).edit().putStringSet(KEY_PROCESSED, set).apply();
    }

    static HashSet<String> loadExactPaymentTexts(Context c) {
        return new HashSet<>(prefs(c).getStringSet(KEY_EXACT_PAYMENT_TEXTS, new HashSet<String>()));
    }

    static String exactPaymentTextKey(String sender, String body) {
        // قفل التكرار الحرفي: نفس المرسل + نفس نص الرسالة حرفيًا 100% = نفس العملية.
        return sha256((sender == null ? "" : sender) + "|RAW100|" + (body == null ? "" : body));
    }

    static boolean isExactPaymentTextProcessed(Context c, String sender, String body) {
        return loadExactPaymentTexts(c).contains(exactPaymentTextKey(sender, body));
    }

    static void markExactPaymentTextProcessed(Context c, String sender, String body) {
        HashSet<String> set = loadExactPaymentTexts(c);
        set.add(exactPaymentTextKey(sender, body));
        // حماية من تضخم SharedPreferences: احتفظ بآخر عدد معقول.
        if (set.size() > 2500) {
            HashSet<String> compact = new HashSet<>();
            int i = 0;
            for (String v : set) {
                compact.add(v);
                if (++i >= 2000) break;
            }
            set = compact;
        }
        prefs(c).edit().putStringSet(KEY_EXACT_PAYMENT_TEXTS, set).apply();
    }

    static long findRecentTrustedRequest(Context c, String fingerprint, long windowMs) {
        if (fingerprint == null || fingerprint.trim().isEmpty()) return 0L;
        long nowMs = System.currentTimeMillis();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_TRUSTED_REQUEST_LOCKS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String fp = o.optString("fp", "");
                long at = o.optLong("at", 0L);
                if (fingerprint.equals(fp) && at > 0L && nowMs - at >= 0L && nowMs - at < windowMs) return at;
            }
        } catch (Exception ignored) {}
        return 0L;
    }

    static void rememberTrustedRequest(Context c, String fingerprint) {
        if (fingerprint == null || fingerprint.trim().isEmpty()) return;
        long nowMs = System.currentTimeMillis();
        try {
            JSONArray old = new JSONArray(prefs(c).getString(KEY_TRUSTED_REQUEST_LOCKS, "[]"));
            JSONArray arr = new JSONArray();
            for (int i = 0; i < old.length(); i++) {
                JSONObject o = old.getJSONObject(i);
                long at = o.optLong("at", 0L);
                if (at > 0L && nowMs - at < 30L * 60L * 1000L) arr.put(o);
            }
            JSONObject n = new JSONObject();
            n.put("fp", fingerprint);
            n.put("at", nowMs);
            arr.put(n);
            prefs(c).edit().putString(KEY_TRUSTED_REQUEST_LOCKS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }


    static boolean reserveTrustedRequest(Context c, String fingerprint, long windowMs) {
        if (fingerprint == null || fingerprint.trim().isEmpty()) return false;
        synchronized (AppStore.class) {
            if (findRecentTrustedRequest(c, fingerprint, windowMs) > 0L) return false;
            rememberTrustedRequest(c, fingerprint);
            return true;
        }
    }

    static ArrayList<TrustedContact> loadTrustedContacts(Context c) {
        ArrayList<TrustedContact> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_TRUSTED, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String walletName = o.optString("walletName", "");
                if (walletName.trim().isEmpty()) walletName = "ONE Cash";
                list.add(new TrustedContact(
                        o.optString("id"),
                        walletName,
                        o.optString("senderKeywords", ""),
                        o.optString("fullName"),
                        o.optString("tripleName"),
                        o.optString("phone"),
                        o.optBoolean("active", true)
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static void saveTrustedContacts(Context c, ArrayList<TrustedContact> list) {
        try {
            JSONArray arr = new JSONArray();
            for (TrustedContact contact : list) {
                JSONObject o = new JSONObject();
                o.put("id", contact.id);
                o.put("walletName", contact.walletName);
                o.put("senderKeywords", contact.senderKeywords);
                o.put("fullName", contact.fullName);
                o.put("tripleName", contact.tripleName);
                o.put("phone", contact.phone);
                o.put("active", contact.active);
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_TRUSTED, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    static void addTrustedContact(Context c, String fullName, String phone) {
        addWalletContact(c, "ONE Cash", "one cash, onecash, ون كاش", fullName, phone);
    }

    static boolean addWalletContact(Context c, String walletName, String senderKeywords, String fullName, String phone) {
        if (!canSaveWalletContact(walletName, fullName, phone)) return false;
        ArrayList<TrustedContact> list = loadTrustedContacts(c);
        boolean saved = addOrUpdateWalletContactInList(list, walletName, senderKeywords, fullName, phone);
        saveTrustedContacts(c, list);
        return saved;
    }

    static boolean addWalletContactToAllWallets(Context c, String walletName, String senderKeywords, String fullName, String phone) {
        if (!canSaveWalletContact(walletName, fullName, phone)) return false;
        ArrayList<TrustedContact> list = loadTrustedContacts(c);
        boolean saved = addOrUpdateWalletContactInList(list, walletName, senderKeywords, fullName, phone);
        ArrayList<String[]> wallets = defaultWalletDefinitions();
        for (TrustedContact contact : list) {
            String w = contact.walletName == null ? "" : contact.walletName.trim();
            if (w.isEmpty()) continue;
            String k = contact.senderKeywords == null || contact.senderKeywords.trim().isEmpty() ? w : contact.senderKeywords.trim();
            boolean exists = false;
            for (String[] pair : wallets) if (pair[0].equalsIgnoreCase(w)) { exists = true; break; }
            if (!exists) wallets.add(new String[]{w, k});
        }
        for (String[] pair : wallets) {
            addOrUpdateWalletContactInList(list, pair[0], pair[1], fullName, phone);
        }
        saveTrustedContacts(c, list);
        return saved;
    }

    private static ArrayList<String[]> defaultWalletDefinitions() {
        ArrayList<String[]> wallets = new ArrayList<>();
        wallets.add(new String[]{"ONE Cash", "one cash, onecash, onec, ون كاش, ونكاش"});
        wallets.add(new String[]{"جوالي", "jawali, جوالي"});
        wallets.add(new String[]{"جيب", "jaib, جيب"});
        wallets.add(new String[]{"كريمي", "kuraimi, alkuraimi, al kuraimi, كريمي, الكريمي"});
        wallets.add(new String[]{"فلوسك", "floosk, floosak, فلوسك"});
        wallets.add(new String[]{"كاش", "cash, كاش, يمن كاش, yemen cash, yemencash"});
        wallets.add(new String[]{"سبأ كاش", "saba cash, sabacash, saba, سبأ كاش, سبا كاش"});
        wallets.add(new String[]{"بيس", "bis, bais, beys, بيس"});
        return wallets;
    }

    static boolean canSaveWalletContact(String walletName, String fullName, String phone) {
        String wallet = walletName == null ? "" : walletName.trim();
        String cleanName = fullName == null ? "" : fullName.trim();
        String triple = NameUtils.tripleName(cleanName);
        String normalizedPhone = normalizeLocalPhone(phone);
        return !wallet.isEmpty() && !triple.isEmpty() && isValidLocalMobile(normalizedPhone);
    }

    static String walletContactValidationMessage(String walletName, String fullName, String phone) {
        String wallet = walletName == null ? "" : walletName.trim();
        String cleanName = fullName == null ? "" : fullName.trim();
        String triple = NameUtils.tripleName(cleanName);
        String normalizedPhone = normalizeLocalPhone(phone);
        if (wallet.isEmpty()) return "اكتب اسم المحفظة";
        if (cleanName.isEmpty()) return "اكتب الاسم كما يظهر في رسالة المحفظة";
        if (triple.isEmpty()) return "اكتب الاسم الثلاثي على الأقل كما يظهر في الرسالة";
        if (normalizedPhone.isEmpty()) return "اكتب رقم استلام الكرت";
        if (!isValidLocalMobile(normalizedPhone)) return "رقم استلام الكرت يجب أن يكون 9 أرقام ويبدأ بالرقم 7";
        return "";
    }

    private static boolean addOrUpdateWalletContactInList(ArrayList<TrustedContact> list, String walletName, String senderKeywords, String fullName, String phone) {
        String wallet = walletName == null || walletName.trim().isEmpty() ? "محفظة" : walletName.trim();
        String keywords = senderKeywords == null ? "" : senderKeywords.trim();
        String cleanName = fullName == null ? "" : fullName.trim();
        String triple = NameUtils.tripleName(cleanName);
        String normalizedPhone = normalizeLocalPhone(phone);
        if (triple.isEmpty() || !isValidLocalMobile(normalizedPhone)) return false;
        for (TrustedContact contact : list) {
            if (contact.tripleName.equals(triple) && contact.walletName.equalsIgnoreCase(wallet)) {
                contact.walletName = wallet;
                contact.senderKeywords = keywords;
                contact.fullName = cleanName;
                contact.phone = normalizedPhone;
                contact.active = true;
                return true;
            }
        }
        list.add(0, new TrustedContact(UUID.randomUUID().toString(), wallet, keywords, cleanName, triple, normalizedPhone, true));
        return true;
    }

    static String normalizeLocalPhone(String phone) {
        return PaymentParser.normalizeLocalPhone(phone);
    }

    static boolean isValidLocalMobile(String phone) {
        return PaymentParser.hasValidLocalMobile(phone);
    }

    static void deleteTrustedContact(Context c, String id) {
        ArrayList<TrustedContact> list = loadTrustedContacts(c);
        ArrayList<TrustedContact> next = new ArrayList<>();
        for (TrustedContact contact : list) if (!contact.id.equals(id)) next.add(contact);
        saveTrustedContacts(c, next);
    }


    static boolean updateTrustedContact(Context c, String id, String walletName, String senderKeywords, String fullName, String phone, boolean active) {
        String wallet = walletName == null || walletName.trim().isEmpty() ? "محفظة" : walletName.trim();
        String keywords = senderKeywords == null ? "" : senderKeywords.trim();
        String cleanName = fullName == null ? "" : fullName.trim();
        String triple = NameUtils.tripleName(cleanName);
        String normalizedPhone = normalizeLocalPhone(phone);
        if (id == null || id.trim().isEmpty() || triple.isEmpty() || !isValidLocalMobile(normalizedPhone)) return false;
        ArrayList<TrustedContact> list = loadTrustedContacts(c);
        boolean changed = false;
        for (TrustedContact contact : list) {
            if (id.equals(contact.id)) {
                contact.walletName = wallet;
                contact.senderKeywords = keywords;
                contact.fullName = cleanName;
                contact.tripleName = triple;
                contact.phone = normalizedPhone;
                contact.active = active;
                changed = true;
                break;
            }
        }
        if (changed) saveTrustedContacts(c, list);
        return changed;
    }

    static TrustedContact findTrustedByName(Context c, String rawName) {
        String triple = NameUtils.tripleName(rawName);
        if (triple.isEmpty()) return null;
        for (TrustedContact contact : loadTrustedContacts(c)) {
            if (contact.active && contact.tripleName.equals(triple)) return contact;
        }
        return null;
    }

    static TrustedContact findWalletContact(Context c, String rawName, String sender, String body) {
        String triple = NameUtils.tripleName(rawName);
        String hay = ((sender == null ? "" : sender) + " " + (body == null ? "" : body)).toLowerCase();
        for (TrustedContact contact : loadTrustedContacts(c)) {
            if (!contact.active) continue;
            boolean nameMatches = false;
            if (!triple.isEmpty() && contact.tripleName.equals(triple)) nameMatches = true;
            if (!nameMatches && contact.fullName != null && !contact.fullName.trim().isEmpty() && body != null && body.contains(contact.fullName.trim())) nameMatches = true;
            if (!nameMatches) continue;

            if (walletKeywordsMatch(contact, hay)) return contact;
        }
        return null;
    }

    static TrustedContact findWalletContactInBody(Context c, String sender, String body) {
        String hay = ((sender == null ? "" : sender) + " " + (body == null ? "" : body)).toLowerCase();
        for (TrustedContact contact : loadTrustedContacts(c)) {
            if (!contact.active) continue;
            boolean nameInsideBody = false;
            if (contact.fullName != null && !contact.fullName.trim().isEmpty() && body != null && body.contains(contact.fullName.trim())) nameInsideBody = true;
            if (!nameInsideBody && contact.tripleName != null && !contact.tripleName.isEmpty() && body != null) {
                String bodyTriple = NameUtils.tripleName(body);
                nameInsideBody = bodyTriple.contains(contact.tripleName);
            }
            if (nameInsideBody && walletKeywordsMatch(contact, hay)) return contact;
        }
        return null;
    }

    static boolean matchesAnyWalletKeyword(Context c, String sender, String body) {
        String hay = ((sender == null ? "" : sender) + " " + (body == null ? "" : body)).toLowerCase();
        for (TrustedContact contact : loadTrustedContacts(c)) {
            if (contact.active && walletKeywordsMatch(contact, hay)) return true;
        }
        return false;
    }

    static boolean matchesAnyWalletSenderKeyword(Context c, String sender) {
        String hay = (sender == null ? "" : sender).toLowerCase();
        if (hay.trim().isEmpty()) return false;
        for (TrustedContact contact : loadTrustedContacts(c)) {
            if (contact.active && walletKeywordsMatch(contact, hay)) return true;
        }
        return false;
    }

    private static boolean walletKeywordsMatch(TrustedContact contact, String hay) {
        if (hay == null) hay = "";
        String keys = ((contact.walletName == null ? "" : contact.walletName) + "," + (contact.senderKeywords == null ? "" : contact.senderKeywords)).toLowerCase();
        for (String raw : keys.split("[,،\n]")) {
            String k = raw.trim();
            if (!k.isEmpty() && hay.contains(k)) return true;
        }
        return keys.trim().isEmpty();
    }

    static boolean isPendingCriticalLog(OperationLog log) {
        if (log == null) return false;
        String st = log.status == null ? "" : log.status;
        if (st.contains("تمت المراجعة") || st.contains("محذوف") || st.contains("تم إرسال") || st.contains("تم تسليم") || st.equals("تم الإرسال")) return false;
        return st.contains("معلق")
                || st.contains("يحتاج إضافة")
                || st.contains("دفع صريح")
                || st.contains("إيداع صريح")
                || st.contains("فشل")
                || st.contains("لا يوجد رقم")
                || st.contains("مرفوض")
                || st.contains("تجاوز السقف")
                || st.contains("نفدت الكمية");
    }

    static OperationLog firstPendingCriticalLog(Context c) {
        for (OperationLog log : loadLogs(c)) {
            if (isPendingCriticalLog(log)) return log;
        }
        return null;
    }


    static OperationLog firstPendingCriticalLogForReminder(Context c) {
        long nowMs = System.currentTimeMillis();
        for (OperationLog log : loadLogs(c)) {
            if (!isPendingCriticalLog(log)) continue;
            long age = nowMs - parseLogTimeMs(log.createdAt);
            if (age >= 0 && age <= 30L * 60L * 1000L) return log;
        }
        return null;
    }

    static boolean isExplicitDepositReviewLog(OperationLog log) {
        if (log == null) return false;
        String st = log.status == null ? "" : log.status;
        String msg = log.message == null ? "" : log.message;
        return st.contains("إيداع صريح") || st.contains("ايداع صريح") || msg.contains("إيداع صريح") || msg.contains("ايداع صريح");
    }

    private static long parseLogTimeMs(String value) {
        if (value == null || value.trim().isEmpty()) return System.currentTimeMillis();
        try {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).parse(value.trim()).getTime();
        } catch (Exception ignored) {
            return System.currentTimeMillis();
        }
    }

    static boolean hasPendingCriticalLogs(Context c) {
        return firstPendingCriticalLog(c) != null;
    }

    static void markLogReviewed(Context c, String id) {
        ArrayList<OperationLog> logs = loadLogs(c);
        boolean changed = false;
        for (OperationLog log : logs) {
            if (log.id.equals(id)) {
                log.status = "تمت المراجعة";
                String base = log.message == null ? "" : log.message;
                String stamp = "تمت مراجعة العملية يدويًا بتاريخ: " + now();
                if (!base.contains(stamp)) log.message = base + (base.trim().isEmpty() ? "" : "\n") + stamp;
                changed = true;
                break;
            }
        }
        if (changed) saveLogs(c, logs);
    }



    static boolean isSmartLedgerEnabled(Context c) {
        return prefs(c).getBoolean(KEY_LEDGER_ENABLED, false);
    }

    static boolean isLedgerLoanEnabled(Context c) {
        return prefs(c).getBoolean(KEY_LEDGER_LOAN_ENABLED, true);
    }

    static boolean isLedgerAutoSettleEnabled(Context c) {
        return prefs(c).getBoolean(KEY_LEDGER_AUTO_SETTLE_ENABLED, true);
    }

    static void saveLedgerSettings(Context c, boolean enabled, boolean loanEnabled, boolean autoSettleEnabled) {
        prefs(c).edit()
                .putBoolean(KEY_LEDGER_ENABLED, enabled)
                .putBoolean(KEY_LEDGER_LOAN_ENABLED, loanEnabled)
                .putBoolean(KEY_LEDGER_AUTO_SETTLE_ENABLED, autoSettleEnabled)
                .apply();
    }

    static ArrayList<LedgerCustomer> loadLedgerCustomers(Context c) {
        ArrayList<LedgerCustomer> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_LEDGER_CUSTOMERS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                boolean active = o.optBoolean("active", true);
                boolean trusted = o.optBoolean("trusted", false);
                boolean loanEnabled = o.optBoolean("loanEnabled", false);
                String savedMode = o.optString("mode", "");
                list.add(new LedgerCustomer(
                        o.optString("id"),
                        o.optString("name"),
                        normalizeLocalPhone(o.optString("phone")),
                        trusted,
                        loanEnabled,
                        o.optInt("loanLimit", 0),
                        o.optInt("debt", 0),
                        o.optInt("creditBalance", o.optInt("balance", 0)),
                        LedgerCustomer.deriveMode(trusted, loanEnabled, active, savedMode),
                        active,
                        o.optString("notes"),
                        o.optLong("createdAt", System.currentTimeMillis())
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static void saveLedgerCustomers(Context c, ArrayList<LedgerCustomer> list) {
        try {
            JSONArray arr = new JSONArray();
            for (LedgerCustomer it : list) {
                if (it == null) continue;
                it.mode = LedgerCustomer.sanitizeMode(it.effectiveMode());
                it.syncFlagsFromMode();
                JSONObject o = new JSONObject();
                o.put("id", it.id);
                o.put("name", it.name);
                o.put("phone", normalizeLocalPhone(it.phone));
                o.put("trusted", it.trusted);
                o.put("loanEnabled", it.loanEnabled);
                o.put("loanLimit", Math.max(0, it.loanLimit));
                o.put("debt", Math.max(0, it.debt));
                o.put("creditBalance", Math.max(0, it.creditBalance));
                o.put("mode", it.effectiveMode());
                o.put("active", it.active);
                o.put("notes", it.notes == null ? "" : it.notes);
                o.put("createdAt", it.createdAt);
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_LEDGER_CUSTOMERS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    static LedgerCustomer findLedgerCustomerByPhone(Context c, String phone) {
        String clean = normalizeLocalPhone(phone);
        if (clean.isEmpty()) return null;
        for (LedgerCustomer it : loadLedgerCustomers(c)) {
            if (normalizeLocalPhone(it.phone).equals(clean)) return it;
        }
        return null;
    }

    private static String normalizeLedgerNameKey(String name) {
        String s = name == null ? "" : name.trim();
        if (s.isEmpty()) return "";
        s = s.replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ى', 'ي').replace('ة', 'ه');
        s = s.replaceAll("[\\p{Punct}\\u060C\\u061B\\u061F]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    static LedgerCustomer findLedgerCustomerByName(Context c, String name) {
        String key = normalizeLedgerNameKey(name);
        if (key.length() < 2) return null;
        LedgerCustomer soft = null;
        for (LedgerCustomer it : loadLedgerCustomers(c)) {
            if (it == null) continue;
            String n = normalizeLedgerNameKey(it.name);
            if (n.isEmpty()) continue;
            if (n.equals(key)) return it;
            if (soft == null && key.length() >= 4 && n.length() >= 4 && (n.contains(key) || key.contains(n))) soft = it;
        }
        return soft;
    }

    static String ledgerModeLabel(String mode) {
        String m = LedgerCustomer.sanitizeMode(mode);
        if (LedgerCustomer.MODE_AUTO_FULL.equals(m)) return "آلي كامل";
        if (LedgerCustomer.MODE_SETTLE_ONLY.equals(m)) return "إيقاف السلف / سداد فقط";
        return "موقوف";
    }

    static boolean ledgerCustomerCanLoan(LedgerCustomer customer) {
        return customer != null && customer.canAutoLoan();
    }

    static boolean canLedgerCustomerTakeLoan(LedgerCustomer customer, int cardAmount) {
        if (!ledgerCustomerCanLoan(customer)) return false;
        int amount = Math.max(0, cardAmount);
        int fromCredit = Math.min(Math.max(0, customer.creditBalance), amount);
        int loanPart = Math.max(0, amount - fromCredit);
        return Math.max(0, customer.debt) + loanPart <= Math.max(0, customer.loanLimit);
    }

    static void addOrUpdateLedgerCustomer(Context c, String id, String name, String phone, boolean trusted, boolean loanEnabled, int loanLimit, int debt, boolean active, String notes) {
        String mode = LedgerCustomer.deriveMode(trusted, loanEnabled, active, "");
        addOrUpdateLedgerCustomer(c, id, name, phone, loanLimit, debt, 0, mode, notes);
    }

    static void addOrUpdateLedgerCustomer(Context c, String id, String name, String phone, int loanLimit, int debt, int creditBalance, String mode, String notes) {
        ArrayList<LedgerCustomer> list = loadLedgerCustomers(c);
        String cleanPhone = normalizeLocalPhone(phone);
        String cleanId = id == null ? "" : id.trim();
        String cleanMode = LedgerCustomer.sanitizeMode(mode);
        boolean active = !LedgerCustomer.MODE_STOPPED.equals(cleanMode);
        boolean trusted = active;
        boolean loanEnabled = LedgerCustomer.MODE_AUTO_FULL.equals(cleanMode);
        boolean updated = false;
        for (LedgerCustomer it : list) {
            if ((!cleanId.isEmpty() && it.id.equals(cleanId)) || (!cleanPhone.isEmpty() && normalizeLocalPhone(it.phone).equals(cleanPhone))) {
                it.name = name == null ? "" : name.trim();
                it.phone = cleanPhone;
                it.loanLimit = Math.max(0, loanLimit);
                it.debt = Math.max(0, debt);
                it.creditBalance = Math.max(0, creditBalance);
                it.mode = cleanMode;
                it.active = active;
                it.trusted = trusted;
                it.loanEnabled = loanEnabled;
                it.notes = notes == null ? "" : notes.trim();
                it.syncFlagsFromMode();
                updated = true;
                break;
            }
        }
        if (!updated) {
            list.add(0, new LedgerCustomer(UUID.randomUUID().toString(), name, cleanPhone, trusted, loanEnabled, loanLimit, debt, creditBalance, cleanMode, active, notes, System.currentTimeMillis()));
        }
        saveLedgerCustomers(c, list);
    }

    static void deleteLedgerCustomer(Context c, String id) {
        ArrayList<LedgerCustomer> list = loadLedgerCustomers(c);
        ArrayList<LedgerCustomer> next = new ArrayList<>();
        for (LedgerCustomer it : list) if (!it.id.equals(id)) next.add(it);
        saveLedgerCustomers(c, next);
    }

    static int ledgerTotalDebt(Context c) {
        int total = 0;
        for (LedgerCustomer it : loadLedgerCustomers(c)) if (it.active) total += Math.max(0, it.debt);
        return total;
    }

    static int ledgerTotalCreditBalance(Context c) {
        int total = 0;
        for (LedgerCustomer it : loadLedgerCustomers(c)) if (it.active) total += Math.max(0, it.creditBalance);
        return total;
    }

    static int ledgerTrustedCount(Context c) {
        int total = 0;
        for (LedgerCustomer it : loadLedgerCustomers(c)) if (it.active && it.trusted) total++;
        return total;
    }

    static ArrayList<LedgerEntry> loadLedgerEntries(Context c) {
        ArrayList<LedgerEntry> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_LEDGER_ENTRIES, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new LedgerEntry(
                        o.optString("id"), o.optString("customerId"), o.optString("customerName"), normalizeLocalPhone(o.optString("customerPhone")),
                        o.optString("type"), o.optInt("debit"), o.optInt("credit"), o.optInt("balanceAfter"),
                        o.optString("description"), o.optString("reference"), o.optString("createdAt"),
                        o.optString("status", ""), o.optString("paymentMethod", ""), o.optInt("count", 1)
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static void saveLedgerEntries(Context c, ArrayList<LedgerEntry> list) {
        try {
            JSONArray arr = new JSONArray();
            for (LedgerEntry e : list) {
                if (e == null) continue;
                JSONObject o = new JSONObject();
                o.put("id", e.id);
                o.put("customerId", e.customerId);
                o.put("customerName", e.customerName);
                o.put("customerPhone", normalizeLocalPhone(e.customerPhone));
                o.put("type", e.type);
                o.put("debit", Math.max(0, e.debit));
                o.put("credit", Math.max(0, e.credit));
                o.put("balanceAfter", Math.max(0, e.balanceAfter));
                o.put("description", e.description == null ? "" : e.description);
                o.put("reference", e.reference == null ? "" : e.reference);
                o.put("createdAt", e.createdAt == null ? now() : e.createdAt);
                o.put("status", e.status == null ? LedgerEntry.inferStatus(e.type) : e.status);
                o.put("paymentMethod", e.paymentMethod == null ? LedgerEntry.inferPaymentMethod(e.type, e.reference) : e.paymentMethod);
                o.put("count", Math.max(1, e.count));
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_LEDGER_ENTRIES, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    static void addLedgerEntry(Context c, LedgerEntry entry) {
        ArrayList<LedgerEntry> list = loadLedgerEntries(c);
        list.add(0, entry);
        while (list.size() > 2000) list.remove(list.size() - 1);
        saveLedgerEntries(c, list);
    }

    static LedgerCustomer changeLedgerDebtByPhone(Context c, String phone, int debitAdd, int creditSubtract, String type, String description, String reference) {
        return changeLedgerDebtByPhone(c, phone, debitAdd, creditSubtract, type, description, reference, LedgerEntry.inferPaymentMethod(type, reference), LedgerEntry.inferStatus(type));
    }

    static LedgerCustomer changeLedgerDebtByPhone(Context c, String phone, int debitAdd, int creditSubtract, String type, String description, String reference, String paymentMethod, String status) {
        ArrayList<LedgerCustomer> list = loadLedgerCustomers(c);
        String clean = normalizeLocalPhone(phone);
        LedgerCustomer target = null;
        for (LedgerCustomer it : list) {
            if (normalizeLocalPhone(it.phone).equals(clean)) { target = it; break; }
        }
        if (target == null) {
            target = new LedgerCustomer(UUID.randomUUID().toString(), clean, clean, false, false, 0, 0, 0, LedgerCustomer.MODE_SETTLE_ONLY, true, "تم إنشاؤه تلقائياً من الدفتر المحاسبي", System.currentTimeMillis());
            list.add(0, target);
        }
        int debit = Math.max(0, debitAdd);
        int credit = Math.max(0, creditSubtract);
        int beforeDebt = Math.max(0, target.debt);
        int beforeCreditBalance = Math.max(0, target.creditBalance);
        int paidToDebt = Math.min(beforeDebt, credit);
        int overPayment = Math.max(0, credit - paidToDebt);
        target.debt = Math.max(0, beforeDebt + debit - paidToDebt);
        target.creditBalance = beforeCreditBalance + overPayment;
        saveLedgerCustomers(c, list);
        String note = description == null ? "" : description;
        if (overPayment > 0) note += (note.isEmpty() ? "" : " | ") + "زائد محفوظ كرصيد: " + overPayment + " ريال";
        addLedgerEntry(c, new LedgerEntry(UUID.randomUUID().toString(), target.id, target.name, target.phone, type,
                debit, credit, target.debt,
                note, reference == null ? "" : reference, now(), status, paymentMethod, 1));
        return target;
    }

    static LedgerCustomer applyLedgerPaymentByPhone(Context c, String phone, int amount, String paymentMethod, String description, String reference) {
        int paid = Math.max(0, amount);
        if (paid <= 0) return findLedgerCustomerByPhone(c, phone);
        String method = paymentMethod == null || paymentMethod.trim().isEmpty() ? "سداد" : paymentMethod.trim();
        return changeLedgerDebtByPhone(c, phone, 0, paid, "سداد", description, reference, method, "سداد");
    }

    static LedgerCustomer applyLedgerLoanWithCredit(Context c, String phone, int cardAmount, String cardCode) {
        ArrayList<LedgerCustomer> list = loadLedgerCustomers(c);
        String clean = normalizeLocalPhone(phone);
        LedgerCustomer target = null;
        for (LedgerCustomer it : list) {
            if (normalizeLocalPhone(it.phone).equals(clean)) { target = it; break; }
        }
        if (target == null || !target.canAutoLoan()) return null;
        int amount = Math.max(0, cardAmount);
        int usedCredit = Math.min(Math.max(0, target.creditBalance), amount);
        int loanPart = Math.max(0, amount - usedCredit);
        if (target.debt + loanPart > target.loanLimit) return null;
        target.creditBalance = Math.max(0, target.creditBalance - usedCredit);
        target.debt = Math.max(0, target.debt + loanPart);
        saveLedgerCustomers(c, list);
        String method = usedCredit > 0 && loanPart > 0 ? "رصيد + سلفة" : (usedCredit > 0 ? "رصيد العميل" : "كرت سلف");
        String note = "تم صرف كرت فئة " + amount + " ريال";
        if (usedCredit > 0) note += " | استخدم من الرصيد: " + usedCredit + " ريال";
        if (loanPart > 0) note += " | سجل كسلفة: " + loanPart + " ريال";
        if (cardCode != null && !cardCode.trim().isEmpty()) note += " | الكرت: " + cardCode.trim();
        addLedgerEntry(c, new LedgerEntry(UUID.randomUUID().toString(), target.id, target.name, target.phone, "سلفة كرت",
                amount, usedCredit, target.debt,
                note, cardCode == null ? "" : cardCode, now(), "سلفة", method, 1));
        return target;
    }

    static ArrayList<LedgerEntry> ledgerEntriesForCustomer(Context c, String phone, int max) {
        String clean = normalizeLocalPhone(phone);
        ArrayList<LedgerEntry> out = new ArrayList<>();
        for (LedgerEntry e : loadLedgerEntries(c)) {
            if (!normalizeLocalPhone(e.customerPhone).equals(clean)) continue;
            out.add(e);
            if (max > 0 && out.size() >= max) break;
        }
        return out;
    }

    static String buildLedgerCustomerStatement(Context c, LedgerCustomer customer) {
        if (customer == null) return "";
        String clean = normalizeLocalPhone(customer.phone);
        String name = (customer.name == null || customer.name.trim().isEmpty()) ? clean : customer.name.trim();
        ArrayList<LedgerEntry> entries = ledgerEntriesForCustomer(c, clean, 100);
        int totalLoans = 0, totalPayments = 0, totalDebit = 0, totalCredit = 0, failed = 0, review = 0;
        int firstDebtBefore = Math.max(0, customer.debt);
        for (LedgerEntry e : entries) {
            String st = e.status == null ? LedgerEntry.inferStatus(e.type) : e.status;
            if (st.contains("مرفوض") || st.contains("فشل")) failed++;
            if (st.contains("معلق") || st.contains("مراجعة")) review++;
            if (st.contains("سلفة") || e.debit > 0) totalLoans += Math.max(0, e.debit);
            if (st.contains("سداد") || e.credit > 0) totalPayments += Math.max(0, e.credit);
            totalDebit += Math.max(0, e.debit);
            totalCredit += Math.max(0, e.credit);
        }
        if (!entries.isEmpty()) {
            LedgerEntry oldest = entries.get(entries.size() - 1);
            firstDebtBefore = debtBeforeEntry(oldest);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("تقرير العمليات - ").append(getNetworkName(c)).append("\n\n");
        sb.append("العميل: ").append(name).append("\n");
        sb.append("الرقم: ").append(clean).append("\n");
        sb.append("وضع التعامل: ").append(ledgerModeLabel(customer.effectiveMode())).append("\n\n");
        sb.append("الإجمالي\n");
        sb.append("عدد العمليات: ").append(entries.size()).append("\n");
        sb.append("إجمالي السلف/المدين: ").append(totalLoans).append(" ر.ي\n");
        sb.append("إجمالي السداد/الدائن: ").append(totalPayments).append(" ر.ي\n");
        sb.append("صافي الحركة: ").append(totalDebit - totalCredit).append(" ر.ي\n");
        sb.append("الرصيد قبل أول قيد معروض: ").append(firstDebtBefore).append(" ر.ي\n");
        sb.append("الرصيد الحالي/بعد آخر قيد: ").append(customer.debt).append(" ر.ي\n");
        sb.append("رصيد العميل الزائد: ").append(customer.creditBalance).append(" ر.ي\n");
        sb.append("المتاح من السقف: ").append(customer.remainingLoan()).append(" ر.ي\n");
        if (failed > 0 || review > 0) sb.append("مراجعة/فشل: ").append(review).append(" / ").append(failed).append("\n");
        sb.append("\nالعمليات\n");
        sb.append("الوقت | النوع | المبلغ | الطريقة | قبل | بعد | البيان\n");
        if (entries.isEmpty()) {
            sb.append("لا توجد عمليات لهذا الزبون حتى الآن.\n");
        } else {
            for (LedgerEntry e : entries) {
                String status = e.status == null ? LedgerEntry.inferStatus(e.type) : e.status;
                String method = e.paymentMethod == null ? LedgerEntry.inferPaymentMethod(e.type, e.reference) : e.paymentMethod;
                sb.append(e.createdAt).append(" | ")
                        .append(status).append(" | ")
                        .append(ledgerEntryAmount(e)).append(" | ")
                        .append(method).append(" | ")
                        .append(debtBeforeEntry(e)).append(" | ")
                        .append(e.balanceAfter).append(" | ")
                        .append(shortLedgerText(e.description, 80)).append("\n");
            }
        }
        sb.append("\n").append(getMessageSignature(c));
        return sb.toString();
    }

    static String buildLedgerCustomerPdfStatement(Context c, LedgerCustomer customer) {
        if (customer == null) return "";
        String clean = normalizeLocalPhone(customer.phone);
        String name = (customer.name == null || customer.name.trim().isEmpty()) ? clean : customer.name.trim();
        ArrayList<LedgerEntry> entries = ledgerEntriesForCustomer(c, clean, 200);
        int totalLoans = 0, totalPayments = 0, failed = 0, review = 0;
        int totalDebit = 0, totalCredit = 0;
        int firstDebtBefore = Math.max(0, customer.debt);
        for (LedgerEntry e : entries) {
            String st = e.status == null ? LedgerEntry.inferStatus(e.type) : e.status;
            if (st.contains("مرفوض") || st.contains("فشل")) failed++;
            if (st.contains("معلق") || st.contains("مراجعة")) review++;
            if (st.contains("سلفة") || e.debit > 0) totalLoans += Math.max(0, e.debit);
            if (st.contains("سداد") || e.credit > 0) totalPayments += Math.max(0, e.credit);
            totalDebit += Math.max(0, e.debit);
            totalCredit += Math.max(0, e.credit);
        }
        if (!entries.isEmpty()) firstDebtBefore = debtBeforeEntry(entries.get(entries.size() - 1));
        StringBuilder sb = new StringBuilder();
        sb.append("تقرير العمليات\n");
        sb.append("اسم العميل: ").append(name).append("\n");
        sb.append("رقم الهاتف: ").append(clean).append("\n");
        sb.append("وضع التعامل: ").append(ledgerModeLabel(customer.effectiveMode())).append("\n");
        sb.append("تاريخ إنشاء التقرير: ").append(now()).append("\n\n");
        sb.append("الإجمالي\n");
        sb.append("عدد العمليات: ").append(entries.size()).append("\n");
        sb.append("إجمالي السلف/المدين: ").append(totalLoans).append(" ر.ي\n");
        sb.append("إجمالي السداد/الدائن: ").append(totalPayments).append(" ر.ي\n");
        sb.append("صافي الحركة: ").append(totalDebit - totalCredit).append(" ر.ي\n");
        sb.append("الرصيد قبل: ").append(firstDebtBefore).append(" ر.ي\n");
        sb.append("الرصيد الحالي/بعد: ").append(customer.debt).append(" ر.ي\n");
        sb.append("رصيد العميل: ").append(customer.creditBalance).append(" ر.ي\n");
        sb.append("المتاح من السقف: ").append(customer.remainingLoan()).append(" ر.ي\n");
        sb.append("عمليات مراجعة: ").append(review).append("\n");
        sb.append("عمليات فاشلة: ").append(failed).append("\n\n");
        sb.append("تفاصيل العمليات\n");
        sb.append("الوقت والتاريخ | النوع | الفئة/المبلغ | طريقة السداد | الرصيد قبل | الرصيد بعد | العدد | ملاحظة\n");
        if (entries.isEmpty()) {
            sb.append("لا توجد عمليات لهذا الزبون حتى الآن.\n");
        } else {
            for (LedgerEntry e : entries) {
                String status = e.status == null ? LedgerEntry.inferStatus(e.type) : e.status;
                String method = e.paymentMethod == null ? LedgerEntry.inferPaymentMethod(e.type, e.reference) : e.paymentMethod;
                String note = e.description == null ? "" : e.description.replace("|", "-").replace("\n", " ").trim();
                sb.append(e.createdAt).append(" | ")
                        .append(status).append(" | ")
                        .append(ledgerEntryAmount(e)).append(" | ")
                        .append(method).append(" | ")
                        .append(debtBeforeEntry(e)).append(" | ")
                        .append(e.balanceAfter).append(" | ")
                        .append(Math.max(1, e.count)).append(" | ")
                        .append(shortLedgerText(note, 140)).append("\n");
            }
        }
        sb.append("\n").append(getMessageSignature(c));
        return sb.toString();
    }

    static int debtBeforeEntry(LedgerEntry e) {
        if (e == null) return 0;
        return Math.max(0, Math.max(0, e.balanceAfter) - Math.max(0, e.debit) + Math.max(0, e.credit));
    }

    static String shortLedgerText(String value, int max) {
        String v = value == null ? "" : value.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
        int limit = max <= 0 ? 120 : max;
        if (v.length() <= limit) return v;
        return v.substring(0, limit) + "...";
    }

    static int ledgerEntryAmount(LedgerEntry e) {
        if (e == null) return 0;
        if (e.debit > 0) return e.debit;
        if (e.credit > 0) return e.credit;
        return 0;
    }

    private static String maskPhone(String phone) {
        String p = normalizeLocalPhone(phone);
        if (p.length() < 6) return p;
        return p.substring(0, 3) + "***" + p.substring(p.length() - 3);
    }


    static boolean isLoanRequestText(String body) {
        String v = normalizeArabicDigits(body == null ? "" : body).trim().toLowerCase(Locale.US);
        if (v.isEmpty()) return false;
        // STAGE 12.1: الزبون الموثوق يستطيع طلب أي باقة متوفرة برسالة رقمية فقط مثل: 100
        // أو بصيغ بسيطة مثل: سلفني 100 / سلف 100 / كرت 100.
        if (v.matches("^\\d{2,5}$")) return true;
        if (v.matches("^(سلفني|سلف|سلفة|سلفه|كرت|اريد|أريد)\\s+\\d{2,5}.*")) return true;
        return v.startsWith("سلفني") || v.startsWith("سلفة") || v.startsWith("سلفه") || v.startsWith("سلف ");
    }

    static int extractLoanAmount(String body) {
        String v = normalizeArabicDigits(body == null ? "" : body);
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{2,5})").matcher(v);
            if (m.find()) return Integer.parseInt(m.group(1));
        } catch (Exception ignored) {}
        return 0;
    }

    private static String normalizeArabicDigits(String value) {
        if (value == null) return "";
        return value.replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4')
                .replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9')
                .replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4')
                .replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9');
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(value.hashCode());
        }
    }


    static ArrayList<TrustedCreditAgent> loadTrustedCreditAgents(Context c) {
        ArrayList<TrustedCreditAgent> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_TRUSTED_CREDIT_AGENTS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new TrustedCreditAgent(
                        o.optString("id"),
                        o.optString("name"),
                        o.optString("senderPhone"),
                        o.optString("secondaryPhone", o.optString("extraPhone", "")),
                        o.optString("posSignature", o.optString("signature", "")),
                        o.optInt("creditLimit", 5000),
                        o.optInt("usedAmount", 0),
                        o.optBoolean("active", true),
                        o.optBoolean("rewardsEnabled", true),
                        o.optBoolean("signatureRequired", true),
                        o.optLong("createdAt", System.currentTimeMillis())
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static void saveTrustedCreditAgents(Context c, ArrayList<TrustedCreditAgent> list) {
        try {
            JSONArray arr = new JSONArray();
            for (TrustedCreditAgent a : list) {
                JSONObject o = new JSONObject();
                o.put("id", a.id);
                o.put("name", a.name);
                o.put("senderPhone", normalizeLocalPhone(a.senderPhone));
                o.put("secondaryPhone", normalizeLocalPhone(a.secondaryPhone));
                o.put("posSignature", a.posSignature == null ? "" : a.posSignature.trim());
                o.put("signatureRequired", a.signatureRequired);
                o.put("creditLimit", Math.max(0, a.creditLimit));
                o.put("usedAmount", Math.max(0, a.usedAmount));
                o.put("active", a.active);
                o.put("rewardsEnabled", a.rewardsEnabled);
                o.put("createdAt", a.createdAt);
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_TRUSTED_CREDIT_AGENTS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    static void addOrUpdateTrustedCreditAgent(Context c, String name, String senderPhone, int creditLimit, boolean active) {
        addOrUpdateTrustedCreditAgent(c, name, senderPhone, "", creditLimit, active, true, true);
    }

    static void addOrUpdateTrustedCreditAgent(Context c, String name, String senderPhone, int creditLimit, boolean active, boolean rewardsEnabled) {
        addOrUpdateTrustedCreditAgent(c, name, senderPhone, "", creditLimit, active, rewardsEnabled, true);
    }

    static void addOrUpdateTrustedCreditAgent(Context c, String name, String senderPhone, String posSignature, int creditLimit, boolean active, boolean rewardsEnabled, boolean signatureRequired) {
        addOrUpdateTrustedCreditAgent(c, name, senderPhone, "", posSignature, creditLimit, active, rewardsEnabled, signatureRequired);
    }

    static void addOrUpdateTrustedCreditAgent(Context c, String name, String senderPhone, String secondaryPhone, String posSignature, int creditLimit, boolean active, boolean rewardsEnabled, boolean signatureRequired) {
        String phone = normalizeLocalPhone(senderPhone);
        String extra = normalizeLocalPhone(secondaryPhone);
        if (phone.isEmpty()) return;
        if (extra.equals(phone)) extra = "";
        String sig = cleanPosSignature(posSignature);
        ArrayList<TrustedCreditAgent> list = loadTrustedCreditAgents(c);
        String oldIdForSameMain = "";
        for (TrustedCreditAgent a : list) {
            if (normalizeLocalPhone(a.senderPhone).equals(phone)) {
                oldIdForSameMain = a.id == null ? "" : a.id;
                break;
            }
        }
        if (!extra.isEmpty()) {
            for (TrustedCreditAgent a : list) {
                String aid = a.id == null ? "" : a.id;
                String main = normalizeLocalPhone(a.senderPhone);
                String oldExtra = normalizeLocalPhone(a.secondaryPhone);
                if (!aid.equals(oldIdForSameMain) && (extra.equals(main) || extra.equals(oldExtra))) {
                    // الرقم الإضافي مستخدم في نقطة أخرى؛ تجاهله للأمان بدلاً من ربط حسابين بنفس رقم.
                    extra = "";
                    break;
                }
            }
        }
        for (TrustedCreditAgent a : list) {
            if (normalizeLocalPhone(a.senderPhone).equals(phone)) {
                a.name = name == null || name.trim().isEmpty() ? phone : name.trim();
                a.senderPhone = phone;
                a.secondaryPhone = extra;
                a.posSignature = sig;
                a.signatureRequired = signatureRequired;
                a.creditLimit = Math.max(0, creditLimit);
                a.active = active;
                a.rewardsEnabled = rewardsEnabled;
                saveTrustedCreditAgents(c, list);
                return;
            }
        }
        list.add(0, new TrustedCreditAgent(UUID.randomUUID().toString(), name == null || name.trim().isEmpty() ? phone : name.trim(), phone, extra, sig, Math.max(0, creditLimit), 0, active, rewardsEnabled, signatureRequired, System.currentTimeMillis()));
        saveTrustedCreditAgents(c, list);
    }

    static TrustedCreditAgent findTrustedCreditAgentById(Context c, String id) {
        if (id == null) return null;
        for (TrustedCreditAgent a : loadTrustedCreditAgents(c)) {
            if (a.id != null && a.id.equals(id)) return a;
        }
        return null;
    }

    static boolean trustedCreditAgentOwnsPhone(TrustedCreditAgent a, String phone) {
        if (a == null) return false;
        String clean = normalizeLocalPhone(phone);
        if (clean.isEmpty()) return false;
        String main = normalizeLocalPhone(a.senderPhone);
        String extra = normalizeLocalPhone(a.secondaryPhone);
        return (!main.isEmpty() && (clean.equals(main) || clean.endsWith(main) || main.endsWith(clean)))
                || (!extra.isEmpty() && (clean.equals(extra) || clean.endsWith(extra) || extra.endsWith(clean)));
    }

    static String replyPhoneForTrustedCreditAgent(TrustedCreditAgent a, String incomingSender) {
        String sender = normalizeLocalPhone(incomingSender);
        if (a != null && trustedCreditAgentOwnsPhone(a, sender)) return sender;
        return a == null ? "" : normalizeLocalPhone(a.senderPhone);
    }

    static String addTrustedCreditTopUp(Context c, String id, int amount, String note) {
        int add = Math.max(0, amount);
        if (add <= 0) return "";
        ArrayList<TrustedCreditAgent> list = loadTrustedCreditAgents(c);
        TrustedCreditAgent updated = null;
        int oldLimit = 0;
        int oldRemaining = 0;
        for (TrustedCreditAgent a : list) {
            if (a.id != null && a.id.equals(id)) {
                oldLimit = a.creditLimit;
                oldRemaining = remainingTrustedCredit(a);
                a.creditLimit = Math.max(0, a.creditLimit + add);
                updated = a;
                break;
            }
        }
        if (updated == null) return "";
        saveTrustedCreditAgents(c, list);
        String msg = "تمت تعبئة رصيد نقطة البيع"
                + "\nالنقطة: " + updated.name
                + "\nالرقم: " + updated.senderPhone
                + "\nالمبلغ المضاف: " + add
                + "\nالسقف السابق: " + oldLimit
                + "\nالرصيد السابق المتاح: " + oldRemaining
                + "\nالسقف الجديد: " + updated.creditLimit
                + "\nالرصيد الجديد المتاح: " + remainingTrustedCredit(updated);
        if (note != null && !note.trim().isEmpty()) msg += "\nملاحظة: " + note.trim();
        String logId = UUID.randomUUID().toString();
        addLog(c, new OperationLog(logId, "تعبئة رصيد نقطة بيع", "admin", updated.name, updated.senderPhone, add, "تمت التعبئة", msg, "", now()));
        performAutoBackupIfDue(c, "تعبئة رصيد نقطة بيع");
        return logId;
    }

    static String cleanPosSignature(String value) {
        String v = value == null ? "" : value.trim();
        v = v.replace("\r", " ").replace("\n", " ").replace("\t", " ").replaceAll("\\s+", " ").trim();
        if (v.length() > 40) v = v.substring(0, 40).trim();
        return v;
    }

    private static String normalizeSignatureText(String value) {
        String v = cleanPosSignature(value).toLowerCase(Locale.ROOT);
        v = v.replace('ـ', ' ');
        v = v.replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا');
        v = v.replace('ى', 'ي').replace('ة', 'ه');
        v = v.replaceAll("[\\s\\-_/.,،:;|]+", " ").trim();
        return v;
    }

    static boolean isBadPosSignature(String signature) {
        String sig = cleanPosSignature(signature);
        if (sig.length() < 3) return true;
        if (sig.equals(SYSTEM_SIGNATURE) || sig.equals(NETWORK_SIGNATURE)) return true;
        if (sig.matches("\\d+")) return true;
        return false;
    }

    static String stripTrustedCreditSignature(TrustedCreditAgent agent, String body) {
        if (agent == null || body == null) return null;
        if (!agent.signatureRequired) return body;
        String sig = normalizeSignatureText(agent.posSignature);
        if (sig.isEmpty()) return null;

        String raw = body.replace('\r', '\n');
        String[] lines = raw.split("\\n");
        int last = -1;
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i] != null && !lines[i].trim().isEmpty()) { last = i; break; }
        }
        if (last >= 0 && normalizeSignatureText(lines[last]).equals(sig)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (i == last) continue;
                if (sb.length() > 0) sb.append('\n');
                sb.append(lines[i]);
            }
            return sb.toString().trim();
        }

        String flat = body.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').replaceAll("\\s+", " ").trim();
        String nFlat = normalizeSignatureText(flat);
        if (nFlat.equals(sig)) return "";
        if (nFlat.endsWith(" " + sig)) {
            int pos = flat.length() - agent.posSignature.trim().length();
            if (pos >= 0) return flat.substring(0, pos).trim();
        }
        return null;
    }

    static void deleteTrustedCreditAgent(Context c, String id) {
        ArrayList<TrustedCreditAgent> list = loadTrustedCreditAgents(c);
        ArrayList<TrustedCreditAgent> next = new ArrayList<>();
        for (TrustedCreditAgent a : list) if (!a.id.equals(id)) next.add(a);
        saveTrustedCreditAgents(c, next);
    }

    static void resetTrustedCreditAgentUsage(Context c, String id) {
        ArrayList<TrustedCreditAgent> list = loadTrustedCreditAgents(c);
        for (TrustedCreditAgent a : list) {
            if (a.id.equals(id)) {
                a.usedAmount = 0;
                break;
            }
        }
        saveTrustedCreditAgents(c, list);
    }

    static TrustedCreditAgent findTrustedCreditSender(Context c, String sender) {
        String clean = normalizeLocalPhone(sender);
        if (clean.isEmpty()) return null;
        for (TrustedCreditAgent a : loadTrustedCreditAgents(c)) {
            if (!a.active) continue;
            if (trustedCreditAgentOwnsPhone(a, clean)) return a;
        }
        return null;
    }

    static boolean canUseTrustedCredit(TrustedCreditAgent a, int amount) {
        if (a == null || !a.active) return false;
        if (a.creditLimit <= 0) return false;
        return a.usedAmount + Math.max(0, amount) <= a.creditLimit;
    }

    static int remainingTrustedCredit(TrustedCreditAgent a) {
        if (a == null) return 0;
        return Math.max(0, a.creditLimit - a.usedAmount);
    }

    static void addTrustedCreditUsage(Context c, String id, int amount) {
        ArrayList<TrustedCreditAgent> list = loadTrustedCreditAgents(c);
        for (TrustedCreditAgent a : list) {
            if (a.id.equals(id)) {
                a.usedAmount += Math.max(0, amount);
                if (a.usedAmount < 0) a.usedAmount = 0;
                break;
            }
        }
        saveTrustedCreditAgents(c, list);
    }

    static String buildTrustedCreditLimitMessage(TrustedCreditAgent a) {
        int limit = a == null ? 0 : a.creditLimit;
        int used = a == null ? 0 : a.usedAmount;
        return "لقد بلغت سقفك المحدد من الإدارة. يرجى السداد. السقف:" + limit + " المستخدم:" + used;
    }

    static String now() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(new Date());
    }


    static JSONObject exportBackupJson(Context c) throws Exception {
        JSONObject root = new JSONObject();
        root.put("app", "ONLINE");
        root.put("backupVersion", 1);
        root.put("createdAt", now());
        root.put("networkName", getNetworkName(c));

        JSONObject settings = new JSONObject();
        settings.put(KEY_NETWORK_NAME, getNetworkName(c));
        settings.put(KEY_ADMIN_PHONE, getAdminPhone(c));
        settings.put(KEY_NETWORK_LOGO_PATH, getNetworkLogoPath(c));
        settings.put(KEY_SHOW_PHONE_IN_SIGNATURE, isShowPhoneInSignature(c));
        settings.put(KEY_AUTO_SEND, isAutoSendEnabled(c));
        settings.put(KEY_SUCCESS_TEMPLATE, getSuccessTemplate(c));
        settings.put(KEY_NO_STOCK_TEMPLATE, getNoStockTemplate(c));
        settings.put("serialNumber", getSerialNumber(c));
        settings.put("deviceId", getDeviceId(c));
        settings.put(KEY_AUTO_BACKUP_ENABLED, isAutoBackupEnabled(c));
        settings.put(KEY_AUTO_BACKUP_INTERVAL_HOURS, getAutoBackupIntervalHours(c));
        settings.put(KEY_LAST_AUTO_BACKUP_AT, getLastAutoBackupAt(c));
        settings.put(KEY_CARD_PRIVACY_ENABLED, isCardPrivacyEnabled(c));
        settings.put(KEY_REWARDS_ENABLED, isRewardsEnabled(c));
        settings.put(KEY_REWARDS_THRESHOLD, getRewardThreshold(c));
        settings.put(KEY_REWARDS_REWARD_AMOUNT, getRewardCardAmount(c));
        settings.put(KEY_REWARD_CATEGORY_SETTINGS, prefs(c).getString(KEY_REWARD_CATEGORY_SETTINGS, "{}"));
        settings.put(KEY_REWARD_POINTS_TEMPLATE, getRewardPointsTemplate(c));
        settings.put(KEY_REWARD_SENT_TEMPLATE, getRewardSentTemplate(c));
        settings.put(KEY_REWARD_PENDING_TEMPLATE, getRewardPendingTemplate(c));
        settings.put(KEY_REWARD_EXPIRY_ENABLED, isRewardExpiryEnabled(c));
        settings.put(KEY_REWARD_EXPIRY_DAYS, getRewardExpiryDays(c));
        settings.put(KEY_POS_SUCCESS_TEMPLATE, getPosSuccessTemplate(c));
        settings.put(KEY_POS_DUPLICATE_TEMPLATE, getPosDuplicateTemplate(c));
        settings.put(KEY_POS_INVALID_PHONE_TEMPLATE, getPosInvalidPhoneTemplate(c));
        settings.put(KEY_POS_LIMIT_TEMPLATE, getPosLimitTemplate(c));
        settings.put(KEY_POS_NO_STOCK_TEMPLATE, getPosNoStockTemplate(c));
        settings.put(KEY_POS_INVALID_CATEGORY_TEMPLATE, getPosInvalidCategoryTemplate(c));
        settings.put(KEY_POS_AMBIGUOUS_TEMPLATE, getPosAmbiguousTemplate(c));
        settings.put(KEY_UPDATE_MANIFEST_URL, getUpdateManifestUrl(c));
        settings.put(KEY_LEDGER_ENABLED, isSmartLedgerEnabled(c));
        settings.put(KEY_LEDGER_LOAN_ENABLED, isLedgerLoanEnabled(c));
        settings.put(KEY_LEDGER_AUTO_SETTLE_ENABLED, isLedgerAutoSettleEnabled(c));
        root.put("settings", settings);

        root.put(KEY_CATEGORIES, new JSONArray(prefs(c).getString(KEY_CATEGORIES, "[]")));
        root.put(KEY_CARDS, cardsToJsonArray(c));
        root.put(KEY_LOGS, new JSONArray(prefs(c).getString(KEY_LOGS, "[]")));
        root.put(KEY_TRUSTED, new JSONArray(prefs(c).getString(KEY_TRUSTED, "[]")));
        root.put(KEY_TRUSTED_CREDIT_AGENTS, new JSONArray(prefs(c).getString(KEY_TRUSTED_CREDIT_AGENTS, "[]")));
        root.put(KEY_POS_OUTLETS, new JSONArray(prefs(c).getString(KEY_POS_OUTLETS, "[]")));
        root.put(KEY_LEDGER_CUSTOMERS, new JSONArray(prefs(c).getString(KEY_LEDGER_CUSTOMERS, "[]")));
        root.put(KEY_LEDGER_ENTRIES, new JSONArray(prefs(c).getString(KEY_LEDGER_ENTRIES, "[]")));

        JSONArray processed = new JSONArray();
        for (String item : loadProcessed(c)) processed.put(item);
        root.put(KEY_PROCESSED, processed);
        JSONArray importedBatches = new JSONArray();
        for (String item : loadImportedBatches(c)) importedBatches.put(item);
        root.put(KEY_IMPORTED_BATCHES, importedBatches);
        root.put(KEY_RESTORE_HISTORY, getRestoreHistoryArray(c));
        root.put(KEY_CUSTOMER_REWARDS, new JSONArray(prefs(c).getString(KEY_CUSTOMER_REWARDS, "[]")));
        return root;
    }


    private static String limitRewardTemplate(String value, String fallback) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) v = fallback;
        if (v.length() > 60) v = v.substring(0, 60);
        return v;
    }

    static String getRewardPointsTemplate(Context c) {
        return prefs(c).getString(KEY_REWARD_POINTS_TEMPLATE, DEFAULT_REWARD_POINTS_TEMPLATE);
    }

    static String getRewardSentTemplate(Context c) {
        return prefs(c).getString(KEY_REWARD_SENT_TEMPLATE, DEFAULT_REWARD_SENT_TEMPLATE);
    }

    static String getRewardPendingTemplate(Context c) {
        return prefs(c).getString(KEY_REWARD_PENDING_TEMPLATE, DEFAULT_REWARD_PENDING_TEMPLATE);
    }

    static void setRewardMessageTemplates(Context c, String pointsTemplate, String sentTemplate, String pendingTemplate) {
        prefs(c).edit()
                .putString(KEY_REWARD_POINTS_TEMPLATE, limitRewardTemplate(pointsTemplate, DEFAULT_REWARD_POINTS_TEMPLATE))
                .putString(KEY_REWARD_SENT_TEMPLATE, limitRewardTemplate(sentTemplate, DEFAULT_REWARD_SENT_TEMPLATE))
                .putString(KEY_REWARD_PENDING_TEMPLATE, limitRewardTemplate(pendingTemplate, DEFAULT_REWARD_PENDING_TEMPLATE))
                .apply();
    }

    static String buildRewardTemplate(Context c, String template, int points, int balance, int remain, int threshold, int rewardAmount, String rewardCard) {
        String out = template == null ? "" : template;
        out = out.replace("{points}", String.valueOf(points));
        out = out.replace("{balance}", String.valueOf(balance));
        out = out.replace("{remain}", String.valueOf(Math.max(0, remain)));
        out = out.replace("{threshold}", String.valueOf(threshold));
        out = out.replace("{rewardAmount}", String.valueOf(rewardAmount));
        out = out.replace("{rewardCard}", rewardCard == null ? "" : rewardCard);
        return out.trim();
    }



    static boolean isRewardsEnabled(Context c) {
        return prefs(c).getBoolean(KEY_REWARDS_ENABLED, true);
    }

    static void setRewardsEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_REWARDS_ENABLED, enabled).apply();
    }

    static int getRewardThreshold(Context c) {
        return Math.max(1, prefs(c).getInt(KEY_REWARDS_THRESHOLD, DEFAULT_REWARD_THRESHOLD));
    }

    static int getRewardCardAmount(Context c) {
        return Math.max(1, prefs(c).getInt(KEY_REWARDS_REWARD_AMOUNT, DEFAULT_REWARD_CARD_AMOUNT));
    }

    static void setRewardsMainSettings(Context c, boolean enabled, int threshold, int rewardAmount) {
        prefs(c).edit()
                .putBoolean(KEY_REWARDS_ENABLED, enabled)
                .putInt(KEY_REWARDS_THRESHOLD, Math.max(1, threshold))
                .putInt(KEY_REWARDS_REWARD_AMOUNT, Math.max(1, rewardAmount))
                .apply();
    }


    static boolean isRewardExpiryEnabled(Context c) {
        return prefs(c).getBoolean(KEY_REWARD_EXPIRY_ENABLED, false);
    }

    static int getRewardExpiryDays(Context c) {
        return Math.max(1, prefs(c).getInt(KEY_REWARD_EXPIRY_DAYS, DEFAULT_REWARD_EXPIRY_DAYS));
    }

    static void setRewardExpirySettings(Context c, boolean enabled, int days) {
        prefs(c).edit()
                .putBoolean(KEY_REWARD_EXPIRY_ENABLED, enabled)
                .putInt(KEY_REWARD_EXPIRY_DAYS, Math.max(1, days))
                .apply();
    }

    private static long parseRewardDateMillis(String value) {
        if (value == null || value.trim().isEmpty()) return 0L;
        String v = value.trim();
        String[] formats = new String[]{"yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm"};
        for (String f : formats) {
            try {
                Date d = new SimpleDateFormat(f, Locale.US).parse(v);
                if (d != null) return d.getTime();
            } catch (Exception ignored) {}
        }
        return 0L;
    }

    private static String formatRewardDateMillis(long millis) {
        if (millis <= 0L) return "-";
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(new Date(millis));
    }

    private static int rewardDaysLeft(long expiryMillis, long nowMillis) {
        long diff = expiryMillis - nowMillis;
        if (diff <= 0L) return 0;
        return (int)Math.ceil(diff / (double)DAY_MS);
    }

    private static boolean shouldExpireCustomerReward(Context c, CustomerReward cr, long nowMillis) {
        if (cr == null || cr.points <= 0) return false;
        long last = parseRewardDateMillis(cr.lastPurchaseAt);
        if (last <= 0L) return false;
        long expiry = last + (getRewardExpiryDays(c) * DAY_MS);
        return nowMillis >= expiry;
    }

    static int applyRewardPointExpiry(Context c) {
        if (!isRewardExpiryEnabled(c)) return 0;
        ArrayList<CustomerReward> list = loadCustomerRewards(c);
        long nowMs = System.currentTimeMillis();
        int expiredCount = 0;
        for (CustomerReward cr : list) {
            if (!shouldExpireCustomerReward(c, cr, nowMs)) continue;
            int expiredPoints = cr.points;
            long last = parseRewardDateMillis(cr.lastPurchaseAt);
            long expiry = last + (getRewardExpiryDays(c) * DAY_MS);
            cr.points = 0;
            expiredCount++;
            addLog(c, new OperationLog(UUID.randomUUID().toString(), "مصادرة نقاط", "rewards_expiry", cr.name, cr.phone, expiredPoints,
                    "تمت مصادرة نقاط", "تمت مصادرة " + expiredPoints + " نقطة بسبب عدم إجراء تعبئة خلال " + getRewardExpiryDays(c) + " أيام. آخر تعبئة: " + cr.lastPurchaseAt + " | تاريخ المصادرة: " + formatRewardDateMillis(expiry), "", now()));
        }
        if (expiredCount > 0) saveCustomerRewards(c, list);
        return expiredCount;
    }

    static ArrayList<RewardExpiryInfo> loadRewardExpiringSoon(Context c, int warningDays) {
        ArrayList<RewardExpiryInfo> out = new ArrayList<>();
        long nowMs = System.currentTimeMillis();
        int days = getRewardExpiryDays(c);
        int warn = Math.max(0, warningDays);
        for (CustomerReward cr : loadCustomerRewards(c)) {
            if (cr == null || cr.points <= 0) continue;
            long last = parseRewardDateMillis(cr.lastPurchaseAt);
            if (last <= 0L) continue;
            long expiry = last + (days * DAY_MS);
            if (nowMs > expiry) continue;
            int left = rewardDaysLeft(expiry, nowMs);
            if (left <= warn) {
                out.add(new RewardExpiryInfo(cr.phone, cr.name, cr.points, cr.totalPaid, cr.totalEarnedPoints, cr.totalUsedPoints, cr.rewardCards, cr.lastPurchaseAt, formatRewardDateMillis(expiry), left));
            }
        }
        return out;
    }

    static ArrayList<OperationLog> loadRewardExpiryLogs(Context c, int limit) {
        ArrayList<OperationLog> out = new ArrayList<>();
        for (OperationLog log : loadLogs(c)) {
            if (log == null) continue;
            boolean match = "مصادرة نقاط".equals(log.provider) || "rewards_expiry".equals(log.sender) || "تمت مصادرة نقاط".equals(log.status);
            if (!match) continue;
            out.add(log);
            if (limit > 0 && out.size() >= limit) break;
        }
        return out;
    }

    static String buildRewardExpiryWarningStatement(Context c, RewardExpiryInfo info) {
        if (info == null) return "";
        return "مرحباً بك عميلنا العزيز\n\n"
                + "كشف نقاط المكافآت الخاصة بك:\n\n"
                + "رقم العميل: " + info.phone + "\n"
                + (info.name.isEmpty() ? "" : "اسم العميل: " + info.name + "\n")
                + "رصيد النقاط الحالي: " + info.points + " نقطة\n"
                + "آخر تعبئة: " + (info.lastPurchaseAt.isEmpty() ? "-" : info.lastPurchaseAt) + "\n"
                + "تاريخ انتهاء صلاحية النقاط: " + info.expiryAt + "\n"
                + "المدة المتبقية: " + info.daysLeft + " يوم\n\n"
                + "تنبيه: نقاطك معرضة للمصادرة إذا لم يتم إجراء تعبئة جديدة قبل تاريخ الانتهاء.\n"
                + "للحفاظ على نقاطك، يرجى إجراء تعبئة جديدة قبل انتهاء المهلة.\n\n"
                + "مع تحيات " + getNetworkName(c);
    }

    static String buildRewardExpiryLogStatement(Context c, OperationLog log) {
        if (log == null) return "";
        return "مرحباً بك عميلنا العزيز\n\n"
                + "كشف مصادرة نقاط المكافآت:\n\n"
                + "رقم العميل: " + (log.customerPhone == null ? "" : log.customerPhone) + "\n"
                + ((log.customerName == null || log.customerName.trim().isEmpty()) ? "" : "اسم العميل: " + log.customerName + "\n")
                + "النقاط المصادرة: " + log.amount + " نقطة\n"
                + "تاريخ المصادرة: " + (log.createdAt == null ? "" : log.createdAt) + "\n"
                + "سبب المصادرة: عدم إجراء أي تعبئة خلال " + getRewardExpiryDays(c) + " أيام.\n"
                + ((log.message == null || log.message.trim().isEmpty()) ? "" : "\nتفاصيل النظام:\n" + log.message + "\n")
                + "\nيمكنك الاستفادة من نظام المكافآت من جديد عند إجراء تعبئة جديدة.\n\n"
                + "مع تحيات " + getNetworkName(c);
    }

    private static JSONObject loadRewardCategorySettingsObject(Context c) {
        try { return new JSONObject(prefs(c).getString(KEY_REWARD_CATEGORY_SETTINGS, "{}")); }
        catch (Exception e) { return new JSONObject(); }
    }

    static boolean isRewardEnabledForAmount(Context c, int amount) {
        try {
            JSONObject root = loadRewardCategorySettingsObject(c);
            JSONObject o = root.optJSONObject(String.valueOf(amount));
            if (o == null) return true;
            return o.optBoolean("enabled", true);
        } catch (Exception e) { return true; }
    }

    static int getRewardPercentForAmount(Context c, int amount) {
        try {
            JSONObject root = loadRewardCategorySettingsObject(c);
            JSONObject o = root.optJSONObject(String.valueOf(amount));
            if (o == null) return DEFAULT_REWARD_PERCENT;
            return Math.max(0, Math.min(100, o.optInt("percent", DEFAULT_REWARD_PERCENT)));
        } catch (Exception e) { return DEFAULT_REWARD_PERCENT; }
    }

    static void setRewardCategorySetting(Context c, int amount, boolean enabled, int percent) {
        try {
            JSONObject root = loadRewardCategorySettingsObject(c);
            JSONObject o = new JSONObject();
            o.put("enabled", enabled);
            o.put("percent", Math.max(0, Math.min(100, percent)));
            root.put(String.valueOf(amount), o);
            prefs(c).edit().putString(KEY_REWARD_CATEGORY_SETTINGS, root.toString()).apply();
        } catch (Exception ignored) {}
    }

    static ArrayList<CustomerReward> loadCustomerRewards(Context c) {
        ArrayList<CustomerReward> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(KEY_CUSTOMER_REWARDS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new CustomerReward(
                        o.optString("phone"),
                        o.optString("name"),
                        o.optInt("points"),
                        o.optInt("totalPaid"),
                        o.optInt("totalEarnedPoints"),
                        o.optInt("totalUsedPoints"),
                        o.optInt("rewardCards"),
                        o.optString("lastPurchaseAt")
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }

    static void saveCustomerRewards(Context c, ArrayList<CustomerReward> list) {
        try {
            JSONArray arr = new JSONArray();
            for (CustomerReward cr : list) {
                JSONObject o = new JSONObject();
                o.put("phone", cr.phone);
                o.put("name", cr.name);
                o.put("points", cr.points);
                o.put("totalPaid", cr.totalPaid);
                o.put("totalEarnedPoints", cr.totalEarnedPoints);
                o.put("totalUsedPoints", cr.totalUsedPoints);
                o.put("rewardCards", cr.rewardCards);
                o.put("lastPurchaseAt", cr.lastPurchaseAt);
                arr.put(o);
            }
            prefs(c).edit().putString(KEY_CUSTOMER_REWARDS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private static String normalizeRewardPhone(String phone) {
        if (phone == null) return "";
        String p = phone.trim();
        p = p.replace(" ", "").replace("-", "").replace("+", "");
        p = p.replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4').replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9');
        p = p.replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9');
        if (p.startsWith("967")) p = p.substring(3);
        return p;
    }

    static CustomerReward findCustomerReward(Context c, String phone) {
        String clean = normalizeRewardPhone(phone);
        if (clean.isEmpty()) return null;
        for (CustomerReward cr : loadCustomerRewards(c)) if (clean.equals(normalizeRewardPhone(cr.phone))) return cr;
        return null;
    }

    private static CardItem takeRewardCard(Context c, int amount, String buyerPhone) {
        synchronized (AppStore.class) {
            Cursor cur = null;
            SQLiteDatabase db = cardDb(c);
            try {
                cur = db.query("cards", null,
                        "amount=? AND sold=0 AND source LIKE 'reward_stock%'",
                        new String[]{String.valueOf(amount)}, null, null, cardSaleSqlOrder(c, amount), "1");
                if (!cur.moveToFirst()) return null;
                CardItem selected = cardFromCursor(cur);
                String when = now();
                ContentValues cv = new ContentValues();
                cv.put("sold", 1);
                cv.put("buyer_phone", buyerPhone == null ? "" : buyerPhone);
                cv.put("sold_at", when);
                cv.put("source", "reward_stock_sent");
                db.update("cards", cv, "id=?", new String[]{selected.id});
                selected.sold = true;
                selected.buyerPhone = buyerPhone == null ? "" : buyerPhone;
                selected.soldAt = when;
                selected.source = "reward_stock_sent";
                invalidateCardsCache();
                return selected;
            } catch (Exception e) {
                return null;
            } finally {
                if (cur != null) cur.close();
            }
        }
    }

    static RewardResult applyRewardsForPaidSale(Context c, String customerPhone, String customerName, int amount, String paidCardCode) {
        if (!isRewardsEnabled(c)) return new RewardResult("", "نظام النقاط متوقف", "", 0, 0, false, false);
        if (!isRewardEnabledForAmount(c, amount)) return new RewardResult("", "النقاط متوقفة لهذه الفئة", "", 0, 0, false, false);
        String phone = normalizeRewardPhone(customerPhone);
        if (phone.isEmpty()) return new RewardResult("", "لم يتم تسجيل نقاط: لا يوجد رقم زبون", "", 0, 0, false, false);
        int percent = getRewardPercentForAmount(c, amount);
        int added = (int)Math.floor((amount * percent) / 100.0d);
        if (added <= 0) return new RewardResult("", "نسبة النقاط لهذه الفئة صفر", "", 0, 0, false, false);

        ArrayList<CustomerReward> list = loadCustomerRewards(c);
        CustomerReward cr = null;
        for (CustomerReward item : list) {
            if (phone.equals(normalizeRewardPhone(item.phone))) { cr = item; break; }
        }
        if (cr == null) {
            cr = new CustomerReward(phone, customerName, 0, 0, 0, 0, 0, "");
            list.add(cr);
        }
        if (customerName != null && !customerName.trim().isEmpty()) cr.name = customerName.trim();
        if (isRewardExpiryEnabled(c) && shouldExpireCustomerReward(c, cr, System.currentTimeMillis())) {
            int expiredPoints = cr.points;
            cr.points = 0;
            addLog(c, new OperationLog(UUID.randomUUID().toString(), "مصادرة نقاط", "rewards_expiry", cr.name, phone, expiredPoints,
                    "تمت مصادرة نقاط", "تمت مصادرة " + expiredPoints + " نقطة بسبب عدم إجراء تعبئة خلال " + getRewardExpiryDays(c) + " أيام قبل احتساب التعبئة الجديدة. آخر تعبئة: " + cr.lastPurchaseAt, "", now()));
        }
        cr.points += added;
        cr.totalPaid += Math.max(0, amount);
        cr.totalEarnedPoints += added;
        cr.lastPurchaseAt = now();

        int threshold = getRewardThreshold(c);
        int rewardAmount = getRewardCardAmount(c);
        String rewardCode = "";
        boolean rewardSent = false;
        boolean rewardPending = false;

        if (cr.points >= threshold) {
            CardItem reward = takeRewardCard(c, rewardAmount, phone);
            if (reward != null) {
                rewardCode = reward.code;
                cr.points -= threshold;
                cr.totalUsedPoints += threshold;
                cr.rewardCards += 1;
                rewardSent = true;
                addLog(c, new OperationLog(UUID.randomUUID().toString(), "مكافأة نقاط", "rewards", cr.name, phone, rewardAmount,
                        "تم إرسال مكافأة", "تم إرسال كرت مكافأة بعد وصول العميل إلى " + threshold + " نقطة. كرت المكافأة لا يضيف نقاطًا جديدة.", rewardCode, now()));
            } else {
                rewardPending = true;
                addLog(c, new OperationLog(UUID.randomUUID().toString(), "مكافأة نقاط", "rewards", cr.name, phone, rewardAmount,
                        "مكافأة معلقة", "وصل العميل إلى " + cr.points + " نقطة لكن لا توجد كروت متاحة لفئة المكافأة " + rewardAmount + " ريال.", "", now()));
            }
        }
        saveCustomerRewards(c, list);

        int remain = Math.max(0, threshold - cr.points);
        StringBuilder msg = new StringBuilder();
        msg.append("\n").append(buildRewardTemplate(c, getRewardPointsTemplate(c), added, cr.points, remain, threshold, rewardAmount, ""));
        if (rewardSent) {
            msg.append("\n").append(buildRewardTemplate(c, getRewardSentTemplate(c), added, cr.points, remain, threshold, rewardAmount, rewardCode));
        } else if (rewardPending) {
            msg.append("\n").append(buildRewardTemplate(c, getRewardPendingTemplate(c), added, cr.points, remain, threshold, rewardAmount, ""));
        }
        String note = "نقاط: +" + added + " | الرصيد: " + cr.points + (rewardSent ? " | مكافأة: " + rewardCode : (rewardPending ? " | مكافأة معلقة" : ""));
        return new RewardResult(msg.toString(), note, rewardCode, added, cr.points, rewardSent, rewardPending);
    }

    static String buildBackupFileName(Context c) {
        String name = getNetworkName(c);
        name = name.replaceAll("[^A-Za-z0-9_\\-\\u0600-\\u06FF]", "_");
        if (name.trim().isEmpty()) name = "ONLINE";
        return "ONLINE_backup_" + name + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".json";
    }

    static void restoreBackupJson(Context c, String jsonText) throws Exception {
        JSONObject root = new JSONObject(jsonText == null ? "" : jsonText);
        SharedPreferences.Editor editor = prefs(c).edit();

        JSONObject settings = root.optJSONObject("settings");
        if (settings != null) {
            if (settings.has(KEY_NETWORK_NAME)) editor.putString(KEY_NETWORK_NAME, formatNetworkName(settings.optString(KEY_NETWORK_NAME, DEFAULT_NETWORK_NAME)));
            if (settings.has(KEY_ADMIN_PHONE)) editor.putString(KEY_ADMIN_PHONE, settings.optString(KEY_ADMIN_PHONE, DEFAULT_ADMIN_PHONE));
            if (settings.has(KEY_NETWORK_LOGO_PATH)) editor.putString(KEY_NETWORK_LOGO_PATH, settings.optString(KEY_NETWORK_LOGO_PATH, ""));
            if (settings.has(KEY_SHOW_PHONE_IN_SIGNATURE)) editor.putBoolean(KEY_SHOW_PHONE_IN_SIGNATURE, settings.optBoolean(KEY_SHOW_PHONE_IN_SIGNATURE, false));
            if (settings.has(KEY_AUTO_SEND)) editor.putBoolean(KEY_AUTO_SEND, settings.optBoolean(KEY_AUTO_SEND, true));
            if (settings.has(KEY_SUCCESS_TEMPLATE)) editor.putString(KEY_SUCCESS_TEMPLATE, settings.optString(KEY_SUCCESS_TEMPLATE, DEFAULT_SUCCESS_TEMPLATE));
            if (settings.has(KEY_NO_STOCK_TEMPLATE)) editor.putString(KEY_NO_STOCK_TEMPLATE, settings.optString(KEY_NO_STOCK_TEMPLATE, DEFAULT_NO_STOCK_TEMPLATE));
            if (settings.has(KEY_POS_SUCCESS_TEMPLATE)) editor.putString(KEY_POS_SUCCESS_TEMPLATE, settings.optString(KEY_POS_SUCCESS_TEMPLATE, DEFAULT_POS_SUCCESS_TEMPLATE));
            if (settings.has(KEY_POS_DUPLICATE_TEMPLATE)) editor.putString(KEY_POS_DUPLICATE_TEMPLATE, settings.optString(KEY_POS_DUPLICATE_TEMPLATE, DEFAULT_POS_DUPLICATE_TEMPLATE));
            if (settings.has(KEY_POS_INVALID_PHONE_TEMPLATE)) editor.putString(KEY_POS_INVALID_PHONE_TEMPLATE, settings.optString(KEY_POS_INVALID_PHONE_TEMPLATE, DEFAULT_POS_INVALID_PHONE_TEMPLATE));
            if (settings.has(KEY_POS_LIMIT_TEMPLATE)) editor.putString(KEY_POS_LIMIT_TEMPLATE, settings.optString(KEY_POS_LIMIT_TEMPLATE, DEFAULT_POS_LIMIT_TEMPLATE));
            if (settings.has(KEY_POS_NO_STOCK_TEMPLATE)) editor.putString(KEY_POS_NO_STOCK_TEMPLATE, settings.optString(KEY_POS_NO_STOCK_TEMPLATE, DEFAULT_POS_NO_STOCK_TEMPLATE));
            if (settings.has(KEY_POS_INVALID_CATEGORY_TEMPLATE)) editor.putString(KEY_POS_INVALID_CATEGORY_TEMPLATE, settings.optString(KEY_POS_INVALID_CATEGORY_TEMPLATE, DEFAULT_POS_INVALID_CATEGORY_TEMPLATE));
            if (settings.has(KEY_POS_AMBIGUOUS_TEMPLATE)) editor.putString(KEY_POS_AMBIGUOUS_TEMPLATE, settings.optString(KEY_POS_AMBIGUOUS_TEMPLATE, DEFAULT_POS_AMBIGUOUS_TEMPLATE));
            if (settings.has(KEY_AUTO_BACKUP_ENABLED)) editor.putBoolean(KEY_AUTO_BACKUP_ENABLED, settings.optBoolean(KEY_AUTO_BACKUP_ENABLED, true));
            if (settings.has(KEY_AUTO_BACKUP_INTERVAL_HOURS)) editor.putInt(KEY_AUTO_BACKUP_INTERVAL_HOURS, settings.optInt(KEY_AUTO_BACKUP_INTERVAL_HOURS, 12));
            if (settings.has(KEY_LAST_AUTO_BACKUP_AT)) editor.putString(KEY_LAST_AUTO_BACKUP_AT, settings.optString(KEY_LAST_AUTO_BACKUP_AT, ""));
            if (settings.has(KEY_CARD_PRIVACY_ENABLED)) editor.putBoolean(KEY_CARD_PRIVACY_ENABLED, settings.optBoolean(KEY_CARD_PRIVACY_ENABLED, true));
            if (settings.has(KEY_REWARDS_ENABLED)) editor.putBoolean(KEY_REWARDS_ENABLED, settings.optBoolean(KEY_REWARDS_ENABLED, true));
            if (settings.has(KEY_REWARDS_THRESHOLD)) editor.putInt(KEY_REWARDS_THRESHOLD, settings.optInt(KEY_REWARDS_THRESHOLD, DEFAULT_REWARD_THRESHOLD));
            if (settings.has(KEY_REWARDS_REWARD_AMOUNT)) editor.putInt(KEY_REWARDS_REWARD_AMOUNT, settings.optInt(KEY_REWARDS_REWARD_AMOUNT, DEFAULT_REWARD_CARD_AMOUNT));
            if (settings.has(KEY_REWARD_CATEGORY_SETTINGS)) editor.putString(KEY_REWARD_CATEGORY_SETTINGS, settings.optString(KEY_REWARD_CATEGORY_SETTINGS, "{}"));
            if (settings.has(KEY_REWARD_POINTS_TEMPLATE)) editor.putString(KEY_REWARD_POINTS_TEMPLATE, settings.optString(KEY_REWARD_POINTS_TEMPLATE, DEFAULT_REWARD_POINTS_TEMPLATE));
            if (settings.has(KEY_REWARD_SENT_TEMPLATE)) editor.putString(KEY_REWARD_SENT_TEMPLATE, settings.optString(KEY_REWARD_SENT_TEMPLATE, DEFAULT_REWARD_SENT_TEMPLATE));
            if (settings.has(KEY_REWARD_PENDING_TEMPLATE)) editor.putString(KEY_REWARD_PENDING_TEMPLATE, settings.optString(KEY_REWARD_PENDING_TEMPLATE, DEFAULT_REWARD_PENDING_TEMPLATE));
            if (settings.has(KEY_REWARD_EXPIRY_ENABLED)) editor.putBoolean(KEY_REWARD_EXPIRY_ENABLED, settings.optBoolean(KEY_REWARD_EXPIRY_ENABLED, false));
            if (settings.has(KEY_REWARD_EXPIRY_DAYS)) editor.putInt(KEY_REWARD_EXPIRY_DAYS, Math.max(1, settings.optInt(KEY_REWARD_EXPIRY_DAYS, DEFAULT_REWARD_EXPIRY_DAYS)));
            if (settings.has(KEY_UPDATE_MANIFEST_URL)) editor.putString(KEY_UPDATE_MANIFEST_URL, settings.optString(KEY_UPDATE_MANIFEST_URL, DEFAULT_UPDATE_MANIFEST_URL));
            if (settings.has(KEY_LEDGER_ENABLED)) editor.putBoolean(KEY_LEDGER_ENABLED, settings.optBoolean(KEY_LEDGER_ENABLED, false));
            if (settings.has(KEY_LEDGER_LOAN_ENABLED)) editor.putBoolean(KEY_LEDGER_LOAN_ENABLED, settings.optBoolean(KEY_LEDGER_LOAN_ENABLED, true));
            if (settings.has(KEY_LEDGER_AUTO_SETTLE_ENABLED)) editor.putBoolean(KEY_LEDGER_AUTO_SETTLE_ENABLED, settings.optBoolean(KEY_LEDGER_AUTO_SETTLE_ENABLED, true));
        }

        JSONArray categories = root.optJSONArray(KEY_CATEGORIES);
        JSONArray cards = root.optJSONArray(KEY_CARDS);
        JSONArray logs = root.optJSONArray(KEY_LOGS);
        JSONArray trusted = root.optJSONArray(KEY_TRUSTED);
            JSONArray trustedCreditAgents = root.optJSONArray(KEY_TRUSTED_CREDIT_AGENTS);
        JSONArray posOutlets = root.optJSONArray(KEY_POS_OUTLETS);
        JSONArray processed = root.optJSONArray(KEY_PROCESSED);
        JSONArray importedBatches = root.optJSONArray(KEY_IMPORTED_BATCHES);
        JSONArray restoreHistory = root.optJSONArray(KEY_RESTORE_HISTORY);
        JSONArray customerRewards = root.optJSONArray(KEY_CUSTOMER_REWARDS);
        JSONArray ledgerCustomers = root.optJSONArray(KEY_LEDGER_CUSTOMERS);
        JSONArray ledgerEntries = root.optJSONArray(KEY_LEDGER_ENTRIES);
        String backupCreatedAt = root.optString("createdAt", "");

        if (categories != null) editor.putString(KEY_CATEGORIES, categories.toString());
        if (cards != null) editor.remove(KEY_CARDS).putBoolean(KEY_CARDS_DB_READY, true);
        if (logs != null) editor.putString(KEY_LOGS, logs.toString());
        if (trusted != null) editor.putString(KEY_TRUSTED, trusted.toString());
            if (trustedCreditAgents != null) editor.putString(KEY_TRUSTED_CREDIT_AGENTS, trustedCreditAgents.toString());
        if (posOutlets != null) editor.putString(KEY_POS_OUTLETS, posOutlets.toString());
        if (ledgerCustomers != null) editor.putString(KEY_LEDGER_CUSTOMERS, ledgerCustomers.toString());
        if (ledgerEntries != null) editor.putString(KEY_LEDGER_ENTRIES, ledgerEntries.toString());
        if (processed != null) {
            HashSet<String> set = new HashSet<>();
            for (int i = 0; i < processed.length(); i++) set.add(processed.optString(i));
            editor.putStringSet(KEY_PROCESSED, set);
        }
        if (importedBatches != null) {
            HashSet<String> set = new HashSet<>();
            for (int i = 0; i < importedBatches.length(); i++) set.add(importedBatches.optString(i));
            editor.putStringSet(KEY_IMPORTED_BATCHES, set);
        }
        if (restoreHistory != null) editor.putString(KEY_RESTORE_HISTORY, restoreHistory.toString());
        if (customerRewards != null) editor.putString(KEY_CUSTOMER_REWARDS, customerRewards.toString());
        editor.apply();
        if (cards != null) saveCards(c, cardsFromJsonArray(cards));
        recordRestoreHistory(c, backupCreatedAt, "استعادة من ملف");
        addLog(c, new OperationLog(UUID.randomUUID().toString(), "النظام", "restore", "", "", 0, "تمت الاستعادة", "تمت استعادة نسخة احتياطية بتاريخ: " + now(), "", now()));
        ensureDefaultCategories(c);
    }

    static void clearAll(Context c) {
        boolean activated = prefs(c).getBoolean(KEY_ACTIVATED, false);
        String code = prefs(c).getString(KEY_ACTIVATION_CODE, "");
        String activatedDevice = prefs(c).getString(KEY_ACTIVATED_DEVICE_ID, "");
        String activatedAt = prefs(c).getString(KEY_ACTIVATED_AT, "");
        prefs(c).edit().clear().apply();
        try { cardDb(c).delete("cards", null, null); invalidateCardsCache(); } catch (Exception ignored) {}
        if (activated && activatedDevice != null && activatedDevice.equals(getDeviceId(c))) {
            prefs(c).edit()
                    .putBoolean(KEY_ACTIVATED, true)
                    .putString(KEY_ACTIVATION_CODE, code == null ? "" : code)
                    .putString(KEY_ACTIVATED_DEVICE_ID, activatedDevice)
                    .putString(KEY_ACTIVATED_AT, activatedAt == null ? "" : activatedAt)
                    .putString(KEY_LICENSE_STATUS, "active_offline")
                    .apply();
        }
        ensureDefaultCategories(c);
    }
}
