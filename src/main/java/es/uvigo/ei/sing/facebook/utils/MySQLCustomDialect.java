package es.uvigo.ei.sing.facebook.utils;

import org.hibernate.dialect.MySQL8Dialect;

public class MySQLCustomDialect extends MySQL8Dialect {
    @Override
    public String getTableTypeString() {
        // Force the encoding to utf8mb4 for every table
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin";
    }
}
