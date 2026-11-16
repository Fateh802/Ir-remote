package com.bala.customtvremote;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.ConsumerIrManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
    private ConsumerIrManager ir;
    private TextView status, hardware;
    private android.content.SharedPreferences prefs;
    private static final int PICK_PROFILE = 71;
    private static final String[] NUMS = {"1","2","3","4","5","6","7","8","9","0"};
    private final int BG = Color.rgb(9,14,24), PANEL = Color.rgb(20,29,44), BLUE = Color.rgb(0,151,220), CYAN = Color.rgb(38,190,255), WHITE = Color.rgb(239,245,252), MUTED = Color.rgb(147,164,185);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("ir_profile", MODE_PRIVATE);
        ir = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
        buildUi();
        refreshHardware();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); t.setGravity(Gravity.CENTER); return t;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER); return l; }
    private LinearLayout.LayoutParams lp(int w, int h, float weight) { return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h), weight); }
    private void gap(LinearLayout parent, int h) { View v = new View(this); parent.addView(v, lp(1,h,0)); }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout root = column(); root.setPadding(dp(16),dp(16),dp(16),dp(24)); scroll.addView(root);
        LinearLayout head = row();
        LinearLayout titleCol = column();
        TextView title = text("CUSTOM TV REMOTE",20,WHITE,true); title.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        TextView sub = text("INFRARED CONTROL  •  OFFLINE",10,MUTED,true); sub.setGravity(Gravity.LEFT);
        titleCol.addView(title); titleCol.addView(sub); head.addView(titleCol,new LinearLayout.LayoutParams(0,-2,1));
        TextView setup = text("⚙  SETUP",12,CYAN,true); setup.setPadding(dp(12),dp(10),dp(12),dp(10)); setup.setBackground(shape(PANEL,12)); setup.setOnClickListener(v -> showSetup()); head.addView(setup); root.addView(head);
        gap(root,14);
        LinearLayout statusCard = column(); statusCard.setPadding(dp(12),dp(10),dp(12),dp(10)); statusCard.setBackground(shape(PANEL,14));
        hardware = text("Checking infrared hardware…",12,WHITE,true); hardware.setGravity(Gravity.LEFT);
        status = text("Unconfigured buttons are disabled until you add their real IR codes.",11,MUTED,false); status.setGravity(Gravity.LEFT);
        statusCard.addView(hardware); statusCard.addView(status); root.addView(statusCard);
        gap(root,16);

        addSection(root,"POWER & INPUT");
        LinearLayout top = row(); addKey(top,"POWER","⏻",true,1); addKey(top,"SOURCE","SOURCE",false,1); root.addView(top);
        LinearLayout inputs = row(); addKey(inputs,"USB","USB",false,1); addKey(inputs,"MEDIA","MEDIA",false,1); addKey(inputs,"HDMI","HDMI",false,1); root.addView(inputs);
        gap(root,12); addSection(root,"NUMBER PAD");
        String[][] digits={{"1","2","3"},{"4","5","6"},{"7","8","9"},{"-","0","↵"}};
        for(String[] line:digits){ LinearLayout r=row(); for(String n:line) addKey(r,n,n.equals("-")?"—":n.equals("↵")?"ENTER":n,false,1); root.addView(r); }
        gap(root,12); addSection(root,"VOLUME & CHANNEL");
        LinearLayout vc=row();
        LinearLayout vol=column(); addKey(vol,"VOL_UP","＋",true,0); addKey(vol,"MUTE","MUTE",false,0); addKey(vol,"VOL_DOWN","－",true,0); vc.addView(vol,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout ch=column(); addKey(ch,"CH_UP","▲  P",true,0); addKey(ch,"ASPECT","ASPECT",false,0); addKey(ch,"CH_DOWN","▼  P",true,0); vc.addView(ch,new LinearLayout.LayoutParams(0,-2,1)); root.addView(vc);
        gap(root,12); addSection(root,"PICTURE & SOUND");
        LinearLayout modes=row(); addKey(modes,"P_MODE","P.MODE",false,1); addKey(modes,"S_MODE","S.MODE",false,1); addKey(modes,"INFO","INFO  ⓘ",false,1); root.addView(modes);
        gap(root,12); addSection(root,"NAVIGATION");
        LinearLayout nav=column(); LinearLayout n1=row(); addKey(n1,"UP","▲",true,1); nav.addView(n1);
        LinearLayout n2=row(); addKey(n2,"LEFT","◀",true,1); addKey(n2,"OK","OK",true,1); addKey(n2,"RIGHT","▶",true,1); nav.addView(n2);
        LinearLayout n3=row(); addKey(n3,"MENU","MENU",false,1); addKey(n3,"DOWN","▼",true,1); addKey(n3,"EXIT","EXIT",false,1); nav.addView(n3); root.addView(nav);
        gap(root,12); addSection(root,"COLOUR KEYS");
        LinearLayout colors=row(); addKey(colors,"RED","●",false,1); addKey(colors,"GREEN","●",false,1); addKey(colors,"YELLOW","●",false,1); addKey(colors,"BLUE","●",false,1); root.addView(colors);
        gap(root,12); addSection(root,"SMART TV APPS");
        LinearLayout apps1=row(); addKey(apps1,"HOTSTAR","hotstar",false,1); addKey(apps1,"NETFLIX","NETFLIX",false,1); addKey(apps1,"YOUTUBE","YouTube",false,1); root.addView(apps1);
        LinearLayout apps2=row(); addKey(apps2,"AMAZON","amazon",false,1); addKey(apps2,"APP","APP",false,1); addKey(apps2,"M_CAST","M.CAST",false,1); root.addView(apps2);
        gap(root,12); addSection(root,"MEDIA PLAYBACK");
        LinearLayout media1=row(); addKey(media1,"REWIND","◀◀",true,1); addKey(media1,"PAUSE","Ⅱ",true,1); addKey(media1,"FORWARD","▶▶",true,1); root.addView(media1);
        LinearLayout media2=row(); addKey(media2,"RECORD","●",true,1); addKey(media2,"PLAY","▶",true,1); addKey(media2,"STOP","■",true,1); root.addView(media2);
        gap(root,18);
        TextView note=text("Tip: tap a programmed key to transmit. Long-press any key to edit its code. Aim the top edge of your phone at the TV.",11,MUTED,false); note.setPadding(dp(8),dp(8),dp(8),dp(8)); root.addView(note);
        setContentView(scroll);
    }

    private void addSection(LinearLayout root,String s){ TextView t=text(s,10,CYAN,true); t.setGravity(Gravity.LEFT); t.setPadding(dp(3),dp(5),0,dp(5)); root.addView(t); }
    private void addKey(LinearLayout parent,String id,String label,boolean primary,float weight){
        TextView b=text(label, label.length()>7?11:15, WHITE, true); b.setMinHeight(dp(48)); b.setPadding(dp(5),dp(8),dp(5),dp(8));
        boolean configured=prefs.contains("code_"+id) || prefs.contains("raw_"+id);
        b.setBackground(shape(configured?(primary?BLUE:Color.rgb(31,54,77)):Color.rgb(27,36,51),12));
        b.setAlpha(configured?1f:.72f);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(52),weight); p.setMargins(dp(3),dp(3),dp(3),dp(3)); parent.addView(b,p);
        b.setOnClickListener(v -> transmit(id)); b.setOnLongClickListener(v -> { editCode(id,label); return true; });
    }

    private void refreshHardware(){
        boolean feature=getPackageManager().hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR);
        boolean available=false;
        try { available=ir!=null && ir.hasIrEmitter(); } catch(Exception ignored) {}
        if(available){
            StringBuilder ranges=new StringBuilder();
            try { ConsumerIrManager.CarrierFrequencyRange[] rs=ir.getCarrierFrequencies(); if(rs!=null) for(ConsumerIrManager.CarrierFrequencyRange r:rs) ranges.append(r.getMinFrequency()).append("–").append(r.getMaxFrequency()).append(" Hz; "); } catch(Exception ignored){}
            hardware.setText("IR TRANSMITTER: AVAILABLE  •  "+(ranges.length()>0?ranges:"frequency ranges not reported"));
            status.setText("Add the codes from your working remote app. Long-press a button to configure it.");
        } else {
            hardware.setText("IR TRANSMITTER: NOT EXPOSED TO THIS APP");
            status.setText("Android feature="+feature+". The OnePlus IR Remote may use an OEM service. If this stays unavailable, this app needs the compatible OEM interface or an external IR device.");
        }
    }

    private void transmit(String id){
        if(ir==null || !ir.hasIrEmitter()){ toast("Android IR transmitter is unavailable to this app."); return; }
        try {
            String raw=prefs.getString("raw_"+id,null);
            if(raw!=null && !raw.trim().isEmpty()){
                String[] bits=raw.trim().split("[,\\s]+"); int[] pattern=new int[bits.length];
                for(int i=0;i<bits.length;i++){ pattern[i]=Integer.parseInt(bits[i]); if(pattern[i]<=0 || pattern[i]>100000) throw new IllegalArgumentException("Pulse values must be 1–100000 microseconds"); }
                int freq=prefs.getInt("freq_"+id,38000); ir.transmit(freq,pattern); toast(id+" sent"); return;
            }
            String hex=prefs.getString("code_"+id,null);
            if(hex==null){ toast("No code saved for "+id+". Long-press this button to configure it."); return; }
            int code=(int)Long.parseLong(hex.replace("0x","").replace("0X",""),16);
            int[] pattern=nec(code); ir.transmit(38000,pattern); toast(id+" sent (NEC)");
        } catch(Exception e){ toast("Could not send "+id+": "+e.getMessage()); }
    }

    // NEC: 38 kHz carrier; alternating mark/space durations in microseconds.
    private int[] nec(int code){
        ArrayList<Integer> p=new ArrayList<>(); p.add(9000); p.add(4500);
        for(int i=0;i<32;i++){ p.add(560); p.add(((code>>>i)&1)==1?1690:560); }
        p.add(560); int[] a=new int[p.size()]; for(int i=0;i<a.length;i++) a[i]=p.get(i); return a;
    }

    private void editCode(String id,String label){
        LinearLayout box=column(); box.setPadding(dp(8),dp(4),dp(8),dp(4));
        TextView info=text("Enter one NEC 32-bit hexadecimal code OR a raw alternating mark/space pattern. Do not guess codes.",12,MUTED,false); info.setGravity(Gravity.LEFT); box.addView(info);
        EditText hex=new EditText(this); hex.setSingleLine(true); hex.setHint("NEC code e.g. 20DF10EF (hex)"); hex.setText(prefs.getString("code_"+id,"")); box.addView(hex);
        EditText freq=new EditText(this); freq.setSingleLine(true); freq.setInputType(2); freq.setHint("Carrier frequency (default 38000 Hz)"); freq.setText(String.valueOf(prefs.getInt("freq_"+id,38000))); box.addView(freq);
        EditText raw=new EditText(this); raw.setMinLines(2); raw.setGravity(Gravity.TOP); raw.setHint("Raw pattern: 9000,4500,560,560,... (microseconds)"); raw.setText(prefs.getString("raw_"+id,"")); box.addView(raw);
        new AlertDialog.Builder(this).setTitle("Configure: "+label).setView(box)
            .setPositiveButton("SAVE",(d,w)->{
                String h=hex.getText().toString().trim().replace("0x","").replace("0X",""); String r=raw.getText().toString().trim();
                try {
                    if(!h.isEmpty()){ if(!h.matches("(?i)[0-9a-f]{1,8}")) throw new Exception("NEC code must be up to 8 hex digits"); prefs.edit().putString("code_"+id,h).remove("raw_"+id).putInt("freq_"+id,38000).apply(); }
                    else if(!r.isEmpty()){ int f=Integer.parseInt(freq.getText().toString().trim()); if(f<20000||f>60000) throw new Exception("Frequency must be 20000–60000 Hz"); String[] a=r.split("[,\\s]+"); if(a.length<4||a.length>500) throw new Exception("Raw pattern must contain 4–500 durations"); for(String x:a){int n=Integer.parseInt(x);if(n<=0||n>100000)throw new Exception("Pulse durations must be 1–100000");} prefs.edit().putString("raw_"+id,r).remove("code_"+id).putInt("freq_"+id,f).apply(); }
                    else prefs.edit().remove("code_"+id).remove("raw_"+id).apply();
                    buildUi(); refreshHardware(); toast("Saved "+id);
                }catch(Exception e){toast("Not saved: "+e.getMessage());}
            }).setNeutralButton("CLEAR",(d,w)->{prefs.edit().remove("code_"+id).remove("raw_"+id).apply();buildUi();refreshHardware();})
            .setNegativeButton("CANCEL",null).show();
    }

    private void showSetup(){
        String[] options={"Check IR hardware","Import profile JSON","Export profile JSON","About / instructions"};
        new AlertDialog.Builder(this).setTitle("Remote setup").setItems(options,(d,which)->{
            if(which==0) refreshHardware();
            if(which==1){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_PROFILE);}
            if(which==2) exportProfile();
            if(which==3) new AlertDialog.Builder(this).setTitle("About").setMessage("Custom TV Remote 1.0\n\nThis app sends NEC or raw IR patterns using Android ConsumerIrManager. It does not contain your TV's learned codes. Transfer codes from your known-working remote profile; unconfigured buttons intentionally do not transmit.").setPositiveButton("OK",null).show();
        }).show();
    }

    private void exportProfile(){
        try{
            JSONObject root=new JSONObject(), buttons=new JSONObject();
            Map<String,?> all=prefs.getAll();
            for(String k:all.keySet()) if(k.startsWith("code_")||k.startsWith("raw_")){
                String id=k.substring(5); JSONObject item=buttons.optJSONObject(id); if(item==null){item=new JSONObject();buttons.put(id,item);} item.put(k.startsWith("code_")?"nec":"raw",all.get(k)); item.put("frequency",prefs.getInt("freq_"+id,38000));
            }
            root.put("format","custom-tv-remote-v1"); root.put("buttons",buttons);
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"custom_tv_remote_profile.json");startActivityForResult(i,72);
            pendingExport=root.toString(2);
        }catch(Exception e){toast("Export error: "+e.getMessage());}
    }
    private String pendingExport;
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
        try{
            if(req==PICK_PROFILE){InputStream in=getContentResolver().openInputStream(data.getData());ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)out.write(buf,0,n);in.close();JSONObject root=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));if(!"custom-tv-remote-v1".equals(root.optString("format")))throw new Exception("Unsupported profile format");JSONObject buttons=root.getJSONObject("buttons");android.content.SharedPreferences.Editor ed=prefs.edit();Iterator<String> it=buttons.keys();while(it.hasNext()){String id=it.next();JSONObject item=buttons.getJSONObject(id);if(item.has("nec"))ed.putString("code_"+id,item.getString("nec"));if(item.has("raw"))ed.putString("raw_"+id,item.getString("raw"));ed.putInt("freq_"+id,item.optInt("frequency",38000));}ed.apply();buildUi();refreshHardware();toast("Profile imported");}
            else if(req==72 && pendingExport!=null){OutputStream o=getContentResolver().openOutputStream(data.getData());o.write(pendingExport.getBytes(StandardCharsets.UTF_8));o.close();toast("Profile exported");pendingExport=null;}
        }catch(Exception e){toast("Profile error: "+e.getMessage());}
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
