package com.yanglinkui.grass;

import com.yanglinkui.grass.zone.RequestZone;
import com.yanglinkui.grass.zone.RequestZoneParser;

public class RequestZoneHolder {

    //可以使用FastThreadLocal, JDK的ThreadLocal性能稍慢
    private static ThreadLocal<RequestZoneHolder> INSTANCES = new ThreadLocal<RequestZoneHolder>();

    private final RequestZone requestZone;

    private int invokedCount = 0;

    RequestZoneHolder(RequestZone requestZone) {
        this.requestZone = requestZone;
    }

    /**
     * 初始化用
     * @param value
     * @return
     */
    public static RequestZoneHolder getInstance(String value) {
        RequestZone requestZone = RequestZoneParser.parse(value);
        RequestZoneHolder holder = new RequestZoneHolder(requestZone);
        INSTANCES.set(holder);

        return holder;
    }

    /**
     * 多线程中用(Callable, Runnable)
     * @param requestZone
     * @return
     */
    public static RequestZoneHolder getInstance(RequestZone requestZone) {
        RequestZoneHolder holder = new RequestZoneHolder(requestZone);
        INSTANCES.set(holder);

        return holder;
    }

    /**
     * 非初始化调用
     * @return
     */
    public static RequestZoneHolder getInstance() {
        RequestZoneHolder holder = INSTANCES.get();
        if (holder != null) {
            holder.invokedCount++;
        }

        return holder;
    }

    public RequestZone getRequestZone() {
        return requestZone;
    }

    public void destroy() {
        if (this.invokedCount == 0) {
            INSTANCES.remove();
        } else {
            this.invokedCount--;
        }
    }

    public void destroy(boolean forced) {
        if (forced) {
            INSTANCES.remove();
        } else {
            destroy();
        }
    }
}
