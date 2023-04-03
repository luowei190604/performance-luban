package com.lyc.performance.luban.agent.transfer;

import java.util.concurrent.atomic.AtomicBoolean;

public class TransferHolder {

    private static ThreadLocal<Boolean> transferHolder = new ThreadLocal<>();

    public static void setLocal(Boolean flag) {
        transferHolder.set(flag);
    }

    public static Boolean getLocal() {
        return transferHolder.get();
    }

    public static void clear() {
        transferHolder.remove();
    }

    private static volatile AtomicBoolean controlSwitch = new AtomicBoolean(true);

    public static void setControlSwitch() {
        boolean b = controlSwitch.get();
        controlSwitch.set(!b);
    }

    public static boolean getControlSwitch() {
        return controlSwitch.get();
    }

}
