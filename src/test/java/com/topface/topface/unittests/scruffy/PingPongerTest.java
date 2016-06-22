package com.topface.topface.unittests.scruffy;

import com.topface.topface.requests.transport.scruffy.PingPonger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Тест для пинпонгера
 * Created by tiberal on 28.05.16.
 */
public class PingPongerTest {

    private PingPonger mPingPonger;

    @Before
    public void setUp() {
        System.out.println("SET_UP");
        mPingPonger = null;
    }

    @Test
    public void tesPingPongerReconnect() {
        final boolean[] wasReconnected = new boolean[1];
        mPingPonger = new PingPonger(new PingPonger.IRequestManagerInteractor() {
            @Override
            public void ping() {
                System.out.println("PING_RECONNECT");
                final Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(PingPonger.PING_TIME * 4);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mPingPonger.onPongReceived("PONG_RECONNECT");
                    }
                });
                thread.start();
            }

            @Override
            public void pong() {
                System.out.println("PONG_RECONNECT");
            }

            @Override
            public void reconnect() {
                System.out.println("RECONNECT");
                wasReconnected[0] = true;

            }
        });
        try {
            Thread.sleep(5 * PingPonger.PING_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(wasReconnected[0]);
        mPingPonger.unsubscribe();
    }

    @Test
    public void testPingPong() {
        final byte[] pongCount = {0};
        mPingPonger = new PingPonger(new PingPonger.IRequestManagerInteractor() {
            @Override
            public void ping() {
                System.out.println("PING");
                try {
                    Thread.sleep(PingPonger.PING_TIME / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mPingPonger.onPongReceived("PONG");
            }

            @Override
            public void pong() {
                System.out.println("PONG");
                pongCount[0]++;
            }

            @Override
            public void reconnect() {
                System.out.println("RECONNECT");

            }
        });
        try {
            Thread.sleep(3 * PingPonger.PING_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(pongCount[0] > 1);
        mPingPonger.unsubscribe();
    }
}
