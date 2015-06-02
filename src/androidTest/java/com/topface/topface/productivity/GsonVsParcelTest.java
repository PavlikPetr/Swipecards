package com.topface.topface.productivity;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.test.InstrumentationTestCase;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;

/**
 * Тест для проверки производительности Gson и Parcelable
 * Created by onikitin on 02.06.15.
 */
@SuppressWarnings("FieldCanBeLocal")
public class GsonVsParcelTest extends InstrumentationTestCase {

    private static final int OVER_NINE_THOUSAND = 9000;
    private static final String TEST_CONSTANT = "test";


    public void testGson() {
        long startSendingTime;
        long stopSendingTime;
        Context context = getInstrumentation().getTargetContext();
        Intent intent = new Intent(TEST_CONSTANT);
        addGsonObjectsToIntent(intent, OVER_NINE_THOUSAND);
        startSendingTime = System.currentTimeMillis();
        context.sendBroadcast(intent);
        stopSendingTime = System.currentTimeMillis();
        Debug.log(this, "sendingGsonDeltaTime " + (stopSendingTime - startSendingTime));
    }

    public void testParcelable() {
        long startSendingTime;
        long stopSendingTime;
        Context context = getInstrumentation().getTargetContext();
        Intent intent = new Intent(TEST_CONSTANT);
        addParcelableObjectsToIntent(intent, OVER_NINE_THOUSAND);
        startSendingTime = System.currentTimeMillis();
        context.sendBroadcast(intent);
        stopSendingTime = System.currentTimeMillis();
        Debug.log(this, "sendingParcelableDeltaTime " + (stopSendingTime - startSendingTime));
    }

    public void testParcelableIncrement() {
        long startSendingTime;
        long stopSendingTime;
        Context context = getInstrumentation().getTargetContext();
        Intent intent = new Intent(TEST_CONSTANT);
        for (int i = 100; i <= 1100; i += 100) {
            addParcelableObjectsToIntent(intent, i);
            startSendingTime = System.currentTimeMillis();
            context.sendBroadcast(intent);
            stopSendingTime = System.currentTimeMillis();
            Debug.log(this, "iter " + i / 100 + " sendingParcelableDeltaTime " + (stopSendingTime - startSendingTime));
        }
    }

    public void testGsonIncrement() {
        long startSendingTime;
        long stopSendingTime;
        Context context = getInstrumentation().getTargetContext();
        Intent intent = new Intent(TEST_CONSTANT);
        for (int i = 100; i <= 1100; i += 100) {
            addGsonObjectsToIntent(intent, i);
            startSendingTime = System.currentTimeMillis();
            context.sendBroadcast(intent);
            stopSendingTime = System.currentTimeMillis();
            Debug.log(this, "iter " + i / 100 + " sendingGsonDeltaTime " + (stopSendingTime - startSendingTime));
        }
    }

    private void addParcelableObjectsToIntent(Intent intent, int objectCount) {
        for (int i = 0; i < objectCount; i++) {
            intent.putExtra(TEST_CONSTANT + i, new TestData(i));
        }
    }

    private void addGsonObjectsToIntent(Intent intent, int objectCount) {
        for (int i = 0; i < objectCount; i++) {
            intent.putExtra(TEST_CONSTANT + i, new TestData1(i));
        }
    }

    private class TestData implements Parcelable {
        public int anInt = 0;
        public float aFloat = 0.1f;
        public long aLong = 10l;
        public double aDouble = .14;
        public byte aByte = 16;
        public boolean aBoolean = true;
        public Integer anIntObject = 1000;
        public Float aFloatObject = .2f;
        public Long aLongObject = 20l;
        public Double aDoubleObject = .9;
        public Byte aByteObject = 8;
        public Boolean aBooleanObject = false;
        public String string = "test";

        public TestData(int i) {
            this.anInt += i;
            this.aFloat += i;
            this.aLong += i;
            this.aDouble += i;
            this.anIntObject += i;
            this.aFloatObject += i;
            this.aLongObject += i;
            this.aDoubleObject += i;
            this.string += i;
        }

        public TestData(Parcel parcel) {
            anInt = parcel.readInt();
            aFloat = parcel.readFloat();
            aLong = parcel.readLong();
            aDouble = parcel.readDouble();
            aByte = parcel.readByte();
            aBoolean = parcel.readByte() > 0;
            anIntObject = parcel.readInt();
            aFloatObject = parcel.readFloat();
            aLongObject = parcel.readLong();
            aDoubleObject = parcel.readDouble();
            aByteObject = parcel.readByte();
            aBooleanObject = parcel.readByte() > 0;
            string = parcel.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(anInt);
            parcel.writeFloat(aFloat);
            parcel.writeLong(aLong);
            parcel.writeDouble(aDouble);
            parcel.writeByte(aByte);
            parcel.writeByte((byte) (aBoolean ? 1 : 0));
            parcel.writeInt(anIntObject);
            parcel.writeFloat(aFloatObject);
            parcel.writeLong(aLongObject);
            parcel.writeDouble(aDoubleObject);
            parcel.writeByte(aByteObject);
            parcel.writeByte((byte) (aBooleanObject ? 1 : 0));
            parcel.writeString(string);
        }

        public final Parcelable.Creator<TestData> CREATOR = new Parcelable.Creator<TestData>() {

            @Override
            public TestData[] newArray(int size) {
                return new TestData[size];
            }

            @Override
            public TestData createFromParcel(Parcel parcel) {
                return new TestData(parcel);
            }
        };
    }

    @SuppressWarnings("unused")
    private class TestData1 implements Parcelable {
        public int anInt = 0;
        public float aFloat = 0.1f;
        public long aLong = 10l;
        public double aDouble = .14;
        public byte aByte = 16;
        public boolean aBoolean = true;
        public Integer anIntObject = 1000;
        public Float aFloatObject = .2f;
        public Long aLongObject = 20l;
        public Double aDoubleObject = .9;
        public Byte aByteObject = 8;
        public Boolean aBooleanObject = false;
        public String string = "test";

        public TestData1(int i) {
            this.anInt += i;
            this.aFloat += i;
            this.aLong += i;
            this.aDouble += i;
            this.anIntObject += i;
            this.aFloatObject += i;
            this.aLongObject += i;
            this.aDoubleObject += i;
            this.string += i;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            String s = JsonUtils.toJson(this);
            parcel.writeString(s);
        }

        transient public final Parcelable.Creator<TestData1> CREATOR = new Parcelable.Creator<TestData1>() {

            @Override
            public TestData1[] newArray(int size) {
                return new TestData1[size];
            }

            @Override
            public TestData1 createFromParcel(Parcel parcel) {
                return JsonUtils.fromJson(parcel.readString(), TestData1.class);
            }
        };
    }
}
