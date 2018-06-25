package com.sqber.filemgr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;

@Component //自动注入
@ConfigurationProperties(prefix = "datasource.mysql")
public class MySQLDataSourceConfig extends HikariConfig{

}
