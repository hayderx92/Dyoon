package com.hayder.sijilduyun;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Sends a file straight into a specific WhatsApp chat (by phone number). */
@CapacitorPlugin(name = "WaShare")
public class WaSharePlugin extends Plugin {

    private boolean installed(String pkg) {
        try { getContext().getPackageManager().getPackageInfo(pkg, 0); return true; }
        catch (PackageManager.NameNotFoundException e) { return false; }
    }

    @PluginMethod
    public void available(PluginCall call) {
        JSObject r = new JSObject();
        r.put("whatsapp", installed("com.whatsapp"));
        r.put("business", installed("com.whatsapp.w4b"));
        call.resolve(r);
    }

    @PluginMethod
    public void sendFile(PluginCall call) {
        String path = call.getString("path");      // file:// uri or absolute path
        String phone = call.getString("phone");    // international digits, e.g. 9647701234567
        String mime = call.getString("mime", "application/pdf");
        String text = call.getString("text", "");
        String prefer = call.getString("prefer", "whatsapp"); // whatsapp | business
        if (path == null || phone == null || phone.isEmpty()) { call.reject("missing path/phone"); return; }

        File file = new File(path.startsWith("file://") ? Uri.parse(path).getPath() : path);
        Uri uri;
        try {
            uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
        } catch (Exception e) { call.reject("file error: " + e.getMessage()); return; }

        List<String> order = new ArrayList<>();
        if ("business".equals(prefer)) { order.add("com.whatsapp.w4b"); order.add("com.whatsapp"); }
        else { order.add("com.whatsapp"); order.add("com.whatsapp.w4b"); }

        for (String pkg : order) {
            if (!installed(pkg)) continue;
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType(mime);
            i.setPackage(pkg);
            i.putExtra(Intent.EXTRA_STREAM, uri);
            if (!text.isEmpty()) i.putExtra(Intent.EXTRA_TEXT, text);
            i.putExtra("jid", phone + "@s.whatsapp.net");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                getContext().startActivity(i);
                JSObject r = new JSObject(); r.put("app", pkg); call.resolve(r);
                return;
            } catch (ActivityNotFoundException ignored) { }
        }
        call.reject("whatsapp not installed", "NO_WHATSAPP");
    }
}
