package com.castleport.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import java.io.InputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

/** Independent technical prototype. No Castle Crashers game code or assets. */
public final class MainActivity extends Activity {
    private static final int PICK_PAK = 19;
    private GameView game;
    private String pakStatus = "PAK: not selected";
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        game = new GameView();
        setContentView(game);
    }
    @Override protected void onResume() { super.onResume(); if (game != null) game.start(); }
    @Override protected void onPause() { if(game != null) game.stop(); super.onPause(); }
    private void pickPak() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i,PICK_PAK);
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if (request != PICK_PAK || result != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if(uri == null) return;
        try (InputStream in = getContentResolver().openInputStream(uri);
             ZipInputStream zip = new ZipInputStream(in)) {
            int count = 0; String example = ""; ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                if(count == 0) example = entry.getName();
                count++;
                zip.closeEntry();
            }
            pakStatus = "PAK: " + count + " entries" + (count > 0 ? " (" + example + ")" : "");
        } catch(Exception e) { pakStatus = "PAK error: " + e.getClass().getSimpleName(); }
    }
    private final class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        private final Paint p = new Paint(3);
        private Thread loop;
        private volatile boolean running;
        private float x = 210, y = 300, vx = 0, vy = 0;
        private boolean left, right, up, down, attack;
        private float attackTime = 0;
        private long lastNs;
        private float fps;
        private final float W = 960, H = 540;
        GameView() { super(MainActivity.this); getHolder().addCallback(this); setFocusable(true); }
        @Override public void surfaceCreated(SurfaceHolder h) { start(); }
        @Override public void surfaceChanged(SurfaceHolder h,int format,int width,int height) { }
        @Override public void surfaceDestroyed(SurfaceHolder h) { stop(); }
        synchronized void start() {
            if(running || !getHolder().getSurface().isValid()) return;
            running = true; lastNs = System.nanoTime();
            loop = new Thread(this,"castle-port-demo-loop"); loop.start();
        }
        synchronized void stop() {
            running = false;
            Thread t = loop; loop = null;
            if (t != null && t != Thread.currentThread()) try {t.join(800);} catch(InterruptedException ignored) {Thread.currentThread().interrupt();}
        }
        @Override public void run() {
            while(running) {
                long now = System.nanoTime();
                float dt = Math.min((now-lastNs)/1_000_000_000f, 0.05f);
                lastNs = now;
                update(dt);
                Canvas c = null;
                try { c = getHolder().lockCanvas(); if(c!=null) drawFrame(c); }
                catch(Exception ignored) {}
                finally { if(c!=null) getHolder().unlockCanvasAndPost(c); }
                try { Thread.sleep(16); } catch(InterruptedException ignored) { break; }
            }
        }
        private void update(float dt) {
            float dx = (right ? 1:0)-(left ? 1:0);
            float dy = (down ? 1:0)-(up ? 1:0);
            float norm = dx != 0 && dy != 0 ? 0.7071068f : 1f;
            vx = dx*210*norm; vy = dy*210*norm;
            x = Math.max(35,Math.min(925,x+vx*dt));
            y = Math.max(155,Math.min(405,y+vy*dt));
            if(attack && attackTime<=0) attackTime=.22f;
            attackTime = Math.max(0,attackTime-dt);
            fps = fps*.9f+(dt>0?1f/dt:0)*.1f;
        }
        private void color(int value) {p.setColor(value);p.setStyle(Paint.Style.FILL);}
        private void rect(Canvas c,float l,float t,float r,float b,int clr){color(clr);c.drawRect(l,t,r,b,p);}
        private void oval(Canvas c,float l,float t,float r,float b,int clr){color(clr);c.drawOval(l,t,r,b,p);}
        private void text(Canvas c,String str,float xx,float yy,int size,int clr) {color(clr);p.setTextSize(size);c.drawText(str,xx,yy,p);}
        private void drawFrame(Canvas raw) {
            raw.drawColor(Color.rgb(16,25,45));
            raw.save(); raw.scale(raw.getWidth()/W,raw.getHeight()/H);
            rect(raw,0,105,960,448,Color.rgb(76,135,105));
            rect(raw,0,398,960,448,Color.rgb(58,105,77));
            rect(raw,0,448,960,540,Color.rgb(27,39,65));
            // Placeholder test subject, deliberately not a Castle Crashers character.
            oval(raw,x-33,y+33,x+33,y+48,Color.argb(90,0,0,0));
            rect(raw,x-22,y-38,x+22,y+32,Color.rgb(66,154,224));
            rect(raw,x-26,y-73,x+26,y-32,Color.rgb(199,224,238));
            rect(raw,x-19,y-57,x+19,y-46,Color.rgb(22,46,70));
            rect(raw,x-20,y+32,x-5,y+44,Color.rgb(35,50,70));
            rect(raw,x+5,y+32,x+20,y+44,Color.rgb(35,50,70));
            if(attackTime > 0) oval(raw,x+24,y-35,x+85,y+26,Color.argb(150,255,218,98));
            text(raw,"CASTLE PORT ENGINE - TECH DEMO v0.1",20,39,25,Color.WHITE);
            text(raw,"FPS: " + (int)fps + " | Frame loop + touch + collision",20,72,18,Color.WHITE);
            text(raw,pakStatus,20,96,15,Color.WHITE);
            // Control regions
            rect(raw,35,459,117,523,Color.rgb(71,92,129));
            rect(raw,130,459,212,523,Color.rgb(71,92,129));
            rect(raw,225,459,307,523,Color.rgb(71,92,129));
            rect(raw,320,459,402,523,Color.rgb(71,92,129));
            rect(raw,700,459,803,523,Color.rgb(148,79,65));
            rect(raw,822,459,945,523,Color.rgb(57,98,132));
            text(raw,"LEFT",42,499,20,Color.WHITE);
            text(raw,"RIGHT",137,499,20,Color.WHITE);
            text(raw,"UP",244,499,20,Color.WHITE);
            text(raw,"DOWN",327,499,20,Color.WHITE);
            text(raw,"ATTACK",714,499,20,Color.WHITE);
            text(raw,"OPEN PAK",829,499,17,Color.WHITE);
            raw.restore();
        }
        @Override public boolean onTouchEvent(MotionEvent event) {
            if(event.getActionMasked()==MotionEvent.ACTION_UP || event.getActionMasked()==MotionEvent.ACTION_CANCEL) {
                left=right=up=down=attack=false; return true;
            }
            if(event.getActionMasked()==MotionEvent.ACTION_DOWN || event.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked()==MotionEvent.ACTION_MOVE || event.getActionMasked()==MotionEvent.ACTION_POINTER_UP) {
                left=right=up=down=attack=false;
                boolean open = false;
                int skip = event.getActionMasked()==MotionEvent.ACTION_POINTER_UP?event.getActionIndex():-1;
                for(int i=0;i<event.getPointerCount();i++) {
                    if(i==skip) continue;
                    float tx=event.getX(i)*W/getWidth(),ty=event.getY(i)*H/getHeight();
                    if(ty<450) continue;
                    if(tx>=35 && tx<117)left=true;
                    else if(tx>=130 && tx<212)right=true;
                    else if(tx>=225 && tx<307)up=true;
                    else if(tx>=320 && tx<402)down=true;
                    else if(tx>=700 && tx<803)attack=true;
                    else if(tx>=822 && event.getActionMasked()==MotionEvent.ACTION_DOWN)open=true;
                }
                if(open) pickPak();
                return true;
            }
            return true;
        }
    }
}
