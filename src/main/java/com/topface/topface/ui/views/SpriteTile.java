package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author maximo guerrero
 */
public class SpriteTile extends Drawable {
    private Bitmap tileSheet; //sprite tile sheet for all animations. rectangles are used to slip and only show parts of one bitmap
    private Hashtable animations; //all animation sequences for this sprite
    private String currentAnimation = "idle"; //current animation sequence
    private int currentFrame = 0; //current frame being played
    private int xpos = 0; // x position
    private int ypos = 0; // y position
    private Rect frameSize = new Rect();
    private int waitDelay = 0; // delay before the next frame

    private ColorFilter cf;
    private float mDensity;

    // Class contains Information about one frame
    private class FrameInfo {
        public Rect rect = new Rect();
        public int nextFrameDelay = 0;
    }

    //Class encapsulates all the data for an animations sequence. List for frames, animcation name, if the sequence will loop and collission info
    private class AnimationSequece {
        public ArrayList sequence;
        public Rect collisionRect;
        public boolean canLoop = false;
        @SuppressWarnings("unused")
        public String name = "idle";
    }

    //takes resource ids for bitmaps and xmlfiles
    public SpriteTile(int BitmapResourceId, int XmlAnimationResourceId, Context context) {
        loadSprite(BitmapResourceId, XmlAnimationResourceId, context);
    }

    //load bitmap and xml data
    public void loadSprite(int spriteid, int xmlid, Context context) {
        tileSheet = BitmapFactory.decodeResource(context.getResources(), spriteid);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDensity = metrics.density / 2;
//load the xml will all the frame animations into a hashtable
        XmlResourceParser xpp = context.getResources().getXml(xmlid);

        animations = new Hashtable();

        try {
            int eventType = xpp.getEventType();
            String animationname = "";
            AnimationSequece animationsequence = new AnimationSequece();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");

                } else if (eventType == XmlPullParser.END_DOCUMENT) {
                    System.out.println("End document");

                } else if (eventType == XmlPullParser.START_TAG) {
                    System.out.println("Start tag " + xpp.getName());
                    if (xpp.getName().toLowerCase().equals("animation")) {
                        animationname = xpp.getAttributeValue(null, "name");
                        animationsequence = new AnimationSequece();
                        animationsequence.name = animationname;
                        animationsequence.sequence = new ArrayList();
                        animationsequence.canLoop = xpp.getAttributeBooleanValue(null, "canLoop", false);
                    } else if (xpp.getName().toLowerCase().equals("framerect")) {
                        FrameInfo frameinfo = new FrameInfo();
                        Rect frame = new Rect();
                        frame.top = xpp.getAttributeIntValue(null, "top", 0);
                        frame.bottom = xpp.getAttributeIntValue(null, "bottom", 0);
                        frame.left = xpp.getAttributeIntValue(null, "left", 0);
                        frame.right = xpp.getAttributeIntValue(null, "right", 0);
                        frameinfo.rect = frame;
                        frameinfo.nextFrameDelay = xpp.getAttributeIntValue(null, "delayNextFrame", 0);
                        animationsequence.sequence.add(frameinfo);
                    } else if (xpp.getName().toLowerCase().equals("collisionrect")) {
                        Rect colrect = new Rect();
                        colrect.top = xpp.getAttributeIntValue(null, "top", 0);
                        colrect.bottom = xpp.getAttributeIntValue(null, "bottom", 0);
                        colrect.left = xpp.getAttributeIntValue(null, "left", 0);
                        colrect.right = xpp.getAttributeIntValue(null, "right", 0);
                        animationsequence.collisionRect = colrect;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().toLowerCase().equals("animation")) {
                        animations.put(animationname, animationsequence);
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    System.out.println("Text " + xpp.getText());

                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            Log.e("ERROR", "ERROR IN SPRITE TILE CODE:" + e.toString());
        }
        Rect firstFrameSize = new Rect(((FrameInfo) ((AnimationSequece) animations.get(currentAnimation)).sequence.get(currentFrame)).rect);
        frameSize.bottom = (int) (firstFrameSize.bottom * mDensity);
        frameSize.top = (int) (firstFrameSize.top * mDensity);
        frameSize.left = (int) (firstFrameSize.left * mDensity);
        frameSize.right = (int) (firstFrameSize.right * mDensity);
        System.out.println("Sprite Loaded ");
    }

    //Draw sprite onto screen
    @Override
    public void draw(Canvas canvas) {
        try {
            FrameInfo frameinfo = (FrameInfo) ((AnimationSequece) animations.get(currentAnimation)).sequence.get(currentFrame);
            Rect rclip = new Rect(frameinfo.rect);

            rclip.set((int) (mDensity * rclip.left), (int) (rclip.top * mDensity), (int) (rclip.right * mDensity), (int) (rclip.bottom * mDensity));

            Rect dest = new Rect(getXpos(), getYpos(), getXpos() + (rclip.right - rclip.left),
                    getYpos() + (rclip.bottom - rclip.top));
//            frameSize = dest;
            if (cf != null) {
//color filter code here

            }
            canvas.drawBitmap(tileSheet, rclip, dest, null);
            update(); //after drawing update the frame counter
        } catch (Exception e) {
            Log.e("ERROR", "ERROR IN SPRITE TILE CODE:" + e.toString() + e.getStackTrace().toString());
        }

    }

    @Override
    public int getOpacity() {
        return 100;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        this.cf = cf;
    }

    //updates the frame counter to the next frame
    public void update() {
        if (waitDelay == 0)//if done waiting
        {
//set current frame back to the first because looping is possible
            if (((AnimationSequece) animations.get(currentAnimation)).canLoop && currentFrame == ((AnimationSequece) animations.get(currentAnimation)).sequence.size() - 1)
                currentFrame = 0;
            else {
                currentFrame++; //go to next frame

                FrameInfo frameinfo = (FrameInfo) ((AnimationSequece) animations.get(currentAnimation)).sequence.get(currentFrame);
                waitDelay = frameinfo.nextFrameDelay; //set delaytime for the next frame
            }
        } else {
            waitDelay--; //wait for delay to expire
        }

    }

    //has this sprite collided with a rect
    public boolean hasCollided(Rect rect) {
        AnimationSequece as = (AnimationSequece) animations.get(currentAnimation);
        if (rect.right < as.collisionRect.left)
            return false;
        if (rect.left > as.collisionRect.right)
            return false;

        if (rect.top > as.collisionRect.bottom)
            return false;
        if (rect.bottom < as.collisionRect.top)
            return false;

        return true;
    }

    //has animation finished playing, returns true on a animaiton that can loop for ever
    public boolean hasAnimationFinished() {
        AnimationSequece as = (AnimationSequece) animations.get(currentAnimation);
        if (currentFrame == as.sequence.size() - 1 && !as.canLoop)
            return true;

        return false;
    }

    public void setXpos(int xpos) {
        this.xpos = xpos;
    }

    public int getXpos() {
        return xpos;
    }

    public void setYpos(int ypos) {
        this.ypos = ypos;
    }

    public int getYpos() {
        return ypos;
    }

    public Rect getFrameSize() {
        return frameSize;
    }
}