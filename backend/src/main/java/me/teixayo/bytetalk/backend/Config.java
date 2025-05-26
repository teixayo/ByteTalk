package me.teixayo.bytetalk.backend;

import lombok.Data;

@Data
public class Config {

    private Database database;

    @Data
    public static class Database {
        private Redis redis;
        private MongoDB mongodb;
        private Elastic elastic;
    }

    @Data
    public static class Redis {
        private boolean toggle;
        private String address;
        private String port;
        private String user;
        private String password;
        private boolean ssl;
        private String prefix;
    }

    @Data
    public static class MongoDB {
        private boolean toggle;
        private String mongoUrl;
    }

    @Data
    public static class Elastic {
        private boolean toggle;
        private String address;
        private String port;
        private String user;
        private String password;
    }
}
