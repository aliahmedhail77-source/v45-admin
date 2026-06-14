package com.fourunet.pro;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

class MikroTikApiClient implements Closeable {
    static class HotspotUser {
        String name;
        String password;
        String profile;
        String comment;
        boolean disabled;
    }



    static class UserManagerUser {
        String username;
        boolean disabled;
    }

    ArrayList<UserManagerUser> getUserManagerUsers() throws Exception {
        try {
            return getUserManagerUsersAt("/user-manager/user/print");
        } catch (Exception v7) {
            try {
                return getUserManagerUsersAt("/tool/user-manager/user/print");
            } catch (Exception v6) {
                throw new Exception("فشل قراءة مستخدمي User Manager. تأكد أن User Manager مثبت ومفعل. آخر خطأ: " + v6.getMessage());
            }
        }
    }

    private ArrayList<UserManagerUser> getUserManagerUsersAt(String path) throws Exception {
        writeSentence(new String[]{
                path,
                "=.proplist=username,name,disabled"
        });
        ArrayList<ArrayList<String>> reply = readReply();
        String error = extractTrap(reply);
        if (error != null) throw new Exception(error);
        ArrayList<UserManagerUser> users = new ArrayList<>();
        for (ArrayList<String> sentence : reply) {
            if (sentence.isEmpty() || !"!re".equals(sentence.get(0))) continue;
            HashMap<String, String> map = mapFromSentence(sentence);
            UserManagerUser u = new UserManagerUser();
            u.username = valueOr(map, "username", valueOr(map, "name", ""));
            u.disabled = "true".equalsIgnoreCase(valueOr(map, "disabled", "false"));
            if (u.username != null && !u.username.trim().isEmpty()) users.add(u);
        }
        return users;
    }

    ArrayList<String> getUserManagerProfiles() throws Exception {
        try {
            return getProfilesAt("/user-manager/profile/print");
        } catch (Exception v7) {
            try {
                return getProfilesAt("/tool/user-manager/profile/print");
            } catch (Exception v6) {
                throw new Exception("فشل قراءة بروفايلات User Manager. تأكد من وجود بروفايلات في User Manager. آخر خطأ: " + v6.getMessage());
            }
        }
    }

    private ArrayList<String> getProfilesAt(String path) throws Exception {
        writeSentence(new String[]{path, "=.proplist=name"});
        ArrayList<ArrayList<String>> reply = readReply();
        String error = extractTrap(reply);
        if (error != null) throw new Exception(error);
        ArrayList<String> profiles = new ArrayList<>();
        for (ArrayList<String> sentence : reply) {
            if (sentence.isEmpty() || !"!re".equals(sentence.get(0))) continue;
            HashMap<String, String> map = mapFromSentence(sentence);
            String name = valueOr(map, "name", "").trim();
            if (!name.isEmpty()) profiles.add(name);
        }
        return profiles;
    }

    void addUserManagerNumericUser(String username, String profile, String customer, String comment) throws Exception {
        addUserManagerNumericUser(username, username, profile, customer, comment);
    }

    void addUserManagerNumericUser(String username, String password, String profile, String customer, String comment) throws Exception {
        String cleanUser = username == null ? "" : username.trim();
        String cleanPass = password == null ? "" : password.trim();
        String cleanProfile = profile == null ? "" : profile.trim();
        String cleanCustomer = (customer == null || customer.trim().isEmpty()) ? "admin" : customer.trim();
        if (cleanUser.isEmpty()) throw new Exception("اسم المستخدم فارغ");
        if (cleanProfile.isEmpty()) throw new Exception("اسم البروفايل فارغ");

        Exception v7Error = null;
        try {
            addUserManagerUserV7(cleanUser, cleanPass, cleanProfile, comment);
            return;
        } catch (Exception e) {
            v7Error = e;
        }

        try {
            addUserManagerUserV6(cleanUser, cleanPass, cleanProfile, cleanCustomer, comment);
        } catch (Exception v6Error) {
            throw new Exception("فشل إنشاء المستخدم في User Manager. RouterOS v7: " + v7Error.getMessage() + " | RouterOS v6: " + v6Error.getMessage());
        }
    }

    private void addUserManagerUserV7(String username, String password, String profile, String comment) throws Exception {
        ArrayList<String> add = new ArrayList<>();
        add.add("/user-manager/user/add");
        add.add("=username=" + username);
        add.add("=password=" + (password == null ? "" : password));
        add.add("=disabled=no");
        if (comment != null && !comment.trim().isEmpty()) add.add("=comment=" + comment.trim());
        try {
            runCommand(add);
        } catch (Exception e) {
            removeWordsStartingWith(add, "=comment=");
            runCommand(add);
        }
        activateUserManagerProfileV7(username, profile);
    }

    private void activateUserManagerProfileV7(String username, String profile) throws Exception {
        ArrayList<Exception> errors = new ArrayList<>();
        ArrayList<String> cmd1 = new ArrayList<>();
        cmd1.add("/user-manager/user/create-and-activate-profile");
        cmd1.add("=numbers=" + username);
        cmd1.add("=profile=" + profile);
        try { runCommand(cmd1); return; } catch (Exception e) { errors.add(e); }

        ArrayList<String> cmd2 = new ArrayList<>();
        cmd2.add("/user-manager/user/create-and-activate-profile");
        cmd2.add("=user=" + username);
        cmd2.add("=profile=" + profile);
        try { runCommand(cmd2); return; } catch (Exception e) { errors.add(e); }

        throw new Exception(errors.isEmpty() ? "فشل ربط البروفايل" : errors.get(errors.size() - 1).getMessage());
    }

    private void addUserManagerUserV6(String username, String password, String profile, String customer, String comment) throws Exception {
        ArrayList<String> add = new ArrayList<>();
        add.add("/tool/user-manager/user/add");
        add.add("=customer=" + customer);
        add.add("=username=" + username);
        add.add("=password=" + (password == null ? "" : password));
        add.add("=disabled=no");
        if (comment != null && !comment.trim().isEmpty()) add.add("=comment=" + comment.trim());
        try {
            runCommand(add);
        } catch (Exception e) {
            removeWordsStartingWith(add, "=comment=");
            runCommand(add);
        }

        ArrayList<String> activate = new ArrayList<>();
        activate.add("/tool/user-manager/user/create-and-activate-profile");
        activate.add("=customer=" + customer);
        activate.add("=numbers=" + username);
        activate.add("=profile=" + profile);
        runCommand(activate);
    }


    private void removeWordsStartingWith(ArrayList<String> words, String prefix) {
        for (int i = words.size() - 1; i >= 0; i--) {
            String w = words.get(i);
            if (w != null && w.startsWith(prefix)) words.remove(i);
        }
    }

    private void runCommand(ArrayList<String> words) throws Exception {
        writeSentence(words.toArray(new String[0]));
        ArrayList<ArrayList<String>> reply = readReply();
        String error = extractTrap(reply);
        if (error != null) throw new Exception(error);
        if (!containsDoneWithoutError(reply)) throw new Exception(extractError(reply));
    }

    private HashMap<String, String> mapFromSentence(ArrayList<String> sentence) {
        HashMap<String, String> map = new HashMap<>();
        for (String word : sentence) {
            if (word.startsWith("=") && word.indexOf('=', 1) > 1) {
                int p = word.indexOf('=', 1);
                map.put(word.substring(1, p), word.substring(p + 1));
            }
        }
        return map;
    }

    private String valueOr(HashMap<String, String> map, String key, String fallback) {
        return map.containsKey(key) && map.get(key) != null ? map.get(key) : fallback;
    }

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    void connect(String host, int port, int timeoutMs) throws Exception {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeoutMs);
        socket.setSoTimeout(timeoutMs);
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    void login(String username, String password) throws Exception {
        // RouterOS 6.43+ supports direct login.
        writeSentence(new String[]{"/login", "=name=" + username, "=password=" + password});
        ArrayList<ArrayList<String>> direct = readReply();
        if (containsDoneWithoutError(direct)) return;

        // Old RouterOS challenge/response login.
        writeSentence(new String[]{"/login"});
        ArrayList<ArrayList<String>> challengeReply = readReply();
        String challenge = null;
        for (ArrayList<String> sentence : challengeReply) {
            for (String word : sentence) {
                if (word.startsWith("=ret=")) challenge = word.substring(5);
            }
        }
        if (challenge == null || challenge.isEmpty()) throw new Exception("فشل تسجيل الدخول إلى MikroTik");
        String response = oldLoginResponse(password, challenge);
        writeSentence(new String[]{"/login", "=name=" + username, "=response=" + response});
        ArrayList<ArrayList<String>> finalReply = readReply();
        if (!containsDoneWithoutError(finalReply)) throw new Exception(extractError(finalReply));
    }

    ArrayList<HotspotUser> getHotspotUsers(String profileFilter, int limit) throws Exception {
        writeSentence(new String[]{
                "/ip/hotspot/user/print",
                "=.proplist=name,password,profile,comment,disabled",
                "?disabled=false"
        });
        ArrayList<ArrayList<String>> reply = readReply();
        String error = extractTrap(reply);
        if (error != null) throw new Exception(error);

        String filter = profileFilter == null ? "" : profileFilter.trim().toLowerCase();
        ArrayList<HotspotUser> users = new ArrayList<>();
        for (ArrayList<String> sentence : reply) {
            if (sentence.isEmpty() || !"!re".equals(sentence.get(0))) continue;
            HashMap<String, String> map = new HashMap<>();
            for (String word : sentence) {
                if (word.startsWith("=") && word.indexOf('=', 1) > 1) {
                    int p = word.indexOf('=', 1);
                    map.put(word.substring(1, p), word.substring(p + 1));
                }
            }
            HotspotUser u = new HotspotUser();
            u.name = map.containsKey("name") ? map.get("name") : "";
            u.password = map.containsKey("password") ? map.get("password") : "";
            u.profile = map.containsKey("profile") ? map.get("profile") : "";
            u.comment = map.containsKey("comment") ? map.get("comment") : "";
            u.disabled = "true".equalsIgnoreCase(map.containsKey("disabled") ? map.get("disabled") : "false");
            if (u.name == null || u.name.trim().isEmpty()) continue;
            if (u.disabled) continue;
            if (!filter.isEmpty()) {
                String profile = u.profile == null ? "" : u.profile.toLowerCase();
                String comment = u.comment == null ? "" : u.comment.toLowerCase();
                if (!profile.contains(filter) && !comment.contains(filter)) continue;
            }
            users.add(u);
            if (limit > 0 && users.size() >= limit) break;
        }
        return users;
    }


    ArrayList<String> getHotspotProfiles() throws Exception {
        writeSentence(new String[]{
                "/ip/hotspot/user/profile/print",
                "=.proplist=name"
        });
        ArrayList<ArrayList<String>> reply = readReply();
        String error = extractTrap(reply);
        if (error != null) throw new Exception(error);
        ArrayList<String> profiles = new ArrayList<>();
        for (ArrayList<String> sentence : reply) {
            if (sentence.isEmpty() || !"!re".equals(sentence.get(0))) continue;
            for (String word : sentence) {
                if (word.startsWith("=name=")) {
                    String name = word.substring(6).trim();
                    if (!name.isEmpty()) profiles.add(name);
                }
            }
        }
        return profiles;
    }

    void addHotspotUser(String name, String password, String profile, String comment) throws Exception {
        ArrayList<String> words = new ArrayList<>();
        words.add("/ip/hotspot/user/add");
        words.add("=name=" + (name == null ? "" : name.trim()));
        words.add("=password=" + (password == null ? "" : password.trim()));
        if (profile != null && !profile.trim().isEmpty()) words.add("=profile=" + profile.trim());
        if (comment != null && !comment.trim().isEmpty()) words.add("=comment=" + comment.trim());
        words.add("=disabled=no");
        writeSentence(words.toArray(new String[0]));
        ArrayList<ArrayList<String>> reply = readReply();
        String error = extractTrap(reply);
        if (error != null) throw new Exception(error);
        if (!containsDoneWithoutError(reply)) throw new Exception(extractError(reply));
    }

    private boolean containsDoneWithoutError(ArrayList<ArrayList<String>> reply) {
        boolean done = false;
        for (ArrayList<String> sentence : reply) {
            if (!sentence.isEmpty() && "!trap".equals(sentence.get(0))) return false;
            if (!sentence.isEmpty() && "!done".equals(sentence.get(0))) done = true;
        }
        return done;
    }

    private String extractTrap(ArrayList<ArrayList<String>> reply) {
        for (ArrayList<String> sentence : reply) {
            if (!sentence.isEmpty() && "!trap".equals(sentence.get(0))) return extractError(reply);
        }
        return null;
    }

    private String extractError(ArrayList<ArrayList<String>> reply) {
        for (ArrayList<String> sentence : reply) {
            for (String word : sentence) {
                if (word.startsWith("=message=")) return word.substring(9);
            }
        }
        return "فشل الاتصال أو تسجيل الدخول إلى MikroTik";
    }

    private ArrayList<ArrayList<String>> readReply() throws Exception {
        ArrayList<ArrayList<String>> all = new ArrayList<>();
        while (true) {
            ArrayList<String> sentence = readSentence();
            all.add(sentence);
            if (!sentence.isEmpty() && "!done".equals(sentence.get(0))) break;
            if (!sentence.isEmpty() && "!fatal".equals(sentence.get(0))) break;
        }
        return all;
    }

    private void writeSentence(String[] words) throws Exception {
        for (String word : words) writeWord(word == null ? "" : word);
        writeWord("");
        out.flush();
    }

    private ArrayList<String> readSentence() throws Exception {
        ArrayList<String> sentence = new ArrayList<>();
        while (true) {
            String word = readWord();
            if (word.length() == 0) break;
            sentence.add(word);
        }
        return sentence;
    }

    private void writeWord(String word) throws Exception {
        byte[] data = word.getBytes(StandardCharsets.UTF_8);
        writeLength(data.length);
        if (data.length > 0) out.write(data);
    }

    private String readWord() throws Exception {
        int len = readLength();
        if (len == 0) return "";
        byte[] data = new byte[len];
        int off = 0;
        while (off < len) {
            int r = in.read(data, off, len - off);
            if (r < 0) throw new Exception("انقطع الاتصال مع MikroTik");
            off += r;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    private void writeLength(int len) throws Exception {
        if (len < 0x80) {
            out.write(len);
        } else if (len < 0x4000) {
            out.write((len >> 8) | 0x80);
            out.write(len & 0xFF);
        } else if (len < 0x200000) {
            out.write((len >> 16) | 0xC0);
            out.write((len >> 8) & 0xFF);
            out.write(len & 0xFF);
        } else if (len < 0x10000000) {
            out.write((len >> 24) | 0xE0);
            out.write((len >> 16) & 0xFF);
            out.write((len >> 8) & 0xFF);
            out.write(len & 0xFF);
        } else {
            out.write(0xF0);
            out.write((len >> 24) & 0xFF);
            out.write((len >> 16) & 0xFF);
            out.write((len >> 8) & 0xFF);
            out.write(len & 0xFF);
        }
    }

    private int readLength() throws Exception {
        int c = in.read();
        if (c < 0) throw new Exception("انقطع الاتصال مع MikroTik");
        if ((c & 0x80) == 0x00) return c;
        if ((c & 0xC0) == 0x80) return ((c & ~0xC0) << 8) + readByte();
        if ((c & 0xE0) == 0xC0) return ((c & ~0xE0) << 16) + (readByte() << 8) + readByte();
        if ((c & 0xF0) == 0xE0) return ((c & ~0xF0) << 24) + (readByte() << 16) + (readByte() << 8) + readByte();
        return (readByte() << 24) + (readByte() << 16) + (readByte() << 8) + readByte();
    }

    private int readByte() throws Exception {
        int b = in.read();
        if (b < 0) throw new Exception("انقطع الاتصال مع MikroTik");
        return b & 0xFF;
    }

    private String oldLoginResponse(String password, String challengeHex) throws Exception {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(0);
        data.write(password.getBytes(StandardCharsets.UTF_8));
        data.write(hexToBytes(challengeHex));
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data.toByteArray());
        return "00" + bytesToHex(digest);
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return out;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xFF));
        return sb.toString();
    }

    @Override
    public void close() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}
