package com.jeonlog.exhibition_recommender.common.metric;

import java.time.LocalDate;

public final class MetricKeys {

    private MetricKeys() {}

    public static String counter(Action action, String type, Object id) {
        return "count:%s:%s:%s".formatted(action.name().toLowerCase(), type, id);
    }

    public static String rank(Action action, String type, LocalDate date) {
        return "rank:%s:%s:%s".formatted(action.name().toLowerCase(), type, date);
    }

    public static String dau(LocalDate date) {
        return "dau:" + date;
    }

    public static String hourDist(LocalDate date) {
        return "hour_dist:" + date;
    }

    public static String onlineUsers() {
        return "online_users";
    }
}
