package org.fluentjdbc.api.mapper;

import org.fluentjdbc.api.query.Mapper;

public class Mappers {
    private static final Mapper<Integer> singleInteger = (rs) -> { return rs.getInt(1); };
    private static final Mapper<Long> singleLong = (rs) -> { return rs.getLong(1); };
    private static final Mapper<String> singleString = (rs) -> { return rs.getString(1); };
  
    public static Mapper<Integer> singleInteger() {
        return singleInteger;
    }
    
    public static Mapper<Long> singleLong() {
        return singleLong;
    }
    
    public static Mapper<String> singleString() {
        return singleString;
    }
}
